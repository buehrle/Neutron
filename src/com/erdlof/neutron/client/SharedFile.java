package com.erdlof.neutron.client;

import com.erdlof.neutron.util.UnwrappedObject;

public class SharedFile extends UnwrappedObject {
	public SharedFile(long ID, String name) {
		super(ID, name);
	}
	
	public SharedFile(UnwrappedObject origin) {
		super(origin);
	}
	
	@Override
	public String toString() {
		return this.getName();
	}
}
