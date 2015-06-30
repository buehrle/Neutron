package com.erdlof.neutron.server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Security;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.erdlof.neutron.util.Request;

public class Main {
	private static final int CONST_PORT = 12345; //TODO: get this into an external config file
	private static final int CONST_FILE_PORT = 12346;
	private static ServerSocket server;
	private static ServerSocket fileServer;
	private static ExecutorService executor;
	private static Random newIDCreator;
	private static volatile ArrayList<Client> activeClients;
	private static volatile ArrayList<ServerFile> sharedFiles;
	
	public static void main(String[] args) {
		Security.addProvider(new BouncyCastleProvider());
		
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
		fileServer = new ServerSocket(CONST_FILE_PORT);
		executor = Executors.newCachedThreadPool();
		newIDCreator = new Random();
		activeClients = new ArrayList<Client>();
		sharedFiles = new ArrayList<ServerFile>();
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
	
	public static synchronized ArrayList<Client> getActiveClients() {
		return activeClients;
	}
	
	public static synchronized ArrayList<ServerFile> getSharedFiles() {
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
		getSharedFiles().add(new ServerFile(fileID, file));
		sendToAllClients(Request.NEW_FILE, fileID, file.getName().getBytes());
	}
	
	public static synchronized File getFileByID(long fileID) {
		for (ServerFile file : getSharedFiles()) {
			if (file.getID() == fileID) return file.getFile();
		}
		return null;
	}
	
	public static ServerSocket getFileServer() {
		synchronized (fileServer) {
			return fileServer;
		}
	}
}
