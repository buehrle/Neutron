package com.erdlof.neutron.util;

import java.util.List;

public class UnwrappedObject implements Wrappable {
	private long ID;
	private String name;
	
	public UnwrappedObject(long ID, String name) {
		this.ID = ID;
		this.name = name;
	}
	
	public UnwrappedObject(UnwrappedObject origin) {
		this.ID = origin.getID();
		this.name = origin.getName();
	}

	@Override
	public long getID() {
		return ID;
	}

	@Override
	public String getName() {
		return name;
	}
	
	public static <T extends UnwrappedObject> T getUnwrappedObjectByID (List<T> list, long ID) {
		for (T element : list) {
			if (element.getID() == ID) return element;
		}
		return null;
	}
}
