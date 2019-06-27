package optic.lua.optimization;

import java.util.List;

public class FunctionSignature {
	private final List<StaticType> parameters;
	private final List<StaticType> results;

	public FunctionSignature(List<StaticType> parameters, List<StaticType> results) {
		this.parameters = List.copyOf(parameters);
		this.results = List.copyOf(results);
	}

	public FunctionSignature(List<StaticType> parameters, StaticType result) {
		this.parameters = List.copyOf(parameters);
		this.results = List.of(result);
	}

	public FunctionSignature(StaticType parameter, StaticType result) {
		this.parameters = List.of(parameter);
		this.results = List.of(result);
	}

	public List<StaticType> resultTypes() {
		return results;
	}

	public List<StaticType> parameterTypes() {
		return parameters;
	}

	@Override
	public String toString() {
		return parameters + " -> " + results;
	}
}
