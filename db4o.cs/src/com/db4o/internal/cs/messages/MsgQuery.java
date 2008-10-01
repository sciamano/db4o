/* Copyright (C) 2006  db4objects Inc.  http://www.db4o.com */

package com.db4o.internal.cs.messages;

import com.db4o.config.*;
import com.db4o.foundation.*;
import com.db4o.internal.*;
import com.db4o.internal.cs.*;
import com.db4o.internal.query.result.*;

public abstract class MsgQuery extends MsgObject {
	
	private static final int ID_AND_SIZE = 2;
	
	private static int nextID;
	
	protected final void writeQueryResult(AbstractQueryResult queryResult, QueryEvaluationMode evaluationMode) {
		
		int queryResultId = 0;
		int maxCount = 0;
		
		if(evaluationMode == QueryEvaluationMode.IMMEDIATE){
			maxCount = queryResult.size();
		} else{
			queryResultId = generateID();
			maxCount = config().prefetchObjectCount();  
		}
		
		MsgD message = QUERY_RESULT.getWriterForLength(transaction(), bufferLength(maxCount));
		StatefulBuffer writer = message.payLoad();
		writer.writeInt(queryResultId);
		
        IntIterator4 idIterator = queryResult.iterateIDs();
        
    	writer.writeIDs(idIterator, maxCount);
        
        if(queryResultId > 0){
        	ServerMessageDispatcher serverThread = serverMessageDispatcher();
			serverThread.mapQueryResultToID(new LazyClientObjectSetStub(queryResult, idIterator), queryResultId);
        }
        
		write(message);
	}

	private int bufferLength(int maxCount) {
		return Const4.INT_LENGTH * (maxCount + ID_AND_SIZE);
	}
	
	private static synchronized int generateID(){
		nextID ++;
		if(nextID < 0){
			nextID = 1;
		}
		return nextID;
	}
	
	protected AbstractQueryResult newQueryResult(QueryEvaluationMode mode){
		return stream().newQueryResult(transaction(), mode);
	}

}