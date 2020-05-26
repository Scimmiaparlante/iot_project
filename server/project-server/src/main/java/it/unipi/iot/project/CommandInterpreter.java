package it.unipi.iot.project;

import java.util.ArrayList;
import java.util.Scanner;

import it.unipi.iot.project.ControlApplication.ActuationResult;
import it.unipi.iot.project.RegisteredActuator.AlarmAction;
import it.unipi.iot.project.RegisteredActuator.IActuatorAction;
import it.unipi.iot.project.RegisteredSensor.SensorType;
import it.unipi.iot.project.Rules.IRuleAction;

public class CommandInterpreter {
	
	static ControlApplication app;
	
	public static void main(String[] args) {
		
		app = new ControlApplication();
		
		//------------- MAIN LOOP ----------------------
		
		String input;
		String words[];
		Scanner sc = new Scanner(System.in);
		Boolean read_again = true;
		
		System.out.println("CLI started");
		
		while(read_again) {
			
			System.out.print("\n>");
			input = sc.nextLine();
			
			words = input.split(" ");
				
			if(words.length < 1)
				continue;
			
			switch (words[0]) {
			case "exit":
				read_again = false;
				break;
			case "list":
				commandList(words);
				break;
			case "read":
				commandRead(words);
				break;
			case "set":
				commandSet(words);
				break;
			case "rules":
				commandRules(words);
				break;
			case "apply":
				commandApply(words);
				break;
			case "help":
			default:
				commandHelp();
				break;
			}
			
		}
		
        sc.close();
        app.stop();
        System.exit(0);
	}


	private static void commandHelp() {
		System.out.println("The available commands are:");
		System.out.println("list [sensors | actuators] \t\t\t show a list of the nodes (or sensors/actuators only) and their number");
		System.out.println("read [<sensor_type>] [-t <timestamp>] \t\t show the readings of the sensors (filter by type and min time)");
		System.out.println("set <res_number> <value> \t\t\t set the resource <res_number> with value <value>");
		System.out.println("apply <rule_num> <sensor_num> <actuator_num> \t apply the specified rule to the specified sensor and actuator");
		System.out.println("help \t\t\t\t\t\t display this message");
		System.out.println("exit \t\t\t\t\t\t terminate the program");
		System.out.print("--------------------------------------------------------------------------------------");
	}
	
	
	private static void commandList(String[] params) 
	{		
		boolean correct = false;
		int numSensors = app.remoteDir_res.sensor_list.size();
		int numActuators = app.remoteDir_res.actuator_list.size();
		
		if (params.length < 2 || params[1].equals("sensors")) {
			correct = true;
			System.out.println("SENSORS:  (" + numSensors + ")");
			for (int i = 0; i < app.remoteDir_res.sensor_list.size(); i++)
				System.out.println(i + ") " + app.remoteDir_res.sensor_list.get(i).toString());
		}
		
		if (params.length < 2 || params[1].equals("actuators")) {
			correct = true;
			System.out.println("ACTUATORS:  (" + numActuators + ")");
			for (int i = 0; i < app.remoteDir_res.actuator_list.size(); i++)
				System.out.println(i + ") " + app.remoteDir_res.actuator_list.get(i).toString());		
		}
		
		if(!correct)
			System.out.println("What is '" + params[1] + "'?");
	}
	
	
	private static void commandRead(String[] words) 
	{		
		SensorType type = null;
		Integer min_time = null;
		
		int words_left = words.length - 1;
		int i = 1;
		while (words_left > 0) {
			if (words[i].equals("-t") && words_left > 1) {
				try {
					min_time = Integer.parseInt(words[i+1]);
					words_left--; i++;
				} catch (NumberFormatException  e) {
					System.out.print("Bad time format, use a timestamp");
					return;
				}
			} else {	//I'm in this case also if there's only '-t'. Not an issue for the moment.
				try {
					type = SensorType.valueOf(words[i].toUpperCase());
				} catch (IllegalArgumentException e) {
					System.out.print("Bad sensor type");
					return;
				}
			}
			words_left--; i++;
		}
		
		ArrayList<SensorReading> res = app.getReadings(type, min_time);
		
		for (SensorReading sr : res) {
			System.out.println(sr.sensor.node_address + sr.sensor.resource_path + 
								" - ts: " + sr.timestamp + " - val:" + sr.value);
		}	
	}
	
	
	private static void commandSet(String[] words) 
	{
		int act_num;
		RegisteredActuator act;
		//get sensor number
		try {
			act_num = Integer.parseInt(words[1]);
			
			if(act_num >= app.remoteDir_res.actuator_list.size() || act_num < 0)
				throw new ArrayIndexOutOfBoundsException();
			
			act = app.remoteDir_res.actuator_list.get(act_num);
			
		} catch (NumberFormatException | IndexOutOfBoundsException  e) {
			System.out.print("Bad actuator number");
			return;
		}
		
		//get action
		ActuationResult res;
		try {
			String action = words[2];
			IActuatorAction aa;
			
			switch (act.type) {
			case ALARM:
				aa = AlarmAction.valueOf(action.toUpperCase());
				break;
			default:
				return;
			}

			res = app.setActuation(act, aa);			
			
		} catch (IllegalArgumentException | IndexOutOfBoundsException e) {
			System.out.print("Bad actuator action");
			return;
		}
		
		if(res != ActuationResult.SUCCESS)
			System.out.print("Something went wrong during the actuation");
	}
	
	private static void commandRules(String[] words) 
	{
		for (int i = 0; i < app.rule_actions.length; i++) {
			IRuleAction ra = app.rule_actions[i];
			
			System.out.println(i + ") [" + ra.getSensorType() + " -> " + ra.getActuatorType() + "] " +  ra.getName());
		}
	}
	
	
	private static void commandApply(String[] words) 
	{
		RegisteredActuator actuator;
		RegisteredSensor sensor;
		IRuleAction rule_act;
		try {
			
			rule_act = app.rule_actions[Integer.parseInt(words[1])];
			sensor = app.remoteDir_res.sensor_list.get(Integer.parseInt(words[2]));
			actuator = app.remoteDir_res.actuator_list.get(Integer.parseInt(words[3]));
			
		} catch (IllegalArgumentException | IndexOutOfBoundsException e) {
			System.out.print("Bad command: specify the 3 numbers");
			return;
		}
		
		boolean success = app.setRule(sensor, actuator, rule_act);
		
		if(success)
			System.out.print("Ruled applied successfully");
		else
			System.out.print("Ruled application failed");
	}
	
}
