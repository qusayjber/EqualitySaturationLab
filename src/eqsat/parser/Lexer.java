package eqsat.parser;

import java.util.ArrayList;
import java.util.List;

public final class Lexer {

    private final String src;
    private int pos;

    public Lexer(String src) { this.src = src == null ? "" : src; }

    public List<Token> tokenize() {
        List<Token> out = new ArrayList<>();
        while (true) {
            skipWhitespace();
            if (pos >= src.length()) { out.add(new Token(TokenType.EOF, "", pos)); return out; }
            int start = pos;
            char c = src.charAt(pos);

            if (Character.isDigit(c)) {
                while (pos < src.length() && Character.isDigit(src.charAt(pos))) pos++;
                out.add(new Token(TokenType.NUMBER, src.substring(start, pos), start));
                continue;
            }
            if (Character.isLetter(c) || c == '_') {
                while (pos < src.length() &&
                        (Character.isLetterOrDigit(src.charAt(pos)) || src.charAt(pos) == '_')) pos++;
                out.add(new Token(TokenType.IDENT, src.substring(start, pos), start));
                continue;
            }
            pos++;
            switch (c) {
                case '+' -> out.add(new Token(TokenType.PLUS, "+", start));
                case '-' -> out.add(new Token(TokenType.MINUS, "-", start));
                case '*' -> out.add(new Token(TokenType.STAR, "*", start));
                case '/' -> out.add(new Token(TokenType.SLASH, "/", start));
                case '^' -> out.add(new Token(TokenType.CARET, "^", start));
                case '(' -> out.add(new Token(TokenType.LPAREN, "(", start));
                case ')' -> out.add(new Token(TokenType.RPAREN, ")", start));
                default -> throw new ParseException("Unknown character '" + c + "'", start);
            }
        }
    }

    private void skipWhitespace() {
        while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) pos++;
    }
}