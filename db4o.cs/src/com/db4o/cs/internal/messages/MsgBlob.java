/* Copyright (C) 2004   Versant Inc.   http://www.db4o.com */

package com.db4o.cs.internal.messages;

import java.io.*;

import com.db4o.*;
import com.db4o.cs.internal.*;
import com.db4o.ext.*;
import com.db4o.internal.*;
import com.db4o.internal.activation.*;

public abstract class MsgBlob extends MsgD implements BlobStatus{

    public BlobImpl _blob;
    int _currentByte;
    int _length;

    public double getStatus() {
        if (_length != 0) {
            return (double) _currentByte / (double) _length;
        }
        return Status.ERROR;
    }

    public abstract void processClient(Socket4Adapter socket) throws IOException;

    BlobImpl serverGetBlobImpl() {
        BlobImpl blobImpl = null;
        int id = _payLoad.readInt();
        synchronized (containerLock()) {
            blobImpl = (BlobImpl) container().getByID(transaction(), id);
            container().activate(transaction(), blobImpl, new FixedActivationDepth(3));
        }
        return blobImpl;
    }

    protected void copy(Socket4Adapter sock,OutputStream rawout,int length,boolean update) throws IOException {
        BufferedOutputStream out = new BufferedOutputStream(rawout);
        byte[] buffer=new byte[BlobImpl.COPYBUFFER_LENGTH];
        int totalread=0;
        while(totalread<length) {
            int stilltoread=length-totalread;
            int readsize=(stilltoread<buffer.length ? stilltoread : buffer.length);
            int curread=sock.read(buffer,0,readsize);
            
            if(curread < 0){
                throw new IOException();
            }
            
            out.write(buffer,0,curread);
            totalread+=curread;
            if(update) {
                _currentByte+=curread;
            }
        }
        out.flush();
        out.close();
    }

    protected void copy(InputStream rawin,Socket4Adapter sock,boolean update) throws IOException {
        BufferedInputStream in = new BufferedInputStream(rawin);
        byte[] buffer=new byte[BlobImpl.COPYBUFFER_LENGTH];
        int bytesread=-1;
        while((bytesread=rawin.read(buffer))>=0) {
            sock.write(buffer,0,bytesread);
            if(update) {
                _currentByte+=bytesread;
            }
        }
        in.close();
    }
}
