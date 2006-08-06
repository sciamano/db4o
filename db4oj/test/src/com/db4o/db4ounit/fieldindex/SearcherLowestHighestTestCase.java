/* Copyright (C) 2006  db4objects Inc.  http://www.db4o.com */

package com.db4o.db4ounit.fieldindex;

import com.db4o.inside.btree.*;

import db4ounit.*;


public class SearcherLowestHighestTestCase implements TestCase, TestLifeCycle{
    
    private Searcher _searcher;
    
    private final int SEARCH_FOR = 9;
    
    private final int[] EVEN_EVEN_VALUES = new int[] {4, 9, 9, 9, 9, 11, 13, 17};
    
    private final int[] EVEN_ODD_VALUES = new int[] {4, 5, 9, 9, 9, 11, 13, 17};
    
    private final int[] ODD_EVEN_VALUES = new int[] {4, 9, 9, 9, 9, 11, 13};
    
    private final int[] ODD_ODD_VALUES = new int[] {4, 5, 9, 9, 9, 11, 13};
    
    private final int[] NO_MATCH_EVEN = new int[] {4, 5, 10, 10, 10, 11};
    
    private final int[] NO_MATCH_ODD = new int[] {4, 5, 10, 10, 10, 11, 13};
    
    
    private final int[][] MATCH_VALUES = new int[][]{
        EVEN_EVEN_VALUES,
        EVEN_ODD_VALUES,
        ODD_EVEN_VALUES,
        ODD_ODD_VALUES
    };
    
    private final int[][] NO_MATCH_VALUES = new int[][]{
        NO_MATCH_EVEN,
        NO_MATCH_ODD
    };
    
    private final SearchTarget[] ALL_TARGETS = new SearchTarget[]{SearchTarget.LOWEST, SearchTarget.ANY, SearchTarget.HIGHEST};
    
    public void testMatch(){
        for (int i = 0; i < MATCH_VALUES.length; i++) {
            int[] values = MATCH_VALUES[i];
            
            int lo = lowMatch(values);
            search(values, SearchTarget.LOWEST);
            Assert.areEqual(lo, _searcher.cursor());
            Assert.isTrue(_searcher.foundMatch());
            
            int hi = highMatch(values);
            search(values, SearchTarget.HIGHEST);
            Assert.areEqual(hi, _searcher.cursor());
            Assert.isTrue(_searcher.foundMatch());
        }
    }
    
    public void testNoMatch(){
        for (int i = 0; i < NO_MATCH_VALUES.length; i++) {
            int[] values = NO_MATCH_VALUES[i];
            
            int lo = lowMatch(values);
            search(values, SearchTarget.LOWEST);
            Assert.areEqual(lo, _searcher.cursor());
            Assert.isFalse(_searcher.foundMatch());
            
            int hi = highMatch(values);
            search(values, SearchTarget.HIGHEST);
            Assert.areEqual(hi, _searcher.cursor());
            Assert.isFalse(_searcher.foundMatch());
        }
    }
    
    public void testEmpty(){
        int[] values = new int[]{};
        for (int i = 0; i < ALL_TARGETS.length; i++) {
            search(values, ALL_TARGETS[i]);
            Assert.areEqual(0, _searcher.cursor());
            Assert.isFalse(_searcher.foundMatch());
            Assert.isFalse(_searcher.beforeFirst());
            Assert.isFalse(_searcher.beyondLast());
        }
    }
    
    public void testOneValueMatch(){
        int[] values = new int[]{9};
        for (int i = 0; i < ALL_TARGETS.length; i++) {
            search(values, ALL_TARGETS[i]);
            Assert.areEqual(0, _searcher.cursor());
            Assert.isTrue(_searcher.foundMatch());
            Assert.isFalse(_searcher.beforeFirst());
            Assert.isFalse(_searcher.beyondLast());
        }
    }
    
    public void testOneValueLower(){
        int[] values = new int[]{8};
        for (int i = 0; i < ALL_TARGETS.length; i++) {
            search(values, ALL_TARGETS[i]);
            Assert.areEqual(0, _searcher.cursor());
            Assert.isFalse(_searcher.foundMatch());
            Assert.isFalse(_searcher.beforeFirst());
            Assert.isTrue(_searcher.beyondLast());
        }
    }
    
    public void testOneValueHigher(){
        int[] values = new int[]{8};
        for (int i = 0; i < ALL_TARGETS.length; i++) {
            search(values, ALL_TARGETS[i]);
            Assert.areEqual(0, _searcher.cursor());
            Assert.isFalse(_searcher.foundMatch());
            Assert.isFalse(_searcher.beforeFirst());
            Assert.isTrue(_searcher.beyondLast());
        }
    }
    
    public void testTwoValuesMatch(){
        int[] values = new int[]{9, 9};
        search(values, SearchTarget.LOWEST);
        Assert.areEqual(0, _searcher.cursor());
        Assert.isTrue(_searcher.foundMatch());
        Assert.isFalse(_searcher.beforeFirst());
        Assert.isFalse(_searcher.beyondLast());
        
        search(values, SearchTarget.ANY);
        Assert.isTrue(_searcher.foundMatch());
        Assert.isFalse(_searcher.beforeFirst());
        Assert.isFalse(_searcher.beyondLast());
        
        search(values, SearchTarget.HIGHEST);
        Assert.areEqual(1, _searcher.cursor());
        Assert.isTrue(_searcher.foundMatch());
        Assert.isFalse(_searcher.beforeFirst());
        Assert.isFalse(_searcher.beyondLast());
    }
    
    public void testTwoValuesLowMatch(){
        int[] values = new int[]{9, 10};
        search(values, SearchTarget.LOWEST);
        Assert.areEqual(0, _searcher.cursor());
        Assert.isTrue(_searcher.foundMatch());
        Assert.isFalse(_searcher.beforeFirst());
        Assert.isFalse(_searcher.beyondLast());

        search(values, SearchTarget.ANY);
        Assert.areEqual(0, _searcher.cursor());
        Assert.isTrue(_searcher.foundMatch());
        Assert.isFalse(_searcher.beforeFirst());
        Assert.isFalse(_searcher.beyondLast());
        
        search(values, SearchTarget.HIGHEST);
        Assert.areEqual(0, _searcher.cursor());
        Assert.isTrue(_searcher.foundMatch());
        Assert.isFalse(_searcher.beforeFirst());
        Assert.isFalse(_searcher.beyondLast());
    }
    
    public void testTwoValuesHighMatch(){
        int[] values = new int[]{6, 9};
        search(values, SearchTarget.LOWEST);
        Assert.areEqual(1, _searcher.cursor());
        Assert.isTrue(_searcher.foundMatch());
        Assert.isFalse(_searcher.beforeFirst());
        Assert.isFalse(_searcher.beyondLast());

        search(values, SearchTarget.ANY);
        Assert.areEqual(1, _searcher.cursor());
        Assert.isTrue(_searcher.foundMatch());
        Assert.isFalse(_searcher.beforeFirst());
        Assert.isFalse(_searcher.beyondLast());
        
        search(values, SearchTarget.HIGHEST);
        Assert.areEqual(1, _searcher.cursor());
        Assert.isTrue(_searcher.foundMatch());
        Assert.isFalse(_searcher.beforeFirst());
        Assert.isFalse(_searcher.beyondLast());
    }
    
    public void testTwoValuesInBetween(){
        int[] values = new int[]{8, 10};
        search(values, SearchTarget.LOWEST);
        Assert.areEqual(0, _searcher.cursor());
        Assert.isFalse(_searcher.foundMatch());
        Assert.isFalse(_searcher.beforeFirst());
        Assert.isFalse(_searcher.beyondLast());

        search(values, SearchTarget.ANY);
        Assert.areEqual(0, _searcher.cursor());
        Assert.isFalse(_searcher.foundMatch());
        Assert.isFalse(_searcher.beforeFirst());
        Assert.isFalse(_searcher.beyondLast());
        
        search(values, SearchTarget.HIGHEST);
        Assert.areEqual(0, _searcher.cursor());
        Assert.isFalse(_searcher.foundMatch());
        Assert.isFalse(_searcher.beforeFirst());
        Assert.isFalse(_searcher.beyondLast());
    }
    
    public void testTwoValuesLower(){
        int[] values = new int[]{7, 8};
        search(values, SearchTarget.LOWEST);
        Assert.areEqual(1, _searcher.cursor());
        Assert.isFalse(_searcher.foundMatch());
        Assert.isFalse(_searcher.beforeFirst());
        Assert.isTrue(_searcher.beyondLast());

        search(values, SearchTarget.ANY);
        Assert.areEqual(1, _searcher.cursor());
        Assert.isFalse(_searcher.foundMatch());
        Assert.isFalse(_searcher.beforeFirst());
        Assert.isTrue(_searcher.beyondLast());
        
        search(values, SearchTarget.HIGHEST);
        Assert.areEqual(1, _searcher.cursor());
        Assert.isFalse(_searcher.foundMatch());
        Assert.isFalse(_searcher.beforeFirst());
        Assert.isTrue(_searcher.beyondLast());
    }
    
    public void testTwoValuesHigher(){
        int[] values = new int[]{10, 11};
        search(values, SearchTarget.LOWEST);
        Assert.areEqual(0, _searcher.cursor());
        Assert.isFalse(_searcher.foundMatch());
        Assert.isTrue(_searcher.beforeFirst());
        Assert.isFalse(_searcher.beyondLast());

        search(values, SearchTarget.ANY);
        Assert.areEqual(0, _searcher.cursor());
        Assert.isFalse(_searcher.foundMatch());
        Assert.isTrue(_searcher.beforeFirst());
        Assert.isFalse(_searcher.beyondLast());
        
        search(values, SearchTarget.HIGHEST);
        Assert.areEqual(0, _searcher.cursor());
        Assert.isFalse(_searcher.foundMatch());
        Assert.isTrue(_searcher.beforeFirst());
        Assert.isFalse(_searcher.beyondLast());
    }
    
    
    private int search(int[] values, SearchTarget target){
        _searcher = new Searcher(target, values.length);
        while(_searcher.incomplete()){
            _searcher.resultIs( values[_searcher.cursor()] - SEARCH_FOR );
        }
        return _searcher.cursor();
    }
    
    private final int lowMatch(int[] values){
        for (int i = 0; i < values.length; i++) {
            if(values[i] == SEARCH_FOR){
                return i;
            }
            if(values[i] > SEARCH_FOR){
                if(i == 0){
                    return 0;
                }
                return i - 1;
            };
        }
        throw new IllegalArgumentException("values");
    }
    
    private final int highMatch(int[] values){
        for (int i = values.length - 1; i >= 0; i--) {
            if(values[i] <= SEARCH_FOR){
                return i;
            }
        }
        throw new IllegalArgumentException("values");
    }
    
    public void setUp() throws Exception {
        _searcher = null;
    }

    public void tearDown() throws Exception {
        
    }

}
