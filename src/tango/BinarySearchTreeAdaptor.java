package tango;

import java.util.ArrayList;

/**
 * An implementation of a basic BST with no rebalancing behaviour
 * 
 * @author Gregory Bint
 * 
 */
public abstract class BinarySearchTreeAdaptor implements BinarySearchTree {

	protected BSTNode _root;
	protected TreeStats _stats;

	/*
	 * -----------------------------------------------------------------------
	 * Construction
	 */
	protected BinarySearchTreeAdaptor() {
		_stats = new TreeStats();
	}

	/*
	 * -----------------------------------------------------------------------
	 * Binary Search Tree Implementation
	 */

	@Override
	public BSTNode insert(int key, Object value) throws OperationNotPermitted {

		BSTNode p = searchByKey(key);
		BSTNode n;

		if (p == null) {
			// tree is empty, create new root

			n = createNode(key, value);
			_root = n;
		} else if (p.key == key) {
			// key exists, update payload

			n = p;
			n.value = value;
		} else {
			// key does not exist

			n = createNode(key, value);
			n.parent = p;

			if (p != null) {
				if (key < p.key) {
					p.left = n;
				} else {
					p.right = n;
				}
			}

			updateSubtreeSizePath(n.parent);
		}

		return n;
	}

	@Override
	public Object remove(int key) throws OperationNotPermitted {

		BSTNode d = searchByKey(key);

		/*
		 * not found case
		 */
		if (d == null || d.key != key)
			return null;

		Object ret_payload = d.value;

		/*
		 * Found, now how to delete it?
		 */
		if (d.left == null && d.right == null) {
			/*
			 * d has no children, is a leaf
			 */

			if (d == _root) {
				_root = null;
			} else {
				clearParentReference(d);
			}

		} else if (d.left != null && d.right != null) {
			/*
			 * d has exactly two children implies that d has a true predecessor
			 */

			BSTNode n = getPredecessorByNode(d);

			// morph d into n
			d.key = n.key;
			d.value = n.value;

			// now we actually want to delete n, which may have a left child
			if (n.left == null) {
				clearParentReference(n);
			} else {
				setParentReference(n, n.left);
				n.left.parent = n.parent;
			}

			// remember this so we can return it
			d = n;

		} else {
			/*
			 * d has exactly one child
			 */
			BSTNode c = (d.left != null ? d.left : d.right);

			c.parent = d.parent;
			if (d == _root) {
				_root = c;
			} else {
				setParentReference(d, c);
			}
		}

		updateSubtreeSizePath(d.parent);

		// this is the node we are going to delete
		return ret_payload;
	}

	@Override
	public Object search(int key) {
		BSTNode n = searchByKey(key);

		if (n == null)
			return null;
		else
			return n.value;
	}

	@Override
	public BSTNode getRoot() {
		return _root;
	}

	@Override
	public TreeStats getStats() {
		return new TreeStats(_stats);
	}

	@Override
	public abstract void initializePerfectTree(int levels);

	/*
	 * Adaptor requirements
	 */
	protected abstract BSTNode createNode(int key, Object value);

	/*
	 * Search Utilities
	 */
	protected BSTNode searchByKey(int key) {
		BSTNode n = getRoot();
		BSTNode p = null;

		while (n != null) {
			p = n;

			_stats.incTraversals();

			if (key < n.key) {
				n = n.left;
			} else if (key > n.key) {
				n = n.right;
			} else {
				break;
			}
		}

		/*
		 * If key is not in tree, then search returns the last thing we saw
		 */
		return p;
	}

	/*
	 * Low-Level Manipulation Utilities.
	 * 
	 * These functions transcend auxiliary trees by ignoring the inspection
	 * methods (which can be overridden to respect some sort of aux tree
	 * structure).
	 */

	/**
	 * Performs a left rotation at n, by bringing up n.right, and pushing down n
	 */
	protected void rotateLeft(BSTNode n) {

		assert (n != null);

		BSTNode pv = n.right;
		assert (pv != null);

		// promote pv
		pv.parent = n.parent;

		if (n.parent == null) {
			if (n == _root) {
				_root = pv;
			} else {
				// do nothing, pv has no parent to notify
			}
		} else {
			setParentReference(n, pv);
		}

		// move pv's left subtree over to n's right
		n.right = pv.left;
		if (n.right != null)
			n.right.parent = n;

		// move n to be the left child of pv
		pv.left = n;
		n.parent = pv;

		_stats.incRotations();

		/*
		 * Fixup other tree stats
		 */

		n.size = 1 + (n.left != null ? n.left.size : 0)
				+ (n.right != null ? n.right.size : 0);

		pv.size = 1 + n.size + (pv.right != null ? pv.right.size : 0);
	}

	protected void rotateRight(BSTNode n) {

		assert (n != null);

		BSTNode pv = n.left;
		assert (pv != null);

		// promote pv
		pv.parent = n.parent;

		if (n.parent == null) {
			if (n == _root) {
				_root = pv;
			} else {
				// do nothing, pv has no parent to notify
			}
		} else {
			setParentReference(n, pv);
		}

		// move pv's right subtree over to n's left
		n.left = pv.right;
		if (n.left != null)
			n.left.parent = n;

		// move n to be the right child of pv
		pv.right = n;
		n.parent = pv;

		_stats.incRotations();

		/*
		 * Fixup other tree stats
		 */

		n.size = 1 + (n.left != null ? n.left.size : 0)
				+ (n.right != null ? n.right.size : 0);

		pv.size = 1 + n.size + (pv.left != null ? pv.left.size : 0);
	}

	/**
	 * Updates the parent of cur to identify nw as it's child, in place of cur
	 * (which is to say, on the same side of cur.parent)
	 * 
	 * @param cur
	 *            the node who has the parent of interest
	 * @param nw
	 *            the node which to which cur.parent will point to
	 */
	protected void setParentReference(BSTNode cur, BSTNode nw) {
		assert (cur != null);
		assert (cur.parent != null);
		assert (nw != null);

		if (cur == cur.parent.left) {
			cur.parent.left = nw;
		} else if (cur == cur.parent.right) {
			cur.parent.right = nw;
		} else {
			assert (false);
		}
	}

	/**
	 * Removes the given node's reference from its parent
	 * 
	 * @param n
	 *            the node to remove. n must not be null, and must have a
	 *            parent.
	 */
	protected void clearParentReference(BSTNode n) {

		assert (n != null);
		assert (n.parent != null);

		if (n == n.parent.left) {
			n.parent.left = null;
		} else if (n == n.parent.right) {
			n.parent.right = null;
		} else {
			assert (false);
		}
	}

	/*
	 * Inspection Functions
	 * 
	 * These functions return information about the structure of the tree. They
	 * can be overridden to allow for a tree-of-tree structure with whatever
	 * boundary conditions
	 */

	/**
	 * Determines if n is the root by checking if its parent pointer is null
	 * 
	 * @param n
	 *            the node to check
	 * @return true if n is considered a root
	 */
	protected boolean isRoot(BSTNode n) {
		assert (n != null);

		return (n.parent == null);
	}

	/**
	 * Determines if n is a leaf by checking if both of its children are null
	 * 
	 * @param n
	 *            the node to inspect
	 * @return true if n is a leaf
	 */
	protected boolean isLeaf(BSTNode n) {
		assert (n != null);

		return ((n.left == null) && (n.right == null));
	}

	/**
	 * Determines if a node is a nil leaf by checking the key value. Any node
	 * that identifies as a nil leaf should also be a true leaf.
	 * 
	 * @param n
	 *            the node to check
	 * @return true if node is a nil leaf
	 */
	protected boolean isNilLeaf(BSTNode n) {
		assert (n != null);

		if (n.key == BinarySearchTree.NIL_KEY) {
			assert (this.isLeaf(n));

			return true;
		} else {
			return false;
		}
	}

	/**
	 * Determines if n has a left child by checking whether its left pointer is
	 * null
	 * 
	 * @param n
	 *            the node to inspect
	 * @return true if n has a left child
	 */
	protected boolean hasLeftChild(BSTNode n) {
		return (n.left != null);
	}

	/**
	 * Determines if n has a right child by checking whether its right pointer
	 * is null
	 * 
	 * @param n
	 *            the node to inspect
	 * @return true if n has a right child
	 */
	protected boolean hasRightChild(BSTNode n) {
		return (n.right != null);
	}

	/**
	 * Note that n must have a parent;
	 * 
	 * @param n
	 *            the node to check
	 * @return true if n is the left child of its parent
	 */
	protected boolean isLeftChild(BSTNode n) {

		assert (n != null);
		assert (n.parent != null);

		return (n == n.parent.left);
	}

	/**
	 * Note that n must have a parent;
	 * 
	 * @param n
	 *            the node to check
	 * @return true if n is the right child of its parent
	 */
	protected boolean isRightChild(BSTNode n) {

		assert (n != null);
		assert (n.parent != null);

		return (n == n.parent.right);
	}

	/**
	 * Finds the child of maximum value in the subtree of n by traversing the
	 * rightmost path from n.
	 * 
	 * @param n
	 *            the node to inspect
	 * @return the maximum value in the subtree of n, which may be n itself
	 */
	protected BSTNode getMaximumChild(BSTNode n) {
		assert (n != null);

		while (hasRightChild(n)) {
			n = n.right;
			_stats.incOtherTraversals();
		}

		return n;
	}

	/**
	 * Finds the child of minimum value in the subtree of n by traversing the
	 * leftmost path from n.
	 * 
	 * @param n
	 *            the node to inspect
	 * @return the maximum value in the subtree of n, which may be n itself
	 */
	protected BSTNode getMinimumChild(BSTNode n) {
		assert (n != null);

		while (hasLeftChild(n)) {
			n = n.left;
			_stats.incOtherTraversals();
		}

		return n;
	}

	/**
	 * Locates the predecessor of a node by exploiting symmetric order
	 * 
	 * @param n
	 *            the node whose predecessor is desired
	 * @return the predecessor to n or null if no predecessor exists
	 */
	protected BSTNode getPredecessorByNode(BSTNode n) {
		if (hasLeftChild(n)) {
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
	protected BSTNode getSuccessorByNode(BSTNode n) {
		if (hasRightChild(n)) {
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

	/**
	 * Determines the sibling of n by first determining if n is a left or right
	 * child of its parent and then returning the other
	 * 
	 * @param n
	 *            the node to inspect
	 * @return the sibling of n, if it exists
	 */
	protected BSTNode getSibling(BSTNode n) {
		assert (n != null);

		if (isRoot(n)) {
			// the root has no sibling
			return null;
		} else {
			if (isLeftChild(n) && hasRightChild(n.parent)) {
				return n.parent.right;
			} else if (isRightChild(n) && hasLeftChild(n.parent)) {
				return n.parent.left;
			} else {
				// if n's parent doesn't have an opposite child, return null
				return null;
			}
		}
	}

	/*
	 * Auxiliary Tree Information Maintenance
	 */

	/**
	 * Fix up the sizes recorded on all subtrees from the given node to the
	 * root. Assumes that the given node's children are correct.
	 * 
	 * The size functions apply to the global tree and so do not use
	 * 
	 * @param n
	 *            the node to begin the update from.
	 */
	protected void updateSubtreeSizePath(BSTNode n) {
		if (n == null)
			return;

		while (true) {
			updateSubtreeSize(n);

			if (n.parent == null) {
				break;
			} else {
				n = n.parent;
			}
		}
	}

	/**
	 * Recalculate the size recorded at the given node. Assumes the node's
	 * children are correct
	 * 
	 * @param n
	 *            the node whose size is to be recalculated
	 */
	protected void updateSubtreeSize(BSTNode n) {
		n.size = 1;

		if (n.left != null)
			n.size += n.left.size;

		if (n.right != null)
			n.size += n.right.size;
	}

	/**
	 * Detaches the given node from its parent. You need to specify the parent
	 * in order to verify you know what you are doing!
	 * 
	 * @param n
	 *            the node to detach
	 * @param par
	 *            the node's parent
	 */
	protected void detach(BSTNode n, BSTNode par) {
		assert (n != null);
		assert (par != null);
		assert (n.parent == par);
		assert ((par.left == n) || (par.right == n));

		clearParentReference(n);
		n.parent = null;
	}

	/**
	 * Attaches the given node to the desired parent. The node must not have a
	 * parent, and the parent must not have a child where the node should go in
	 * symmetric order. Additionally, the node must not be a nil leaf.
	 * 
	 * @param n
	 *            the node to attach
	 * @param par
	 *            the parent to attach it to
	 */
	protected void attachUp(BSTNode n, BSTNode par) {
		assert (n != null);
		assert (par != null);
		assert (n.parent == null);
		assert (!isNilLeaf(n));

		if (n.key < par.key) {
			assert (par.left == null);
			par.left = n;
		} else {
			assert (par.right == null);
			par.right = n;
		}

		n.parent = par;
	}

	/**
	 * Attaches the given node to the left of the given parent. Is Nil-leaf
	 * aware. n must have no parent, par must have no left child, and symmetric
	 * order must hold on the keys
	 * 
	 * @param n
	 *            the node to attach
	 * @param par
	 *            the parent to attach it to
	 */
	protected void attachLeft(BSTNode n, BSTNode par) {
		assert (n != null);
		assert (par != null);
		assert (n.parent == null);
		assert (par.left == null);

		if (isNilLeaf(n)) {
			// no special conditions
		} else {
			assert (n.key < par.key);
		}

		par.left = n;
		n.parent = par;
	}

	/**
	 * Attaches the given node to the right of the given parent. Is Nil-leaf
	 * aware. n must have no parent, par must have no right child, and symmetric
	 * order must hold on the keys (except for nils)
	 * 
	 * @param n
	 *            the node to attach
	 * @param par
	 *            the parent to attach it to
	 */
	protected void attachRight(BSTNode n, BSTNode par) {
		assert (n != null);
		assert (par != null);
		assert (n.parent == null);
		assert (par.right == null);

		if (isNilLeaf(n)) {
			// no special conditions
		} else {
			assert (n.key > par.key);
		}

		par.right = n;
		n.parent = par;
	}

	@Override
	public String toString() {
		// return an in-order traversal from the root
		ArrayList<Integer> elements = new ArrayList<Integer>();

		fillInOrderElements(_root, elements);

		return elements.toString();
	}

	private void fillInOrderElements(BSTNode n, ArrayList<Integer> elements) {
		if (n.left != null)
			fillInOrderElements(n.left, elements);

		elements.add(n.key);

		if (n.right != null)
			fillInOrderElements(n.right, elements);
	}
}
