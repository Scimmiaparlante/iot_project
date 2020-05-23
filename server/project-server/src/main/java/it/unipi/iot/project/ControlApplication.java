package it.unipi.iot.project;

import org.eclipse.californium.core.CoapServer;

public class ControlApplication {
	
	public CoapServer server;
	public CoapRemoteDirectoryResource remoteDir_res;
	
	
	public ControlApplication()	{
		
		System.out.println("-- Control Application --");
		
		System.out.println("Starting the Coap Server for the remote directory...");
		
		server = new CoapServer();
		remoteDir_res = new CoapRemoteDirectoryResource("remote_dir");
		server.add(remoteDir_res);
		server.start();
		
		System.out.println("Coap server started!");
		
	}
	
	
	public void stop() {
		server.stop();
	}
	
	
	
	
	
	
}
