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

import com.erdlof.neutron.util.CryptoUtils;
import com.erdlof.neutron.util.Request;

public class Main implements ServerCoordinator {
	private final int CONST_PORT = 50192; //TODO: get this into an external config file
	private final int CONST_FILE_PORT = 50193;
	private ServerSocket server;
	private ServerSocket fileServer;
	private ExecutorService executor;
	private Random newIDCreator;
	private volatile ArrayList<Client> activeClients;
	private volatile ArrayList<ServerFile> sharedFiles;
	
	public static void main(String[] args) {
		new Main();
	}
	
	public Main() {
		CryptoUtils.removeCryptographyRestrictions(); // OOOOOOOH ILLEGAL /o\
		Security.addProvider(new BouncyCastleProvider());
		
		try {
			init();
			System.out.println("Server started.");
			
			while (true) {
				Socket clientSocket = server.accept(); // wait for a connection
				
				Client client = new Client(clientSocket, newIDCreator.nextLong(), this);
				executor.execute(client);
			}
		} catch (Exception e) {
		}
	}
	
	private void init() throws IOException {
		server = new ServerSocket(CONST_PORT); //open the port
		fileServer = new ServerSocket(CONST_FILE_PORT);
		executor = Executors.newCachedThreadPool();
		newIDCreator = new Random();
		activeClients = new ArrayList<Client>();
		sharedFiles = new ArrayList<ServerFile>();
	}
	
	//the following methods could also be implemented in Client.java, but I like it that way, it's more clear
	//there is only one intrinsic lock available, we want that
	@Override
	public synchronized void sendToAllClients(int request, long senderID, byte[] data) { //sends the data to all clients
		for (Client client : getActiveClients()) {
			client.sendToClientFromID(request, senderID, data);
		}
	}
	
	@Override
	public synchronized void sendToAllClients(int request, long senderID) { //sends the data to all clients
		for (Client client : getActiveClients()) {
			client.sendToClientFromID(request, senderID);
		}
	}
	
	@Override
	public synchronized ArrayList<Client> getActiveClients() {
		return activeClients;
	}
	
	@Override
	public synchronized ArrayList<ServerFile> getSharedFiles() {
		return sharedFiles;
	}
	
	@Override
	public synchronized void registerClient(Client client) {
		getActiveClients().add(client);
		sendToAllClients(Request.CLIENT_CONNECT_NOTIFICATION, client.getID(), client.getName().getBytes());
	}
	
	@Override
	public synchronized void unregisterClient(Client client) {
		if (getActiveClients().contains(client)) {
			getActiveClients().remove(client);
			sendToAllClients(Request.CLIENT_DISCONNECT_NOTIFICATION, client.getID());
		}
	}
	
	@Override
	public synchronized void registerFile(File file) {
		long fileID = newIDCreator.nextLong();
		getSharedFiles().add(new ServerFile(fileID, file));
		sendToAllClients(Request.NEW_FILE, fileID, file.getName().getBytes());
	}
	
	@Override
	public synchronized File getFileByID(long fileID) {
		for (ServerFile file : getSharedFiles()) {
			if (file.getID() == fileID) return file.getFile();
		}
		return null;
	}
	
	@Override
	public ServerSocket getFileServer() {
		synchronized (fileServer) {
			return fileServer;
		}
	}
}
