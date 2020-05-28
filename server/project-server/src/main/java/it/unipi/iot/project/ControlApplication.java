package it.unipi.iot.project;

import java.util.ArrayList;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import it.unipi.iot.project.RegisteredActuator.ActuatorType;
import it.unipi.iot.project.RegisteredActuator.AlarmAction;
import it.unipi.iot.project.RegisteredActuator.IActuatorAction;
import it.unipi.iot.project.RegisteredSensor.SensorType;
import it.unipi.iot.project.Rules.IRuleAction;
import it.unipi.iot.project.Rules.Rule;


public class ControlApplication {
	
	CoapServer server;
	CoapRemoteDirectoryResource remoteDir_res;
	ArrayList<SensorReading> sensor_data;
	IRuleAction[] rule_actions;
	ArrayList<Rule> rules;
	
	public enum ActuationResult {SUCCESS, FAILURE};
	
	public ControlApplication()	
	{
		sensor_data = new ArrayList<SensorReading>();
		init_rules();
		
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
	
	
	public void storeReading(String name, int time, float val, String meas_unit, RegisteredSensor sens) 
	{
		sensor_data.add(new SensorReading(name, time, val, meas_unit, sens));
	}
	
	public void storeReading(SensorReading reading)
	{
		sensor_data.add(reading);
	}
	
	public void storeReading(SensorReading[] readings)
	{
		for (SensorReading r : readings)
			sensor_data.add(r);			
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
	
	
	public ActuationResult setActuation(RegisteredActuator actuator, IActuatorAction action) 
	{
		CoapClient client = new CoapClient("coap://[" + actuator.node_address.toString().substring(1) + "]/" + actuator.resource_path);

		CoapResponse response = client.post(action.getActionCommand(), MediaTypeRegistry.TEXT_PLAIN);

		if(response == null || !response.isSuccess())
			return ActuationResult.FAILURE;

		return ActuationResult.SUCCESS;
	}
	
	
	public boolean setRule(RegisteredSensor sensor, RegisteredActuator actuator, IRuleAction rule_act) 
	{
		if (actuator.type != rule_act.getActuatorType()) {
			System.out.println("Bad actuator type");
			return false;
		} else if (sensor.type != rule_act.getSensorType()) {
			System.out.println("Bad sensor type");
			return false;
		}
		
		//create rule
		rules.add(new Rule(sensor, actuator, rule_act));
		
		return true;
	}
	
	
	
	//----------Here you need to define the control rules----------ru-----------------------------------------------
	
	private void init_rules() {
		
		rules = new ArrayList<Rule>();
		
		rule_actions = new IRuleAction[] {
				
				//RULE #1 - FIRE ALARM ACTIVATION
				new IRuleAction() {
					
					@Override public String getName() { return "Fire-alarm trigger rule"; }
					@Override public ActuatorType getActuatorType() { return ActuatorType.FIREALARM; }
					@Override public SensorType getSensorType() { return SensorType.FIRE; }
					
					@Override public IActuatorAction check(float input) {
						if(input == 1) { return AlarmAction.ON; }
						return null;
					}
				},			
					
	
		};
		
	}

	
}
