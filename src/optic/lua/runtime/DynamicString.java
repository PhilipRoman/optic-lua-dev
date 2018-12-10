package optic.lua.runtime;

import java.lang.invoke.*;

final class DynamicString extends Dynamic {
	private static final MethodHandle STR_BYTES;

	static {
		MethodHandle strBytes;
		try {
			strBytes = MethodHandles.lookup().findGetter(String.class, "value", byte[].class);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			System.err.println("Fast string array access not supported");
			try {
				strBytes = MethodHandles.lookup().findVirtual(String.class, "getBytes", MethodType.methodType(byte[].class));
			} catch (NoSuchMethodException | IllegalAccessException e1) {
				throw new AssertionError("You fucked up really bad.");
			}
		}
		STR_BYTES = strBytes;
	}

	//	final byte[] value;
	final String value;

	protected DynamicString(String value) {
		super(Dynamic.STRING);
//		try {
//			this.value = (byte[])STR_BYTES.invokeExact(value);
//		} catch (Throwable throwable) {
//			throw new AssertionError("How did we get here?");
//		}
		this.value = value;
	}
}
