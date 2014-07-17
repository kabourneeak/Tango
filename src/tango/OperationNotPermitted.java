package tango;

public class OperationNotPermitted extends Exception {

	private static final long serialVersionUID = 541433075263733159L;

	public OperationNotPermitted(String msg) {
		super(msg);
	}
}
