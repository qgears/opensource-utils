package hu.qgears.commons.test;

import hu.qgears.commons.RationalValue;

import org.junit.Assert;
import org.junit.Test;


public class TestRationalValue {
	@Test
	public void testGcd()
	{
		Assert.assertEquals(2, RationalValue.gcd(14, 8));
		Assert.assertEquals(3, RationalValue.gcd(27, 33));
		RationalValue a=new RationalValue(1, 3);
		RationalValue b=new RationalValue(1, 4);
		Assert.assertEquals("7/12", ""+a.add(b));
		Assert.assertEquals("4/3", ""+a.div(b));
		a=new RationalValue(1, 3);
		b=new RationalValue(4, 3);
		Assert.assertEquals("1/4", ""+a.div(b));
	}
}
