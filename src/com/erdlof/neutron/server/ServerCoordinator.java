package com.erdlof.neutron.server;

import java.io.File;
import java.net.ServerSocket;
import java.util.ArrayList;

public interface ServerCoordinator {
	public void sendToAllClients(int request, long senderID, byte[] data);
	public void sendToAllClients(int request, long senderID);
	public ArrayList<Client> getActiveClients();
	public ArrayList<ServerFile> getSharedFiles();
	public void registerClient(Client client);
	public void unregisterClient(Client client);
	public void registerFile(File file);
	public File getFileByID(long fileID);
	public ServerSocket getFileServer();
}
