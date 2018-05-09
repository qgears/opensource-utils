package hu.qgears.commons.test;

import org.junit.Test;

import hu.qgears.commons.Pair;
import hu.qgears.commons.UtilEquals;
import org.junit.Assert;

public class UtilEqualsTest {

	
	@Test
	public void testSafeEqualsNull(){
		Assert.assertTrue(UtilEquals.safeEquals(null, null));
	}

	@Test
	public void testSafeEqualsWithNullA(){
		Assert.assertFalse(UtilEquals.safeEquals(null, new Object()));
	}
	@Test
	public void testSafeEqualsWithNullB(){
		Assert.assertFalse(UtilEquals.safeEquals(new Object(), null));
	}

	@Test
	public void testPairWithNull(){
		Object o = new Object();
		Pair<Object, Object> p1 = new Pair<>(o,null);
		Pair<Object, Object> p2 = new Pair<>(o,null);
		Assert.assertTrue(p1.equals(p2));
	}
	
}
