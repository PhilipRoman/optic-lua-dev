package optic.lua.messages;

import optic.lua.io.CodeSource;
import org.jetbrains.annotations.*;

public interface MessageBuilder extends Message {
	/**
	 * @return mutable (not necessarily new) message containing the same information as original
	 * @implNote may return the original itself
	 */
	static MessageBuilder from(Message msg) {
		if (msg instanceof MessageBuilder) {
			return (MessageBuilder) msg;
		} else {
			return MessageImpl.copyOf(msg);
		}
	}

	void setCause(@Nullable Throwable cause);

	void setLine(int line);

	void setColumn(int column);

	void setSource(@Nullable CodeSource source);

	void setLevel(Level level);

	void setPhase(Phase phase);

	void addTag(@NotNull Tag tag);

	default void addTags(@NotNull Tag... tags) {
		for (Tag tag : tags) {
			addTag(tag);
		}
	}

	void removeTag(@NotNull Tag tag);

	default void removeTags(@NotNull Tag... tags) {
		for (Tag tag : tags) {
			removeTag(tag);
		}
	}
}
