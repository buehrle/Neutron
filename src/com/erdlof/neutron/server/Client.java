package com.erdlof.neutron.server;

import java.io.IOException;
import java.net.Socket;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import com.erdlof.neutron.util.CryptoUtils;
import com.erdlof.neutron.util.RequestedAction;

public class Client implements Runnable {
	private Socket clientSocket;
	private BetterDataInputStream clientInput;
	private BetterDataOutputStream clientOutput;
	
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
			
			keyGen.init(256);
			secretKey = keyGen.generateKey();
			
			IV = CryptoUtils.createTotallyRandomIV();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try {
			clientInput = new BetterDataInputStream(clientSocket.getInputStream());
			clientOutput = new BetterDataOutputStream(clientSocket.getOutputStream());
			
			publicKey = CryptoUtils.getPublicKeyFromEncoded(clientInput.getBytes()); // get the client's public key from a byte array

			wrapCipher = Cipher.getInstance("RSA");
			wrapCipher.init(Cipher.WRAP_MODE, publicKey);
			byte[] wrappedKey = wrapCipher.wrap(secretKey);
			clientOutput.sendBytes(wrappedKey);
			
			clientOutput.sendBytes(IV);

			inputCipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			outputCipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			inputCipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(IV));
			outputCipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(IV));
			
			clientInput.initCipher(inputCipher);
			clientOutput.initCipher(outputCipher);
			
			clientName = new String(clientInput.getBytesDecrypted(), "UTF-8");
			System.out.println("Just logged in: " + clientName);
			
			while(!Thread.currentThread().isInterrupted()) {
				if (clientInput.available() > 0) {
					int request = clientInput.getRequest();
					
					switch (request) { //what does the client want???
						case RequestedAction.SEND_TEXT:
							Main.textMessageReceived(this, new String(clientInput.getBytesDecrypted(), "UTF-8"));		
							break;
						default: 
							Thread.currentThread().interrupt();
							break;
					}
				}
			}
			
			clientInput.close();
			clientOutput.close();
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
