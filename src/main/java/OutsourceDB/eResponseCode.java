package outsourceDB;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum eResponseCode {

	SUCCESS(2),
	CLIENT_ERROR(4),
	SERVER_ERROR(5);

	private String m_Status;

	private static final Map<Integer, eResponseCode> lookup = new HashMap<Integer, eResponseCode>();

	static {
		for (eResponseCode RC : EnumSet.allOf(eResponseCode.class))
			lookup.put(RC.getCode(), RC);
	}

	private int code;

	private eResponseCode(int code) {
	      this.code = code / 100;
	 }

	public int getCode() {
		return code;
	}

	public static eResponseCode get(int code) { 
	      return lookup.get(code); 
	}
}
