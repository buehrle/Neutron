package com.erdlof.neutron.filesharing;

public interface FileSendingListener extends FileShareErrorListener {
	public void sendingProgress(int bytesSent);
	public void sendingCompleted();
}
