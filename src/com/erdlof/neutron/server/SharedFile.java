package com.erdlof.neutron.server;

import java.io.File;

import com.erdlof.neutron.util.Wrappable;

public class SharedFile implements Wrappable {
	private long ID;
	private File file;
	
	public SharedFile(File file, long ID) {
		this.ID = ID;
		this.file = file;
	}
	
	public long getID() {
		return ID;
	}
	
	public File getFile() {
		return file;
	}

	@Override
	public String getName() {
		return file.getName();
	}
}
