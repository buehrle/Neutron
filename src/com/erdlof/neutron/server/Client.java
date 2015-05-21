package com.erdlof.neutron.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import com.erdlof.neutron.util.CryptoUtils;
import com.erdlof.neutron.util.RequestedAction;

public class Client implements Runnable {
	private Socket clientSocket;
	private DataInputStream clientInitInput;
	private DataOutputStream clientInitOutput;
	
	private CipherInputStream clientCipheredInput;
	private CipherOutputStream clientCipheredOutput;
	private Cipher wrapCipher;
	private Cipher outputCipher;
	private Cipher inputCipher;
	
	private String clientName;
	private PublicKey publicKey; //the CLIENT'S public key
	private SecretKey secretKey;
	
	public Client(Socket clientSocket) throws IOException {
		this.clientSocket = clientSocket;
		
		try {
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			SecureRandom random = new SecureRandom();
			
			keyGen.init(256, random);
			secretKey = keyGen.generateKey();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try {
			clientInitInput = new DataInputStream(clientSocket.getInputStream());
			clientInitOutput = new DataOutputStream(clientSocket.getOutputStream());
			
			byte[] encodedPublicKey = new byte[getLengthOfFollowingData(clientInitInput)];
			clientInitInput.read(encodedPublicKey);
			publicKey = CryptoUtils.getPublicKeyFromEncoded(encodedPublicKey); // get the client's public key from a byte array

			wrapCipher = Cipher.getInstance("RSA");
			wrapCipher.init(Cipher.WRAP_MODE, publicKey);
			// BIS HIER STIMMT NOCH ALLES
			byte[] wrappedKey = wrapCipher.wrap(secretKey);
			clientInitOutput.write(CryptoUtils.intToByteArray(wrappedKey.length));
			clientInitOutput.write(wrappedKey);
			clientInitOutput.flush();
			
			inputCipher = Cipher.getInstance("AES/CBC/NoPadding");
			outputCipher = Cipher.getInstance("AES/CBC/NoPadding");
			inputCipher.init(Cipher.DECRYPT_MODE, secretKey);
			outputCipher.init(Cipher.ENCRYPT_MODE, secretKey);
			
			clientCipheredInput = new CipherInputStream(clientInitInput, inputCipher); // create the encrypted streams with the keys we just exchanged, cipher needed
			clientCipheredOutput = new CipherOutputStream(clientInitOutput, outputCipher);
			
			byte[] clientName_ = new byte[getLengthOfFollowingData(clientCipheredInput)];
			clientCipheredInput.read(clientName_); // get the client's nickname
			clientName = new String(clientName_, "UTF-8");
			System.out.println("Just logged in: " + clientName);
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
