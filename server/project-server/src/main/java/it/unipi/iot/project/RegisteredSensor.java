package it.unipi.iot.project;

import java.net.InetAddress;

public class RegisteredSensor extends RegisteredNode {
	
	public enum SensorType {
		TEMPERATURE
	}
	
	public SensorType type;
	
	public RegisteredSensor(InetAddress addr, SensorType _type, String path) {
		super(addr, path);
		this.type = _type;
	}
}