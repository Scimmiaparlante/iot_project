package it.unipi.iot.project;

import java.net.InetAddress;

public class RegisteredNode {
	
	public InetAddress node_address;
	public String resource_path;
		
	public RegisteredNode(InetAddress addr, String path) {
		node_address = addr;
		resource_path = path;
	}
	
	public String toString() {
		return node_address.toString().substring(1)+ "/" + resource_path + ";";
	}
}

