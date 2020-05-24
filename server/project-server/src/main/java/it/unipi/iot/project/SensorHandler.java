package it.unipi.iot.project;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


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
		System.out.println("Looks like someone sent me this: " + content);
		
		int base_time;
		//String base_name;
		
		try {
			JSONObject json_payload = (JSONObject) JSONValue.parseWithException(content);
			
			//base_name = json_payload.get("bn").toString();
			base_time = Integer.parseInt(json_payload.get("bt").toString());
			JSONArray e = (JSONArray) json_payload.get("e");
			
			for (int i = 0; i < e.size(); i++) {
				JSONObject record = (JSONObject) e.get(i);
				
				float val = Float.parseFloat(record.get("v").toString());
				int time = Integer.parseInt(record.get("t").toString());
				
				app.storeReading(base_time + time, val, sensor);
			}
					
		} catch (org.json.simple.parser.ParseException e) {
			System.err.println("Bad data from " + sensor.resource_path);
			return;
		}
		
		//parsa il messaggio
		//app.storeReading(....)
	}
	
	
	
	
	@Override public void onError() 
	{
		System.err.println("--------Failed--------");
	}
	
};