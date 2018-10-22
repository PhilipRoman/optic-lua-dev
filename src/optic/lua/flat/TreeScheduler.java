package optic.lua.flat;

import org.antlr.runtime.tree.CommonTree;

import java.util.List;

public interface TreeScheduler {
	List<Step> schedule(CommonTree tree);
}
