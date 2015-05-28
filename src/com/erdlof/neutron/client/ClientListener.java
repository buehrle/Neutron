package com.erdlof.neutron.client;

public interface ClientListener {
	public void connectionEstablished();
	public void connectionFailed();
	public void disconnected();
	public void setRequest(int request, long senderID, byte[] data);
	public void setDisconnectRequest(int request);
}
