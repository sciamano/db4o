package com.db4o.replication.hibernate;

import com.db4o.ObjectSet;
import com.db4o.ext.Db4oUUID;
import com.db4o.foundation.ObjectSetIteratorFacade;
import com.db4o.foundation.Visitor4;
import com.db4o.inside.replication.CollectionHandlerImpl;
import com.db4o.inside.replication.ReadonlyReplicationProviderSignature;
import com.db4o.inside.replication.ReplicationReference;
import com.db4o.inside.replication.ReplicationReferenceImpl;
import com.db4o.inside.replication.TestableReplicationProvider;
import com.db4o.inside.replication.TestableReplicationProviderInside;
import com.db4o.inside.traversal.CollectionFlattener;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;
import org.hibernate.event.EventListeners;
import org.hibernate.event.FlushEvent;
import org.hibernate.event.FlushEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;
import org.hibernate.mapping.PersistentClass;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Facade to a Hibernate-mapped database. During Instantiation of an instance of
 * this class, it will do <ol> <li> Registers {@link ReadonlyReplicationProviderSignature}
 * and {@link ReplicationRecord} with Hibernate. Hibernate generates
 * corresponding table if they do not exist. </li> <li> Each POJO is mapped to a
 * table in Hibernate, this Provider checks each table for the existence of
 * version, long part and the provider id columns. </li> <li> Creates a table to
 * hold the version/transaction number </li> </ol>
 *
 * @author Albert Kwan
 * @since 5.0
 */
public final class HibernateReplicationProviderImpl implements TestableReplicationProvider,
		HibernateReplicationProvider, TestableReplicationProviderInside {
	protected static final String IS_NULL = " IS NULL ";

	/**
	 * The Hibernate Configuration, for getting metadata of mapped classes.
	 */
	protected final Configuration _cfg;

	/**
	 * The Hibernate facade to a JDBC Connection. The connection is created when
	 * this provider is instantiated. The connection terminates at  {@link
	 * #clearAllReferences()} .
	 */
	protected final Session _session;

	/**
	 * The ReplicationProviderSignature of this  Hibernate-mapped database.
	 */
	protected MySignature _mySig;

	/**
	 * Allows the application to define units of work, while maintaining
	 * abstraction from the underlying transaction implementation (eg. JTA, JDBC).
	 * A transaction is associated with a Session and is usually instantiated by a
	 * call to Session.beginTransaction().
	 * <p/>
	 * A single session might span multiple transactions since the notion of a
	 * session (a conversation between the application and the datastore) is of
	 * coarser granularity than the notion of a transaction.
	 * <p/>
	 * However, it is intended that there be at most one uncommitted Transaction
	 * associated with a particular Session at any time.
	 */
	protected Transaction _transaction;

	/**
	 * Hibernate mapped classes, excluding  {@link ReadonlyReplicationProviderSignature}
	 * and {@link ReplicationRecord}.
	 */
	protected final Set _mappedClasses;

	/**
	 * The Signature of the peer in the current Transaction.
	 */
	protected PeerSignature _peerSignature;

	protected final Map _referencesByObject = new IdentityHashMap();

	/**
	 * The ReplicationRecord of {@link #_peerSignature}.
	 */
	protected ReplicationRecord _replicationRecord;

	/**
	 * Current transaction number = {@link #getLastReplicationVersion()} + 1. The
	 * minimum version number is 1, when this database is never replicated with
	 * other peers.
	 */
	protected long _currentVersion;

	/**
	 * The max(version numbers of all replication records).
	 */
	protected long _lastVersion;

	protected final String _name;

	/**
	 * Objects which meta data not yet updated.
	 */
	protected final Set _dirtyRefs = new HashSet();

	protected SessionFactory _sessionFactory;

	protected final CollectionFlattener _collectionHandler = new CollectionHandlerImpl();

	protected final Set _uuidsReplicatedInThisSession = new HashSet();

	protected boolean _inReplication = false;

	public HibernateReplicationProviderImpl(Configuration cfg) {
		this(cfg, null, null);
	}

	public HibernateReplicationProviderImpl(Configuration cfg, String name, byte[] signature) {
		this._cfg = cfg;
		this._cfg.setProperty("hibernate.format_sql", "true");
		this._cfg.setProperty("hibernate.use_sql_comments", "true");
		this._cfg.setProperty("hibernate.cache.provider_class", "org.hibernate.cache.NoCacheProvider");
		this._cfg.setProperty("hibernate.cache.use_query_cache", "false");
		this._cfg.setProperty("hibernate.cache.use_second_level_cache", "false");
		this._cfg.setProperty("hibernate.cglib.use_reflection_optimizer", "true");
		this._cfg.setProperty("hibernate.connection.release_mode", "after_transaction");

		Util.addMetaDataClasses(cfg);

		new MetaDataTablesCreator(cfg).createTables();

		EventListeners eventListeners = this._cfg.getEventListeners();

		//TODO use createFlushListeners()
		//FlushEventListener[] myFlushListeners = createFlushListeners(eventListeners);

		eventListeners.setFlushEventListeners(new FlushEventListener[]{new MyFlushEventListener()});
		eventListeners.setPostUpdateEventListeners(new PostUpdateEventListener[]{new MyPostUpdateEventListener()});
		_sessionFactory = this._cfg.buildSessionFactory();
		_session = _sessionFactory.openSession();
		_session.setFlushMode(FlushMode.ALWAYS);
		_mappedClasses = getMappedClasses();
		_name = name;

		if (signature == null) {
			initMySignature();
		} else {
			setSignature(signature);
		}

		_transaction = _session.beginTransaction();

	}

	public final ReadonlyReplicationProviderSignature getSignature() {
		return _mySig;
	}

	public final Object getMonitor() {
		return this;
	}

	public final void startReplicationTransaction(ReadonlyReplicationProviderSignature aPeerSignature) {
		ensureReplicationInActive();

		byte[] peerSigBytes = aPeerSignature.getBytes();

		if (Arrays.equals(peerSigBytes, getSignature().getBytes()))
			throw new RuntimeException("peerSigBytes must not equal to my own sig");

		_transaction.commit();
		_transaction = _session.beginTransaction();

		PeerSignature existingPeerSignature = getPeerSignature(peerSigBytes);
		if (existingPeerSignature == null) {
			this._peerSignature = new PeerSignature(peerSigBytes);
			_session.save(this._peerSignature);
			_session.flush();
			if (getPeerSignature(peerSigBytes) == null)
				throw new RuntimeException("Cannot insert existingPeerSignature");
			_replicationRecord = new ReplicationRecord();
			_replicationRecord.setPeerSignature(_peerSignature);
		} else {
			this._peerSignature = existingPeerSignature;
			_replicationRecord = getRecord(this._peerSignature);
		}

		_lastVersion = Util.getMaxVersion(_session.connection());
		_currentVersion = _lastVersion + 1;
		_inReplication = true;
	}

	public void syncVersionWithPeer(long version) {
		ensureReplicationActive();

		if (version < Constants.MIN_VERSION_NO)
			throw new RuntimeException("version must be great than " + Constants.MIN_VERSION_NO);

		_replicationRecord.setVersion(version);
		_session.saveOrUpdate(_replicationRecord);

		if (getRecord(_peerSignature).getVersion() != version)
			throw new RuntimeException("The version numbers of persisted record does not match the parameter");
	}

	public synchronized final void commitReplicationTransaction(long raisedDatabaseVersion) {
		ensureReplicationActive();
		commit();
		_uuidsReplicatedInThisSession.clear();
		_dirtyRefs.clear();
		_inReplication = false;
	}

	public synchronized final void rollbackReplication() {
		ensureReplicationActive();

		_transaction.rollback();
		_transaction = _session.beginTransaction();
		clearAllReferences();
		_dirtyRefs.clear();
		_uuidsReplicatedInThisSession.clear();
		_inReplication = false;
	}

	public final long getCurrentVersion() {
		ensureReplicationActive();

		return _currentVersion;
	}

	public final long getLastReplicationVersion() {
		ensureReplicationActive();

		return _lastVersion;
	}

	public final void storeReplica(Object obj) {
		ensureReplicationActive();

		ReplicationReference ref = getCachedReference(obj);
		if (ref == null) throw new RuntimeException("Reference should always be available before storeReplica");

		_uuidsReplicatedInThisSession.add(ref.uuid());

		//Hibernate does not treat Collection as 1st class object, so storing a Collection is no-op
		if (_collectionHandler.canHandle(obj)) return;

		_session.saveOrUpdate(obj);
		_dirtyRefs.add(ref);
	}

	public final void activate(Object object) {
		ensureReplicationActive();

		Hibernate.initialize(object);
	}

	public final ReplicationReference produceReference(Object obj, Object referencingObj, String fieldName) {
		ensureReplicationActive();

		ReplicationReference existing = getCachedReference(obj);

		if (existing != null) return existing;

		if (_collectionHandler.canHandle(obj)) {
			if (referencingObj == null) throw new NullPointerException("referencingObj cannot be null");
			if (fieldName == null) throw new NullPointerException("fieldName cannot be null");

			return produceCollectionReference(obj, referencingObj, fieldName);
		} else {
			return produceObjectReference(obj);
		}
	}

	public ReplicationReference referenceNewObject(Object obj, ReplicationReference counterpartReference,
			ReplicationReference referencingObjCounterPartRef, String fieldName) {
		ensureReplicationActive();

		if (obj == null) throw new NullPointerException("obj is null");
		if (counterpartReference == null) throw new NullPointerException("counterpartReference is null");

		if (_collectionHandler.canHandle(obj)) {
			if (referencingObjCounterPartRef == null)
				throw new NullPointerException("referencingObjCounterPartRef is null");
			if (fieldName == null) throw new NullPointerException("fieldName is null");
			return referenceClonedCollection(obj, counterpartReference, referencingObjCounterPartRef, fieldName);
		} else {
			Db4oUUID uuid = counterpartReference.uuid();
			long version = counterpartReference.version();

			return createReference(obj, uuid, version);
		}
	}

	public final ReplicationReference produceReferenceByUUID(final Db4oUUID uuid, Class hint) {
		ensureReplicationActive();

		if (uuid == null) throw new IllegalArgumentException("uuid cannot be null");
		if (hint == null) throw new IllegalArgumentException("hint cannot be null");

		if (_collectionHandler.canHandle(hint)) {
			return produceCollectionReferenceByUUID(uuid);
		} else {
			return produceObjectReferenceByUUID(uuid, hint);
		}
	}

	public final void clearAllReferences() {
		ensureReplicationActive();

		_referencesByObject.clear();
	}

	public final ObjectSet objectsChangedSinceLastReplication() {
		ensureReplicationActive();

		_session.flush();
		Set out = new HashSet();

		Set queriedTables = new HashSet();

		for (Iterator iterator = _mappedClasses.iterator(); iterator.hasNext();) {
			PersistentClass persistentClass = (PersistentClass) iterator.next();
			String tableName = persistentClass.getTable().getName();

			if (!queriedTables.contains(tableName)) {
				queriedTables.add(tableName);

				//Case 1 - Objects inserted to Db since last replication with any peers.
				out.addAll(getNewObjectsSinceLastReplication(persistentClass));

				//Case 2 - Objects updated since last replication with any peers.
				out.addAll(getChangedObjectsSinceLastReplication(persistentClass));
			}
		}
		return new ObjectSetIteratorFacade(out.iterator());
	}

	public final ObjectSet objectsChangedSinceLastReplication(Class clazz) {
		ensureReplicationActive();

		_session.flush();
		Set out = new HashSet();
		PersistentClass persistentClass = _cfg.getClassMapping(clazz.getName());
		if (persistentClass != null) {
			out.addAll(getNewObjectsSinceLastReplication(persistentClass));
			out.addAll(getChangedObjectsSinceLastReplication(persistentClass));
		}
		return new ObjectSetIteratorFacade(out.iterator());
	}

	public final boolean hasReplicationReferenceAlready(Object obj) {
		ensureReplicationActive();

		return getCachedReference(obj) != null;
	}

	public final void visitCachedReferences(Visitor4 visitor) {
		ensureReplicationActive();

		Iterator i = _referencesByObject.values().iterator();
		while (i.hasNext()) {
			visitor.visit(i.next());
		}
	}

	public boolean wasChangedSinceLastReplication(ReplicationReference reference) {
		ensureReplicationActive();
		if (_uuidsReplicatedInThisSession.contains(reference.uuid())) return false;
		return reference.version() > getLastReplicationVersion();
	}

	public void closeIfOpened() {
		_session.close();
		_sessionFactory.close();
	}

	public Session getSession() {
		return _session;
	}

	public final String getModifiedObjectCriterion() {
		ensureReplicationActive();

		return Db4oColumns.DB4O_VERSION + " > " + getLastReplicationVersion();
	}

	public void delete(Class clazz) {
		String className = clazz.getName();
		_session.createQuery("delete from " + className).executeUpdate();
	}

	public void commit() {
		_session.flush();
		_transaction.commit();
		_transaction = _session.beginTransaction();
	}

	public final ObjectSet getStoredObjects(Class aClass) {
		_session.flush();
		return new ObjectSetIteratorFacade(_session.createCriteria(aClass).list().iterator());
	}

	public final void storeNew(Object object) {
		_session.save(object);
	}

	public final void update(Object o) {
		_session.update(o);
		_session.flush();
	}

	public final String getName() {
		return _name;
	}

	protected ReplicationReference produceCollectionReference(Object obj, Object referencingObj, String fieldName) {
		final ReplicationReference refObjRef = produceReference(referencingObj, null, null);

		if (refObjRef == null) {
			return null;
		} else {
			return createRefForCollection(obj, refObjRef, fieldName, generateUuidLongPartSeqNo(), _currentVersion);
		}
	}

	protected ReplicationReference produceObjectReference(Object obj) {
		//System.out.println("produceObjectReference() obj = " + obj);
		if (!_session.contains(obj)) return null;

		String tableName = Util.getTableName(_cfg, obj.getClass());
		String pkColumn = Util.getPrimaryKeyColumnName(_cfg, obj);
		Serializable identifier = _session.getIdentifier(obj);

		String sql = "SELECT "
				+ Db4oColumns.DB4O_VERSION
				+ ", " + Db4oColumns.DB4O_UUID_LONG_PART
				+ ", " + ReplicationProviderSignature.SIGNATURE_ID_COLUMN_NAME
				+ " FROM " + tableName
				+ " where " + pkColumn + "=" + identifier;

		ResultSet rs = null;

		try {
			rs = _session.connection().createStatement().executeQuery(sql);

			if (!rs.next())
				return null;

			ReplicationReference out;

			long longPart = rs.getLong(2);
			if (longPart < Constants.MIN_SEQ_NO) {
				Db4oUUID uuid = new Db4oUUID(generateUuidLongPartSeqNo(), getMySig().getBytes());
				ReplicationReferenceImpl ref = new ReplicationReferenceImpl(obj, uuid, getLastReplicationVersion());
				storeReplicationMetaData(ref);
				out = createReference(obj, uuid, ref.version());
			} else {
				ReadonlyReplicationProviderSignature owner = getById(rs.getLong(3));
				Db4oUUID uuid = new Db4oUUID(rs.getLong(2), owner.getBytes());
				long version = rs.getLong(1);
				out = createReference(obj, uuid, version);
			}
			return out;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			Util.closeResultSet(rs);
		}
	}

	protected ReplicationReference referenceClonedCollection(Object obj, ReplicationReference counterpartReference,
			ReplicationReference referencingObjRef, String fieldName) {
		return createRefForCollection(obj, referencingObjRef, fieldName, counterpartReference.uuid().getLongPart(), counterpartReference.version());
	}

	protected ReplicationReference createRefForCollection(Object obj, ReplicationReference referencingObjRef,
			String fieldName, long uuidLong, long version) {
		final byte[] signaturePart = referencingObjRef.uuid().getSignaturePart();

		ReplicationComponentField rcf = produceReplicationComponentField(referencingObjRef.object().getClass().getName(), fieldName);
		ReplicationComponentIdentity rci = new ReplicationComponentIdentity();

		rci.setReferencingObjectField(rcf);
		rci.setReferencingObjectUuidLongPart(referencingObjRef.uuid().getLongPart());
		rci.setProvider(getProviderSignature(signaturePart));
		rci.setUuidLongPart(uuidLong);

		Db4oUUID uuid = new Db4oUUID(uuidLong, signaturePart);

		_session.save(rci);
		return createReference(obj, uuid, version);
	}

	protected ReplicationComponentField produceReplicationComponentField(String referencingObjectClassName,
			String referencingObjectFieldName) {
		Criteria criteria = _session.createCriteria(ReplicationComponentField.class);
		criteria.add(Restrictions.eq("referencingObjectClassName", referencingObjectClassName));
		criteria.add(Restrictions.eq("referencingObjectFieldName", referencingObjectFieldName));

		final List exisitings = criteria.list();
		int count = exisitings.size();

		if (count == 0) {
			ReplicationComponentField out = new ReplicationComponentField();
			out.setReferencingObjectClassName(referencingObjectClassName);
			out.setReferencingObjectFieldName(referencingObjectFieldName);
			_session.save(out);

			//Double-check, you know Hibernate sometimes fail to save an object.
			return produceReplicationComponentField(referencingObjectClassName, referencingObjectFieldName);
		} else if (count > 1) {
			throw new RuntimeException("Only one Record should exist for this peer");
		} else {
			return (ReplicationComponentField) exisitings.get(0);
		}
	}

	protected ReplicationReference produceObjectReferenceByUUID(Db4oUUID uuid, Class hint) {
		_session.flush();
		ReadonlyReplicationProviderSignature signature = getProviderSignature(uuid.getSignaturePart());

		final long sigId;

		if (signature == null) return null;
		else sigId = signature.getId();

		String alias = "whatever";

		String tableName = Util.getTableName(_cfg, hint);

		String sql = "SELECT {" + alias + ".*} FROM " + tableName + " " + alias
				+ " where " + Db4oColumns.DB4O_UUID_LONG_PART + "=" + uuid.getLongPart()
				+ " AND " + ReplicationProviderSignature.SIGNATURE_ID_COLUMN_NAME + "=" + sigId;
		SQLQuery sqlQuery = _session.createSQLQuery(sql);
		sqlQuery.addEntity(alias, hint);

		final List results = sqlQuery.list();

		final int rowCount = results.size();

		ReplicationReference out;
		if (rowCount == 0) {
			out = null;
		} else if (rowCount == 1) {
			final Object obj = results.get(0);

			ReplicationReference existing = getCachedReference(obj);
			if (existing != null) return existing;

			long version = Util.getVersion(_cfg, _session, obj);
			out = createReference(obj, uuid, version);
		} else {
			throw new RuntimeException("The object may either be found or not, it will never find more than one objects");
		}

		return out;
	}

	protected ReplicationReference produceCollectionReferenceByUUID(Db4oUUID uuid) {
		Criteria criteria = _session.createCriteria(ReplicationComponentIdentity.class);
		criteria.add(Restrictions.eq("uuidLongPart", new Long(uuid.getLongPart())));
		criteria.createCriteria("provider").add(Restrictions.eq("bytes", uuid.getSignaturePart()));

		final List exisitings = criteria.list();
		int count = exisitings.size();

		ReplicationReference out;
		if (count == 0) {
			out = null;
		} else if (count > 1) {
			throw new RuntimeException("Only one Record should exist for this peer");
		} else {
			Object obj = exisitings.get(0);
			ReplicationReference existing = getCachedReference(obj);
			if (existing != null) return existing;

			long version = Util.getVersion(_cfg, _session, obj);
			out = createReference(obj, uuid, version);
		}

		return out;
	}

	protected void setSignature(byte[] b) {
		//Idempotent
		if (_mySig != null) return;
		final Criteria criteria = _session.createCriteria(MySignature.class);
		final List firstResult = criteria.list();

		if (firstResult.size() == 1)
			_session.delete(firstResult.get(0));
		else if (firstResult.size() > 1)
			throw new RuntimeException("Number of MySignature should be either 0 or 1");

		_mySig = new MySignature(b);
		_session.save(_mySig);
		_session.flush();
	}

	protected Collection getNewObjectsSinceLastReplication(PersistentClass type) {
		String alias = "whatever";

		String tableName = type.getTable().getName();
		//dumpTable(tableName);
		String sql = "SELECT {" + alias + ".*} FROM " + tableName + " " + alias
				+ " where " + Db4oColumns.DB4O_UUID_LONG_PART + IS_NULL
				//+ " AND " + Db4oColumns.DB4O_VERSION + IS_NULL
				+ " AND " + ReplicationProviderSignature.SIGNATURE_ID_COLUMN_NAME + IS_NULL;
		//+ " AND class=" + type
		SQLQuery sqlQuery = _session.createSQLQuery(sql);
		sqlQuery.addEntity(alias, type.getMappedClass());

		List list = sqlQuery.list();
		generateReplicationMetaData(list);
		return list;
	}

	protected Collection getChangedObjectsSinceLastReplication(PersistentClass type) {
		String alias = "whatever";

		String tableName = type.getTable().getName();
		String sql = "SELECT {" + alias + ".*} FROM " + tableName + " " + alias
				+ " where " + Db4oColumns.DB4O_VERSION + ">" + _lastVersion;
		//+ " where " + Db4oColumns.DB4O_VERSION + ">" + lastVersion + " OR " + Db4oColumns.DB4O_VERSION + "=0";

		SQLQuery sqlQuery = _session.createSQLQuery(sql);
		sqlQuery.addEntity(alias, type.getMappedClass());

		return sqlQuery.list();
	}

	protected ReplicationProviderSignature getProviderSignature(byte[] signaturePart) {
		final List exisitingSigs = _session.createCriteria(ReadonlyReplicationProviderSignature.class)
				.add(Restrictions.eq(ReplicationProviderSignature.SIGNATURE_BYTE_ARRAY_COLUMN_NAME, signaturePart))
				.list();
		if (exisitingSigs.size() == 1)
			return (ReplicationProviderSignature) exisitingSigs.get(0);
		else if (exisitingSigs.size() == 0) return null;
		else
			throw new RuntimeException("result size = " + exisitingSigs.size() + ". It should be either 1 or 0");
	}

	protected void initMySignature() {
		final Criteria criteria = _session.createCriteria(MySignature.class);

		final List firstResult = criteria.list();
		final int mySigCount = firstResult.size();

		if (mySigCount < 1) {
			_mySig = MySignature.generateSignature();
			_session.save(_mySig);
		} else if (mySigCount == 1) {
			_mySig = (MySignature) firstResult.get(0);
		} else {
			throw new RuntimeException("Number of MySignature should be exactly 1, but i got " + mySigCount);
		}
	}

	protected static void sleep(int i, String s) {
		System.out.println(s);
		try {
			Thread.sleep(i * 1000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	protected PeerSignature getPeerSignature(byte[] bytes) {
		final List exisitingSigs = _session.createCriteria(PeerSignature.class)
				.add(Restrictions.eq(ReplicationProviderSignature.SIGNATURE_BYTE_ARRAY_COLUMN_NAME, bytes))
				.list();

		if (exisitingSigs.size() == 1)
			return (PeerSignature) exisitingSigs.get(0);
		else if (exisitingSigs.size() == 0) return null;
		else
			throw new RuntimeException("result size = " + exisitingSigs.size() + ". It should be either 1 or 0");
	}

	protected ReplicationRecord getRecord(PeerSignature peerSignature) {
		Criteria criteria = _session.createCriteria(ReplicationRecord.class).createCriteria("peerSignature").add(Restrictions.eq("id", new Long(peerSignature.getId())));

		final List exisitingRecords = criteria.list();
		int count = exisitingRecords.size();

		if (count == 0)
			throw new RuntimeException("Record not found. Hibernate was unable to persist the record in the last replication round");
		else if (count > 1)
			throw new RuntimeException("Only one Record should exist for this peer");
		else
			return (ReplicationRecord) exisitingRecords.get(0);
	}

	protected void generateReplicationMetaData(Collection newObjects) {
		for (Iterator iterator = newObjects.iterator(); iterator.hasNext();) {
			Object o = iterator.next();
			Db4oUUID uuid = new Db4oUUID(generateUuidLongPartSeqNo(), getMySig().getBytes());
			ReplicationReferenceImpl ref = new ReplicationReferenceImpl(o, uuid, _currentVersion);
			storeReplicationMetaData(ref);
		}
	}

	protected MySignature getMySig() {
		if (_mySig == null)
			initMySignature();
		return _mySig;
	}

	protected long generateUuidLongPartSeqNo() {

		Connection connection = _session.connection();
		Statement st = null;
		ResultSet rs = null;

		try {
			st = connection.createStatement();
			rs = st.executeQuery("SELECT count(*) FROM " + Constants.UUID_LONG_PART_SEQUENCE);
			rs.next();

			long rowCount = rs.getLong(1);
			if (rowCount == 0) {
				insertSeqNo(st);
			} else if (rowCount > 1) {
				st.executeQuery("DELETE FROM " + Constants.UUID_LONG_PART_SEQUENCE);
				insertSeqNo(st);
			}

			ResultSet rs2 = st.executeQuery("SELECT " + Constants.CURRENT_SEQ_NO + " FROM " + Constants.UUID_LONG_PART_SEQUENCE);
			rs2.next();
			long currSeqNo = rs2.getLong(1);
			long raised = currSeqNo + 1;

			st.execute("UPDATE " + Constants.UUID_LONG_PART_SEQUENCE + " SET " + Constants.CURRENT_SEQ_NO + " = " + raised);
			st.close();

			return currSeqNo;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			Util.closeResultSet(rs);
			Util.closeStatement(st);
		}

	}

	protected static void insertSeqNo(Statement st) throws SQLException {
		st.executeUpdate("INSERT INTO " + Constants.UUID_LONG_PART_SEQUENCE + " ( " + Constants.CURRENT_SEQ_NO + " ) VALUES ( " + Constants.MIN_SEQ_NO + " ) ");
	}

	protected ReplicationReference createReference(Object obj, Db4oUUID uuid, long version) {
		ReplicationReference result = new ReplicationReferenceImpl(obj, uuid, version);
		_referencesByObject.put(obj, result);
		return result;
	}

	protected ReadonlyReplicationProviderSignature getById(long sigId) {
		return (ReadonlyReplicationProviderSignature) _session.get(ReplicationProviderSignature.class, new Long(sigId));
	}

	protected void storeReplicationMetaData(ReplicationReference ref) {
		String tableName = Util.getTableName(_cfg, ref.object().getClass());
		String pkColumn = Util.getPrimaryKeyColumnName(_cfg, ref.object());
		Serializable identifier = _session.getIdentifier(ref.object());

		String sql = "UPDATE " + tableName + " SET " + Db4oColumns.DB4O_VERSION + "=?"
				+ ", " + Db4oColumns.DB4O_UUID_LONG_PART + "=?"
				+ ", " + ReplicationProviderSignature.SIGNATURE_ID_COLUMN_NAME + "=?"
				+ " WHERE " + pkColumn + " =?";

		PreparedStatement ps = null;
		try {
			ps = _session.connection().prepareStatement(sql);

			long refVer = ref.version();
			ps.setLong(1, refVer);
			ps.setLong(2, ref.uuid().getLongPart());
			ps.setLong(3, getProviderSignature(ref.uuid().getSignaturePart()).getId());
			ps.setObject(4, identifier);

			int affected = ps.executeUpdate();
			if (affected != 1) {
				throw new RuntimeException("Unable to update db4o columns");
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			Util.closePreparedStatement(ps);
		}
	}

	protected ReplicationReference getCachedReference(Object obj) {
		return (ReplicationReference) _referencesByObject.get(obj);
	}

	protected static String flattenBytes(byte[] b) {
		String out = "";
		for (int i = 0; i < b.length; i++) {
			out += ", " + b[i];
		}
		return out;
	}

	protected Set getMappedClasses() {
		Set out = new HashSet();
		Iterator classMappings = _cfg.getClassMappings();
		while (classMappings.hasNext()) {
			PersistentClass persistentClass = (PersistentClass) classMappings.next();
			Class claxx = persistentClass.getMappedClass();

			if (Util.skip(claxx))
				continue;

			out.add(persistentClass);
		}


		return out;
	}

	public final String toString() {
		return "name = " + _name + ", sig = " + flattenBytes(getMySig().getBytes());
	}

	protected void ensureReplicationActive() {
		if (!_inReplication)
			throw new UnsupportedOperationException("Method not supported because replication transaction is not active");
	}

	protected void ensureReplicationInActive() {
		if (_inReplication)
			throw new UnsupportedOperationException("Method not supported because replication transaction is active");
	}

	protected FlushEventListener[] createFlushListeners(EventListeners eventListeners) {
		FlushEventListener[] defaultFlushListeners = eventListeners.getFlushEventListeners();
		FlushEventListener[] myFlushListeners;

		if (defaultFlushListeners == null) {
			myFlushListeners = new FlushEventListener[]{new MyFlushEventListener()};
		} else {
			final int count = defaultFlushListeners.length;
			myFlushListeners = new FlushEventListener[count + 1];
			System.arraycopy(defaultFlushListeners, 0, myFlushListeners, 0, count);

			if (myFlushListeners[count] != null)
				throw new RuntimeException("bug");
			myFlushListeners[count] = new MyFlushEventListener();
		}
		return myFlushListeners;
	}

	final class MyFlushEventListener implements FlushEventListener {
		public final void onFlush(FlushEvent event) throws HibernateException {
			for (Iterator iterator = _dirtyRefs.iterator(); iterator.hasNext();) {
				ReplicationReference ref = (ReplicationReference) iterator.next();
				storeReplicationMetaData(ref);
			}
			_dirtyRefs.clear();
		}
	}

	final class MyPostUpdateEventListener implements PostUpdateEventListener {
		public final void onPostUpdate(PostUpdateEvent event) {
			synchronized (HibernateReplicationProviderImpl.this) {
				Object entity = event.getEntity();

				if (Util.skip(entity)) return;

				//TODO performance sucks, but this method is called when testing only.
				long newVer = Util.getMaxVersion(_session.connection()) + 1;
				Util.incrementObjectVersion(_session.connection(), event.getId(), newVer,
						Util.getTableName(_cfg, entity.getClass()), Util.getPrimaryKeyColumnName(_cfg, entity));
			}
		}
	}
}