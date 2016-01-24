package com.erdlof.neutron.client;

import java.util.ArrayList;

public interface ClientListener {
	public void connectionEstablished(ArrayList<SharedAssociation> partners, ArrayList<SharedAssociation> filesOnServer);
	public void connectionFailed();
	public void disconnected();
	public void setDisconnectRequest(int request);
	public void textMessage(long senderID, byte[] message);
	public void clientConnected(long senderID, byte[] name);
	public void clientDisconnected(long senderID);
	public void newFile(long fileID, byte[] name);
}
