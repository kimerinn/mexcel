package mexcel.exception;

public class ParserException extends MicroExcelException {
	private static final long serialVersionUID = -5158734321381157464L;

	public ParserException() {
	}

	public ParserException(String arg0) {
		super(arg0);
	}

	public ParserException(Throwable arg0) {
		super(arg0);
	}

	public ParserException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ParserException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
