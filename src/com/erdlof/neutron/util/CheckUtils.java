package com.erdlof.neutron.util;

public class CheckUtils {

	public static boolean isProperNickname(String nickname) {
		return (nickname != null && nickname != "" && nickname.length() >= 6 && nickname.length() <= 12 && !nickname.contains(" ") && !nickname.contains(";") && !nickname.contains(":")); //TODO server policies
	}
}
