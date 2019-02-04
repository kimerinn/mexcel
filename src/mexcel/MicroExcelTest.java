package mexcel;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class MicroExcelTest {
	MicroExcel me;
	ByteArrayInputStream in;
	ByteArrayOutputStream outBuf;
	ByteArrayOutputStream errBuf;
	PrintStream out;
	PrintStream err;

	@BeforeEach
	void setUp() throws Exception {
		me = new MicroExcel();
		outBuf = new ByteArrayOutputStream();
		errBuf = new ByteArrayOutputStream();
		out = new PrintStream(outBuf);
		err = new PrintStream(errBuf);
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	final void testReadTable() throws Exception {
		MicroExcel me = new MicroExcel();
		StringBuffer inBuf = new StringBuffer("2 2\n" +
											  "1 2\n" +
											  "3 4\n");
		in = new ByteArrayInputStream(inBuf.toString().getBytes());
		
		me.readTable(in, err);
		me.compute(out, err);
		assertEquals("\r\n" +
						"1	2	\r\n" +
						"3	4	\r\n", new String(outBuf.toByteArray()));
	}

	@Test
	final void testCompute() throws Exception {
		MicroExcel me = new MicroExcel();
		StringBuffer inBuf = new StringBuffer("2 2\n" +
											  "=1+3 =1-1\n" +
											  "=3*4 =4-2+6/2\n");
		in = new ByteArrayInputStream(inBuf.toString().getBytes());
		
		me.readTable(in, err);
		me.compute(out, err);
		assertEquals("\r\n" +
						"4	0	\r\n"+
						"12	4	\r\n", new String(outBuf.toByteArray()));
	}

	@Test
	final void testAddressCompute() throws Exception {
		MicroExcel me = new MicroExcel();
		StringBuffer inBuf = new StringBuffer("2 2\n" +
											  "=a2+3   =1-1+5\n" +
											  "=b2*4-2 =4-2+6/2\n");
		in = new ByteArrayInputStream(inBuf.toString().getBytes());
		
		me.readTable(in, err);
		me.compute(out, err);
		assertEquals("\r\n" +
						"17	5	\r\n" + 
						"14	4	\r\n", new String(outBuf.toByteArray()));
	}

	@Test
	final void testEtalon() throws Exception {
		MicroExcel me = new MicroExcel();
		StringBuffer inBuf = new StringBuffer("3 4\n" +
											  "12			=C2 	3		'Sample\n" + 
											  "=A1+B1*C1/5 	=A2*B1	=B3-C3 	'Spread\n" + 
											  "'Test		=4-3 	5 		'Sheet\n");
		in = new ByteArrayInputStream(inBuf.toString().getBytes());
		
		me.readTable(in, err);
		me.compute(out, err);
		assertEquals("\r\n" +
		"12	-4	3	Sample\t\r\n" +
		"4	-16	-4	Spread\t\r\n" +
		"Test	1	5	Sheet\t\r\n", new String(outBuf.toByteArray()));
	}

	@Test
	final void testErrors() throws Exception {
		MicroExcel me = new MicroExcel();
		StringBuffer inBuf = new StringBuffer("2 2\n" +
											  "=b1+3 'aaa\n" +
											  "=4    =3+4+\n");
		in = new ByteArrayInputStream(inBuf.toString().getBytes());
		
		me.readTable(in, err);
		me.compute(out, err);
		assertEquals("\r\n" +
						"#Can't use text in expression! Cell B1	aaa	\r\n" +
						"4	#Not enough operands	\r\n", new String(outBuf.toByteArray()));
	}

	@Test
	final void testEmptyCells() throws Exception {
		MicroExcel me = new MicroExcel();
		StringBuffer inBuf = new StringBuffer("2 2\n" +
											  "null 'aaa\n" +
											  "=4 null\n");
		in = new ByteArrayInputStream(inBuf.toString().getBytes());
		
		me.readTable(in, err);
		me.compute(out, err);
		assertEquals("\r\n" +
						"null	aaa	\r\n" +
						"4	null	\r\n", new String(outBuf.toByteArray()));
	}
}
