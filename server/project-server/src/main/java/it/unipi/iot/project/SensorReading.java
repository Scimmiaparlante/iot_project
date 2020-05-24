package it.unipi.iot.project;

public class SensorReading {
	
	public int timestamp;
	public RegisteredSensor sensor;
	public float value;
	
	public SensorReading(int t, float v, RegisteredSensor s) {
		timestamp = t;
		value = v;
		sensor = s;		
	}
}