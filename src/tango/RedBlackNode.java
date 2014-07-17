package tango;

/**
 * Augments a TangoNode with RedBlack properties
 * 
 * @author Gregory Bint
 * 
 */
public class RedBlackNode extends TangoNode {

	public RBColor color;

	/**
	 * The number of black nodes on the longest (any, in a valid RBT) path from
	 * this node to a leaf, including this node.
	 * 
	 * NOTE: this value is only valid at the root
	 */
	public int blackHeight;

	/**
	 * Creates a new RedBlackNode, coloured RED
	 * 
	 * @param key
	 *            the initial key for the new node
	 * @param value
	 *            the initial payload for the new node
	 */
	public RedBlackNode(int key, Object value) {
		super(key, value);

		this.color = RBColor.RED;
	}

	@Override
	public String toString() {
		return super.toString() + ":" + color;
	}
}
