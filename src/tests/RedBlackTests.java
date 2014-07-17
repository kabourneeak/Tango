package tests;

import static org.junit.Assert.*;

import org.junit.Test;

import tango.BSTNode;
import tango.BinarySearchTree;
import tango.OperationNotPermitted;
import tango.RBColor;
import tango.RedBlackNode;
import tango.RedBlackTree;

public class RedBlackTests {

	@Test
	public void testNewTree() {
		RedBlackTree t = new RedBlackTree();

		RedBlackNode n = (RedBlackNode) t.getRoot();

		assertNotNull(n);
		assertEquals(BinarySearchTree.NIL_KEY, n.key);
		assertEquals(RBColor.BLACK, n.color);
		assertNull(n.left);
		assertNull(n.right);
	}

	@Test
	public void testBalancedInsert() throws OperationNotPermitted {

		RedBlackTree t = new RedBlackTree();
		RedBlackNode n1, n2, n3, n4, n5, n6, n7;

		/*
		 * level 0
		 */
		n4 = (RedBlackNode) t.insert(4, "4");

		assertEquals(n4, t.getRoot());
		assertEquals(null, n4.parent);
		assertTrue(isNilLeaf(n4.left));
		assertTrue(isNilLeaf(n4.right));
		assertEquals(RBColor.BLACK, n4.color);
		validateTree(t);

		/*
		 * level 1
		 */
		n2 = (RedBlackNode) t.insert(2, "2");

		assertEquals(n4, n2.parent);
		assertTrue(isNilLeaf(n4.right));
		assertEquals(n2, n4.left);
		assertTrue(isNilLeaf(n2.left));
		assertTrue(isNilLeaf(n2.right));
		assertEquals(RBColor.BLACK, n4.color);
		assertEquals(RBColor.RED, n2.color);
		validateTree(t);

		//
		n6 = (RedBlackNode) t.insert(6, "6");

		assertEquals(n4, n6.parent);
		assertEquals(n6, n4.right);
		assertTrue(isNilLeaf(n6.left));
		assertTrue(isNilLeaf(n6.right));
		assertEquals(RBColor.BLACK, n4.color);
		assertEquals(RBColor.RED, n6.color);
		validateTree(t);

		/*
		 * level 2
		 */

		// this insertion triggers case 3, so n1's parent and uncle should
		// become black
		n1 = (RedBlackNode) t.insert(1, "1");

		assertEquals(n2, n1.parent);
		assertEquals(n1, n2.left);
		assertTrue(isNilLeaf(n1.left));
		assertTrue(isNilLeaf(n1.right));

		assertEquals(RBColor.BLACK, n4.color);
		assertEquals(RBColor.BLACK, n2.color);
		assertEquals(RBColor.BLACK, n6.color);

		assertEquals(RBColor.RED, n1.color);
		validateTree(t);

		//
		n5 = (RedBlackNode) t.insert(5, "5");

		assertEquals(n6, n5.parent);
		assertEquals(n5, n6.left);
		assertTrue(isNilLeaf(n5.left));
		assertTrue(isNilLeaf(n5.right));

		assertEquals(RBColor.BLACK, n4.color);
		assertEquals(RBColor.BLACK, n2.color);
		assertEquals(RBColor.BLACK, n6.color);

		assertEquals(RBColor.RED, n5.color);
		validateTree(t);

		//
		n3 = (RedBlackNode) t.insert(3, "3");

		assertEquals(n2, n3.parent);
		assertEquals(n3, n2.right);
		assertTrue(isNilLeaf(n3.left));
		assertTrue(isNilLeaf(n3.right));

		assertEquals(RBColor.BLACK, n4.color);
		assertEquals(RBColor.BLACK, n2.color);
		assertEquals(RBColor.BLACK, n6.color);

		assertEquals(RBColor.RED, n3.color);
		validateTree(t);

		//
		n7 = (RedBlackNode) t.insert(7, "7");

		assertEquals(n6, n7.parent);
		assertEquals(n7, n6.right);
		assertTrue(isNilLeaf(n7.left));
		assertTrue(isNilLeaf(n7.right));

		assertEquals(RBColor.BLACK, n4.color);
		assertEquals(RBColor.BLACK, n2.color);
		assertEquals(RBColor.BLACK, n6.color);

		assertEquals(RBColor.RED, n7.color);
		validateTree(t);

	}

	@Test
	public void testSimpleDeletion() throws OperationNotPermitted {

		RedBlackTree t;

		t = new RedBlackTree();

		t.insert(1, "1");

		assertEquals(1, t.getRoot().key);
		assertEquals(null, t.remove(0));
		assertEquals("1", t.remove(1));
		assertEquals(null, t.remove(1));
		assertTrue(isNilLeaf(t.getRoot()));
	}

	@Test
	public void testDeleteMid() {

	}

	@Test
	public void testDeleteLots() throws OperationNotPermitted {

		RedBlackTree t;
		int n = 1024;

		t = new RedBlackTree();

		for (int i = 1; i < n; ++i) {
			t.insert(i, Integer.valueOf(i));
		}

		validateTree(t);

		// odds
		for (int i = 1; i < n; i += 2) {
			assertEquals(i, t.remove(i));
			assertEquals(null, t.remove(i));
			validateTree(t);
		}

		// evens
		for (int i = 2; i < n; i += 2) {
			assertEquals(i, t.remove(i));
			assertEquals(null, t.remove(i));
			validateTree(t);
		}

		// tree should now be empty
		assertTrue(isNilLeaf(t.getRoot()));

	}

	@Test
	public void testSortedInsertion() throws OperationNotPermitted {

		RedBlackTree t;
		int n = 1024;

		/* increasing */
		t = new RedBlackTree();
		validateTree(t);

		for (int i = 1; i < n; ++i) {
			t.insert(i, Integer.valueOf(i));
			validateTree(t);
		}

		/* decreasing */
		t = new RedBlackTree();
		validateTree(t);

		for (int i = n; i >= 1; --i) {
			t.insert(i, Integer.valueOf(i));
			validateTree(t);
		}

	}

	public static boolean isNilLeaf(BSTNode n) {
		if (n == null)
			return false;

		return (n.key == BinarySearchTree.NIL_KEY);
	}

	public static boolean isValidNilLeaf(BSTNode n) {
		if (n == null)
			return false;

		RedBlackNode r = (RedBlackNode) n;

		return (r.key == BinarySearchTree.NIL_KEY && r.color == RBColor.BLACK
				&& r.left == null && r.right == null);
	}

	public static void validateTree(RedBlackTree t) {
		BSTTests.validateTree(t);

		RedBlackNode root = (RedBlackNode) t.getRoot();

		if (root == null) {
			return;

		} else {

			assertEquals(RBColor.BLACK, root.color);

			int blackHeight = validateSubTree((RedBlackNode) t.getRoot());

			assertEquals(blackHeight, root.blackHeight);
		}
	}

	public static int validateSubTree(RedBlackNode n) {

		assert (n != null);

		RedBlackNode p = (RedBlackNode) n.parent;
		RedBlackNode l = (RedBlackNode) n.left;
		RedBlackNode r = (RedBlackNode) n.right;

		// check black height
		int l_blackHeight = 0;
		int r_blackHeight = 0;

		if (l != null)
			l_blackHeight = validateSubTree(l);

		if (r != null)
			r_blackHeight = validateSubTree(r);

		assertEquals(l_blackHeight, r_blackHeight);

		// check leaf/internal properties
		if (isNilLeaf(n)) {
			assertTrue(isValidNilLeaf(n));

		} else {
			/* n is an internal node */
			assertNotNull(n.left);
			assertNotNull(n.right);
		}

		// check colour properties
		if (n.color == RBColor.RED) {
			// red node cannot be the root
			assertNotNull(p);

			// make sure we don't have two reds in a row
			assertEquals(RBColor.BLACK, p.color);
		}

		return l_blackHeight + (n.color == RBColor.BLACK ? 1 : 0);
	}

}
