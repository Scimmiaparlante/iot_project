package it.unipi.iot.project;

import java.util.ArrayList;

import org.eclipse.californium.core.CoapServer;

import it.unipi.iot.project.RegisteredSensor.SensorType;


public class ControlApplication {
	
	CoapServer server;
	CoapRemoteDirectoryResource remoteDir_res;
	ArrayList<SensorReading> sensor_data;
	
	public ControlApplication()	
	{
		sensor_data = new ArrayList<SensorReading>();
		
		System.out.println("-- Control Application --");
		
		System.out.println("Starting the Coap Server for the remote directory...");
		
		server = new CoapServer();
		remoteDir_res = new CoapRemoteDirectoryResource("remote_dir", this);
		server.add(remoteDir_res);
		server.start();
		
		System.out.println("Coap server started!");
		
	}
	
	
	public void stop() 
	{
		server.stop();
	}
	
	
	public void storeReading(int time, float val, RegisteredSensor sens) 
	{
		sensor_data.add(new SensorReading(time, val, sens));
	}
	
	
	public ArrayList<SensorReading> getReadings() 
	{
		return getReadings(null, null);
	}
	
	public ArrayList<SensorReading> getReadings(SensorType type) 
	{
		return getReadings(type, null);
	}
	
	public ArrayList<SensorReading> getReadings(Integer min_time) 
	{
		return getReadings(null, min_time);
	}
	
	
	public ArrayList<SensorReading> getReadings(SensorType type, Integer min_time) 
	{
		ArrayList<SensorReading> ret = new ArrayList<SensorReading>();

		for (SensorReading sr : sensor_data)
			if (type == null || sr.sensor.type == type)
				if (min_time == null || sr.timestamp >= min_time)
					ret.add(sr);
		
		return ret;
	}
	
	
	
}
