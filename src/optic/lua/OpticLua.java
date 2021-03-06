package optic.lua;

import optic.lua.messages.StandardFlags;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.*;

import java.nio.file.Path;
import java.util.*;

/**
 * This class describes the command line interface as required by "picocli" library.
 */
@Command(description = "Lua to JVM compiler", version = "optic-lua [pre-alpha]")
final class OpticLua {
	@Parameters(paramLabel = "FILE", description = "list of Lua source files to compile")
	Set<Path> sources = new HashSet<>();

	@Nullable
	@Option(names = {"-r", "--run"}, paramLabel = "FILE", description = "Lua source file to execute")
	Path mainSource = null;

	@Option(names = {"-i", "--shell"}, description = "launch interactive Lua shell")
	boolean interactiveShell = false;

	@Option(names = {"-R", "--rt-stats"}, description = "show function call site statistics")
	boolean showRtStats = false;

	@Option(names = {"--dump"}, description = "output generated Java code")
	boolean javaCodeDump = false;

	@Option(names = {"-c", "--class"}, description = "create class files corresponding to sources")
	boolean generateClasses = false;

	@Option(names = {"-F"}, hideParamSyntax = true, paramLabel = "...", description = "set advanced compiler options")
	Map<StandardFlags, Boolean> compilerFlags = new HashMap<>();

	@Option(names = {"-v", "--version"}, versionHelp = true, description = "display version information")
	boolean versionRequested = false;

	@Option(names = {"-h", "--help", "-?"}, usageHelp = true, description = "display a help message")
	boolean helpRequested = false;

	@Option(names = {"-t", "--time"}, description = "show how long script execution takes")
	boolean showTime = false;

	@Option(names = {"-n", "--repeat"}, paramLabel = "NUMBER", description = "execute main file multiple times")
	int nTimes = 1;
}
