package com.db4o.util.io;

import java.io.*;
import java.lang.reflect.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import com.db4o.io.*;

import sun.nio.ch.*;

public class NIOFileAdapter extends IoAdapter {
	private int hits=0;
	private int misses=0;
	
	private int _pageSize;
	private FileChannel _channel;
	private MappedByteBuffer _page;
	private int _pageId;
	private long _position;
	private long _size;	
	private boolean _dirty;
	private Map _id2Page;
	private LinkedList _lruPages;
	private int _lruLimit;
	
	public NIOFileAdapter(int pageSize,int lruLimit) {
		_pageSize=pageSize;
		_lruLimit=lruLimit;
	}

	private NIOFileAdapter(String filename,boolean lockFile,long initialLength,int pageSize,int lruLimit) throws IOException {
		_pageSize=pageSize;
		_channel = new RandomAccessFile(filename, "rw").getChannel();
		_size=_channel.size();
		_page=null;
		_pageId=0;
		_position=0;
		_dirty=false;
		_id2Page=new HashMap();
		_lruPages=new LinkedList();
		_lruLimit=lruLimit;
	}

	public void seek(long position) throws IOException {
		_position=position;
	}

	public void close() throws IOException {
		for (Iterator pageiter = _id2Page.values().iterator(); pageiter.hasNext();) {
			MappedByteBuffer curpage = (MappedByteBuffer) pageiter.next();
			closePage(curpage);
		}
		_id2Page.clear();
		_page=null;
		_channel.close();
		//System.err.println("Hits: "+hits+", Misses: "+misses);
	}

	public long getLength() throws IOException {
		return _size;
	}

	public int read(byte[] bytes, int length) throws IOException {
		if(length<=0) {
			return 0;
		}
		int alreadyRead=0;
		int stillToRead=length;
		while(stillToRead>0) {
			forcePage();
			try {
				_page.position(pageOffset(_position));
			}
			catch(IllegalArgumentException exc) {
				return -1;
			}
			int hereToRead=(int)min(_page.limit()-_page.position(),stillToRead);
			if(hereToRead==0) {
				break;
			}
			_page.get(bytes,alreadyRead,hereToRead);
			stillToRead-=hereToRead;
			alreadyRead+=hereToRead;
			_position+=hereToRead;
		}
		return (alreadyRead>0 ? alreadyRead : -1);
	}

	public void write(byte[] bytes, int length) throws IOException {	
		if(length<=0) {
			return;
		}
		if(_position+length>_size) {
			_size=_position+length;
		}
		int alreadyWritten=0;
		int stillToWrite=length;
		while(stillToWrite>0) {
			forcePage();
			int pageOffset=pageOffset(_position);
			_page.limit((int)min(_pageSize,pageOffset+length));
			_page.position(pageOffset);
			int hereToWrite=(int)min(_page.capacity()-_page.position(),stillToWrite);
			_page.put(bytes,alreadyWritten,hereToWrite);
			stillToWrite-=hereToWrite;
			alreadyWritten+=hereToWrite;
			_position+=hereToWrite;
			_dirty=true;
		}
	}

	public void sync() throws IOException {
		// FIXME internalSync(_page);
	}

	private void internalSync(MappedByteBuffer page) {
		if(_dirty&&page!=null) {
			page.flip();
			page.force();
		}
	}

	public void unlock() {
	}

	public void lock() {
	}

	private MappedByteBuffer page(int pageId) throws IOException {
		MappedByteBuffer page=_channel.map(FileChannel.MapMode.READ_WRITE,pagePosition(pageId),_pageSize);
		page.limit(pageSize(pageId));
		return page;
	}
	
	private long pagePosition(int pageId) {
		return (long)pageId*_pageSize;
	}

	private int pageSize(int pageId) throws IOException {
		long sizeLeft=_size-pagePosition(pageId);
		return (sizeLeft>_pageSize ? _pageSize : (int)sizeLeft);
	}

	private void forcePage() throws IOException {
		int pageId=pageId(_position);
		int pageOffset=pageOffset(_position);
		Integer pageIdKey=Integer.valueOf(pageId);
		if(_id2Page.containsKey(pageIdKey)) {
			_lruPages.remove(pageIdKey);
			_lruPages.addFirst(pageIdKey);
			_page=(MappedByteBuffer)_id2Page.get(pageIdKey);
			_page.limit(pageSize(pageId));
			hits++;
			return;
		}
		closePage();
		loadPage(pageId,pageOffset);
		if (_lruPages.size()==_lruLimit) {
			Integer dropPageKey=(Integer)_lruPages.removeLast();
			MappedByteBuffer page=(MappedByteBuffer)_id2Page.remove(dropPageKey);
			try {
				Method unmap=FileChannelImpl.class.getDeclaredMethod("unmap",new Class[]{MappedByteBuffer.class});
				unmap.setAccessible(true);
				unmap.invoke(null,new Object[]{page});
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
		Integer addedPageKey=Integer.valueOf(_pageId);
		_id2Page.put(addedPageKey,_page);
		_lruPages.addFirst(addedPageKey);
		_dirty=false;
		misses++;
	}
	
	private void closePage() throws IOException {
		closePage(_page);
		_page=null;
	}

	private void closePage(MappedByteBuffer page) throws IOException {
		if(page!=null) {
			internalSync(page);
		}
	}

	private void loadPage(int pageId,int pageOffset) throws IOException {
		_page=page(pageId);
		_pageId=pageId;
		_dirty=false;
	}

	private int pageId(long position) {
		return (int)(position/_pageSize);
	}

	private int pageOffset(long position) {
		return (int)(position%_pageSize);
	}

	private long min(long a,long b) {
		return (a>b ? b : a);
	}

	private long max(long a,long b) {
		return (a<b ? b : a);
	}

	public IoAdapter open(String path, boolean lockFile, long initialLength) throws IOException {
		return new NIOFileAdapter(path,lockFile,initialLength,_pageSize,_lruLimit);
	}
}
