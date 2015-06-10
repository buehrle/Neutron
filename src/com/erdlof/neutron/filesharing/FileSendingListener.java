package com.erdlof.neutron.filesharing;

public interface FileSendingListener extends FileShareListener {
	public void sendingProgress(int bytesSent);
	public void sendingCompleted();
}
