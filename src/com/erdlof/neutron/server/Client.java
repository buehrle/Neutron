package com.erdlof.neutron.server;

import java.io.IOException;
import java.net.Socket;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import com.erdlof.neutron.streams.BetterDataInputStream;
import com.erdlof.neutron.streams.BetterDataOutputStream;
import com.erdlof.neutron.util.CheckUtils;
import com.erdlof.neutron.util.CommunicationUtils;
import com.erdlof.neutron.util.CryptoUtils;
import com.erdlof.neutron.util.Request;
import com.erdlof.neutron.util.Wrappable;

public class Client implements Runnable, Wrappable {
	private static final int MAX_COUNTS_UNTIL_TIMEOUT = 1000;
	private static final String ALGORITHM_PADDING = "AES/CBC/PKCS5PADDING";

	private Socket clientSocket;
	private int timeoutCounter;
	private BetterDataInputStream clientInput;
	private BetterDataOutputStream clientOutput;
	
	private Cipher wrapCipher;
	private Cipher outputCipher;
	private Cipher inputCipher;
	
	private String clientName;
	private long clientID;
	private PublicKey publicKey; //the CLIENT'S public key
	private SecretKey secretKey;
	
	private byte[] IV;
	
	public Client(Socket clientSocket, long clientID) throws IOException {
		this.clientID = clientID;
		this.clientSocket = clientSocket;
		timeoutCounter = 0;
		
		try {
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			
			keyGen.init(256);
			secretKey = keyGen.generateKey(); //generate the AES-key we will use to communicate
			
			IV = CryptoUtils.createTotallyRandomIV(); //generate the initialization vector
		} catch (Exception e) {
			
		}
	}
	
	@Override
	public void run() {
		initConnection();
		
		try {
			while(!Thread.currentThread().isInterrupted()) {
				if (timeoutCounter <= MAX_COUNTS_UNTIL_TIMEOUT) {
					if (clientInput.available() > 0) {
						int request = clientInput.getRequest();
						
						switch (request) { //what does the client want???
							case Request.SEND_TEXT:
								byte[] tempData = clientInput.getBytesDecrypted();
								if (tempData.length > 0) Main.sendToAllClients(request, clientID, tempData);
								break;
							case Request.SEND_FILE:
								//TODO add filesharing
								break;
							case Request.GET_FILE:
								//TODO filesharing #2
								break;
							case Request.REGULAR_DISCONNECT:
								System.out.println("Regular disconnect.");
								performShutdown(); //ask the current loop to exit and close all resources
								break;
							case Request.ALIVE: //heartbeat
								break;
							default: 
								clientOutput.sendRequest(Request.ILLEGAL_REQUEST);
								performShutdown(); //ask the current loop to exit and close all resources
								break;
						}
						
						timeoutCounter = 0;
					} else {
						timeoutCounter++;
					}
					
					if (!Thread.currentThread().isInterrupted()) Thread.sleep(10); // otherwise we would consume too much resources
					
				} else {
					System.out.println("Client did not respond for too long. Closing connection");
					performShutdown(); //ask the current loop to exit and close all resources
				}
			}
		} catch (Exception e) {
			try {
				clientOutput.sendRequest(Request.UNEXPECTED_ERROR); //only send this if the connection was established before the exception occured
			} catch (Exception e1) {
			}
			e.printStackTrace();
		} finally {
			try {
				clientInput.close();
				clientOutput.close();
				clientSocket.close();			
			} catch (IOException e) {
			} finally {
				Main.unregisterClient(this); //the fact that this is executed on the end of the thread MAY cause an exception, but that's okay
			}
		}
	}
	
	private void initConnection() {
		try {
			clientInput = new BetterDataInputStream(clientSocket.getInputStream());
			clientOutput = new BetterDataOutputStream(clientSocket.getOutputStream());
			
			publicKey = CryptoUtils.getPublicKeyFromEncoded(clientInput.getBytes()); // get the client's public key from a byte array
	
			wrapCipher = Cipher.getInstance("RSA");
			wrapCipher.init(Cipher.WRAP_MODE, publicKey);
			byte[] wrappedKey = wrapCipher.wrap(secretKey); //we wrap the AES-key with a public RSA-key from the client to transfer it securely
			clientOutput.sendBytes(wrappedKey);
			
			clientOutput.sendBytes(IV); //it does what it seems to do.
	
			inputCipher = Cipher.getInstance(ALGORITHM_PADDING); //the cipher for decrypting data from the client
			outputCipher = Cipher.getInstance(ALGORITHM_PADDING); //encrypting
			inputCipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(IV));
			outputCipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(IV));
			
			clientInput.initCipher(inputCipher); //init the streams with the ciphers
			clientOutput.initCipher(outputCipher);
			
			clientOutput.sendBytesEncrypted(CryptoUtils.longToByteArray(clientID)); //send the ID to the client
			
			clientName = new String(clientInput.getBytesDecrypted());
			
			if (CheckUtils.isProperNickname(clientName)) {
				clientOutput.sendRequest(Request.LEGAL_NAME);
				clientOutput.sendBytesEncrypted(CommunicationUtils.wrapList(Main.getActiveClients()));
				clientOutput.sendBytesEncrypted(CommunicationUtils.wrapList(Main.getSharedFiles()));
				
				Main.registerClient(this);
				System.out.println("Just logged in: " + clientName);
			} else {
				clientOutput.sendRequest(Request.ILLEGAL_NAME);
				performShutdown();
			}
		} catch (Exception e) {
			System.out.println("Unexpected error while initializing connection.");
			performShutdown(); //ask the current loop to exit and close all resources
		}
	}
	
	public synchronized void performShutdown() {
		Thread.currentThread().interrupt();
	}

	@Override
	public String getName() {
		return clientName;
	}

	@Override
	public long getID() {
		return clientID;
	}
	
	public void sendToClientFromID(int request, long senderID, byte[] data) { //the ID represents the SENDER of the data
		try {
			clientOutput.sendRequest(request);
			clientOutput.sendBytesEncrypted(CryptoUtils.longToByteArray(senderID));
			clientOutput.sendBytesEncrypted(data);
		} catch (Exception e) {
			performShutdown(); //ask the current loop to exit and close all resources
		}
	}
	
	public void sendToClientFromID(int request, long senderID) { //the ID represents the SENDER of the data
		try {
			clientOutput.sendRequest(request);
			clientOutput.sendBytesEncrypted(CryptoUtils.longToByteArray(senderID));
		} catch (Exception e) {
			performShutdown(); //ask the current loop to exit and close all resources
		}
	}
}
