package optic.lua.io;

public class FilePathCodec {
	public static String encode(String name) {
		if (name.isEmpty()) {
			throw new IllegalArgumentException("Name must not be empty!");
		}
		var builder = new StringBuilder(name.length());
		char first = name.charAt(0);
		if (('0' <= first && first <= '9')) {
			builder.append("_p");
		}
		for (int i = 0; i < name.length(); i++) {
			encodeChar(builder, name.charAt(i));
		}
		return builder.toString();
	}

	private static void encodeChar(StringBuilder builder, char c) {
		if (('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || ('0' <= c && c <= '9')) {
			builder.append(c);
		} else {
			encodeSpecial(builder, c);
		}
	}

	private static void encodeSpecial(StringBuilder builder, char c) {
		builder.append('_');
		switch (c) {
			case '-':
				builder.append('m');
				break;
			case '_':
				builder.append('u');
				break;
			case '.':
				builder.append('d');
				break;
			case '/':
				builder.append('s');
				break;
			case '\\':
				builder.append('b');
				break;
			default:
				String str = Integer.toString((int) c);
				builder.append(str.length());
				builder.append(str);
		}
	}

	public static String decode(String name) {
		if (name.isEmpty()) {
			throw new IllegalArgumentException("Name must not be empty");
		}
		int start = name.startsWith("_p") ? 2 : 0;
		var builder = new StringBuilder(name.length());
		for (int i = start; i < name.length(); i++) {
			char c = name.charAt(i);
			if (c == '_') {
				char next = name.charAt(++i);
				switch (next) {
					case 'm':
						builder.append('-');
						break;
					case 'u':
						builder.append('_');
						break;
					case 'd':
						builder.append('.');
						break;
					case 's':
						builder.append('/');
						break;
					case 'b':
						builder.append('\\');
						break;
					default:
						int length = next - '0';
						if (length <= 0 || length > 9) {
							throw new IllegalArgumentException(length + ": " + name);
						}
						i++;
						String number = name.substring(i, i + length);
						i += length - 1;
						builder.append((char) Integer.parseInt(number));
				}
			} else {
				builder.append(c);
			}
		}
		return builder.toString();
	}
}
