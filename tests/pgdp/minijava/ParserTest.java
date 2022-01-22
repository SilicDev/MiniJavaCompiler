package pgdp.minijava;

import org.junit.jupiter.api.Test;
import pgdp.minijava.ast.SyntaxTreeNode;
import pgdp.minijava.exceptions.IllegalCharacterException;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
    @Test
    public void testParseCondition() throws IllegalCharacterException {
        var text = "(true)";
        // Test case (cond)
        var tokens = Tokenizer.tokenize(text);
        var tree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        var pos = Parser.parseCondition(tokens, 0, tree);
        var expectedTree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        var temp = new SyntaxTreeNode(SyntaxTreeNode.Type.COND, "");
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, "("));
        var temp2 = new SyntaxTreeNode(SyntaxTreeNode.Type.COND, "");
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.BOOL, "true"));
        temp.addChild(temp2);
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, ")"));
        expectedTree.addChild(temp);
        assertEquals(expectedTree, tree);
        assertEquals(3, pos);

        // Test true
        tokens = Tokenizer.tokenize(text);
        tree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        pos = Parser.parseCondition(tokens, 1, tree);
        expectedTree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        temp = new SyntaxTreeNode(SyntaxTreeNode.Type.COND, "");
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.BOOL, "true"));
        expectedTree.addChild(temp);
        assertEquals(expectedTree, tree);
        assertEquals(2, pos);

        // Test false
        text = "(false)";
        tokens = Tokenizer.tokenize(text);
        tree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        pos = Parser.parseCondition(tokens, 1, tree);
        expectedTree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        temp = new SyntaxTreeNode(SyntaxTreeNode.Type.COND, "");
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.BOOL, "false"));
        expectedTree.addChild(temp);
        assertEquals(expectedTree, tree);
        assertEquals(2, pos);

        // Test var
        text = "(flag)";
        tokens = Tokenizer.tokenize(text);
        tree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        pos = Parser.parseCondition(tokens, 1, tree);
        expectedTree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        temp = new SyntaxTreeNode(SyntaxTreeNode.Type.COND, "");
        temp2 = new SyntaxTreeNode(SyntaxTreeNode.Type.EXPR, "");
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, "flag"));
        temp.addChild(temp2);
        expectedTree.addChild(temp);
        assertEquals(expectedTree, tree);
        assertEquals(2, pos);

        // Test !cond
        text = "(!false)";
        tokens = Tokenizer.tokenize(text);
        tree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        pos = Parser.parseCondition(tokens, 1, tree);
        expectedTree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        temp = new SyntaxTreeNode(SyntaxTreeNode.Type.COND, "");
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, "!"));
        temp2 = new SyntaxTreeNode(SyntaxTreeNode.Type.COND, "");
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.BOOL, "false"));
        temp.addChild(temp2);
        expectedTree.addChild(temp);
        assertEquals(expectedTree, tree);
        assertEquals(3, pos);

        // Test cond bbinop cond
        text = "(false || true)";
        tokens = Tokenizer.tokenize(text);
        tree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        pos = Parser.parseCondition(tokens, 1, tree);
        expectedTree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        temp = new SyntaxTreeNode(SyntaxTreeNode.Type.COND, "");
        temp2 = new SyntaxTreeNode(SyntaxTreeNode.Type.COND, "");
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.BOOL, "false"));
        temp.addChild(temp2);
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, "||"));
        temp2 = new SyntaxTreeNode(SyntaxTreeNode.Type.COND, "");
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.BOOL, "true"));
        temp.addChild(temp2);
        expectedTree.addChild(temp);
        assertEquals(expectedTree, tree);
        assertEquals(4, pos);

        // Test expr comp expr
        text = "(0 < i)";
        tokens = Tokenizer.tokenize(text);
        tree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        pos = Parser.parseCondition(tokens, 1, tree);
        expectedTree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        temp = new SyntaxTreeNode(SyntaxTreeNode.Type.COND, "");
        temp2 = new SyntaxTreeNode(SyntaxTreeNode.Type.EXPR, "");
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NUMBER, "0"));
        temp.addChild(temp2);
        temp2 = new SyntaxTreeNode(SyntaxTreeNode.Type.COMP, "<");
        temp.addChild(temp2);
        temp2 = new SyntaxTreeNode(SyntaxTreeNode.Type.EXPR, "");
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, "i"));
        temp.addChild(temp2);
        expectedTree.addChild(temp);
        assertEquals(expectedTree, tree);
        assertEquals(4, pos);

        // Test 0 < i && i < 10
        text = "(0 < i * 2 && i < 10)";
        tokens = Tokenizer.tokenize(text);
        tree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        pos = Parser.parseCondition(tokens, 1, tree);
        expectedTree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        temp = new SyntaxTreeNode(SyntaxTreeNode.Type.COND, "");
        temp2 = new SyntaxTreeNode(SyntaxTreeNode.Type.COND, "");
        var temp3 = new SyntaxTreeNode(SyntaxTreeNode.Type.EXPR, "");
        temp3.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NUMBER, "0"));
        temp2.addChild(temp3);
        temp3 = new SyntaxTreeNode(SyntaxTreeNode.Type.COMP, "<");
        temp2.addChild(temp3);
        temp3 = new SyntaxTreeNode(SyntaxTreeNode.Type.EXPR, "");
        var temp4 = new SyntaxTreeNode(SyntaxTreeNode.Type.EXPR, "");
        temp4.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, "i"));
        temp3.addChild(temp4);
        temp3.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, "*"));
        temp4 = new SyntaxTreeNode(SyntaxTreeNode.Type.EXPR, "");
        temp4.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NUMBER, "2"));
        temp3.addChild(temp4);
        temp2.addChild(temp3);
        temp.addChild(temp2);
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, "&&"));
        temp2 = new SyntaxTreeNode(SyntaxTreeNode.Type.COND, "");
        temp3 = new SyntaxTreeNode(SyntaxTreeNode.Type.EXPR, "");
        temp3.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, "i"));
        temp2.addChild(temp3);
        temp3 = new SyntaxTreeNode(SyntaxTreeNode.Type.COMP, "<");
        temp2.addChild(temp3);
        temp3 = new SyntaxTreeNode(SyntaxTreeNode.Type.EXPR, "");
        temp3.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NUMBER, "10"));
        temp2.addChild(temp3);
        temp.addChild(temp2);
        expectedTree.addChild(temp);
        assertEquals(expectedTree, tree);
        assertEquals(10, pos);
    }

    @Test
    public void testParseDeclaration() throws IllegalCharacterException {
        // Test type name;
        var text = "int i;";
        var tokens = Tokenizer.tokenize(text);
        var tree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        var pos = Parser.parseDeclaration(tokens, 0, tree);
        var expectedTree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        var temp = new SyntaxTreeNode(SyntaxTreeNode.Type.DECL, "");
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.TYPE, "int"));
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, "i"));
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, ";"));
        expectedTree.addChild(temp);
        assertEquals(expectedTree, tree);
        assertEquals(3, pos);

        // Test type name;
        text = "int i = 0;";
        tokens = Tokenizer.tokenize(text);
        tree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        pos = Parser.parseDeclaration(tokens, 0, tree);
        expectedTree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        temp = new SyntaxTreeNode(SyntaxTreeNode.Type.DECL, "");
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.TYPE, "int"));
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, "i"));
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, "="));
        var temp2 = new SyntaxTreeNode(SyntaxTreeNode.Type.EXPR, "");
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NUMBER, "0"));
        temp.addChild(temp2);
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, ";"));
        expectedTree.addChild(temp);
        assertEquals(expectedTree, tree);
        assertEquals(5, pos);

        // Test type name;
        text = "boolean i = true;";
        tokens = Tokenizer.tokenize(text);
        tree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        pos = Parser.parseDeclaration(tokens, 0, tree);
        expectedTree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        temp = new SyntaxTreeNode(SyntaxTreeNode.Type.DECL, "");
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.TYPE, "boolean"));
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, "i"));
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, "="));
        temp2 = new SyntaxTreeNode(SyntaxTreeNode.Type.COND, "");
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.BOOL, "true"));
        temp.addChild(temp2);
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, ";"));
        expectedTree.addChild(temp);
        assertEquals(expectedTree, tree);
        assertEquals(5, pos);

        // Test type name, name;
        text = "int i, i2;";
        tokens = Tokenizer.tokenize(text);
        tree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        pos = Parser.parseDeclaration(tokens, 0, tree);
        expectedTree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        temp = new SyntaxTreeNode(SyntaxTreeNode.Type.DECL, "");
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.TYPE, "int"));
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, "i"));
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, ","));
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, "i2"));
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, ";"));
        expectedTree.addChild(temp);
        assertEquals(expectedTree, tree);
        assertEquals(5, pos);
    }

    @Test
    public void testParseExpression() throws IllegalCharacterException {
        // Test number;
        var text = "(10)";
        var tokens = Tokenizer.tokenize(text);
        var tree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        var pos = Parser.parseExpression(tokens, 1, tree);
        var expectedTree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        var temp = new SyntaxTreeNode(SyntaxTreeNode.Type.EXPR, "");
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NUMBER, "10"));
        expectedTree.addChild(temp);
        assertEquals(expectedTree, tree);
        assertEquals(2, pos);

        // Test name;
        text = "(value)";
        tokens = Tokenizer.tokenize(text);
        tree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        pos = Parser.parseExpression(tokens, 1, tree);
        expectedTree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        temp = new SyntaxTreeNode(SyntaxTreeNode.Type.EXPR, "");
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, "value"));
        expectedTree.addChild(temp);
        assertEquals(expectedTree, tree);
        assertEquals(2, pos);

        // Test (expr);
        text = "(value)";
        tokens = Tokenizer.tokenize(text);
        tree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        pos = Parser.parseExpression(tokens, 0, tree);
        expectedTree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        temp = new SyntaxTreeNode(SyntaxTreeNode.Type.EXPR, "");
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, "("));
        var temp2 = new SyntaxTreeNode(SyntaxTreeNode.Type.EXPR, "");
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, "value"));
        temp.addChild(temp2);
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, ")"));
        expectedTree.addChild(temp);
        assertEquals(expectedTree, tree);
        assertEquals(3, pos);

        // Test -expr;
        text = "(-value)";
        tokens = Tokenizer.tokenize(text);
        tree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        pos = Parser.parseExpression(tokens, 1, tree);
        expectedTree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        temp = new SyntaxTreeNode(SyntaxTreeNode.Type.EXPR, "");
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, "-"));
        temp2 = new SyntaxTreeNode(SyntaxTreeNode.Type.EXPR, "");
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, "value"));
        temp.addChild(temp2);
        expectedTree.addChild(temp);
        assertEquals(expectedTree, tree);
        assertEquals(3, pos);

        // Test expr binop expr;
        text = "(value * value)";
        tokens = Tokenizer.tokenize(text);
        tree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        pos = Parser.parseExpression(tokens, 1, tree);
        expectedTree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        temp = new SyntaxTreeNode(SyntaxTreeNode.Type.EXPR, "");
        temp2 = new SyntaxTreeNode(SyntaxTreeNode.Type.EXPR, "");
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, "value"));
        temp.addChild(temp2);
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, "*"));
        temp2 = new SyntaxTreeNode(SyntaxTreeNode.Type.EXPR, "");
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, "value"));
        temp.addChild(temp2);
        expectedTree.addChild(temp);
        assertEquals(expectedTree, tree);
        assertEquals(4, pos);
    }

    @Test
    public void testParseStatement() throws IllegalCharacterException {
        // Test ;
        var text = ";";
        var tokens = Tokenizer.tokenize(text);
        var tree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        var pos = Parser.parseStatement(tokens, 0, tree);
        var expectedTree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        var temp = new SyntaxTreeNode(SyntaxTreeNode.Type.STMT, "");
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, ";"));
        expectedTree.addChild(temp);
        assertEquals(expectedTree, tree);
        assertEquals(1, pos);

        // Test {stmt}
        text = "{;}";
        tokens = Tokenizer.tokenize(text);
        tree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        pos = Parser.parseStatement(tokens, 0, tree);
        expectedTree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        temp = new SyntaxTreeNode(SyntaxTreeNode.Type.STMT, "");
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, "{"));
        var temp2 = new SyntaxTreeNode(SyntaxTreeNode.Type.STMT, "");
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, ";"));
        temp.addChild(temp2);
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, "}"));
        expectedTree.addChild(temp);
        assertEquals(expectedTree, tree);
        assertEquals(3, pos);

        // Test {stmt stmt stmt}
        text = "{;;;}";
        tokens = Tokenizer.tokenize(text);
        tree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        pos = Parser.parseStatement(tokens, 0, tree);
        expectedTree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        temp = new SyntaxTreeNode(SyntaxTreeNode.Type.STMT, "");
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, "{"));
        temp2 = new SyntaxTreeNode(SyntaxTreeNode.Type.STMT, "");
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, ";"));
        temp.addChild(temp2);
        temp2 = new SyntaxTreeNode(SyntaxTreeNode.Type.STMT, "");
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, ";"));
        temp.addChild(temp2);
        temp2 = new SyntaxTreeNode(SyntaxTreeNode.Type.STMT, "");
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, ";"));
        temp.addChild(temp2);
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, "}"));
        expectedTree.addChild(temp);
        assertEquals(expectedTree, tree);
        assertEquals(5, pos);

        // Test name = expr;
        text = "i = 0;";
        tokens = Tokenizer.tokenize(text);
        tree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        pos = Parser.parseStatement(tokens, 0, tree);
        expectedTree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        temp = new SyntaxTreeNode(SyntaxTreeNode.Type.STMT, "");
        temp2 = new SyntaxTreeNode(SyntaxTreeNode.Type.ASS, "");
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, "i"));
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, "="));
        var temp3 = new SyntaxTreeNode(SyntaxTreeNode.Type.EXPR, "");
        temp3.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NUMBER, "0"));
        temp2.addChild(temp3);
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, ";"));
        temp.addChild(temp2);
        expectedTree.addChild(temp);
        assertEquals(expectedTree, tree);
        assertEquals(4, pos);

        // Test name = expr;
        text = "i = true;";
        tokens = Tokenizer.tokenize(text);
        tree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        pos = Parser.parseStatement(tokens, 0, tree);
        expectedTree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        temp = new SyntaxTreeNode(SyntaxTreeNode.Type.STMT, "");
        temp2 = new SyntaxTreeNode(SyntaxTreeNode.Type.ASS, "");
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, "i"));
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, "="));
        temp3 = new SyntaxTreeNode(SyntaxTreeNode.Type.EXPR, "");
        var temp4 = new SyntaxTreeNode(SyntaxTreeNode.Type.COND, "");
        temp4.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.BOOL, "true"));
        temp3.addChild(temp4);
        temp2.addChild(temp3);
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, ";"));
        temp.addChild(temp2);
        expectedTree.addChild(temp);
        assertEquals(expectedTree, tree);
        assertEquals(4, pos);

        // Test name = readInt();
        text = "i = readInt();";
        tokens = Tokenizer.tokenize(text);
        tree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        pos = Parser.parseStatement(tokens, 0, tree);
        expectedTree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        temp = new SyntaxTreeNode(SyntaxTreeNode.Type.STMT, "");
        temp2 = new SyntaxTreeNode(SyntaxTreeNode.Type.ASS, "");
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, "i"));
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, "="));
        temp3 = new SyntaxTreeNode(SyntaxTreeNode.Type.EXPR, "");
        temp4 = new SyntaxTreeNode(SyntaxTreeNode.Type.FUNCCALL, "");
        temp4.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, "readInt"));
        temp4.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, "("));
        temp4.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, ")"));
        temp3.addChild(temp4);
        temp2.addChild(temp3);
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, ";"));
        temp.addChild(temp2);
        expectedTree.addChild(temp);
        assertEquals(expectedTree, tree);
        assertEquals(6, pos);

        // Test write(name);
        text = "write(i);";
        tokens = Tokenizer.tokenize(text);
        tree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        pos = Parser.parseStatement(tokens, 0, tree);
        expectedTree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        temp = new SyntaxTreeNode(SyntaxTreeNode.Type.STMT, "");
        temp2 = new SyntaxTreeNode(SyntaxTreeNode.Type.FUNCCALL, "");
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, "write"));
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, "("));
        temp3 = new SyntaxTreeNode(SyntaxTreeNode.Type.EXPR, "");
        temp3.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.NAME, "i"));
        temp2.addChild(temp3);
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, ")"));
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, ";"));
        temp.addChild(temp2);
        expectedTree.addChild(temp);
        assertEquals(expectedTree, tree);
        assertEquals(5, pos);

        // Test if (cond) stmt;
        text = "if(true);";
        tokens = Tokenizer.tokenize(text);
        tree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        pos = Parser.parseStatement(tokens, 0, tree);
        expectedTree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        temp = new SyntaxTreeNode(SyntaxTreeNode.Type.STMT, "");
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, "if"));
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, "("));
        temp2 = new SyntaxTreeNode(SyntaxTreeNode.Type.COND, "");
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.BOOL, "true"));
        temp.addChild(temp2);
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, ")"));
        temp2 = new SyntaxTreeNode(SyntaxTreeNode.Type.STMT, "");
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, ";"));
        temp.addChild(temp2);
        expectedTree.addChild(temp);
        assertEquals(expectedTree, tree);
        assertEquals(5, pos);

        // Test if (cond) stmt else stmt;
        text = "if(true); else ;";
        tokens = Tokenizer.tokenize(text);
        tree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        pos = Parser.parseStatement(tokens, 0, tree);
        expectedTree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        temp = new SyntaxTreeNode(SyntaxTreeNode.Type.STMT, "");
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, "if"));
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, "("));
        temp2 = new SyntaxTreeNode(SyntaxTreeNode.Type.COND, "");
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.BOOL, "true"));
        temp.addChild(temp2);
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, ")"));
        temp2 = new SyntaxTreeNode(SyntaxTreeNode.Type.STMT, "");
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, ";"));
        temp.addChild(temp2);
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, "else"));
        temp2 = new SyntaxTreeNode(SyntaxTreeNode.Type.STMT, "");
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, ";"));
        temp.addChild(temp2);
        expectedTree.addChild(temp);
        assertEquals(expectedTree, tree);
        assertEquals(7, pos);

        // Test while (cond) stmt;
        text = "while(true);";
        tokens = Tokenizer.tokenize(text);
        tree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        pos = Parser.parseStatement(tokens, 0, tree);
        expectedTree = new SyntaxTreeNode(SyntaxTreeNode.Type.PROGRAM, "");
        temp = new SyntaxTreeNode(SyntaxTreeNode.Type.STMT, "");
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, "while"));
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, "("));
        temp2 = new SyntaxTreeNode(SyntaxTreeNode.Type.COND, "");
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.BOOL, "true"));
        temp.addChild(temp2);
        temp.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, ")"));
        temp2 = new SyntaxTreeNode(SyntaxTreeNode.Type.STMT, "");
        temp2.addChild(new SyntaxTreeNode(SyntaxTreeNode.Type.SYMBOL, ";"));
        temp.addChild(temp2);
        expectedTree.addChild(temp);
        assertEquals(expectedTree, tree);
        assertEquals(5, pos);
    }
}