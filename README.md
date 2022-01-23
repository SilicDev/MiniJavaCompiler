# MiniJavaCompiler
At the moment a lot of Java token are detected but not properly parsed.
I may cut down on it later.

The compiler takes a miniJava file and compiles it into miniJVM.

Mind that I only have a basic understanding of compilers.

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

The tokens are categorized as follows:

| TokenType  | Explanation                                                                        | Examples                           |
| ---------- | ---------------------------------------------------------------------------------- | ---------------------------------- |
| KEYWORD    | Words that are reserved as keywords in Java.                                       | if, while, return, etc.            |
| SEPARATOR  | Symbols that structure  the code in some form or another.                          | '; , . { } ( ) :' etc.             |
| OPERATOR   | Symbols that represent a operation in Java.                                        | int and bool operators             |
| LITERAL    | Any value that's directly fed into the program.                                    | -10, "Hello", true, etc.           |
| COMMENT    | Text that has been marked as comment. The parser decides how to handle them.       | '//text', '/* more text */'        |
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

# Observations

'// TODO'