package optic.lua.runtime;

public class StandardLibrary {

	private static final DynamicString HASH_STRING = DynamicString.of("#");

	private static final MultiValue[] TYPE_STRING_CACHE = {
			MultiValue.of(DynamicString.of("nil")),
			MultiValue.of(DynamicString.of("number")),
			MultiValue.of(DynamicString.of("function")),
			MultiValue.of(DynamicString.of("string")),
			MultiValue.of(DynamicString.of("bool")),
			MultiValue.of(DynamicString.of("table")),
	};

	public static MultiValue tostring(MultiValue args) {
		if (args.length() == 0) {
			Errors.argument(1, "value");
			return null;
		}
		return MultiValue.of(DynamicString.of(args.select(0).toString()));
	}

	public static MultiValue type(MultiValue args) {
		if (args.length() == 0) {
			Errors.argument(1, "value");
			return null;
		}
		Dynamic x = args.select(0);
		return TYPE_STRING_CACHE[x.type];
	}

	public static MultiValue select(MultiValue args) {
		var symbol = args.select(0);
		if (symbol.equals(HASH_STRING)) {
			return MultiValue.singleInt(args.length() - 1);
		} else if (symbol.type == Dynamic.NUMBER) {
			int n = DynamicOps.toInt(symbol);
			return args.selectFrom(n);
		} else {
			Errors.argument(1, "integer or '#'");
			return null;
		}
	}

	public static MultiValue table_concat(MultiValue args) {
		Dynamic first = args.select(0);
		if (first.type != Dynamic.TABLE) {
			Errors.argument(1, "table");
			return null;
		}
		DynamicTable table = (DynamicTable) first;
		int len = table.length();
		if (len == 0) {
			return MultiValue.of(DynamicString.of(""));
		}
		int size = 0;
		for (int i = 1; i <= len; i++) {
			Dynamic value = table.arrayGet(i);
			switch (value.type) {
				case Dynamic.FUNCTION:
				case Dynamic.BOOL:
				case Dynamic.TABLE:
				case Dynamic.NIL:
					throw new IllegalArgumentException("Invalid value (" + value + ") #" + i);
				case Dynamic.NUMBER:
					size += 8;
					break;
				case Dynamic.STRING:
					size += ((DynamicString) value).value.length();
					break;
			}
		}
		StringBuilder builder = new StringBuilder(size);
		for (int i = 1; i <= len; i++) {
			builder.append(table.arrayGet(i).toString());
		}
		return MultiValue.of(DynamicString.of(builder.toString()));
	}
}
