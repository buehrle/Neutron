package com.erdlof.neutron.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main implements ClientEventListener {
	private static final int CONST_PORT = 12345; //TODO: get this into an external config file
	private static final int CONST_MAX_CLIENTS = 30; //TODO: and this also.

	public static void main(String[] args) {
		try {
			ServerSocket server = new ServerSocket(CONST_PORT);
			ExecutorService executor = Executors.newFixedThreadPool(CONST_MAX_CLIENTS);
			
			while (true) {
				Socket clientSocket = server.accept();
				
				executor.execute(new Client(clientSocket));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public synchronized void textMessageReceived(Client client) {
		
	}
	
	

}
