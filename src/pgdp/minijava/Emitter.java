package pgdp.minijava;

import java.util.HashMap;
import java.util.Map;

public class Emitter {
    private Emitter() {

    }
    
    public static String emit(SyntaxTreeNode node) {
        return emitCode(node, new HashMap<>());
    }

    private static String emitCode(SyntaxTreeNode node, Map<String, Integer> variableMap) {
        return switch (node.getType()) {
            case PROGRAM -> emitProgram(node, variableMap);
            case DECL -> emitDeclaration(node, variableMap);
            case NAME -> null;
            case NUMBER -> null;
            case BOOL -> null;
            case EXPR -> null;
            case COND -> null;
            case COMP -> null;
            case TYPE -> null;
            case STMT -> null;
            case LABEL -> emitLabel(node, variableMap);
            case SYMBOL -> null;
        };
    }

    private static String emitProgram(SyntaxTreeNode node, Map<String, Integer> variableMap) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < node.getNumberChildren(); i++) {
            SyntaxTreeNode child = node.getChild(i);
            out.append(emitCode(child, variableMap)).append("\n");
        }
        return out.toString();
    }

    private static String emitLabel(SyntaxTreeNode node, Map<String, Integer> variableMap) {
        return node.toString() + ":";
    }

    private static String emitDeclaration(SyntaxTreeNode node, Map<String, Integer> variableMap) {
        return "";
    }

    private static class CodeUnit {

    }
}
