package com.erdlof.neutron.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import com.erdlof.neutron.util.CryptoUtils;

public class Client implements Runnable {
	private Socket clientSocket;
	private DataInputStream clientInput;
	private DataOutputStream clientOutput;
	
	private String clientName;
	private PublicKey publicKey;
	
	public Client(Socket clientSocket) throws IOException {
		this.clientSocket = clientSocket;
		clientInput = new DataInputStream(this.clientSocket.getInputStream());
		clientOutput = new DataOutputStream(this.clientSocket.getOutputStream());
	}
	
	@Override
	public void run() {
		try {
			byte[] encodedKey = null;			
			clientInput.read(encodedKey);
			publicKey = CryptoUtils.getPublicKeyFromEncoded(encodedKey); // get the client's public key from a byte array
			
			clientOutput.write(0); //TODO this sends the server's public key to the client
			clientOutput.flush();
			
			byte[] clientName_ = null;
			clientInput.read(clientName_); // get the client's nickname
			clientName = new String(clientName_, "UTF-8");
			
			while(!Thread.currentThread().isInterrupted()) {
				byte[] tempMessage = null;
				if (clientInput.read(tempMessage) != -1) {
					Main.textMessageReceived(this, new String(tempMessage, "UTF-8"));
				}
				
			}
			
			clientInput.close();
			clientOutput.close();
			clientSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getClientName() {
		return clientName;
	}

}