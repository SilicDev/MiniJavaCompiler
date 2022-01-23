package pgdp.minijava;

import pgdp.minijava.ast.SyntaxTreeNode;
import pgdp.minijava.exceptions.IllegalCharacterException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

public class Parser {
    private Parser() {

    }

    public static SyntaxTreeNode parseFromFile(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath, Charset.defaultCharset()))) {
            return parseFromString(br.lines().reduce("", (s, str) -> s.concat(str).concat("\n")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static SyntaxTreeNode parseFromString(String rawCode) {
        try {
            List<Token> tokens = Tokenizer.tokenize(rawCode);
            tokens = tokens.stream().filter(token -> token.getTokenType() != TokenType.COMMENT).toList();
            return parseTokens(tokens);
        } catch (IllegalCharacterException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static SyntaxTreeNode parseTokens(List<Token> tokens) {
        var root = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        var pos = 0;
        while(pos < tokens.size()) {
            pos = parseLine(tokens, pos, root);
        }
        return root;
    }

    public static int parseLine(List<Token> tokens, int pos, SyntaxTreeNode root) {
        Token current = tokens.get(pos);
        if (current.getTokenType() == TokenType.KEYWORD && types.contains(current.getContentAsString())) {
            pos = parseDeclaration(tokens, pos, root);
        } else {
            pos = parseStatement(tokens, pos, root);
        }
        return pos;
    }

    public static int parseDeclaration(List<Token> tokens, int pos, SyntaxTreeNode root) {
        SyntaxTreeNode node = new SyntaxTreeNode(SyntaxTreeNode.Type.DECL, "");
        var type = new SyntaxTreeNode(SyntaxTreeNode.Type.TYPE, tokens.get(pos++).getContentAsString());
        node.addChild(type);
        node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, tokens.get(pos++).getContentAsString()));
        while(!tokens.get(pos).getContentAsString().equals(";")) {
            if(tokens.get(pos).getContentAsString().equals("=")) {
                node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, tokens.get(pos++).getContentAsString()));
                if(type.getValue().equals("boolean")) {
                    pos = parseCondition(tokens, pos, node);
                } else {
                    pos = parseExpression(tokens, pos, node);
                }
                break;
            }
            node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, tokens.get(pos++).getContentAsString()));
            node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, tokens.get(pos++).getContentAsString()));
        }
        if(!tokens.get(pos).getContentAsString().equals(";")) {
            throw new IllegalStateException("Expected semicolon at line " + tokens.get(pos).getLine());
        }
        node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, tokens.get(pos++).getContentAsString()));
        root.addChild(node);
        return pos;
    }

    public static int parseStatement(List<Token> tokens, int pos, SyntaxTreeNode root) {
        SyntaxTreeNode node = new SyntaxTreeNode(SyntaxTreeNode.Type.STMT, "");
        Token current = tokens.get(pos++);
        if(current.getTokenType() == TokenType.IDENTIFIER) {
            pos = parseIdentifierStatement(tokens, pos - 1, node);
        } else if(current.getTokenType() == TokenType.SEPARATOR) {
            if(current.getContentAsString().equals(";")){
                node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, current.getContentAsString()));
            } else if(current.getContentAsString().equals("{")){
                node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, current.getContentAsString()));
                pos = parseLine(tokens, pos, node);
                while(pos < tokens.size() && !(tokens.get(pos).getTokenType() == TokenType.SEPARATOR && tokens.get(pos).getContentAsString().equals("}"))) {
                    pos = parseLine(tokens, pos, node);
                }
                node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, tokens.get(pos++).getContentAsString()));
            }
        } else if(current.getTokenType() == TokenType.KEYWORD) {
            if(!allowedKeywords.contains(current.getContentAsString())) {
                if(current.getContentAsString().equals("_")) {
                    throw new IllegalStateException("\"_\" is not a valid identifier!");
                }
                if(current.getContentAsString().equals("goto")) {
                    throw new UnsupportedOperationException("Java does not support goto operations");
                }
                throw new UnsupportedOperationException("Can't use Java keyword " + current.getContentAsString());
            }
            if(current.getContentAsString().equals("return")) {
                node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, current.getContentAsString()));
                current = tokens.get(pos++);
                if(current.getContentAsString().equals(";")) {
                    node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, current.getContentAsString()));
                    pos++;
                } else {
                    throw new UnsupportedOperationException("Can't return values!");
                }
            }
            if(current.getContentAsString().equals("while")) {
                node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, current.getContentAsString()));
                current = tokens.get(pos++);
                if(current.getContentAsString().equals("(")) {
                    node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, current.getContentAsString()));
                    pos = parseCondition(tokens, pos, node);
                    current = tokens.get(pos);
                    if(current.getContentAsString().equals(")")) {
                        node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, current.getContentAsString()));
                        pos = parseStatement(tokens, pos + 1, node);
                    } else {
                        throw new IllegalStateException("Couldn't close statement brackets (" +current.getLine() +")");
                    }
                } else {
                    throw new IllegalStateException("Couldn't open while brackets (" +current.getLine() +")");
                }
            } else if(current.getContentAsString().equals("if")) {
                node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, current.getContentAsString()));
                current = tokens.get(pos++);
                if(current.getContentAsString().equals("(")) {
                    node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, current.getContentAsString()));
                    pos = parseCondition(tokens, pos, node);
                    current = tokens.get(pos++);
                    if(current.getContentAsString().equals(")")) {
                        node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, current.getContentAsString()));
                        pos = parseStatement(tokens, pos, node);
                        if(pos < tokens.size()) {
                            current = tokens.get(pos);
                            if (current.getContentAsString().equals("else")) {
                                node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, current.getContentAsString()));
                                pos = parseStatement(tokens, pos + 1, node);
                            }
                        }
                    } else {
                        throw new IllegalStateException("Couldn't close statement brackets (" +current.getLine() +")");
                    }
                } else {
                    throw new IllegalStateException("Couldn't open if brackets (" +current.getLine() +")");
                }
            }
        }
        root.addChild(node);
        return pos;
    }

    public static int parseCondition(List<Token> tokens, int pos, SyntaxTreeNode root) {
        var node = new SyntaxTreeNode(SyntaxTreeNode.Type.COND, "");
        Token current = tokens.get(pos++);
        if(current.getTokenType() == TokenType.OPERATOR) {
            if(current.getContentAsString().equals("!")) {
                node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, current.getContentAsString()));
            }
            pos = parseCondition(tokens, pos, node);
        }else if(current.getTokenType() == TokenType.LITERAL) {
            if(current.getContentAsString().equals("true")) {
                node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.BOOL, current.getContentAsString()));
            } else if(current.getContentAsString().equals("false")) {
                node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.BOOL, current.getContentAsString()));
            } else {
                pos = parseExpression(tokens, pos - 1, node);
                current = tokens.get(pos++);
                if(current.getTokenType() == TokenType.OPERATOR && current.getContentAsString().matches("==|!=|>|<|<=|>=")) {
                    node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.COMP, current.getContentAsString()));
                    pos = parseExpression(tokens, pos, node);
                }
            }
        } else if(current.getTokenType() == TokenType.SEPARATOR) {
            if(current.getContentAsString().equals("(")) {
                node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, current.getContentAsString()));
                pos = parseCondition(tokens, pos, node);
                current = tokens.get(pos++);
                if(current.getContentAsString().equals(")")) {
                    node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, current.getContentAsString()));
                } else {
                    throw new IllegalStateException("Couldn't close brackets (" +current.getLine() +")");
                }
            } else {
                throw new IllegalStateException("Couldn't open condition brackets (" +current.getLine() +")");
            }
        } else if(current.getTokenType() == TokenType.IDENTIFIER &&
                (tokens.get(pos).getContentAsString().equals(")") || tokens.get(pos).getContentAsString().matches("&&|&|\\|\\||\\|\\^"))){
            pos = parseExpression(tokens, pos - 1, node);
        } else if(current.getTokenType() == TokenType.IDENTIFIER) {
            pos = parseExpression(tokens, pos - 1, node);
            current = tokens.get(pos++);
            if (current.getTokenType() == TokenType.OPERATOR && current.getContentAsString().matches("==|!=|<|>|<=|>=")) {
                node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.COMP, current.getContentAsString()));
                pos = parseExpression(tokens, pos, node);
            }
        } else {
            throw new IllegalStateException("Unexpected symbol " + current.getContentAsString() + "at line " + current.getLine());
        }
        if(pos < tokens.size()) {
            Token next = tokens.get(pos);
            if (next.getTokenType() == TokenType.OPERATOR && next.getContentAsString().matches("&&|&|\\|\\||\\|\\^")) {
                var temp = new SyntaxTreeNode(SyntaxTreeNode.Type.COND, "");
                for (int i = 0; i < node.getNumberChildren(); i++) {
                    temp.addChild(node.getChild(i));
                }
                node = new SyntaxTreeNode(SyntaxTreeNode.Type.COND, "");
                node.addChild(temp);
                node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, next.getContentAsString()));
                pos++;
                pos = parseCondition(tokens, pos, node);
            }
        }
        root.addChild(node);
        return pos;
    }

    public static int parseIdentifierStatement(List<Token> tokens, int pos, SyntaxTreeNode root) {
        Token current = tokens.get(pos++);
        Token next = tokens.get(pos++);
        if(next.getContentAsString().equals(":")) {
            root.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.LABEL, current.getContentAsString()));
            root.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, next.getContentAsString()));
            pos = parseStatement(tokens, pos, root);
        } else if(next.getContentAsString().equals("(")){
            var temp = new SyntaxTreeNode(SyntaxTreeNode.Type.FUNCCALL, "");
            temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, current.getContentAsString()));
            temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, next.getContentAsString()));
            if(!tokens.get(pos).getContentAsString().equals(")")) {
                pos = parseExpression(tokens, pos, temp);
                while (!tokens.get(pos).getContentAsString().equals(")")) {
                    pos = parseExpression(tokens, pos + 1, temp);
                    temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, tokens.get(pos++).getContentAsString()));
                }
            }
            temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, tokens.get(pos++).getContentAsString()));
            root.addChild(temp);
            root.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, tokens.get(pos++).getContentAsString()));
        } else if(next.getContentAsString().equals("=")) {
            var temp = new SyntaxTreeNode(SyntaxTreeNode.Type.ASS, "");
            temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, current.getContentAsString()));
            // =
            temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, next.getContentAsString()));
            pos = parseExpression(tokens, pos, temp);
            root.addChild(temp);
            // ;
            root.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, tokens.get(pos++).getContentAsString()));
        } else {
            throw new IllegalStateException("Not a statement (" + current.getLine() + ")");
        }
        return pos;
    }

    public static int parseExpression(List<Token> tokens, int pos, SyntaxTreeNode root) {
        SyntaxTreeNode node = new SyntaxTreeNode(SyntaxTreeNode.Type.EXPR, "");
        Token current = tokens.get(pos++);
        Token next = tokens.get(pos);
        if(current.getTokenType() == TokenType.LITERAL) {
            if(!current.getContentAsString().matches("true|false")) {
                if (next.getTokenType() == TokenType.OPERATOR && next.getContentAsString().matches("[+\\-*/%&|^]")) {
                    var temp = new SyntaxTreeNode(SyntaxTreeNode.Type.EXPR, "");
                    temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NUMBER, current.getContentAsString()));
                    node.addChild(temp);
                    node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, next.getContentAsString()));
                    pos++;
                    pos = parseExpression(tokens, pos, node);
                } else {
                    node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NUMBER, current.getContentAsString()));
                }
            } else {
                pos = parseCondition(tokens, pos - 1, node);
            }
        } else if(current.getTokenType() == TokenType.IDENTIFIER) {
            if(tokens.get(pos).getTokenType() == TokenType.SEPARATOR && tokens.get(pos).getContentAsString().equals("(")) {
                var temp = new SyntaxTreeNode(SyntaxTreeNode.Type.FUNCCALL, "");
                temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, current.getContentAsString()));
                temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, tokens.get(pos++).getContentAsString()));
                if(!tokens.get(pos).getContentAsString().equals(")")) {
                    pos = parseExpression(tokens, pos, temp);
                    while (!tokens.get(pos).getContentAsString().equals(")")) {
                        pos = parseExpression(tokens, pos + 1, temp);
                        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, tokens.get(pos++).getContentAsString()));
                    }
                }
                temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, tokens.get(pos++).getContentAsString()));
                node.addChild(temp);
            } else {
                if (next.getTokenType() == TokenType.OPERATOR && next.getContentAsString().matches("[+\\-*/%&|^]")) {
                    var temp = new SyntaxTreeNode(SyntaxTreeNode.Type.EXPR, "");
                    temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, current.getContentAsString()));
                    node.addChild(temp);
                    pos++;
                    node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, next.getContentAsString()));
                    pos = parseExpression(tokens, pos, node);
                } else {
                    node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, current.getContentAsString()));
                }
            }
        } else if(current.getTokenType() == TokenType.SEPARATOR && current.getContentAsString().equals("(")) {
            node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, current.getContentAsString()));
            pos = parseExpression(tokens, pos, node);
            current = tokens.get(pos++);
            if(current.getTokenType() == TokenType.SEPARATOR && current.getContentAsString().equals(")")) {
                node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, current.getContentAsString()));
            } else {
                throw new IllegalStateException("Couldn't close expression brackets (" +current.getLine() +")");
            }
        } else if(current.getTokenType() == TokenType.OPERATOR && current.getContentAsString().equals("-")) {
            if(tokens.get(pos).getTokenType() == TokenType.LITERAL && !tokens.get(pos).getContentAsString().matches("true|false")) {
                node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NUMBER, current.getContentAsString() + tokens.get(pos).getContentAsString()));
                pos++;
            } else {
                node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, current.getContentAsString()));
                pos = parseExpression(tokens, pos, node);
            }
        } else {
            pos = parseExpression(tokens, pos, node);
            if(node.getChild(node.getNumberChildren() - 1).getType() == SyntaxTreeNode.Type.COND) {
                throw new IllegalStateException();
            }
            current = tokens.get(pos++);
            if(current.getTokenType() == TokenType.OPERATOR && current.getContentAsString().matches("[+\\-*/%&|^]")) {
                node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, current.getContentAsString()));
                pos = parseExpression(tokens, pos, node);
                if(node.getChild(node.getNumberChildren() - 1).getType() == SyntaxTreeNode.Type.COND) {
                    throw new IllegalStateException();
                }
            } else if(tokens.get(pos).getTokenType() == TokenType.SEPARATOR && tokens.get(pos).getContentAsString().equals("(")) {
                node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, tokens.get(pos++).getContentAsString()));
                node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, tokens.get(pos++).getContentAsString()));
            }
        }
        root.addChild(node);
        return pos;
    }

    private final static List<String> types = List.of(
            "int",
            "double",
            "boolean",
            "char",
            "byte",
            "short",
            "long",
            "float"
    );

    private static final List<String> allowedKeywords = List.of(
            "boolean",
            "else",
            "for",
            "if",
            "int",
            "return",
            "while"
    );
}
