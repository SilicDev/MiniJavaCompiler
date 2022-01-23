package pgdp.minijava;

import pgdp.minijava.ast.SyntaxTreeNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Emitter {
    private static int whileLoops = 0;
    private static int ifStatements = 0;

    private Emitter() {

    }
    
    public static String emit(SyntaxTreeNode node) {
        Map<String, Integer> variableMap = generateVariableMap(node, new HashMap<>());
        whileLoops = 0;
        ifStatements = 0;
        var temp = emitCode(node, variableMap).trim();
        if(!temp.endsWith("\nHALT")) {
            temp += "\nHALT";
        }
        temp = cleanUpResult(temp).trim();
        return temp;
    }

    private static String cleanUpResult(String in) {
        ArrayList<String> lines = new ArrayList<>(in.lines().toList());
        ArrayList<String> newLines = new ArrayList<>();
        if(lines.size() != 0) {
            newLines.add(lines.get(0));
        }
        for (int i = 1; i < lines.size(); i++) {
            var current = lines.get(i);
            var prev = lines.get(i - 1);
            if(current.startsWith("ALLOC") && prev.startsWith("ALLOC")) {
                newLines.remove(prev);
                newLines.add("ALLOC " + (Integer.parseInt(current.substring(6)) + Integer.parseInt(prev.substring(6))));
            }else if(current.endsWith(":") && prev.endsWith(":")){
                String prevLabel = prev.substring(0, prev.length() - 1);
                String currentLabel = current.substring(0, current.length() - 1);
                for (int j = 0; j < lines.size(); j++) {
                    lines.set(j, lines.get(j).replace(prevLabel, currentLabel));
                }
                newLines.remove(prev);
                for (int j = 0; j < newLines.size(); j++) {
                    newLines.set(j, newLines.get(j).replace(prevLabel, currentLabel));
                }
                newLines.add(current);
            } else {
                newLines.add(current);
            }
        }
        return newLines.stream().reduce("", (s, s2) -> s + "\n" + s2);
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
            } else if(child.getType() == SyntaxTreeNode.Type.NAME && (i != 1 && node.getType() != SyntaxTreeNode.Type.FUNCCALL)) {
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
            //case BOOL -> null;
            //case NAME -> null;
            //case NUMBER -> null;
            //case TYPE -> null;
            //case EXPR -> emitExpression(node, variableMap);
            //case COND -> null;
            //case COMP -> null;
            case STMT -> emitStatement(node, variableMap);
            case LABEL -> emitLabel(node, variableMap);
            //case SYMBOL -> null;
            case FUNCCALL -> emitFunctionCall(node, variableMap);
            //case ASS -> emitAssignment(node, variableMap);
            default -> throw new IllegalStateException("Not a statement!");
        };
    }

    private static String emitProgram(SyntaxTreeNode node, Map<String, Integer> variableMap) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < node.getNumberChildren(); i++) {
            SyntaxTreeNode child = node.getChild(i);
            out.append(emitCode(child, variableMap));
        }
        return out.toString();
    }

    private static String emitLabel(SyntaxTreeNode node, Map<String, Integer> variableMap) {
        return node.getValue() + ":\n";
    }

    private static String emitDeclaration(SyntaxTreeNode node, Map<String, Integer> variableMap) {
        boolean[] vars = new boolean[variableMap.size()];
        int variables = 0;
        boolean readValue = false;
        String value = "";
        for (int j = 0; j < node.getNumberChildren(); j++) {
            SyntaxTreeNode child = node.getChild(j);
            if(child.getType() == SyntaxTreeNode.Type.NAME){
                variables++;
                vars[variableMap.get(child.getValue())] = true;
            }
            if(child.getType() == SyntaxTreeNode.Type.SYMBOL && child.getValue().equals("=")) {
                readValue = true;
            }
            if(readValue){
                if(child.getType() == SyntaxTreeNode.Type.EXPR){
                    value = emitExpression(child, variableMap);
                } else if(child.getType() == SyntaxTreeNode.Type.COND) {
                    value = emitCondition(child, variableMap);
                } else {
                    throw new IllegalStateException("Expected value!");
                }
            }
        }
        StringBuilder out = new StringBuilder();
        out.append("ALLOC ").append(variables).append("\n");
        if(readValue) {
            for (int i = 0; i < vars.length; i++) {
                if(vars[i]) {
                    out.append(value).append("STORE ").append(i).append("\n");
                }
            }
        }
        return out.toString();
    }

    private static String emitExpression(SyntaxTreeNode node, Map<String, Integer> variableMap) {
        SyntaxTreeNode next = node.getChild(0);
        if(next.getType() == SyntaxTreeNode.Type.NUMBER) {
            return "CONST " + next.getValue() + "\n";
        }
        if(next.getType() == SyntaxTreeNode.Type.NAME) {
            return "LOAD " + variableMap.get(next.getValue()) + "\n";
        }
        if(next.getType() == SyntaxTreeNode.Type.SYMBOL) {
            if(next.getValue().equals("(")) {
                return emitExpression(node.getChild(1), variableMap);
            }
            if(next.getValue().equals("-")) {
                return emitExpression(node.getChild(1), variableMap) + "NEG\n";
            }
            throw new IllegalStateException("Unexpected symbol " + next.getValue());
        }
        if(next.getType() == SyntaxTreeNode.Type.EXPR) {
            return emitExpression(next, variableMap) + emitExpression(node.getChild(2), variableMap) + emitOperator(node.getChild(1), variableMap);
        }
        if(next.getType() == SyntaxTreeNode.Type.FUNCCALL) {
            return emitFunctionCall(next, variableMap);
        }
        if(next.getType() == SyntaxTreeNode.Type.COND) {
            return emitCondition(next, variableMap);
        }
        throw new IllegalStateException("Couldn't resolve expression");
    }

    private static String emitStatement(SyntaxTreeNode node, Map<String, Integer> variableMap) {
        SyntaxTreeNode next = node.getChild(0);
        if(next.getType() == SyntaxTreeNode.Type.SYMBOL) {
            if(next.getValue().equals(";")) {
                System.err.println("Unnecessary Semicolon detected");
                return "";
            }
            if(next.getValue().equals("{")) {
                StringBuilder out = new StringBuilder();
                for (int i = 1; i < node.getNumberChildren() - 1; i++) {
                    out.append(emitCode(node.getChild(i), variableMap));
                }
                return out.toString();
            }
            if(next.getValue().equals("return")) {
                return "HALT\n";
            }
            if(next.getValue().equals("while")) {
                var start = "while" + whileLoops + ":\n" + emitCondition(node.getChild(2), variableMap) + "FJUMP whileEnd" + whileLoops +"\n";
                var end = "JUMP " + "while" + whileLoops + "\nwhileEnd" + whileLoops +":\n";
                whileLoops++;
                return start + emitCode(node.getChild(4), variableMap) + end;
            }
            if(next.getValue().equals("if")) {
                var currentIfs = ifStatements++;
                var start = emitCondition(node.getChild(2), variableMap) + "FJUMP else" + currentIfs + "\n";
                var temp = start + emitCode(node.getChild(4), variableMap);
                var end = "else" + currentIfs + ":\n";
                if(node.getNumberChildren() > 5) {
                    var middle = "JUMP ifend" + currentIfs + "\n" + end;
                    end = "ifend" + currentIfs +":\n";
                    temp += middle + emitCode(node.getChild(6), variableMap);
                }
                temp += end;
                return temp;
            }
            throw new IllegalStateException("Unexpected symbol " + next.getValue());
        }
        if(next.getType() == SyntaxTreeNode.Type.LABEL) {
            return emitLabel(next, variableMap) + emitCode(node.getChild(2), variableMap);
        }
        if(next.getType() == SyntaxTreeNode.Type.ASS) {
            return emitAssignment(next, variableMap);
        }
        if(next.getType() == SyntaxTreeNode.Type.FUNCCALL) {
            return emitFunctionCall(next, variableMap);
        }
        throw new IllegalStateException("Unexpected symbol " + next.getValue());
    }

    private static String emitAssignment(SyntaxTreeNode node, Map<String, Integer> variableMap) {
        SyntaxTreeNode next = node.getChild(0);
        int pos = variableMap.get(next.getValue());
        String store = "STORE " + pos;
        return emitExpression(node.getChild(2), variableMap) + store + "\n";
    }

    private static String emitFunctionCall(SyntaxTreeNode node, Map<String, Integer> variableMap) {
        SyntaxTreeNode next = node.getChild(0);
        if(next.getValue().equals("write")) {
            return emitExpression(node.getChild(2), variableMap) + "WRITE\n";
        }
        if(next.getValue().equals("readInt")) {
            return "READ\n";
        }
        throw new UnsupportedOperationException("This compiler does not support CALL and RET");
    }

    private static String emitOperator(SyntaxTreeNode node, Map<String, Integer> variableMap) {
        return switch (node.getValue()) {
            case "+" -> "ADD\n";
            case "-" -> "SUB\n";
            case "*" -> "MUL\n";
            case "/" -> "DIV\n";
            case "%" -> "MOD\n";
            default -> throw new IllegalStateException();
        };
    }

    private static String emitCondition(SyntaxTreeNode node, Map<String, Integer> variableMap) {
        SyntaxTreeNode next = node.getChild(0);
        if(next.getType() == SyntaxTreeNode.Type.BOOL) {
            if(next.getValue().equals("true")) {
                return "TRUE\n";
            }
            if(next.getValue().equals("false")) {
                return "FALSE\n";
            }
            throw new IllegalStateException("Unexpected non bool value detected as bool!");
        }

        if(next.getType() == SyntaxTreeNode.Type.SYMBOL) {
            if(next.getValue().equals("(")) {
                return emitCondition(node.getChild(1), variableMap);
            }
            if(next.getValue().equals("!")) {
                return emitExpression(node.getChild(1), variableMap) + "NOT\n";
            }
            throw new IllegalStateException("Unexpected symbol " + next.getValue());
        }
        if(next.getType() == SyntaxTreeNode.Type.COND) {
            return emitCondition(next, variableMap) + emitCondition(node.getChild(2), variableMap) + emitBoolOperator(node.getChild(1), variableMap);
        }
        if(next.getType() == SyntaxTreeNode.Type.EXPR) {
            if(node.getNumberChildren() == 1) {
                return emitExpression(next, variableMap);
            }
            return emitExpression(next, variableMap) + emitExpression(node.getChild(2), variableMap) + emitComparator(node.getChild(1), variableMap);
        }
        throw new IllegalStateException();
    }

    private static String emitComparator(SyntaxTreeNode node, Map<String, Integer> variableMap) {
        return switch (node.getValue()) {
            case "==" -> "EQ\n";
            case "!=" -> "NEQ\n";
            case "<" -> "LESS\n";
            case "<=" -> "LEQ\n";
            case ">" -> "LEQ\nNOT\n";
            case ">=" -> "LESS\nNOT\n";
            default -> throw new IllegalStateException();
        };
    }

    private static String emitBoolOperator(SyntaxTreeNode node, Map<String, Integer> variableMap) {
        return switch (node.getValue()) {
            case "&&", "&" -> "AND\n";
            case "||", "|" -> "OR\n";
            default -> throw new IllegalStateException();
        };
    }
}
