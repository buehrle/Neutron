package com.erdlof.neutron.server;

import java.io.IOException;
import java.net.Socket;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import com.erdlof.neutron.util.CryptoUtils;
import com.erdlof.neutron.util.RequestedAction;

public class Client implements Runnable {
	private Socket clientSocket;
	private BetterDataInputStream clientInitInput;
	private BetterDataOutputStream clientInitOutput;
	
	private DataCipherInputStream clientCipheredInput;
	private DataCipherOutputStream clientCipheredOutput;
	private Cipher wrapCipher;
	private Cipher outputCipher;
	private Cipher inputCipher;
	
	private String clientName;
	private PublicKey publicKey; //the CLIENT'S public key
	private SecretKey secretKey;
	
	private byte[] IV;
	
	public Client(Socket clientSocket) throws IOException {
		this.clientSocket = clientSocket;
		
		try {
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			SecureRandom random = new SecureRandom();
			
			keyGen.init(256, random);
			secretKey = keyGen.generateKey();
			
			IV = CryptoUtils.createTotallyRandomIV();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try {
			clientInitInput = new BetterDataInputStream(clientSocket.getInputStream());
			clientInitOutput = new BetterDataOutputStream(clientSocket.getOutputStream());
			
			publicKey = CryptoUtils.getPublicKeyFromEncoded(clientInitInput.getBytes()); // get the client's public key from a byte array

			wrapCipher = Cipher.getInstance("RSA");
			wrapCipher.init(Cipher.WRAP_MODE, publicKey);
			byte[] wrappedKey = wrapCipher.wrap(secretKey);
			clientInitOutput.sendBytes(wrappedKey);
			
			clientInitOutput.sendBytes(IV);

			inputCipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			outputCipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			inputCipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(IV));
			outputCipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(IV));
			
			clientCipheredInput = new DataCipherInputStream(clientInitInput, inputCipher); // create the encrypted streams with the keys we just exchanged, cipher needed
			clientCipheredOutput = new DataCipherOutputStream(clientInitOutput, outputCipher);
			
			clientName = new String(clientCipheredInput.getBytes(), "UTF-8");
			System.out.println("Just logged in: " + clientName);
			
			while(!Thread.currentThread().isInterrupted()) {
				if (clientCipheredInput.available() > 0) {
					int request = clientCipheredInput.read();
					
					switch (request) { //what does the client want???
						case RequestedAction.SEND_TEXT:
							Main.textMessageReceived(this, new String(clientCipheredInput.getBytes(), "UTF-8"));		
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

	public String getClientName() {
		return clientName;
	}

}
