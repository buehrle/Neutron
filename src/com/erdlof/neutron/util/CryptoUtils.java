package com.erdlof.neutron.util;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class CryptoUtils {

	public static PublicKey getPublicKeyFromEncoded(byte[] encodedKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
		return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encodedKey));
	}
	
	public static int byteArrayToInt(byte[] b) {
	    return   b[3] & 0xFF |
	            (b[2] & 0xFF) << 8 |
	            (b[1] & 0xFF) << 16 |
	            (b[0] & 0xFF) << 24;
	}

	public static byte[] intToByteArray(int a){
	    return new byte[] {
	        (byte) ((a >> 24) & 0xFF),
	        (byte) ((a >> 16) & 0xFF),   
	        (byte) ((a >> 8) & 0xFF),   
	        (byte) (a & 0xFF)
	    };
	}
	
	public static byte[] createTotallyRandomIV() {
		byte[] tempIV = new byte[16];
		SecureRandom random = new SecureRandom();
		random.nextBytes(tempIV);
		return tempIV;
	}
	
	public static byte[] fillUpWithZerosAndData(byte[] actualData, int lengthOfResult) {
		if (lengthOfResult < actualData.length) return null;
		
		byte[] tempResult = new byte[lengthOfResult];
		
		for (int i = 0; i < actualData.length; i++) {
			tempResult[i] = actualData[i];
		}
		
		for (int i = lengthOfResult - actualData.length - 1; i < lengthOfResult; i++) {
			tempResult[i] = 0;
		}
		
		return tempResult;
	}
	
	public static byte[] cutZeros(byte[] filledData, int expectedLength) {
		if (expectedLength > filledData.length) return null;
		
		byte [] tempResult = new byte[expectedLength];
		
		for (int i = 0; i < expectedLength; i++) {
			tempResult[i] = filledData[i];
		}
		
		return tempResult;
	}

}
