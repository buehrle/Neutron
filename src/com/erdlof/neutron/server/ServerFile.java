package com.erdlof.neutron.server;

import java.io.File;

import com.erdlof.neutron.client.SharedAssociation;

@SuppressWarnings("serial")
public class ServerFile extends SharedAssociation {
	private File file;
	
	public ServerFile(long ID, File file) {
		super(ID, file.getName());
		this.file = file;
	}
	
	public File getFile() {
		return file;
	}
}
