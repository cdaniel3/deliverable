package com.deliverable.poc.junit;

import junit.framework.TestCase;

public class JavaTest extends TestCase {
	protected int value1, value2;
	
	//assign values
	protected void setUp() {
		value1 = 3;
		value2 = 3;
	}
	
	//test method and add 2 values
	public void testAdd() {
		double result = value1 + value2;
		assertTrue(result == 6);
	}
}
