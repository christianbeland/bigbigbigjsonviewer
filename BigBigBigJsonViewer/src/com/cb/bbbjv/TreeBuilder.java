package com.cb.bbbjv;

import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.tree.DefaultMutableTreeNode;

public class TreeBuilder {

	public TreeBuilder() {
		
	}
	
	public void build(DefaultMutableTreeNode top, FileInputStream stream) {
		
		getAround(top, stream);
	}
	
	private void getAround(DefaultMutableTreeNode top, FileInputStream stream) {
		try {
			int content;
			StringBuilder sb = new StringBuilder();
			String data;
			boolean isInArray = false;
			
			while ((content = stream.read()) != -1 && (isInArray || sb != null)) {
				System.out.print((char) content);
				data = Character.toString((char) content);
				
				if (content == '{') {
					// New object
					addObject(top, stream);
					
				} else if (content == '}') {
					// End of object
					DefaultMutableTreeNode node 
						= new DefaultMutableTreeNode(new Entity(sb.toString()));
					
					top.add(node);
					
					System.err.print(sb.toString());
					
					sb = null;
					
				} else if (content == ',') {
					// End of properties
					
				} else if (content == '[') {
					// New array
					isInArray = true;
					
				} else if (content == ']') {
					// End array
					isInArray = false;
					
				} else {
					sb.append(data);
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			
		} finally {
			try {
				if (stream.available() == 0) {
					try {
						stream.close();
					} catch (IOException e) {
						e.printStackTrace();	
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void addObject(DefaultMutableTreeNode top, FileInputStream stream) {
		DefaultMutableTreeNode node 
			= new DefaultMutableTreeNode(new Entity("+"));
	
		top.add(node);
		
		getAround(node, stream);
	}

	class Entity {
		private String content;
		
		public Entity(String content) {
			this.content = content;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return content;
		}
	}
}
