package com.erdlof.neutron.filesharing;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import com.erdlof.neutron.streams.BetterDataInputStream;

public class FileReceiver extends Thread {
	private Socket socket;
	private byte[] IV;
	private SecretKey key;
	private Cipher cipher;
	private int maxFileLength;
	private FileReceivingListener listener;
	private boolean hasFileSizeRestriction = false;
	private File file;
	private final int bufferSize;
	
	public FileReceiver(Socket socket, byte[] IV, SecretKey key, FileReceivingListener listener, String destination, final int bufferSize, int maxFileLength) throws IOException {
		this(socket, IV, key, listener, destination, bufferSize);
		this.maxFileLength = maxFileLength;
		this.hasFileSizeRestriction = true;
	}
	
	public FileReceiver(Socket socket, byte[] IV, SecretKey key, FileReceivingListener listener, String destination, final int bufferSize) throws IOException {
		this.socket = socket;
		this.IV = IV;
		this.key = key;
		this.listener = listener;
		this.bufferSize = bufferSize;
		file = new File(destination);
		file.getParentFile().mkdirs();
		file.createNewFile();
	}
	
	@Override
	public void run() {
		try (BufferedOutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(file));
				BetterDataInputStream input = new BetterDataInputStream(socket.getInputStream())) {
			
			cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(IV));
			
			int fileLength = input.readInt();
			listener.setFileSize(fileLength);
			
			if (!hasFileSizeRestriction ? true : fileLength < maxFileLength) {
				byte[] tempData = new byte[fileLength];
				
				for (int i = 0; i < fileLength; i++) {
					tempData[i] = input.readByte();
					if (i % bufferSize == 0) listener.receivingProgress(i);
					
					if (listener.isFilesharingCanceled()) throw new FileshareCanceledException();
				}
				
				fileOutputStream.write(cipher.doFinal(tempData));
				fileOutputStream.flush();
				listener.receivingCompleted(file);
			} else {
				throw new Exception();
			}
		} catch (FileshareCanceledException e) {
		} catch (Exception e) {
			e.printStackTrace();
			file.delete();
			listener.fileShareError();
		}
		
	}
}
