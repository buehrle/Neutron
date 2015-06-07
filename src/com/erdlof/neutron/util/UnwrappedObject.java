package com.erdlof.neutron.util;

import java.util.List;

public class UnwrappedObject implements Wrappable {
	private long ID;
	private String name;
	
	public UnwrappedObject(long ID, String name) {
		this.ID = ID;
		this.name = name;
	}

	@Override
	public long getID() {
		return ID;
	}

	@Override
	public String getName() {
		return name;
	}
	
	public static UnwrappedObject getUnwrappedObjectByID(List<UnwrappedObject> list, long ID) {
		for (UnwrappedObject object : list) {
			if (object.getID() == ID) return object;
		}
		return null;
	}

}
