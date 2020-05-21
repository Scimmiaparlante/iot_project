package it.unipi.iot.project;

import org.eclipse.californium.core.CoapServer;

public class RDCoapServer extends CoapServer {

	public static void main(String[] args)	{
		
		System.out.print("Starting the Coap Server for the remote directory...");
		RDCoapServer server = new RDCoapServer();
		server.add(new CoapRemoteDirectory("remote_dir"));
		server.start();
		System.out.println("Coap server started!");
	}
}
