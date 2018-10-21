package optic.lua;

import nl.bigo.luaparser.*;
import optic.lua.flat.*;
import org.antlr.runtime.*;
import org.antlr.runtime.tree.CommonTree;

import java.util.List;
import java.util.stream.*;

public class Main {
	public static void main(String[] args) throws Exception {
		String fileName = "sample.lua";
		System.out.printf("\nParsing `%s`...\n\n", fileName);
		var lexer = new Lua52Lexer(new ANTLRFileStream(fileName));
		var parser = new Lua52Parser(new CommonTokenStream(lexer));
		CommonTree ast = parser.parse().getTree();
		Flattener flattener = new Flattener();
		List<Step> steps = flattener.flatten(ast);
		steps.forEach(s -> print(s, 0));
	}

	private static void print(Step step, int depth) {
		String indent = Stream.generate(() -> "  ")
				.limit(depth)
				.collect(Collectors.joining());
		System.err.println(indent + step);
		step.children().forEach(s -> print(s, depth + 1));
	}
}