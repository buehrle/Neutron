package com.erdlof.neutron.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

import com.erdlof.neutron.util.CryptoUtils;
import com.erdlof.neutron.util.RequestedAction;

public class Client implements Runnable {
	private Socket clientSocket;
	private DataInputStream clientKeyInput;
	private DataOutputStream clientKeyOutput;
	
	private CipherInputStream clientCipheredInput;
	private CipherOutputStream clientCipheredOutput;
	private Cipher outputCipher;
	private Cipher inputCipher;
	
	private String clientName;
	private PublicKey publicKey; //the CLIENT'S public key
	
	public Client(Socket clientSocket) throws IOException {
		this.clientSocket = clientSocket;
	}
	
	@Override
	public void run() {
		try {
			clientKeyInput = new DataInputStream(clientSocket.getInputStream());
			clientKeyOutput = new DataOutputStream(clientSocket.getOutputStream());		
			
			byte[] encodedKey = new byte[getLengthOfFollowingData(clientKeyInput)];
			clientKeyInput.read(encodedKey);
			publicKey = CryptoUtils.getPublicKeyFromEncoded(encodedKey); // get the client's public key from a byte array

			clientKeyOutput.write(CryptoUtils.intToByteArray(Main.getServerKeyPair().getPublic().getEncoded().length));
			clientKeyOutput.write(Main.getServerKeyPair().getPublic().getEncoded()); // this sends the server's public key to the client
			clientKeyOutput.flush();

			inputCipher = Cipher.getInstance("RSA");
			outputCipher = Cipher.getInstance("RSA");
			inputCipher.init(Cipher.DECRYPT_MODE, Main.getServerKeyPair().getPrivate());
			outputCipher.init(Cipher.ENCRYPT_MODE, publicKey);
			
			clientCipheredInput = new CipherInputStream(clientKeyInput, inputCipher); // create the encrypted streams with the keys we just exchanged, cipher needed
			clientCipheredOutput = new CipherOutputStream(clientKeyOutput, outputCipher);
			
			byte[] clientName_ = new byte[getLengthOfFollowingData(clientCipheredInput)];
			clientCipheredInput.read(clientName_); // get the client's nickname
			clientName = new String(clientName_, "UTF-8");
			
			while(!Thread.currentThread().isInterrupted()) {
				if (clientCipheredInput.available() > 0) {
					int request = clientCipheredInput.read();
					
					switch (request) { //what does the client want???
						case RequestedAction.SEND_TEXT:
							byte[] tempMessage = new byte[getLengthOfFollowingData(clientCipheredInput)];
							if (clientCipheredInput.read(tempMessage) != -1) {
								Main.textMessageReceived(this, new String(tempMessage, "UTF-8"));
							}				
							break;
						default: 
							Thread.currentThread().interrupt();
							break;
					}
				}
			}
			
			clientCipheredInput.close();
			clientCipheredOutput.close();
			clientSocket.close();
		} catch (Exception e) {
			System.out.println("SERVER");
			e.printStackTrace();
		}
	}
	
	private int getLengthOfFollowingData(InputStream stream) throws IOException {
		byte[] lengthTemp = new byte[4];
		stream.read(lengthTemp);
		return CryptoUtils.byteArrayToInt(lengthTemp);
	}
	
	public String getClientName() {
		return clientName;
	}

}
