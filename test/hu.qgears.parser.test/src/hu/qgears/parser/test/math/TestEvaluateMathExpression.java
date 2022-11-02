package hu.qgears.parser.test.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import hu.qgears.parser.impl.ParseException;
import hu.qgears.parser.math.EvaluateMathExpression;

public class TestEvaluateMathExpression {

	private static final double EPS = 0.0001d;

	@Test
	public void evalConst() throws Exception {
		EvaluateMathExpression e = new EvaluateMathExpression(null);
		
		assertEquals(7,  e.evaluate("7"),EPS);
	}

	@Test
	public void evalNegConst() throws Exception {
		EvaluateMathExpression e = new EvaluateMathExpression(null);
		
		assertEquals(-7,  e.evaluate("-7"),EPS);
		assertEquals(-1.234,  e.evaluate("-1.234"),EPS);
	}
	
	@Test
	public void evalDoubleConst() throws Exception {
		EvaluateMathExpression e = new EvaluateMathExpression(null);
		assertEquals(7.45,  e.evaluate("7.45"),EPS);
	}
	@Test
	public void evalAddition() throws Exception {
		EvaluateMathExpression e = new EvaluateMathExpression(null);
		assertEquals(4,  e.evaluate("2 +2 "),EPS);
		assertEquals(4,  e.evaluate("2+2"),EPS);
		assertEquals(4,  e.evaluate(" 2 + 2 "),EPS);
		assertEquals(6,  e.evaluate("2+ 2 + 2 "),EPS);
	}
	

	@Test
	public void evalConstWithBrackets() throws Exception {
		EvaluateMathExpression e = new EvaluateMathExpression(null);
		assertEquals(2,  e.evaluate("(2) "),EPS);
		assertEquals(3.14,  e.evaluate("(3.14) "),EPS);
	}
	@Test
	public void evalAdditionWithBrackets() throws Exception {
		EvaluateMathExpression e = new EvaluateMathExpression(null);
		assertEquals(4,  e.evaluate("2 +2 "),EPS);
		assertEquals(4,  e.evaluate("(2+2)"),EPS);
		assertEquals(4,  e.evaluate(" 2 + 2 "),EPS);
		assertEquals(6,  e.evaluate("2+ (2 + 2) "),EPS);
	}
	

	@Test
	public void evalSubs() throws Exception {
		EvaluateMathExpression e = new EvaluateMathExpression(null);
		assertEquals(4,  e.evaluate("6 - 2 "),EPS);
		assertEquals(4,  e.evaluate(" (6 - 2) "),EPS);
		assertEquals(4,  e.evaluate("6 - (2) "),EPS);

		assertEquals(9,  e.evaluate("6 + 5 - 2 "),EPS);

		assertEquals(5,  e.evaluate("6 - (2 - 1) "),EPS);
		assertEquals(3,  e.evaluate("6 - 5 + 2 "),EPS);
		
	}
	
	@Test
	public void evalSubs2() throws Exception {
		EvaluateMathExpression e = new EvaluateMathExpression(null);
		assertEquals(3,  e.evaluate("6 - 2 - 1 "),EPS);
		assertEquals(2,  e.evaluate("6 - 2 - 1 -1 "),EPS);
		assertEquals(4,  e.evaluate("6 - (2 - 1) -1 "),EPS);
	}

	@Test
	public void evalMul() throws Exception {
		EvaluateMathExpression e = new EvaluateMathExpression(null);
		assertEquals(8,  e.evaluate("2 * 4 "),EPS);
		assertEquals(8,  e.evaluate("2*4"),EPS);
		assertEquals(-5,  e.evaluate("2.5*-2"),EPS);
		assertEquals(10,  e.evaluate("2 * 4 +2 "),EPS);
		assertEquals(26.5,  e.evaluate("2.5 + 4*6 "),EPS);
		
		assertEquals(8,  e.evaluate("(2 * 4) "),EPS);
		assertEquals(12,  e.evaluate("2 * (4 +2) "),EPS);
		assertEquals(39,  e.evaluate("(2.5 + 4)*6 "),EPS);
		
		assertEquals(8.8,  e.evaluate("(2.5 - 0.3)*4 "),EPS);
		
	}

	@Test
	public void evalVar() throws Exception {
		EvaluateMathExpression e = new EvaluateMathExpression(s ->
		{
			switch (s) {
			case "VAR":
				return 12d;
			default:
				return null;
			}
		});
		assertEquals(12,  e.evaluate("VAR"),EPS);
		assertEquals(24,  e.evaluate("VAR + VAR"),EPS);
		assertEquals(10,  e.evaluate("VAR - 1 * 2"),EPS);
		assertEquals(9,  e.evaluate("VAR - 1 - 2"),EPS);
	}
	
	
	@Test
	public void evalVarUndefined() throws Exception {
		EvaluateMathExpression e = new EvaluateMathExpression(s -> null);
		try {
			e.evaluate("VAR2");
			fail("No exception generated in case of undefined var");
		} catch (Exception ex) {
			assertTrue(ex.getMessage().contains("VAR2"));
		}
	}
	@Test
	public void evalVarNoResolver() throws Exception {
		EvaluateMathExpression e = new EvaluateMathExpression(null);
		try {
			e.evaluate("VAR2");
			fail("No exception generated in case of undefined var");
		} catch (Exception ex) {
			assertTrue(ex.getMessage().contains("VAR2"));
		}
	}

	@Test
	public void evalNegNumbers() throws Exception {
		EvaluateMathExpression e = new EvaluateMathExpression(s -> 5d);
		assertEquals(1,  e.evaluate("-1 + 2"),EPS);
		assertEquals(-8,  e.evaluate("-1 * 6 - 2 "),EPS);

		assertEquals(10,  e.evaluate(" VAR - ( VAR * -1 ) "),EPS);
		
	}

	@Test
	public void evalDivide() throws Exception {
		EvaluateMathExpression e = new EvaluateMathExpression(null);
		assertEquals(0.5,  e.evaluate("1 / 2"),EPS);
		assertEquals(-.25,  e.evaluate("1 / 2 / -2 "),EPS);
	}
	
	@Test
	public void evalBracketed() throws Exception {
		EvaluateMathExpression e = new EvaluateMathExpression(null);
		assertEquals(16, e.evaluate("(3 + 3.5 + 1.5) * 2"), EPS);
		assertEquals(-2.5, e.evaluate("(3 + 3.5 - 1.5) / -2"), EPS);
		assertEquals(-4, e.evaluate("(3 + 3.5 - -1.5) / -2"), EPS);
	}
	
	@Test(expected = ParseException.class)
	public void evalMultiLine() throws Exception {
		EvaluateMathExpression e = new EvaluateMathExpression(null);
		e.evaluate("7.45\n7.45");
	}
	
	@Test(expected = ParseException.class)
	public void evalMissingBracket() throws Exception {
		EvaluateMathExpression e = new EvaluateMathExpression(null);
		e.evaluate("(2 +3 + 6");
	}
	
	@Test(expected = ParseException.class)
	public void evalMissingBracket2() throws Exception {
		EvaluateMathExpression e = new EvaluateMathExpression(null);
		e.evaluate("2 +3) + 6");
	}
	
}
