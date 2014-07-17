package tango;

import java.util.ArrayList;

public abstract class TangoTree extends BinarySearchTreeAdaptor implements
		BinarySearchTree {

	/*
	 * -----------------------------------------------------------------------
	 * BinarySearchTree Implementation
	 */
	@Override
	public BSTNode insert(int key, Object value) throws OperationNotPermitted {
		throw new OperationNotPermitted(
				"TangoTree objects do not allow insertion");
	}

	@Override
	public Object remove(int key) throws OperationNotPermitted {
		throw new OperationNotPermitted(
				"TangoTree objects do not allow removal");
	}

	/**
	 * "Ladies and gentlemen this is where the magic happens. Hold on to your
	 * fucking hats." -- Drew Martin, 2009.
	 */
	@Override
	public Object search(int key) {

		TangoNode n = (TangoNode) getRoot();
		assert (isMarked(n));

		/*
		 * The main search does not stop at aux tree boundaries
		 */
		while (!isNilLeaf(n)) {
			_stats.incTraversals();

			if (key < n.key) {
				n = (TangoNode) n.left;
			} else if (key > n.key) {
				n = (TangoNode) n.right;
			} else {
				// found it
				break;
			}

			/*
			 * When the walk visits a marked node, we need to updated our
			 * preferred paths:
			 * 
			 * 1. Cut the aux tree containing the parent of n, cutting at a
			 * depth 1 less than the minimum depth of the nodes in the tree
			 * rooted at n (i.e. one less than n.min)
			 * 
			 * 1a) The "top path" of the cut is the preferred path we have
			 * traversed so far on this search
			 * 
			 * 1b) The "bottom path" of the cut is the remainder of the
			 * preferred path as at the previous search.
			 * 
			 * 2a) We join the top path with the path at n.
			 * 
			 * 2b) The bottom path of that cut is now marked and exists as its
			 * own aux tree.
			 * 
			 * 2c) This effectively toggles the preferred child at n.parent from
			 * one path to the other
			 * 
			 * 3. Suppose our search ends at x_i. When we reach x_i, we cut that
			 * tree, isolating any nodes of depth greater than x_i, and join the
			 * top path to the previous aux tree.
			 */
			if (isMarked(n)) {
				// perform cut and join; move n to root of modified aux tree
				n = tangoCutAndJoin(n);
			}
		}

		assert (n != null);

		/*
		 * Step 3: When the final node of a search is accessed, we need to do
		 * one more cut and join
		 */

		// removes everything after n, and creates marks
		TangoNode r;
		r = tangoCutAt(n);

		// find the first marked predecessor
		TangoNode p;
		p = findMarkedPredecessor(r, n.key);

		if (p != null) {
			tangoJoin(r, p, n.depth);
		}

		/*
		 * Returns the last thing we saw, which may be the actual search result
		 * or some leaf if the desired key was not present in the tree
		 */
		if (isNilLeaf(n)) {
			return n.parent.value;
		} else {
			return n.value;
		}
	}

	/*
	 * -----------------------------------------------------------------------
	 * Tango Functions
	 */

	/**
	 * Finds a marked predecessor from the root of an aux tree. This is done by
	 * searching for key - 1
	 * 
	 * @param root
	 *            the root of the aux tree to search
	 * @param key
	 *            the key whose predecessor should be found
	 * @return the marked predecessor, or null if none is found
	 */
	private TangoNode findMarkedPredecessor(TangoNode root, int key) {

		key = key - 1;

		TangoNode n = root;

		while (!isNilLeaf(n)) {
			_stats.incOtherTraversals();

			if (key < n.key) {
				n = (TangoNode) n.left;
			} else if (key > n.key) {
				n = (TangoNode) n.right;
			} else {
				// we actually found the predecessor, which means it is already
				// in the preferred path and not marked
				return null;
			}

			// we don't want to count the root itself!
			if (isMarked(n)) {
				return n;
			}
		}

		// nothing found.
		return null;
	}

	private TangoNode tangoCutAndJoin(TangoNode n) {

		// find root of parent aux tree
		TangoNode topPath = (TangoNode) n.parent;

		while (!isMarked(topPath)) {
			assert (topPath.parent != null);
			topPath = (TangoNode) topPath.parent;
		}

		// calculate cut depth. Recall depth gets smaller towards the
		// root and we are trying to make room for n "under" p.
		int cutDepth = n.minDepth - 1;

		// cut parent aux tree at depth
		topPath = tangoCut(topPath, cutDepth);

		// join onto it
		topPath = tangoJoin(topPath, n, cutDepth);

		return topPath;
	}

	/**
	 * Cuts at after the given node. Used for the final step of the search
	 */
	private TangoNode tangoCutAt(TangoNode n) {
		// find root of _current_ aux tree
		TangoNode topPath = (TangoNode) n;

		while (!isMarked(topPath)) {
			assert (topPath.parent != null);
			topPath = (TangoNode) topPath.parent;
		}

		// calculate cut depth. Recall depth gets smaller towards the
		// root and we are trying to remove everything under n
		int cutDepth = n.depth;

		// cut parent aux tree at depth
		topPath = tangoCut(topPath, cutDepth);

		return topPath;
	}

	/**
	 * Implements the tango cut algorithm.
	 * 
	 * @param vRoot
	 *            the root of the path/aux tree to cut
	 * @param cutDepth
	 *            the depth to cut the path at. Everything with depth strictly
	 *            greater than cutDepth will be isolated into a marked subtree
	 * @return the root of the aux tree containing x after the operation is
	 *         completed
	 */
	private TangoNode tangoCut(TangoNode vRoot, int cutDepth) {

		TangoNode nRoot;

		/* get the interval [l,r] */
		TangoNode l = findMinWithDepth(vRoot, cutDepth);
		TangoNode r = findMaxWithDepth(vRoot, cutDepth);

		/* get the interval (lp, rp) */
		TangoNode lp = null;
		if (l != null) {
			lp = (TangoNode) getPredecessorByNode(l);
		}

		TangoNode rp = null;
		if (r != null) {
			rp = (TangoNode) getSuccessorByNode(r);
		}

		/* isolate [l,r] */
		if ((lp == null) && (rp == null)) {
			/*
			 * if there is no predecessor nor successor, then the entire
			 * interval [l,r] belongs in the cut. We don't change anything
			 */

			assert (isMarked(vRoot));

			nRoot = vRoot;

		} else if (rp == null) {
			/*
			 * In this case, we have lp, but not rp
			 * 
			 * The tree to be cut out is on lp's right
			 */

			tangoSplit(lp, vRoot);

			assert (!isTangoLeaf(lp.right));

			markNode(lp.right);
			updateMinMaxPath(lp);

			// re-assemble the tree on lp without the marked subtree
			nRoot = tangoMerge(lp);

		} else if (lp == null) {
			/*
			 * In this case, we have rp, but not lp
			 * 
			 * The tree to be cut out is on rp's left
			 */

			tangoSplit(rp, vRoot);

			assert (!isTangoLeaf(rp.left));

			markNode(rp.left);
			updateMinMaxPath(rp);

			// re-assemble the tree on rp without the marked subtree
			nRoot = tangoMerge(rp);

		} else {
			/*
			 * In this case, we have both rp and lp
			 * 
			 * The tree to be cut out is on rp's left. This is the same as the
			 * above case but with more assertions we can check
			 */

			// split out the lower path
			tangoSplit(lp, vRoot);
			tangoSplit(rp, lp.right);

			assert (!isTangoLeaf(rp.left));

			markNode(rp.left);
			updateMinMaxPath(rp); // will cover lp as well.

			// reassemble the tree without the marked subtree
			tangoMerge(rp);
			nRoot = tangoMerge(lp);
		}

		return nRoot;
	}

	/**
	 * Returns the node with the minimum key value within the auxiliary tree
	 * rooted at x. The traversal will not cross any marked nodes. The basic
	 * idea is to walk left whenever possible to achieve a minimum value. If
	 * walking left is not possible, then a right walk is permitted so long as
	 * the target depth has not yet been reached.
	 * 
	 * @param x
	 *            the node to begin the search at
	 * @param cutDepth
	 *            the threshold depth that must be exceeded (i.e. we want a node
	 *            strictly deeper than this depth)
	 * @return the minimum value node at depth > cutDepth, or null if no such
	 *         node exists
	 */
	private TangoNode findMinWithDepth(TangoNode x, int cutDepth) {
		assert (x != null);

		while (true) {
			TangoNode xl = (TangoNode) x.left;
			TangoNode xr = (TangoNode) x.right;

			if (!isTangoLeaf(xl) && xl.maxDepth > cutDepth) {
				/*
				 * A smaller value at depth > cutDepth can yet be found
				 */
				x = xl;
			} else if (x.depth > cutDepth) {
				/*
				 * We couldn't go left without reducing our depth, so we have
				 * found our minimum
				 */
				break;
			} else if (!isTangoLeaf(xr)) {
				/*
				 * We have not yet reached our target depth, so travelling right
				 * might be necessary.
				 */
				x = xr;
			} else {
				/*
				 * Nothing could be found. No nodes of depth > cutDepth exist in
				 * the subtree at x
				 */
				return null;
			}
		}

		return x;
	}

	/**
	 * Returns the node with the maximum key value within the auxiliary tree
	 * rooted at x. The traversal will not cross any marked nodes. The basic
	 * idea is to walk right whenever possible to achieve a maximum value. If
	 * walking right is not possible, then a left walk is permitted so long as
	 * the target depth has not yet been reached.
	 * 
	 * @param x
	 *            the node to begin the search at
	 * @param cutDepth
	 *            the threshold depth that must be exceeded (i.e. we want a node
	 *            strictly deeper than this depth)
	 * @return the maximum value node at depth > cutDepth, or null if no such
	 *         node exists
	 */
	private TangoNode findMaxWithDepth(TangoNode x, int cutDepth) {
		assert (x != null);

		while (true) {
			TangoNode xr = (TangoNode) x.right;
			TangoNode xl = (TangoNode) x.left;

			if (!isTangoLeaf(xr) && xr.maxDepth > cutDepth) {
				/*
				 * A larger value at depth > cutDepth can yet be found
				 */
				x = xr;
			} else if (x.depth > cutDepth) {
				/*
				 * We couldn't go left without reducing our depth, so we have
				 * found our minimum
				 */
				break;
			} else if (!isTangoLeaf(xl)) {
				/*
				 * We have not yet reached our target depth, so travelling right
				 * might be necessary.
				 */
				x = xl;
			} else {
				/*
				 * Nothing could be found. No nodes of depth > cutDepth exist in
				 * the subtree at x
				 */
				return null;
			}
		}

		return x;
	}

	/**
	 * Join the node n to the tree rooted at p
	 * 
	 * @param topPath
	 *            the root of the tree being adjoined to
	 * @param n
	 *            the root of the tree being joined to topPath
	 * @param cutDepth
	 *            the path depth to join with
	 * @return the root of the tree representing joined path
	 */
	private TangoNode tangoJoin(TangoNode topPath, TangoNode n, int cutDepth) {
		assert (topPath != null);
		assert (n != null);
		assert (isRoot(topPath));
		assert (isRoot(n));
		assert (topPath.maxDepth < n.minDepth);
		assert (cutDepth == topPath.maxDepth);
		assert (cutDepth == (n.minDepth - 1));

		TangoNode newRoot = null;

		TangoNode lp = null; // l' - n's predecessor in par
		TangoNode rp = null; // r' - n's successor in par

		/* walk down from topPath into n to find lp and rp */
		TangoNode x = topPath;

		while (x != n) {
			if (x.key > n.key) {
				rp = x;
				x = (TangoNode) x.left;
			} else {
				lp = x;
				x = (TangoNode) x.right;
			}
		}

		/* locate bottom path and join */
		if ((lp == null) && (rp == null)) {
			/*
			 * This shouldn't happen, as it would indicate that the top path was
			 * empty.
			 */

			assert (false);

		} else if (rp == null) {
			/*
			 * In this case, we have lp, but not rp
			 * 
			 * The tree to be cut out is on lp's right
			 */

			tangoSplit(lp, topPath);

			assert (isTangoLeaf(lp.right));

			unmarkNode(lp.right);
			updateMinMaxPath(lp.right);

			newRoot = tangoMerge(lp);

		} else if (lp == null) {
			/*
			 * In this case, we have rp, but not lp
			 * 
			 * The tree to be joined is on rp's left
			 */

			tangoSplit(rp, topPath);

			assert (isTangoLeaf(rp.left));

			unmarkNode(rp.left);
			updateMinMaxPath(rp.left);

			newRoot = tangoMerge(rp);

		} else {
			/*
			 * In this case, we have both rp and lp
			 * 
			 * The tree to be joined is on rp's left, as in the last case, but
			 * with more things we can check
			 */

			tangoSplit(lp, topPath);
			tangoSplit(rp, lp.right);

			assert (rp == lp.right);
			assert (isTangoLeaf(rp.left));

			unmarkNode(rp.left);
			updateMinMaxPath(rp.left); // will cover lp as well.

			tangoMerge(rp);
			newRoot = tangoMerge(lp);

		}

		assert (newRoot != null);

		return newRoot;
	}

	/**
	 * Perform a "tree-of-trees" split at the given node. This brings the given
	 * node up to the root position, with all values less than it on its left
	 * subtree, and all values greater than it on its right subtree
	 * 
	 * @param n
	 *            the node to split upon
	 * @param vRoot
	 *            the virtual root of the subtree to split upon. n should be a
	 *            descendant of vRoot
	 * @return the root of the aux tree containing n after the operation is
	 *         complete, which will be n itself.
	 */
	private TangoNode tangoSplit(TangoNode n, BSTNode vRoot) {
		assert (n != null);
		assert (vRoot != null);

		TangoNode ret;

		ret = tangoSplitImpl(n, vRoot);

		assert (ret == n);

		return ret;
	}

	protected abstract TangoNode tangoSplitImpl(TangoNode n, BSTNode vRoot);

	/**
	 * Implements a "tree-of-trees" merge on n, n.left, and n.right
	 * 
	 * @param n
	 *            the node at the center of the merge
	 * @return the root of the aux tree containing n after the operation is
	 *         complete
	 */
	private TangoNode tangoMerge(TangoNode n) {
		assert (n != null);

		TangoNode ret;

		ret = tangoMergeImpl(n);

		return ret;
	}

	protected abstract TangoNode tangoMergeImpl(TangoNode n);

	/*
	 * -----------------------------------------------------------------------
	 * BinarySearchTreeAdaptor Implementation
	 */
	@Override
	protected TangoNode createNode(int key, Object value) {
		return new TangoNode(key, value);
	}

	protected TangoNode createNil() {
		return createNil(null);
	}

	protected TangoNode createNil(BSTNode parent) {
		TangoNode nil = createNode(NIL_KEY, null);

		nil.parent = parent;

		return nil;
	}

	@Override
	public void initializePerfectTree(int levels) {

		_root = createPerfectSubtree(levels - 1, 0, 1);

	}

	private TangoNode createPerfectSubtree(int levels, int depth, int nextId) {
		if ((levels - depth) == 0) {
			TangoNode n = createNode(nextId, new Integer(nextId));
			++nextId;

			/* attach nils */
			TangoNode nl = createNil(n);
			nl.depth = depth + 1;
			n.left = nl;

			TangoNode nr = createNil(n);
			nr.depth = depth + 1;
			n.right = nr;

			/* set this node */
			n.size = 3;
			n.depth = depth;
			n.marked = true;
			n.minDepth = depth;
			n.maxDepth = depth;

			return n;
		} else {
			/* build subtrees */
			TangoNode l = createPerfectSubtree(levels, depth + 1, nextId);
			nextId += Math.pow(2, (levels - depth)) - 1;

			TangoNode p = createNode(nextId, new Integer(nextId));
			++nextId;

			TangoNode r = createPerfectSubtree(levels, depth + 1, nextId);

			l.parent = p;
			p.left = l;

			r.parent = p;
			p.right = r;

			/* set node properties */
			p.size = l.size + r.size + 1;
			p.depth = depth;
			p.marked = true;
			p.minDepth = depth;
			p.maxDepth = depth;

			return p;
		}
	}

	/*
	 * -----------------------------------------------------------------------
	 * Depth Maintenance
	 */

	@Override
	protected void rotateLeft(BSTNode n) {
		/* perform the rotation */
		super.rotateLeft(n);

		/* adjust mark; it should always be at the root of the aux tree */

		// the pivot should not have been previously marked
		assert (!isMarked(n.parent));

		if (isMarked(n)) {
			markNode(n.parent);
			unmarkNode(n);
		}

		/* adjust min/max depth */
		updateMinMax(n);
		updateMinMax(n.parent);
	}

	@Override
	protected void rotateRight(BSTNode n) {
		/* perform the rotation */
		super.rotateRight(n);

		/* adjust mark; it should always be at the root of the aux tree */

		// the pivot should not have been previously marked
		assert (!isMarked(n.parent));

		if (isMarked(n)) {
			markNode(n.parent);
			unmarkNode(n);
		}

		/* adjust min/max depth */
		updateMinMax(n);
		updateMinMax(n.parent);
	}

	private void updateMinMax(BSTNode n) {
		updateMinMax((TangoNode) n);
	}

	protected void updateMinMax(TangoNode n) {
		assert (n != null);

		int min = n.depth;
		int max = n.depth;

		TangoNode nl = (TangoNode) n.left;
		TangoNode nr = (TangoNode) n.right;

		if (!isTangoLeaf(nl)) {
			min = Math.min(min, nl.minDepth);
			max = Math.max(max, nl.maxDepth);
		}

		if (!isTangoLeaf(nr)) {
			min = Math.min(min, nr.minDepth);
			max = Math.max(max, nr.maxDepth);
		}

		n.minDepth = min;
		n.maxDepth = max;
		// System.out.println("Updated min/max - " + n);
	}

	protected void updateMinMaxPath(BSTNode n) {
		assert (n != null);

		updateMinMax(n);

		// the traversal cost of this is paid for by the search

		while (!isRoot(n)) {
			n = n.parent;
			updateMinMax(n);
		}
	}

	/*
	 * -----------------------------------------------------------------------
	 * Tree Inspection
	 */

	/**
	 * Recall that Red/Black Trees should have nil nodes on their fringe. In the
	 * case of the aux trees, we treat a marked child as though it were a nil
	 * leaf, as it is regarded as being "outside" the tree of interest.
	 * 
	 * @param n
	 *            the node to inspect
	 * @return true if it is an actual or implied nil leaf
	 */
	protected boolean isTangoLeaf(BSTNode n) {
		// assert (n != null);

		return (n == null || isNilLeaf(n) || isMarked(n));
	}

	/**
	 * In a Tango tree, we don't generally want to progress past a marked node,
	 * so we update the isRoot check accordingly.
	 */
	@Override
	protected boolean isRoot(BSTNode n) {
		return (super.isRoot(n) || isMarked(n));
	}

	/**
	 * An aux tree node is a leaf if has no children within in the same aux
	 * tree, which may happen due to nulls or marks
	 */
	@Override
	protected boolean isLeaf(BSTNode n) {
		return ((!hasLeftChild(n)) && (!hasRightChild(n)));
	}

	@Override
	protected boolean hasLeftChild(BSTNode n) {
		assert (n != null);

		if (super.hasLeftChild(n)) {
			return !isMarked(n.left);
		} else {
			return false;
		}
	}

	@Override
	protected boolean hasRightChild(BSTNode n) {
		assert (n != null);

		if (super.hasRightChild(n)) {
			return !isMarked(n.right);
		} else {
			return false;
		}
	}

	@Override
	protected boolean isLeftChild(BSTNode n) {
		if (super.isLeftChild(n)) {
			/*
			 * n is left of something, but if n is marked, then it cannot be
			 * considered a child of its BST parent within the same aux tree
			 */
			return !isMarked(n);
		} else {
			return false;
		}
	}

	@Override
	protected boolean isRightChild(BSTNode n) {
		if (super.isRightChild(n)) {
			/*
			 * n is right of something, but if n is marked, then it cannot be
			 * considered a child of its BST parent within the same aux tree
			 */
			return !isMarked(n);
		} else {
			return false;
		}
	}

	protected boolean isMarked(TangoNode n) {
		assert (n != null);

		return (n.marked == true);
	}

	protected void markNode(TangoNode n) {
		assert (n != null);

		n.marked = true;
	}

	protected void unmarkNode(TangoNode n) {
		assert (n != null);

		n.marked = false;
	}

	@Override
	protected BSTNode getMinimumChild(BSTNode n) {
		BSTNode m = super.getMinimumChild(n);

		if (isNilLeaf(m)) {
			return m.parent;
		} else {
			return m;
		}
	}

	@Override
	protected BSTNode getMaximumChild(BSTNode n) {
		BSTNode m = super.getMaximumChild(n);

		if (isNilLeaf(m)) {
			return m.parent;
		} else {
			return m;
		}
	}

	/**
	 * Locates the predecessor of a node by exploiting symmetric order
	 * 
	 * @param n
	 *            the node whose predecessor is desired
	 * @return the predecessor to n or null if no predecessor exists
	 */
	@Override
	protected BSTNode getPredecessorByNode(BSTNode n) {
		if (hasLeftChild(n) && !isNilLeaf(n.left)) {
			return getMaximumChild(n.left);
		} else {
			while (!isRoot(n) && isLeftChild(n)) {
				n = n.parent;
				_stats.incOtherTraversals();
			}

			if (isRoot(n)) {
				return null;
			} else {
				return n.parent;
			}
		}
	}

	/**
	 * Locates the successor of a node by exploiting symmetric order
	 * 
	 * @param n
	 *            the node whose successor is desired
	 * @return the successor to n or null if no successor exists
	 */
	@Override
	protected BSTNode getSuccessorByNode(BSTNode n) {
		if (hasRightChild(n) && !isNilLeaf(n.right)) {
			return getMinimumChild(n.right);
		} else {
			while (!isRoot(n) && isRightChild(n)) {
				n = n.parent;
				_stats.incOtherTraversals();
			}

			if (isRoot(n)) {
				return null;
			} else {
				return n.parent;
			}
		}
	}

	protected boolean isMarked(BSTNode n) {
		assert (n instanceof TangoNode);

		return isMarked((TangoNode) n);
	}

	protected void markNode(BSTNode n) {
		assert (n instanceof TangoNode);

		markNode((TangoNode) n);
	}

	protected void unmarkNode(BSTNode n) {
		assert (n instanceof TangoNode);

		unmarkNode((TangoNode) n);
	}

	/*
	 * Debugging helpers
	 */

	@Override
	public String toString() {
		// return an in-order traversal from the root
		ArrayList<Integer> elements = new ArrayList<Integer>();

		fillInOrderElements(_root, elements);

		return elements.toString();
	}

	private void fillInOrderElements(BSTNode n, ArrayList<Integer> elements) {
		if (n.left != null && n.left.key != NIL_KEY)
			fillInOrderElements(n.left, elements);

		elements.add(n.key);

		if (n.right != null && n.right.key != NIL_KEY)
			fillInOrderElements(n.right, elements);
	}
}
