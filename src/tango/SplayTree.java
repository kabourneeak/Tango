package tango;

public class SplayTree extends BinarySearchTreeAdaptor implements
		BinarySearchTree {

	public SplayTree() {

	}

	@Override
	public Object search(int key) {

		BSTNode n;

		n = super.searchByKey(key);

		splay(n);

		return n.value;
	}

	/**
	 * Perform a splay operation on the given node. It will be splayed to the
	 * root
	 * 
	 * @param u
	 *            the node to splay up
	 */
	public void splay(BSTNode u) {

		assert (u != null);

		BSTNode p = null; // parent
		BSTNode g = null; // grand-parent

		while (!isRoot(u)) {
			p = u.parent;
			g = null;

			if (p != null)
				g = p.parent;

			if (g == null) /* p is the root, so ZIG */
			{
				if (isLeftChild(u)) {
					rotateRight(p);
				} else {
					rotateLeft(p);
				}
			} else {
				/* there is a grand-parent */

				if (isLeftChild(u)) {
					/* u is left-child of p */

					if (isLeftChild(p)) {
						/* u is left of p, and p is left of g; ZIG-ZIG */
						rotateRight(g);
						rotateRight(p);
					} else {
						/* u is left of p, but p is right of g; */
						rotateRight(p);
						rotateLeft(g);
					}
				} else {
					/* u is right-child of p */

					if (isRightChild(p)) {
						/* u is right of p, and p is right of g, ZIG-ZIG */
						rotateLeft(g);
						rotateLeft(p);
					} else {
						/* u is right of p, but p is left of g, ZIG-ZAG */
						rotateLeft(p);
						rotateRight(g);
					}
				}

				/* g should now be child of x */
			}
		}
	}

	/*
	 * BinarySearchTreeAdaptor Implementation
	 */

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

	@Override
	protected BSTNode createNode(int key, Object value) {
		return new BSTNode(key, value);
	}

}
