package optic.lua.messages;

@FunctionalInterface
public interface Option<T> {
	Option<String> INDENT = () -> "\t";

	T defaultValue();
}
