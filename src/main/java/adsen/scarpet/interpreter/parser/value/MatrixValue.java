package adsen.scarpet.interpreter.parser.value;

import adsen.scarpet.interpreter.parser.exception.InternalExpressionException;
import adsen.scarpet.interpreter.parser.util.Matrix;

import java.util.List;

public class MatrixValue extends Value implements ContainerValueInterface {

    private Matrix matrix;

    public MatrixValue(Matrix m) {
        this.matrix = m;
    }

    public MatrixValue(ListValue m) {
        int rows = m.length();
        int columns = m.items.get(0).length();

        double[][] matVals = new double[rows][columns];

        if (m.items.stream().anyMatch(v -> {
            if (!(v instanceof ListValue)) return false;
            if (v.length() != columns) return false;
            List<Value> lv = ((ListValue) v).items;
            //boolean ret = lv.stream().allMatch(mv -> mv instanceof NumericValue);
            for (Value value : lv) {
                if (!(value instanceof NumericValue)) return false;

            }
            return true;
        }))
            throw new InternalExpressionException("A matrix must be defined as a list of list of numbers");
        for (int r = 0; r < m.length(); r++) {
            Value v = m.items.get(r);
            if (!(v instanceof ListValue))
                throw new InternalExpressionException("A matrix must be defined as a list of list of numbers");
            List<Value> lv = ((ListValue) v).items;
            if (lv.size() != columns) throw new InternalExpressionException("Must have even length rows in a matrix");
            for (int c = 0; c < lv.size(); c++) {
                Value mv = lv.get(c);
                if (!(mv instanceof NumericValue))
                    throw new InternalExpressionException("A matrix must be defined as a list of list of numbers");
                matVals[c][r] = ((NumericValue) mv).getDouble();
            }
        }
    }

    @Override
    public String getString() {
        return matrix.toString();
    }

    @Override
    public String getTypeString() {
        return "matrix";
    }

    @Override
    public boolean getBoolean() {
        return matrix.isEmpty();
    }

    @Override
    public Value add(Value o) {
        if (o instanceof MatrixValue) {
            return new MatrixValue(matrix.add(((MatrixValue) o).matrix));
        }
        if (o instanceof ListValue) {//todo check for vectors
            return NULL;
        }
        throw new InternalExpressionException("Cannot add non-matrix or vector value to a matrix");
    }

    @Override
    public Value subtract(Value o) {
        if (o instanceof MatrixValue) {
            return new MatrixValue(matrix.subtract(((MatrixValue) o).matrix));
        }
        if (o instanceof ListValue) {//todo check for vectors
            return NULL;
        }
        throw new InternalExpressionException("Cannot subtract non-matrix or vector value from a matrix");
    }

    @Override
    public Value multiply(Value o) {
        if (o instanceof MatrixValue) {
            return new MatrixValue(matrix.multiply(((MatrixValue) o).matrix));
        }
        if (o instanceof ListValue) {//todo check for vectors
            return NULL;
        }
        if (o instanceof NumericValue) {
            return new MatrixValue(matrix.multiply(((NumericValue) o).getDouble()));
        }
        throw new InternalExpressionException("Cannot multiply non-matrix, vector or scalar value with a matrix");
    }

    @Override
    public Value divide(Value o) {//todo getting inverses to divide a number by this
        throw new InternalExpressionException("Unsupported operation (so far...)");
    }

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
