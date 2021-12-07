package adsen.scarpet.interpreter.parser.util;

import adsen.scarpet.interpreter.parser.Fluff;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class Matrix implements Collection<Double> {
    /**
     * The length of the matrix - number of columns
     */
    public final int M;
    /**
     * The width of the matrix - number for rows
     */
    public final int N;
    private double[][] values;

    public Matrix(int M, int N) {
        this.M = M;
        this.N = N;

        this.values = new double[N][M];
    }

    public Matrix(double[]... values) {
        this.values = values;
        this.M = values[0].length;
        this.N = values.length;
    }

    //Matrix operations
    /**
     * Gives an identity matrix of a given size
     */
    public static Matrix identity(int size) {
        Matrix mat = new Matrix(size, size);
        for (int s = 0; s < size; s++) {
            mat.set(s, s, 1);
        }
        return mat;
    }

    /**
     * Sets a given value at a given position.
     *
     * @return The old value at this position
     * @throws ArithmeticException() if x or y is greater than M or N size of this matrix or negative
     */
    public double set(int x, int y, double newValue) {
        double oldValue = values[y][x];
        values[y][x] = newValue;
        return oldValue;
    }

    /**
     * Returns the value at a given coordinate
     *
     * @return The value at this position
     * @throws ArithmeticException() if x or y is greater than M or N size of this matrix or negative
     */
    public double get(int x, int y) {
        return values[y][x];
    }

    /**
     * Iterates over the whole matrix, at each point giving you the current x and y position (I denote them as m and n)
     * as well as the current value of the matrix at the position. It accepts a return value which will set that number
     * as the matrix's new value at that position.
     * It's a way to lazily avoid the double for loops all the time.
     *
     * @param iteration The function which you define to deal with the parameters.
     */
    public void iterate(Fluff.TriFunction<Integer, Integer, Double, Double> iteration) {
        for (int m = 0; m < M; m++) {
            for (int n = 0; n < N; n++) {
                set(m, n, iteration.apply(m, n, get(m, n)));
            }
        }
    }

    /**
     * Adds the values of another matrix to those of this one, and returns the answer as a new matrix.
     */
    public Matrix add(Matrix other) {
        if (other.M != M || other.N != N) throw new ArithmeticException("Cannot add matrices of uneven sizes");

        Matrix output = new Matrix(values);

        output.iterate((m, n, v) -> v + other.get(m, n));
        return output;
    }

    /**
     * Subtracts the values of another matrix from those of this one, and returns the answer as a new matrix.
     */
    public Matrix subtract(Matrix other) {
        if (other.M != M || other.N != N) throw new ArithmeticException("Cannot add matrices of uneven sizes");

        Matrix output = new Matrix(values);

        output.iterate((m, n, v) -> v - other.get(m, n));
        return output;
    }

    /**
     * Multiplies this matrix with a scalar
     */
    public Matrix multiply(double other) {
        Matrix output = new Matrix(values);
        output.iterate((m, n, v) -> v * other);
        return output;
    }

    /**
     * Matrix multiplication
     */
    public Matrix multiply(Matrix other) {
        if (other.N != M)
            throw new ArithmeticException("When multiplying, the first matrix must have the same number of columns as the second's rows");
        Matrix output = new Matrix(other.M, N);
        output.iterate((m, n, v) -> {
            double ret = 0.0D;
            for (int i = 0; i < M; i++) {
                ret += this.get(i, n) * other.get(m, i);
            }
            return ret;
        });

        return output;
    }

    /**
     * Multiplies with the inverse
     */
    public Matrix divide(Matrix other){
        if (!(isSquare() && other.isSquare() && M==other.M))
            throw new ArithmeticException("When dividing, both matrices must be square matrices of the same dimension");

        return multiply(other.inverse());
    }

    /**
     * https://gist.github.com/hallazzang/4e6abbb05ff2d3e168a87cf10691c4fb
     */
    private static double _determinant(Matrix matrix) {
        if (matrix.M == 1) {
            return matrix.values[0][0];
        } else if (matrix.M == 2) {
            return matrix.values[0][0] * matrix.values[1][1] - matrix.values[0][1] * matrix.values[1][0];
        } else {
            double result = 0.0;

            for (int col = 0; col < matrix.M; ++col) {
                Matrix sub = matrix.subMatrix( 1, col + 1);

                result += (Math.pow(-1, 1 + col + 1) * matrix.values[0][col] * _determinant(sub));
            }

            return result;
        }
    }

    /**
     * https://gist.github.com/hallazzang/4e6abbb05ff2d3e168a87cf10691c4fb
     */
    public Matrix subMatrix(int excludedRow, int excludedCol) {
        Matrix result = new Matrix(N - 1, M - 1);

        for (int row = 0, p = 0; row < N; ++row) {
            if (row != excludedRow - 1) {
                for (int col = 0, q = 0; col < M; ++col) {
                    if (col != excludedCol - 1) {
                        result.values[p][q] = values[row][col];
                        ++q;
                    }
                }
                ++p;
            }
        }

        return result;
    }

    /**
     * https://gist.github.com/hallazzang/4e6abbb05ff2d3e168a87cf10691c4fb
     */
    public double determinant() {
        if (!isSquare()) throw new ArithmeticException("Cannot have determinant of a non-square matrix");
        return _determinant(this);
    }

    /**
     * https://gist.github.com/hallazzang/4e6abbb05ff2d3e168a87cf10691c4fb
     */
    public Matrix inverse() {
        double det = determinant();

        if (det == 0.0) throw new ArithmeticException("Cannot get inverse with 0 determinant");

        Matrix result = new Matrix(M, N);

        for (int m = 0; m < M; ++m) {
            for (int n = 0; n < N; ++n) {
                Matrix sub = subMatrix(m + 1, n + 1);

                result.values[n][m] = (1.0 / det * Math.pow(-1, m + n) * _determinant(sub));
            }
        }

        return result;
    }

    public boolean isSquare() {
        return M == N;
    }

    public Matrix transpose() {
        Matrix result = new Matrix(N, M);

        result.iterate((m, n, v) -> get(n, m));

        return result;
    }
    //Other Java stuff

    @Override
    public String toString() { //todo see if I can make this faster
        StringBuilder sb = new StringBuilder();
        String[][] strVals = new String[N][M];
        StringBuilder strVal;
        int maxStrLength = 0;
        for (int n = 0; n < N; n++) { // using double for-loops to allow modifying variables
            for (int m = 0; m < M; m++) {
                strVal = new StringBuilder(String.valueOf(get(m, n)));
                strVals[n][m] = strVal.toString();
                maxStrLength = Math.max(maxStrLength, strVal.length());
            }
        }
        // Now making all the numbers the same length
        for (int n = 0; n < N; n++) {
            for (int m = 0; m < M; m++) {
                strVal = new StringBuilder(strVals[n][m]);
                for (int i = 0; i < maxStrLength - strVal.length(); i++) {
                    strVal.append("0");
                }
                strVals[n][m] = strVal.toString();
            }
        }
        for (String[] row : strVals) {
            sb.append(Arrays.toString(row)).append('\n');
        }
        return sb.toString();
    }

    @Override
    public int size() {
        return M * N;
    }

    @Override
    public boolean isEmpty() {
        return Arrays.deepEquals(values, new double[M][N]);
    }

    @Override
    public boolean contains(Object o) {
        for (Double d : this) {
            if (d == o) return true;
        }
        return false;
    }

    @Override
    public Iterator<Double> iterator() {
        return new Iterator<Double>() {
            int x = 0;
            int y = 0;

            @Override
            public boolean hasNext() {
                return y != N && x != M;
            }

            @Override
            public Double next() {
                double ret = values[x][y];
                x++;
                if (x == M) {
                    x = 0;
                    y++;
                }
                return ret;
            }
        };
    }

    @Override
    public Object[] toArray() {
        Double[] array = new Double[M * N];
        iterate((m, n, v) -> {
            array[M * n + m] = v;
            return v;
        });
        return array;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        Double[] arrayData = new Double[M * N];
        iterate((m, n, v) -> {
            arrayData[M * n + m] = v;
            return v;
        });
        if (a.length < size())
            // Make a new array of a's runtime type, but my contents:
            //noinspection unchecked
            return (T[]) Arrays.copyOf(arrayData, size(), a.getClass());
        System.arraycopy(arrayData, 0, a, 0, size());
        if (a.length > size())
            a[size()] = null;
        return a;
    }

    @Override
    public boolean add(Double aDouble) {
        throw new UnsupportedOperationException(); //doesn't rly make sense here, use set method
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException(); //doesn't rly make sense here, use set method
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) return false;
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends Double> c) {
        throw new UnsupportedOperationException(); //doesn't rly make sense here, use set method
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException(); //doesn't rly make sense here, use set method
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException(); //doesn't rly make sense here, use set method
    }

    @Override
    public void clear() {
        this.values = new double[M][N];
    }
}
