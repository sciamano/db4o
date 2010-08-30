package com.db4o.rmi;

import java.io.*;
import java.lang.reflect.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class Distributor<T> implements Peer<T>, ByteArrayConsumer {

	private static final int REQUEST = 0;
	private static final int RESPONSE = 1;

	private AtomicLong nextRequest = new AtomicLong(0);

	private ConcurrentMap<Long, Request> requests = new ConcurrentHashMap<Long, Request>();

	private DataOutputStream out;

	private Runnable feeder;

	private ByteArrayConsumer consumer;
	private PeerProxy<T> rootClient;
	private AtomicLong objectsId = new AtomicLong();
	
	private ConcurrentMap<Object, PeerServer> serving = new ConcurrentHashMap<Object, PeerServer>();
	private ConcurrentMap<Long, PeerServer> servingById = new ConcurrentHashMap<Long, PeerServer>();
	
	private ConcurrentMap<Long, PeerProxy> proxying = new ConcurrentHashMap<Long, PeerProxy>();

	public Distributor(ByteArrayConsumer consumer, Class<T> rootFacade) {
		this(consumer, null, rootFacade);
	}

	public Distributor(ByteArrayConsumer consumer, T root) {
		this(consumer, root, null);
	}

	private Distributor(ByteArrayConsumer consumer, T root, Class<T> rootFacade) {
		initStream();
		setConsumer(consumer);

		if (root == null) {
			rootClient = proxyFor(objectsId.getAndIncrement(), rootFacade);
		} else {
			serverFor(root);
		}
	}

	private void initStream() {
		ByteArrayOutputStream bout = new ByteArrayOutputStream() {
			@Override
			public void flush() throws IOException {
				super.flush();
				Distributor.this.consumer.consume(this.buf, 0, this.count);
				reset();
			}
		};
		this.out = new DataOutputStream(bout);
	}

	public void setConsumer(ByteArrayConsumer consumer) {
		this.consumer = consumer;
	}

	protected Request request(long objectId, Method method, Object[] args, boolean expectsResponse) {
		Request r = new Request(this, method, args);
		long requestId = -1;
		
		if (expectsResponse) {
			requestId = nextRequest.getAndIncrement();
			requests.put(requestId, r);
		}

		sendRequest(objectId, requestId, r);

		return r;
	}

	public void consume(byte[] buffer, int offset, int length) throws IOException {
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(buffer, offset, length));
		do {
			processOne(in);
			
		} while (in.readBoolean());
	}

	private void processOne(DataInputStream in) throws IOException {

		byte op = in.readByte();

		switch (op) {
		case REQUEST:
			processRequest(in);
			break;

		case RESPONSE:
			processResponse(in);
			break;

		default:
			throw new RuntimeException("Unknown operation: " + op);
		}

	}

	private void processRequest(DataInputStream in) throws IOException {

		long serverId = in.readLong();

		PeerServer server = servingById.get(serverId);

		long requestId = in.readLong();
		Request r = new Request(this, server.getObject());
		r.deserialize(in);
		r.invoke();
		if (requestId != -1) {
			sendResponse(requestId, r.getInternal());
		}
	}

	private void sendRequest(long objectId, long requestId, Request r) {
		try {
			synchronized (out) {
				out.writeByte(REQUEST);
				out.writeLong(objectId);
				out.writeLong(requestId);
				r.serialize(out);
				out.writeBoolean(false);
				out.flush();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void sendResponse(long requestId, Object value) throws IOException {
		synchronized (out) {
			out.writeByte(RESPONSE);
			out.writeLong(requestId);
			if (value == null) {
				out.writeBoolean(false);
			} else {
				out.writeBoolean(true);
				Class clazz = value.getClass();
				out.writeUTF(clazz.getName());
				Serializer s = Request.serializerFor(clazz);
				s.serialize(out, value);
			}
			out.writeBoolean(false);
			out.flush();
		}
	}

	private void processResponse(DataInputStream in) throws IOException {
		long requestId = in.readLong();
		Request r = requests.remove(requestId);

		if (r == null) {
			throw new IllegalStateException("Request " + requestId + " is unknown");
		}

		Object o = null;
		if (in.readBoolean()) {
			o = Request.serializerFor(in.readUTF()).deserialize(in);
		}
		r.set(o);
	}

	public T sync() {
		return rootClient.sync();
	}

	public T async() {
		return rootClient.async();
	}

	public <R> T async(final Callback<R> callback) {
		return rootClient.async(callback);
	}

	public void setFeeder(Runnable feeder) {
		this.feeder = feeder;
	}

	public Runnable getFeeder() {
		return feeder;
	}

	public PeerServer serverFor(Object o) {

		PeerServer server = serving.get(o);

		if (server == null) {

			server = new PeerServer(objectsId.getAndIncrement(), o);

			serving.put(o, server);
			servingById.put(server.getId(), server);
		}

		return server;
	}

	public <S> PeerProxy<S> proxyFor(long id, Class<S> clazz) {

		PeerProxy<S> proxy = proxying.get(id);

		if (proxy == null) {

			proxy = new PeerProxy<S>(this, id, clazz);

			proxying.put(proxy.getId(), proxy);
		}

		return proxy;
	}

	public void feed() {
		if (feeder == null) {
			return;
		}
		feeder.run();
	}

}