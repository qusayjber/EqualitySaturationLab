package eqsat.parser;

public class ParseException extends RuntimeException {
    private final int position;
    public ParseException(String message, int position) {
        super(message + " (at position " + position + ")");
        this.position = position;
    }
    public int position() { return position; }
}