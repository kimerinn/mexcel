package mexcel.table;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import mexcel.exception.MicroExcelException;
import mexcel.exception.ParserException;

public class Table {
	private static enum TOKEN_TYPE {POSITIVE_NUMBER, CELL, OPERATOR};
	public int MAX_ROWS = 26;//A-Z
	public int MAX_COLUMNS = 9;//1-9
	private int width, height;
	private Map<String, Cell> cells = new HashMap<>();
	private Map<String, Cell> computedCells = new HashMap<>();
	private ExpressionParser expressionParser = new ExpressionParser();
	
	public Table(int w, int h) throws Exception{
		if (h >= MAX_ROWS) {
			throw new MicroExcelException("Table can't contain more than " + MAX_ROWS + " rows");
		}
		
		if (w >= MAX_COLUMNS) {
			throw new MicroExcelException("Table can't contain more than " + MAX_COLUMNS + " columns");
		}

		this.width = w;
		this.height = h;
	}
	
	/**
	 * Computes table data. 
	 * Cell references are being resolved and expressions computed. If expression could not be computed, cell is filled with #{error_description} 
	 */
	public void computeCells() throws Exception {
		//firstly, copying all nonexpression cells to result
		for (Cell cell: cells.values()) {
			if (cell.getCellType() != Cell.CELL_TYPE.EXPRESSION_CELL) {
				computedCells.put(cell.getName(), new Cell(cell.getName(), cell.getRawValue()));
			}
		}
		//now compute expression cells
		for (Cell cell: cells.values()) {
			if (cell.getCellType() == Cell.CELL_TYPE.EXPRESSION_CELL) {
				try {
					expressionParser.evaluateAndStore(cell.getName(), cell.getExpVal().toUpperCase().replaceAll("\\p{javaWhitespace}+", ""));
				}
				catch (MicroExcelException e) {
					//wrong expression in cell, nothing to log 
				}
			}
		}
	}

	public void printCells(PrintStream out, PrintStream err) {
		out.println();

		for (int row = 0; row < this.height; row++) {
			for (int column = 0; column < this.width; column++) {
				String cellAddr = buildCellAddress(row, column);
				
				if (!computedCells.containsKey(cellAddr)) {
					out.print("null\t");
				}
				else {
					Cell cell = computedCells.get(cellAddr);
					out.print(cell.toString() + "\t");
				}
				
			}
			
			out.println();
		}
	}

	private String buildCellAddress(int row, int column) {
		char colSym = (char)('A' + column);
		return "" + colSym + "" + (row+1); 
	}
	
	public void setCell(int row, int col, String val) throws Exception {
		if ((row >= height) || (col >= width)) {
			throw new MicroExcelException("Table bounds violation");
		}
		
		String cellName = buildCellAddress(row, col); 

		if ("null".equals(val) || "".equals(val) || (val == null)) {
			return;//we do not store empty cells
		} 
		else {
			Cell cell = new Cell(cellName, val);
			cells.put(cellName, cell);
		}
	}

    private static final String POSITIVE_NUMBER_TOKEN_MASK = "\\d+";
    private static final Pattern POSITIVE_NUMBER_CHECKER = Pattern.compile(POSITIVE_NUMBER_TOKEN_MASK);
    private static final String CELL_TOKEN_MASK = "[A-Z]\\d";
    private static final Pattern CELL_CHECKER = Pattern.compile(CELL_TOKEN_MASK);
    private static final String OPERATOR_TOKEN_MASK = "[\\*\\/+-]";
    private static final Pattern OPERATOR_CHECKER = Pattern.compile(OPERATOR_TOKEN_MASK);

    class ExpressionParser {
    	/**
    	 * Evaluates expression and store result cell in computed cells collection 
		 * @param cellAddress Cell address
		 * @param epression Cell expression
		 * @throws MicroExcelException 
    	 */
	    public int evaluateAndStore(String cellAddress, String expression) throws MicroExcelException {
	    	ListIterator<String> expressionIterator = buildListIterator(expression); 
	    	
	    	try {
	    		int result = eval(expressionIterator);
	    		computedCells.put(cellAddress, new Cell(cellAddress, String.valueOf(result)));
		        return result;
	    	}
	    	catch (MicroExcelException e) {//expression contains error somewhere
	    		computedCells.put(cellAddress, new Cell(cellAddress, "#" + e.getMessage()));
	    		throw e;
	    	}
	    }
	    
	    private ListIterator<String> buildListIterator(String expression) {
	    	StringTokenizer expressionTokenizer = new StringTokenizer(expression, "+-*/", true);
	    	List<String> tokens = new LinkedList<>();
	    	
	    	while (expressionTokenizer.hasMoreTokens()) {
	    		tokens.add(expressionTokenizer.nextToken());
	    	}
	    	
	    	return tokens.listIterator(tokens.size());
	    }

	    private int eval(ListIterator<String> expressionTokenizer) throws MicroExcelException{
	    	if (!expressionTokenizer.hasPrevious()) {
	    		throw new ParserException("Unfinished expression");
	    	}
	    	
	    	String operand2Str = expressionTokenizer.previous();
	    	int operand1, operand2;
	    	
	    	switch (tokenType(operand2Str)) {
	    		case POSITIVE_NUMBER: operand2 = Integer.parseInt(operand2Str); break; 
	    		case CELL: operand2 = resolveCell(operand2Str); break; 
	    		case OPERATOR: throw new ParserException("Not enough operands");
				default: throw new ParserException("Unknown token: " + operand2Str);
	    	}
	    	//finishing calculation
			if (expressionTokenizer.hasPrevious()) {
				String operator = expressionTokenizer.previous();
				operand1 = eval(expressionTokenizer);
				return doOperation(operand1, operator, operand2);
			}
			else {
				return operand2;//expression start
			}
	    }
	    
	    private int resolveCell(String cellAddr) throws MicroExcelException {
	    	if (!cells.containsKey(cellAddr)) {
	    		throw new ParserException("Cell " + cellAddr + " is not defined");
	    	}
	    	else {
	    		Cell resolvedCell = computedCells.containsKey(cellAddr) ? computedCells.get(cellAddr) : cells.get(cellAddr);//use computed values, if possible
	    		
	    		switch (resolvedCell.getCellType()) {
	    			case TEXT_CELL: throw new ParserException("Can't use text in expression! Cell " + cellAddr);
	    			case NUMBER_CELL: return resolvedCell.getNumVal();
	    			case EXPRESSION_CELL: return evaluateAndStore(resolvedCell.getName(), 
	    				resolvedCell.getExpVal().toUpperCase().replaceAll("\\p{javaWhitespace}+", ""));
	    			case ERROR_CELL: throw new MicroExcelException("Expression error in cell " + cellAddr);
	    			default: throw new ParserException("Unknown cell type: " + resolvedCell.getCellType());
	    		}
	    	}
	    }
	    
	    private int doOperation(int operand1, String operator, int operand2) throws ParserException {
	    	if ("+".equals(operator)) {
	    		return operand1 + operand2;
	    	}
	    	else if ("-".equals(operator)) {
	    		return operand1 - operand2;
	    	}
	    	else if ("*".equals(operator)) {
	    		return operand1 * operand2;
	    	}
	    	else if ("/".equals(operator)) {
	    		return operand1 / operand2;
	    	}
	    	else {
	    		throw new ParserException("Unknown operator: " + operator);
	    	}
	    }
	    
	    private TOKEN_TYPE tokenType(String token) throws ParserException {
	    	if (POSITIVE_NUMBER_CHECKER.matcher(token).matches()) {
	    		return TOKEN_TYPE.POSITIVE_NUMBER;
	    	}
	    	else if (CELL_CHECKER.matcher(token).matches()) {
	    		return TOKEN_TYPE.CELL;
	    	}
	    	else if (OPERATOR_CHECKER.matcher(token).matches()) {
	    		return TOKEN_TYPE.OPERATOR;
	    	}
	    	else {
	    		throw new ParserException("Unknown token in expression: " + token);
	    	}
	    }
	}
}
