package tango;

/**
 * A concrete version of the BinarySearchTreeAdaptor
 * 
 * @author Gregory Bint
 * 
 */
public class BasicBST extends BinarySearchTreeAdaptor implements
		BinarySearchTree {

	@Override
	protected BSTNode createNode(int key, Object value) {
		return new BSTNode(key, value);
	}

	public void setRoot(BSTNode r) {
		assert (r == null || r.parent == null);

		_root = r;
	}

	/*
	 * Increase visibility for testing purposes
	 */

	@Override
	public void rotateLeft(BSTNode n) {
		super.rotateLeft(n);
	}

	@Override
	public void rotateRight(BSTNode n) {
		super.rotateRight(n);
	}

	/**
	 * Creates a perfect binary search tree
	 * 
	 * @param levels
	 *            the number of levels to produce in the tree. A tree with k
	 *            levels will have nodes 1 .. 2^k -1
	 */
	@Override
	public void initializePerfectTree(int levels) {

		_root = createPerfectSubtree(levels - 1, 0, 1);

	}

	private BSTNode createPerfectSubtree(int levels, int depth, int nextId) {
		if ((levels - depth) == 0) {
			BSTNode n = createNode(nextId, new Integer(nextId));
			++nextId;

			return n;
		} else {
			/* build subtrees */
			BSTNode l = createPerfectSubtree(levels, depth + 1, nextId);
			nextId += Math.pow(2, (levels - depth)) - 1;

			BSTNode p = createNode(nextId, new Integer(nextId));
			++nextId;

			BSTNode r = createPerfectSubtree(levels, depth + 1, nextId);

			l.parent = p;
			p.left = l;

			r.parent = p;
			p.right = r;

			/* set node properties */
			p.size = l.size + r.size + 1;

			return p;
		}
	}
}
