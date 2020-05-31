package it.unipi.iot.project.Rules;

import it.unipi.iot.project.RegisteredActuator;
import it.unipi.iot.project.RegisteredActuator.IActuatorAction;
import it.unipi.iot.project.RegisteredSensor;
import it.unipi.iot.project.SensorReading;

/*
 * This class represents an applied rule
 * The rules can be applied to a specific sensor/actuator couple and contains a rule action to be checked
 * when the system received data from the sensor
 */

public class Rule {
	
	public RegisteredSensor sensor;
	public RegisteredActuator actuator;
	
	public IRuleAction action;
	
	public Rule(RegisteredSensor rs, RegisteredActuator ra, IRuleAction ac) 
	{
		sensor = rs;
		actuator = ra;
		action= ac;
	}
	
	
	public IActuatorAction check(SensorReading reading)
	{
		return action.check(reading);
	}
	
	public String toString()
	{
		return ("[" + sensor.node_address.toString().substring(1) + sensor.resource_path
				+ " -> " 
				+ actuator.node_address.toString().substring(1) + actuator.resource_path
				+ "] " +  action.getName());
	}
}
