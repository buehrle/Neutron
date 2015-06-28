package com.erdlof.neutron.client;

public interface FileSelectorListener {
	public void downloadFile(long ID, String name);
	public void uploadFile();
}
