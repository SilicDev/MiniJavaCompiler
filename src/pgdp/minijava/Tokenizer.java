package pgdp.minijava;

import pgdp.minijava.exceptions.IllegalCharacterException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tokenizer {
    private Tokenizer() {
    }

    public static ArrayList<Token> tokenize(String rawCode) throws IllegalCharacterException {
        var out = new ArrayList<Token>();
        if(rawCode == null) {
            return out;
        }
        String[] lines = rawCode.lines().toList().toArray(new String[0]);
        State state = new State();
        state.inComment = false;
        state.text = "";
        for (int i = 0; i < lines.length; i++) {
            if(!state.inComment && lines[i].trim().startsWith("//")) {
                out.add(new Token(TokenType.COMMENT, lines[i].trim(), i));
            }
            state.isOperator = false;
            state.isNumber = false;
            state.quoteMark = '\0';
            char[] chars = lines[i].toCharArray();
            for (int j = 0; j < chars.length; j++) {
                char c = chars[j];
                if (Character.isWhitespace(c)) {
                    if (!state.text.isBlank()) {
                        if(state.isOperator) {
                            out.add(createOperatorToken(state, state.text, i));
                        } else {
                            out.add(new Token(determineTypeFromText(state.text), state.text, i));
                        }
                        state.isNumber = false;
                        state.text = "";
                    }
                    continue;
                }
                if (!state.inComment && c == '*' && state.text.startsWith("/")) {
                    state.inComment = true;
                    state.text += c;
                    continue;
                }
                if (state.inComment && c == '/' && state.text.endsWith("*")) {
                    state.inComment = false;
                    state.text += c;
                    out.add(new Token(TokenType.COMMENT, state.text, i));
                    state.text = "";
                    continue;
                }
                if (state.isOperator) {
                    if (state.text.length() == 1 && continueOperator(out, state, chars[j], i)) {
                        continue;
                    } else {
                        out.add(createOperatorToken(state, state.text, i));
                    }
                }
                if (state.text.isEmpty()) {
                    if (!Character.isJavaIdentifierStart(c)) {
                        handleNonIdentifier(out, state, i, j, c);
                        continue;
                    }
                    state.text += c;
                } else {
                    if(!Character.isJavaIdentifierPart(c)) {
                        if(!state.isNumber && state.quoteMark == '\0') {
                            out.add(new Token(determineTypeFromText(state.text), state.text, i));
                            state.text = "";
                            handleNonIdentifier(out, state, i, j, c);
                        } else if (state.isNumber) {
                            String strC = "" + c;
                            if(!strC.matches("[0-9]")) {
                                state.isNumber = false;
                                out.add(new Token(TokenType.LITERAL, state.text, i));
                                state.text = "";
                                handleNonIdentifier(out, state, i, j, c);
                            }
                        }
                        continue;
                    }
                    state.text += c;
                }
            }
        }
        return out;
    }

    private static void handleNonIdentifier(ArrayList<Token> out, State state, int i, int j, char c) throws IllegalCharacterException {
        String strC = "" + c;
        if (strC.matches("[.,:;()\\[\\]{}]")) {
            out.add(new Token(TokenType.SEPARATOR, strC, i));
            return;
        }
        if (strC.matches("[<>=!*+%/\\-&|^]")) {
            state.isOperator = true;
            state.text += c;
            return;
        }
        if(state.quoteMark == '\0' && strC.matches("[\"']")) {
            state.quoteMark = c;
            state.text += c;
            return;
        }
        if(state.quoteMark == c) {
            state.quoteMark = '\0';
            state.text += c;
            out.add(new Token(TokenType.LITERAL, state.text, i));
            return;
        }
        if(strC.matches("[0-9]")) {
            state.text += c;
            state.isNumber = true;
            return;
        }
        throw new IllegalCharacterException(c, i, j);
    }

    private static boolean continueOperator(ArrayList<Token> out, State state, char aChar, int i) {
        String strC = "" + aChar;
        if (strC.matches("[<>=!*+%/\\-&|^]")) {
            if (state.text.matches("[&|]")) {
                if (strC.matches("[" + state.text + "=]")) {
                    out.add(createOperatorToken(state, state.text + strC, i));
                    return true;
                }
            } else if(state.text.matches("[+-]")){
                if (strC.matches("[" + state.text + "=]")) {
                    out.add(createOperatorToken(state, state.text + strC, i));
                    return true;
                }
            } else if(state.text.matches("[<>]")){
                if(strC.equals("=")) {
                    out.add(createOperatorToken(state, state.text + strC, i));
                    return true;
                } else {
                    out.add(createOperatorToken(state, state.text, i));
                    return true;
                }
            } else {
                if(strC.equals("=")) {
                    out.add(createOperatorToken(state, state.text + strC, i));
                    return true;
                }
            }
        }
        return false;
    }

    private static Token createOperatorToken(State state, String text, int i) {
        Token token = new Token(TokenType.OPERATOR, text, i);
        state.text = "";
        state.isOperator = false;
        return token;
    }

    private static TokenType determineTypeFromText(String text) {
        if(keywords.contains(text)) {
            return TokenType.KEYWORD;
        }
        String start = text.substring(0, 1);
        if(start.matches("true|false|[0-9]|\"")) {
            return TokenType.LITERAL;
        }
        if(text.matches("[a-zA-Z_$][a-zA-Z0-9_$-]*")) {
            return TokenType.IDENTIFIER;
        }
        throw new IllegalStateException("Couldn't determine TokenType for string: " + text);
    }

    private static final List<String> keywords = List.of(
            "_",
            "abstract",
            "assert",
            "boolean",
            "break",
            "byte",
            "case",
            "catch",
            "char",
            "class",
            "const",
            "continue",
            "default",
            "do",
            "double",
            "else",
            "enum",
            "extends",
            "final",
            "finally",
            "float",
            "for",
            "goto",
            "if",
            "implements",
            "import",
            "instanceof",
            "int",
            "interface",
            "long",
            "native",
            "new",
            "package",
            "private",
            "protected",
            "public",
            "return",
            "short",
            "static",
            "super",
            "switch",
            "synchronized",
            "this",
            "throw",
            "throws",
            "transient",
            "try",
            "void",
            "volatile",
            "while"
    );

    private static class State {
        boolean inComment = false;
        String text = "";
        boolean isOperator = false;
        boolean isNumber = false;
        char quoteMark = '\0';
    }
}
