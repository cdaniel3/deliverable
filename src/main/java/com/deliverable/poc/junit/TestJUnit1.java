package com.deliverable.poc.junit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.deliverable.model.Ticket;

public class TestJUnit1 {
	String message = "ticketname";	
	
   @Test
   public void testPrintMessage() {	

		Ticket t = new Ticket();
		t.setName("ticketname");
      System.out.println("Inside testPrintMessage()");    
      assertEquals(message, t.getName());     
   }
   
   @Test
   public void testEquality() {
	   String s = "a string";
	   assertEquals("a string", s);
   }
   
   @Test
   // Notice the "test" prefix to the method name isn't needed here b/c of the annotation
   public void checkEquality() {
	   String s = "a string";
	   assertEquals("a string", s);
   }
}
