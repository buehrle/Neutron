package com.erdlof.neutron.client;

import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import com.erdlof.neutron.streams.BetterDataInputStream;
import com.erdlof.neutron.streams.BetterDataOutputStream;
import com.erdlof.neutron.util.Request;

public class Main implements Runnable {
	private BetterDataInputStream serverInput;
	private BetterDataOutputStream serverOutput;
	private Cipher inputCipher;
	private Cipher outputCipher;
	private Cipher unwrapCipher;
	
	private SecretKey secretKey;
	private KeyPair keyPair;
	private byte[] IV;
	private int number;
	
	public static void main(String[] args) {
		
		for (int i = 0; i < 10; i++) {
			new Thread(new Main(i)).start();
		}
		
	}
	
	public Main(int number) {
		this.number = number;
	}
	
	@Override
	public void run() {
		try {
			//THIS IS JUST TESTING CODE AND NOT INTENDED FOR LOOKING AT IT. PLEASE GO AWAY OR READ VERY CAREFULLY.
			@SuppressWarnings("resource")
			Socket client = new Socket("localhost", 12345);
			serverInput = new BetterDataInputStream(client.getInputStream());
			serverOutput = new BetterDataOutputStream(client.getOutputStream());
			
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA"); //generate the keys
			keyGen.initialize(2048);
			keyPair = keyGen.generateKeyPair();
			
			serverOutput.sendBytes(keyPair.getPublic().getEncoded());

			byte[] wrappedKey = serverInput.getBytes();
			
			IV = serverInput.getBytes();

			unwrapCipher = Cipher.getInstance("RSA");
			unwrapCipher.init(Cipher.UNWRAP_MODE, keyPair.getPrivate());
			secretKey = (SecretKey) unwrapCipher.unwrap(wrappedKey, "AES", Cipher.SECRET_KEY);
			
			inputCipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			outputCipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			inputCipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(IV));
			outputCipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(IV));
			
			serverInput.initCipher(inputCipher);
			serverOutput.initCipher(outputCipher);
			
			serverOutput.sendBytesEncrypted("bertHerbertHerbertHerbertertff".getBytes());
			serverOutput.sendRequest(Request.SEND_TEXT);
			serverOutput.sendBytesEncrypted((number + " sagt Hallo.").getBytes());
			
			while (true) {
				serverOutput.sendRequest(Request.ALIVE);
				Thread.sleep(1000);
			}

//			
		} catch (Exception e) {
			System.out.println("CLIENT");
			e.printStackTrace();
		}
		
	}

}
