package it.unipi.iot.project;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;

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
		
		SensorReading[] readings = null;
		try {
			readings = SensorReading.fromJSONsenML(content, sensor);
			
			app.storeReading(readings);
					
		} catch (org.json.simple.parser.ParseException | NullPointerException e) {
			System.err.println("Bad data from " + sensor.resource_path);
			return;
		}
		
		//check if there are rules to apply
		for (SensorReading sr : readings)
			for (Rule r : app.getRules())
				if(r.sensor == this.sensor) {
					
					//there is a rule, apply it (we need final variables to create the new thread utilising their values)
					final Rule fr = r;
					final IActuatorAction command = r.check(sr);
					final String origin = sr.sensor.node_address.toString().substring(1);
					if(command != null) {
						//run in a separate thread, otherwise the post call hangs
						Thread t = new Thread() { public void run() { app.setActuation(fr.actuator, command, origin);} };
						t.start();
					}
				}
	}
	
	
	
	
	@Override public void onError() 
	{
		System.err.println("--------Failed--------");
	}
	
};
