package optic.lua.util;

public final class StringUtils {
	private StringUtils() {
	}

	public static String escape(String str) {
		int size = str.length();
		StringBuilder buffer = new StringBuilder(2 * size);

		for (int i = 0; i < size; ++i) {
			char ch = str.charAt(i);
			if (ch > 4095) {
				buffer.append("\\u").append(Integer.toHexString(ch));
			} else if (ch > 255) {
				buffer.append("\\u0").append(Integer.toHexString(ch));
			} else if (ch > 127) {
				buffer.append("\\u00").append(Integer.toHexString(ch));
			} else if (ch < ' ') {
				switch (ch) {
					case '\b':
						buffer.append('\\');
						buffer.append('b');
						break;
					case '\t':
						buffer.append('\\');
						buffer.append('t');
						break;
					case '\n':
						buffer.append('\\');
						buffer.append('n');
						break;
					case '\u000b':
					default:
						if (ch > 15) {
							buffer.append("\\u00").append(Integer.toHexString(ch));
						} else {
							buffer.append("\\u000").append(Integer.toHexString(ch));
						}
						break;
					case '\f':
						buffer.append('\\');
						buffer.append('f');
						break;
					case '\r':
						buffer.append('\\');
						buffer.append('r');
				}
			} else {
				switch (ch) {
					case '"':
						buffer.append('\\');
						buffer.append('"');
						break;
					case '\'':
						buffer.append('\\');
						buffer.append('\'');
						break;
					case '\\':
						buffer.append('\\');
						buffer.append('\\');
						break;
					default:
						buffer.append(ch);
				}
			}
		}

		return buffer.toString();
	}
}
