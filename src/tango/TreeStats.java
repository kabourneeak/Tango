package tango;

/**
 * A simple class for tracking some statistics about a tree's life time.
 * 
 * @author Gregory Bint
 * 
 */
public class TreeStats {
	private long _numRotations;
	private long _numTraversals;
	private long _numOtherTraversals;

	public TreeStats() {
		reset();
	}

	public TreeStats(TreeStats src) {
		_numRotations = src._numRotations;
		_numTraversals = src._numTraversals;
		_numOtherTraversals = src._numOtherTraversals;
	}

	public long getRotations() {
		return _numRotations;
	}

	public long getTraversals() {
		return _numTraversals;
	}

	public long getOtherTraversals() {
		return _numOtherTraversals;
	}

	public void incRotations() {
		++_numRotations;
	}

	public void incTraversals() {
		++_numTraversals;
	}

	public void incOtherTraversals() {
		++_numOtherTraversals;
	}

	public void reset() {
		_numRotations = 0;
		_numTraversals = 0;
		_numOtherTraversals = 0;
	}

	@Override
	public String toString() {
		return "TreeStats: rot=" + _numRotations + ",trav=" + _numTraversals
				+ ",otrav=" + _numOtherTraversals;
	}
}
