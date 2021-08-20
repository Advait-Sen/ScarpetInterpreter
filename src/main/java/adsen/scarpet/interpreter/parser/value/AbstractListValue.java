package adsen.scarpet.interpreter.parser.value;

import adsen.scarpet.interpreter.parser.exception.InternalExpressionException;

import java.util.Iterator;

public abstract class AbstractListValue extends Value implements Iterable<Value> {

    @Override public abstract Iterator<Value> iterator();
    //public List<Value> unpack() { return Lists.newArrayList(iterator()); }
    public void fatality() { }
    public void append(Value v)
    {
        throw new InternalExpressionException("Cannot append a value to an abstract list");
    }
}
