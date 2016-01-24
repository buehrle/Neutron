package com.erdlof.neutron.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import com.erdlof.neutron.client.SharedAssociation;

public class CommunicationUtils {
	public static <T extends SharedAssociation> ArrayList<SharedAssociation> castToSharedAssociation(ArrayList<T> source) {
		ArrayList<SharedAssociation> destination = new ArrayList<SharedAssociation>();
		
		for (T element : source) {
			destination.add((SharedAssociation) element);
		}
		
		return destination;
	}
	
	public static byte[] serializableObjectToByteArray(Serializable object) {
		try (ByteArrayOutputStream output = new ByteArrayOutputStream(); ObjectOutput obj = new ObjectOutputStream(output)) {
			obj.writeObject(object);
			return output.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Object byteArrayToObject(byte[] data) {
		try(ByteArrayInputStream input = new ByteArrayInputStream(data); ObjectInput obj = new ObjectInputStream(input)) {
			return obj.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
