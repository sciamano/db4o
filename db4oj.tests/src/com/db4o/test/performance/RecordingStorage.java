/* Copyright (C) 2004 - 2006  db4objects Inc.  http://www.db4o.com */

package com.db4o.test.performance;

import java.io.*;

import com.db4o.ext.*;
import com.db4o.io.*;

/**
 * IO adapter for benchmark.
 * @exclude
 */
public class RecordingStorage extends StorageDecorator {
	// NOTE/FIXME: Ugly hack to prevent YapRandomAccessFile timer file handle from
	// writing asonchronously to our log file. Very fragile, basically YapRandomAccessFile
	// timer handles - or IoAdapter design ;P - needs to be fixed.
	private int _runningId;	
	private String _logPath;

	public RecordingStorage(Storage storage, String logPath) {
		super(storage);
		_logPath = logPath;
		_runningId=0;
	}

	@Override
	protected Bin decorate(Bin bin) {
		RecordingBin recordingBin = new RecordingBin(bin, _logPath+"." + _runningId);
		_runningId++;
		return recordingBin;
	}
	
	static class RecordingBin extends BinDecorator {
		private RandomAccessFile _writer;
		private int _runs;
	
		public RecordingBin(Bin bin, String logPath) {
			super(bin);
			try {
				_writer = new RandomAccessFile(logPath, "rw");
			} 
			catch (IOException e) {
				throw new Db4oIOException(e);
			}
			_runs=0;
		}

		public void close() throws Db4oIOException {
			super.close();
			writeLogChar('q');
			//System.err.println(_runs);
			try {
				_writer.close();
			} 
			catch (IOException exc) {
				throw new Db4oIOException(exc);
			}
		}
	
		public int read(long pos, byte[] buffer, int length) throws Db4oIOException {
			writeLog('r', pos, length);
			return super.read(pos, buffer, length);
	
		}
	
		public void write(long pos, byte[] buffer, int length) throws Db4oIOException {
			writeLog('w', pos, length);
			super.write(pos, buffer, length);
		}
		
		public void sync() throws Db4oIOException {
			writeLogChar('f');
			super.sync();
		}
		
		private void writeLog(char type, long pos, int length)
				throws Db4oIOException {
			try {
				_writer.writeChar(type);
				_writer.writeLong(pos);
				_writer.writeInt(length);
			} catch (IOException e) {
				throw new Db4oIOException(e);
			}
			_runs++;
		}
		
		private void writeLogChar(char c) throws Db4oIOException {
			try {
				_writer.writeChar(c);
			} catch (IOException e) {
				throw new Db4oIOException(e);
			}
			_runs++;
		}
	}
}