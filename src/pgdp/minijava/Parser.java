package pgdp.minijava;

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

    private static SyntaxTreeNode parseTokens(List<Token> tokens) {
        var root = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        var pos = 0;
        while(pos < tokens.size()) {
            pos = parseLine(tokens, pos, root);
            pos++;
        }
        return root;
    }

    private static int parseLine(List<Token> tokens, int pos, SyntaxTreeNode root) {
        Token current = tokens.get(pos);
        if (current.getTokenType() == TokenType.KEYWORD && types.contains(current.getContentAsString())) {
            pos = parseDeclaration(tokens, pos, root);
        } else {
            pos = parseStatement(tokens, pos, root);
        }
        return pos;
    }

    private static int parseDeclaration(List<Token> tokens, int pos, SyntaxTreeNode root) {
        SyntaxTreeNode node = new SyntaxTreeNode(SyntaxTreeNode.Type.DECL, "");
        node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.TYPE, tokens.get(pos++).getContentAsString()));
        node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, tokens.get(pos++).getContentAsString()));
        while(!tokens.get(pos).getContentAsString().equals(";")) {
            node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, tokens.get(pos++).getContentAsString()));
            node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, tokens.get(pos++).getContentAsString()));
        }
        node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, tokens.get(pos++).getContentAsString()));
        root.addChild(node);
        return pos;
    }

    private static int parseStatement(List<Token> tokens, int pos, SyntaxTreeNode root) {
        SyntaxTreeNode node = new SyntaxTreeNode(SyntaxTreeNode.Type.STMT, "");
        Token current = tokens.get(pos);
        if(current.getTokenType() == TokenType.IDENTIFIER) {
            pos = parseIdentifierStatement(tokens, pos, node);
        } else if(current.getTokenType() == TokenType.SEPARATOR) {
            if(current.getContentAsString().equals(";")){
                node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, tokens.get(pos).getContentAsString()));
                pos++;
            } else if(current.getContentAsString().equals("{")){
                node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, tokens.get(pos++).getContentAsString()));
                pos = parseLine(tokens, pos, node);
                while(pos < tokens.size() && !(tokens.get(pos).getTokenType() == TokenType.SEPARATOR && tokens.get(pos).getContentAsString().equals("}"))) {
                    pos = parseLine(tokens, pos, node);
                }
                node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, tokens.get(pos++).getContentAsString()));
            }
        } else if(current.getTokenType() == TokenType.KEYWORD) {
            if(current.getContentAsString().equals("while")) {
                pos++;
                node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, current.getContentAsString()));
                current = tokens.get(pos++);
                if(current.getContentAsString().equals("(")) {
                    node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, current.getContentAsString()));
                    pos = parseCondition(tokens, pos, node) + 1;
                    current = tokens.get(pos++);
                    if(current.getContentAsString().equals(")")) {
                        node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, current.getContentAsString()));
                        pos = parseStatement(tokens, pos, node);
                    } else {
                        throw new IllegalStateException("Couldn't close statement brackets (" +current.getLine() +")");
                    }
                } else {
                    throw new IllegalStateException("Couldn't open while brackets (" +current.getLine() +")");
                }
            } else if(current.getContentAsString().equals("if")) {
                pos++;
                node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, current.getContentAsString()));
                current = tokens.get(pos++);
                if(current.getContentAsString().equals("(")) {
                    node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, current.getContentAsString()));
                    pos = parseCondition(tokens, pos, node) + 1;
                    current = tokens.get(pos++);
                    if(current.getContentAsString().equals(")")) {
                        node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, current.getContentAsString()));
                        pos = parseStatement(tokens, pos, node);
                        current = tokens.get(pos);
                        if(current.getContentAsString().equals("else")) {
                            pos++;
                            node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, current.getContentAsString()));
                            pos = parseStatement(tokens, pos, node);
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

    private static int parseCondition(List<Token> tokens, int pos, SyntaxTreeNode root) {
        var node = new SyntaxTreeNode(SyntaxTreeNode.Type.COND, "");
        Token current = tokens.get(pos++);
        if(current.getTokenType() == TokenType.LITERAL) {
            if(current.getContentAsString().equals("true")) {
                node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.BOOL, current.getContentAsString()));
            } else if(current.getContentAsString().equals("false")) {
                node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.BOOL, current.getContentAsString()));
            } else {
                pos = parseExpression(tokens, pos - 1, node) + 1;
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
        } else {
            var oldPos = pos;
            try {
                pos = parseExpression(tokens, pos - 1, node);
                pos++;
                current = tokens.get(pos++);
                if (current.getTokenType() == TokenType.OPERATOR && current.getContentAsString().matches("==|!=|<|>|<=|>=")) {
                    node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.COMP, current.getContentAsString()));
                    pos = parseExpression(tokens, pos, node);
                } else {
                    pos = oldPos;
                    pos = parseCondition(tokens, pos, node);
                    if (current.getTokenType() == TokenType.OPERATOR && current.getContentAsString().matches("&&|&|\\|\\||\\|\\^")) {
                        node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, current.getContentAsString()));
                        pos = parseCondition(tokens, pos, node);
                    }
                }
            }catch (IllegalStateException e) {
                pos = oldPos;
                pos = parseCondition(tokens, pos, node);
                if (current.getTokenType() == TokenType.OPERATOR && current.getContentAsString().matches("&&|&|\\|\\||\\|\\^")) {
                    node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, current.getContentAsString()));
                    pos = parseCondition(tokens, pos, node);
                }
            }
        }
        root.addChild(node);
        return pos;
    }

    private static int parseIdentifierStatement(List<Token> tokens, int pos, SyntaxTreeNode root) {
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
                pos = parseExpression(tokens, pos, temp) + 1;
                while (!tokens.get(pos).getContentAsString().equals(")")) {
                    pos = parseExpression(tokens, pos, temp) + 1;
                    temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, tokens.get(pos++).getContentAsString()));
                }
            }
            temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, tokens.get(pos++).getContentAsString()));
            root.addChild(temp);
        } else {
            var temp = new SyntaxTreeNode(SyntaxTreeNode.Type.ASS, "");
            temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, current.getContentAsString()));
            // =
            temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, next.getContentAsString()));
            pos = parseExpression(tokens, pos, temp) + 1;
            // ;
            temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, tokens.get(pos++).getContentAsString()));
            root.addChild(temp);
        }
        return pos;
    }

    private static int parseExpression(List<Token> tokens, int pos, SyntaxTreeNode root) {
        SyntaxTreeNode node = new SyntaxTreeNode(SyntaxTreeNode.Type.EXPR, "");
        Token current = tokens.get(pos);
        if(current.getTokenType() == TokenType.LITERAL && !current.getContentAsString().matches("true|false")) {
            Token next = tokens.get(pos + 1);
            if (next.getTokenType() == TokenType.OPERATOR && next.getContentAsString().matches("[+\\-*/%&|^]")) {
                var temp = new SyntaxTreeNode(SyntaxTreeNode.Type.EXPR, "");
                temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NUMBER, current.getContentAsString()));
                node.addChild(temp);
                pos += 2;
                node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, next.getContentAsString()));
                pos = parseExpression(tokens, pos, node);
            } else {
                node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NUMBER, current.getContentAsString()));
            }
        } else if(current.getTokenType() == TokenType.IDENTIFIER) {
            if(tokens.get(pos + 1).getTokenType() == TokenType.SEPARATOR && tokens.get(pos + 1).getContentAsString().equals("(")) {
                var temp = new SyntaxTreeNode(SyntaxTreeNode.Type.FUNCCALL, "");
                temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, current.getContentAsString()));
                pos++;
                temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, tokens.get(pos).getContentAsString()));
                pos++;
                temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, tokens.get(pos).getContentAsString()));
                node.addChild(temp);
            } else {
                Token next = tokens.get(pos + 1);
                if (next.getTokenType() == TokenType.OPERATOR && next.getContentAsString().matches("[+\\-*/%&|^]")) {
                    var temp = new SyntaxTreeNode(SyntaxTreeNode.Type.EXPR, "");
                    temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, current.getContentAsString()));
                    node.addChild(temp);
                    pos += 2;
                    node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, next.getContentAsString()));
                    pos = parseExpression(tokens, pos, node);
                } else {
                    node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, current.getContentAsString()));
                }
            }
        } else if(current.getTokenType() == TokenType.SEPARATOR && current.getContentAsString().equals("(")) {
            node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, current.getContentAsString()));
            pos = parseExpression(tokens, pos + 1, node);
            current = tokens.get(pos + 1);
            if(current.getTokenType() == TokenType.SEPARATOR && current.getContentAsString().equals(")")) {
                node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, current.getContentAsString()));
            } else {
                throw new IllegalStateException("Couldn't close expression brackets (" +current.getLine() +")");
            }
        } else if(current.getTokenType() == TokenType.OPERATOR && current.getContentAsString().equals("-")) {
            node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, current.getContentAsString()));
            pos++;
            pos = parseExpression(tokens, pos, node);
        } else {
            pos = parseExpression(tokens, pos + 1, node);
            pos++;
            current = tokens.get(pos++);
            if(current.getTokenType() == TokenType.OPERATOR && current.getContentAsString().matches("[+\\-*/%&|^]")) {
                node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, current.getContentAsString()));
                pos = parseExpression(tokens, pos, node);
            } else if(tokens.get(pos + 1).getTokenType() == TokenType.SEPARATOR && tokens.get(pos + 1).getContentAsString().equals("(")) {
                pos++;
                node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, tokens.get(pos).getContentAsString()));
                pos++;
                node.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, tokens.get(pos).getContentAsString()));
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
}
