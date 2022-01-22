package pgdp.minijava;

import pgdp.minijava.ast.SyntaxTreeNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Compiler {
    private Compiler() {

    }

    public static void main(String[] args) {
        compileFromFile("resources/input.java");
    }

    public static void compileFromFile(String filePath) {
        SyntaxTreeNode node = Parser.parseFromFile(filePath);
        String out = """
                //Generated using my MiniJavaCompiler at https://github.com/SilicDev/MiniJavaCompiler
                """ + Emitter.emit(node);

        if(filePath.startsWith("resources/")) {
            filePath = "resources/bin/" + filePath.substring(10);
        }
        if(filePath.endsWith(".java")) {
            filePath = filePath.substring(0, filePath.length() - 5) + ".jvm";
        }
        Path path = Path.of(filePath);
        try {
            Files.writeString(path, out, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
