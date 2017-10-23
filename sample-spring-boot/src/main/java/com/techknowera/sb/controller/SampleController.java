package com.techknowera.sb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class SampleController {
	
	@Autowired
	MyMessage msg;
	
	@RequestMapping("/hello")
	public String home(){
		return msg.returnMessage();
	}
}


