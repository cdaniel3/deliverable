package com.deliverable.poc.junit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.deliverable.model.Priority;
import com.deliverable.model.Ticket;

public class TestJUnit2 {
	String message = "High";	
	
   @Test
   public void testGetPriority() {	

		Ticket t = new Ticket();
		Priority p = new Priority();
		p.setValue("High");
		t.setPriority(p);
      System.out.println("Inside testGetPriority()");    
      assertEquals(message, t.getPriority().getValue());     
   }
}