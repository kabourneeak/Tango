package tango;

/**
 * Unbalanced Tango Trees
 * 
 * This implementation of the tango tree auxiliary tree structure does not
 * attempt to balance at all.
 * 
 * @author Gregory Bint
 * 
 */
public class UnbalancedTango extends TangoTree {

	@Override
	protected TangoNode tangoSplitImpl(TangoNode n, BSTNode vRoot) {
		if (isRoot(n))
			return n;

		if (n == vRoot)
			return n;

		// mark v to make it a fake root
		boolean vMark = isMarked(vRoot);

		if (!vMark)
			markNode(vRoot);

		TangoNode p;

		while (!isMarked(n)) {
			p = (TangoNode) n.parent;

			// rotate such that n gets dragged up
			if (isLeftChild(n)) {
				rotateRight(p);
			} else {
				rotateLeft(p);
			}
		}

		// the mark is transferred to n, so that is what we must unmark
		if (!vMark)
			unmarkNode(n);

		return n;
	}

	@Override
	protected TangoNode tangoMergeImpl(TangoNode n) {

		// now that n considers its children again, we should update min/max
		updateMinMax(n);

		return n;
	}

}
