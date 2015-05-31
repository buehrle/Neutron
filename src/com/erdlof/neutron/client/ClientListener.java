package com.erdlof.neutron.client;

import java.util.List;

public interface ClientListener {
	public void connectionEstablished(Partner[] partners);
	public void connectionFailed();
	public void disconnected();
	public void setRequest(int request, long senderID, byte[] data);
	public void setDisconnectRequest(int request);
}
