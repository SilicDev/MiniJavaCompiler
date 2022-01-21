package pgdp.minijava;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Emitter {
    private Emitter() {

    }
    
    public static String emit(SyntaxTreeNode node) {
        Map<String, Integer> variableMap = generateVariableMap(node, new HashMap<>());
        return emitCode(node, variableMap);
    }

    private static Map<String, Integer> generateVariableMap(SyntaxTreeNode node, Map<String, Integer> variableMap) {
        Map<String, Integer> out = new HashMap<>(variableMap);
        List<Map<String, Integer>> variableMaps = new ArrayList<>();
        int freeID = 0;
        for (int i = 0; i < node.getNumberChildren(); i++) {
            SyntaxTreeNode child = node.getChild(i);
            if(child.getType() == SyntaxTreeNode.Type.DECL) {
                for (int j = 0; j < child.getNumberChildren(); j++) {
                    SyntaxTreeNode n = child.getChild(j);
                    if(n.getType() == SyntaxTreeNode.Type.NAME){
                        if(variableMap.containsKey(n.getValue())) {
                            throw new IllegalStateException(n.getValue() + " has already been defined in this context");
                        } else {
                            out.put(n.getValue(), freeID++);
                        }
                    }
                }
            } else if(child.getType() == SyntaxTreeNode.Type.NAME) {
                if(!(variableMap.containsKey(child.getValue()) || out.containsKey(child.getValue()))) {
                    throw new IllegalStateException(child.getValue() + " hasn't been defined in this context");
                }
            } else {
                variableMaps.add(generateVariableMap(child, out));
            }
        }
        return mergeVariableMaps(out, variableMaps);
    }

    private static Map<String, Integer> mergeVariableMaps(Map<String, Integer> variableMap, List<Map<String, Integer>> list) {
        for (Map<String, Integer> map : list) {
            final int size = variableMap.size();
            map.forEach(((s, integer) -> {if(!variableMap.containsKey(s)) variableMap.put(s, integer + size);} ));
        }
        return variableMap;
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
            case FUNCCALL -> null;
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
}
