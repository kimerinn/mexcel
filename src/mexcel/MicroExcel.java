package mexcel;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import mexcel.exception.MicroExcelException;
import mexcel.table.Table;

public class MicroExcel {
	private Table table;
	
	public MicroExcel() {
	}
	
	/**
	 * Reads table from input stream. First line contains table height and width. 
	 * Next lines contain cell data. Cells could contain nothing (null), non-negative number, text or expression (including cell references).
	 * 
	 * expression ::= ’=’ term {operation term}*
	 * term ::= cell_reference | nonnegative_number
	 * cell_reference ::= [A-Za-z][0-9] —
	 * operation ::= ’+’ | ’-’ | ’*’ | ’/’
	 * text ::= ’\’’ {printable_character}
	 * 
	 * Data delimiter is whitespace.
	 * 
	 * Table data example:
	 * 3 4
	 * 12 =C2 3 ’Sample
	 * =A1+B1*C1/5 =A2*B1 =B3-C3 ’Spread
	 * ’Test =4-3 5 ’Sheet
	 * 
	 * @param in Input stream
	 * @param err Error stream
	 */
	public void readTable(InputStream in, PrintStream err) throws Exception {
		try (Scanner scanner = new Scanner(in)) {
			int tableHeight = scanner.nextInt();
			int tableWidth = scanner.nextInt();
			table = new Table(tableWidth, tableHeight);
			scanner.nextLine();
			
			for (int row = 0; row < tableHeight; row++) {
				if (!scanner.hasNextLine()) {
					throw new MicroExcelException("Not enough rows in table: " + row);
				}
				
				for (int column = 0; column < tableWidth; column++) {
					if (!scanner.hasNext()) {
						throw new MicroExcelException("Not enough columns in table: " + row + "," + column);
					}
					
					table.setCell(row, column, scanner.next());
				}
			}
		}
	}
	
	/**
	 * Computes table data and prints result to output. 
	 * @param out Output stream
	 * @param err Error stream
	 * @throws Exception 
	 */
	public void compute(PrintStream out,  PrintStream err) throws Exception {
		table.computeCells();
		table.printCells(out, err);
	}
	
	public static void main(String args[]) {
		MicroExcel mExcel = new MicroExcel();
		
		try {
			mExcel.readTable(System.in, System.err);
			mExcel.compute(System.out, System.err);
		} 
		catch (MicroExcelException e) {
			System.err.println(e.getMessage());
		} 
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
}
