package de.mpg.cbs.edled.view;

import javax.swing.JTree;

/**
 * Interface for every view that contains a JTree
 * that might be replaced in the future.
 * 
 * @author Oliver Z.
 */
interface TreeReceiver {
	
	/**
	 * Accepting a given JTree.
	 * 
	 * @param tree The JTree to set/accept.
	 */
	void setTree(final JTree tree);
	
}
