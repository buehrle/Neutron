package com.erdlof.neutron.client;

import java.io.File;

import javax.swing.ProgressMonitor;

import com.erdlof.neutron.filesharing.FileReceivingListener;
import com.erdlof.neutron.filesharing.FileSendingListener;

public class FileshareIndicatorMonitor extends ProgressMonitor implements FileSendingListener, FileReceivingListener {
	public FileshareIndicatorMonitor(Object message, String note, int min, int max) {
		super(null, message, note, min, max);
	}

	@Override
	public void fileShareError() {
		this.setNote("Filesharing failed");
	}

	@Override
	public void receivingProgress(int bytesReceived) {
		this.setProgress(bytesReceived);
		this.setNote(bytesReceived + " of " + this.getMaximum() + " bytes received.");
	}

	@Override
	public void receivingCompleted(File file) {
		this.setNote("Receiving file completed!");
		
	}

	@Override
	public void sendingProgress(int bytesSent) {
		this.setProgress(bytesSent);
		this.setNote(bytesSent + " of " + this.getMaximum() + " bytes sent.");
	}

	@Override
	public void sendingCompleted() {
		this.setNote("Sending file completed!");
	}

	@Override
	public boolean isFilesharingCanceled() {
		return isCanceled();
	}

	@Override
	public void setFileSize(int size) {
		this.setMaximum(size);
	}
	

}
