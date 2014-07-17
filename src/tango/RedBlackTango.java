package tango;

/**
 * Red/Black Tango
 * 
 * This implementation keeps the auxiliary tree structures as valid Red/Black
 * trees
 * 
 * @author Gregory Bint
 * 
 */
public class RedBlackTango extends TangoTree {

	/*
	 * -----------------------------------------------------------------------
	 * BinarySearchTreeAdaptor Implementation
	 */
	@Override
	protected RedBlackNode createNode(int key, Object value) {
		return new RedBlackNode(key, value);
	}

	@Override
	protected RedBlackNode createNil() {
		return createNil(null);
	}

	@Override
	protected RedBlackNode createNil(BSTNode parent) {
		RedBlackNode nil = createNode(NIL_KEY, null);

		nil.color = RBColor.BLACK;
		nil.parent = parent;
		nil.blackHeight = 1;

		return nil;
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
			TangoNode nl = createNil(n);
			nl.depth = depth + 1;
			n.left = nl;

			TangoNode nr = createNil(n);
			nr.depth = depth + 1;
			n.right = nr;

			/* set this node */
			n.size = 3;
			n.color = RBColor.BLACK;
			n.blackHeight = 2; // has a nil leaf, so its bh is 2
			n.depth = depth;
			n.marked = true;
			n.minDepth = depth;
			n.maxDepth = depth;

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
			p.blackHeight = 2; // this tree is a singleton with 2 virtual nils
			p.depth = depth;
			p.marked = true;
			p.minDepth = depth;
			p.maxDepth = depth;

			return p;
		}
	}

	/*
	 * -----------------------------------------------------------------------
	 * Tango Tree Implementation
	 */

	@Override
	protected RedBlackNode tangoSplitImpl(TangoNode tn, BSTNode vRoot) {
		assert (tn != null);

		RedBlackNode n = (RedBlackNode) tn;

		/*
		 * Prepare tree for splitting
		 */

		BSTNode vpar = vRoot.parent;

		if (vpar != null)
			detach(vRoot, vpar);

		// remember mark
		boolean vMark = isMarked(vRoot);

		if (vMark)
			unmarkNode(vRoot);

		/*
		 * Red Black Split
		 */

		RedBlackNode k = (RedBlackNode) vRoot;
		RedBlackNode tl = null; // the left tree
		RedBlackNode vl = null; // the next pivot to use for the left tree
		RedBlackNode tr = null; // the right tree
		RedBlackNode vr = null; // the next pivot to use for the right tree

		while (!isTangoLeaf(k)) {

			assert (isRoot(k));

			// prepare for merge
			RedBlackNode kl = (RedBlackNode) k.left;
			RedBlackNode kr = (RedBlackNode) k.right;

			detach(kl, k);
			detach(kr, k);

			// make kl and kr into valid red black trees
			// this causes no harm if they are aux trees
			{
				kl.color = RBColor.BLACK;
				updateBlackHeight(kl);

				kr.color = RBColor.BLACK;
				updateBlackHeight(kr);
			}

			if (n.key < k.key) {
				tr = tangoMergeRedBlack(kr, vr, tr);
				assert (tr.color == RBColor.BLACK);

				vr = k;
				k = kl;

			} else if (n.key > k.key) {
				tl = tangoMergeRedBlack(tl, vl, kl);
				assert (tl.color == RBColor.BLACK);

				vl = k;
				k = kr;

			} else {

				/*
				 * n.key == k.key, this will be the last operation
				 * 
				 * This happens in every call to split because we are only
				 * working with values known to be in the tree
				 */
				tl = tangoMergeRedBlack(tl, vl, kl);
				assert (tl.color == RBColor.BLACK);
				vl = null;

				tr = tangoMergeRedBlack(kr, vr, tr);
				assert (tr.color == RBColor.BLACK);
				vr = null;

				// officially, we would want to insert k as the minimum of t2,
				// but actually, we want to k to simply span tl and tr

				attachLeft(tl, k);
				attachRight(tr, k);

				break;
			}
		}

		assert (vl == null);
		assert (vr == null);

		assert (isRoot(k));
		assert (n == k);

		/*
		 * Restore global tree after splitting
		 */

		// attach to tree
		if (vpar == null) {
			this._root = n;
		} else {
			attachUp(n, vpar);
		}

		// restore mark
		if (vMark)
			markNode(n);

		return n;
	}

	@Override
	protected RedBlackNode tangoMergeImpl(TangoNode tn) {
		assert (tn != null);

		RedBlackNode n = (RedBlackNode) tn;

		/*
		 * Gather parts
		 */
		RedBlackNode np = (RedBlackNode) n.parent;
		RedBlackNode nl = (RedBlackNode) n.left;
		RedBlackNode nr = (RedBlackNode) n.right;
		boolean root_mark = false;

		if (isMarked(n)) {
			root_mark = true;
			unmarkNode(n);
		}

		if (np != null)
			detach(n, np);

		detach(nl, n);
		detach(nr, n);

		// make all three trees into valid red/black trees
		{
			n.color = RBColor.BLACK;
			updateBlackHeight(n);

			// if (!isTangoLeaf(nl)) {
			nl.color = RBColor.BLACK;
			updateBlackHeight(nl);
			// }

			// if (!isTangoLeaf(nr)) {
			nr.color = RBColor.BLACK;
			updateBlackHeight(nr);
			// }
		}

		/*
		 * Perform Red/Black merge
		 */
		RedBlackNode newroot = tangoMergeRedBlack(nl, n, nr);

		/*
		 * Re-attach
		 */

		// re-attach root
		if (np == null) {
			this._root = newroot;
		} else {
			attachUp(newroot, np);
		}

		// restore mark
		if (root_mark) {
			markNode(newroot);
		}

		return newroot;

	}

	/**
	 * Performs a Red/Black merge on three RedBlack trees
	 * 
	 * @param nl
	 *            the left tree. All values in this tree must be strictly less
	 *            than all values in n and nr
	 * @param n
	 *            the singleton pivot node that will be used to merge nl and nr.
	 *            This value must be strictly between nl and nr
	 * @param nr
	 *            the right tree. All values in this tree must be strictly
	 *            greater than all values in nl and n
	 * @return the root of the merged tree
	 */
	private RedBlackNode tangoMergeRedBlack(RedBlackNode nl, RedBlackNode n,
			RedBlackNode nr) {

		/*
		 * The existence (or not) of nl and nr give 4 cases
		 */
		if (n == null) {
			/*
			 * There is no pivot at all
			 */
			if (nr != null) {
				assert (nr.color == RBColor.BLACK);
				n = nr;

			} else if (nl != null) {
				assert (nl.color == RBColor.BLACK);
				n = nl;

			} else {
				// everything was null
				assert (false);
			}

		} else if (isTangoLeaf(nl) && isTangoLeaf(nr)) {
			/*
			 * n is by itself, we are done
			 */

			// restore hanging aux trees to n
			if (nl != null) {
				attachLeft(nl, n);
				assert (nl.color == RBColor.BLACK);
			}

			if (nr != null) {
				attachRight(nr, n);
				assert (nr.color == RBColor.BLACK);
			}

			n.color = RBColor.RED;
			updateBlackHeight(n);
			assert (n.blackHeight == 1);

		} else if (isTangoLeaf(nl)) {
			/*
			 * nl is empty, so n needs to be joined to the minimal child of nr
			 */
			attachAsMinimum(n, nr);

			// restore nl to n;
			if (nl != null) {
				attachLeft(nl, n);
				assert (nl.color == RBColor.BLACK);
			}

			// prepare for rebalance
			n.color = RBColor.RED;
			updateBlackHeight(n);

		} else if (isTangoLeaf(nr)) {
			/*
			 * nr is empty, so n needs to be joined to the maximal child of nl
			 */
			attachAsMaximum(n, nl);

			// restore n.right to n;
			if (nr != null) {
				attachRight(nr, n);
				assert (nr.color == RBColor.BLACK);
			}

			// prepare for rebalance
			n.color = RBColor.RED;
			updateBlackHeight(n);

		} else {

			/*
			 * Both nl and nr have some values that need merging.
			 * 
			 * This gives rise to 3 cases;
			 */
			int lh = nl.blackHeight;
			int rh = nr.blackHeight;

			if (lh == rh) {
				/*
				 * n was the root of nl and nr already, the tree will already be
				 * balanced if we just put it back together.
				 */

				// restore links
				attachLeft(nl, n);
				attachRight(nr, n);

				assert (nl.color == RBColor.BLACK);
				assert (nr.color == RBColor.BLACK);

				// prepare for rebalance
				n.color = RBColor.RED;

			} else if (lh < rh) {
				/*
				 * right tree, nr, is larger
				 */

				TangoNode p = findMinWithBlackHeight(nr, nl.blackHeight);
				BSTNode pp = p.parent;

				assert (pp != null); // since nr is larger, this must be true

				attachLeft(nl, n);
				assert (nl.color == RBColor.BLACK);

				detach(p, pp);
				attachRight(p, n); // p should be right of n;

				assert (p == n.right);

				attachLeft(n, pp);
				updateMinMaxPath(n);

				// prepare for rebalance
				n.color = RBColor.RED;

			} else {
				/*
				 * left tree is larger
				 */

				TangoNode p = findMaxWithBlackHeight(nl, nr.blackHeight);
				BSTNode pp = p.parent;

				assert (pp != null); // since nl is larger, this must be true

				attachRight(nr, n);
				assert (nr.color == RBColor.BLACK);

				detach(p, pp);
				attachLeft(p, n); // p should be left of n;

				assert (p == n.left);

				attachRight(n, pp);
				updateMinMaxPath(n);

				// prepare for rebalance
				n.color = RBColor.RED;
			}
		}

		/*
		 * Finish rebalance
		 */

		// update these first as they are maintained by rotation
		updateMinMaxPath(n);
		updateSubtreeSizePath(n);

		insertFixUpCase1(n);

		updateBlackHeightPath(n);

		/*
		 * Find new root
		 */

		// n is in the tree somewhere, so we can just walk up to find the root
		RedBlackNode newroot = n;
		while (newroot.parent != null) {
			newroot = (RedBlackNode) newroot.parent;
		}

		return newroot;
	}

	/**
	 * Find the minimum valued black node of the given height
	 * 
	 * @param r
	 *            the tree to search
	 * @param blackHeight
	 *            the desired black height
	 * @return a black node of the given height
	 */
	private TangoNode findMinWithBlackHeight(RedBlackNode r, int blackHeight) {

		assert (r != null);
		assert (r.blackHeight > blackHeight);

		while (!isTangoLeaf(r)) {
			if (r.color == RBColor.BLACK && r.blackHeight == blackHeight)
				break;

			// descend minimal side
			r = (RedBlackNode) r.left;

			_stats.incOtherTraversals();
		}

		assert (r.color == RBColor.BLACK);
		assert (r.blackHeight == blackHeight);

		return r;
	}

	/**
	 * Find the maximum valued black node of the given height
	 * 
	 * @param r
	 *            the tree to search
	 * @param blackHeight
	 *            the desired black height
	 * @return a black node of the given height
	 */
	private TangoNode findMaxWithBlackHeight(RedBlackNode r, int blackHeight) {

		assert (r != null);
		assert (r.blackHeight > blackHeight);

		while (!isTangoLeaf(r)) {
			if (r.color == RBColor.BLACK && r.blackHeight == blackHeight)
				break;

			// descend maximal side
			r = (RedBlackNode) r.right;

			_stats.incOtherTraversals();
		}

		assert (r.color == RBColor.BLACK);
		assert (r.blackHeight == blackHeight);

		return r;
	}

	/**
	 * Attaches the given node as the maximum child of the given tree,
	 * preserving symmetric order, and adjusting the position of any existing
	 * child on t.
	 * 
	 * @param n
	 *            the node to attach
	 * @param t
	 *            the tree to attach to
	 */
	private void attachAsMaximum(BSTNode n, BSTNode t) {
		assert (n != null);
		assert (t != null);

		BSTNode a = getMaximumChild(t);
		BSTNode ar = a.right;

		assert (a.key < n.key);
		assert (a.right != null);
		assert (n.left == null);

		// preserve a.right on n.left;
		detach(ar, a);
		attachLeft(ar, n);

		// stitch up n onto t
		attachRight(n, a);

		updateMinMaxPath(n);
	}

	/**
	 * Attaches the given node as the minimum child of the given tree,
	 * preserving symmetric order, and adjusting the position of any existing
	 * child on t.
	 * 
	 * @param n
	 *            the node to attach
	 * @param t
	 *            the tree to attach to
	 */
	private void attachAsMinimum(BSTNode n, BSTNode t) {
		assert (n != null);
		assert (t != null);

		BSTNode a = getMinimumChild(t);
		BSTNode al = a.left;

		assert (n.key < a.key);
		assert (a.left != null);
		assert (n.right == null);

		// preserve a.left on n.right;
		detach(al, a);
		attachRight(al, n);

		// stitch up n onto t
		attachLeft(n, a);

		updateMinMaxPath(n);
	}

	/**
	 * Updates all black heights along the path from the given node to the
	 * nearest aux root. The given nodes children (if any) are assumed to have
	 * correct black heights.
	 * 
	 * @param n
	 *            the node to start updating black heights at.
	 */
	protected void updateBlackHeightPath(RedBlackNode n) {
		assert (n != null);

		// get n at least
		updateBlackHeight(n);

		// look up to the root
		while (!isRoot(n)) {
			n = (RedBlackNode) n.parent;
			updateBlackHeight(n);
		}
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

}
