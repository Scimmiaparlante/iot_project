package it.unipi.iot.project;

import java.util.ArrayList;
import java.util.Scanner;

import it.unipi.iot.project.RegisteredSensor.SensorType;

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
		System.out.println("list [sensors | actuators] \t\t\t show a list of the nodes (or sensors/actuators)");
		System.out.println("read [<sensor_type>] [-t <timestamp>] \t\t show the readings of the sensors (filter by type and min time)");
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
			for (RegisteredSensor sensor : app.remoteDir_res.sensor_list)
				System.out.println(sensor.toString());
		}
		
		if (params.length < 2 || params[1].equals("actuators")) {
			correct = true;
			System.out.println("ACTUATORS:  (" + numActuators + ")");
			for (RegisteredActuator actuator : app.remoteDir_res.actuator_list)
				System.out.println(actuator.toString());		
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
		
/* OLD WAY
		if (words.length >= 2) {
			try {
				type = SensorType.valueOf(words[1].toUpperCase());
			} catch (IllegalArgumentException e) {
				System.out.print("Bad sensor type");
				return;
			}
		}
*/
		
		ArrayList<SensorReading> res = app.getReadings(type, min_time);
		
		for (SensorReading sr : res) {
			System.out.println(sr.sensor.node_address + sr.sensor.resource_path + 
								" - ts: " + sr.timestamp + " - val:" + sr.value);
		}
		
	}
	
}
