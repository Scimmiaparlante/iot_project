package it.unipi.iot.project.Rules;

import it.unipi.iot.project.RegisteredActuator.ActuatorType;
import it.unipi.iot.project.RegisteredActuator.IActuatorAction;
import it.unipi.iot.project.RegisteredSensor.SensorType;

public interface IRuleAction {
	
	Boolean check(IActuatorAction command, float input);
	String getName();
	ActuatorType getActuatorType();	//NOTE: I need an interface for this because abstract variables are not allowed in Java
	SensorType getSensorType();
}
