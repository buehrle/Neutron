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
import javax.crypto.spec.IvParameterSpec;

import com.erdlof.neutron.server.BetterDataInputStream;
import com.erdlof.neutron.server.BetterDataOutputStream;
import com.erdlof.neutron.server.DataCipherInputStream;
import com.erdlof.neutron.server.DataCipherOutputStream;
import com.erdlof.neutron.util.CryptoUtils;
import com.erdlof.neutron.util.RequestedAction;

public class Main {
	static DataCipherOutputStream clientCipheredOutput;
	static DataCipherInputStream clientCipheredInput;
	static BetterDataInputStream serverInitInput;
	static BetterDataOutputStream serverInitOutput;
	
	static KeyPair keyPair;
	static byte[] IV;
	
	public static void main(String[] args) {
		try {
			Socket client = new Socket("localhost", 12345);
			serverInitInput = new BetterDataInputStream(client.getInputStream());
			serverInitOutput = new BetterDataOutputStream(client.getOutputStream());
			
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA"); //generate the keys
			SecureRandom random = new SecureRandom();
			keyGen.initialize(2048, random);
			keyPair = keyGen.generateKeyPair();
			
			serverInitOutput.sendBytes(keyPair.getPublic().getEncoded());

			byte[] wrappedKey = serverInitInput.getBytes();
			
			IV = serverInitInput.getBytes();

			Cipher unwrapCipher = Cipher.getInstance("RSA");
			unwrapCipher.init(Cipher.UNWRAP_MODE, keyPair.getPrivate());
			SecretKey secretKey = (SecretKey) unwrapCipher.unwrap(wrappedKey, "AES", Cipher.SECRET_KEY);

			Cipher inputCipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			Cipher outputCipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			inputCipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(IV));
			outputCipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(IV));
			
			clientCipheredInput = new DataCipherInputStream(serverInitInput, inputCipher);
			clientCipheredOutput = new DataCipherOutputStream(serverInitOutput, outputCipher);
			

			byte[] test = new byte[16];
			clientCipheredOutput.sendBytes(test);
			
//			
		} catch (Exception e) {
			System.out.println("CLIENT");
			e.printStackTrace();
		}
		
		
	}

}
