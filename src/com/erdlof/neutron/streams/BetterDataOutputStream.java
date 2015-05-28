package com.erdlof.neutron.streams;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import com.erdlof.neutron.util.CryptoUtils;

public class BetterDataOutputStream extends DataOutputStream {
	private Cipher outputCipher;

	public BetterDataOutputStream(OutputStream out) {
		super(out);
	}
	
	public void initCipher(Cipher outputCipher) {
		this.outputCipher = outputCipher;
	}
	
	public void sendBytes(byte[] bytes) throws IOException {
		super.write(CryptoUtils.intToByteArray(bytes.length));
		super.write(bytes);
		super.flush();
	}
	
	public void sendBytesEncrypted(byte[] bytes) throws IllegalBlockSizeException, BadPaddingException, IOException {
		if (outputCipher == null) throw new NullPointerException("The Cipher was not initialized!");
		
		byte[] tempDataCiphered = outputCipher.doFinal(bytes);
		byte[] lengthCiphered = outputCipher.doFinal(CryptoUtils.intToByteArray(tempDataCiphered.length));
		
		super.write(lengthCiphered);
		super.write(tempDataCiphered);
		super.flush();
	}
	
	public synchronized void sendRequest(int request) throws IllegalBlockSizeException, BadPaddingException, IOException {
		if (outputCipher == null) throw new NullPointerException("The Cipher was not initialized!");
		
		super.write(outputCipher.doFinal(CryptoUtils.intToByteArray(request)));
		super.flush();
	}
}
