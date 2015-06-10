package com.erdlof.neutron.client;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyPair;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import com.erdlof.neutron.filesharing.FileSender;
import com.erdlof.neutron.streams.BetterDataInputStream;
import com.erdlof.neutron.streams.BetterDataOutputStream;
import com.erdlof.neutron.util.CommunicationUtils;
import com.erdlof.neutron.util.CryptoUtils;
import com.erdlof.neutron.util.Request;

public class Connection extends Thread {
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
	
	private volatile int aliveCounter;
	
	private ClientListener listener;
	private String serverAdress;
	
	public Connection(KeyPair keyPair, String name, String serverAdress, ClientListener listener) {
		this.listener = listener;
		this.keyPair = keyPair;
		this.name = name;
		this.serverAdress = serverAdress;
		
		aliveCounter = 0;
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
							listener.textMessage(CryptoUtils.byteArrayToLong(serverInput.getBytesDecrypted()), serverInput.getBytesDecrypted());
							break;
						case Request.CLIENT_DISCONNECT_NOTIFICATION:
							listener.clientDisconnected(CryptoUtils.byteArrayToLong(serverInput.getBytesDecrypted()));
							break;
						case Request.CLIENT_CONNECT_NOTIFICATION:
							listener.clientConnected(CryptoUtils.byteArrayToLong(serverInput.getBytesDecrypted()), serverInput.getBytesDecrypted());
							break;
						default:
							listener.setDisconnectRequest(request);
							performShutdown();
							break;
					}
				}
				
				if (!Thread.currentThread().isInterrupted()) {
					Thread.sleep(10);
					
					if (incrementAliveCounter() > 500) {
						serverOutput.sendRequest(Request.ALIVE);
						resetAliveCounter();
					}
				}
			}
		} catch (InterruptedException e) {
		} catch (Exception e) {
			e.printStackTrace();
			listener.setDisconnectRequest(Request.UNEXPECTED_ERROR);
		} finally {
			try {
				serverInput.close();
				serverOutput.close();
				client.close();
			} catch (IOException e) {
			} finally {
				listener.disconnected();
			}
		}
	}
	
	public void init() { //pretty much the same as in server.Client.java
		try {
			client = new Socket(serverAdress, 12345);
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
			
			int nameRequest = serverInput.getRequest();
			
			if (nameRequest == Request.LEGAL_NAME) {
				byte[] wrappedPartnerList = serverInput.getBytesDecrypted();
				byte[] wrappedFileList = serverInput.getBytesDecrypted();
				
				listener.connectionEstablished(CommunicationUtils.unwrapList(wrappedPartnerList), CommunicationUtils.unwrapList(wrappedFileList));
			} else {
				listener.setDisconnectRequest(nameRequest);
				performShutdown();
			}
		} catch (Exception e) {
			e.printStackTrace();
			listener.connectionFailed();
			performShutdown();
		}
	}
	
	private synchronized int incrementAliveCounter() {
		return aliveCounter++;
	}
	
	private synchronized void resetAliveCounter() {
		aliveCounter = 0;
	}
	
	private void performShutdown() {
		this.interrupt();
	}

	public long getClientID() {
		return clientID;
	}
	
	public void sendData(int request, byte[] data) {
		resetAliveCounter();
		try {
			serverOutput.sendRequest(request);
			serverOutput.sendBytesEncrypted(data);
		} catch (Exception e) {
		}
	}
	
	public void disconnect() {
		try {
			serverOutput.sendRequest(Request.REGULAR_DISCONNECT);
		} catch (Exception e) {}
		performShutdown();
	}
	
	public void uploadFile(File file) {
		try {
			sendData(Request.SEND_FILE, file.getName().getBytes());
			FileshareIndicatorMonitor monitor = new FileshareIndicatorMonitor("Uploading file...", "", 0, 0);
			new FileSender(new Socket(client.getInetAddress(), 12346), IV, secretKey, monitor, file);
		} catch (IOException e) {
		}
	}
}
