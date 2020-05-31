package it.unipi.iot.project.Rules;

import it.unipi.iot.project.RegisteredActuator.ActuatorType;
import it.unipi.iot.project.RegisteredActuator.IActuatorAction;
import it.unipi.iot.project.RegisteredSensor.SensorType;
import it.unipi.iot.project.SensorReading;

/*
 * This class represents the interface for a rule description.
 * The check function shall contain the code to execute when an input from an associated sensor is received
 * The check function shall return null if no action must be performed, the action to be performed otherwise
 */

public interface IRuleAction {
	
	public IActuatorAction check(SensorReading reading);
	String getName();
	ActuatorType getActuatorType();	//NOTE: I need an interface for these because abstract variables are not allowed in Java
	SensorType getSensorType();
}
