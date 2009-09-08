/* Copyright (C) 2004 - 2007  Versant Inc.   http://www.db4o.com */

package com.db4o.cs.internal;

import java.io.IOException;

import com.db4o.*;
import com.db4o.config.Configuration;
import com.db4o.cs.internal.messages.*;
import com.db4o.events.Event4;
import com.db4o.ext.*;
import com.db4o.foundation.*;
import com.db4o.foundation.network.*;
import com.db4o.internal.*;
import com.db4o.internal.events.Event4Impl;
import com.db4o.internal.threading.ThreadPool4;

public class ObjectServerImpl implements ObjectServerEvents, ObjectServer, ExtObjectServer, Runnable {
	
	private static final int START_THREAD_WAIT_TIMEOUT = 5000;

	private final String _name;

	private ServerSocket4 _serverSocket;
	
	private int _port;

	private int i_threadIDGen = 1;

	private final Collection4 _dispatchers = new Collection4();

	private LocalObjectContainer _container;
	private ClientTransactionPool _transactionPool;

	private final Object _startupLock=new Object();
	
	private Config4Impl _config;
	
	private BlockingQueue _committedInfosQueue = new BlockingQueue();
	
	private CommittedCallbacksDispatcher _committedCallbacksDispatcher;
    
    private boolean _caresAboutCommitted;

	private final Socket4Factory _socketFactory;

	private final boolean _isEmbeddedServer;
	
	private final ClassInfoHelper _classInfoHelper = new ClassInfoHelper();
	
	private final Event4Impl<ClientConnectionEventArgs> _clientConnected = Event4Impl.newInstance();
	private final Event4Impl<ServerClosedEventArgs> _closed = Event4Impl.newInstance();
	
	public ObjectServerImpl(final LocalObjectContainer container, Socket4Factory socketFactory, int port) {
		this(container, socketFactory, (port < 0 ? 0 : port), port == 0);
	}
	
	private ObjectServerImpl(final LocalObjectContainer container, Socket4Factory socketFactory, int port, boolean isEmbeddedServer) {
		_isEmbeddedServer = isEmbeddedServer;
		_socketFactory = socketFactory;
		_container = container;
		_transactionPool = new ClientTransactionPool(container);
		_port = port;
		_config = _container.configImpl();
		_name = "db4o ServerSocket FILE: " + container.toString() + "  PORT:"+ _port;
		
		_container.setServer(true);	
		configureObjectServer();
		
		_container.classCollection().checkAllClassChanges();
		
		boolean ok = false;
		try {
			ensureLoadStaticClass();
			startCommittedCallbackThread(_committedInfosQueue);
			startServer();
			ok = true;
		} finally {
			if(!ok) {
				close();
			}
		}
	}

	private void startServer() {		
		if (isEmbeddedServer()) {
			return;
		}
		
		synchronized(_startupLock) {
			startServerSocket();
			startServerThread();
			boolean started=false;
			while(!started) {
				try {
					_startupLock.wait(START_THREAD_WAIT_TIMEOUT);
					started=true;
				}
				// not specialized to InterruptException for .NET conversion
				catch (Exception exc) {
				}
			}
		}
	}

	private void startServerThread() {
		synchronized(_startupLock) {
			threadPool().start(this);
		}
	}

	private ThreadPool4 threadPool() {
	    return _container.threadPool();
    }

	private void startServerSocket() {
		try {
			_serverSocket = _socketFactory.createServerSocket(_port);
			_port = _serverSocket.getLocalPort();
		} catch (IOException e) {
			throw new Db4oIOException(e);
		}
		_serverSocket.setSoTimeout(_config.timeoutServerSocket());
	}

	private boolean isEmbeddedServer() {
		return _isEmbeddedServer;
	}

	private void ensureLoadStaticClass() {
		_container.produceClassMetadata(_container._handlers.ICLASS_STATICCLASS);
	}

	private void configureObjectServer() {
		_config.callbacks(false);
		_config.isServer(true);
		// the minimum activation depth of com.db4o.User.class should be 1.
		// Otherwise, we may get null password.
		_config.objectClass(User.class).minimumActivationDepth(1);
	}

	public void backup(String path) throws IOException {
		_container.backup(path);
	}

	final void checkClosed() {
		if (_container == null) {
			Exceptions4.throwRuntimeException(Messages.CLOSED_OR_OPEN_FAILED, _name);
		}
		_container.checkClosed();
	}
	
	/**
	 * System.IDisposable.Dispose()
	 */
	public void dispose() {
		close();
	}

	public synchronized boolean close() {
		return close(ShutdownMode.NORMAL);
	}

	public synchronized boolean close(ShutdownMode mode) {
		try {
			closeServerSocket();
			stopCommittedCallbacksDispatcher();
			closeMessageDispatchers(mode);
			return closeFile(mode);
		}
		finally {
			triggerClosed();
		}
	}

	private void stopCommittedCallbacksDispatcher() {
		if(_committedCallbacksDispatcher != null){
			_committedCallbacksDispatcher.stop();
		}
	}

	private boolean closeFile(ShutdownMode mode) {
		if (_container != null) {
			_transactionPool.close(mode);
			_container = null;
		}
		return true;
	}

	private void closeMessageDispatchers(ShutdownMode mode) {
		Iterator4 i = iterateDispatchers();
		while (i.moveNext()) {
			try {
				((ServerMessageDispatcher) i.current()).close(mode);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		i = iterateDispatchers();
		while (i.moveNext()) {
			try {
				((ServerMessageDispatcher) i.current()).join();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Iterator4 iterateDispatchers() {
		synchronized (_dispatchers) {
			return new Collection4(_dispatchers).iterator();
		}
	}

	private void closeServerSocket() {
		try {
			if (_serverSocket != null) {
				_serverSocket.close();
			}
		} catch (Exception e) {
			if (Deploy.debug) {
				System.out
						.println("ObjectServer.close() ServerSocket failed to close.");
			}
		}
		_serverSocket = null;
	}

	public Configuration configure() {
		return _config;
	}

	public ExtObjectServer ext() {
		return this;
	}

	private ServerMessageDispatcherImpl findThread(int a_threadID) {
		synchronized (_dispatchers) {
			Iterator4 i = _dispatchers.iterator();
			while (i.moveNext()) {
				ServerMessageDispatcherImpl serverThread = (ServerMessageDispatcherImpl) i.current();
				if (serverThread._threadID == a_threadID) {
					return serverThread;
				}
			}
		}
		return null;
	}

	Transaction findTransaction(int threadID) {
		ServerMessageDispatcherImpl dispatcher = findThread(threadID);
		return (dispatcher == null ? null : dispatcher.getTransaction());
	}

	public synchronized void grantAccess(String userName, String password) {
		checkClosed();
		synchronized (_container.lock()) {
			User existing = getUser(userName);
			if (existing != null) {
				setPassword(existing, password);
			} else {
				addUser(userName, password);
			}
			_container.commit();
		}
	}

	private void addUser(String userName, String password) {
		_container.store(new User(userName, password));
	}

	private void setPassword(User existing, String password) {
		existing.password = password;
		_container.store(existing);
	}

	public User getUser(String userName) {
		final ObjectSet result = queryUsers(userName);
		if (!result.hasNext()) {
			return null;
		}
		return (User) result.next();
	}

	private ObjectSet queryUsers(String userName) {
		_container.showInternalClasses(true);
		try {
			return _container.queryByExample(new User(userName, null));
		} finally {
			_container.showInternalClasses(false);
		}
	}

	public ObjectContainer objectContainer() {
		return _container;
	}

	public synchronized ObjectContainer openClient() {
		checkClosed();
		synchronized (_container.lock()) {
		    return new ObjectContainerSession(_container);
		}
	}
	
	void removeThread(ServerMessageDispatcherImpl dispatcher) {
		synchronized (_dispatchers) {
			_dispatchers.remove(dispatcher);
            checkCaresAboutCommitted();
		}
	}

	public synchronized void revokeAccess(String userName) {
		checkClosed();
		synchronized (_container.lock()) {
			deleteUsers(userName);
			_container.commit();
		}
	}

	private void deleteUsers(String userName) {
		ObjectSet set = queryUsers(userName);
		while (set.hasNext()) {
			_container.delete(set.next());
		}
	}

	public void run() {
		setThreadName();
		logListeningOnPort();
		notifyThreadStarted();
		listen();
	}

	private void startCommittedCallbackThread(BlockingQueue committedInfosQueue) {
		if(isEmbeddedServer()) {
			return;
		}
		_committedCallbacksDispatcher = new CommittedCallbacksDispatcher(this, committedInfosQueue);
		threadPool().start(_committedCallbacksDispatcher);
	}

	private void setThreadName() {
		Thread.currentThread().setName(_name);
	}

	private void listen() {
		while (_serverSocket != null) {
			withEnvironment(new Runnable() { public void run() {
				try {
					ServerMessageDispatcherImpl messageDispatcher = new ServerMessageDispatcherImpl(ObjectServerImpl.this, new ClientTransactionHandle(_transactionPool),
							_serverSocket.accept(), newThreadId(), false, _container.lock());
					
					addServerMessageDispatcher(messageDispatcher);
					triggerClientConnected(messageDispatcher);
						
					threadPool().start(messageDispatcher);
				} catch (Exception e) {
					//e.printStackTrace();
				}
			}});								
		}
	}

	private void triggerClientConnected(ServerMessageDispatcher messageDispatcher) {
		_clientConnected.trigger(new ClientConnectionEventArgs(messageDispatcher));
    }

	private void triggerClosed() {
		_closed.trigger(new ServerClosedEventArgs());
    }


	private void notifyThreadStarted() {
		synchronized (_startupLock) {
			_startupLock.notifyAll();
		}
	}

	private void logListeningOnPort() {
		_container.logMsg(Messages.SERVER_LISTENING_ON_PORT, "" + _serverSocket.getLocalPort());
	}

	private int newThreadId() {
		return i_threadIDGen++;
	}

	private void addServerMessageDispatcher(ServerMessageDispatcher thread) {
		synchronized (_dispatchers) {
			_dispatchers.add(thread);
            checkCaresAboutCommitted();
		}
	}

	public void addCommittedInfoMsg(MCommittedInfo message) {
		_committedInfosQueue.add(message);			
	}
	
	public void broadcastMsg(Msg message, BroadcastFilter filter) {		
		Iterator4 i = iterateDispatchers();
		while(i.moveNext()){
			ServerMessageDispatcher dispatcher = (ServerMessageDispatcher) i.current();
			if(filter.accept(dispatcher)) {
				dispatcher.write(message);
			}
		}
	}
    
    public boolean caresAboutCommitted(){
        return _caresAboutCommitted;
    }
    
    public void checkCaresAboutCommitted(){
        _caresAboutCommitted = anyDispatcherCaresAboutCommitted();
    }

	private boolean anyDispatcherCaresAboutCommitted() {
        Iterator4 i = iterateDispatchers();
        while(i.moveNext()){
            ServerMessageDispatcher dispatcher = (ServerMessageDispatcher) i.current();
            if(dispatcher.caresAboutCommitted()){
                return true;
            }
        }
		return false;
	}

	public int port() {
		return _port;
	}
	
	public int clientCount(){
	    synchronized(_dispatchers){
	        return _dispatchers.size();
	    }
	}

	public ClassInfoHelper classInfoHelper() {
		return _classInfoHelper;
	}

	public Event4<ClientConnectionEventArgs> clientConnected() {
		return _clientConnected;
    }

	public Event4<ServerClosedEventArgs> closed() {
		return _closed;
    }
	
	void withEnvironment(Runnable runnable) {
		_container.withEnvironment(runnable);
	}	
}
