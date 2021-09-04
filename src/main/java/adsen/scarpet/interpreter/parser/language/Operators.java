package adsen.scarpet.interpreter.parser.language;

import adsen.scarpet.interpreter.parser.Context;
import adsen.scarpet.interpreter.parser.Expression;
import adsen.scarpet.interpreter.parser.LazyValue;
import adsen.scarpet.interpreter.parser.exception.InternalExpressionException;
import adsen.scarpet.interpreter.parser.value.FunctionSignatureValue;
import adsen.scarpet.interpreter.parser.value.ListValue;
import adsen.scarpet.interpreter.parser.value.NumericValue;
import adsen.scarpet.interpreter.parser.value.StringValue;
import adsen.scarpet.interpreter.parser.value.Value;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Operators {


    public static final Map<String, Integer> precedence = new HashMap<>() {{
        put("unary+-!", 60);
        put("exponent**", 40);
        put("multiplication*/%", 30);
        put("addition+-", 20);
        put("compare>=><=<", 10);
        put("shift<<>>", 9);
        put("equal==!=", 8);
        put("bit-and&", 7);
        put("bit-xor^", 5);
        put("bit-or|", 4);
        put("and&&", 3);
        put("or||", 2);
        put("def->", 1);
        put("nextop;", 0);
    }};

    public static void apply(Expression expression) {
        expression.addBinaryOperator("+", precedence.get("addition+-"), true, Value::add);
        expression.addBinaryOperator("-", precedence.get("addition+-"), true, Value::subtract);
        expression.addBinaryOperator("*", precedence.get("multiplication*/%"), true, Value::multiply);
        expression.addBinaryOperator("/", precedence.get("multiplication*/%"), true, Value::divide);
        expression.addBinaryOperator("%", precedence.get("multiplication*/%"), true, (v1, v2) ->
                new NumericValue(NumericValue.asNumber(v1).getDouble() % NumericValue.asNumber(v2).getDouble()));
        expression.addBinaryOperator("^", precedence.get("exponent**"), false, (v1, v2) ->
                new NumericValue(Math.pow(NumericValue.asNumber(v1).getDouble(), NumericValue.asNumber(v2).getDouble())));

        expression.addBinaryOperator("<<", precedence.get("shift<<>>"), false, (v1, v2) ->
                new NumericValue(NumericValue.asNumber(v1).getLong() << NumericValue.asNumber(v2).getLong()));
        expression.addBinaryOperator(">>", precedence.get("shift<<>>"), false, (v1, v2) ->
                new NumericValue(NumericValue.asNumber(v1).getLong() >> NumericValue.asNumber(v2).getLong()));

        expression.addBinaryOperator("&", precedence.get("bit-and&"), false, (v1, v2) ->
                new NumericValue(NumericValue.asNumber(v1).getLong() & NumericValue.asNumber(v2).getLong()));
        expression.addBinaryOperator("^", precedence.get("bit-xor^"), false, (v1, v2) ->
                new NumericValue(NumericValue.asNumber(v1).getLong() ^ NumericValue.asNumber(v2).getLong()));
        expression.addBinaryOperator("|", precedence.get("bit-or|"), false, (v1, v2) ->
                new NumericValue(NumericValue.asNumber(v1).getLong() | NumericValue.asNumber(v2).getLong()));

        expression.addLazyBinaryOperator("&&", precedence.get("and&&"), false, (c, t, lv1, lv2) ->
        {
            Value v1 = lv1.evalValue(c, Context.BOOLEAN);
            if (!v1.getBoolean()) return (cc, tt) -> v1;
            Value v2 = lv2.evalValue(c, Context.BOOLEAN);
            return v2.getBoolean() ? ((cc, tt) -> v2) : LazyValue.FALSE;
        });

        expression.addLazyBinaryOperator("||", precedence.get("or||"), false, (c, t, lv1, lv2) ->
        {
            Value v1 = lv1.evalValue(c, Context.BOOLEAN);
            if (v1.getBoolean()) return (cc, tt) -> v1;
            Value v2 = lv2.evalValue(c, Context.BOOLEAN);
            return v2.getBoolean() ? ((cc, tt) -> v2) : LazyValue.FALSE;
        });

        expression.addBinaryOperator("~", precedence.get("compare>=><=<"), true, Value::in);

        expression.addBinaryOperator(">", precedence.get("compare>=><=<"), false, (v1, v2) ->
                v1.compareTo(v2) > 0 ? Value.TRUE : Value.FALSE);
        expression.addBinaryOperator(">=", precedence.get("compare>=><=<"), false, (v1, v2) ->
                v1.compareTo(v2) >= 0 ? Value.TRUE : Value.FALSE);
        expression.addBinaryOperator("<", precedence.get("compare>=><=<"), false, (v1, v2) ->
                v1.compareTo(v2) < 0 ? Value.TRUE : Value.FALSE);
        expression.addBinaryOperator("<=", precedence.get("compare>=><=<"), false, (v1, v2) ->
                v1.compareTo(v2) <= 0 ? Value.TRUE : Value.FALSE);
        expression.addBinaryOperator("==", precedence.get("equal==!="), false, (v1, v2) ->
                v1.equals(v2) ? Value.TRUE : Value.FALSE);
        expression.addBinaryOperator("!=", precedence.get("equal==!="), false, (v1, v2) ->
                v1.equals(v2) ? Value.FALSE : Value.TRUE);

        expression.addLazyBinaryOperator("=", precedence.get("assign=<>"), false, (c, t, lv1, lv2) ->
        {
            Value v1 = lv1.evalValue(c);
            Value v2 = lv2.evalValue(c);
            if (v1 instanceof ListValue.ListConstructorValue && v2 instanceof ListValue) {
                List<Value> ll = ((ListValue) v1).getItems();
                List<Value> rl = ((ListValue) v2).getItems();
                if (ll.size() < rl.size()) throw new InternalExpressionException("Too many values to unpack");
                if (ll.size() > rl.size()) throw new InternalExpressionException("Too few values to unpack");
                for (Value v : ll) v.assertAssignable();
                Iterator<Value> li = ll.iterator();
                Iterator<Value> ri = rl.iterator();
                while (li.hasNext()) {
                    String lname = li.next().getVariable();
                    Value vval = ri.next().reboundedTo(lname);
                    c.setVariable(lname, (cc, tt) -> vval);
                }
                return (cc, tt) -> Value.TRUE;
            }
            v1.assertAssignable();
            String varname = v1.getVariable();
            Value copy = v2.reboundedTo(varname);
            LazyValue boundedLHS = (cc, tt) -> copy;
            c.setVariable(varname, boundedLHS);
            return boundedLHS;
        });

        expression.addLazyBinaryOperator("+=", precedence.get("assign=<>"), false, (c, t, lv1, lv2) ->
        {
            Value v1 = lv1.evalValue(c);
            Value v2 = lv2.evalValue(c);
            if (v1 instanceof ListValue.ListConstructorValue && v2 instanceof ListValue) {
                List<Value> ll = ((ListValue) v1).getItems();
                List<Value> rl = ((ListValue) v2).getItems();
                if (ll.size() < rl.size()) throw new InternalExpressionException("Too many values to unpack");
                if (ll.size() > rl.size()) throw new InternalExpressionException("Too few values to unpack");
                for (Value v : ll) v.assertAssignable();
                Iterator<Value> li = ll.iterator();
                Iterator<Value> ri = rl.iterator();
                while (li.hasNext()) {
                    Value lval = li.next();
                    String lname = lval.getVariable();
                    Value result = lval.add(ri.next()).bindTo(lname);
                    c.setVariable(lname, (cc, tt) -> result);
                }
                return (cc, tt) -> Value.TRUE;
            }
            v1.assertAssignable();
            String varname = v1.getVariable();
            LazyValue boundedLHS;
            if (v1 instanceof ListValue) {
                ((ListValue) v1).append(v2);
                boundedLHS = (cc, tt) -> v1;
            } else {
                Value result = v1.add(v2).bindTo(varname);
                boundedLHS = (cc, tt) -> result;
            }
            c.setVariable(varname, boundedLHS);
            return boundedLHS;
        });

        expression.addLazyBinaryOperator("<>", precedence.get("assign=<>"), false, (c, t, lv1, lv2) ->
        {
            Value v1 = lv1.evalValue(c);
            Value v2 = lv2.evalValue(c);
            if (v1 instanceof ListValue.ListConstructorValue && v2 instanceof ListValue.ListConstructorValue) {
                List<Value> ll = ((ListValue) v1).getItems();
                List<Value> rl = ((ListValue) v2).getItems();
                if (ll.size() < rl.size()) throw new InternalExpressionException("Too many values to unpack");
                if (ll.size() > rl.size()) throw new InternalExpressionException("Too few values to unpack");
                for (Value v : ll) v.assertAssignable();
                for (Value v : rl) v.assertAssignable();
                Iterator<Value> li = ll.iterator();
                Iterator<Value> ri = rl.iterator();
                while (li.hasNext()) {
                    Value lval = li.next();
                    Value rval = ri.next();
                    String lname = lval.getVariable();
                    String rname = rval.getVariable();
                    lval.reboundedTo(rname);
                    rval.reboundedTo(lname);
                    c.setVariable(lname, (cc, tt) -> rval);
                    c.setVariable(rname, (cc, tt) -> lval);
                }
                return (cc, tt) -> Value.TRUE;
            }
            v1.assertAssignable();
            v2.assertAssignable();
            String lvalvar = v1.getVariable();
            String rvalvar = v2.getVariable();
            Value lval = v2.reboundedTo(lvalvar);
            Value rval = v1.reboundedTo(rvalvar);
            c.setVariable(lvalvar, (cc, tt) -> lval);
            c.setVariable(rvalvar, (cc, tt) -> rval);
            return (cc, tt) -> lval;
        });

        expression.addUnaryOperator("-", false, (v) -> new NumericValue(-NumericValue.asNumber(v).getDouble()));

        expression.addUnaryOperator("+", false, (v) -> new NumericValue(NumericValue.asNumber(v).getDouble()));

        expression.addLazyUnaryOperator("!", precedence.get("unary+-!"), false, (c, t, lv) -> lv.evalValue(c, Context.BOOLEAN).getBoolean() ? (cc, tt) -> Value.FALSE : (cc, tt) -> Value.TRUE); // might need context boolean

        expression.addLazyBinaryOperator(";", Operators.precedence.get("nextop;"), true, (c, t, lv1, lv2) ->
        {
            lv1.evalValue(c, Context.VOID);
            return lv2;
        });

        //assigns const procedure to the lhs, returning its previous value
        expression.addLazyBinaryOperatorWithDelegation("->", Operators.precedence.get("def->"), false, (c, type, e, t, lv1, lv2) ->
        {
            Value v1 = lv1.evalValue(c, Context.SIGNATURE);
            if (v1 instanceof FunctionSignatureValue sign) {
                expression.addContextFunction(c, sign.getName(), e, t, sign.getArgs(), sign.getGlobals(), lv2);
            } else {
                v1.assertAssignable();
                c.setVariable(v1.getVariable(), lv2);
            }
            return (cc, tt) -> new StringValue("OK");
        });
    }
}
