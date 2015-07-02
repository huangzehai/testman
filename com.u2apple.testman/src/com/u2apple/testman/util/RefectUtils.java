package com.u2apple.testman.util;

public final class RefectUtils {
	
	private RefectUtils(){
		
	}
	
	public static String setterName(String fieldName){
		return "set"+StringUtils.toCapital(fieldName);
	}

}
