package it.unipi.iot.project;

import java.net.InetAddress;

public class RegisteredNode implements Comparable<RegisteredNode> {
	
	public InetAddress node_address;
	public String resource_path;
		
	public RegisteredNode(InetAddress addr, String path) {
		node_address = addr;
		resource_path = path;
	}
	
	public String toString() {
		return node_address.toString().substring(1)+ "/" + resource_path + ";";
	}

	@Override
	public int compareTo(RegisteredNode arg0) {
		
		int ret = this.node_address.toString().compareTo(arg0.node_address.toString());
		
		if (ret == 0)
			ret = this.resource_path.compareTo(arg0.resource_path);
		
		return ret;
	}
}

