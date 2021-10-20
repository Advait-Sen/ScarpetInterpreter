package adsen.scarpet.interpreter.parser.value;

public class MatrixValue implements ContainerValueInterface {
    @Override
    public boolean put(Value where, Value value) {
        return false;
    }

    @Override
    public Value get(Value where) {
        return null;
    }

    @Override
    public boolean has(Value where) {
        return false;
    }

    @Override
    public boolean delete(Value where) {
        return false;
    }
}
