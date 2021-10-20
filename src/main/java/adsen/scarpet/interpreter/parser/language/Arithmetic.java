package adsen.scarpet.interpreter.parser.language;

import adsen.scarpet.interpreter.parser.Context;
import adsen.scarpet.interpreter.parser.Expression;
import adsen.scarpet.interpreter.parser.exception.InternalExpressionException;
import adsen.scarpet.interpreter.parser.value.ListValue;
import adsen.scarpet.interpreter.parser.value.NumericValue;
import adsen.scarpet.interpreter.parser.value.Value;

/**
 * <h1>Arithmetic operations</h1>
 * <div style="padding-left: 20px; border-radius: 5px 45px; border:1px solid grey;">
 * <h2>Basic Arithmetic Functions</h2>
 * <p>There is bunch of them - they require a number and spit out a number,
 * doing what you would expect them to do.</p>
 * <h3><code>fact(n)</code></h3>
 * <p>Factorial of a number, a.k.a <code>n!</code>, just not in <code>scarpet</code>. Gets big... quick...</p>
 * <h3><code>sqrt(n)</code></h3>
 * <p>Square root. For other fancy roots, use <code>^</code>, math and yo noggin. Imagine square roots on a tree...</p>
 * <h3><code>abs(n)</code></h3>
 * <p>Absolut value.</p>
 * <h3><code>round(n)</code></h3>
 * <p>Closest integer value. Did you know the earth is also round?</p>
 * <h3><code>floor(n)</code></h3>
 * <p>Highest integer that is still no larger then <code>n</code>. Insert a floor pun here.</p>
 * <h3><code>ceil(n)</code></h3>
 * <p>First lucky integer that is not smaller than <code>n</code>. As you would expect, ceiling is typically
 * right above the floor.</p>
 * <h3><code>ln(n)</code></h3>
 * <p>Natural logarithm of <code>n</code>. Naturally.</p>
 * <h3><code>ln1p(n)</code></h3>
 * <p>Natural logarithm of <code>n+1</code>. Very optimistic.</p>
 * <h3><code>log10(n)</code></h3>
 * <p>Decimal logarithm of <code>n</code>. Its ceiling is the length of its floor.</p>
 * <h3><code>log(n)</code></h3>
 * <p>Binary logarithm of <code>n</code>. Finally, a proper one, not like the other 100.</p>
 * <h3><code>log1p(n)</code></h3>
 * <p>Binary logarithm of <code>n+1</code>. Also always positive.</p>
 * <h3><code>mandelbrot(a, b, limit)</code></h3>
 * <p>Computes the value of the mandelbrot set, for set <code>a</code> and <code>b</code> spot.
 * Spot the beetle. Why not.</p>
 * <h3><code>min(arg, ...), min(list), max(arg, ...), max(list)</code></h3>
 * <p>Compute minimum or maximum of supplied arguments assuming natural sorting order. In case you are
 * missing <code>argmax</code>, just use <code>a ~ max(a)</code>, little less efficient, but still fun.
 * </p>
 * <p>
 * Interesting bit - <code>min</code> and <code>max</code> don't remove variable associations from arguments, which
 * means can be used as LHS of assignments (obvious case), or argument spec in function definitions (far less obvious).
 * </p>
 * <pre>
 * a = 1; b = 2; min(a,b) = 3; l(a,b)  =&gt; [3, 2]
 * a = 1; b = 2; fun(x, min(a,b)) -&gt; l(a,b); fun(3,5)  =&gt; [5, 0]
 * </pre>
 * <p>Absolutely no idea, how the latter might be useful in practice. But since it compiles, can ship it.</p>
 *
 * <h3><code>relu(n)</code></h3>
 * <p>Linear rectifier of <code>n</code>. 0 below 0, n above. Why not. <code>max(0,n)</code>
 * with less moral repercussions.</p>
 * <h2>Trigonometric / Geometric Functions</h2>
 * <h3><code>sin(x)</code></h3>
 * <h3><code>cos(x)</code></h3>
 * <h3><code>tan(x)</code></h3>
 * <h3><code>asin(x)</code></h3>
 * <h3><code>acos(x)</code></h3>
 * <h3><code>atan(x)</code></h3>
 * <h3><code>atan2(x,y)</code></h3>
 * <h3><code>sinh(x)</code></h3>
 * <h3><code>cosh(x)</code></h3>
 * <h3><code>tanh(x)</code></h3>
 * <h3><code>sec(x)</code></h3>
 * <h3><code>csc(x)</code></h3>
 * <h3><code>sech(x)</code></h3>
 * <h3><code>csch(x)</code></h3>
 * <h3><code>cot(x)</code></h3>
 * <h3><code>acot(x)</code></h3>
 * <h3><code>coth(x)</code></h3>
 * <h3><code>asinh(x)</code></h3>
 * <h3><code>acosh(x)</code></h3>
 * <h3><code>atanh(x)</code></h3>
 * <h3><code>rad(deg)</code></h3>
 * <h3><code>deg(rad)</code></h3>
 * <p>Use as you wish</p>
 *
 * </div>
 */
public class Arithmetic {
    public static void apply(Expression expression) {
        expression.addLazyFunction("not", 1, (c, t, lv) -> lv.get(0).evalValue(c, Context.BOOLEAN).getBoolean() ? ((cc, tt) -> Value.FALSE) : ((cc, tt) -> Value.TRUE));

        expression.addUnaryFunction("fact", (v) ->
        {
            long number = NumericValue.asNumber(v).getLong();
            long factorial = 1;
            for (int i = 1; i <= number; i++) {
                factorial = factorial * i;
            }
            return new NumericValue(factorial);
        });
        expression.addMathematicalUnaryFunction("sin", (d) -> Math.sin(Math.toRadians(d)));
        expression.addMathematicalUnaryFunction("cos", (d) -> Math.cos(Math.toRadians(d)));
        expression.addMathematicalUnaryFunction("tan", (d) -> Math.tan(Math.toRadians(d)));
        expression.addMathematicalUnaryFunction("asin", (d) -> Math.toDegrees(Math.asin(d)));
        expression.addMathematicalUnaryFunction("acos", (d) -> Math.toDegrees(Math.acos(d)));
        expression.addMathematicalUnaryFunction("atan", (d) -> Math.toDegrees(Math.atan(d)));
        expression.addMathematicalBinaryFunction("atan2", (d, d2) -> Math.toDegrees(Math.atan2(d, d2)));
        expression.addMathematicalUnaryFunction("sinh", Math::sinh);
        expression.addMathematicalUnaryFunction("cosh", Math::cosh);
        expression.addMathematicalUnaryFunction("tanh", Math::tanh);
        expression.addMathematicalUnaryFunction("sec", (d) -> 1.0 / Math.cos(Math.toRadians(d))); // Formula: sec(x) = 1 / cos(x)
        expression.addMathematicalUnaryFunction("csc", (d) -> 1.0 / Math.sin(Math.toRadians(d))); // Formula: csc(x) = 1 / sin(x)
        expression.addMathematicalUnaryFunction("sech", (d) -> 1.0 / Math.cosh(d));                // Formula: sech(x) = 1 / cosh(x)
        expression.addMathematicalUnaryFunction("csch", (d) -> 1.0 / Math.sinh(d));                // Formula: csch(x) = 1 / sinh(x)
        expression.addMathematicalUnaryFunction("cot", (d) -> 1.0 / Math.tan(Math.toRadians(d))); // Formula: cot(x) = cos(x) / sin(x) = 1 / tan(x)
        expression.addMathematicalUnaryFunction("acot", (d) -> Math.toDegrees(Math.atan(1.0 / d)));// Formula: acot(x) = atan(1/x)
        expression.addMathematicalUnaryFunction("coth", (d) -> 1.0 / Math.tanh(d));                // Formula: coth(x) = 1 / tanh(x)
        expression.addMathematicalUnaryFunction("asinh", (d) -> Math.log(d + (Math.sqrt(Math.pow(d, 2) + 1))));  // Formula: asinh(x) = ln(x + sqrt(x^2 + 1))
        expression.addMathematicalUnaryFunction("acosh", (d) -> Math.log(d + (Math.sqrt(Math.pow(d, 2) - 1))));  // Formula: acosh(x) = ln(x + sqrt(x^2 - 1))
        expression.addMathematicalUnaryFunction("atanh", (d) -> {                                     // Formula: atanh(x) = 0.5*ln((1 + x)/(1 - x))
            if (Math.abs(d) > 1 || Math.abs(d) == 1)
                throw new InternalExpressionException("Number must be |x| < 1");
            return 0.5 * Math.log((1 + d) / (1 - d));
        });
        expression.addMathematicalUnaryFunction("rad", Math::toRadians);
        expression.addMathematicalUnaryFunction("deg", Math::toDegrees);
        expression.addMathematicalUnaryFunction("ln", Math::log);
        expression.addMathematicalUnaryFunction("ln1p", Math::log1p);
        expression.addMathematicalUnaryFunction("log10", Math::log10);
        expression.addMathematicalUnaryFunction("log", a -> Math.log(a) / Math.log(2));
        expression.addMathematicalUnaryFunction("log1p", x -> Math.log1p(x) / Math.log(2));
        expression.addMathematicalUnaryFunction("sqrt", Math::sqrt);
        expression.addMathematicalUnaryFunction("abs", Math::abs);
        expression.addMathematicalUnaryFunction("round", (d) -> (double) Math.round(d));
        expression.addMathematicalUnaryFunction("floor", Math::floor);
        expression.addMathematicalUnaryFunction("ceil", Math::ceil);

        expression.addLazyFunction("mandelbrot", 3, (c, t, lv) -> {
            double a0 = NumericValue.asNumber(lv.get(0).evalValue(c)).getDouble();
            double b0 = NumericValue.asNumber(lv.get(1).evalValue(c)).getDouble();
            long maxiter = NumericValue.asNumber(lv.get(2).evalValue(c)).getLong();
            double a = 0.0D;
            double b = 0.0D;
            long iter = 0;
            while (a * a + b * b < 4 && iter < maxiter) {
                double temp = a * a - b * b + a0;
                b = 2 * a * b + b0;
                a = temp;
                iter++;
            }
            long iFinal = iter;
            return (cc, tt) -> new NumericValue(iFinal);
        });

        expression.addFunction("max", (lv) ->
        {
            if (lv.size() == 0)
                throw new InternalExpressionException("max() requires at least one parameter");
            Value max = null;
            if (lv.size() == 1 && lv.get(0) instanceof ListValue)
                lv = ((ListValue) lv.get(0)).getItems();
            for (Value parameter : lv) {
                if (max == null || parameter.compareTo(max) > 0) max = parameter;
            }
            return max;
        });

        expression.addFunction("min", (lv) ->
        {
            if (lv.size() == 0)
                throw new InternalExpressionException("min() requires at least one parameter");
            Value min = null;
            if (lv.size() == 1 && lv.get(0) instanceof ListValue)
                lv = ((ListValue) lv.get(0)).getItems();
            for (Value parameter : lv) {
                if (min == null || parameter.compareTo(min) < 0) min = parameter;
            }
            return min;
        });

        expression.addUnaryFunction("relu", (v) -> v.compareTo(Value.ZERO) < 0 ? Value.ZERO : v);
    }
}
