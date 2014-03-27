/**
 * 
 */
package com.cb.bbbjv;

import java.util.ArrayList;
import java.util.List;

/**
 * @author christianbeland
 *
 */
public class StreamConfiguration {
	
	
	private long currentOffset;
	private long blockSize;
	private List<ObjectListener> listeners;
	
	public StreamConfiguration(long currentOffset, long blockSize) {
		super();
		this.currentOffset = currentOffset;
		this.blockSize = blockSize;
		this.listeners = new ArrayList<ObjectListener>();
	}
	
	public synchronized void addListener(ObjectListener l) {
		listeners.add(l);
	}
	
	public synchronized long getCurrentOffset() {
		return currentOffset;
	}
	
	public synchronized void setCurrentOffset(long currentOffset) {
		
		if (currentOffset != this.currentOffset) {
			this.currentOffset = currentOffset;
			
			for(ObjectListener l : listeners) {
				l.updated();
			}
		}
	}
	
	public synchronized long getBlockSize() {
		return blockSize;
	}
	
	public synchronized void setBlockSize(long blockSize) {

		if (blockSize != this.blockSize) {
			this.blockSize = blockSize;
	
			for(ObjectListener l : listeners) {
				l.updated();
			}
		}
	}
}
