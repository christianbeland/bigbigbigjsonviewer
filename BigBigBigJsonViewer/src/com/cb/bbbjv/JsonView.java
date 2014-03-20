package com.cb.bbbjv;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

public class JsonView extends JPanel  implements TreeSelectionListener {
	
	private static final long serialVersionUID = -176328818653183403L;
	private JEditorPane htmlPane;
	  
	private JTree tree;
    private static boolean useSystemLookAndFeel = false;
	    
	    
	public JsonView() {
        super(new GridLayout(1,0));

        DefaultMutableTreeNode top =
            new DefaultMutableTreeNode("The Java Series");
        createNodes(top);
 
        tree = new JTree(top);
        tree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);
 
        tree.addTreeSelectionListener(this);
 
        JScrollPane treeView = new JScrollPane(tree);
 
        htmlPane = new JEditorPane();
        htmlPane.setEditable(false);
 
        JScrollPane htmlView = new JScrollPane(htmlPane);
 
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(treeView);
        splitPane.setBottomComponent(htmlView);
 
        Dimension minimumSize = new Dimension(100, 50);
        htmlView.setMinimumSize(minimumSize);
        treeView.setMinimumSize(minimumSize);
        splitPane.setDividerLocation(100); 
        splitPane.setPreferredSize(new Dimension(500, 300));
 
        add(splitPane);
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
        try {
            if (url != null) {
                htmlPane.setPage(url);
            } else { 
            	htmlPane.setText("File Not Found");
            }
        } catch (IOException e) {
            System.err.println("Attempted to read a bad URL: " + url);
        }
    }
 
    private void createNodes(DefaultMutableTreeNode top) {
    	
    	FileInputStream stream;
		try {
			stream = new FileInputStream(new File("example.json"));
    	
			TreeBuilder tb = new TreeBuilder();
			tb.build(top, stream);
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    }
}
