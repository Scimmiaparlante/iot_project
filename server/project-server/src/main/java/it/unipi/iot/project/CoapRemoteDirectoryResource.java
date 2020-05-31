package it.unipi.iot.project;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import it.unipi.iot.project.RegisteredActuator.ActuatorType;
import it.unipi.iot.project.RegisteredSensor.SensorType;

/* ---------------------------------------------------------------
 * --------------------- COAP RESOURCE ---------------------------
 * -------------------------------------------------------------*/



public class CoapRemoteDirectoryResource extends CoapResource {
	
	ArrayList<RegisteredSensor> sensor_list;		//SORTED LIST containing the registered sensors
	ArrayList<RegisteredActuator> actuator_list;	//SORTED LIST containing the registered sensors
	ControlApplication app;							//reference to the main application
	
	static final boolean debug = true;				//varible to eneble the debug print

	//constructor
	public CoapRemoteDirectoryResource(String name, ControlApplication ca) 
	{
		super(name);
		setObservable(true);
		
		sensor_list = new ArrayList<RegisteredSensor>();
		actuator_list = new ArrayList<RegisteredActuator>();
		app = ca;
	}
	
	//function to handle GET requests
	//responds with a list of sensors and actuators
	public void handleGET(CoapExchange exchange) 
	{
		String response  = "";
		
		for (RegisteredSensor sens : sensor_list)
			response += sens.toString();
		for (RegisteredActuator act : actuator_list)
			response += act.toString();

		exchange.respond(response);		
	}
	
	
	//function to handle POST requests
	public void handlePOST(CoapExchange exchange) 
	{
		InetAddress addr = exchange.getSourceAddress();
		String payload = new String(exchange.getRequestPayload());
		int content_format = exchange.getRequestOptions().getContentFormat();
		
		// not a json message -> bad_request
		if (content_format != MediaTypeRegistry.APPLICATION_JSON) {
			bad_request(exchange, "Bad content_format");
			return;
		}
		
		String action, type, subtype, path;
		try {
			//parse the JSON request
			JSONObject json_payload = (JSONObject) JSONValue.parseWithException(payload);
			
			action = json_payload.get("a").toString();			//action: register
			type = json_payload.get("t").toString();			//type: actuator/sensor
			subtype = json_payload.get("st").toString();		//subtype: type of actuator/sensor
			path = json_payload.get("p").toString();			//coap resource path
					
		} catch (org.json.simple.parser.ParseException e) {
			bad_request(exchange, "Bad JSON request");
			return;
		}
			
		int ret = 0;
		switch (action) {
		case "register":
			ret = handleRegistration(type, subtype, path, addr);
			break;
		default:
			bad_request(exchange, "Unknown action");
			return;
		}
		
		if(ret != 0) {
			bad_request(exchange, "Registration parameters non correct");
			return;
		}
		
		//if we get here, the action has ben performed correctly: success reply
		exchange.respond(ResponseCode.CHANGED);
		printDebug("Registration successful from " + addr.toString() + " for " + subtype);
	}
	
	//function to handle the registration of a resource
	private int handleRegistration(String type, String subtype, String path, InetAddress addr)
	{
		int ret = 0;		
		
		switch (type) {
		case "sensor":
			ret = handleRegistrationSensor(subtype, path, addr);
			break;
		case "actuator":
			ret = handleRegistrationActuator(subtype, path, addr);
			break;
		default:
			ret = 1;
			break;
		}

		return ret;
	}
	
	//function to handle the registration of a sensor
	private int handleRegistrationSensor(String type, String path, InetAddress addr) 
	{	
		SensorType type_enum;
		
		//assign the type, depending on the received string
		switch (type) {
		case "temperature":
			type_enum = SensorType.TEMPERATURE;
			break;
		case "heartbeat":
			type_enum = SensorType.HEARTBEAT;
			break;
		case "bloodpressure":
			type_enum = SensorType.BLOODPRESSURE;
			break;
		case "fire":
			type_enum = SensorType.FIRE;
			break;
		default:
			return 1;
		}
		

		//issue a get request to test that the data is correct
		CoapClient client = new CoapClient("coap://[" + addr.toString().substring(1) + "]/" + path);
		CoapResponse response = client.get();
		if(response == null || !response.isSuccess())
			return 1;
			
		
		//check if it is already present to avoid duplicates
		int i;
		for (i = 0; i < sensor_list.size(); i++)
			if(sensor_list.get(i).node_address.equals(addr) && sensor_list.get(i).resource_path.equals(path))
				break;
		
		if (i == sensor_list.size()) {
			
			//ok, no duplicates. Create new data structures and subscribe
			CoapObserveRelation relation = null;
			
			RegisteredSensor sensor = new RegisteredSensor(addr, type_enum, path, relation);
			
			SensorHandler handler = new SensorHandler(app, sensor);
			relation = client.observe(handler);
			
			synchronized (sensor_list) {
				sensor_list.add(sensor);
				Collections.sort(sensor_list);
			}
		}

		return 0;
	}
	
	
	//function to handle the registration of a sensor
	private int handleRegistrationActuator(String type, String path, InetAddress addr) 
	{
		ActuatorType type_enum;
		
		//assign the type, depending on the received string
		switch (type) {
		case "alarm":
			type_enum = ActuatorType.ALARM;
			break;
		case "firealarm":
			type_enum = ActuatorType.FIREALARM;
			break;
		case "patientalarm":
			type_enum = ActuatorType.PATIENTALARM;
			break;
		case "dashboard":
			type_enum = ActuatorType.DASHBOARD;
			break;
		case "aircond":
			type_enum = ActuatorType.AIRCOND;
			break;
		default:
			return 1;
		}
		
		//avoid duplicates
		int i;
		for (i = 0; i < actuator_list.size(); i++)
			if(actuator_list.get(i).node_address.equals(addr) && actuator_list.get(i).resource_path.equals(path))
				break;
		
		if (i == actuator_list.size()) {
			synchronized (actuator_list) {
				actuator_list.add(new RegisteredActuator(addr, type_enum, path));
				Collections.sort(actuator_list);
			}
		}

		return 0;
	}
	
	
	//utility function to transmit a BAD_REQUEST response.
	private void bad_request(CoapExchange exch, String mess) 
	{
		printDebug(mess + " from " + exch.getSourceAddress());
		exch.respond(ResponseCode.BAD_REQUEST);
	}
	
	//debug function to print errors
	private void printDebug(String s) 
	{
		if(debug)
			System.out.println(s);
	}
}