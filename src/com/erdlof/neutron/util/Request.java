package com.erdlof.neutron.util;

public final class Request {
	public static final int SEND_TEXT = 0; // send text.
	public static final int SEND_FILE = 1; // request to upload file.
	public static final int KICKED_FROM_SERVER = 2; // information for the client
	public static final int UNEXPECTED_ERROR = 4; //..
	public static final int ILLEGAL_REQUEST = 5; //..
	public static final int REGULAR_DISCONNECT = 6; //tell the other side that you will disconnect
	public static final int ALIVE = 7; //alive signal from the client to the server
	public static final int CLIENT_DISCONNECT_NOTIFICATION = 8; //inform the clients about one client disconnected
	public static final int CLIENT_CONNECT_NOTIFICATION = 9;
	public static final int ILLEGAL_NAME = 10;
	public static final int SERVER_SHUTDOWN = 11;
	public static final int GET_FILE = 12; //request to download a previously uploaded file
	public static final int NEW_FILE = 13;
	public static final int LEGAL_NAME = 14;
}
