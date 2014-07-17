package tests;

import static org.junit.Assert.*;

import org.junit.Test;

import tango.BSTNode;
import tango.BasicBST;
import tango.BinarySearchTree;
import tango.OperationNotPermitted;

public class BSTTests {

	@Test
	public void testBalancedInsertion() throws OperationNotPermitted {
		BasicBST t = new BasicBST();
		BSTNode n1, n2, n3, n4, n5, n6, n7;

		n4 = t.insert(4, "4");
		assertEquals(4, n4.key);
		n2 = t.insert(2, "2");
		assertEquals(2, n2.key);
		n6 = t.insert(6, "6");
		assertEquals(6, n6.key);
		n1 = t.insert(1, "1");
		assertEquals(1, n1.key);
		n3 = t.insert(3, "3");
		assertEquals(3, n3.key);
		n5 = t.insert(5, "5");
		assertEquals(5, n5.key);
		n7 = t.insert(7, "7");
		assertEquals(7, n7.key);

		assertEquals(n4, t.getRoot());
		assertEquals(n4, n2.parent);
		assertEquals(n4, n6.parent);
		assertEquals(n2, n1.parent);
		assertEquals(n2, n3.parent);
		assertEquals(n6, n5.parent);
		assertEquals(n6, n7.parent);
	}

	@Test
	public void testDelete1() throws OperationNotPermitted {
		BasicBST t = new BasicBST();

		BSTNode n4 = t.insert(4, "4");

		assertEquals(n4, t.getRoot());
		assertEquals("4", t.search(4));
		assertEquals("4", t.remove(4));
		assertNull(t.search(4));
		assertNull(t.getRoot());
	}

	@Test
	public void testDeleteMid() throws OperationNotPermitted {
		BasicBST t = createPerfectTree(3);

		assertEquals(6, t.search(6));
		assertEquals(6, t.remove(6));
		assertNull(t.remove(6));
	}

	@Test
	public void testDeletion() throws OperationNotPermitted {
		BasicBST t;

		int L = 4;
		t = createPerfectTree(L);
		int n = (int) (Math.pow(2, L + 1)) - 1;

		for (int i = 1; i < n; ++i) {
			assertEquals(i, t.remove(i));
			validateTree(t);
		}

		assertEquals(n, t.getRoot().key);
	}

	@Test
	public void testPerfectTree() {

		BasicBST t;

		t = createPerfectTree(0);
		validateTree(t);

		t = createPerfectTree(1);
		validateTree(t);

		t = createPerfectTree(2);
		validateTree(t);

		t = createPerfectTree(3);
		validateTree(t);

		t = createPerfectTree(4);
		validateTree(t);
	}

	@Test
	public void testRotation() {

		BasicBST t = createPerfectTree(4);

		BSTNode origRoot = t.getRoot();

		t.rotateLeft(t.getRoot());
		validateTree(t);
		assertFalse(t.getRoot() == origRoot);

		t.rotateRight(t.getRoot());
		validateTree(t);
		assertTrue(t.getRoot() == origRoot);

		t.rotateRight(t.getRoot());
		validateTree(t);
		assertFalse(t.getRoot() == origRoot);

		t.rotateLeft(t.getRoot());
		validateTree(t);
		assertTrue(t.getRoot() == origRoot);

	}

	/*
	 * Utilities
	 */

	public static boolean isNilLeaf(BSTNode n) {
		if (n == null)
			return false;

		return (n.key == BinarySearchTree.NIL_KEY);
	}

	public static BasicBST createPerfectTree(int levels) {
		assert (levels >= 0);

		BasicBST t = new BasicBST();

		BSTNode r = createPerfectSubtree(levels, 1);

		t.setRoot(r);

		return t;
	}

	public static BSTNode createPerfectSubtree(int levels, int nextId) {
		if (levels == 0) {
			BSTNode n = new BSTNode(nextId, new Integer(nextId));
			++nextId;

			return n;
		} else {
			BSTNode l = createPerfectSubtree(levels - 1, nextId);
			nextId += Math.pow(2, levels) - 1;

			BSTNode p = new BSTNode(nextId, new Integer(nextId));
			++nextId;

			BSTNode r = createPerfectSubtree(levels - 1, nextId);

			l.parent = p;
			p.left = l;

			r.parent = p;
			p.right = r;

			p.size = l.size + r.size + 1;

			return p;
		}
	}

	public static void validateTree(BinarySearchTree t) {

		if (t.getRoot() == null)
			return;

		else {
			BSTNode r = t.getRoot();

			assertNull(r.parent);

			validateSubTree(r);
		}
	}

	public static int validateSubTree(BSTNode n) {
		if (n == null) {
			System.err.println("n is null");
			return -1;
		}

		int l_size = 0;
		BSTNode l = n.left;

		if (l != null) {
			assertEquals(n, l.parent);
			assertTrue(l.key < n.key);

			l_size = validateSubTree(l);
		}

		int r_size = 0;
		BSTNode r = n.right;

		if (r != null) {
			assertEquals(n, r.parent);

			// ignore RedBlack nil leaves for this check
			if (!isNilLeaf(r))
				assertTrue(r.key > n.key);

			r_size = validateSubTree(r);
		}

		int v_size = (l_size + r_size + 1);

		assertEquals(v_size, n.size);

		/* return calculated size */
		return v_size;
	}
}
