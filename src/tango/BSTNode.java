package tango;

public class BSTNode {
	public int key;
	public Object value;

	public BSTNode parent;
	public BSTNode left = null;
	public BSTNode right = null;

	/**
	 * The size of the subtree rooted at this node, including the node itself
	 */
	public int size;

	public BSTNode(int key, Object value) {
		this.key = key;
		this.value = value;
		this.size = 1;
	}

	@Override
	public String toString() {
		return key + ":" + size;
	}
}
