package adsen.scarpet.interpreter.parser;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * sole purpose of this package is to provide public access to package-private methods of Expression and APIExpression
 * classes so they don't leave garbage in javadocs
 */
public class ExpressionInspector {
    private static final HashSet<String> scarpetNativeFunctions;
    private static HashSet<String> APIFunctions;

    static {
        Set<String> allFunctions = (new APIExpression("null")).getExpr().getFunctionNames();
        scarpetNativeFunctions = new HashSet<>(new Expression().getFunctionNames());
        allFunctions.removeIf(s -> !scarpetNativeFunctions.contains(s));
        APIFunctions = new HashSet<>(allFunctions);
    }

    public static List<String> Expression_getExpressionSnippet(Tokenizer.Token token, Expression expr) {
        return Expression.getExpressionSnippet(token, expr);
    }

    public static String Expression_getName(Expression e) {
        return e.getName();
    }

    /**
     * Set of all in-built functions, so mostly maths and loops, but other stuff as well
     */
    public Set<String> scarpetNativeFunctions() {
        return scarpetNativeFunctions;
    }

    /**
     * Functions added externally, as opposed to being in-built with scarpet parser itself
     */
    public Set<String> APIfunctions() {
        return APIFunctions;
    }
}
