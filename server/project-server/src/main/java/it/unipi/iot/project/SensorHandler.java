package it.unipi.iot.project;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import it.unipi.iot.project.RegisteredActuator.IActuatorAction;
import it.unipi.iot.project.Rules.Rule;


public class SensorHandler implements CoapHandler {
	
	ControlApplication app;
	RegisteredSensor sensor;
	
	public SensorHandler(ControlApplication a, RegisteredSensor s) 
	{
		app = a;
		sensor = s;
	}
	
	
	
	@Override public void onLoad(CoapResponse response) 
	{
		String content = response.getResponseText();
		//System.out.println("Received this: " + content);
		
		int base_time;
		float val = 0;
		//String base_name;
		
		try {
			JSONObject json_payload = (JSONObject) JSONValue.parseWithException(content);
			
			//base_name = json_payload.get("bn").toString();
			base_time = Integer.parseInt(json_payload.get("bt").toString());
			JSONArray e = (JSONArray) json_payload.get("e");
			
			for (int i = 0; i < e.size(); i++) {
				JSONObject record = (JSONObject) e.get(i);
				
				val = Float.parseFloat(record.get("v").toString());
				int time = Integer.parseInt(record.get("t").toString());
				
				app.storeReading(base_time + time, val, sensor);
			}
					
		} catch (org.json.simple.parser.ParseException e) {
			System.err.println("Bad data from " + sensor.resource_path);
			return;
		}
		
		//check if there are rules to apply
		for (Rule r : app.rules) {
			if(r.sensor == this.sensor) {
				
				final Rule fr = r;
				final IActuatorAction command = r.check(val);
				if(command != null) {
					//run in a separate thread, otherwise the post call hangs
					Thread t = new Thread() { public void run() { app.setActuation(fr.actuator, command);} };
					t.start();
				}
			}
		}
	}
	
	
	
	
	@Override public void onError() 
	{
		System.err.println("--------Failed--------");
	}
	
};
