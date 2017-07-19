package com.deliverable.poc.junit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestJUnit {
	@Test
	
	public void testAdd() {
		String str = "working fine";
		assertEquals("working fine", str);
	}

}
