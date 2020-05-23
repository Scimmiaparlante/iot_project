package it.unipi.iot.project;

import java.net.InetAddress;

public class RegisteredActuator extends RegisteredNode {
	
	public enum ActuatorType {
		ALARM
	}
	
	public ActuatorType type;
	
	public RegisteredActuator(InetAddress addr, ActuatorType _type, String path) {
		super(addr, path);
	
		this.type = _type;
	}	
}