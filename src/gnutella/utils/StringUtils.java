package gnutella.utils;

public class StringUtils {
	public static String join(String[] array, String with) {
		StringBuffer buf = new StringBuffer();
		for (String s : array) {
			if (buf.length() > 0) {
				buf.append(with);
			}
			buf.append(s);
		}
		return buf.toString();
	}
}