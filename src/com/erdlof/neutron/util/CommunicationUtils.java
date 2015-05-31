package com.erdlof.neutron.util;

import java.util.ArrayList;
import java.util.List;

import com.erdlof.neutron.client.Partner;
import com.erdlof.neutron.server.Client;

public class CommunicationUtils {
	public static byte[] wrapClientData(List<Client> clients) {
		//yeah, that IS embarrassing.
		byte[] tempData;
		
		if (!clients.isEmpty()) {
			StringBuilder serializedData = new StringBuilder();
			
			for (Client client : clients) {
				serializedData.append(Long.toString(client.getClientID(), 16) + ":" + client.getClientName() + ";");
			}
			
			tempData = serializedData.toString().getBytes();
		} else {
			tempData = new byte[] {0};
		}
		
		return tempData;
	}
	
	public static Partner[] unwrapClientData(byte[] data) {
		if (data.length > 1) {
			String serializedData = new String(data);
			String[] tempClients = serializedData.split(";");
			
			Partner[] partners = new Partner[tempClients.length];
			
			for (int i = 0; i < tempClients.length; i++) {
				String[] splitClientData = tempClients[i].split(":");
				
				partners[i] = new Partner(Long.parseLong(splitClientData[0], 16), splitClientData[1]);
			}
			
			return partners;
		} else {
			return new Partner[0];
		}
	}
}
