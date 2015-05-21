package com.erdlof.neutron.server;

import java.io.IOException;
import java.io.InputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;

import com.erdlof.neutron.util.CryptoUtils;

public class DataCipherInputStream extends CipherInputStream {

	public DataCipherInputStream(InputStream is, Cipher c) {
		super(is, c);
	}
	
	public byte[] getBytes() throws IOException {
		byte[] lengthTemp = new byte[4];
		super.read(lengthTemp);
		
		byte[] tempData = new byte[CryptoUtils.byteArrayToInt(lengthTemp)];
		super.read(tempData);
		
		return tempData;
	}

}
