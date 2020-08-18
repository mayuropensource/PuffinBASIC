# PuffinBASIC
BASIC interpreter written in Java.

BASIC (Beginners' All-purpose Symbolic Instruction Code) is a general-purpose high-level
language from the 1960s. PuffinBASIC is an implementation of the BASIC language specification.
PuffinBASIC conforms most closely to GWBASIC.

## Version

0.1 experimental

## Code Samples

### Print multiplication tables

```
10 FOR I% = 1 TO 10
20   PRINT "Multiplication table of ", I%
30   FOR J% = 1 TO 10
40     PRINT I%, "x", J%, "=", I%*J%
50   NEXT J%
60 NEXT I%
```
### Print prime numbers from 1 to 100

```
10 FOR I% = 1 TO 100
20   J% = 3
30   N% = I% \ 2
40   ISPRIME% = (I% > 1) AND ((I% MOD 2 <> 0) OR (I% = 2))
50   WHILE J% <= N% AND ISPRIME% = -1
60     ISPRIME% = I% MOD J% <> 0
70     J% = J% + 2
80   WEND
90   IF ISPRIME% THEN PRINT STR$(I%), " is prime"
100 NEXT I%
```

### Print Fibonacci Series

```
10 A@ = 0 : B@ = 1
20 FOR I% = 1 TO 20
30   C@ = A@ + B@
40   PRINT C@,
50   A@ = B@ : B@ = C@
60 NEXT I%
70 PRINT ""
```
### Print trigonometric function graph

```
10 PRINT SPACE$(40), "0"
20 FOR D = 0 TO 360 STEP 10
30   x# = 3.14159 * D / 180.0
40   y# = SIN(x#)
50   PRINT SPACE$(40 + CINT(y# * 40)), "*"
60 NEXT D
```

## Dependencies

JDK 11+, Maven, antlr4

## Build and test

```
$ mvn compile
$ mvn test
```

## Run using Maven
```
$ mvn exec:java -Dexec.args="samples/graph.bas"
```

## Working with Intellij

Import the pom.xml file in Intellij.
After importing, if you make a change to the antlr4 grammar,
regenerate the antlr4 code using following command and then reload the changes.
```
$ mvn generate-sources
```

## How it works?

1. Uses antlr4 to define the language grammar.
2. It parses the program using antlr4 lexer+parser and generates intermediate representation (IR) of the source code. 
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

### Type conversion

Numeric types are converted into one another via implicit type conversion.
String and numeric types are not implicitly converted. 

## Variables

A variable name must start with a letter followed by one or more letters or numeric digits.
A variable name must not start with 'FN' because it is reserved for user defined functions.

A variable name has an optional suffix which sets the data type.
Int32 variable has '%' suffix, int64 variable has '@' suffix, float32 has '!' suffix,
float64 has '#' suffix, and String has '$' suffix. 
If a suffix is omitted, default data type assumed, the global default is Float64. 

There are two kinds of variables:
1. Scalar variable stores a single value, e.g. A%
2. Array variable stores a multi-dimensional array. An array variable must defined using DIM statement.
An array can have any number of dimensions. The minimum array index is 0 and maximum is dimension length - 1.

Example:
```
10 DIM A%(3, 4)
20 A%(1, 2) = 5
```

## Operators

Order of operations:

1. Arithmetic
2. Relational
3. Logical or bit-wise

Arithmetic:
1. '^'    Exponentiation
2. '-'    Unary minus
3. '*'    Multiplication
4. '/'    Floating point division
5. '\'    Integer division
6. 'mod'  Modulus
7. '+'    Addition
8. '-'    Subtraction

Relational operators return -1 for true and 0 for false.
Relational operators work with both numbers and Strings.

Relational:
1. '='    Equals
2. '<>'   Not equals
3. '<'    Less than
4. '>'    Greate than
5. '<='   Less than or equal
6. '>='   Greater than or equal

If inputs to logical operators is -1 and 0, they return -1 for true and 0 for false.
Otherwise, logical operators work like bit-wise operators.

Logical or bit-wise:
1. 'NOT'
2. 'AND'
3. 'OR'
4. 'XOR'
5. 'EQV'
6. 'IMP'

## Functions

Functions always return a single value.
A function may accept 0 or more parameters.

### Numeric Functions

#### ABS

Returns absolute value of a numeric expression.

Syntax:

```
ABS(n)
```

where, n is a numeric expression

Example:

```
ABS(-1)
1
```

#### EXP

Returns e to the power n.

Syntax:

```
EXP(n)
```

#### FIX

Truncates n to a whole number.
For positive number, it returns the floor.
FOr negative number, it returns the ceiling.

Syntax:

```
FIX(n)
```

Example:

```
FIX(-2.3)
-2
```

#### INT

Returns the floor of the number n.

Syntax:

```
INT(n)
```

Example:

```
INT(-2.3)
-3
```

#### LOG

Returns the natural logarithm of a numeric value.
n must be greater than 0.

Syntax:

```
LOG(n)
```

#### RND

Returns the next random number between 0 and 1.

Syntax:

```
RND
```

#### SQR

Returns the square root of n.
n must be greater than or equal to 0.

Syntax:

```
SQR(n)
```

#### Trigonometric Functions

SIN, COS and TAN take angle in radians and return sine, cosine and tangent of that angle.
To compute angle in radians, multiply the angle in degrees with pi/180.

ATAN takes a numeric value, computes arc tangent of the value and returns the angle in radians. 
To compute angle is degrees, multiply the angle in radians with 180/pi.

Syntax:

```
SIN(radians)
COS(radians)
TAN(radians)
ATAN(value)
```

### Numeric Conversion Functions

These functions are used to numeric type conversion.
CINT(n) converts the given numeric value to int32 with bounds check.
CLONG(n) converts the given numeric value to int64 with bounds check.
For CINT and CLNG, if the value is out of bounds, a DATA_OUT_OF_RANGE runtime error is thrown.

CSNG(n) converts the given numeric value to float32.
CDBL(n) converts the given numeric value to float64.

Syntax:

```
CINT(n)
CLNG(n)
CSNG(n)
CDBL(n)
```

### String Functions

#### ASC

Returns the numeric ASCII code for the first character in the string. 
If the string is empty, an ILLEGAL_FUNCTION_PARAM runtime error is thrown.
 
Syntax:

```
ASC(x$)
```

Example:

```
ASC("A")
65
```

#### CHR$

Converts the given ASCII numeric value to equivalent single character string.

Syntax:

```
CHR$(n)
```

Example:

```
CHR$(65)
"A"
```

#### HEX$

Returns the hexadecimal String equivalent of the given number.

Syntax:

```
HEX$(n)
```

Example:

```
HEX$(16)
"10"
```

#### OCT$

Returns the octal String equivalent of the given number.

Syntax:

```
OCT$(n)
```

Example:

```
OCT$(8)
"10"
```

#### INSTR

Returns the position (starting with 1) of the first occurrence of string y$ in string x$.
n is the start offset of the search position, starting at 1.
The default value of n is 1.
It returns 0, if n < LEN(x$), x$ is empty, or y$ is not found.
It returns n, if y$ is empty.

Syntax:

```
INSTR([n], x$, y$)
```

Example:

```
INSTR("12FOO34FOO", "FOO")
3
```

#### LEFT$

Returns the String with left-most n characters from string x$.

Syntax:

```
LEFT$(x$, n)
```

Example:

```
LEFT$("ABCD", 2)
"AB"
```

#### LEN

Returns the length of the String x$ in number of ASCII characters.

Syntax:

```
LEN(x$)
```

Example:

```
LEN("ABC")
3
```

#### MID$

Returns a String of m ASCII characters from the String x$ beginning at the nth character.

If m is omitted or is larger than remaining String length, all right-most characters beginning at n are returned.  
If n > LEN(x$) or m is 0, empty String is returned.

Syntax:

```
MID$(x$, n[, m])
```

#### RIGHT$

Returns the String with right-most n characters from string x$.

Syntax:

```
RIGHT(x$, n)
```

Example:

```
RIGHT("ABCD", 2)
"CD"
```

#### SPACE$

Returns a String with n spaces.

Syntax:

```
SPACE$(n)
```

#### STR$

Returns the String representation of the value n.

Syntax:

```
STR$(n)
```

#### STRING$

(First form) Returns a String of length n with all characters with the ASCII value j. 
(Second form) Returns a String of length n with all characters are the first character from x$. 
If the string is empty, an ILLEGAL_FUNCTION_PARAM runtime error is thrown.

Syntax:

```
STRING$(n, j)
STRING$(n, x$)
```

#### VAL

Converts a String containing a number to a numeric value.
It is the opposite of STR$ function.

Syntax:

```
VAL(x$)
```

### Packing and Unpacking Functions

#### Packing

These functions are used to pack numeric values to String before being written to a
Random Access File.

MKI$ packs Int32 to a 4 byte String.
MKL$ packs Int64 to a 8 byte String.
MKS$ packs Float32 to a 4 byte String.
MKD$ packs Float64 to a 8 byte String.

Syntax:

```
MKI$(n)
MKL$(n)
MKS$(n)
MKD$(n)
```

#### Unpacking

These functions are used to unpack numeric values from String after being read from a
Random Access File.

CVI unpacks Int32 from a 4 byte String. 
CVL unpacks Int64 from a 8 byte String. 
CVS unpacks Float32 from a 4 byte String. 
CVD unpacks FLoat64 from a 8 byte String. 

Syntax:

```
CVI(x$)
CVL(x$)
CVS(x$)
CVD(x$)
```

### ENVIRON$

Reads the String value for the given environment variable.

Syntax:

```
ENVIRON$(x$)
```

### TIMER

Returns number of seconds (Float64) elapsed since mid-night in the local time zone.

Syntax:

```
TIMER
```

### INPUT$

Reads n character String from the keyboard.
This function waits for ENTER key to be pressed and returns first n characters.

```
INPUT$(n)
```

### File handling functions

#### EOF

Returns -1 (true) or 0 (false) when end of file is reached
while reading a sequential file.
filenum is the file number.

Syntax:

```
EOF(filenum)
```

#### LOC

Returns the current position (in record number) in the file.
filenum is the file number.
For random access file, LOC returns the record number of last PUT ot GET.
For sequential access file, LOC returns the number of 128-byte block read or written.
LOC may not return exact number in case of the sequential access file.

Syntax:

```
LOC(filenum)
```

#### LOF

Returns the length of file in bytes.
filenum is the file number.

```
LOF(filenum)
```

## Statements

TBA
