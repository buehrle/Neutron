package com.erdlof.neutron.client;

import java.io.Serializable;

public class ClientSettings implements Serializable {
	private static final long serialVersionUID = -5317232730567972692L;
	private String defaultNickname;
	private int communicationPort;
	private int fileSharePort;
	private boolean useDefinedKeys;
	private String publicKeyPath;
	private String privateKeyPath;
	
	public String getDefaultNickname() {
		return defaultNickname;
	}
	public void setDefaultNickname(String defaultNickname) {
		this.defaultNickname = defaultNickname;
	}
	public int getCommunicationPort() {
		return communicationPort;
	}
	public void setCommunicationPort(int communicationPort) {
		this.communicationPort = communicationPort;
	}
	public int getFileSharePort() {
		return fileSharePort;
	}
	public void setFileSharePort(int fileSharePort) {
		this.fileSharePort = fileSharePort;
	}
	public boolean isUseDefinedKeys() {
		return useDefinedKeys;
	}
	public void setUseDefinedKeys(boolean useDefinedKeys) {
		this.useDefinedKeys = useDefinedKeys;
	}
	public String getPublicKeyPath() {
		return publicKeyPath;
	}
	public void setPublicKeyPath(String publicKeyPath) {
		this.publicKeyPath = publicKeyPath;
	}
	public String getPrivateKeyPath() {
		return privateKeyPath;
	}
	public void setPrivateKeyPath(String privateKeyPath) {
		this.privateKeyPath = privateKeyPath;
	}
	
	
}
