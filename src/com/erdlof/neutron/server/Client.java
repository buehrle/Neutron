package com.erdlof.neutron.server;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.erdlof.neutron.client.SharedAssociation;
import com.erdlof.neutron.filesharing.FileReceiver;
import com.erdlof.neutron.filesharing.FileReceivingListener;
import com.erdlof.neutron.filesharing.FileSender;
import com.erdlof.neutron.filesharing.FileSendingListener;
import com.erdlof.neutron.streams.BetterDataInputStream;
import com.erdlof.neutron.streams.BetterDataOutputStream;
import com.erdlof.neutron.util.CheckUtils;
import com.erdlof.neutron.util.CryptoUtils;
import com.erdlof.neutron.util.Request;

@SuppressWarnings("serial")
public class Client extends SharedAssociation implements Runnable, FileReceivingListener, FileSendingListener {
	private static final int MAX_COUNTS_UNTIL_TIMEOUT = 1000;
	private static final String ALGORITHM_PADDING = "AES/CBC/PKCS5PADDING";

	private Socket clientSocket;
	private int timeoutCounter;
	private BetterDataInputStream clientInput;
	private BetterDataOutputStream clientOutput;
	
	private Cipher wrapCipher;
	private Cipher outputCipher;
	private Cipher inputCipher;
	
	private PublicKey publicKey; //the CLIENT'S public key
	private SecretKey secretKey;
	
	private byte[] IV;
	
	private ServerCoordinator coordinator;
	
	public Client(Socket clientSocket, long clientID, ServerCoordinator coordinator) throws IOException {
		super(clientID, "");
		
		this.clientSocket = clientSocket;
		timeoutCounter = 0;
		
		this.coordinator = coordinator;
		
		try {
			KeyGenerator keyGen = KeyGenerator.getInstance("AES", BouncyCastleProvider.PROVIDER_NAME);
			
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
								if (tempData.length > 0) coordinator.sendToAllClients(request, super.getID(), tempData);
								break;
							case Request.SEND_FILE: //can I pls send a file to the server
								String fileName = new String(clientInput.getBytesDecrypted());
								
								new FileReceiver(coordinator.getFileServer().accept(), IV, secretKey, this, "/home/erdlof/workspace/" + fileName, 1024).start(); //TODO CONFIG FILES for the standard file destination
								break;
							case Request.GET_FILE:
								long fileID = CryptoUtils.byteArrayToLong(clientInput.getBytesDecrypted());
								
								new FileSender(coordinator.getFileServer().accept(), IV, secretKey, this, coordinator.getFileByID(fileID), 1024).start();
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
				coordinator.unregisterClient(this); //the fact that this is executed on the end of the thread MAY cause an exception, but that's okay
			}
		}
	}
	
	private void initConnection() {
		try {
			clientInput = new BetterDataInputStream(clientSocket.getInputStream());
			clientOutput = new BetterDataOutputStream(clientSocket.getOutputStream());
			
			publicKey = CryptoUtils.getPublicKeyFromEncoded(clientInput.getBytes()); // get the client's public key from a byte array
	
			wrapCipher = Cipher.getInstance("RSA", BouncyCastleProvider.PROVIDER_NAME);
			wrapCipher.init(Cipher.WRAP_MODE, publicKey);
			byte[] wrappedKey = wrapCipher.wrap(secretKey); //we wrap the AES-key with a public RSA-key from the client to transfer it securely
			clientOutput.sendBytes(wrappedKey);
			
			clientOutput.sendBytes(IV); //it does what it seems to do.
	
			inputCipher = Cipher.getInstance(ALGORITHM_PADDING, BouncyCastleProvider.PROVIDER_NAME); //the cipher for decrypting data from the client
			outputCipher = Cipher.getInstance(ALGORITHM_PADDING, BouncyCastleProvider.PROVIDER_NAME); //encrypting
			inputCipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(IV));
			outputCipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(IV));
			
			clientInput.initCipher(inputCipher); //init the streams with the ciphers
			clientOutput.initCipher(outputCipher);
			
			clientOutput.sendBytesEncrypted(CryptoUtils.longToByteArray(super.getID())); //send the ID to the client
			
			super.setName(new String(clientInput.getBytesDecrypted()));
			
			if (CheckUtils.isProperNickname(super.getName())) {
				clientOutput.sendRequest(Request.LEGAL_NAME);
				
				clientOutput.sendIntEncrypted(coordinator.getActiveClients().size());
				clientOutput.sendIntEncrypted(coordinator.getSharedFiles().size());
				
				for (SharedAssociation client : coordinator.getActiveClients()) {
					clientOutput.sendBytesEncrypted(CryptoUtils.longToByteArray(client.getID()));
					clientOutput.sendBytesEncrypted(client.getName().getBytes());
				}
				
				for (SharedAssociation file : coordinator.getSharedFiles()) {
					clientOutput.sendBytesEncrypted(CryptoUtils.longToByteArray(file.getID()));
					clientOutput.sendBytesEncrypted(file.getName().getBytes());
				}
				
				coordinator.registerClient(this);
				System.out.println("Just logged in: " + super.getName());
			} else {
				clientOutput.sendRequest(Request.ILLEGAL_NAME);
				performShutdown();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Unexpected error while initializing connection.");
			performShutdown(); //ask the current loop to exit and close all resources
		}
	}
	
	public synchronized void performShutdown() {
		Thread.currentThread().interrupt();
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

	@Override
	public void fileShareError() {} //eventually we will do this later 

	@Override
	public void receivingProgress(int bytesReceived) {}

	@Override
	public void receivingCompleted(File file) {
		coordinator.registerFile(file);
	}

	@Override
	public void sendingProgress(int bytesSent) {}

	@Override
	public void sendingCompleted() {}

	@Override
	public synchronized boolean isFilesharingCanceled() {
		return Thread.currentThread().isInterrupted();
	}

	@Override
	public void setFileSize(int size) {}
}
