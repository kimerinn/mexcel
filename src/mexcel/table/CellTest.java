package mexcel.table;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import mexcel.exception.MicroExcelException;

class CellTest {

	@Test
	final void testGetCellNumType() throws Exception{
		Cell cell = new Cell("a1", "12");
		assertEquals(cell.getCellType(), Cell.CELL_TYPE.NUMBER_CELL);
	}

	@Test
	final void testGetCellTextType() throws Exception{
		Cell cell = new Cell("a1", "'dummy");
		assertEquals(cell.getCellType(), Cell.CELL_TYPE.TEXT_CELL);
	}

	@Test
	final void testGetCellExpType() throws Exception{
		Cell cell = new Cell("a1", "=12+b2");
		assertEquals(cell.getCellType(), Cell.CELL_TYPE.EXPRESSION_CELL);
	}

	@Test
	final void testGetCellErrType() throws Exception{
		Cell cell = new Cell("a1", "#this is error");
		assertEquals(cell.getCellType(), Cell.CELL_TYPE.ERROR_CELL);
	}

	@Test
	final void testGetCellWrongType() throws Exception {
		try {
			Cell cell = new Cell("a1", "!12");
			fail("Should not get this line");
		}
		catch (MicroExcelException e) {
			assertEquals(e.getMessage(), "Unknown data format");
		}
	}

	@Test
	final void testGetTextVal()  throws Exception {
		Cell cell = new Cell("a1", "'dummy");
		assertEquals(cell.getTextVal(), "dummy");
	}

	@Test
	final void testGetExpVal() throws Exception {
		Cell cell = new Cell("a1", "=12+b2");
		assertEquals(cell.getExpVal(), "12+b2");
	}

	@Test
	final void testGetNumVal() throws Exception {
		Cell cell = new Cell("a1", "12");
		assertEquals(cell.getNumVal(), 12);
	}

	@Test
	final void testGetErrVal() throws Exception {
		Cell cell = new Cell("a1", "#this is error");
		assertEquals(cell.getErrVal(), "this is error");
	}

	@Test
	final void testToString() throws Exception {
		Cell cell = new Cell("a1", "#this is error");
		assertEquals(cell.toString(), "#this is error");
	}
}
