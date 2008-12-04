/* Copyright (C) 2008  db4objects Inc.  http://www.db4o.com */

package com.db4o.io;

import com.db4o.foundation.*;

/**
 * @exclude
 */
public class BinConfiguration {
	
	private final String _uri;
	
	private final boolean _lockFile;
	
	private final long _initialLength;
	
	private final boolean _readOnly;
	
	private final ListenerRegistry<Integer> _blockSizeListenerRegistry;

	public BinConfiguration(String uri, boolean lockFile, long initialLength, boolean readOnly, ListenerRegistry<Integer> blockSizeListenerRegistry) {
		_uri = uri;
		_lockFile = lockFile;
		_initialLength = initialLength;
		_readOnly = readOnly;
		_blockSizeListenerRegistry = blockSizeListenerRegistry;
	}
	
	public String uri(){
		return _uri;
	}
	
	public boolean lockFile(){
		return _lockFile;
	}
	
	public long initialLength(){
		return _initialLength;
	}
	
	public boolean readOnly(){
		return _readOnly;
	}
	
	public ListenerRegistry<Integer> blockSizeListenerRegistry(){
		return _blockSizeListenerRegistry;
	}

}