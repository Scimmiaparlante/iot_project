package it.unipi.iot.project;

import java.net.InetAddress;

public class RegisteredActuator extends RegisteredNode {
	
	public interface IActuatorAction {
		public String getActionCommand();
	}
	
	//enumeration containing the types of existing actuators
	public enum ActuatorType {
		ALARM, FIREALARM, PATIENTALARM, DASHBOARD, AIRCOND
	}
	
	//----- here we define the actions for each possible actuator type;
	// The system is going to accept an actuation command for an actuator only if it is in this list
	// It implements an interface to share the getActionCommand() function, that translated the command into a string for the actuator
	// and to be utilised by the application without knowing the actual type
	public enum AlarmAction implements IActuatorAction {
		ON, OFF;
		public String getActionCommand() {
			if(this == ON) return "cmd=1";
			else return "cmd=0";
		}
	}
	
	public enum PatAlarmAction implements IActuatorAction {
		RESET, LVL1, LVL2, LVL3;
		public String getActionCommand() {
			if(this == RESET) return "lvl=0";
			else if(this == LVL1) return "lvl=1";
			else if(this == LVL2) return "lvl=2";
			else return "lvl=3";
		}
	}
	
	public enum DashboardAction implements IActuatorAction {
		HB_HIGH, HB_LOW, MINPRESS_HIGH, MINPRESS_LOW, MAXPRESS_HIGH, MAXPRESS_LOW;
		public float val;
		public String getActionCommand() {
			String ret =  " (" + val +")";
			if(this == HB_HIGH) return "msg=HEARTBEAT TOO HIGH" + ret;
			else if(this == HB_LOW) return "msg=HEARTBEAT TOO LOW" + ret;
			else if(this == MINPRESS_HIGH) return "msg=DIASTOLIC PRESSURE TOO HIGH" + ret;
			else if(this == MINPRESS_LOW) return "msg=DIASTOLIC PRESSURE TOO LOW" + ret;
			else if(this == MAXPRESS_HIGH) return "msg=SYSTOLIC PRESSURE TOO HIGH" + ret;
			else /*if(this == MAXPRESS_LOW)*/ return "msg=SYSTOLIC PRESSURE TOO LOW" + ret;
		}
	}
	
	public enum AircondAction implements IActuatorAction {
		OFF, HOT, COLD;
		public String getActionCommand() {
			if(this == HOT) return "cmd=1";
			else if(this == COLD) return "cmd=2";
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