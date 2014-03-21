package com.cb.bbbjv.stream;

import java.io.IOException;
import java.io.RandomAccessFile;

import data.TextPart;

public class TextStream implements IStream<TextPart> {
	
	byte[] buffer = null;
	
	public TextStream() {
		
	}
	
	public TextPart getPart(RandomAccessFile rad, long byteOffset, int sizeInBytes, long fileLengthInBytes) throws IOException {
		TextPart part = null;
		
		if (byteOffset < fileLengthInBytes && byteOffset >= 0) {
			// try to reuse last allocated buffer array
			if (buffer == null || buffer.length < sizeInBytes) {
				buffer = new byte[(int) sizeInBytes];
			}
			
			rad.seek(byteOffset);
			
			int actualSize = rad.read(buffer, 0, sizeInBytes);
			
			if (actualSize > 0) {
				
				StringBuilder sb = new StringBuilder();
				
				for(int i = 0 ; i < actualSize ; i++) {
					sb.append(Character.toString((char)buffer[i]));
				}
				
				part = new TextPart(sb.toString(), byteOffset);
				
				System.out.println("Loaded " + actualSize + " bytes from offset " + byteOffset);
			}
		}
		
		
		return part;
	}

}
