package pgdp.minijava;

public class Compiler {
    public static void main(String[] args) {
        compileFromFile("resources/input.java");
    }

    public static void compileFromFile(String filePath) {
        SyntaxTreeNode node = Parser.parseFromFile(filePath);
        String out = Emitter.emit(node);
    }
}
