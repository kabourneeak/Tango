package console;

public enum TreeType {

	BASIC("Basic"), REDBLACK("RedBlack"), SPLAY("Splay"), RBTANGO("rbTango"), UTANGO(
			"uTango"), UNKNOWN("Unknown");

	private String _str;

	private TreeType(String str) {
		_str = str;
	}

	public static TreeType fromString(String str) {
		str = str.trim();

		for (TreeType t : TreeType.values()) {
			if (str.equalsIgnoreCase(t._str))
				return t;
		}

		return UNKNOWN;
	}

	@Override
	public String toString() {
		return _str;
	}

}
