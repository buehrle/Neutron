package com.erdlof.neutron.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

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
			
			byte[] encodedKeyLength = new byte[8];
			clientKeyInput.read(encodedKeyLength);
			byte[] encodedKey = new byte[CryptoUtils.byteArrayToInt(encodedKeyLength)];
			clientKeyInput.read(encodedKey);
			publicKey = CryptoUtils.getPublicKeyFromEncoded(encodedKey); // get the client's public key from a byte array
			
			clientKeyOutput.write(CryptoUtils.intToByteArray(Main.getServerKeyPair().getPublic().getEncoded().length));
			clientKeyOutput.write(Main.getServerKeyPair().getPublic().getEncoded()); //TODO this sends the server's public key to the client
			clientKeyOutput.flush();
			
			clientKeyInput.close(); //we don't need them anymore ;(
			clientKeyOutput.close();
			
			inputCipher = Cipher.getInstance("RSA");
			outputCipher = Cipher.getInstance("RSA");
			inputCipher.init(Cipher.DECRYPT_MODE, Main.getServerKeyPair().getPrivate());
			outputCipher.init(Cipher.ENCRYPT_MODE, publicKey);
			
			clientCipheredInput = new CipherInputStream(clientSocket.getInputStream(), inputCipher); //TODO create the encrypted streams with the keys we just exchanged, cipher needed
			clientCipheredOutput = new CipherOutputStream(clientSocket.getOutputStream(), outputCipher);
			
			byte[] clientNameLength = new byte[8]; //read the length of the client's name as 8 byte
			clientCipheredInput.read(clientNameLength);
			byte[] clientName_ = new byte[CryptoUtils.byteArrayToInt(clientNameLength)];
			clientCipheredInput.read(clientName_); // get the client's nickname
			clientName = new String(clientName_, "UTF-8");
			
			while(!Thread.currentThread().isInterrupted()) {
				if (clientCipheredInput.available() > 0) {
					int request = clientCipheredInput.read();
					
					switch (request) { //what does the client want???
						case RequestedAction.SEND_TEXT:
							byte[] tempMessageLength = new byte [8];
							clientCipheredInput.read(tempMessageLength);
							
							byte[] tempMessage = new byte[CryptoUtils.byteArrayToInt(tempMessageLength)];
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
			e.printStackTrace();
		}
	}
	
	public String getClientName() {
		return clientName;
	}

}
