/* Copyright (C) 2004   db4objects Inc.   http://www.db4o.com */

package com.db4o;

import com.db4o.foundation.*;
import com.db4o.inside.callbacks.Callbacks;
import com.db4o.inside.classindex.ClassIndexStrategy;
import com.db4o.inside.marshall.*;
import com.db4o.inside.query.*;
import com.db4o.query.*;
import com.db4o.reflect.*;
import com.db4o.types.*;

/**
 * QQuery is the users hook on our graph.
 * 
 * A QQuery is defined by it's constraints.
 * 
 * NOTE: This is just a 'partial' base class to allow for variant implementations
 * in db4oj and db4ojdk1.2. It assumes that itself is an instance of QQuery
 * and should never be used explicitly.
 * 
 * @exclude
 */
public abstract class QQueryBase implements Unversioned {

    private static final transient IDGenerator i_orderingGenerator = new IDGenerator();

    transient Transaction i_trans;
    public Collection4 i_constraints = new Collection4();

    public QQuery i_parent;
    public String i_field;

    public QueryComparator _comparator;
    
    private final QQuery _this;
    
    protected QQueryBase() {
        // C/S only
    	_this = cast(this);
    }

    protected QQueryBase(Transaction a_trans, QQuery a_parent, String a_field) {
    	_this = cast(this);
        i_trans = a_trans;
        i_parent = a_parent;
        i_field = a_field;
    }

    void addConstraint(QCon a_constraint) {
        i_constraints.add(a_constraint);
    }

    private void addConstraint(Collection4 col, Object obj) {
        boolean found = false;
        Iterator4 j = iterateConstraints();
        while (j.moveNext()) {
            QCon existingConstraint = (QCon)j.current();
            boolean[] removeExisting = { false };
            QCon newConstraint = existingConstraint.shareParent(obj, removeExisting);
            if (newConstraint != null) {
                addConstraint(newConstraint);
                col.add(newConstraint);
                if (removeExisting[0]) {
                    removeConstraint(existingConstraint);
                }
                found = true;
            }
        }
        if (!found) {
            QConObject newConstraint = new QConObject(i_trans, null, null, obj);
            addConstraint(newConstraint);
            col.add(newConstraint);
        }
    }

    /**
	 * Search for slot that corresponds to class. <br>If not found add it.
	 * <br>Constrain it. <br>
	 */
    public Constraint constrain(Object example) {
        synchronized (streamLock()) {
            ReflectClass claxx = null;
            example = Platform4.getClassForType(example);
            
            Reflector reflector = i_trans.reflector(); 
            
            if(example instanceof ReflectClass){
            	claxx = (ReflectClass)example;
            }else{
            	if(example instanceof Class){
            		claxx = reflector.forClass((Class)example);
            	}
            }
             
            if (claxx != null) {
                
                if(claxx.equals(stream().i_handlers.ICLASS_OBJECT)){
                    return null;
                }
                
                Collection4 col = new Collection4();
                if (claxx.isInterface()) {
                    Collection4 classes = stream().classCollection().forInterface(claxx);
                    if (classes.size() == 0) {
                        QConClass qcc = new QConClass(i_trans, null, null, claxx);
                        addConstraint(qcc);
                        return qcc;
//                        return null;
                    }
                    Iterator4 i = classes.iterator();
                    Constraint constr = null;
                    while (i.moveNext()) {
                        YapClass yapClass = (YapClass)i.current();
                        ReflectClass yapClassClaxx = yapClass.classReflector();
                        if(yapClassClaxx != null){
                            if(! yapClassClaxx.isInterface()){
                                if(constr == null){
                                    constr = constrain(yapClassClaxx);
                                }else{
                                    constr = constr.or(constrain(yapClass.classReflector()));
                                }
                            }
                        }
                        
                    }
                    return constr;
                }

                Iterator4 constraintsIterator = iterateConstraints();
                while (constraintsIterator.moveNext()) {
                    QCon existingConstraint = (QConObject)constraintsIterator.current();
                    boolean[] removeExisting = { false };
                    QCon newConstraint =
                        existingConstraint.shareParentForClass(claxx, removeExisting);
                    if (newConstraint != null) {
                        addConstraint(newConstraint);
                        col.add(newConstraint);
                        if (removeExisting[0]) {
                            removeConstraint(existingConstraint);
                        }
                    }
                }
                if (col.size() == 0) {
                    QConClass qcc = new QConClass(i_trans, null, null, claxx);
                    addConstraint(qcc);
                    return qcc;
                }

                if (col.size() == 1) {
                    return firstConstraint(col);
                }
                Constraint[] constraintArray = new Constraint[col.size()];
                col.toArray(constraintArray);
                return new QConstraints(i_trans, constraintArray);
            }
            
            QConEvaluation eval = Platform4.evaluationCreate(i_trans, example);
			if (eval != null) {
                Iterator4 i = iterateConstraints();
                while (i.moveNext()) {
                    ((QCon)i.current()).addConstraint(eval);
                }
                return null;
            }
            Collection4 constraints = new Collection4();
            addConstraint(constraints, example);
            return toConstraint(constraints);
        }
    }

	private Constraint firstConstraint(Collection4 col) {
		Iterator4 i = col.iterator();
		i.moveNext();
		return (Constraint)i.current();
	}

    public Constraints constraints() {
        synchronized (streamLock()) {
            Constraint[] constraints = new Constraint[i_constraints.size()];
            i_constraints.toArray(constraints);
            return new QConstraints(i_trans, constraints);
        }
    }

    public Query descend(final String a_field) {
        synchronized (streamLock()) {
            final QQuery query = new QQuery(i_trans, _this, a_field);
            int[] run = { 1 };
            if (!descend1(query, a_field, run)) {

                // try to add unparented nodes on the second run,
                // if not added in the first run and a descendant
                // was not found

                if (run[0] == 1) {
                    run[0] = 2;
                    if (!descend1(query, a_field, run)) {
                        return null;
                    }
                }
            }
            return query;
        }
    }

    private boolean descend1(final QQuery query, final String a_field, int[] run) {
        final boolean[] foundClass = { false };
        if (run[0] == 2 || i_constraints.size() == 0) {

            // On the second run we are really creating a second independant
            // query network that is not joined to other higher level
			// constraints.
            // Let's see how this works out. We may need to join networks.

            run[0] = 0; // prevent a double run of this code

            final boolean[] anyClassCollected = { false };

            stream().classCollection().attachQueryNode(a_field, new Visitor4() {

                public void visit(Object obj) {

                    Object[] pair = ((Object[]) obj);
                    YapClass parentYc = (YapClass)pair[0];
                    YapField yf = (YapField)pair[1];
                    YapClass childYc = yf.getFieldYapClass(stream());

                    boolean take = true;

                    if (childYc instanceof YapClassAny) {
                        if (anyClassCollected[0]) {
                            take = false;
                        } else {
                            anyClassCollected[0] = true;
                        }
                    }

                    if (take) {

                        QConClass qcc =
                            new QConClass(
                                i_trans,
                                null,
                                yf.qField(i_trans),
                                parentYc.classReflector());
                        addConstraint(qcc);
                    }

                }

            });

        }
        Iterator4 i = iterateConstraints();
        while (i.moveNext()) {
            if (((QCon)i.current()).attach(query, a_field)) {
                foundClass[0] = true;
            }
        }
        return foundClass[0];
    }

    public ObjectSet execute() {
    	
    		Callbacks callbacks = stream().callbacks();
    		callbacks.onQueryStarted(cast(this));
    		
    	    QueryResult qresult = getQueryResult();
    	    
    	    callbacks.onQueryFinished(cast(this));
    	    
		return new ObjectSetFacade(qresult);
    }
    
    public QueryResult getQueryResult() {
    	synchronized (streamLock()) {
            
            YapStream stream = stream();
            
            if(i_constraints.size() == 0){
                QueryResultImpl res = stream.createQResult(i_trans);
                stream.getAll(i_trans, res);
                return res;
            }
            
			QueryResult result = classOnlyQuery();
			if(result != null) {
                result.reset();
				return result;
			}
	        QueryResultImpl qResult = stream().createQResult(i_trans);
	        execute1(qResult);
	        return qResult;
        }
    }

	protected YapStream stream() {
		return i_trans.stream();
	}

	private QueryResult classOnlyQuery() {
        
		if(i_constraints.size()!=1||_comparator!=null) {
			return null;
		}
		Constraint constr=firstConstraint(); 
		if(constr.getClass()!=QConClass.class) {
			return null;
		}
		QConClass clazzconstr=(QConClass)constr;
		YapClass clazz=clazzconstr.i_yapClass;
		if(clazz==null) {
			return null;
		}
		if(clazzconstr.hasChildren() || clazz.isArray()) {
			return null;
		}
        
        if (stream().isClient()) {
            long[] ids = clazz.getIDs(i_trans); 
            QResultClient resClient = new QResultClient(i_trans, ids.length);
            for (int i = 0; i < ids.length; i++) {
                resClient.add((int)ids[i]);
            }
            sort(resClient);
            return resClient;
        }
        
        if (!clazz.hasIndex()) {
			return null;
		}
		
		final QueryResultImpl resLocal = new QueryResultImpl(i_trans);
		final ClassIndexStrategy index = clazz.index();
		index.traverseAll(i_trans, new Visitor4() {
			public void visit(Object a_object) {
				resLocal.add(((Integer)a_object).intValue());
			}
		});
		sort(resLocal);
		return resLocal;
	}

	private Constraint firstConstraint() {
		Iterator4 iterator = iterateConstraints();
		if (!iterator.moveNext()) {
			return null;
		}
		return (Constraint)iterator.current();
	}

    void execute1(final QueryResultImpl result) {
        if (stream().isClient()) {
            marshall();
            ((YapClient)stream()).queryExecute(_this, result);
        } else {
            executeLocal(result);
        }
    }
    
    public static class CreateCandidateCollectionResult {
    	public final boolean checkDuplicates;
        public final boolean topLevel;
        public final List4 candidateCollection;
        
    	public CreateCandidateCollectionResult(List4 candidateCollection_, boolean checkDuplicates_, boolean topLevel_) {
    		candidateCollection = candidateCollection_;
    		topLevel = topLevel_;
    		checkDuplicates = checkDuplicates_;
		}
    }

    void executeLocal(final QueryResultImpl result) {
        
		CreateCandidateCollectionResult r = createCandidateCollection();
        
        boolean checkDuplicates = r.checkDuplicates;
        boolean topLevel = r.topLevel;
        List4 candidateCollection = r.candidateCollection;
        
        if (Debug.queries) {
        	Iterator4 i = iterateConstraints();
            while (i.moveNext()) {
                ((QCon)i.current()).log("");
            }
        }
        
        if (candidateCollection != null) {

        	Iterator4 i = new Iterator4Impl(candidateCollection);
            while (i.moveNext()) {
                ((QCandidates)i.current()).execute();
            }

            if (candidateCollection._next != null) {
                checkDuplicates = true;
            }

            if (checkDuplicates) {
                result.checkDuplicates();
            }

            final YapStream stream = stream();
            i = new Iterator4Impl(candidateCollection);
            while (i.moveNext()) {
                QCandidates candidates = (QCandidates)i.current();
                if (topLevel) {
                    candidates.traverse(result);
                } else {
                    QQueryBase q = this;
                    final Collection4 fieldPath = new Collection4();
                    while (q.i_parent != null) {
                        fieldPath.add(q.i_field);
                        q = q.i_parent;
                    }
                    candidates.traverse(new Visitor4() {
                        public void visit(Object a_object) {
                            QCandidate candidate = (QCandidate)a_object;
                            if (candidate.include()) {
                                TreeInt ids = new TreeInt(candidate._key);
                                final TreeInt[] idsNew = new TreeInt[1];
                                Iterator4 itPath = fieldPath.iterator();
                                while (itPath.moveNext()) {
                                    idsNew[0] = null;
                                    final String fieldName = (String) (itPath.current());
                                    if (ids != null) {
                                        ids.traverse(new Visitor4() {
                                            public void visit(Object treeInt) {
                                                int id = ((TreeInt)treeInt)._key;
                                                YapWriter reader =
                                                    stream.readWriterByID(i_trans, id);
                                                if (reader != null) {
                                                    ObjectHeader oh = new ObjectHeader(stream, reader);
                                                    idsNew[0] = oh.yapClass().collectFieldIDs(
                                                            oh._marshallerFamily,
                                                            oh._headerAttributes,
                                                            idsNew[0],
                                                            reader,
                                                            fieldName);
                                                }
                                            }
                                        });
                                    }
                                    ids = idsNew[0];
                                }
                                if(ids != null){
                                    ids.traverse(new Visitor4() {
	                                    public void visit(Object treeInt) {
	                                        result.addKeyCheckDuplicates(((TreeInt)treeInt)._key);
	                                    }
	                                });
                                }
                            }
                        }
                    });
                }
            }
        }
        sort(result);
        result.reset();
    }

	public CreateCandidateCollectionResult createCandidateCollection() {
		boolean checkDuplicates = false;
        boolean topLevel = true;
        List4 candidateCollection = null;
        Iterator4 i = iterateConstraints();
        while (i.moveNext()) {
            QCon qcon = (QCon)i.current();
            QCon old = qcon;            
            qcon = qcon.getRoot();
            if (qcon != old) {
                checkDuplicates = true;
                topLevel = false;
            }
            YapClass yc = qcon.getYapClass();
            if (yc == null) {
            	break;
            }
            candidateCollection = addConstraintToCandidateCollection(candidateCollection, qcon);
        }
		return new CreateCandidateCollectionResult(candidateCollection, checkDuplicates, topLevel);
	}

	private List4 addConstraintToCandidateCollection(List4 candidateCollection, QCon qcon) {
		
		if (candidateCollection != null) {
		    if (tryToAddToExistingCandidate(candidateCollection, qcon)) {
		    	return candidateCollection;
		    }
		}
		
		QCandidates candidates = new QCandidates(i_trans, qcon.getYapClass(), null);
		candidates.addConstraint(qcon);
		return new List4(candidateCollection, candidates);
	}

	private boolean tryToAddToExistingCandidate(List4 candidateCollection, QCon qcon) {
		Iterator4 j = new Iterator4Impl(candidateCollection);
		while (j.moveNext()) {
		    QCandidates candidates = (QCandidates)j.current();
		    if (candidates.tryAddConstraint(qcon)) {
		        return true;
		    }
		}
		return false;
	}

    public final Transaction getTransaction() {
        return i_trans;
    }
    
    Iterator4 iterateConstraints(){
        return i_constraints.iterator();
    }

    public Query orderAscending() {
        synchronized (streamLock()) {
            setOrdering(i_orderingGenerator.next());
            return _this;
        }
    }

    public Query orderDescending() {
        synchronized (streamLock()) {
            setOrdering(-i_orderingGenerator.next());
            return _this;
        }
    }

    private void setOrdering(final int ordering) {
        Iterator4 i = iterateConstraints();
        while (i.moveNext()) {
            ((QCon)i.current()).setOrdering(ordering);
        }
    }

    void marshall() {
        Iterator4 i = iterateConstraints();
        while (i.moveNext()) {
            ((QCon)i.current()).getRoot().marshall();
        }
    }

    void removeConstraint(QCon a_constraint) {
        i_constraints.remove(a_constraint);
    }

    void unmarshall(final Transaction a_trans) {
        i_trans = a_trans;
        Iterator4 i = iterateConstraints();
        while (i.moveNext()) {
            ((QCon)i.current()).unmarshall(a_trans);
        }
    }

    Constraint toConstraint(final Collection4 constraints) {       
        if (constraints.size() == 1) {
        	return firstConstraint();        	
        } else if (constraints.size() > 0) {
            Constraint[] constraintArray = new Constraint[constraints.size()];
            constraints.toArray(constraintArray);
            return new QConstraints(i_trans, constraintArray);
        }
        return null;
    }

    protected Object streamLock() {
        return stream().i_lock;
    }

	public Query sortBy(QueryComparator comparator) {
		_comparator=comparator;
		return _this;
	}
	
	private void sort(QueryResultImpl result) {
        if(_comparator!=null) {
        	result.sort(_comparator);
        }
	}
	
    // cheat emulating '(QQuery)this'
	private static QQuery cast(QQueryBase obj) {
		return (QQuery)obj;
	}
}
