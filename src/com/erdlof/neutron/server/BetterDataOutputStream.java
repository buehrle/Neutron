package com.erdlof.neutron.server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.erdlof.neutron.util.CryptoUtils;

public class BetterDataOutputStream extends DataOutputStream {

	public BetterDataOutputStream(OutputStream out) {
		super(out);
	}
	
	public void sendBytes(byte[] bytes) throws IOException {
		super.write(CryptoUtils.intToByteArray(bytes.length));
		super.write(bytes);
		super.flush();
	}	

}
