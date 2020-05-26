package it.unipi.iot.project.Rules;

import it.unipi.iot.project.RegisteredActuator;
import it.unipi.iot.project.RegisteredActuator.IActuatorAction;
import it.unipi.iot.project.RegisteredSensor;

public class Rule {
	
	public RegisteredSensor sensor;
	public RegisteredActuator actuator;
	
	IRuleAction action;
	
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
}
