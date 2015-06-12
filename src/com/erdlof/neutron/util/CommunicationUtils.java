package com.erdlof.neutron.util;

import java.util.ArrayList;
import java.util.List;

public class CommunicationUtils {
	public static <T extends Wrappable> byte[] wrapList(List<T> list) { //requires a class that implements wrappable. to wrap means, to serialize both ID and name of the object.
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

	public static List<UnwrappedObject> unwrapList(byte[] data) {
		if (data.length > 1) {
			String serializedData = new String(data);
			String[] tempUnwrapped = serializedData.split(";");
			
			List<UnwrappedObject> unwrappedElements = new ArrayList<UnwrappedObject>();
			
			for (int i = 0; i < tempUnwrapped.length; i++) {
				String[] splitUnwrappedData = tempUnwrapped[i].split(":");
				
				unwrappedElements.add(new UnwrappedObject(Long.parseLong(splitUnwrappedData[0], 16), splitUnwrappedData[1]));
			}
			
			return unwrappedElements;
		} else {
			return new ArrayList<UnwrappedObject>();
		}
	}
}
