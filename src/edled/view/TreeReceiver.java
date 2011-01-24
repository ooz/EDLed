package edled.view;

import javax.swing.JTree;

/**
 * Interface for every view that contains a JTree
 * that might be replaced in the future.
 * 
 * @author Oliver Zscheyge
 */
interface TreeReceiver {
	
	/**
	 * Accepting a given JTree.
	 * 
	 * @param tree The JTree to set/accept.
	 */
	void setTree(final JTree tree);
	
}
