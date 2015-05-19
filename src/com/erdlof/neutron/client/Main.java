package com.erdlof.neutron.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

import com.erdlof.neutron.util.CryptoUtils;

public class Main {
	private static PublicKey serverPublicKey;
	
	public static void main(String[] args) {
		try {
			Socket client = new Socket("localhost", 12345);
			DataInputStream keyInput = new DataInputStream(client.getInputStream());
			DataOutputStream keyOutput = new DataOutputStream(client.getOutputStream());
			
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA"); //generate the keys
			keyGen.initialize(1024);
			KeyPair clientKeyPair = keyGen.generateKeyPair();

			keyOutput.write(CryptoUtils.intToByteArray(clientKeyPair.getPublic().getEncoded().length));
			keyOutput.write(clientKeyPair.getPublic().getEncoded());
			keyOutput.flush();

			byte[] serverPublicKeyLength = new byte[4];
			keyInput.read(serverPublicKeyLength);
			byte[] serverPublicKeyByte = new byte[CryptoUtils.byteArrayToInt(serverPublicKeyLength)];
			keyInput.read(serverPublicKeyByte);
			serverPublicKey = CryptoUtils.getPublicKeyFromEncoded(serverPublicKeyByte);

//			keyInput.close();
//			keyOutput.close();
			
			Cipher inputCipher = Cipher.getInstance("RSA");
			Cipher outputCipher = Cipher.getInstance("RSA");
			inputCipher.init(Cipher.DECRYPT_MODE, clientKeyPair.getPrivate());
			outputCipher.init(Cipher.ENCRYPT_MODE, serverPublicKey);
			
			CipherInputStream clientCipheredInput = new CipherInputStream(client.getInputStream(), inputCipher); //TODO create the encrypted streams with the keys we just exchanged, cipher needed
			CipherOutputStream clientCipheredOutput = new CipherOutputStream(client.getOutputStream(), outputCipher);
			
			final String name = "Herbert";
			clientCipheredOutput.write(CryptoUtils.intToByteArray(name.getBytes().length));
			clientCipheredOutput.write(name.getBytes());
			clientCipheredOutput.flush();
			
		} catch (Exception e) {
			System.out.println("CLIENT");
			e.printStackTrace();
		}
		
		
	}

}
