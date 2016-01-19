package com.erdlof.neutron.client;

import java.io.Serializable;

public class SharedAssociation implements Serializable {
	private static final long serialVersionUID = 6958109323575540981L;
	private final long ID;
	private String name;
	
	public SharedAssociation(long ID, String name) {
		this.ID = ID;
		this.name = name;
	}
	
	public long getID() {
		return ID;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public static final SharedAssociation getSharedAssociationByID(SharedAssociation[] list, long ID) {
		for (SharedAssociation item : list) {
			if (item.getID() == ID) return item;
		}
		
		return null;
	}
}