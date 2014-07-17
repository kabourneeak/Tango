package tango;

public enum RBColor {
	RED("RED"), BLACK("BLACK");

	private String _txtName;

	private RBColor(String txtName) {
		this._txtName = txtName;
	}

	@Override
	public String toString() {
		return this._txtName;
	}
}
