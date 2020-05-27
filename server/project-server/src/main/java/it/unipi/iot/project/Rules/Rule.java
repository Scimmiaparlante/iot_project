package it.unipi.iot.project.Rules;

import it.unipi.iot.project.RegisteredActuator;
import it.unipi.iot.project.RegisteredActuator.IActuatorAction;
import it.unipi.iot.project.RegisteredSensor;

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
	
	
	public IActuatorAction check(float input)
	{
		return action.check(input);
	}
	
	public String toString()
	{
		return ("[" + sensor.node_address.toString().substring(1) + sensor.resource_path
				+ " -> " 
				+ actuator.node_address.toString().substring(1) + actuator.resource_path
				+ "] " +  action.getName());
	}
}
