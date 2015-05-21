package com.erdlof.neutron.server;

import java.io.IOException;
import java.io.OutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;

import com.erdlof.neutron.util.CryptoUtils;

public class DataCipherOutputStream extends CipherOutputStream {

	public DataCipherOutputStream(OutputStream os, Cipher c) {
		super(os, c);
	}
	
	public void sendBytes(byte[] bytes) throws IOException {
		super.write(CryptoUtils.intToByteArray(bytes.length));
		super.write(bytes);
		super.flush();
	}
	
	public void sendRequest(byte request) throws IOException {
		super.write(request);
		super.flush();
	}

}
