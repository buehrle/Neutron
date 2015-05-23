package com.erdlof.neutron.client;

import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import com.erdlof.neutron.server.BetterDataInputStream;
import com.erdlof.neutron.server.BetterDataOutputStream;

public class Main {
	static BetterDataInputStream serverInput;
	static BetterDataOutputStream serverOutput;
	static Cipher inputCipher;
	static Cipher outputCipher;
	
	static KeyPair keyPair;
	static byte[] IV;
	
	public static void main(String[] args) {
		try {
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

			Cipher unwrapCipher = Cipher.getInstance("RSA");
			unwrapCipher.init(Cipher.UNWRAP_MODE, keyPair.getPrivate());
			SecretKey secretKey = (SecretKey) unwrapCipher.unwrap(wrappedKey, "AES", Cipher.SECRET_KEY);

			inputCipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			outputCipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			inputCipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(IV));
			outputCipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(IV));
			
			serverInput.initCipher(inputCipher);
			serverOutput.initCipher(outputCipher);
			
			serverOutput.sendBytesEncrypted("Zügfgdfgdfgdffdgdfgdfgdfgdfgdfgdfgdfge".getBytes());
//			
		} catch (Exception e) {
			System.out.println("CLIENT");
			e.printStackTrace();
		}
		
		
	}

}
