package com.erdlof.neutron.client;

import com.erdlof.neutron.util.UnwrappedObject;

public interface ClientListener {
	public void connectionEstablished(UnwrappedObject[] partners, UnwrappedObject[] filesOnServer);
	public void connectionFailed();
	public void disconnected();
	public void setDisconnectRequest(int request);
	public void textMessage(long senderID, byte[] message);
	public void clientConnected(long senderID, byte[] name);
	public void clientDisconnected(long senderID);
}
