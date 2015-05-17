package com.erdlof.neutron.util;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class CryptoUtils {

	public static PublicKey getPublicKeyFromEncoded(byte[] encodedKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
		return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encodedKey));
	}

}
