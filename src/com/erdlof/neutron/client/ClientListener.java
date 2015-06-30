package com.erdlof.neutron.client;

import java.util.List;

public interface ClientListener {
	public void connectionEstablished(List<SharedAssociation> partners, List<SharedAssociation> filesOnServer);
	public void connectionFailed();
	public void disconnected();
	public void setDisconnectRequest(int request);
	public void textMessage(long senderID, byte[] message);
	public void clientConnected(long senderID, byte[] name);
	public void clientDisconnected(long senderID);
	public void newFile(long fileID, byte[] name);
}
