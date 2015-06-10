package com.erdlof.neutron.filesharing;

public interface FileShareListener {
	public void fileShareError();
	public boolean isFilesharingCanceled();
	public void setFileSize(int size);
}
