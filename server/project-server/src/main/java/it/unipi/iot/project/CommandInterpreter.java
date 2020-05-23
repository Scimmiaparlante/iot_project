package it.unipi.iot.project;

import java.util.Scanner;

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
			case "help":
			default:
				commandHelp();
				break;
			}
			
		}
		
        sc.close();
        app.stop();
	}
	
	
	private static void commandHelp() {
		System.out.println("The available commands are:");
		System.out.println("list [sensors | actuators] \t\t\t show a list of the nodes (or sensors/actuators)");
		System.out.println("help \t\t\t\t\t\t display this message");
		System.out.println("exit \t\t\t\t\t\t terminate the program");
		System.out.print("--------------------------------------------------------------------------------------");
	}
	
	
	private static void commandList(String params[]) 
	{		
		boolean correct = false;
		int numSensors = app.remoteDir_res.sensor_list.size();
		int numActuators = app.remoteDir_res.actuator_list.size();
		
		if (params.length < 2 || params[1].equals("sensors")) {
			correct = true;
			System.out.println("SENSORS:  (" + numSensors + ")");
			for (int i = 0; i < numSensors; i++)
				System.out.println(app.remoteDir_res.sensor_list.get(i).toString());
		}
		
		if (params.length < 2 || params[1].equals("actuators")) {
			correct = true;
			System.out.println("ACTUATORS:  (" + numActuators + ")");
			for (int i = 0; i < numActuators; i++)
				System.out.println(app.remoteDir_res.actuator_list.get(i).toString());		
		}
		
		if(!correct)
			System.out.println("What is '" + params[1] + "'?");
		
	}
	
}
