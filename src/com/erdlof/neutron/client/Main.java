package com.erdlof.neutron.client;

import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.UIManager;

import com.erdlof.neutron.util.Request;

public class Main extends JFrame implements ClientListener {
	private static final long serialVersionUID = 527099896996818525L;
	private Connection connection;
	private Toolkit toolkit;
	
	public static void main(String[] args) {
		new Main();
	}
	
	public Main() { //set up the window
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		toolkit = Toolkit.getDefaultToolkit();
		setTitle("Neutron v0.1");
		setSize(823, 103);
		
		int x = (int) (toolkit.getScreenSize().width - this.getWidth()) / 2;
		int y = (int) (toolkit.getScreenSize().height - this.getHeight()) / 2;
		
		setLocation(x, y);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.initializeComponents();
		
		setVisible(true);
    }
	
	private void initializeComponents() {
		//TODO add elements
	}
	
	public synchronized void connectionEstablished() {
		
	}
	
	public synchronized void connectionFailed() {
		
	}
	
	public synchronized void disconnected() {
		
	}
	
	public synchronized void setRequest(int request, long senderID, byte[] data) {
		switch (request) { //TODO implement the notifications
			case Request.SEND_TEXT:
				break;
			case Request.CLIENT_CONNECT_NOTIFICATION:
				break;
			case Request.CLIENT_DISCONNECT_NOTIFICATION:
				break;
		}
	}
	
	public synchronized void setDisconnectRequest(int request) {
		switch (request) {
			case Request.UNEXPECTED_ERROR:
				break;
			case Request.SERVER_SHUTDOWN:
				break;
			case Request.KICKED_FROM_SERVER:
				break;
			case Request.ILLEGAL_REQUEST:
				break;
			case Request.ILLEGAL_NAME:
				break;
			default:
				break;
		}
	}
}
