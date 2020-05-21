package it.unipi.iot.project;

import java.net.InetAddress;
import java.util.ArrayList;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;


class RegisteredNode {
	InetAddress node_address;
	
	public RegisteredNode(InetAddress addr) {
		node_address = addr;
	}
	
	public String toString() {
		return node_address.toString();
	}
}


public class CoapRemoteDirectory extends CoapResource {
	
	ArrayList<RegisteredNode> node_list;
	 
	
	public CoapRemoteDirectory(String name) {
		super(name);
		setObservable(true);
		
		node_list = new ArrayList<RegisteredNode>(); 
	}
	
	public void handleGET(CoapExchange exchange) {
		
		String response = "";
		
		for (int i = 0; i < node_list.size(); i++)
			response += node_list.toString() + ",";
		
		
		
		exchange.respond(response);		
	}
	
	public void handlePOST(CoapExchange exchange) {
		
		InetAddress addr = exchange.getSourceAddress();
		String payload = new String(exchange.getRequestPayload());
		
		if (!payload.equals("register")) {
			System.out.println("Bad request from " + addr.toString());
			exchange.respond(ResponseCode.BAD_REQUEST);
			return;
		}
			
		
		//avoid duplicates
		int i;
		for (i = 0; i < node_list.size(); i++)
			if(node_list.equals(addr))
				break;
		
		if (i < node_list.size())
			node_list.add(new RegisteredNode(addr));
		
		//even if duplicate, respond with success code
		exchange.respond(ResponseCode.CHANGED);
		System.out.println("Registration successful from " + addr.toString());
	}
}