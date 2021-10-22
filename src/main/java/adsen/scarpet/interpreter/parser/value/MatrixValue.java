package adsen.scarpet.interpreter.parser.value;

import adsen.scarpet.interpreter.parser.exception.InternalExpressionException;
import adsen.scarpet.interpreter.parser.util.Matrix;

import java.util.Iterator;
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
                matVals[r][c] = ((NumericValue) mv).getDouble();
            }
        }
        matrix = new Matrix(matVals);
    }

    public MatrixValue(double[][] mat) {
        this.matrix = new Matrix(mat);
    }

    public Matrix getMatrix() {
        return matrix;
    }

    public int rows() {
        return matrix.N;
    }

    public int columns() {
        return matrix.M;
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
        if (o instanceof ListValue && ((ListValue) o).canBeVector()) {
            return new MatrixValue(matrix.add(((ListValue) o).toVector().matrix));
        }
        throw new InternalExpressionException("Cannot add non-matrix or vector value to a matrix");
    }

    @Override
    public Value subtract(Value o) {
        if (o instanceof MatrixValue) {
            return new MatrixValue(matrix.subtract(((MatrixValue) o).matrix));
        }
        if (o instanceof ListValue && ((ListValue) o).canBeVector()) {
            return new MatrixValue(matrix.subtract(((ListValue) o).toVector().matrix));
        }
        throw new InternalExpressionException("Cannot subtract non-matrix or vector value from a matrix");
    }

    @Override
    public Value multiply(Value o) {
        if (o instanceof MatrixValue) {
            return new MatrixValue(matrix.multiply(((MatrixValue) o).matrix));
        }
        if (o instanceof ListValue && ((ListValue) o).canBeVector()) {
            return new MatrixValue(matrix.multiply(((ListValue) o).toVector().matrix));
        }
        if (o instanceof NumericValue) {
            return new MatrixValue(matrix.multiply(((NumericValue) o).getDouble()));
        }
        throw new InternalExpressionException("Cannot multiply non-matrix, vector or scalar value with a matrix");
    }

    @Override
    public Value divide(Value o) {//todo getting inverses to divide a number by this
        if (o instanceof MatrixValue) {
            return new MatrixValue(matrix.divide(((MatrixValue) o).matrix));
        }
        if (o instanceof ListValue && ((ListValue) o).canBeVector()) {
            return new MatrixValue(matrix.divide(((ListValue) o).toVector().matrix));
        }
        if (o instanceof NumericValue) {
            return new MatrixValue(matrix.multiply(1.0D / ((NumericValue) o).getDouble()));
        }
        throw new InternalExpressionException("Cannot divide non-matrix, vector or scalar value by a matrix");
    }

    @Override
    public boolean equals(Value other) {
        if (!(other instanceof MatrixValue mo)) return false;
        if (mo.rows() != rows() || mo.columns() != columns()) return false;
        Iterator<Double> oit = mo.matrix.iterator();
        for (Double aDouble : matrix)
            if (!aDouble.equals(oit.next())) return false;

        return true;
    }

    @Override
    public boolean put(Value where, Value value) {
        if (!(where instanceof ListValue lWhere) || !(lWhere.length() == 2 && lWhere.canBeVector()))
            throw new InternalExpressionException("Must access a matrix's content with a pair of numeric coordinates");

        if (!(value instanceof NumericValue))
            throw new InternalExpressionException("Matrices must have numeric values");

        double oldValue = matrix.set((int) lWhere.items.get(0).readInteger(), (int) lWhere.items.get(1).readInteger(), value.readNumber());

        return oldValue == value.readNumber();
    }

    @Override
    public Value get(Value where) {
        if (!(where instanceof ListValue lWhere) || !(lWhere.length() == 2 && lWhere.canBeVector()))
            throw new InternalExpressionException("Must access a matrix's content with a pair of numeric coordinates");

        return new NumericValue(matrix.get((int) lWhere.items.get(0).readInteger(), (int) lWhere.items.get(1).readInteger()));
    }

    @Override
    public boolean has(Value where) {
        if (!(where instanceof ListValue lWhere) || !(lWhere.length() == 2 && lWhere.canBeVector()))
            throw new InternalExpressionException("Must access a matrix's content with a pair of numeric coordinates");
        int x = (int) lWhere.items.get(0).readInteger();
        int y = (int) lWhere.items.get(1).readInteger();
        return 0 < x && rows() > x && 0 < y && y > columns();
    }

    @Override
    public boolean delete(Value where) {
        throw new InternalExpressionException("Unsupported operation, cannot delete items from a matrix");
    }
}
