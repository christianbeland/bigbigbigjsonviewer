package com.cb.bbbjv;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Stack;

import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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

	private JLabel rangeSliderValue1;

	private JLabel rangeSliderValue2;

	private JSlider rangeSlider;

	private StreamKeyboardNavigator navigator;

	private StreamConfiguration configuration;

	private RandomAccessFile rad;

	private JFormattedTextField textArea;
	
	private static long DEFAULT_BLOCK_SIZE_IN_BYTES = 100000;
	    
	    
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

        JPanel settingsPane = new JPanel();
        settingsPane.setLayout(new BoxLayout(settingsPane, BoxLayout.PAGE_AXIS));
        
        NumberFormat f = NumberFormat.getNumberInstance(); 
        f.setMaximumIntegerDigits(12);
        f.setMinimumIntegerDigits(4);
        f.setMaximumFractionDigits(0);
        
        textArea = new JFormattedTextField(f);
        textArea.setText(Long.toString(DEFAULT_BLOCK_SIZE_IN_BYTES));
        textArea.setMaximumSize(new Dimension(100, 30));
        
        PropertyChangeListener l = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String text = evt.getNewValue() != null ? evt.getNewValue().toString() : "";
                
                if (configuration != null) {
                	configuration.setBlockSize(Long.parseLong(text));
                }
            }
        };
        textArea.addPropertyChangeListener("value", l);
        
        rangeSliderValue1 = new JLabel();
        rangeSliderValue2 = new JLabel();
        rangeSliderValue1.setHorizontalAlignment(JLabel.LEFT);
        rangeSliderValue2.setHorizontalAlignment(JLabel.LEFT);
        
        rangeSlider = new JSlider();
        rangeSlider.setPreferredSize(new Dimension(300, rangeSlider.getPreferredSize().height));
        rangeSlider.setMinimum(0);
       
        settingsPane.add(textArea);
        settingsPane.add(rangeSliderValue1);
        settingsPane.add(rangeSlider);
        settingsPane.add(rangeSliderValue2);
      
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setTopComponent(htmlView);
        mainSplitPane.setBottomComponent(settingsPane);
        mainSplitPane.setDividerLocation(550); 
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setTopComponent(treeView);
        splitPane.setBottomComponent(mainSplitPane);
        
        Dimension minimumSize = new Dimension(100, 50);
        htmlPane.setMinimumSize(minimumSize);
        treeView.setMinimumSize(minimumSize);
        splitPane.setDividerLocation(100); 
        splitPane.setPreferredSize(new Dimension(900, 700));
 
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
 
        //Object nodeInfo = node.getUserObject();
        if (node.isLeaf()) {
            //BookInfo book = (BookInfo)nodeInfo;
            //displayURL(book.bookURL);

        } else {
            //displayURL(); 
        }
    }
 
    private void createNodes(DefaultMutableTreeNode top) {
    	
		try {
			// Read-only for now
			String accessMode = "r"; 
			
			rad = new RandomAccessFile(new File(EXAMPLE_JSON), accessMode);
    	
			final long fileLength = rad.length();

			int startingBlockSize = (int)Math.min(fileLength, DEFAULT_BLOCK_SIZE_IN_BYTES);
			
			// TODO: Long slider
			rangeSlider.setMaximum((int)fileLength);
			
			rangeSlider.setValue(0);
			
			int currentOffset = 0;
			
			final IStream<TextPart> textStream = new TextStream();
			TextPart part = textStream.getPart(rad, currentOffset, startingBlockSize, fileLength);
			
			htmlPane.setText(prettyPrintIncompleteJSON(part));
			
			configuration = new StreamConfiguration(currentOffset, startingBlockSize);
			configuration.addListener(new ObjectListener() {

				@Override
				public void updated() {
					// TODO set value without loop
					rangeSlider.setValue((int) configuration.getCurrentOffset());
					
	                rangeSliderValue1.setText(String.valueOf(configuration.getCurrentOffset()) + " bytes");
	                rangeSliderValue2.setText(String.valueOf(configuration.getCurrentOffset() + configuration.getBlockSize())+" / "+fileLength + " bytes");
				}
			});
			
			configuration.addListener(new ObjectListener() {

				@Override
				public void updated() {
					TextPart part = null;
					try {
						part = textStream.getPart(rad, configuration.getCurrentOffset(), (int) configuration.getBlockSize(), fileLength);
						
						if (part != null) {
							htmlPane.setText(prettyPrintIncompleteJSON(part)); 
						}
						
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});

            rangeSliderValue1.setText(String.valueOf(configuration.getCurrentOffset()) + " bytes");
            rangeSliderValue2.setText(String.valueOf(configuration.getCurrentOffset() + configuration.getBlockSize())+" / "+fileLength + " bytes");
            
			navigator = new StreamKeyboardNavigator(fileLength, configuration);
			htmlPane.addKeyListener(navigator);
			
	        rangeSlider.addChangeListener(new ChangeListener() {
	            public void stateChanged(ChangeEvent e) {
	                JSlider slider = (JSlider) e.getSource();
	                
	                int currentOffset = slider.getValue();
	                
            		configuration.setCurrentOffset(currentOffset);
	            }
	        });
	        
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
		    	
		    	// FIXME: APPEND INVALID AT THE END OF PART!!
		    	if (lastEnd + 2 < uglyAndIncomplete.length()) {
		    		sb.append(uglyAndIncomplete.substring(lastEnd + 2, uglyAndIncomplete.length()));
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
    
    
}
