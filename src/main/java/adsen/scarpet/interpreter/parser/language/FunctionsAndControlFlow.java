package adsen.scarpet.interpreter.parser.language;

import adsen.scarpet.interpreter.parser.Context;
import adsen.scarpet.interpreter.parser.Expression;
import adsen.scarpet.interpreter.parser.LazyValue;
import adsen.scarpet.interpreter.parser.UserDefinedFunction;
import adsen.scarpet.interpreter.parser.exception.ExitStatement;
import adsen.scarpet.interpreter.parser.exception.InternalExpressionException;
import adsen.scarpet.interpreter.parser.exception.ReturnStatement;
import adsen.scarpet.interpreter.parser.exception.ThrowStatement;
import adsen.scarpet.interpreter.parser.value.FunctionSignatureValue;
import adsen.scarpet.interpreter.parser.value.GlobalValue;
import adsen.scarpet.interpreter.parser.value.Value;

import java.util.ArrayList;
import java.util.List;


/**
 * <h1>User-defined functions and program control flow</h1>
 * <div style="padding-left: 20px; border-radius: 5px 45px; border:1px solid grey;">
 * <h2>Writing programs with more than 1 line</h2>
 * <h3><code>Operator ;</code></h3>
 * <p>To effectively write programs that have more than one line, a programmer needs way to specify a sequence of
 * commands that execute one after another. In <code>scarpet</code> this can be achieved with <code>;</code>. Its an operator,
 * and by separating statements with semicolons. And since whitespaces and <code>$</code> signs are all treats as
 * whitespaces, how you layout your code doesn't matter, as long as it is readable to everyone involved. The
 * usefulness of preceding all the lines of the script with <code>$</code> is explained in the preamble</p>
 * <pre>
 * expr;
 * expr;
 * expr;
 * expr
 * </pre>
 * <p>Notice that the last expression is not followed by a semicolon. Since instruction separation is functional
 * in <code>scarpet</code>, and not barely an instruction delimiter,
 * terminating the code with a dangling operator wouldn't be valid.</p>
 * <p>In general <code>expr; expr; expr; expr</code> is equivalent to
 * <code>(((expr ; expr) ; expr) ; expr)</code>. In case the programmer forgets that it should be warned with a
 * helpful error at compile time.</p>
 * <p>Result of the evaluated expression is the same as the result of the second expression, but first expression is
 * also evaluated for side effects</p>
 * <pre>
 * expr1 ; expr2 =&gt; expr2  // with expr1 as a side effect
 * </pre>
 * <p>All defined functions are compiled, stored persistently, and available globally -
 * accessible to all other scripts. Functions can only be undefined via call to <code>undef('fun')</code>, which
 * would erase global entry for function <code>fun</code>. Since all variables have local scope inside each function,
 * one way to share large objects is via global variables
 * </p>
 * <h2>Global variables</h2>
 * <p>Any variable that is used with a name that starts with <code>'global_'</code> will be stored and accessible globally,
 * not, inside current scope. It will also persist across scripts, so if a procedure needs to use its own construct, it needs to
 * define it, or initialize it explicitly, or undefine it via <code>undef</code></p>
 * <pre>
 * a() -&gt; global_list+=1; global_list = l(1,2,3); a(); a(); global_list  // =&gt; [1,2,3,1,1]
 * </pre>
 * <h3><code>Operator -&gt;</code></h3>
 * <p>To organize code better than a flat sequence of operations, one can define functions. Definition is correct if
 * has the following form</p>
 * <pre>
 *     fun(args, ...) -&gt; expr
 * </pre>
 * <p>Where <code>fun(args, ...)</code> is a function signature indicating function name, number of arguments,
 * and their names, and expr is an expression (can be complex) that is evaluated when <code>fun</code> is called.
 * Names in the signature don't need to be used anywhere else, other occurrences of these names
 * will be masked in this function scope.
 * Function call creates new scope for variables inside <code>expr</code>, so all non-global variables are not
 * visible from the caller scope. All parameters are passed by value to the new scope, including lists</p>
 * <pre>
 * a(lst) -&gt; lst+=1; list = l(1,2,3); a(list); a(list); list  // =&gt; [1,2,3]
 * </pre>
 * <p>In case the inner function wants to operate and modify larger objects, lists from the outer
 * scope, but not global, it needs to use <code>outer</code> function in function signature</p>
 * <h3><code>outer(arg)</code></h3>
 * <p><code>outer</code> function can only be used in the function signature, and it will
 * cause an error everywhere else. It borrows the reference to that variable from the outer scope and allows
 * its modification in the inner scope. Any modification of outer variable will result in change of them in
 * the outer function. In case the variable was not set yet in the outer scope - it will be created. This construct
 * is similar to <code>global</code> scoping from python</p>
 * <pre>
 * a(outer(list)) -&gt; list+=1; list = l(1,2,3); a(); a(); list  // =&gt; [1,2,3,1,1]
 * </pre>
 * <p>The return value of a function is the value of the last expression. This as the same effect as using outer
 * or global lists, but is more expensive</p>
 * <pre>
 * a(lst) -&gt; lst+=1; list = l(1,2,3); list=a(list); list=a(list); list  // =&gt; [1,2,3,1,1]
 * </pre>
 * <p>Ability to combine more statements into one expression, with functions, passing parameters, and global and outer
 * scoping allow to organize even larger scripts</p>
 * <h2>Control flow</h2>
 * <h3><code>return(expr?)</code></h3>
 * <p>Sometimes its convenient to break the organized control flow, or it is not practical to pass
 * the final result value of a function to the last statement, in this case a return statement can be used</p>
 * <p>If no argument is provided - returns null value.</p>
 * <pre>
 * def() -&gt; (
 *  expr1;
 *  expr2;
 *  return(expr3); // function terminates returning expr3
 *  expr4;     // skipped
 *  expr5      // skipped
 * )
 * </pre>
 * <p>In general its cheaper to leave the last expression as a return value, rather than calling returns everywhere,
 * but it would often lead to a messy code.</p>
 * <h3><code>exit(expr?)</code></h3>
 * <p>It terminates entire program passing <code>expr</code> as the result of the program execution, or null if omitted.</p>
 * <h3><code>try(expr, catch_expr(_)?) ... throw(value?)</code></h3>
 * <p><code>try</code> function evaluates expression, and continues further unless <code>throw</code> function is called
 * anywhere inside <code>expr</code>. In that case the <code>catch_expr</code> is evaluates with <code>_</code> set
 * to the argument <code>throw</code> was called with.
 * This mechanic accepts skipping thrown value - it throws null instead, and catch expression - then try returns null as well
 * This mechanism allows to terminate large portion of a convoluted
 * call stack and continue program execution. There is only one level of exceptions currently in carpet, so if the inner
 * function also defines the <code>try</code> catchment area, it will received the exception first, but it can technically
 * rethrow the value its getting for the outer scope. Unhandled throw acts like an exit statement.</p>
 * <h3><code>if(cond, expr, cond?, expr?, ..., default?)</code></h3>
 * <p>If statement is a function that takes a number of conditions that are evaluated one after another and if
 * any of them turns out true, its <code>expr</code> gets returned, otherwise, if all conditions fail, the return value is
 * <code>default</code> expression, or <code>null</code> if default is skipped</p>
 * <p><code>if</code> function is equivalent to <code>if (cond) expr; else if (cond) expr; else default;</code>
 * from Java, just in a functional form </p>
 * </div>
 */
public class FunctionsAndControlFlow {

    public static void apply(Expression expression) {
        // artificial construct to handle user defined functions and function definitions
        expression.addLazyFunction(".", -1, (c, t, lv) -> { // adjust based on c
            String name = lv.get(lv.size() - 1).evalValue(c).getString();
            //lv.remove(lv.size()-1); // ain't gonna cut it // maybe it will because of the eager eval changes
            if (t != Context.SIGNATURE) // just call the function
            {
                if (!c.host.globalFunctions.containsKey(name)) {
                    throw new InternalExpressionException("Function " + name + " is not defined yet");
                }
                List<LazyValue> lvargs = new ArrayList<>(lv.size() - 1);
                for (int i = 0; i < lv.size() - 1; i++) {
                    lvargs.add(lv.get(i));
                }
                UserDefinedFunction acf = c.host.globalFunctions.get(name);
                Value retval = acf.lazyEval(c, t, acf.expression, acf.token, lvargs).evalValue(c);
                return (cc, tt) -> retval; ///!!!! dono might need to store expr and token in statics? (e? t?)
            }

            // gimme signature
            List<String> args = new ArrayList<>();
            List<String> globals = new ArrayList<>();
            for (int i = 0; i < lv.size() - 1; i++) {
                Value v = lv.get(i).evalValue(c, Context.LOCALIZATION);
                if (!v.isBound()) {
                    throw new InternalExpressionException("Only variables can be used in function signature, not  " + v.getString());
                }
                if (v instanceof GlobalValue) {
                    globals.add(v.boundVariable);
                } else {
                    args.add(v.boundVariable);
                }
            }
            Value retval = new FunctionSignatureValue(name, args, globals);
            return (cc, tt) -> retval;
        });
        expression.addLazyFunction("outer", 1, (c, t, lv) -> {
            if (t != Context.LOCALIZATION)
                throw new InternalExpressionException("outer scoping of variables is only possible in function signatures");
            return (cc, tt) -> new GlobalValue(lv.get(0).evalValue(c));
        });

        expression.addFunction("exit", (lv) -> {
            throw new ExitStatement(lv.size() == 0 ? Value.NULL : lv.get(0));
        });
        expression.addFunction("return", (lv) -> {
            throw new ReturnStatement(lv.size() == 0 ? Value.NULL : lv.get(0));
        });
        expression.addFunction("throw", (lv) -> {
            throw new ThrowStatement(lv.size() == 0 ? Value.NULL : lv.get(0));
        });

        expression.addLazyFunction("try", -1, (c, t, lv) ->
        {
            if (lv.size() == 0)
                throw new InternalExpressionException("try needs at least an expression block");
            try {
                Value retval = lv.get(0).evalValue(c, t);
                return (c_, t_) -> retval;
            } catch (ThrowStatement ret) {
                if (lv.size() == 1)
                    return (c_, t_) -> Value.NULL;
                LazyValue __ = c.getVariable("_");
                c.setVariable("_", (__c, __t) -> ret.retval.reboundedTo("_"));
                Value val = lv.get(1).evalValue(c, t);
                c.setVariable("_", __);
                return (c_, t_) -> val;
            }
        });

        // if(cond1, expr1, cond2, expr2, ..., ?default) => value
        expression.addLazyFunction("if", -1, (c, t, lv) ->
        {
            if (lv.size() < 2)
                throw new InternalExpressionException("if statement needs to have at least one condition and one case");
            for (int i = 0; i < lv.size() - 1; i += 2) {
                if (lv.get(i).evalValue(c, Context.BOOLEAN).getBoolean()) {
                    //int iFinal = i;
                    Value ret = lv.get(i + 1).evalValue(c);
                    return (cc, tt) -> ret;
                }
            }
            if (lv.size() % 2 == 1) {
                Value ret = lv.get(lv.size() - 1).evalValue(c);
                return (cc, tt) -> ret;
            }
            return (cc, tt) -> Value.ZERO;
        });
    }

}
