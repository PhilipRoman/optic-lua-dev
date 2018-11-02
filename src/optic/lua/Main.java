package optic.lua;

import nl.bigo.luaparser.*;
import optic.lua.ssa.*;
import org.antlr.runtime.*;
import org.antlr.runtime.tree.CommonTree;
import org.slf4j.*;

import java.util.List;
import java.util.stream.*;

public class Main {
	private static final Logger log = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws Exception {
		var codeSource = CodeSource.ofFile("sample.lua");
		log.info("Parsing {}", codeSource);
		var lexer = new Lua52Lexer(codeSource.charStream());
		var parser = new Lua52Parser(new CommonTokenStream(lexer));
		CommonTree ast = parser.parse().getTree();
		log.info("Flattening {}", codeSource);
		SSATranslator ssa = MutableFlattener::flatten;
		List<Step> steps = ssa.translate(ast);
		steps.forEach(s -> print(s, 0));
	}

	private static void print(Step step, int depth) {
		String indent = "    " + Stream.generate(() -> "|   ")
				.limit(depth)
				.collect(Collectors.joining());
		System.err.println(indent + step);
		step.children().forEach(s -> print(s, depth + 1));
	}
}