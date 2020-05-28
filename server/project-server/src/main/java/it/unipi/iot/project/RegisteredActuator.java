package it.unipi.iot.project;

import java.net.InetAddress;

public class RegisteredActuator extends RegisteredNode {
	
	public interface IActuatorAction {
		public String getActionCommand();
	}
	
	public enum ActuatorType {
		ALARM, FIREALARM
	}
	
	//----- here we define the actions for each possible actuator type;
	public enum AlarmAction implements IActuatorAction {
		ON, OFF;
		public String getActionCommand() {
			if(this == ON) return "cmd=1";
			else return "cmd=0";
		}
	}
	//-----------------------------------------------------------------
	
	
	public ActuatorType type;
	
	public RegisteredActuator(InetAddress addr, ActuatorType _type, String path) {
		super(addr, path);
	
		this.type = _type;
	}
	
	public String toString() 
	{
		return "TYPE: " + type.toString() + " - " + super.toString();
	}
}