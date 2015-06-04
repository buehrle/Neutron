package com.erdlof.neutron.filesharing;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.Socket;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import com.erdlof.neutron.streams.BetterDataInputStream;

public class FileReceiver extends Thread {
	private Socket socket;
	private byte[] IV;
	private SecretKey key;
	private int maxFileLength;
	private FileReceivingListener listener;
	private BetterDataInputStream input;
	private Cipher cipher;
	private BufferedOutputStream fileOutputStream;
	private int encryptedFileLength;
	private boolean hasFileSizeRestriction = false;
	private File file;
	
	public FileReceiver(Socket socket, byte[] IV, SecretKey key, FileReceivingListener listener, String destination, int encryptedFileLength, int maxFileLength) throws FileNotFoundException {
		this(socket, IV, key, listener, destination, encryptedFileLength);
		this.maxFileLength = maxFileLength;
		this.hasFileSizeRestriction = true;
	}
	
	public FileReceiver(Socket socket, byte[] IV, SecretKey key, FileReceivingListener listener, String destination, int encryptedFileLength) throws FileNotFoundException {
		file = new File(destination);
		file.getParentFile().mkdirs();
		fileOutputStream = new BufferedOutputStream(new FileOutputStream(file));
		this.socket = socket;
		this.IV = IV;
		this.key = key;
		this.listener = listener;
		this.encryptedFileLength = encryptedFileLength;
	}
	
	@Override
	public void run() {
		try {
			input = new BetterDataInputStream(socket.getInputStream());
			cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(IV));

			if (!hasFileSizeRestriction ? true : encryptedFileLength < maxFileLength) {
				int fileReceiveCounter = (int) encryptedFileLength / 16;
				
				for (int i = 0; i < fileReceiveCounter; i++) {
					byte[] tempData = new byte[16];
					input.read(tempData);
					byte[] decryptedDataPortion = cipher.doFinal(tempData);
					fileOutputStream.write(decryptedDataPortion, 0, decryptedDataPortion.length);
					listener.receivingProgress((int) Math.ceil((i / fileReceiveCounter) * 100));
				}
				
				fileOutputStream.flush();
				listener.receivingCompleted(file);
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
			listener.fileShareError();
			file.delete();
		} finally {
			try {
				fileOutputStream.close();
				input.close();
				socket.close();
			} catch (Exception e) {
			}
		}
		
	}
}
