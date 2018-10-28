package optic.lua.ssa;

import org.antlr.runtime.tree.CommonTree;

import java.util.List;

public interface SSATranslator {
	List<Step> translate(CommonTree tree);
}
