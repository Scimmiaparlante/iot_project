package it.unipi.iot.project;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

public class SensorReading {
	
	public int timestamp;
	public RegisteredSensor sensor;
	public float value;
	public String name;
	public String meas_unit;
		
	public SensorReading(String n, int t, float v, String mu, RegisteredSensor s) {
		timestamp = t;
		value = v;
		sensor = s;
		name = n;
		meas_unit = mu;
	}
	
	
	@SuppressWarnings("unchecked")
	static SensorReading[] fromJSONsenML(String json_text, RegisteredSensor sens) throws ParseException, NullPointerException
	{
		int base_time;
		String base_name;
		String base_um;
		
		JSONObject json_payload = (JSONObject) JSONValue.parseWithException(json_text);
		
		base_name = json_payload.get("bn").toString();
		base_time = Integer.parseInt(json_payload.get("bt").toString());
		base_um = json_payload.get("bu").toString();
		
		JSONArray e = (JSONArray) json_payload.get("e");
		SensorReading[] ret = new SensorReading[e.size()];
		
		for (int i = 0; i < e.size(); i++) {
			JSONObject record = (JSONObject) e.get(i);
						
			float val = Float.parseFloat(record.get("v").toString());
			int time = Integer.parseInt((String) record.getOrDefault("t", "0").toString());
			String um = record.getOrDefault("u", base_um).toString();
			String name = record.getOrDefault("n", "").toString();
			
			ret[i] = new SensorReading(base_name + name, base_time + time, val, um, sens);
		}
		
		return ret;
	}
	
	
	public String toString()
	{
		return (sensor.node_address + sensor.resource_path + " -> name: " + name +
				" - ts: " + timestamp + " - val:" + value + " " + meas_unit);
	}
	
	
}