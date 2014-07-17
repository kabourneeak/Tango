package gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import tango.BSTNode;
import tango.BasicBST;
import tango.BinarySearchTree;
import tango.OperationNotPermitted;
import tango.RBColor;
import tango.RedBlackNode;
import tango.RedBlackTango;
import tango.RedBlackTree;
import tango.SplayTree;
import tango.TangoNode;
import tango.TangoTree;
import tango.UnbalancedTango;

/**
 * Controller for Tree GUI. Inspired by/Heavily adapted from classroom code by
 * Lou Nel
 * 
 * @author Gregory Bint
 * 
 */
public class TreeEditor extends JPanel implements ActionListener {

	private static final long serialVersionUID = 3753498074280946475L;

	private BinarySearchTree tree;
	private TreeEditorGUI view;

	/*
	 * Application Startup
	 */
	public static void main(String args[]) {

		TreeEditorGUI frame = new TreeEditorGUI("Tree Viewer");

		frame.runGUI();
	}

	/*
	 * Construction
	 */
	public TreeEditor(TreeEditorGUI aView) {
		view = aView;

		// create initial tree
		newTangoTree();

		setSize(600, 500);
		setBackground(Color.white);
	}

	/*
	 * Event Handlers
	 */
	public void insertItems(String itemsList) {
		String items[] = itemsList.split(",");

		try {

			for (int i = 0; i < items.length; i++) {
				String itemString = items[i].trim();

				if (itemString.length() > 0) {
					tree.insert(Integer.parseInt(itemString), itemString);
				}
			}
		} catch (OperationNotPermitted e) {
			// do nothing, some trees do not allow insertion
		}

		update();
	}

	public void removeItems(String itemsList) {
		String items[] = itemsList.split(",");

		try {
			for (int i = 0; i < items.length; i++) {
				String itemString = items[i].trim();

				if (itemString.length() > 0) {
					tree.remove(Integer.parseInt(itemString));
				}
			}
		} catch (OperationNotPermitted e) {
			// do nothing, some trees do not allow removal
		}

		update();
	}

	public void findItems(String itemsList) {
		String items[] = itemsList.split(",");

		for (int i = 0; i < items.length; i++) {
			String itemString = items[i].trim();

			if (itemString.length() > 0) {
				System.out.print("Searching for " + itemString + "... ");
				Object res = tree.search(Integer.parseInt(itemString));
				System.out.println("found " + res + ".");
				System.out.println(tree.getStats());
			}
		}

		update();
	}

	@Override
	public void paintComponent(Graphics aPen) {
		super.paintComponent(aPen);

		// switch to Graphics2D pen so we can control stroke widths better etc.
		Graphics2D aPen2D = (Graphics2D) aPen;
		aPen2D.setStroke(new BasicStroke(1.0f));

		// tree.draw(aPen2D);
		drawTree(aPen2D);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	}

	public void newBasicTree() {
		tree = new BasicBST();

		tree.initializePerfectTree(5);

		update();
	}

	public void newRedBlackTree() {
		tree = new RedBlackTree();

		tree.initializePerfectTree(5);

		update();
	}

	public void newSplayTree() {
		tree = new SplayTree();

		tree.initializePerfectTree(5);

		update();
	}

	public void newUTangoTree() {
		TangoTree tango = new UnbalancedTango();

		tango.initializePerfectTree(5);

		tree = tango;
	}

	public void newTangoTree() {
		TangoTree tango = new RedBlackTango();

		tango.initializePerfectTree(5);

		tree = tango;
	}

	public void insertIncreasing(int low, int high) {
		try {
			for (int i = low; i <= high; ++i) {
				tree.insert(i, Integer.toString(i));
			}

		} catch (OperationNotPermitted e) {
			// do nothing; some trees don't allow insertion
		}

		update();
	}

	/*
	 * Update functions
	 */
	public void update() {
		repaint();
		view.requestFocus();
	}

	/*
	 * Tree Drawing functions
	 */
	public static final int LEVEL_HEIGHT = 40;
	public static final int NODE_SIZE = 10;

	public static final Color LABEL_COLOUR = Color.white;
	public static final int LABEL_FONT_SIZE = 9;
	public static final Font LABEL_FONT = new Font("Serif", Font.PLAIN,
			LABEL_FONT_SIZE);
	public static final Color EDGE_COLOUR = Color.black;
	public static final Color UNCOLOURED_NODE_COLOUR = new Color(220, 220, 220);
	public static final Color RED_NODE_COLOUR = Color.red;
	public static final Color BLACK_NODE_COLOUR = Color.black;
	public static final Color MARKED_NODE_COLOUR = Color.green;

	private void drawTree(Graphics2D aPen) {
		drawSubTree(aPen, tree.getRoot(), NODE_SIZE, 0, this.getWidth(), 0, 0);
	}

	private void drawSubTree(Graphics2D aPen, BSTNode n, int height, int left,
			int right, int px, int py) {

		if (n == null)
			return;

		int width = (right - left);
		double l_size = (n.left == null ? 1 : n.left.size);
		double r_size = (n.right == null ? 1 : n.right.size);
		double r_ratio = (r_size / l_size);

		// the weighted "middle" of our slab
		int mid = left + (int) (width / (r_ratio + 1));

		/* calculate my position */
		int nx = mid;
		int ny = height;

		/* draw children first */
		int cy = height + LEVEL_HEIGHT;

		drawSubTree(aPen, n.left, cy, left, mid, nx, ny);

		drawSubTree(aPen, n.right, cy, mid, right, nx, ny);

		/* draw edge before node, for clipping reasons */
		if (n.parent != null) {
			aPen.setColor(EDGE_COLOUR);
			aPen.drawLine(nx, ny, px, py);
		}

		/* draw node */
		drawNode(aPen, n, nx, ny);

	}

	private void drawNode(Graphics2D aPen, BSTNode n, int x, int y) {
		if (n == null) {
			return;
		}

		/* decide on drawing parameters */

		// set defaults
		Color col = UNCOLOURED_NODE_COLOUR;
		int node_size = NODE_SIZE;
		boolean drawOval = true;

		if (n instanceof RedBlackNode) {

			RedBlackNode r = (RedBlackNode) n;

			if (r.color == RBColor.BLACK) {
				col = BLACK_NODE_COLOUR;
			} else {
				col = RED_NODE_COLOUR;
			}

			if (r.key == BinarySearchTree.NIL_KEY) {
				drawOval = false;
				node_size = NODE_SIZE / 2;
			}
		}

		if (n instanceof TangoNode) {
			TangoNode t = (TangoNode) n;

			if (t.marked == true) {
				col = MARKED_NODE_COLOUR;
			}
		}

		/* draw the node */
		aPen.setColor(col);

		if (drawOval) {
			aPen.fillOval(x - node_size, y - node_size, node_size * 2,
					node_size * 2);
		} else {
			aPen.fillRect(x - node_size, y - node_size, node_size * 2,
					node_size * 2);
		}

		/* draw label, if not a nil leaf */
		if (n.key != Integer.MIN_VALUE) {
			aPen.setFont(LABEL_FONT);
			aPen.setColor(LABEL_COLOUR);

			FontMetrics metrics = aPen.getFontMetrics();

			String label = Integer.toString(n.key);
			int labelWidth = metrics.stringWidth(label);

			aPen.drawString(label, x - (labelWidth / 2), y
					+ (LABEL_FONT_SIZE / 2));
		}
	}
}
