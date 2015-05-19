package com.erdlof.neutron.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
	private static final int CONST_PORT = 12345; //TODO: get this into an external config file
	private static final int CONST_MAX_CLIENTS = 30; //TODO: and this also.
	private static ServerSocket server;
	private static KeyPair serverKeyPair;
	
	public static void main(String[] args) {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA"); //generate the keys
			keyGen.initialize(2048);
			serverKeyPair = keyGen.generateKeyPair();
			
			server = new ServerSocket(CONST_PORT);
			ExecutorService executor = Executors.newFixedThreadPool(CONST_MAX_CLIENTS);
			
			System.out.println("Server started.");
			
			while (true) {
				Socket clientSocket = server.accept();
				
				executor.execute(new Client(clientSocket));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static synchronized KeyPair getServerKeyPair() {
		return serverKeyPair;
	}
	
	public static synchronized void textMessageReceived(Client client, String message) {
		System.out.println(client.getClientName() + ": " + message);
	}
}
