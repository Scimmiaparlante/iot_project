package it.unipi.iot.project.Rules;

import it.unipi.iot.project.RegisteredActuator.ActuatorType;
import it.unipi.iot.project.RegisteredActuator.IActuatorAction;
import it.unipi.iot.project.RegisteredSensor.SensorType;
import it.unipi.iot.project.SensorReading;

public interface IRuleAction {
	
	public IActuatorAction check(SensorReading reading);
	String getName();
	ActuatorType getActuatorType();	//NOTE: I need an interface for these because abstract variables are not allowed in Java
	SensorType getSensorType();
}
