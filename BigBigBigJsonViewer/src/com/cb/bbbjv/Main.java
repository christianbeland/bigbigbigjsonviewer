package com.cb.bbbjv;

import javax.swing.JFrame;

public class Main {

	    public static void main(String[] args) {

	        javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	                createAndShowGUI();
	            }
	        });
	    }
	 

	    private static void createAndShowGUI() {

	        JFrame frame = new JFrame("Big Big Big Json Viewer");
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	 
	        JsonView newContentPane = new JsonView();
	        newContentPane.setOpaque(true);
	        frame.setContentPane(newContentPane);
	 
	        frame.pack();
	        frame.setVisible(true);
	    }
}
