package com.erdlof.neutron.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.erdlof.neutron.util.CryptoUtils;

public class BetterDataInputStream extends DataInputStream {

	public BetterDataInputStream(InputStream in) {
		super(in);
	}
	
	public byte[] getBytes() throws IOException {
		byte[] lengthTemp = new byte[4];
		super.read(lengthTemp);
		
		byte[] tempData = new byte[CryptoUtils.byteArrayToInt(lengthTemp)];
		super.read(tempData);
		
		return tempData;
	}

}
