package it.unipi.iot.project;

import java.util.ArrayList;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import it.unipi.iot.project.RegisteredActuator.ActuatorType;
import it.unipi.iot.project.RegisteredActuator.AircondAction;
import it.unipi.iot.project.RegisteredActuator.AlarmAction;
import it.unipi.iot.project.RegisteredActuator.DashboardAction;
import it.unipi.iot.project.RegisteredActuator.IActuatorAction;
import it.unipi.iot.project.RegisteredActuator.PatAlarmAction;
import it.unipi.iot.project.RegisteredSensor.SensorType;
import it.unipi.iot.project.Rules.IRuleAction;
import it.unipi.iot.project.Rules.Rule;


public class ControlApplication {
	
	CoapServer server;
	CoapRemoteDirectoryResource remoteDir_res;		//reference t the remote directoy (here are the lists of sensors and actuators)
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
	
	//get the sensors' readings, filtered by sensor type and minimum time instant
	public ArrayList<SensorReading> getReadings(SensorType type, Integer min_time) 
	{
		ArrayList<SensorReading> ret = new ArrayList<SensorReading>();

		for (SensorReading sr : sensor_data)
			if (type == null || sr.sensor.type == type)
				if (min_time == null || sr.timestamp >= min_time)
					ret.add(sr);
		
		return ret;
	}
	
	//function to perform an action on a specified sensor.
	//The origin parameter is an ID of the entity that triggered the command, in case the actuator needs to know it (e.g. dashboard)
	public ActuationResult setActuation(RegisteredActuator actuator, IActuatorAction action, String origin) 
	{
		CoapClient client = new CoapClient("coap://[" + actuator.node_address.toString().substring(1) + "]/" + actuator.resource_path);

		CoapResponse response = client.post(action.getActionCommand() + "&or=" + origin, MediaTypeRegistry.TEXT_PLAIN);

		if(response == null || !response.isSuccess())
			return ActuationResult.FAILURE;

		return ActuationResult.SUCCESS;
	}
	
	
	//function to apply a rule to specific sensor and actuator
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
	
	public ArrayList<Rule> getRules()
	{
		return rules;
	}
	
	public IRuleAction[] getRuleActions()
	{
		return rule_actions;
	}
	
	public ArrayList<RegisteredSensor> getRegisteredSensors()
	{
		return remoteDir_res.sensor_list;
	}
	
	public ArrayList<RegisteredActuator> getRegisteredActuators()
	{
		return remoteDir_res.actuator_list;
	}
	
	//----------Here you need to define the control rules---------------------------------------------------------
	
	private void init_rules() {
		
		rules = new ArrayList<Rule>();
		
		/*
		 * Here is a list of the existing applicable rules in the system.
		 * The rule actions must be defined creating an (anonymous) object that implements the IRuleAction interface.
		 * Each object represent a rule that can be applied to a real sensor and actuator at runtime
		 */
		
		rule_actions = new IRuleAction[] {
				
				//RULE #0 - FIRE ALARM ACTIVATION
				new IRuleAction() {
					
					@Override public String getName() { return "Fire-alarm trigger rule"; }
					@Override public ActuatorType getActuatorType() { return ActuatorType.FIREALARM; }
					@Override public SensorType getSensorType() { return SensorType.FIRE; }
					
					@Override public IActuatorAction check(SensorReading reading) {
						if(reading.value == 1) { return AlarmAction.ON; }
						return null;
					}
				},
				
				//RULE #1 - PATIENT ALARM ACTIVATION FOR HEARTBEAT
				new IRuleAction() {
					
					@Override public String getName() { return "Patient-alarm for heartbeat trigger rule"; }
					@Override public ActuatorType getActuatorType() { return ActuatorType.PATIENTALARM; }
					@Override public SensorType getSensorType() { return SensorType.HEARTBEAT; }
					
					@Override public IActuatorAction check(SensorReading reading) {
						if(reading.value > 140 || reading.value < 35) { return PatAlarmAction.LVL3; }
						else if(reading.value > 120 || reading.value < 45) { return PatAlarmAction.LVL2; }
						else if(reading.value > 100 || reading.value < 55) { return PatAlarmAction.LVL1; }
						return null;
					}
				},
				
				//RULE #2 - PATIENT ALARM ACTIVATION FOR PRESSURE
				new IRuleAction() {
					
					@Override public String getName() { return "Patient-alarm for pressure trigger rule"; }
					@Override public ActuatorType getActuatorType() { return ActuatorType.PATIENTALARM; }
					@Override public SensorType getSensorType() { return SensorType.BLOODPRESSURE; }
					
					@Override public IActuatorAction check(SensorReading reading) {
						if(reading.name.equals("pressureMin")) {
							if(reading.value < 30) { return PatAlarmAction.LVL3; }
							else if(reading.value < 40) { return PatAlarmAction.LVL2; }
							else if(reading.value < 50) { return PatAlarmAction.LVL1; }
						} else if(reading.name.equals("pressureMax")) {
							if(reading.value > 180) { return PatAlarmAction.LVL3; }
							else if(reading.value > 165) { return PatAlarmAction.LVL2; }
							else if(reading.value > 150) { return PatAlarmAction.LVL1; }
						}
						return null;
					}
				},
				
				//RULE #3 - DASHBOARD MESSAGES FOR PRESSURE
				new IRuleAction() {
					
					@Override public String getName() { return "Dashboard messages for pressure sensors"; }
					@Override public ActuatorType getActuatorType() { return ActuatorType.DASHBOARD; }
					@Override public SensorType getSensorType() { return SensorType.BLOODPRESSURE; }
					
					@Override public IActuatorAction check(SensorReading reading) {
						if(reading.name.equals("pressureMin")) {
							if(reading.value < 50) { DashboardAction r = DashboardAction.MINPRESS_LOW; r.val = reading.value; return r; }
						} else if(reading.name.equals("pressureMax")) {
							if(reading.value > 150) { DashboardAction r = DashboardAction.MAXPRESS_HIGH; r.val = reading.value; return r; }
						}
						return null;
					}
				},
				
				//RULE #4 - DASHBOARD MESSAGES FOR HEARTBEAT
				new IRuleAction() {
					
					@Override public String getName() { return "Dashboard messages for heartbeat sensors"; }
					@Override public ActuatorType getActuatorType() { return ActuatorType.DASHBOARD; }
					@Override public SensorType getSensorType() { return SensorType.HEARTBEAT; }
					
					@Override public IActuatorAction check(SensorReading reading) {
						if(reading.value > 100) { DashboardAction r = DashboardAction.HB_HIGH; r.val = reading.value; return r; }
						else if(reading.value < 55) { DashboardAction r = DashboardAction.HB_LOW; r.val = reading.value; return r; }
						return null;
					}
				},
					
				//RULE #5 - AIR CONDITIONING BASED ON TEMPERATURET
				new IRuleAction() {
					
					@Override public String getName() { return "Air conditioning activation"; }
					@Override public ActuatorType getActuatorType() { return ActuatorType.AIRCOND; }
					@Override public SensorType getSensorType() { return SensorType.TEMPERATURE; }
					
					@Override public IActuatorAction check(SensorReading reading) {
						if(reading.value > 26) { return AircondAction.COLD;}
						else if(reading.value < 15) { return AircondAction.HOT;}
						return AircondAction.OFF;
					}
				},
		};
		
		/* END OF LIST */
		
	}

	
}
