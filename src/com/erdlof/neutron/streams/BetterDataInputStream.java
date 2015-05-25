package com.erdlof.neutron.streams;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import com.erdlof.neutron.util.CryptoUtils;

public class BetterDataInputStream extends DataInputStream {
	private Cipher inputCipher;
	
	public BetterDataInputStream(InputStream in) {
		super(in);
	}
	
	public void initCipher(Cipher inputCipher) {
		this.inputCipher = inputCipher;
	}
	
	public byte[] getBytes() throws IOException { //read a number of bytes (with forwarded length) from the network and return it.
		byte[] lengthTemp = new byte[4];
		super.read(lengthTemp);
		
		byte[] tempData = new byte[CryptoUtils.byteArrayToInt(lengthTemp)];
		super.read(tempData);
		
		return tempData;
	}
	
	public byte[] getBytesDecrypted() throws  IllegalBlockSizeException, BadPaddingException, IOException { //does the same as getBytes() but it also uses the cipher to decrypt the data
		if (inputCipher == null) throw new NullPointerException("The Cipher was not initialized!");
		
		byte[] lengthTempCiphered = new byte[16];
		super.read(lengthTempCiphered);
		
		int lengthTemp = CryptoUtils.byteArrayToInt(inputCipher.doFinal(lengthTempCiphered));
		
		byte[] tempData = new byte[lengthTemp];
		super.read(tempData);

		return inputCipher.doFinal(tempData);
	}
	
	public int getRequest() throws IOException, IllegalBlockSizeException, BadPaddingException {
		if (inputCipher == null) throw new NullPointerException("The Cipher was not initialized!");
		
		byte[] cipheredRequest = new byte[16];
		super.read(cipheredRequest);
		
		return CryptoUtils.byteArrayToInt(inputCipher.doFinal(cipheredRequest));
	}

}
