package com.u2apple.testman.util;

import java.util.regex.Pattern;

public class AndroidDeviceUtils {

	public static String getBrandByProductId(String productId) {
		if (productId == null) {
			return null;
		} else {
			return productId.substring(0, productId.indexOf("-"));
		}
	}
	
	public static String getMethodName(String productId) {
		if (productId == null) {
			throw new IllegalArgumentException("product id is null.");
		} else if (Pattern.matches("^\\d.*$", productId)) {
			return "test_" + productId.replaceAll("\\s", "").replace("-", "_");
		} else {
			return productId.replaceAll("\\s","").replace("-", "_");
		}
	}
	
	public static String createTestFileName(String brand){
		return StringUtils.toCapital(brand) + "DeviceStoreTest.java";
	}
	
	public static String format(String model) {
		return model == null ? model : model.replaceAll("\\+", "plus")
				.replaceAll("[^a-zA-Z0-9]", "");
	}

}
