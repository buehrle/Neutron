package com.erdlof.neutron.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
	private static final int CONST_PORT = 12345; //TODO: get this into an external config file
	private static ServerSocket server;
	private static ExecutorService executor;
	private static Random clientIDCreator;
	
	public static void main(String[] args) {
		
		try {
			init();
			System.out.println("Server started.");
			
			while (true) {
				Socket clientSocket = server.accept(); // wait for a connection
				
				executor.execute(new Client(clientSocket, clientIDCreator.nextLong()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void init() throws IOException {
		server = new ServerSocket(CONST_PORT); //open the port
		executor = Executors.newCachedThreadPool();
		clientIDCreator = new Random();
	}
	
	public static synchronized void textMessageReceived(Client client, String message) {
		System.out.println(client.getClientName() + ": " + message);
	}
}
