package com.erdlof.neutron.client;

import com.erdlof.neutron.util.UnwrappedObject;

public class Partner extends UnwrappedObject {
	public Partner(long ID, String name) {
		super(ID, name);
	}
	public Partner(UnwrappedObject origin) {
		super(origin);
	}
	
	@Override
	public String toString() {
		return getName();
	}
}
