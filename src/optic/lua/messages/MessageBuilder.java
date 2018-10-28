package optic.lua.messages;

import optic.lua.CodeSource;

public interface MessageBuilder extends Message {
	void setCause(Throwable cause);

	void setLine(int line);

	void setColumn(int column);

	void setSource(CodeSource source);

	void setLevel(Level level);

	void setPhase(Phase phase);
}
