package com.erdlof.neutron.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;

import com.erdlof.neutron.util.CryptoUtils;
import com.erdlof.neutron.util.RequestedAction;

public class Main {
	static CipherOutputStream clientCipheredOutput;
	public static void main(String[] args) {
		try {
			Socket client = new Socket("localhost", 12345);
			DataInputStream serverInitInput = new DataInputStream(client.getInputStream());
			DataOutputStream serverInitOutput = new DataOutputStream(client.getOutputStream());
			
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA"); //generate the keys
			SecureRandom random = new SecureRandom();
			keyGen.initialize(2048, random);
			KeyPair keyPair = keyGen.generateKeyPair();
			
			byte[] encodedPublicKey = keyPair.getPublic().getEncoded();

			serverInitOutput.write(CryptoUtils.intToByteArray(encodedPublicKey.length));
			serverInitOutput.write(encodedPublicKey);
			serverInitOutput.flush();

			byte[] wrappedKeyLength = new byte[4];
			serverInitInput.read(wrappedKeyLength);
			byte[] wrappedKey = new byte[CryptoUtils.byteArrayToInt(wrappedKeyLength)];
			serverInitInput.read(wrappedKey);

			Cipher unwrapCipher = Cipher.getInstance("RSA");
			unwrapCipher.init(Cipher.UNWRAP_MODE, keyPair.getPrivate());
			Key secretKey = unwrapCipher.unwrap(wrappedKey, "AES", Cipher.SECRET_KEY);

			Cipher inputCipher = Cipher.getInstance("AES/CBC/NoPadding");
			Cipher outputCipher = Cipher.getInstance("AES/CBC/NoPadding");
			inputCipher.init(Cipher.DECRYPT_MODE, secretKey);
			outputCipher.init(Cipher.ENCRYPT_MODE, secretKey);
			
			CipherInputStream clientCipheredInput = new CipherInputStream(serverInitInput, inputCipher);
			clientCipheredOutput = new CipherOutputStream(serverInitOutput, outputCipher);
			

			final String name = "1111111111111111";
			clientCipheredOutput.write(name.getBytes().length);
			clientCipheredOutput.write(name.getBytes());
			clientCipheredOutput.flush();
			
//			clientCipheredOutput.write(RequestedAction.SEND_TEXT);
//			final String hi = "Ich mag Schokolade.";
//			clientCipheredOutput.write(CryptoUtils.intToByteArray(hi.getBytes().length));
//			clientCipheredOutput.write(hi.getBytes());
//			
		} catch (Exception e) {
			System.out.println("CLIENT");
			e.printStackTrace();
		}
		
		
	}

}
