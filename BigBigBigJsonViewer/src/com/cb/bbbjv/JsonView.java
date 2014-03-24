package com.cb.bbbjv;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
import java.util.logging.Logger;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import jsyntaxpane.DefaultSyntaxKit;
import com.cb.bbbjv.stream.IStream;
import com.cb.bbbjv.stream.TextStream;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import data.IDataPart;
import data.TextPart;

public class JsonView extends JPanel  implements TreeSelectionListener {
	
	private static final String EXAMPLE_JSON = "ugly_big_example.json";

	private static final long serialVersionUID = -176328818653183403L;
	private JEditorPane htmlPane;
	  
	private JTree tree;
//	private SyntaxHighlighter highlighter;
	private static int DEFAULT_BLOCK_SIZE_IN_BYTES = 100000;
	    
	    
	public JsonView() {
        super(new GridLayout(1,0));

        DefaultSyntaxKit.initKit();
        
        DefaultMutableTreeNode top =
            new DefaultMutableTreeNode("A Node");
 
        tree = new JTree(top);
        tree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);
 
        tree.addTreeSelectionListener(this);
 
        JScrollPane treeView = new JScrollPane(tree);
 
        htmlPane = new JEditorPane();
        htmlPane.setEditable(false);
        
        JScrollPane htmlView = new JScrollPane(htmlPane);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setTopComponent(treeView);
        splitPane.setBottomComponent(htmlView);
 
        Dimension minimumSize = new Dimension(100, 50);
        htmlPane.setMinimumSize(minimumSize);
        treeView.setMinimumSize(minimumSize);
        splitPane.setDividerLocation(100); 
        splitPane.setPreferredSize(new Dimension(900, 600));
 
        add(splitPane);
        

        htmlPane.setContentType("text/java");
        
        createNodes(top);
    }
	
    /* (non-Javadoc)
     * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
     */
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                           tree.getLastSelectedPathComponent();
 
        if (node == null) return;
 
        Object nodeInfo = node.getUserObject();
        if (node.isLeaf()) {
            //BookInfo book = (BookInfo)nodeInfo;
            //displayURL(book.bookURL);

        } else {
            //displayURL(); 
        }
    }
 
    private void displayURL(URL url) {
//        try {
            if (url != null) {
                //htmlPane.setPage(url);
            } else { 
            	//htmlPane.setText("File Not Found");
            }
//        } catch (IOException e) {
//            System.err.println("Attempted to read a bad URL: " + url);
//        }
    }
 
    private void createNodes(DefaultMutableTreeNode top) {
    	
    	RandomAccessFile rad;
		try {
			// Read-only for now
			String accessMode = "r"; 
			
			rad = new RandomAccessFile(new File(EXAMPLE_JSON), accessMode);
    	
			long fileLength = rad.length();
			
			int currentOffset = 0;
			
			IStream<TextPart> textStream = new TextStream();
			
			TextPart part = textStream.getPart(rad, currentOffset, DEFAULT_BLOCK_SIZE_IN_BYTES, fileLength);
			
			htmlPane.setText(prettyPrintIncompleteJSON(part));
			//htmlPane.setText(prettyPrintIncompleteJSON(part));
			htmlPane.addKeyListener(new StreamKeyNavigator(rad, textStream, currentOffset, fileLength));
			
			// TODO: Build tree from data part (even if some nodes are incomplete)
			
			//TreeBuilder tb = new TreeBuilder();
			//tb.build(top, stream);
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public String prettyPrintIncompleteJSON(IDataPart part) {
		String uglyAndIncomplete = part.toString();
		
		int openingLevel = 0;
		
	    char[] data = uglyAndIncomplete.toCharArray();
	    Stack<Level> levels = new Stack<Level>();
	    
	    int firstValidOpening = -1;
	    int firstValidOpeningLevel = 0;
	    
	    int lastValidClosing = 0;
	    
	    ArrayList<Level> otherOnSameLevel = new ArrayList<Level>();
	    
		for(int i = 0 ; i < data.length ; i++) {
			char c = data[i];
			
			if (c == '{') {
				openingLevel++;
				levels.push(new Level(openingLevel, i, -1));
				
			} else if (c == '}' && levels.size() > 0) {
				openingLevel--;

				// Closing last valid level
				Level o = levels.pop();
				o.end = i;
				
				if (firstValidOpening == -1 || o.start < firstValidOpening) {
					firstValidOpening = o.start;
					firstValidOpeningLevel = o.level;
				}
				
				// NOTE: For now it only accept one valid on the same level
				
				// TODO: Make array of valid for root level (and pretty print each of them) to allow parsing event if 
				//       the rest of the object enclosing the array is not complete/valid
				if (o.level < firstValidOpeningLevel || o.start == firstValidOpening) {
					lastValidClosing = o.end;
					
					otherOnSameLevel.clear();
					
				} else if (o.level == firstValidOpeningLevel) {
					
					otherOnSameLevel.add(o);
				}
				 
				//System.out.println(uglyAndIncomplete.substring(o.start, o.end + 1));
			}
		}
		
		String invalidBeforePart = "";
		String prettyJsonString = "";
		String invalidAfterPart = "";
		
		if (firstValidOpening > 0) {
		    invalidBeforePart = uglyAndIncomplete.substring(0, firstValidOpening - 1);
	
		    String validPart = uglyAndIncomplete.substring(firstValidOpening, lastValidClosing + 1);

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonParser jp = new JsonParser();
			
		    if (otherOnSameLevel.size() > 0) {
		    	StringBuilder sb = new StringBuilder();
		    	
		    	int lastEnd = lastValidClosing + 1;
		    	
		    	for(int i = 0 ; i < otherOnSameLevel.size() ; i++) {
		    		Level o = otherOnSameLevel.get(i);
		    		
		    		// Append invalid json between valid json
		    		if (i == 0 || o.start > lastEnd) {
		    			sb.append(uglyAndIncomplete.substring(lastEnd + 1, o.start - 1));
		    		}
			    	
		    		// Pretty print JSON
		    		sb.append(prettyfyValidJson(gson, jp, uglyAndIncomplete.substring(o.start, o.end + 1)));
			    	lastEnd = o.end + 1;
		    	}
		    	invalidAfterPart = sb.toString();
		    	
		    } else {
		    	invalidAfterPart = uglyAndIncomplete.substring(lastValidClosing + 2, uglyAndIncomplete.length());
		    }
			//System.out.println(validPart);
			 
			try {
				JsonElement je = jp.parse(validPart);
				prettyJsonString = gson.toJson(je);
				
			} catch (Exception e) {
				System.err.println("Valid part cannot be parsed : " + validPart);
				prettyJsonString = uglyAndIncomplete;
			}
		} else {
			System.err.println("No valid JSON in : " + uglyAndIncomplete);
			prettyJsonString = uglyAndIncomplete;
		}
		
		return (new StringBuilder()).append(invalidBeforePart).append(System.lineSeparator())
				.append(prettyJsonString).append(System.lineSeparator()).append(invalidAfterPart).toString();
    }
    
    public String prettyfyValidJson(Gson gson, JsonParser jp, String validJson) {
    	String prettyJsonString = null;
    	
    	try {
			JsonElement je = jp.parse(validJson);
			prettyJsonString = gson.toJson(je);
			
		} catch (Exception e) {
			System.err.println("Valid part cannot be parsed : " + validJson);
			prettyJsonString = validJson;
		}
    	
    	return prettyJsonString;
    }
    
    public static int indexOfNth(String src, String str, int nth) {
        int index = src.indexOf(str);
        
        if (index == -1) {
        	return -1;
        }

        for (int i = 1; i < nth; i++) {
            index = src.indexOf(src, index + 1);
            
            if (index == -1) {
            	return -1;
            }
        }
        return index;
    }
    
    // TODO: Refactor
    class Level {
    	int level;
    	int start;
    	int end;
    	
		public Level(int level, int start, int end) {
			this.level = level;
			this.start = start;
			this.end = end;
		}
    }
    
    class StreamKeyNavigator implements KeyListener {

		private IStream<TextPart> textStream;
		private int currentOffset;
		private RandomAccessFile rad;
		private long fileLength;

		public StreamKeyNavigator(RandomAccessFile rad, IStream<TextPart> textStream, int currentOffset, long fileLength) {
			this.rad = rad;
			this.textStream = textStream;
			this.currentOffset = currentOffset;
			this.fileLength = fileLength;
		}

		@Override
		public void keyTyped(KeyEvent e) {
			// Nothing to do
		}

		@Override
		public void keyPressed(KeyEvent e) {
	        int keyCode = e.getKeyCode();
	        switch( keyCode ) { 
	            case KeyEvent.VK_UP:
	            	// TODO: Use interface to notify another component that would extract data from stream
	            	if (fetchAndDisplayDataFromStream(currentOffset - DEFAULT_BLOCK_SIZE_IN_BYTES)) {
	            		currentOffset -= DEFAULT_BLOCK_SIZE_IN_BYTES;
	            	}
	                break;
	            case KeyEvent.VK_DOWN:
	            	// TODO: Use interface to notify another component that would extract data from stream
	            	if (fetchAndDisplayDataFromStream(currentOffset + DEFAULT_BLOCK_SIZE_IN_BYTES)) {
	            		currentOffset += DEFAULT_BLOCK_SIZE_IN_BYTES;
	            	}
	                break;
	         }
	    } 

		@Override
		public void keyReleased(KeyEvent e) {
			// Nothing to do
		}
	    
	    private boolean fetchAndDisplayDataFromStream(int currentOffset) {
			TextPart part = null;
			try {
				part = textStream.getPart(rad, currentOffset, DEFAULT_BLOCK_SIZE_IN_BYTES, fileLength);
				
				if (part != null) {
					htmlPane.setText(prettyPrintIncompleteJSON(part)); 
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return (part != null);
	    }
    	
    }
}
