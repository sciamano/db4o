/* Copyright (C) 2008  db4objects Inc.  http://www.db4o.com */

package com.db4o.io;

/**
 * Representation of a container for storage of db4o
 * database data (to file, to memory). 
 */
public interface Bin {

	/**
	 * returns the length of the Bin (on disc, in memory).  
	 */
	long length();

	/**
	 * reads a given number of bytes into an array of bytes at an 
	 * offset position.
	 * @param position the offset position to read at
	 * @param bytes the byte array to read bytes into
	 * @param bytesToRead the number of bytes to be read
	 * @return
	 */
	int read(long position, byte[] bytes, int bytesToRead);
	
	/**
	 * writes a given number of bytes from an array of bytes at 
	 * an offset position 
	 * @param position the offset position to write at
	 * @param bytes the array of bytes to write
	 * @param bytesToWrite the number of bytes to write
	 */
	void write(long position, byte[] bytes, int bytesToWrite);
	
	/**
	 * flushes the buffer content to the physical storage
	 * media.
	 */
	void sync();

	/**
	 * closes the Bin.
	 */
	void close();

}