package tango;

/**
 * A straight implementation of Red Black Trees
 * 
 * @author Gregory Bint
 * 
 */
public class RedBlackTree extends BinarySearchTreeAdaptor implements
		BinarySearchTree {

	/*
	 * -----------------------------------------------------------------------
	 * Construction
	 */
	public RedBlackTree() {
		super();

		_root = createNil();
	}

	/*
	 * -----------------------------------------------------------------------
	 * BinarySearchTree Implementation
	 */
	@Override
	public BSTNode insert(int key, Object value) throws OperationNotPermitted {
		RedBlackNode n = (RedBlackNode) super.searchByKey(key);

		if (isNilLeaf(n)) {
			/*
			 * this key is not yet in the tree, the search terminated at a nil
			 * node
			 */

			assert (n.key == NIL_KEY);

			// morph the leaf into our new node
			n.key = key;
			n.value = value;
			n.color = RBColor.RED; // newly inserted values are always red

			// attach new leaves
			n.left = createNil(n);
			n.right = createNil(n);

			// set correct size
			n.size = 3;
			updateSubtreeSizePath(n.parent);

			// rebalance
			if (_root == n) {
				n.color = RBColor.BLACK;
			} else {
				insertFixUpCase1(n);
			}

			updateBlackHeightPath(n);

		} else {
			/* this key is already in the tree */

			// update the payload
			n.value = value;
		}

		return n;
	}

	@Override
	public Object remove(int key) throws OperationNotPermitted {
		RedBlackNode d = (RedBlackNode) super.searchByKey(key);

		// key was not found
		if (d == null || isNilLeaf(d))
			return null;

		// d is not a nil, so it must have two children
		assert (d.left != null);
		assert (d.right != null);

		RedBlackNode dl = (RedBlackNode) d.left;
		RedBlackNode dr = (RedBlackNode) d.right;
		RedBlackNode r;

		Object ret_payload = d.value;

		if (isNilLeaf(dl) && isNilLeaf(dr)) {
			/*
			 * d is at the "bottom" of the tree, so stitch up one of the nil
			 * leaves to d's parent
			 */

			if (d == _root) {
				// d was the only element in the tree
				_root = dl;
				dl.parent = null;
			} else {
				setParentReference(d, dl);
				dl.parent = d.parent;
			}

			r = dl; // record rebalancing point

		} else if (!isNilLeaf(dl) && !isNilLeaf(dr)) {
			/*
			 * d is an "internal" node, i.e. has two non-nil children
			 * 
			 * We will actually keep d in the tree by morphing it into its
			 * predecessor and then deleting that instead
			 */

			// TODO verify which getMinimumChild is called by this
			RedBlackNode n = (RedBlackNode) getPredecessorByNode(d);

			// copy n up to d
			d.key = n.key;
			d.value = n.value;

			// now we drop n, which must have a left child, nil or otherwise
			setParentReference(n, n.left);
			n.left.parent = n.parent;

			// grab rebalance point
			r = (RedBlackNode) n.left;

			// update d to point to the node we actually delete
			d = n;

		} else {

			/*
			 * d has exactly 1 non-nil child
			 */

			// get the non-nil child
			RedBlackNode c = (isNilLeaf(dl) ? dr : dl);

			c.parent = d.parent;
			if (d == _root) {
				_root = c;
			} else {
				setParentReference(d, c);
			}

			// grab rebalance point
			r = c;
		}

		updateSubtreeSizePath(d.parent);

		if (d.color == RBColor.BLACK) {
			if (r.color == RBColor.RED) {
				// repaint c to maintain length of black chains
				r.color = RBColor.BLACK;
			} else {
				deleteFixUpCase1(r);
			}
		} else {
			/* d.color == RBColor.RED */

			// deleting a red node doesn't change any balance conditions
			// so: do nothing
		}

		updateBlackHeightPath(r);

		return ret_payload;
	}

	/*
	 * -----------------------------------------------------------------------
	 * BinarySearchTreeAdaptor Implementation
	 */
	@Override
	protected RedBlackNode createNode(int key, Object value) {
		return new RedBlackNode(key, value);
	}

	@Override
	public void initializePerfectTree(int levels) {

		_root = createPerfectSubtree(levels - 1, 0, 1);

	}

	private RedBlackNode createPerfectSubtree(int levels, int depth, int nextId) {
		if ((levels - depth) == 0) {
			RedBlackNode n = createNode(nextId, new Integer(nextId));
			++nextId;

			/* attach nils */
			RedBlackNode nl = createNil(n);
			n.left = nl;

			RedBlackNode nr = createNil(n);
			n.right = nr;

			/* set this node */
			n.size = 3;
			n.color = RBColor.BLACK;
			n.blackHeight = 2;

			return n;
		} else {
			/* build subtrees */
			RedBlackNode l = createPerfectSubtree(levels, depth + 1, nextId);
			nextId += Math.pow(2, (levels - depth)) - 1;

			RedBlackNode p = createNode(nextId, new Integer(nextId));
			++nextId;

			RedBlackNode r = createPerfectSubtree(levels, depth + 1, nextId);

			l.parent = p;
			p.left = l;

			r.parent = p;
			p.right = r;

			/* set node properties */
			p.size = l.size + r.size + 1;
			p.color = RBColor.BLACK;
			p.blackHeight = r.blackHeight + 1;

			return p;
		}
	}

	/*
	 * -----------------------------------------------------------------------
	 * RedBlack Implementation
	 */
	protected RedBlackNode createNil() {
		return createNil(null);
	}

	protected RedBlackNode createNil(BSTNode parent) {
		RedBlackNode nil = createNode(NIL_KEY, null);
		nil.color = RBColor.BLACK;
		nil.parent = parent;
		nil.blackHeight = 1;

		return nil;
	}

	/*
	 * -----------------------------------------------------------------------
	 * Tree maintenance
	 */

	/**
	 * Updates the black height of a single node, assuming that its children
	 * have correct black heights
	 * 
	 * @param n
	 *            the node to update
	 */
	protected void updateBlackHeight(RedBlackNode n) {
		assert (n != null);

		// check nil leaf condition
		if (isNilLeaf(n)) {
			assert (n.blackHeight == 1);
			return;
		}

		// assume that at least "nil leaves" exist to the left and right of n
		int lh = 1;
		int rh = 1;

		if (hasLeftChild(n)) {
			lh = ((RedBlackNode) n.left).blackHeight;
		}

		if (hasRightChild(n)) {
			rh = ((RedBlackNode) n.right).blackHeight;
		}

		// the tree is supposed to be valid
		assert (lh == rh);

		// update n
		n.blackHeight = lh;

		if (n.color == RBColor.BLACK) {
			n.blackHeight += 1;
		}
	}

	/**
	 * Updates the black height of the root node by counting up from the given
	 * node, which must be a leaf or nil leaf
	 * 
	 * @param n
	 *            the leaf or nil leaf to start counting from.
	 */
	protected void updateBlackHeightPath(RedBlackNode n) {
		assert (n != null);
		// assert (isNilLeaf(n) || isNilLeaf(n.left));

		int bh = 0;

		if (n.left != null) // then n.left must be a nil leaf
			bh = 1;

		while (n != null) {
			if (n.color == RBColor.BLACK)
				++bh;

			// update along the way for no special reason
			n.blackHeight = bh;

			n = (RedBlackNode) n.parent;
		}
	}

	/*
	 * Properties of Red-Black Trees
	 * 
	 * 1. A node is either red or black.
	 * 
	 * 2. The root is black. (This rule is used in some definitions and not
	 * others. Since the root can always be changed from red to black but not
	 * necessarily vice-versa this rule has little effect on analysis.)
	 * 
	 * 3. All leaves are black.
	 * 
	 * 4. Both children of every red node are black.
	 * 
	 * 5. Every simple path from a node to a descendant leaf contains the same
	 * number of black nodes.
	 * 
	 * A. P3 infers that no red is followed by a red, and that no red has a red
	 * for a parent
	 */

	/*
	 * -----------------------------------------------------------------------
	 * Insert Fixup
	 */

	/**
	 * Assumptions: n is the new node.
	 * 
	 * Case1: n is the root. n should be black, to satisfy P2. Note that if it
	 * is made black, then we have just added 1 black node to the path of every
	 * other node, so P5 is maintained
	 */
	protected void insertFixUpCase1(RedBlackNode n) {
		if (isRoot(n)) {
			n.color = RBColor.BLACK;
			updateBlackHeight(n);
		} else {
			updateBlackHeight(n);
			insertFixUpCase2(n);
		}
	}

	/**
	 * Assumptions: n is not the root, and therefore has a parent, p
	 * 
	 * Case2: n is red and the parent of n is black. In this case, we do not
	 * have 2 reds following each other, so we are okay
	 */
	private void insertFixUpCase2(RedBlackNode n) {
		assert (n != null);
		RedBlackNode p = (RedBlackNode) n.parent;

		if (isBlack(p)) {
			updateBlackHeight(p);
			return; // n is allowed to be red
		} else {
			insertFixUpCase3(n);
		}
	}

	/**
	 * Assumptions: p exists; n is red, and p must be red; therefore, g exists
	 * (if p had no g, p would be root and be black); g is black (otherwise tree
	 * was unbalanced before starting)
	 * 
	 * Case3: n's uncle, u, is also red. To fix this, we switch both p and u to
	 * black, and g to red. Note: this may cause g to violate some properties,
	 * so we start this whole process over again with g.
	 */
	private void insertFixUpCase3(RedBlackNode n) {

		assert (n != null);

		RedBlackNode p = (RedBlackNode) n.parent;
		assert (p != null);

		RedBlackNode g = (RedBlackNode) p.parent;
		assert (g != null);

		RedBlackNode u = (RedBlackNode) getSibling(p);

		if ((u != null) && isRed(u)) {
			p.color = RBColor.BLACK;
			updateBlackHeight(p);
			u.color = RBColor.BLACK;
			updateBlackHeight(u);

			g.color = RBColor.RED;
			updateBlackHeight(g);

			// there may now be a red/red violation at g
			insertFixUpCase1(g);
		} else {
			insertFixUpCase4(n);
		}
	}

	/**
	 * Assumptions: p exists; g exists; u exists but could be NIL; p is red; u
	 * is black; g is black
	 */
	private void insertFixUpCase4(RedBlackNode n) {

		assert (n != null);

		RedBlackNode p = (RedBlackNode) n.parent;
		assert (p != null);

		assert (p.parent != null);

		if (isLeftChild(p)) {
			if (isRightChild(n)) {
				rotateLeft(p);

				// p is now a child of n
				updateBlackHeight(p);
				updateBlackHeight(n);

				// change our focus to p
				n = p;
			}
		} else /* isRightChild(p) == true */{
			if (isLeftChild(n)) {
				rotateRight(p);

				// p is now a child of n
				updateBlackHeight(p);
				updateBlackHeight(n);

				// change our focus to p
				n = p;
			}
		}

		insertFixUpCase5(n);
	}

	/**
	 * Assumptions: p exists; g exists; u exists but may be NIL; p is red; u is
	 * black; g is black
	 */
	private void insertFixUpCase5(RedBlackNode n) {
		assert (n != null);

		RedBlackNode p = (RedBlackNode) n.parent;
		assert (p != null);

		RedBlackNode g = (RedBlackNode) p.parent;
		assert (g != null);

		p.color = RBColor.BLACK;
		g.color = RBColor.RED;

		if (isLeftChild(p)) {
			rotateRight(g);
		} else {
			rotateLeft(g);
		}

		updateBlackHeight(g);
		updateBlackHeight(p);
	}

	/*
	 * -----------------------------------------------------------------------
	 * Delete Fix Up
	 */

	/**
	 * Assumptions: n exists and is black
	 * 
	 * Case1: Is n the new root?
	 */
	protected void deleteFixUpCase1(RedBlackNode n) {
		assert (n != null);
		assert (n.color == RBColor.BLACK);

		if (isRoot(n)) {
			// done
		} else {
			deleteFixUpCase2(n);
		}
	}

	/**
	 * Assumptions: n exists, p exists, if p exists, then s must exist
	 * 
	 * Case2: s is red
	 */
	private void deleteFixUpCase2(RedBlackNode n) {
		assert (n != null);
		RedBlackNode p = (RedBlackNode) n.parent;
		RedBlackNode s = (RedBlackNode) getSibling(n);

		if (isRed(s)) {
			assert (p.color == RBColor.BLACK); // since p has a red child

			p.color = RBColor.RED;
			s.color = RBColor.BLACK;

			if (isLeftChild(n)) {
				rotateLeft(p);
			} else {
				rotateRight(p);
			}

			/*
			 * note that as a result of this rotation, one of s's children will
			 * now be sibling to n. Since s was red, both of it's children must
			 * be black.
			 * 
			 * Therefore, the new sibling of n is definitely black.
			 */

		}

		/* finally */
		deleteFixUpCase3(n);
	}

	/**
	 * Assumptions: n exists, p exists, s exists, s is black
	 * 
	 * Case3: p, s, and s's children are black
	 */
	private void deleteFixUpCase3(RedBlackNode n) {
		assert (n != null);
		RedBlackNode p = (RedBlackNode) n.parent;
		RedBlackNode s = (RedBlackNode) getSibling(n);

		assert (s != null);
		assert (s.color == RBColor.BLACK);

		RedBlackNode sl = (RedBlackNode) s.left; // may be null
		RedBlackNode sr = (RedBlackNode) s.right; // may be null

		if (isBlack(p) && isBlack(s) && isBlack(sl) && isBlack(sr)) {
			/*
			 * d was black, and when deleted, all the paths on n's side of the
			 * tree got shorter by 1 black.
			 * 
			 * Now, since s, and s's children are all black, we can repaint s to
			 * red without violating any properties, and also removing 1 black
			 * from all paths on s's side of the tree, which evens things out.
			 * 
			 * However, now there is the possibility that all paths travelling
			 * through p have one less black than all paths not travelling
			 * through p, so we continue the fix-up at p.
			 */

			s.color = RBColor.RED;

			deleteFixUpCase1(p);
			return;

		} else {
			deleteFixUpCase4(n);
		}
	}

	/**
	 * Assumptions: n exists, p exists, s exists s is black
	 * 
	 * Case4: s, and s's children are black, but p is red
	 */
	private void deleteFixUpCase4(RedBlackNode n) {
		assert (n != null);
		RedBlackNode p = (RedBlackNode) n.parent;
		RedBlackNode s = (RedBlackNode) getSibling(n);

		assert (s != null);

		RedBlackNode sl = (RedBlackNode) s.left; // may be null
		RedBlackNode sr = (RedBlackNode) s.right; // may be null

		if (isRed(p) && isBlack(s) && isBlack(sl) && isBlack(sr)) {

			/*
			 * This has the affect of not changing the number of blacks on any
			 * paths through S, but adds one black to all paths through N. Since
			 * N just lost a black node when d was deleted, this evens things
			 * out.
			 */

			s.color = RBColor.RED;
			p.color = RBColor.BLACK;

			// finished
			return;
		} else {
			deleteFixUpCase5(n);
		}
	}

	/**
	 * Assumptions: n exists, p exists, s exists s is black
	 * 
	 * Case5: s is black, and s's inner child wrt p is red, while the other is
	 * black
	 */
	private void deleteFixUpCase5(RedBlackNode n) {
		assert (n != null);
		// RedBlackNode p = (RedBlackNode) n.parent;
		RedBlackNode s = (RedBlackNode) getSibling(n);

		assert (s != null);
		RedBlackNode sl = (RedBlackNode) s.left;
		RedBlackNode sr = (RedBlackNode) s.right;

		if (isLeftChild(s)) {
			// s is the left child of p, so its inner child wrt to p is its
			// right

			if (isBlack(sl) && isRed(sr)) {
				s.color = RBColor.RED;
				sr.color = RBColor.BLACK;

				rotateLeft(s);
			}
		} else {
			// s is the right child of p, so its inner child wrt to p is its
			// left

			if (isRed(sl) && isBlack(sr)) {
				s.color = RBColor.RED;
				sl.color = RBColor.BLACK;

				rotateRight(s);
			}
		}

		/* finally */
		deleteFixUpCase6(n);
	}

	/**
	 * Assumptions: n exists, p exists, s exists
	 * 
	 * Case6: s is black, s's right child is red, and s is the right child of p
	 */
	private void deleteFixUpCase6(RedBlackNode n) {
		assert (n != null);
		RedBlackNode p = (RedBlackNode) n.parent;
		RedBlackNode s = (RedBlackNode) getSibling(n);

		assert (s != null);
		RedBlackNode sl = (RedBlackNode) s.left;
		RedBlackNode sr = (RedBlackNode) s.right;

		s.color = p.color;
		p.color = RBColor.BLACK;

		if (isLeftChild(s)) {
			// s is the left child of p, so its outer child wrt to p is its left
			sl.color = RBColor.BLACK;

			rotateRight(p);
		} else {
			// s is the right child of p, so its outer child wrt to p is its
			// right
			sr.color = RBColor.BLACK;

			rotateLeft(p);
		}
	}

	/*
	 * -----------------------------------------------------------------------
	 * Other tree inspection functions
	 */

	/**
	 * Checks the colour of a node;
	 * 
	 * @param n
	 *            the node to check
	 * @return true if node/nil is red
	 */
	protected boolean isRed(RedBlackNode n) {
		assert (n != null);

		return (n.color == RBColor.RED);
	}

	/**
	 * Checks the colour of a node;
	 * 
	 * @param n
	 *            the node to check
	 * @return true if node/nil is black
	 */
	protected boolean isBlack(RedBlackNode n) {
		assert (n != null);

		return (n.color == RBColor.BLACK);
	}

}
