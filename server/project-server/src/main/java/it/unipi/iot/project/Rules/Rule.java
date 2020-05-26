package it.unipi.iot.project.Rules;

import it.unipi.iot.project.RegisteredActuator;
import it.unipi.iot.project.RegisteredActuator.IActuatorAction;
import it.unipi.iot.project.RegisteredSensor;

public class Rule {
	
	RegisteredSensor sensor;
	RegisteredActuator actuator;
	
	IRuleAction action;
	
	public Rule(RegisteredSensor rs, RegisteredActuator ra, IRuleAction ac) 
	{
		sensor = rs;
		actuator = ra;
		action= ac;
	}
	
	
	public Boolean check(IActuatorAction command, float input)
	{
		return action.check(command, input);
	}
}
