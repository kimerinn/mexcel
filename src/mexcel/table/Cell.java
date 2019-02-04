package mexcel.table;

import mexcel.exception.MicroExcelException;

public class Cell {
	public static enum CELL_TYPE {
		TEXT_CELL, NUMBER_CELL, EXPRESSION_CELL, ERROR_CELL
	};

	private String rawValue;
	private String name;
	private String textVal;
	private String expVal;
	private String errVal;
	private int numVal;
	private CELL_TYPE cellType;

	public Cell(String name, String rawVal) throws MicroExcelException {
		this.name = name;
		this.rawValue = rawVal;
		parse(rawVal);
	}

	private void parse(String rawVal) throws MicroExcelException {
		if (rawVal.startsWith("'")) {
			this.cellType = CELL_TYPE.TEXT_CELL;
			this.textVal = rawVal.substring(1);
		} else if (rawVal.startsWith("=")) {
			this.cellType = CELL_TYPE.EXPRESSION_CELL;
			this.expVal = rawVal.substring(1);
		} else if (rawVal.startsWith("#")) {
			this.cellType = CELL_TYPE.ERROR_CELL;
			this.errVal = rawVal.substring(1);
		} else {
			try {
				this.cellType = CELL_TYPE.NUMBER_CELL;
				this.numVal = Integer.valueOf(rawVal);
			} catch (NumberFormatException e) {
				throw new MicroExcelException("Unknown data format");
			}
		}
	}

	public String getName() {
		return this.name;
	}

	public CELL_TYPE getCellType() {
		return this.cellType;
	}

	public String getRawValue() {
		return rawValue;
	}

	public String getTextVal() {
		return textVal;
	}

	public String getExpVal() {
		return expVal;
	}

	public int getNumVal() {
		return numVal;
	}

	public String getErrVal() {
		return errVal;
	}

	public String toString() {
		switch (getCellType()) {
		case TEXT_CELL:
			return getTextVal();
		case EXPRESSION_CELL:
			return getExpVal();
		case NUMBER_CELL:
			return String.valueOf(getNumVal());
		case ERROR_CELL:
			return "#" + getErrVal();// we want to render errors with "#"
		default:
			return rawValue;
		}
	}
}
