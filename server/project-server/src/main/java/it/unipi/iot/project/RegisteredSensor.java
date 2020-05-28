package it.unipi.iot.project;

import java.net.InetAddress;

import org.eclipse.californium.core.CoapObserveRelation;

public class RegisteredSensor extends RegisteredNode {
	
	public enum SensorType {
		TEMPERATURE
	}
	
	public SensorType type;
	public CoapObserveRelation coap_rel;
	
	public RegisteredSensor(InetAddress addr, SensorType _type, String path, CoapObserveRelation rel) {
		super(addr, path);
		this.type = _type;
		this.coap_rel = rel;
	}
	
	public String toString() 
	{
		return "TYPE: " + type.toString() + " - " + super.toString();
	}
}