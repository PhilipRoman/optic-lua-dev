package optic.lua.messages;

@FunctionalInterface
interface Option<T> {
	T defaultValue();

	Option<String> INDENT = () -> "\t";
}
