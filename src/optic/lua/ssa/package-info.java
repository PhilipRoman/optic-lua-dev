/**
 * SSA (single static assignment) is an intermediate representation where all variables
 * are only assigned once and all variables are known ahead of time. Furthermore, all
 * operations are split into basic {@link optic.lua.ssa.Step steps}. Example:
 * <pre> int foo = (3 + 5) / 2 </pre>
 * would be translated to:
 * <pre>
 * a = const 3
 * b = const 5
 * c = add a b
 * d = const 2
 * e = div c d
 * assign foo e </pre>
 * Note that assignment to <code>foo</code> is a higher-level operation instead of being
 * treated like a SSA assignment. Local variables do <strong>not</strong> correspond to
 * registers. Variables are resolved as local or global in later stages of compilation.
 *
 * @see optic.lua.ssa.Register
 * @see optic.lua.ssa.SSATranslator
 */
package optic.lua.ssa;