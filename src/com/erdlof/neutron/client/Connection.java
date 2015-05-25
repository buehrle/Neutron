package com.erdlof.neutron.client;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyPair;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import com.erdlof.neutron.streams.BetterDataInputStream;
import com.erdlof.neutron.streams.BetterDataOutputStream;
import com.erdlof.neutron.util.CryptoUtils;
import com.erdlof.neutron.util.Request;

public class Connection implements Runnable {
	private static final String ALGORITHM_PADDING = "AES/CBC/PKCS5PADDING";
	
	private BetterDataInputStream serverInput;
	private BetterDataOutputStream serverOutput;
	private Cipher inputCipher;
	private Cipher outputCipher;
	private Cipher unwrapCipher;
	
	private Socket client;
	
	private SecretKey secretKey;
	private KeyPair keyPair;
	private byte[] IV;
	private byte[] wrappedKey;
	
	private String name;
	private long clientID;
	
	public Connection(KeyPair keyPair, String name) {
		this.keyPair = keyPair;
		this.name = name;
	}
	
	@Override
	public void run() {
		init();
		
		try {
			while (!Thread.currentThread().isInterrupted()) {
				if (serverInput.available() > 0) {
					int request = serverInput.getRequest();
					
					switch (request) {
						case Request.SEND_TEXT:
						case Request.SEND_FILE:
						case Request.CLIENT_DISCONNECT_NOTIFICATION:
						case Request.CLIENT_CONNECT_NOTIFICATION:
						default:
							Main.setDisconnectRequest(request);
							performShutdown();
							break;
					}
				}
				//TODO implement the alive-sender ELEGANTLY
				if (!Thread.currentThread().isInterrupted()) Thread.sleep(10);
			}
		} catch (Exception e) {
			Main.setDisconnectRequest(Request.UNEXPECTED_ERROR);
		} finally {
			try {
				serverInput.close();
				serverOutput.close();
				client.close();
			} catch (IOException e) {
			}
		}
		Main.disconnected();
	}
	
	public void init() { //pretty much the same as in server.Client.java
		try {
			client = new Socket("localhost", 12345);
			serverInput = new BetterDataInputStream(client.getInputStream());
			serverOutput = new BetterDataOutputStream(client.getOutputStream());
			
			serverOutput.sendBytes(keyPair.getPublic().getEncoded());

			wrappedKey = serverInput.getBytes();
			
			IV = serverInput.getBytes();

			unwrapCipher = Cipher.getInstance("RSA");
			unwrapCipher.init(Cipher.UNWRAP_MODE, keyPair.getPrivate());
			secretKey = (SecretKey) unwrapCipher.unwrap(wrappedKey, "AES", Cipher.SECRET_KEY);
			
			inputCipher = Cipher.getInstance(ALGORITHM_PADDING);
			outputCipher = Cipher.getInstance(ALGORITHM_PADDING);
			inputCipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(IV));
			outputCipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(IV));
			
			serverInput.initCipher(inputCipher);
			serverOutput.initCipher(outputCipher);
			
			clientID = CryptoUtils.byteArrayToLong(serverInput.getBytesDecrypted());
			
			serverOutput.sendBytesEncrypted(name.getBytes());
			Main.connectionEstablished();
		} catch (Exception e) {
			Main.connectionFailed();
			performShutdown();
		}
	}
	
	private void performShutdown() {
		Thread.currentThread().interrupt();
	}

	public long getClientID() {
		return clientID;
	}
	
	

}
