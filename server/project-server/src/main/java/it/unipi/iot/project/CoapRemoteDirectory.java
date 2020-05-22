package it.unipi.iot.project;

import java.net.InetAddress;
import java.nio.file.Path;
import java.util.ArrayList;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import it.unipi.iot.project.RegisteredActuator.ActuatorType;
import it.unipi.iot.project.RegisteredSensor.SensorType;


/* ---------------------------------------------------------------
 * ----------------------- NODE TYPES ----------------------------
 * -------------------------------------------------------------*/

class RegisteredNode {
	
	public InetAddress node_address;
	public String resource_path;
		
	public RegisteredNode(InetAddress addr, String path) {
		node_address = addr;
		resource_path = path;
	}
	
	public String toString() {
		return node_address.toString();
	}
}


class RegisteredSensor extends RegisteredNode {
	
	public enum SensorType {
		TEMPERATURE
	}
	
	public SensorType type;
	
	public RegisteredSensor(InetAddress addr, SensorType _type, String path) {
		super(addr, path);
		this.type = _type;
	}
}


class RegisteredActuator extends RegisteredNode {
	
	public enum ActuatorType {
		ALARM
	}
	
	public ActuatorType type;
	
	public RegisteredActuator(InetAddress addr, ActuatorType _type, String path) {
		super(addr, path);
	
		this.type = _type;
	}	
}



/* ---------------------------------------------------------------
 * --------------------- COAP RESOURCES --------------------------
 * -------------------------------------------------------------*/



public class CoapRemoteDirectory extends CoapResource {
	
	ArrayList<RegisteredSensor> sensor_list;
	ArrayList<RegisteredActuator> actuator_list;

	
	public CoapRemoteDirectory(String name) {
		super(name);
		setObservable(true);
		
		sensor_list = new ArrayList<RegisteredSensor>(); 
		actuator_list = new ArrayList<RegisteredActuator>(); 
	}
	
	//function to handle GET requests
	public void handleGET(CoapExchange exchange) {
		
		String response_s = "", response_a = "";
		
		for (int i = 0; i < sensor_list.size(); i++) {
			response_s += sensor_list.get(i).node_address.toString() + "/";
			response_s += sensor_list.get(i).resource_path + ";";
			response_a += actuator_list.get(i).node_address.toString() + "/";
			response_a += actuator_list.get(i).resource_path + ";";
		}
		
		String response = response_s + response_a;
		
		exchange.respond(response);		
	}
	
	
	//function to handle POST requests
	public void handlePOST(CoapExchange exchange) {
		
		InetAddress addr = exchange.getSourceAddress();
		String payload = new String(exchange.getRequestPayload());
		int content_format = exchange.getRequestOptions().getContentFormat();
		
		// not a json message
		if (content_format != MediaTypeRegistry.APPLICATION_JSON) {
			bad_request(exchange, "Bad content_format");
			return;
		}
		
		String action, type, subtype, path;
		try {
			JSONObject json_payload = (JSONObject) JSONValue.parseWithException(payload);
			
			action = json_payload.get("a").toString();
			type = json_payload.get("t").toString();
			subtype = json_payload.get("st").toString();
			path = json_payload.get("p").toString();
					
		} catch (org.json.simple.parser.ParseException e) {
			bad_request(exchange, "Bad JSON request");
			return;
		}
			
		int ret = 0;
		switch (action) {
		case "register":
			ret = handle_registration(type, subtype, path, addr);
			break;
		default:
			bad_request(exchange, "Unknown action");
			return;
		}
		
		if(ret != 0) {
			bad_request(exchange, "Registration parameters non correct");
			return;
		}
		
		exchange.respond(ResponseCode.CHANGED);
		System.out.println("Registration successful from " + addr.toString());
	}
	
	
	private int handle_registration(String type, String subtype, String path, InetAddress addr){
		int ret = 0;		
		
		switch (type) {
		case "sensor":
			ret = handle_registration_sensor(subtype, path, addr);
			break;
		case "actuator":
			ret = handle_registration_actuator(subtype, path, addr);
			break;
		default:
			ret = 1;
			break;
		}

		return ret;
	}
	
	
	private int handle_registration_sensor(String type, String path, InetAddress addr) {
		
		SensorType type_enum;
		
		switch (type) {
		case "temperature":
			type_enum = SensorType.TEMPERATURE;
			break;
			
		default:
			return 1;
		}
		
		
		//avoid duplicates
		int i;
		for (i = 0; i < sensor_list.size(); i++)
			if(sensor_list.get(i).node_address.equals(addr) && sensor_list.get(i).resource_path.equals(path))
				break;
		
		if (i < sensor_list.size())
			sensor_list.add(new RegisteredSensor(addr, type_enum, path));
		

		return 0;
	}
	
	
	private int handle_registration_actuator(String type, String path, InetAddress addr) {
		
		ActuatorType type_enum;
		
		switch (path) {
		case "alarm":
			type_enum = ActuatorType.ALARM;
			break;
			
		default:
			return 1;
		}
		
		
		//avoid duplicates
		int i;
		for (i = 0; i < actuator_list.size(); i++)
			if(actuator_list.get(i).node_address.equals(addr) && sensor_list.get(i).resource_path.equals(path))
				break;
		
		if (i < actuator_list.size())
			actuator_list.add(new RegisteredActuator(addr, type_enum, path));
		

		return 0;
	}

	

	
	
	private void bad_request(CoapExchange exch, String mess) {
		System.out.println(mess + " from " + exch.getSourceAddress());
		exch.respond(ResponseCode.BAD_REQUEST);
	}
	
	
}