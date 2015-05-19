package com.erdlof.neutron.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import com.erdlof.neutron.util.RequestedAction;

public class Main {

	public static void main(String[] args) {
		try {
			Socket client = new Socket("localhost", 12345);
			DataInputStream input = new DataInputStream(client.getInputStream());
			DataOutputStream output = new DataOutputStream(client.getOutputStream());
			
			output.writeInt(1);
			output.writeByte(1);
			output.flush();
			
			input.read();
			
			String tempName = "Herbert";
			output.writeInt(tempName.getBytes().length);
			output.write(tempName.getBytes());
			
			while (true) {
				output.write(0);
				String tempMessage = "Hi";
				output.writeInt(tempMessage.getBytes().length);
				output.write(tempMessage.getBytes());
				output.flush();
			}
			
		} catch (Exception e) {
			System.out.println("CLIENT");
			e.printStackTrace();
		}
		
		
	}

}
