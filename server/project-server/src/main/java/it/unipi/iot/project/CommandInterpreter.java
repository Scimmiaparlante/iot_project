package it.unipi.iot.project;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import it.unipi.iot.project.ControlApplication.ActuationResult;
import it.unipi.iot.project.RegisteredActuator.ActuatorType;
import it.unipi.iot.project.RegisteredActuator.AircondAction;
import it.unipi.iot.project.RegisteredActuator.AlarmAction;
import it.unipi.iot.project.RegisteredActuator.DashboardAction;
import it.unipi.iot.project.RegisteredActuator.IActuatorAction;
import it.unipi.iot.project.RegisteredActuator.PatAlarmAction;
import it.unipi.iot.project.RegisteredSensor.SensorType;
import it.unipi.iot.project.Rules.IRuleAction;
import it.unipi.iot.project.Rules.Rule;

/*
 * This class implements the CLI of the application
 */

public class CommandInterpreter {
	
	static ControlApplication app;			//refereence to the application class
	
	public static void main(String[] args) {
		
		app = new ControlApplication();
		
		//------------- MAIN LOOP ----------------------
		
		String input;
		Scanner sc = new Scanner(System.in);
		Boolean read_again = true;
		
		System.out.println("CLI started");
		
		//-- MAIN LOOP --//
		while(read_again) {
			
			System.out.print("\n>");
			input = sc.nextLine();
			
			//what I read is parsed by this function
			read_again = parseCommand(input);			
		}
		
		//System shutdown
        sc.close();
        app.stop();
        System.exit(0);
	}
	
	//This function dispatches the command execution to the approrpiate function, reading the first word
	private static boolean parseCommand(String input)
	{
		String[] words = input.split(" ");

		if(words.length < 1)		//no input
			return true;
		
		//see commandHelp() for details on each command
		switch (words[0]) {
		case "exit":
			return false;			//false will make the main loop end
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
		case "unapply":
			commandUnapply(words);
			break;
		case "script":
			commandScript(words);
			break;
		case "commands":
			commandCommands(words);
			break;
		case "":
			break;
		case "help":
		default:
			commandHelp();
			break;
		}
		
		return true;
	}


	//show the help message
	private static void commandHelp() {
		System.out.println("The available commands are:");
		System.out.println("list [sensors | actuators] \t\t\t show a list of the nodes (or sensors/actuators only) and their number");
		System.out.println("read [<sensor_type>] [-t <timestamp>] \t\t show the readings of the sensors (filter by type and min time)");
		System.out.println("set <res_number> <value> [params] \t\t set resource <res_number> with value <value>. Parameters depend on type");
		System.out.println("rules [applied] \t\t\t\t list the existing (or applied) rules");
		System.out.println("apply <rule_num> <sensor_num> <actuator_num> \t apply the specified rule to the specified sensor and actuator");
		System.out.println("unapply <applied_rule_num> \t\t\t unapply the specified rule (number from the \"rules applied\" command)");
		System.out.println("script <file> \t\t\t\t\t execute the commands inside the specified file automatically");
		System.out.println("commands \t\t\t\t\t display the available commands for each actuator type");
		System.out.println("help \t\t\t\t\t\t display this message");
		System.out.println("exit \t\t\t\t\t\t terminate the program");
		System.out.println("--------------------------------------------------------------------------------------");
	}
	
	
	//list the sensors/actuators
	private static void commandList(String[] params) 
	{		
		boolean correct = false;
		int numSensors = app.getRegisteredSensors().size();
		int numActuators = app.getRegisteredActuators().size();
		
		//print all the sensors (if both or only sensors)
		if (params.length < 2 || params[1].equals("sensors")) {
			correct = true;
			System.out.println("SENSORS:  (" + numSensors + ")");
			for (int i = 0; i < numSensors; i++)
				System.out.println(i + ") " + app.getRegisteredSensors().get(i).toString());
		}
		
		//print all the actuators (if both or only actuators)
		if (params.length < 2 || params[1].equals("actuators")) {
			correct = true;
			System.out.println("ACTUATORS:  (" + numActuators + ")");
			for (int i = 0; i < numActuators; i++)
				System.out.println(i + ") " + app.getRegisteredActuators().get(i).toString());		
		}
		
		//if we didn't get in one of the two ifs, the command was wrong
		if(!correct)
			System.out.println("What is '" + params[1] + "'?");
	}
	
	
	//function that prints all the received date. The readings can be filtered by type and time
	private static void commandRead(String[] words) 
	{		
		SensorType type = null;
		Integer min_time = null;
		
		int words_left = words.length - 1;
		int i = 1;
		while (words_left > 0) {								//until I read all the words
			if (words[i].equals("-t") && words_left > 1) {		//if I meet "-t" I nned also a number following
				try {
					min_time = Integer.parseInt(words[i+1]);
					words_left--; i++;
				} catch (NumberFormatException  e) {
					System.out.println("Bad time format, use a timestamp");
					return;
				}
			} else {	//type of sensor specified. I'm in this case also if there's only '-t'. Not an issue.
				try {
					type = SensorType.valueOf(words[i].toUpperCase());
				} catch (IllegalArgumentException e) {
					System.out.println("Bad sensor type");
					return;
				}
			}
			words_left--; i++;
		}
		
		//retrieve the readings from the application and print them
		ArrayList<SensorReading> res = app.getReadings(type, min_time);
		
		for (SensorReading sr : res)
			System.out.println(sr.toString());
	}
	
	
	//function to command an action on an actuator
	private static void commandSet(String[] words) 
	{
		int act_num;
		RegisteredActuator act;
		//get sensor number first
		try {
			act_num = Integer.parseInt(words[1]);
			
			act = app.getRegisteredActuators().get(act_num);
			
		} catch (NumberFormatException | IndexOutOfBoundsException  e) {
			System.out.println("Bad actuator number");
			return;
		}
		
		//get action (depending on the sensor type, a limited specific set off commands is available)
		ActuationResult res;
		try {
			String action = words[2];
			IActuatorAction aa;
			
			switch (act.type) {
			case ALARM:
			case FIREALARM:
				aa = AlarmAction.valueOf(action.toUpperCase());
				break;
			case PATIENTALARM:
				aa = PatAlarmAction.valueOf(action.toUpperCase());
				break;
			case DASHBOARD:
				DashboardAction da = DashboardAction.valueOf(action.toUpperCase());
				da.val = Integer.parseInt(words[3]);
				aa = da;
				break;
			case AIRCOND:
				aa = AircondAction.valueOf(action.toUpperCase());
				break;
			default:
				return;
			}
			
			//perform the actuation
			res = app.setActuation(act, aa, "CLI");			
			
		} catch (IllegalArgumentException | IndexOutOfBoundsException e) {
			//If the command what not in the chosen enum, the command was wrong
			System.out.println("Bad actuator action");
			return;
		}
		
		if(res != ActuationResult.SUCCESS)
			System.out.println("Something went wrong during the actuation");
	}
	
	//This command shows all the available or active rules
	private static void commandRules(String[] words) 
	{
		//If the command is "rules applied", see the applied rules
		if(words.length >= 2 && words[1].equals("applied")) {
			//applied rules
			for (int i = 0; i < app.getRules().size(); i++) {
				Rule r = app.getRules().get(i);
				
				System.out.println(i + ") " + r.toString());
			}
			
		} else {
			//existing rules
			for (int i = 0; i < app.getRuleActions().length; i++) {
				IRuleAction ra = app.getRuleActions()[i];
				
				System.out.println(i + ") [" + ra.getSensorType() + " -> " + ra.getActuatorType() + "] " +  ra.getName());
			}
		}
	}
	
	
	//command to apply a rule to one registered sensor and one registered actuator
	private static void commandApply(String[] words) 
	{
		RegisteredActuator actuator;
		RegisteredSensor sensor;
		IRuleAction rule_act;
		try {
			
			rule_act = app.getRuleActions()[Integer.parseInt(words[1])];
			sensor = app.getRegisteredSensors().get(Integer.parseInt(words[2]));
			actuator = app.getRegisteredActuators().get(Integer.parseInt(words[3]));
			
		} catch (IllegalArgumentException | IndexOutOfBoundsException e) {
			System.out.println("Bad command: specify the 3 numbers correctly");
			return;
		}
		
		boolean success = app.setRule(sensor, actuator, rule_act);
		
		if(success)
			System.out.println("Rule applied successfully");
		else
			System.out.println("Rule application failed");
	}
	
	//command to remove an applied rule
	private static void commandUnapply(String[] words) 
	{
		try {
			app.getRules().remove(Integer.parseInt(words[1]));
			
		} catch (IllegalArgumentException | IndexOutOfBoundsException e) {
			System.out.println("Bad index: specify the index correctly");
			return;
		}
	}
	
	//command to load a text file containg commands
	private static void commandScript(String[] words) 
	{
		try {
			String filename = words[1];			
			Scanner sc = new Scanner(new File(filename));
						
			while(sc.hasNextLine()) {
				
				String input = sc.nextLine();
				parseCommand(input);
			}
			
	        sc.close();
			System.out.println("Script completed!");
			
		} catch (FileNotFoundException | IndexOutOfBoundsException e) {
			System.out.println("Bad file name");
		}
	}
	
	
	//command that displays the possible actions for each type of actuator
	private static void commandCommands(String[] words)
	{
		System.out.println(ActuatorType.ALARM.toString() + " -> " + Arrays.toString(AlarmAction.values()));
		System.out.println(ActuatorType.FIREALARM.toString() + " -> " + Arrays.toString(AlarmAction.values()));
		System.out.println(ActuatorType.PATIENTALARM.toString() + " -> " + Arrays.toString(PatAlarmAction.values()));
		System.out.println(ActuatorType.DASHBOARD.toString() + " -> " + Arrays.toString(DashboardAction.values()) + " <sensor_value>");
		System.out.println(ActuatorType.AIRCOND.toString() + " -> " + Arrays.toString(AircondAction.values()));
	}
	
	
	
}
