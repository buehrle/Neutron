package com.erdlof.neutron.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class CommunicationUtils {
	public static <T extends Serializable> byte[] serializableObjectToByteArray(T object) {
		try (ByteArrayOutputStream output = new ByteArrayOutputStream(); ObjectOutput obj = new ObjectOutputStream(output)) {
			obj.writeObject(object);
			return output.toByteArray();
		} catch (IOException e) {
		}
		return null;
	}
	
	public static Object byteArrayToObject(byte[] data) {
		System.out.println(data.length);
		try(ByteArrayInputStream input = new ByteArrayInputStream(data); ObjectInput obj = new ObjectInputStream(input)) {
			return obj.readObject();
		} catch (Exception e) {
		}
		return null;
	}
}
