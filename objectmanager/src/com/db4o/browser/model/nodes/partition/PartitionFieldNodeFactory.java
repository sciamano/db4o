/*
 * Copyright (C) 2005 db4objects Inc.  http://www.db4o.com
 */
package com.db4o.browser.model.nodes.partition;

import com.db4o.browser.model.Database;
import com.db4o.browser.model.nodes.IModelNode;
import com.db4o.browser.model.nodes.InstanceNode;

public class PartitionFieldNodeFactory {
    public static final int THRESHOLD=75;
    
//    public static IModelNode[] create(IModelNode[] source) {
//        if (source.length <= THRESHOLD) {
//            return source;
//        } else {
//            PartitionSpec spec = new PartitionSpec(source.length, THRESHOLD);
//            int numNodes = spec.getNumRootNodes();
//            IModelNode[] result = new IModelNode[numNodes];
//            for (int nodePos=0; nodePos < numNodes; ++nodePos) {
//                result[nodePos] = new OldPartitionFieldNode(source, spec, 0, nodePos);
//            }
//            return result;
//        }
//    }
    
    public static IModelNode[] create(IModelNode[] source,int startIdx,int endIdx,Database database) {
        int length = endIdx-startIdx;
        boolean overflow = length>THRESHOLD;
        int numInstances=(overflow ? THRESHOLD : length);
        int resultLength=(overflow ? THRESHOLD+1 : length);
        IModelNode[] result=new IModelNode[resultLength];
        for(int resultidx=0;resultidx<numInstances;resultidx++) {
            result[resultidx]=source[startIdx+resultidx];
        }
        if(overflow) {
            result[result.length-1]=new PartitionFieldNode(database,source,startIdx+numInstances,endIdx);
        }
        return result;
    }

    public static IModelNode[] create(long[] sourceIds,int startIdx,int endIdx,Database database) {
        int length = endIdx-startIdx;
        boolean overflow = length>THRESHOLD;
        int numInstances=(overflow ? THRESHOLD : length);
        int resultLength=(overflow ? THRESHOLD+1 : length);
        IModelNode[] result=new IModelNode[resultLength];
        for(int resultidx=0;resultidx<numInstances;resultidx++) {
            result[resultidx]=new InstanceNode(sourceIds[startIdx+resultidx], database);
        }
        if(overflow) {
            result[result.length-1]=new PartitionInstanceNode(database,sourceIds,startIdx+numInstances,endIdx);
        }
        return result;
    }
}
