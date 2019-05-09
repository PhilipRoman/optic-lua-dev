package optic.lua.messages;

import optic.lua.io.CodeSource;

@Deprecated
public final class Context {
	private final Options options;
	private final MessageReporter reporter;

	public Context(Options options, MessageReporter reporter) {
		this.options = options;
		this.reporter = reporter;
	}

	public Options options() {
		return options;
	}

	public MessageReporter reporter() {
		return reporter;
	}

	public Context withPhase(Phase phase) {
		return new Context(options, reporter.withPhase(phase));
	}

	public Context withSource(CodeSource source) {
		return new Context(options, reporter.withSource(source));
	}
}