package tango;

/**
 * The basic operations that should be supported by any Binary Search Tree
 * 
 * @author Gregory Bint
 * 
 */
public interface BinarySearchTree {

	public static final int NIL_KEY = Integer.MIN_VALUE;

	/**
	 * Insert a new key and value into the tree. Or, if the key is already
	 * present in the tree, simply update the value
	 * 
	 * @param key
	 *            the key to insert
	 * @param value
	 *            the value to associate with the given key
	 * @return the BSTNode object that is created or updated
	 */
	public BSTNode insert(int key, Object value) throws OperationNotPermitted;

	/**
	 * Removes the node with the given key from the tree, returning the payload
	 * attached to it. If the key is not found, the tree is unchanged.
	 * 
	 * @param key
	 *            the key to remove
	 * @return the value attached to the removed node.
	 */
	public Object remove(int key) throws OperationNotPermitted;

	/**
	 * Searches for the given key, returning the node matching that key if such
	 * a key is present in the tree, or the last node seen along the search path
	 * to that key, which may be null if the tree is empty.
	 * 
	 * @param key
	 *            the key to search for
	 */
	public Object search(int key);

	/**
	 * @return the root node of the tree
	 */
	public BSTNode getRoot();

	/**
	 * @return a copy of the current TreeStats object.
	 */
	public TreeStats getStats();

	/**
	 * Creates a perfect binary search tree
	 * 
	 * @param levels
	 *            the number of levels to produce in the tree. A tree with k
	 *            levels will have nodes 1 .. 2^k -1
	 */
	public void initializePerfectTree(int levels);
}
