package com.erdlof.neutron.client;

import com.erdlof.neutron.util.Request;

public class Main {
	private Connection connection;
	
	public static void main(String[] args) {
		new Main();
	}
	
	public Main() {

	}
	
	public static synchronized void connectionEstablished() {
		
	}
	
	public static synchronized void connectionFailed() {
		
	}
	
	public static synchronized void disconnected() {
		
	}
	
	public static synchronized void setDisconnectRequest(int request) {
//	case Request.UNEXPECTED_ERROR:
//		Main.setDisconnectReason(DisconnectReason.ERROR);
//	case Request.SERVER_SHUTDOWN:
//		Main.setDisconnectReason(DisconnectReason.SERVER_SHUTDOWN);
//	case Request.KICKED_FROM_SERVER:
//		Main.setDisconnectReason(DisconnectReason.KICK);
//	case Request.ILLEGAL_REQUEST:
//		Main.setDisconnectReason(DisconnectReason.ILLEGAL_REQUEST);
//	case Request.ILLEGAL_NAME:
//		Main.setDisconnectReason(reason)
	}
}
