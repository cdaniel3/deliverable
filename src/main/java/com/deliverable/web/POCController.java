package com.deliverable.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller(value = "sayHelloController")
@RequestMapping("/saySomething")
public class POCController {

	// http://localhost:8080/cd-deliverable-0.1.0/helloworld/saySomething/sayhello 
	
	@RequestMapping("/sayhello")
	public ModelAndView sayHello() {
		Map<String, String> modelData = new HashMap<String, String>();
		modelData.put("msg", "Hello World !!");
		return new ModelAndView("helloworld", modelData);
	}
	
}
