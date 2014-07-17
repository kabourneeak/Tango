package console;

public class ItemNotFoundException extends Exception {

	private static final long serialVersionUID = 6715195786597388174L;

	public ItemNotFoundException() {

	}

	public ItemNotFoundException(String msg) {
		super(msg);
	}
}
