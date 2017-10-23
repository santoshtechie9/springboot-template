package com.techknowera.sb.controller;

import org.springframework.stereotype.Component;

@Component
public class MyMessage{
	public String returnMessage(){
		return "Hello, welcome to spring boot. I am doing good";
	}
}