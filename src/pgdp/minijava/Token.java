package pgdp.minijava;

public class Token {
    private final TokenType tokenType;
    private final StringBuilder content;
    private final int line;

    public Token(TokenType tokenType, int line) {
        this(tokenType, "", line);
    }

    public Token(TokenType tokenType, String initialText, int line) {
        this.tokenType = tokenType;
        content = new StringBuilder(initialText);
        this.line = line + 1;
    }

    public void addCharacter(char c) {
        content.append(c);
    }

    public void addString(String text) {
        content.append(text);
    }

    public String getContentAsString() {
        return content.toString();
    }

    @Override
    public String toString() {
       return "(" + tokenType +
            ", " + content +
            ')';
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public int getLine() {
        return line;
    }
}
