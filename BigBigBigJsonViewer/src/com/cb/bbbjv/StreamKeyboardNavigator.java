package com.cb.bbbjv;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class StreamKeyboardNavigator implements KeyListener {

	private StreamConfiguration configuration;
	private long fileLength;

	public StreamKeyboardNavigator(long fileLength, StreamConfiguration configuration) {
		this.configuration = configuration;
		this.fileLength = fileLength;
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// Nothing to do
	}

	@Override
	public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        
        long currentOffset = configuration.getCurrentOffset();
        long blockSize = configuration.getBlockSize();
        
        switch( keyCode ) { 
            case KeyEvent.VK_UP:
            	currentOffset = Math.max(currentOffset - blockSize, 0);
                break;
                
            case KeyEvent.VK_DOWN:
            	currentOffset = Math.min(currentOffset + blockSize, fileLength);
                break;
         }

		 configuration.setCurrentOffset(currentOffset);
    } 

	@Override
	public void keyReleased(KeyEvent e) {
		// Nothing to do
	}
}
