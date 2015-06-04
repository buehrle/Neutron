package com.erdlof.neutron.filesharing;

import java.io.File;

public interface FileReceivingListener extends FileShareErrorListener {
	public void receivingProgress(int percentage);
	public void receivingCompleted(File file);
}
