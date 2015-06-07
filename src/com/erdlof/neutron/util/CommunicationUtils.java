package com.erdlof.neutron.util;

import java.util.List;

public class CommunicationUtils {
	public static <T extends Wrappable> byte[] wrapList(List<T> list) {
		//yeah, that IS embarrassing.
		byte[] tempData;
		
		if (!list.isEmpty()) {
			StringBuilder serializedData = new StringBuilder();
			
			for (T toWrap : list) {
				serializedData.append(Long.toString(toWrap.getID(), 16) + ":" + toWrap.getName() + ";");
			}
			
			tempData = serializedData.toString().getBytes();
		} else {
			tempData = new byte[] {0};
		}
		
		return tempData;
	}
	
	public static UnwrappedObject[] unwrapList(byte[] data) {
		if (data.length > 1) {
			String serializedData = new String(data);
			String[] tempUnwrapped = serializedData.split(";");
			
			UnwrappedObject[] unwrapped = new UnwrappedObject[tempUnwrapped.length];
			
			for (int i = 0; i < tempUnwrapped.length; i++) {
				String[] splitUnwrappedData = tempUnwrapped[i].split(":");
				
				unwrapped[i] = new UnwrappedObject(Long.parseLong(splitUnwrappedData[0], 16), splitUnwrappedData[1]);
			}
			
			return unwrapped;
		} else {
			return new UnwrappedObject[0];
		}
	}
}
