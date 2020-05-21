package it.unipi.iot.project;

import org.eclipse.californium.core.CoapServer;

public class RDCoapServer extends CoapServer {

	public RDCoapServer() {
		super(9874);
	}
	
	public static void main(String[] args)	{
		System.out.println("Starting the Coap Server for the remote directory...");
		RDCoapServer server = new RDCoapServer();
		server.add(new CoapRemoteDirectory("remote_dir"));
		server.start();
		System.out.println("Coap server started!");
		//while(true);
		
		synchronized (server) {
			try {
				server.wait();
			} catch (InterruptedException e) {
				System.out.println("Interrupted...terminating");
			}
		}
	}
}
