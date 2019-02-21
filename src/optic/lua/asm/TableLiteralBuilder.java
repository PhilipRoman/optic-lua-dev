package optic.lua.asm;

import optic.lua.messages.CompilationFailure;
import optic.lua.util.Trees;
import org.antlr.runtime.tree.Tree;

import java.util.*;

import static nl.bigo.luaparser.Lua52Walker.FIELD;

public class TableLiteralBuilder {
	private final LinkedHashMap<RValue, RValue> table = new LinkedHashMap<>(4);
	private final Flattener flattener;
	private final int size;
	private final List<Step> steps = new ArrayList<>(8);
	private int fieldIndex = 0;
	private int arrayFieldIndex = 1;

	public TableLiteralBuilder(Flattener flattener, int numberOfEntries) {
		this.flattener = flattener;
		size = numberOfEntries;
	}

	public void addEntry(Tree tree) throws CompilationFailure {
		var field = Trees.expect(FIELD, tree);
		boolean hasKey = field.getChildCount() == 2;
		if (!(field.getChildCount() == 1 || field.getChildCount() == 2)) {
			throw new AssertionError("Expected 1 or 2 children in " + tree.toStringTree());
		}
		if (hasKey) {
			var key = flattener.flattenExpression(field.getChild(0)).applyTo(steps);
			var value = flattener.flattenExpression(field.getChild(1)).applyTo(steps);
			table.put(key, value);
		} else {
			var key = RValue.number(arrayFieldIndex++);
			var value = flattener.flattenExpression(field.getChild(0)).applyTo(steps);
			boolean isLastField = fieldIndex == size - 1;
			table.put(key, isLastField ? value : value.discardRemaining().applyTo(steps));
		}
		fieldIndex++;
	}

	public LinkedHashMap<RValue, RValue> getTable() {
		return table;
	}

	public List<Step> getSteps() {
		return steps;
	}
}
