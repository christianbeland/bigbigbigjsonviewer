package com.cb.bbbjv.stream;

import java.io.IOException;
import java.io.RandomAccessFile;

import data.IDataPart;

public interface IStream<A extends IDataPart> {
	
	A getPart(RandomAccessFile rad, long byteOffset, int sizeInByte, long fileLength) throws IOException;
}
