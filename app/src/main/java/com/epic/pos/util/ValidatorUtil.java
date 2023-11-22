package com.epic.pos.util;


public class ValidatorUtil {

	private static ValidatorUtil theInstance = null;

	private ValidatorUtil(){}

	public static synchronized ValidatorUtil getInstance(){
		if(theInstance == null){
			theInstance = new ValidatorUtil();
		}
		return theInstance;
	}

	public String zeroPadString(String value, int units){
		return zeroPadString(value, units, false);
	}

	public String zeroPadString(String value, int units, boolean isRightPadding) {
		try {
			if (value.length() == units) {
				return value;

			} else {
				StringBuffer sb = new StringBuffer();

				for (int i = 0; i < units - value.length(); i++) {
					sb.append("0");
				}
				if (isRightPadding)
					return value + sb;
				else
					return sb + value;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public String anyCharPadString(String value, int units, String character, boolean isRightPadding) {
		try {
			if (value.length() == units) {
				return value;

			} else {
				StringBuffer sb = new StringBuffer();

				for (int i = 0; i < units - value.length(); i++) {
					sb.append(character);
				}
				if (isRightPadding)
					return value + sb;
				else
					return sb + value;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
}
