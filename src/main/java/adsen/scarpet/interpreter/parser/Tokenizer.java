package adsen.scarpet.interpreter.parser;

import adsen.scarpet.interpreter.parser.exception.ExpressionException;
import adsen.scarpet.interpreter.parser.exception.InternalExpressionException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Expression tokenizer that allows to iterate over a {@link String}
 * expression token by token. Blank characters will be skipped.
 */
public class Tokenizer implements Iterator<Tokenizer.Token> {

    /**
     * What character to use for decimal separators.
     */
    private static final char decimalSeparator = '.';
    /**
     * What character to use for minus sign (negative values).
     */
    private static final char minusSign = '-';
    private final boolean comments;
    private final boolean newLinesMarkers;
    /**
     * The original input expression.
     */
    private final String input;
    private final Expression expression;
    /**
     * Actual position in expression string.
     */
    private int pos = 0;
    private int lineNo = 0;
    private int linePos = 0;
    /**
     * The previous token or <code>null</code> if none.
     */
    private Token previousToken;

    Tokenizer(Expression expr, String input, boolean allowComments, boolean allowNewLineMakers) {
        this.input = input;
        this.expression = expr;
        this.comments = allowComments;
        this.newLinesMarkers = allowNewLineMakers;
    }

    public static List<Token> simplePass(String input) {
        Tokenizer tok = new Tokenizer(null, input, false, false);
        List<Token> res = new ArrayList<>();
        while (tok.hasNext()) res.add(tok.next());
        return res;
    }

    private static boolean isSemicolon(Token tok) {
        return (tok.type == Token.TokenType.OPERATOR && tok.surface.equals(";"))
                || (tok.type == Token.TokenType.UNARY_OPERATOR && tok.surface.equals(";u"));
    }

    @Override
    public boolean hasNext() {
        return (pos < input.length());
    }

    /**
     * Peek at the next character, without advancing the iterator.
     *
     * @return The next character or character 0, if at end of string.
     */
    private char peekNextChar() {
        return (pos < (input.length() - 1)) ? input.charAt(pos + 1) : 0;
    }

    private boolean isHexDigit(char ch) {
        return ch == 'x' || ch == 'X' || (ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f')
                || (ch >= 'A' && ch <= 'F');
    }

    public List<Token> postProcess() {
        Iterable<Token> iterable = () -> this;
        List<Token> originalTokens = StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
        List<Token> cleanedTokens = new ArrayList<>();
        Token last = null;
        while (originalTokens.size() > 0) {
            Token current = originalTokens.remove(originalTokens.size() - 1);
            if (current.type == Token.TokenType.MARKER && current.surface.startsWith("//"))// skipping comments
                continue;
            if (!isSemicolon(current) || (last != null && last.type != Token.TokenType.CLOSE_PAREN &&
                    last.type != Token.TokenType.COMMA && !isSemicolon(last))) {
                if (isSemicolon(current)) {
                    current.surface = ";";
                    current.type = Token.TokenType.OPERATOR;
                }
                if (current.type == Token.TokenType.MARKER) {
                    // dealing with tokens in reversed order
                    if ("{".equals(current.surface)) {
                        cleanedTokens.add(current.morphedInto(Token.TokenType.OPEN_PAREN, "("));
                        current.morph(Token.TokenType.FUNCTION, "m");
                    } else if ("[".equals(current.surface)) {
                        cleanedTokens.add(current.morphedInto(Token.TokenType.OPEN_PAREN, "("));
                        current.morph(Token.TokenType.FUNCTION, "l");
                    } else if ("}".equals(current.surface) || "]".equals(current.surface)) {
                        current.morph(Token.TokenType.CLOSE_PAREN, ")");
                    }
                }
                cleanedTokens.add(current);
            }
            if (!(current.type == Token.TokenType.MARKER && current.surface.equals("$")))
                last = current;
        }
        Collections.reverse(cleanedTokens);
        return cleanedTokens;
    }

    @Override
    public Token next() {
        Token token = new Token();

        if (pos >= input.length()) {
            return previousToken = null;
        }
        char ch = input.charAt(pos);
        while (Character.isWhitespace(ch) && pos < input.length()) {
            linePos++;
            if (ch == '\n') {
                lineNo++;
                linePos = 0;
            }
            ch = input.charAt(++pos);
        }
        token.pos = pos;
        token.lineNo = lineNo;
        token.linePos = linePos;

        boolean isHex = false;

        if (Character.isDigit(ch)) {// || (ch == decimalSeparator && Character.isDigit(peekNextChar())))
            // decided to no support this notation to favour element access via . operator

            if (ch == '0' && (peekNextChar() == 'x' || peekNextChar() == 'X'))
                isHex = true;
            while ((isHex && isHexDigit(ch)) ||
                    (Character.isDigit(ch) || ch == decimalSeparator || ch == 'e' || ch == 'E'
                            || (ch == minusSign && token.length() > 0
                            && ('e' == token.charAt(token.length() - 1)
                            || 'E' == token.charAt(token.length() - 1)))
                            || (ch == '+' && token.length() > 0
                            && ('e' == token.charAt(token.length() - 1)
                            || 'E' == token.charAt(token.length() - 1))))
                            && (pos < input.length())) {
                token.append(input.charAt(pos++));
                linePos++;
                ch = pos == input.length() ? 0 : input.charAt(pos);
            }
            token.type = isHex ? Token.TokenType.HEX_LITERAL : Token.TokenType.LITERAL;
        } else if (ch == '\'') {
            pos++;
            linePos++;
            token.type = Token.TokenType.STRING;
            if (pos == input.length() && expression != null)
                throw new ExpressionException(this.expression, token, "Program truncated");
            ch = input.charAt(pos);
            while (ch != '\'') {
                if (ch == '\\') {
                    char nextChar = peekNextChar();
                    if (nextChar == 'n') {
                        token.append('\n');
                    } else if (nextChar == 't') {
                        token.append('\t');
                    } else if (nextChar == 'r') {
                        throw new ExpressionException(this.expression, token,
                                "Carriage return character is not supported");
                        //token.append('\r');
                    } else if (nextChar == '\\' || nextChar == '\'') {
                        token.append(nextChar);
                    } else {
                        pos--;
                        linePos--;
                    }
                    pos += 2;
                    linePos += 2;
                } else {
                    token.append(input.charAt(pos++));
                    linePos++;
                    if (ch == '\n') {
                        lineNo++;
                        linePos = 0;
                    }
                }
                if (pos == input.length() && expression != null)
                    throw new ExpressionException(this.expression, token, "Program truncated");
                ch = input.charAt(pos);
            }
            pos++;
            linePos++;
            token.type = Token.TokenType.STRING;
        } else if (Character.isLetter(ch) || "_".indexOf(ch) >= 0) {
            while ((Character.isLetter(ch) || Character.isDigit(ch) || "_".indexOf(ch) >= 0
                    || token.length() == 0 && "_".indexOf(ch) >= 0) && (pos < input.length())) {
                token.append(input.charAt(pos++));
                linePos++;
                ch = pos == input.length() ? 0 : input.charAt(pos);
            }
            // Remove optional white spaces after function or variable name
            if (Character.isWhitespace(ch)) {
                while (Character.isWhitespace(ch) && pos < input.length()) {
                    ch = input.charAt(pos++);
                    linePos++;
                    if (ch == '\n') {
                        lineNo++;
                        linePos = 0;
                    }
                }
                pos--;
                linePos--;
            }
            token.type = ch == '(' ? Token.TokenType.FUNCTION : Token.TokenType.VARIABLE;
        } else if (ch == '(' || ch == ')' || ch == ',' || ch == '{' || ch == '}' || ch == '[' || ch == ']') {
            switch (ch) {
                case '(':
                    token.type = Token.TokenType.OPEN_PAREN;
                case ')':
                    token.type = Token.TokenType.CLOSE_PAREN;
                case ',':
                    token.type = Token.TokenType.COMMA;
                default:
                    token.type = Token.TokenType.MARKER;
            }
            token.append(ch);
            pos++;
            linePos++;

            if (expression != null && previousToken != null &&
                    previousToken.type == Token.TokenType.OPERATOR &&
                    (ch == ')' || ch == ',' || ch == ']' || ch == '}') &&
                    !previousToken.surface.equalsIgnoreCase(";")
            )
                throw new ExpressionException(this.expression, previousToken,
                        "Can't have operator " + previousToken.surface + " at the end of a subexpression");
        } else {
            StringBuilder greedyMatch = new StringBuilder();
            int initialPos = pos;
            int initialLinePos = linePos;
            ch = input.charAt(pos);
            int validOperatorSeenUntil = -1;
            while (!Character.isLetter(ch) && !Character.isDigit(ch) && "_".indexOf(ch) < 0
                    && !Character.isWhitespace(ch) && ch != '(' && ch != ')' && ch != ','
                    && (pos < input.length())) {
                greedyMatch.append(ch);
                if (comments && "//".equals(greedyMatch.toString())) {

                    while (ch != '\n' && pos < input.length()) {
                        ch = input.charAt(pos++);
                        linePos++;
                        greedyMatch.append(ch);
                    }
                    if (ch == '\n') {
                        lineNo++;
                        linePos = 0;
                    }
                    token.append(greedyMatch.toString());
                    token.type = Token.TokenType.MARKER;
                    return token; // skipping setting previous
                }
                pos++;
                linePos++;
                if (Expression.none.isAnOperator(greedyMatch.toString())) {
                    validOperatorSeenUntil = pos;
                }
                ch = pos == input.length() ? 0 : input.charAt(pos);
            }
            if (newLinesMarkers && "$".equals(greedyMatch.toString())) {
                lineNo++;
                linePos = 0;
                token.type = Token.TokenType.MARKER;
                token.append('$');
                return token; // skipping previous token look back
            }
            if (validOperatorSeenUntil != -1) {
                token.append(input.substring(initialPos, validOperatorSeenUntil));
                pos = validOperatorSeenUntil;
                linePos = initialLinePos + validOperatorSeenUntil - initialPos;
            } else {
                token.append(greedyMatch.toString());
            }

            if (previousToken == null || previousToken.type == Token.TokenType.OPERATOR
                    || previousToken.type == Token.TokenType.OPEN_PAREN || previousToken.type == Token.TokenType.COMMA
                    || (previousToken.type == Token.TokenType.MARKER && (previousToken.surface.equals("{") || previousToken.surface.equals("[")))
            ) {
                token.surface += "u";
                token.type = Token.TokenType.UNARY_OPERATOR;
            } else {
                token.type = Token.TokenType.OPERATOR;
            }
        }
        if (expression != null && previousToken != null &&
                (
                        token.type == Token.TokenType.LITERAL ||
                                token.type == Token.TokenType.HEX_LITERAL ||
                                token.type == Token.TokenType.VARIABLE ||
                                token.type == Token.TokenType.STRING ||
                                (token.type == Token.TokenType.MARKER && (previousToken.surface.equalsIgnoreCase("{") || previousToken.surface.equalsIgnoreCase("["))) ||
                                token.type == Token.TokenType.FUNCTION
                ) && (
                previousToken.type == Token.TokenType.VARIABLE ||
                        previousToken.type == Token.TokenType.FUNCTION ||
                        previousToken.type == Token.TokenType.LITERAL ||
                        previousToken.type == Token.TokenType.CLOSE_PAREN ||
                        (previousToken.type == Token.TokenType.MARKER && (previousToken.surface.equalsIgnoreCase("}") || previousToken.surface.equalsIgnoreCase("]"))) ||
                        previousToken.type == Token.TokenType.HEX_LITERAL ||
                        previousToken.type == Token.TokenType.STRING
        )
        ) {
            throw new ExpressionException(this.expression, previousToken, "'" + token.surface +
                    "' is not allowed after '" + previousToken.surface + "'");
        }
        return previousToken = token;
    }

    @Override
    public void remove() {
        throw new InternalExpressionException("remove() not supported");
    }

    public static class Token {
        public String surface = "";
        public TokenType type;
        public int pos;
        public int linePos;
        public int lineNo;

        public void append(char c) {
            surface += c;
        }

        public void append(String s) {
            surface += s;
        }

        public char charAt(int pos) {
            return surface.charAt(pos);
        }

        public int length() {
            return surface.length();
        }

        public Token morphedInto(TokenType newType, String newSurface) {
            Token created = new Token();
            created.surface = newSurface;
            created.type = newType;
            created.pos = pos;
            created.linePos = linePos;
            created.lineNo = lineNo;
            return created;
        }

        public void morph(TokenType type, String s) {
            this.type = type;
            this.surface = s;
        }

        @Override
        public String toString() {
            return surface;
        }

        enum TokenType {
            VARIABLE, FUNCTION, LITERAL, OPERATOR, UNARY_OPERATOR,
            OPEN_PAREN, COMMA, CLOSE_PAREN, HEX_LITERAL, STRING, MARKER
        }
    }
}
