package mexcel.exception;

public class MicroExcelException extends Exception {
	private static final long serialVersionUID = 6566179140944394031L;

	public MicroExcelException() {
	}

	public MicroExcelException(String arg0) {
		super(arg0);
	}

	public MicroExcelException(Throwable arg0) {
		super(arg0);
	}

	public MicroExcelException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public MicroExcelException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
