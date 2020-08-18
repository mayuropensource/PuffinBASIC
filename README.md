# PuffinBASIC
BASIC interpreter written in Java.

BASIC (Beginners' All-purpose Symbolic Instruction Code) is a general-purpose high-level
language from the 1960s.

## Version

0.1 experimental

## Dependencies

JDK 11+, Maven, antlr4

## Build and test

```$xslt
$ mvn compile
$ mvn test
```

## Run using Maven
```$xslt
$ mvn exec:java -Dexec.args="samples/forloop.bas"
```

## Working with Intellij

Import the pom.xml file in Intellij.
After importing, if you make a chane to the antlr4 grammas,
regenerate the antlr4 code using following command and then reload the changes.
```$xslt
$ mvn generate-sources
```

## How it works?

1. Uses antlr4 to define the language grammar.
2. It parses the program using antlr4 lexer and parser and generates intermediate representation (IR) of the source code. 
During parsing, it populates a symbol table.
3. At runtime, it runs the IR instructions using the symbol table.

# Reference

## Mode of operation

### Indirect Mode

1. Write a program in your favorite editor.
2. Save it in a text file with a '.bas' extension (not a requirement).
3. Use maven or PuffinBasicInterpreterMain to run the program.

PuffinBASIC supports indirect mode only.

## Compatibility

PuffinBASIC is mostly compatible with Microsoft's GWBASIC.
Currently, it does not support any graphics or mouse/joy instructions.
In the future, we may add support for graphics using Java Swing/2D graphics.

PuffinBASIC will not support assembly instructions.

### Input

PuffinBASIC aims for cross-platform compatibility and doesn't support platform specific features. 
Input statements and functions are line based and require 'ENTER' key to be pressed.
Same applies for the sequential file writes, print statements always output a line and input statements read the whole line.

### Case sensitivity

Operators, Statements and Standard Functions are case-insensitive.
Constants, variables and user defined functions are case-sensitive.

## Commands

PuffinBASIC does not support BASIC Commands, such as LIST, RUN, etc.

## Data Types

1. int32 (32-bit signed integer): Int32 constants can have an optional '%' suffix.
Int32 constants can be decimal, octal or hexadecimal.
Octal numbers must have '&' or '&O' prefix, e.g. &12 or &O12.
Hexadecimal numbers must have '&H' prefix, e.g. &HFF.

2. int64 (64-bit signed integer): Int64 constants must have '@' suffix.
Int64 constants can be decimal, octal or hexadecimal.

3. float32 (32-bit signed IEEE-754 floating-point): Float32 constants can have an optional '!' suffix.
Float32 constants can use a decimal format or scientific notations, e.g. 1.2 or 1.2E-2.

4. float64 (64-bit signed IEEE-754 floating-point): Float64 constants can have an optional '#' suffix.
Float32 constants can use a decimal format or scientific notations, e.g. 1.2 or 1.2E-2.

5. String: String stores any non-newline (or carriage return) ASCII character. 
A string must be enclosed within double-quotes, e.g. "A TEST, STRING".
There is no limit of length of a string.

## Variables

A variable name must start with a letter followed by one or more letters or numeric digits.
A variable name must not start with 'FN' because it is reserved for user defined functions.

There are two kinds of variables:
1. Scalar variable stores a single value.
2. Array variable stores a multi-dimensional array.

## Operators

## Functions

## Statements
