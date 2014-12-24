package com.u2apple.testman.util;

public final class StringUtils {

	private StringUtils() {

	}

	public static String capitalize(String text) {
		return Character.toUpperCase(text.charAt(0)) + text.substring(1);
	}

}
