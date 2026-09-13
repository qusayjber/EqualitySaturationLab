package eqsat.parser;

import eqsat.model.*;

import java.util.List;

/**
 * Recursive-descent parser.
 *
 *   expression     -> additive
 *   additive       -> multiplicative (('+' | '-') multiplicative)*
 *   multiplicative -> unary (('*' | '/') unary)*
 *   unary          -> '-' unary | power
 *   power          -> primary ('^' unary)?          // right associative
 *   primary        -> NUMBER | IDENT | '(' expression ')'
 */
public final class Parser {

    private final List<Token> tokens;
    private int index;

    public Parser(String source) {
        this.tokens = new Lexer(source).tokenize();
        this.index = 0;
    }

    public static Expr parse(String source) { return new Parser(source).parse(); }

    public Expr parse() {
        if (peek().type() == TokenType.EOF)
            throw new ParseException("Empty expression", 0);
        Expr e = parseAdditive();
        if (peek().type() != TokenType.EOF)
            throw new ParseException("Unexpected token '" + peek().text() + "'", peek().position());
        return e;
    }

    // ---------------------------------------------------------------

    private Expr parseAdditive() {
        Expr left = parseMultiplicative();
        while (true) {
            TokenType t = peek().type();
            if (t == TokenType.PLUS)      { advance(); left = new BinaryExpr(Op.ADD, left, parseMultiplicative()); }
            else if (t == TokenType.MINUS){ advance(); left = new BinaryExpr(Op.SUB, left, parseMultiplicative()); }
            else return left;
        }
    }

    private Expr parseMultiplicative() {
        Expr left = parseUnary();
        while (true) {
            TokenType t = peek().type();
            if (t == TokenType.STAR)       { advance(); left = new BinaryExpr(Op.MUL, left, parseUnary()); }
            else if (t == TokenType.SLASH) { advance(); left = new BinaryExpr(Op.DIV, left, parseUnary()); }
            else return left;
        }
    }

    private Expr parseUnary() {
        if (peek().type() == TokenType.MINUS) {
            advance();
            return new UnaryExpr(Op.NEG, parseUnary());
        }
        return parsePower();
    }

    private Expr parsePower() {
        Expr base = parsePrimary();
        if (peek().type() == TokenType.CARET) {
            advance();
            return new BinaryExpr(Op.POW, base, parseUnary());
        }
        return base;
    }

    private Expr parsePrimary() {
        Token t = peek();
        switch (t.type()) {
            case NUMBER -> {
                advance();
                try { return new ConstExpr(Long.parseLong(t.text())); }
                catch (NumberFormatException ex) {
                    throw new ParseException("Invalid number '" + t.text() + "'", t.position());
                }
            }
            case IDENT -> { advance(); return new VarExpr(t.text()); }
            case LPAREN -> {
                advance();
                Expr inner = parseAdditive();
                expect(TokenType.RPAREN, "')'");
                return inner;
            }
            case RPAREN -> throw new ParseException("Unbalanced ')'", t.position());
            case EOF -> throw new ParseException("Unexpected end of expression", t.position());
            default -> throw new ParseException("Unexpected token '" + t.text() + "'", t.position());
        }
    }

    // ---------------------------------------------------------------

    private Token peek() { return tokens.get(index); }

    private void advance() { if (index < tokens.size() - 1) index++; }

    private void expect(TokenType type, String what) {
        if (peek().type() != type)
            throw new ParseException("Expected " + what + " but found '" + peek().text() + "'", peek().position());
        advance();
    }
}