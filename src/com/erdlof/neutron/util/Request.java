package com.erdlof.neutron.util;

public final class Request {
	public static final int SEND_TEXT = 0;
	public static final int SEND_FILE = 1;
	public static final int KICKED_FROM_SERVER = 2;
	public static final int BANNED_FROM_SERVER = 3;
	public static final int UNEXPECTED_ERROR = 4;
	public static final int ILLEGAL_REQUEST = 5;
	public static final int REGULAR_DISCONNECT = 6;
	public static final int ALIVE = 7;
}
