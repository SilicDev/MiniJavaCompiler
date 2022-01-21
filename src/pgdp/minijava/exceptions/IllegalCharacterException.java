package pgdp.minijava.exceptions;

public class IllegalCharacterException extends Exception {

    public IllegalCharacterException(char c, int line, int pos) {
        super("Illegal character " + c + " at pos " + pos + " at line " + line);
    }
}
