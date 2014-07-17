package tango;

/**
 * Represents a node in a Tango Tree.
 * 
 * @author Gregory Bint
 * 
 */
public class TangoNode extends BSTNode {

	public boolean marked;

	/**
	 * The depth of this node. Should not be changed after the Tango Tree is
	 * initialized.
	 */
	public int depth;

	/**
	 * The minimum depth that appears in the augmented subtree of this node
	 */
	public int minDepth;

	/**
	 * The maximum depth that appears in the augmented subtree of this node
	 */
	public int maxDepth;

	/*
	 * -----------------------------------------------------------------------
	 * Construction
	 */
	public TangoNode(int key, Object value) {
		super(key, value);

		marked = false;
	}

	/*
	 * -----------------------------------------------------------------------
	 * Tango Node implementation
	 */
	/**
	 * Checks the colour of a node;
	 * 
	 * @param n
	 *            the node to check
	 * @return true if node/nil is red
	 */
	protected boolean isMarked(TangoNode n) {
		assert (n != null);

		return (n.marked == true);
	}

	@Override
	public String toString() {
		return "TN:k" + key + ":" + (marked ? "M" : "U") + ":d" + depth + ":m"
				+ minDepth + ":M" + maxDepth + ":" + ":p"
				+ (this.parent == null ? "-" : this.parent.key) + ":l"
				+ (this.left == null ? "-" : this.left.key) + ":r"
				+ (this.right == null ? "-" : this.right.key);
	}
}
