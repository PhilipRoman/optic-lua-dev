package optic.lua.messages;

import optic.lua.CodeSource;
import org.jetbrains.annotations.NotNull;

public class SimpleMessageFormat implements MessageFormat<String> {
	@NotNull
	@Override
	public String format(Message message) {
		var b = new StringBuilder();
		b.append('[');
		b.append(message.level());
		b.append("] ");
		message.source().map(CodeSource::name).ifPresent(str -> b.append(str).append(' '));
		var line = message.line();
		if (line.isPresent()) {
			b.append(line.getAsInt());
			var column = message.column();
			if (column.isPresent()) {
				b.append(':');
				b.append(column.getAsInt());
			}
		}
		b.append(" - ");
		b.append(message.message());
		message.cause().map(Throwable::getMessage).ifPresent(msg -> b.append(" caused by exception: ").append(msg));
		return b.toString();
	}
}
