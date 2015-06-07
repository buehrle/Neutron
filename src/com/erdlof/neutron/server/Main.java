package com.erdlof.neutron.server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.erdlof.neutron.util.Request;

public class Main {
	private static final int CONST_PORT = 12345; //TODO: get this into an external config file
	private static ServerSocket server;
	private static ExecutorService executor;
	private static Random newIDCreator;
	private static List<Client> activeClients;
	private static List<SharedFile> sharedFiles;
	
	public static void main(String[] args) {
		try {
			init();
			System.out.println("Server started.");
			
			while (true) {
				Socket clientSocket = server.accept(); // wait for a connection
				
				Client client = new Client(clientSocket, newIDCreator.nextLong());
				executor.execute(client);
			}
		} catch (Exception e) {
		}
	}
	
	private static void init() throws IOException {
		server = new ServerSocket(CONST_PORT); //open the port
		executor = Executors.newCachedThreadPool();
		newIDCreator = new Random();
		activeClients = new ArrayList<Client>();
		sharedFiles = new ArrayList<SharedFile>();
	}
	//the following methods could also be implemented in Client.java, but I like it that way, it's more clear
	//there is only one intrinsic lock available, we want that
	public static synchronized void sendToAllClients(int request, long senderID, byte[] data) { //sends the data to all clients
		for (Client client : getActiveClients()) {
			client.sendToClientFromID(request, senderID, data);
		}
	}
	
	public static synchronized void sendToAllClients(int request, long senderID) { //sends the data to all clients
		for (Client client : getActiveClients()) {
			client.sendToClientFromID(request, senderID);
		}
	}
	
	public static synchronized List<Client> getActiveClients() {
		return activeClients;
	}
	
	public static synchronized List<SharedFile> getSharedFiles() {
		return sharedFiles;
	}
	
	public static synchronized void registerClient(Client client) {
		getActiveClients().add(client);
		sendToAllClients(Request.CLIENT_CONNECT_NOTIFICATION, client.getID(), client.getName().getBytes());
	}
	
	public static synchronized void unregisterClient(Client client) {
		if (getActiveClients().contains(client)) {
			getActiveClients().remove(client);
			sendToAllClients(Request.CLIENT_DISCONNECT_NOTIFICATION, client.getID());
		}
	}
	
	public static synchronized void registerFile(File file) {
		long fileID = newIDCreator.nextLong();
		getSharedFiles().add(new SharedFile(file, fileID));
		sendToAllClients(Request.NEW_FILE, fileID, file.getName().getBytes());
	}
}
