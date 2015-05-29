package com.erdlof.neutron.client;

public class Partner { //represents the other chat users.
	private String name;
	private long ID;
	
	public Partner(long ID, String name) {
		this.name = name;
		this.ID = ID;
	}
	
	public String getName() {
		return name;
	}
	
	public long getID() {
		return ID;
	}
}
