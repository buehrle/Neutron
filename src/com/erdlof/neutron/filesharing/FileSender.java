package com.erdlof.neutron.filesharing;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import com.erdlof.neutron.streams.BetterDataOutputStream;

public class FileSender extends Thread {
	private Socket socket;
	private byte[] IV;
	private SecretKey key;
	private Cipher cipher;
	private FileSendingListener listener;
	private File source;
	
	public FileSender(Socket socket, byte[] IV, SecretKey key, FileSendingListener listener, File source) {
		this.socket = socket;
		this.listener = listener;
		this.source = source;
	}
	
	@Override
	public void run() {
		try (BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(source));
				BetterDataOutputStream output = new BetterDataOutputStream(socket.getOutputStream())) {
			
			cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(IV));
			
			byte[] tempData = new byte[(int) source.length()];
			fileInputStream.read(tempData, 0, (int) source.length());
			
			byte[] encTempData = cipher.doFinal(tempData);
			
			output.writeInt(encTempData.length);
			output.flush();
			
			for (int i = 0; i < encTempData.length; i++) {
				output.write(encTempData[i]);
				output.flush();
				if (i % 512 == 0) listener.sendingProgress(i);
			}
			
			listener.sendingCompleted();
		} catch (Exception e) {
			listener.fileShareError();
		}
		
	}
}
