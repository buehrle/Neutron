package com.erdlof.neutron.util;

import java.nio.ByteBuffer;
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
		return ByteBuffer.wrap(b).getInt();
	}

	public static byte[] intToByteArray(int a){
		return ByteBuffer.allocate(4).putInt(a).array();
	}
	
	public static byte[] longToByteArray(long a) {
		return ByteBuffer.allocate(8).putLong(a).array();
	}
	
	public static long byteArrayToLong(byte[] b) {
		return ByteBuffer.wrap(b).getLong();
	}
	
	public static byte[] createTotallyRandomIV() {
		byte[] tempIV = new byte[16];
		SecureRandom random = new SecureRandom();
		random.nextBytes(tempIV);
		return tempIV;
	}
	
	@Deprecated
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
	
	@Deprecated
	public static byte[] cutZeros(byte[] filledData, int expectedLength) {
		if (expectedLength > filledData.length) return null;
		
		byte [] tempResult = new byte[expectedLength];
		
		for (int i = 0; i < expectedLength; i++) {
			tempResult[i] = filledData[i];
		}
		
		return tempResult;
	}
	
	public static void removeCryptographyRestrictions() {
		if (!isRestrictedCryptography()) {
			return;
		}
		try {
			java.lang.reflect.Field isRestricted;
			try {
				final Class<?> c = Class.forName("javax.crypto.JceSecurity");
				isRestricted = c.getDeclaredField("isRestricted");
			} catch (final ClassNotFoundException e) {
				try {
					// Java 6 has obfuscated JCE classes
					final Class<?> c = Class.forName("javax.crypto.SunJCE_b");
					isRestricted = c.getDeclaredField("g");
				} catch (final ClassNotFoundException e2) {
					throw e;
				}
			}
			isRestricted.setAccessible(true);
			isRestricted.set(null, false);
		} catch (final Throwable e) {

		}
	}

	private static boolean isRestrictedCryptography() {
		return "Java(TM) SE Runtime Environment".equals(System.getProperty("java.runtime.name"));
	}

}
