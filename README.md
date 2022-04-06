# MiniJavaCompiler
At the moment a lot of Java token are detected but not properly parsed.
I may cut down on it later.

The compiler takes a miniJava file and compiles it into miniJVM.

Mind that I only have a basic understanding of compilers.

--- 

# How does it work?

The compiler consists of three major components:
1. [The Tokenizer](#the-tokenizer) : Converts the miniJava code into tokens for the parser.
2. [The Parser](#the-parser) : Converts the tokens into an Abstract Syntax Tree (AST)
3. [The Emitter](#the-emitter) : Converts the AST into executable miniJVM code

## The Tokenizer
The tokenizer splits the raw miniJava code into tokens of different types.
Tokens are the smallest unit of code in the text and are basically just like
words in natural languages. The tokenizer is comparatively stupid, as all it
knows about the code is the token it is currently creating. As such some code 
words would not be able to be accurately determined this way for a complete
java compiler.

I decided to go with an approach that creates the token once the tokenizer 
detects a valid token end (i.e. whitespace, separator, invalid char for the 
current term), as otherwise it seemed difficult to determine if a token should 
be an identifier or a keyword for example.

The tokens are categorized as follows:

| TokenType  | Explanation                                                                        | Examples                           |
| ---------- | ---------------------------------------------------------------------------------- | ---------------------------------- |
| KEYWORD    | Words that are reserved as keywords in Java.                                       | if, while, return, etc.            |
| SEPARATOR  | Symbols that structure  the code in some form or another.                          | `; , . { } ( ) :` etc.             |
| OPERATOR   | Symbols that represent a operation in Java.                                        | int and bool operators             |
| LITERAL    | Any value that's directly fed into the program.                                    | `-10`, `"Hello"`, `true`, etc.     |
| COMMENT    | Text that has been marked as comment. The parser decides how to handle them.       | `//text`, `/* more text */`        |
| IDENTIFIER | The default option. About everything that doesn't fit any of the other categories. | label, variable and function names |

This implementation of the tokenizer recognizes many keywords of standard Java 
not used in miniJava.
This is mostly done to lock the user from using these names as identifiers.

:warning: However, the tokenizer cannot tokenize floating point values correctly, although it would be feasible to implement.

The tokens are then passed to the parser.

## The Parser
The parser iterates over the received tokens and builds an Abstract Syntax Tree 
(AST) from them. The AST is a direct representation of the control flow of the 
program. The nodes of the AST are of different types signifying different kinds 
of statements and expressions. The parser is already a bit smarter: it can argue
about the parent of the new node, the current node, the current token and tokens 
relative to the current token. This allows the parser to throw exceptions if an
unexpected token comes up or in other words the parser can expect certain token 
types based on the current context. Further, at this point the parser can throw 
out comments and unsupported keywords.

The nodes are categorized as follows:

PROGRAM
- Unique node only used for the root of the tree
- Holds no value

DECL (short for declaration)
- The root of a declaration.
- Holds no value.
- Expects a TYPE as first child and a NAME as second child.
- Optionally accepts more NAMEs separated by commas.
- If an equal sign is encountered the tokens after it are parsed as expression as default value.
- Ends in a semicolon.
- Currently the only way of assigning a condition as value of a variable.

NAME
- Represents a variable name.
- Holds the name of the identifier.

NUMBER
- Represents a numeric value.
- Holds the value as a string.

BOOL
- Represents a bool value.
- Holds the value as a string.

EXPR (short for expression)
- Represents an expression.
- Holds no value.
- Cannot stand alone / Is always part of a statement.
- Can have a single child of a value type (NAME, NUMBER, BOOL)
- OR Can be another expression surrounded by parentheses ()
- OR Can be another non bool expression preceded by -
- OR Can be non bool two expressions split by an int operator
- OR Can be a function call (only readInt()).

COND (short for condition)
- Represents a condition.
- Holds no value.
- Cannot stand alone / Is always part of a statement.
- Can have a single child of a bool value type.
- OR Can be another condition surrounded by parentheses ()
- OR Can be another condition preceded by !
- OR Can be two conditions split by a bool operator
- OR Can be two expressions split by an int comparator.

COMP (short for comparator)
- Represents a comparator.
- Holds the comparator.

TYPE 
- Represents a type for a declaration.
- Holds the type.
- Only used to allow DECL to parse a condition as default value for booleans.

STMT (short for statement)
- Represents an statement.
- Holds no value.
- Can stand alone.
- Can have a single semicolon as child.
- OR can be any amount of statements surrounded by curly brackets
- OR can be a function call (only write(int))
- OR can be an assignment
- OR can be an if condition with or without else
- OR can be a while loop
- OR can be a label and a statement separated by a colon.

LABEL
- Represents a label.
- Holds the name of the label.
- :warning: The parser and subsequent steps do not filter duplicate labels!

FUNCCALL (short for function call)
- Represents a function call.
- Holds no value.
- Expects a name as first child followed by an opening parentheses.
- After the opening parentheses any amount of expressions can follow separated by commas.
- Afterwards follows a closing parentheses.

ASS (short for assignment)
- The root of an assignment.
- Holds no value.
- Expects a NAME as first child and an equal sign as second child.
- Expects an expression as third child.
- Ends in a semicolon.

SYMBOL
- Default for tokens whose type isn't relevant to the AST.
- Holds the text of the symbol.

As such the AST for this max() function:
```
int i, j;
i = readInt();
j = readInt();
if(i < j) {
    write(j);
} else {
    write(i);
}
```
might look like this:
```
PROGRAM :
    DECL : 
        TYPE : int
        NAME : i
        SYMBOL : ,
        NAME : j
        SYMBOL : ;
    STMT :
        ASS :
            NAME : i
            SYMBOL : =
            EXPR : 
                FUNCCALL : 
                    NAME : readInt
                    SYMBOL : (
                    SYMBOL : )
        SYMBOL : ;
    STMT :
        ASS :
            NAME : j
            SYMBOL : =
            EXPR : 
                FUNCCALL : 
                    NAME : readInt
                    SYMBOL : (
                    SYMBOL : )
        SYMBOL : ;
    STMT :
        SYMBOL : if
        SYMBOL : (
        COND : 
            EXPR :
                NAME : i
            COMP : <
            EXPR :
                NAME : j
        SYMBOL : )
        STMT : 
            SYMBOL : {
            STMT : 
                FUNCCALL :
                    NAME : write
                    SYMBOL : (
                    EXPR :
                        NAME : j
                    SYMBOL : )
                SYMBOL : ;
            SYMBOL : }
        SYMBOL : else
        STMT :
            SYMBOL : {
            STMT :
                FUNCCALL : 
                    NAME : write
                    SYMBOL : (
                    EXPR : 
                        NAME : i
                    SYMBOL : )
                SYMBOL : ;
            SYMBOL : }
```
:note: Of course a lot of symbols can be cut (especially `( ) { } ; :`) but for 
debugging purposes I decided to keep the symbols for now.

The AST is then passed to the emitter.

## The Emitter

The emitter tries to compile the Abstract Syntax Tree received from the parser to miniJVM code.

This process is split into multiple passes.

In the first pass the emitter determines if all variables are declared before 
they are used.

In the second pass (the main pass) the emitter iterates over the AST and compiles it into raw miniJVM code,
however, by altering the emit functions it would be possible to compile into other languages as well.
Should the emitter encounter unexpected nodes at this time, the compiling process fails.

In the third pass a few optimizations are done:
1. Usages of ALLOC directly after another are unified into one.
2. Label declarations directly after another are squashed into one prioritizing labels declared by java labels.

---

# Limitations

The Parser is unable to parse functions/lambdas and classes. Further the 
increment and decrement operators ++ and -- are not parsed. Similarly `+= -= *= /=`
etc. are not parsed.

The Parser has not been stress tested for large, complicated conditions.

As mentioned before floating point values are not supported; all number types 
are defaulted to int.

Only the parser and tokenizer are able to return line numbers when throwing 
exceptions, however, adding a line variable to the SyntaxTreeNodes solves this 
if needed.

Currently the parser does not ensure the types of the variables.

---
# Observations

The tokenizer encounters some issues for specific symbols in a compiler for 
complexer languages. For example the tokenizer can not decide whether a `<` is a
comparator or a separator for a generic type (i.e. `List<T>`) This can only be 
resolved in the AST using an expanded TYPE node. Overall the tokenizer can 
barely detect issues apart from illegal characters in the code.

The Parser has issues recognizing conditions in assignments as value as they can
start with expressions. However, a similar hack as employed in parseCondition 
might solve this issue. Another solution might be to keep track of the variables
and their types at this stage.

The AST gives a huge advantage when compiling over a simple token list as all 
information like code blocks, statement type, and control flow are encoded 
through the different node types and the tree structure. The node types also 
restrict the expected node types of the children so that a syntax error is found
with relative ease at this stage.

The emitter is very rigid, and would I write something like this again I would 
probably choose to move the logic into subclasses of SyntaxTreeNode with a 
factory instead of using an enum. However, for a one-week project this is fine, 
and it would theoretically allow me to exchange the emitter to compile for other
languages.

---

# Sources

These sites helped me understand how a compiler works:
- [List of Java keywords (Wikipedia)](https://en.wikipedia.org/wiki/List_of_Java_keywords)
- [Lexical analysis (Wikipedia)](https://en.wikipedia.org/wiki/Lexical_analysis)
- [Write your own compiler (Blog post https://blog.klipse.tech)](https://blog.klipse.tech/javascript/2017/02/08/tiny-compiler-parser.html)
- [How does a parser (for example, HTML) work? (stackoverflow)](https://stackoverflow.com/questions/3150293/how-does-a-parser-for-example-html-work)
- [Constructing an Abstract Syntax Tree with a list of Tokens (stackoverflow)](https://stackoverflow.com/questions/25049751/constructing-an-abstract-syntax-tree-with-a-list-of-tokens/25106688#25106688)
- [Compiling an AST to Assembly (stackoverflow)](https://stackoverflow.com/questions/42445522/compiling-an-ast-to-assembly)

---

[Markdown cheat sheet](https://www.markdownguide.org/cheat-sheet)
