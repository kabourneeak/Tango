package console;

public enum SequenceType {

	FILE("File"), INCREASING("Increasing"), DECREASING("Decreasing"), PERMUTATION(
			"Permutation"), RANDOM("Random"), SQUAREROOT("Sqrt"), UNKNOWN(
			"Unknown");

	private String _str;

	private SequenceType(String str) {
		_str = str;
	}

	public static SequenceType fromString(String str) {
		str = str.trim();

		for (SequenceType t : SequenceType.values()) {
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
