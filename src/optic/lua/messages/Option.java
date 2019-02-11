package optic.lua.messages;

@FunctionalInterface
public interface Option<T> {
	T defaultValue();

	Option<String> INDENT = () -> "\t";
}
