package com.db4o.test.performance;

import com.db4o.*;
import com.db4o.config.*;
import com.db4o.foundation.io.*;
import com.db4o.io.*;
import com.db4o.query.*;

import db4ounit.*;

/**
 * @decaf.ignore
 */
public class SlotCachePerformanceTest {
	
	private static final int BENCHMARKS = 5;

	private static final int STORED_ITEMS = 50000;
	
	private static final int QUERY_ITERATIONS = 300;
	
	private static final int QUERY_VALUES = 10;
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		SlotCachePerformanceTest test0 = benchmarkWithOutSlotCache();
		SlotCachePerformanceTest test1 = benchmarkWithSlotCache();

		// warm up
		test0.run();
		test1.run();
		
		double totalRatio = 0;
		for (int i=0; i<BENCHMARKS; ++i) {
			
			
			final long t0 = test0.run();
			final long t1 = test1.run();
			
			final double ratio = t1/((double)t0);
			report(test1, ratio);
			totalRatio += ratio;
		}
		System.out.print("On average ");
		report(test1, totalRatio / BENCHMARKS);
	}

	private static SlotCachePerformanceTest benchmarkWithOutSlotCache() {
		return new SlotCachePerformanceTest("WithOutSlotCache", randomAccessFileAdapter(), 0);
	}

	private static SlotCachePerformanceTest benchmarkWithSlotCache() {
		return new SlotCachePerformanceTest("WithSlotCache", randomAccessFileAdapter(), 30);
	}

	private static void report(SlotCachePerformanceTest test1, final double ratio) {
	    System.out.println(test1._name + " is " + (ratio > 1 ? "slower by " : "faster by ") + ((int)(((ratio > 1 ? ratio : (1 - ratio)) * 100) % 100)) + "%");
    }
	
	private static IoAdapter randomAccessFileAdapter(){
		return new RandomAccessFileAdapter();
	}

	public static class Item {

		private int _id;
		
		private boolean[] _payLoad;

		public Item(int id) {
			_id = id;
			_payLoad = new boolean[100];
        }
		
		public int id() {
			return _id;
		}
	}
	
	private final String _name;
	private ObjectContainer _container;
	private String _filename;
	private final IoAdapter _io;
	private final int _slotCacheSize;
	
	public SlotCachePerformanceTest(String name, IoAdapter ioAdapter, int slotCacheSize) {
		_name = name;
		_io = ioAdapter;
		_slotCacheSize = slotCacheSize;
    }

	private long run() {
		openFile();
		if(STORED_ITEMS > 0){
			for (int i = 0; i < STORED_ITEMS; i++) {
				_container.store(new Item(i));
			}
			commit();
		}
		
		try {
			final long t0 = System.nanoTime();
			queryItems();
			final long t1 = System.nanoTime();
			final long elapsed = t1-t0;
			System.out.println(_name + ": " + ((int)(elapsed/1000000.0)) + "ms");
			return elapsed;
			
		} finally {
			dispose();
		}
    }

	private void commit() {
	    _container.commit();
    }

	private void dispose() {
	    _container.close();
	    File4.delete(_filename);
    }

	private void openFile() {
	    _filename = Path4.getTempFileName();
		_container = Db4oEmbedded.openFile(configuration(), _filename);
    }

	private EmbeddedConfiguration configuration() {
	    final EmbeddedConfiguration config = Db4oEmbedded.newConfiguration();
		config.common().objectClass(Item.class).objectField("_id").indexed(true);
		config.file().io(_io);
		config.cache().slotCacheSize(_slotCacheSize);
	    return config;
    }

	private void queryItems() {
		for (int j = 0; j < QUERY_VALUES; j++) {
			final Integer current = new Integer(j);
			for (int i = 0; i < QUERY_ITERATIONS; i++) {
				final Query query = newItemQuery(current);
				final ObjectSet<Object> result = query.execute();
				while (result.hasNext()) {
					final Item found = (Item)result.next();
					Assert.areEqual(current.intValue(), found.id());
				}
			}
		}
    }

	private Query newItemQuery(final Integer current) {
	    final Query query = _container.query();
	    query.constrain(Item.class);
	    query.descend("_id").constrain(current);
	    return query;
    }

}