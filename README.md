# PuffinBASIC
A cross-platform modern BASIC compiler/interpreter written in Java.

<img src="puffin.png" width="64"/>

BASIC (Beginners' All-purpose Symbolic Instruction Code) is a general-purpose high-level
language from the 1960s. PuffinBASIC is an implementation of the BASIC language specification.
PuffinBASIC conforms most closely to GWBASIC.

#### TESSEL - A 2D Tile Game
YouTube Video: <a href="https://youtu.be/L8xkM-g3Zms"><img src="samples/tessel/images/tesselsnap1.png" width="64"></a>
Source: <a href="samples/tessel/tessel.bas">tessel.bas</a>

#### FlyPuffinFly - A Scrolling Game
YouTube Video: <a href="https://youtu.be/bIIamQp9N3o"><img src="samples/puffingame/images/flypuffinflysnap.png" width="64"></a>
Source: <a href="samples/puffingame/flypuffinfly.bas">flypuffinfly.bas</a>

#### Conway's Game of Life
YouTube Video: <a href="https://youtu.be/TpsxQrxKqdg"><img src="samples/gameoflife/images/gameoflifesnap.png" width="64"></a>
<a href="https://youtu.be/A5tN6cBqoAc">#2</a>
Source: <a href="samples/gameoflife/gameoflife.bas">gameoflife.bas</a>

#### Mandelbrot Set
YouTube Video: <a href="https://youtu.be/7CukYv9tfXA"><img src="samples/mandelbrotsnap.png" width="64"></a>
<a href="https://youtu.be/xKp9U2BXTHM">Zoomed</a>
Source: <a href="samples/mandelbrot.bas">mandelbrot.bas</a>

## Why Implement BASIC?

1. Love and Familiarity: GWBASIC was my first programming language, I loved it and made many games in it. 
I'm also very familiar with the language.
1. Interpreter language: BASIC is an interpreted language and hence a good choice for implementing this interpreter.
1. Learning antlr4: I was learning antlr4 and BASIC was the perfect language to try because 
of its simple instruction set.
1. Resurrect BASIC: Implementations such as GWBASIC don't work on most modern platforms. PuffinBASIC works everywhere Java works.
1. Improve BASIC: I wanted to add modern graphics, better data types, etc.

## Version

0.1 experimental (NOT YET READY FOR PRODUCTION USE!)

The interpreter is not yet thoroughly tested and is likely to have bugs.
This is an active project so expect large changes at frequent intervals.

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
### Print prime numbers between 1 and 100

Line numbers are optional in PuffinBASIC.

```
FOR I% = 1 TO 100
  J% = 3
  N% = I% \ 2
  ISPRIME% = (I% > 1) AND ((I% MOD 2 <> 0) OR (I% = 2))
  WHILE J% <= N% AND ISPRIME% = -1
    ISPRIME% = I% MOD J% <> 0
    J% = J% + 2
  WEND
  IF ISPRIME% THEN PRINT STR$(I%), " is prime"
NEXT I%
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

### Graphics

```
10 SCREEN "PuffinBASIC 2D Graphics", 800, 600
20 LINE (100, 100) - (200, 200), "B"
30 FOR I% = 10 TO 50 STEP 10
40   CIRCLE (150, 150), I%, I%
50 NEXT I%
60 COLOR 255, 0, 0
70 LINE (200, 200) - (250, 300), "BF"
80 COLOR 0, 255, 255
90 FONT "Georgia", "bi", 32
100 DRAWSTR "Graphics with PuffinBASIC", 10, 400
110 DIM A%(101, 101)
120 GET (100, 100) - (201, 201), A%
130 PUT (250, 250), A%
140 DIM B%(32, 32)
150 LOADIMG "samples/enemy1.png", B%
160 FOR I% = 1 TO 5
170   PUT (400, 100 * I%), B%
180 NEXT
190 COLOR 255, 255, 0
200 DRAW "M600,400; UN50; RN50; DB50; F100"
210 COLOR 255, 255, 255
220 CIRCLE (700, 100), 10, 20
230 COLOR 255, 0, 255
240 PAINT (700, 100), 255, 255, 255
250 CIRCLE (700, 400), 50, 50, 0, 90
260 CIRCLE (700, 500), 50, 50, 90, 180, "F"
1000 SLEEP 5000
```

<img src="samples/puffin_graphics.png" width="512"/>

## Dependencies
JDK 11+, Maven, antlr4

## Build and test

```
$ mvn compile
$ mvn test
```

## Run using Maven
```
$ mvn exec:java -D"exec.args"="samples/graph.bas"
```

Graphics mode:
```
$ mvn exec:java -D"exec.args"="-g samples/graphics.bas"
```

## Working with Intellij

Import the pom.xml file in Intellij.
After importing, if you make a change to the antlr4 grammar,
regenerate the antlr4 code using following command and then reload the changes.
```
$ mvn generate-sources
```

## How it works?

1. PuffinBASIC's grammar is defined using antlr4.
1. At runtime, the user source code is parsed using antlr4 lexer+parser.
1. After parsing, an intermediate representation (IR) of the source code is generated. A symbol table keeps track of variables, scalars, arrays, etc. objects.
1. At runtime, the interpreter processes the IR instructions and executes them.

Since PuffinBASIC generates IR from source code and the runtime executes the IR, we can say PuffinBASIC is both a compiler and an interpreter.

## Planned Improvements

1. Add decimal data type.
1. ...

## Performance

PuffinBASIC is an interpreter, and it should not be expected to have very good performance characteristics.
Certain operations such as PRINT USING, INPUT, etc are not optimized for performance.
We have not benchmarked PuffinBASIC primitives.
That being said, we have written games with graphics in PuffinBASIC is work very well.

## Memory

PuffinBASIC runs within a JVM and can use as much memory as available for the JVM process.

# Reference

## Mode of operation

### Indirect Mode

1. Write a program in your favorite editor.
1. Save it in a text file with a '.bas' extension (not a requirement).
1. Use maven or PuffinBasicInterpreterMain to run the program.

PuffinBASIC supports indirect mode only.

### Line-number and No-line-number modes

PuffinBASIC programs can have one of two modes:
1. Line-number mode: All lines must start with a line number. GOTO/GOSUB statements can use both line number and label.
1. No-line-number mode: No line should start with a line number. GOTO/GOSUB statements can use label only.

## Compatibility

PuffinBASIC is mostly compatible with Microsoft's GWBASIC.
Graphics is supported using Java 2D graphics.

PuffinBASIC will not support assembly instructions.

### Input

PuffinBASIC aims for cross-platform compatibility and doesn't support platform specific features. 
Input statements and functions are line based and require 'ENTER' key to be pressed.
Same applies for the sequential file writes, print statements always output a line and input statements read the whole line.

### Case sensitivity

Operators, Statements and Standard Functions are case-insensitive.
Constants, variables and user defined functions are case-sensitive.

### Graphics

Graphics uses platform-independent Swing window.
Graphics functions are slightly different and more general than GWBASIC.
Graphics statements/functions require a '-g' flag to be set at runtime.
See Graphics section in reference.
PRINT/WRITE statements are displayed on standard out only.
For displaying text on Swing window, new statements are added.

### DIM

DIM statement declares size of each dimension (rather than maximum value of the dimension).

### Data Types

PuffinBASIC supports scalar, array, struct, list, set and dict types.
Int32, Int4, Float32, Float64, and String are the scalar types.
Each scalar type has a corresponding array type.

## Commands

PuffinBASIC does not support BASIC Commands, such as LIST, RUN, etc.

## Errors

PuffinBASIC can raise following kind of errors:
1. PuffinBasicSyntaxError: if source code has lexical or parsing error, e.g. missing parenthesis.
1. PuffinBasicSemanticError: if source code has a semantic error, e.g. data type mismatch. 
1. PuffinBasicRuntimeError: if a runtime error happens, e.g. division by zero, IO error, etc.
1. PuffinBasicInternalError: if there is a problem with PuffinBASIC implementation.

## Data Types

### Scalar Types

1. Int32 (32-bit signed integer): Int32 constants can have an optional '%' suffix.
Int32 constants can be decimal, octal or hexadecimal.
Octal numbers must have '&' or '&O' prefix, e.g. &12 or &O12.
Hexadecimal numbers must have '&H' prefix, e.g. &HFF.

1. Int64 (64-bit signed integer): Int64 constants must have '@' suffix.
Int64 constants can be decimal, octal or hexadecimal.

1. Float32 (32-bit signed IEEE-754 floating-point): Float32 constants can have an optional '!' suffix.
Float32 constants can use a decimal format or scientific notations, e.g. 1.2 or 1.2E-2.

1. Float64 (64-bit signed IEEE-754 floating-point): Float64 constants can have an optional '#' suffix.
Float32 constants can use a decimal format or scientific notations, e.g. 1.2 or 1.2E-2.

1. String: String stores any non-newline (or carriage return) ASCII character. 
A string must be enclosed within double-quotes, e.g. "A TEST, STRING".
There is no limit on the length of a string.

### Array Types

1. N-dimensional Int32 Array
1. N-dimensional Int64 Array
1. N-dimensional Float32 Array
1. N-dimensional Float64 Array
1. N-dimensional String Array

### Composite Types

1. Struct: User defined type composed of scalar, array, struct, list, set and dict types.
1. List: Variable length list of scalar and struct values.
1. Set: Unordered open hash-set of scalar values.
1. Dict: Unordered open hash-map of scalar to scalar and struct values.

### Type conversion

Numeric types are converted into one another via implicit type conversion.
String and numeric types are not implicitly converted. 
Use STR$ or VAL functions for conversion between String and numeric values.

## Variables

A variable name must start with a letter followed by one or more letters or numeric digits.
A variable name must not start with 'FN' because it is reserved for user defined functions.

A variable name has an optional suffix which sets the data type.
Int32 variable has '%' suffix, int64 variable has '@' suffix, float32 has '!' suffix,
float64 has '#' suffix, and String has '$' suffix. 
If a suffix is omitted, default data type assumed, the global default is Float64. 

There are three kinds of variables:
1. Scalar variable stores a single value, e.g. A%
1. Array variable stores a multi-dimensional array. An array variable must defined using DIM statement.
An array can have any number of dimensions. The minimum array index is 0 and maximum is dimension length - 1.
1. Composite variable such as struct, list, set and dict.

Example:
```
10 DIM A%(3, 4)
20 A%(1, 2) = 5
```

### Variable Reference

A variable can reference to another variable, i.e.
a scalar variable can reference to another scalar variable of same data type,
and an array variable can reference to another array variable of same data type.

Syntax:

```
REF variable => refVariable
```

Example:

```
A% = 10
REF A% => B%
B% = 3 ' Both A% and B% have value 3.

DIM X%(2,3)
DIM Y%(0,0)
REF X% => Y%
Y%(0,0) = 3 ' Both X%(0,0) and Y%(0,0) have value 3.
```

## Operators

Order of operations:

1. Arithmetic
1. Relational
1. Logical or bit-wise

Arithmetic:
1. '^'    Exponentiation
1. '-'    Unary minus
1. '*'    Multiplication
1. '/'    Floating point division
1. '\\'    Integer division
1. 'mod'  Modulus
1. '+'    Addition
1. '-'    Subtraction

Relational operators return -1 for true and 0 for false.
Relational operators work with both numbers and Strings.

Relational:
1. '='    Equals
1. '<>'   Not equals
1. '<'    Less than
1. '>'    Greate than
1. '<='   Less than or equal
1. '>='   Greater than or equal

If inputs to logical operators is -1 and 0, they return -1 for true and 0 for false.
Otherwise, logical operators work like bit-wise operators.
'>>' and '<<' are bitwise right-shift and left-shift operators.

Logical or bit-wise:
1. 'NOT'
1. 'AND'
1. 'OR'
1. 'XOR'
1. 'EQV'
1. 'IMP'
1. '>>'
1. '<<'

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

LOG returns the natural logarithm of a numeric value.
LOG10 returns base-10 logarithm of a numeric value.
LOG2 returns base-2 logarithm of a numeric value.
n must be greater than 0.

Syntax:

```
LOG(n)
LOG10(n)
LOG2(n)
```

#### EXP

Returns E raised to the power of n.

Syntax:

```
EXP(n)
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

#### EULERE

Returns the value of Euler's number E.

Syntax:

```
EULERE()
```

#### PI

Returns the value of PI.

Syntax:

```
PI()
```

#### FLOOR, CEIL, ROUND

Syntax:

```
FLOOR(n)
CEIL(n)
ROUND(n)
```

#### MIN

Calculates min of n and m.
n and m must be numeric values.

Syntax:

```
MIN(n, m)
```

#### MAX

Calculates max of n and m.
n and m must be numeric values.

Syntax:

```
MAX(n, m)
```

#### Trigonometric Functions

SIN, COS and TAN take angle in radians and return sine, cosine and tangent of that angle.
To compute angle in radians, multiply the angle in degrees with pi/180.

ASIN, ACOS and ATN takes a numeric value, computes arc sin, arc cos, arc tangent of the value and returns the angle in radians. 
To compute angle is degrees, multiply the angle in radians with 180/pi.

SINH, COSH and TANH are hyperbolic sin, hyperbolic cos and hyperbolic tan functions respectively.

TORAD converts degrees to radians.
TODEG converts radians to degrees.

Syntax:

```
SIN(radians)
COS(radians)
TAN(radians)
ASIN(value)
ACOS(value)
ATN(value)
SINH(radians)
COSH(radians)
TANH(radians)
TORAD(degress)
TODEG(radians)
```

### Numeric Conversion Functions

These functions are used for numeric type conversion.
CINT(n) converts the given numeric value to int32 with bounds check.
CLNG(n) converts the given numeric value to int64 with bounds check.
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

### Array Functions

#### ARRAY1DMIN, ARRAY1DMAX, ARRAY1DMEAN, ARRAY1DSUM, ARRAY1DSTD, ARRAY1DMEDIAN, ARRAY1DPCT

Compute summary and descriptive statistics on the given 1-dimensional array.
Syntax:

```
ARRAY1DMIN(arrayvariable)     ' min
ARRAY1DMAX(arrayvariable)     ' max
ARRAY1DMEAN(arrayvariable)     ' mean
ARRAY1DSUM(arrayvariable)      ' sum
ARRAY1DSTD(arrayvariable)      ' standard deviation
ARRAY1DMEDIAN(arrayvariable)   ' median
ARRAY1DPCT(arrayvariable, pct) ' percentile, where pct=0-100
```

Example:

```
ARRAY1DMIN(C%)
ARRAY1DMAX(C%)
ARRAY1DMEAN(C%)
ARRAY1DSUM(C%)
ARRAY1DSTD(C%)
ARRAY1DMEDIAN(C%)
ARRAY1DPCT(C%, 90)
```

#### ARRAY1DBINSEARCH

Search the value x in the given 1-dimensional array variable.
If the value x is found, the index is returned.
If the value is not found, (-(insertion point) - 1) is returned.

Syntax:

```
ARRAY1DBINSEARCH(arrayvariable, x)
```

Example:

```
ARRAY1DBINSEARCH(C%, 2)
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

### HSB2RGB

Converts HSB color float values (each 0-1) to an int32 RGB values.

Syntax:

```
HSB2RGB(hue, saturation, brightness)
```

Example:

```
HSB2RGB(0.5, 1.0, 1.0)
```

### Mouse Functions

Mouse functions work in graphics mode only.

Mouse x/y functions returns x/y coordinate relative to the window.

Mouse button functions return the button number. 
They return -1 if no button was pressed since last function call.

Syntax:

```
MOUSEMOVEDX()
MOUSEMOVEDY()
MOUSEDRAGGEDX()
MOUSEDRAGGEDY()
MOUSEBUTTONCLICKED()
MOUSEBUTTONPRESSED()
MOUSEBUTTONRELEASED()
```

## Statements

### Line

Source code consists of multiple lines.

Each line starts with an optional integer line number.
The line number is followed by one or more statements and ends with an optional comment.
Multiple statements in a line can be separated by colon (':').

```
10 LET A = 1 : PRINT A REM COMMENT
20 REM COMMENT
``` 

### Comments

A comment starts with REM or single quote ("'").

Syntax:
```
REM USER TEXT
' COMMENT TEXT
```

### Variables

#### Assignment

Scalar variables are declared and assigned value using LET statement.
The LET keyword is optional.

Syntax:

```
LET variable = expr
variable = expr
```

Example:

```
LET A% = 1
B$ = "ABC"
```

#### Reference Assignment

AUTO can be used to reference the result of an expression
without specifying the type of the reference variable or
allocating the variable.

Syntax:

```
AUTO refvar = expr
```

Example:

```
AUTO keys = dict1.keys()
```

#### Arrays

Use DIM keyword to declare an array variable.
dim1, dim2, ... are dimension size (and not max dim value.)
Arrays are stored in row-major order.
The minimum index in an array dimension is 0 and maximum is dim size - 1.

Syntax:

```
DIM variable(dim1, dim2, ...)
```

Example:

```
DIM A%(3, 5)
```

The above statements declares a 3x5 Int32 variable.

#### Struct

User defined type composed of scalar, array, struct, list, set and dict types

Syntax:

```
STRUCT typename { members } ' Define the struct type.
typename varname {}         ' Create an instance of the type.
```

Example 1: Example of scalar types.

```
STRUCT struct1 { A% , B% }
struct1 s1 {}
s1.A% = 2
s2.A% = 10
PRINT s1.A%, s2.A%
```

Example 2: Nested struct.

```
STRUCT struct2 { C%, struct1 child }
struct2 s3 {}
s3.child.A% = 100
s3.C% = 50
PRINT s3.child.A%, s3.C%
```

Example 3: Example of all data types.

```
STRUCT struct4 { A%, B@, C!, D#, E$, DIM ARR%(2,3), LIST<$> l1, SET<%> s1, DICT<%, #> d1 }
struct4 s41 {}
s41.A% = 1
s41.B@ = 2
s41.C! = 3
s41.D# = 4
s41.E$ = "str"
s41.ARR%(1, 1) = 5
s41.l1.append("abc")
s41.s1.add(23)
s41.d1.put(10, 2.5)
PRINT s41.A%, s41.B@, s41.C!, s41.D#, s41.E$, s41.ARR%(1, 1), s41.l1.get(0), s41.s1.contains(23), s41.d1.getOrDefault(10, 0)
```

#### List

A variable length list of scalar and struct values.

TODO: struct value is not tested yet.

Syntax:

```
LIST<DATATYPE|STRUCT> varname

Supported functions:
varname.append(VALUE) ' Append the value to the list.
varname.get(INDEX)    ' Get the value at the given index.
varname.insert(INDEX, VALUE) ' Insert the value at the given index.
varname.values()      ' Get the array of values.
varname.clear()       ' Clear the list.
LEN(varname)          ' Get the length of the list.
```

Example:

```
LIST<$> list1
list1.append("a")
list1.append("b")
PRINT list1.get(0)

auto l1val = list1.values()
FOR I% = 0 TO LEN(l1val) - 1
  PRINT l1val(I%),
NEXT : PRINT ""

list1.insert(0, "c")
list1.clear()
```

#### Set

An unordered open hash-set of scalar values.

Syntax:

```
SET<DATATYPE> varname

Supported functions:
varname.add(VALUE)      ' Add the VALUE to the set.
varname.contains(VALUE) ' Check if the VALUE exists in the set.
varname.values()        ' Get the array of unordered values.
varname.remove(VALUE)   ' Remove the VALUE from the set if it exists.
varname.clear()         ' Clear the set.
LEN(varname)            ' Get the length of the set.
```

Example:

```
SET<$> set1

set1.add("a")
PRINT set1.contains("a")

auto s1val = set1.values()
FOR I% = 0 TO LEN(s1val) - 1
  PRINT s1val(I%),
NEXT : PRINT ""

set1.remove("a")
set1.clear()
PRINT LEN(set1)
```

#### Dict

An unordered open hash-map of scalar key to scalar/struct values.

TODO: struct values are not tested yet.

Syntax:

```
DICT<KEYTYPE, VALUETYPE|STRUCT> varname

Supported functions:
varname.put(KEY, VALUE)  ' Put the KEY/VALUE pair in the dict, overwriting any previous value.
varname.getOrDefault(KEY, DEFAULT_VALUE)  ' Get the value of the KEY if present, DEFAULT_VALUE if absent.
varname.removeKey()      ' Remove the KEY from the dict if it exists.
varname.containsKey()    ' Check if the KEY exists in the dict.
varname.keys()           ' Get the array of keys in the dict.
varname.clear()          ' Clear the dict.
LEN(varname)             ' Get the length of the dict.
```

Example:

```
DICT<$,%> dict1

dict1.put("a", 65)

auto d1val = dict1.keys()
FOR I% = 0 TO LEN(d1val) - 1
  PRINT d1val(I%),
NEXT : PRINT ""

PRINT dict1.remove("a")
PRINT dict1.getOrDefault("a", -1)
PRINT dict1.containsKey("a")

dict1.clear()
PRINT LEN(dict1)
```

### Default Variable Data Type

The following keywords can be used to declare default data type of a variable,
(used when a variable does not have a suffix).

Syntax:

```
DEFINT letter-letter[, letter-letter]
DEFLNG letter-letter[, letter-letter]
DEFSNG letter-letter[, letter-letter]
DEFDBL letter-letter[, letter-letter]
```

Example:

```
DEFINT A-C
```

The above example declares variables starting with A, B and C as Int32.

### Control Statements

#### IF-THEN-ELSE

In case of nested IF-THEN-ELSE, ELSE matches the closest IF statement.

Syntax:

```
IF expression THEN statements ELSE statements
```

Example:

```
IF A > 1 THEN PRINT "A > 1" ELSE PRINT "A <= 1"
```

#### IF-GOTO-ELSE

Syntax:

```
IF expression GOTO linenumber ELSE statements
```

Example:

```
20 IF A > 1 GOTO 100 ELSE 200
```

#### IF-THEN-BEGIN-ELSE-BEGIN-END_IF

PuffinBASIC supports then/else blocks with IF statement.

Syntax:

```
IF expr THEN BEGIN
  stmt1
  stmt2
  ...
ELSE BEGIN
  stmtk
  stmtk+1
  ...
END IF
```

Example:

```
IF A% < 20 THEN BEGIN
  PRINT A%, "LT 20"
ELSE BEGIN
  PRINT A%, "GE 20"
END IF
```

#### LABEL

Create a label.
GOSUB/GOTO can be used with the label name.

Syntax:

```
LABEL "labelname"
```

Example:

```
LABEL "DRAW A LINE"
```

#### GOTO

Jump to the given line number.
In no-linenumber mode, GOTO can be used with label.

Syntax:

```
20 GOTO 100

GOTO "label"
```

#### FOR-NEXT-STEP

For loop.

Syntax:

```
FOR variable = expression TO expression [STEP expression]
...
NEXT [variable]
```

Example:

```
10 FOR I% = 1 TO 10 STEP 2
20 PRINT I%
30 NEXT I%
```

#### WHILE-WEND

While loop.

Syntax:

```
WHILE expression
...
WEND
```

Example:

```
20 WHILE A < 10
30 A = A + 1
40 PRINT A
50 WEND
```

#### END

Marks the end of the program.

#### GOSUB-RETURN

Goto the subroutine. After subroutine finishes (on RETURN statement), 
the program execution returns to the statement after GOSUB.
Subroutines should be defined at the end of the program.
Subroutines share the same scope as the main program.

In no-linenumber mode, GOSUB can be used with label.

Syntax:

```
GOSUB linenum
RETURN [linenum]

GOSUB "label"
RETURN 
```

Example:

```
...
20 GOSUB 110
...
100 END
110 REM subrouting 1
...
200 RETURN
```

```
...
GOSUB "sub1"
...
END

LABEL "sub1"
...
RETURN
```

### User Defined Functions

A UDF executes an expression.
The UDF returns the result of the expression.
The UDF can declare local scoped parameters.
Parameters are locally scoped but UDF can access variables from global scope.
Recursive UDF is not supported.

Syntax:

```
DEF variable(variables) = expr
```

The variable name must start with 'FN' and may have a suffix to declare the return data type.


Example:

```
DEF FNsquare%(X%) = X% * X%
```

The above example computes the square of Int32 parameter X%.

### READ-DATA-RESTORE

DATA keyword is used to define constant values.
The READ keyword is used to read the constant values sequentially.
When all the values are read, the read cursor can be reset by using RESTORE statement.

Syntax:

```
DATA constants
...
DATA constants

READ variables
RESTORE
READ variables
```

Example:

```
10 DATA "STRING", 2, 5.2
20 READ A$, B%, C#
30 RESRORE
40 READ A$, B%, C#
```

### Input Output

#### PRINT

Print the expressions to standard out.

Syntax:

```
PRINT expressions
```

The expressions can be either separated by comma or semi-colon.

Example:

```
PRINT "AB", 1, 2
PRINT "AB"; 1; 2
PRINT "AB", 1, 2,
```

If there is no comma at the end of PRINT statement, a new-line is printed.
If there is a comma at the end of PRINT statement, no new-line is printed.

#### PRINT USING

Formats each expression using the given format and prints to standard out.

Syntax:

```
PRINT USING format; expressions
```

The format is a string expression.

##### Formatting String expressions
- '!' Prints the first character of each String expression.
- '&' Prints the entire String for each String expression.
- '\\n spaces\\' Prints n+2 characters from each String expression.

##### Formatting numeric expressions
- '#' specifies 1 digit position.
- '.' specifies decimal point.
- ',' adds comma in formatted number.
      
###### First optional prefix:
- '+' prefix will add a sign prefix.
- '-' prefix will add a minus prefix for negative number.
 
###### Next optional prefix:
- '\*\*' causes leading spaces to be filled with '*' and specifies 2 more digit positions.
- '\*\*$' adds dollar prefix, causes leading spaces to be filled with '*' and specifies 2 more digit positions.
- '$$' add dollar prefix and specifies 1 more difit position.
      
###### First optional suffix:
- '+' suffix will add a sign suffix.
- '-' suffix will add a minus suffix for negative number.
      
###### Next optional suffix:
- '^^^^' suffix indicates scientific notation.

Examples:

```
PRINT USING "\ \"; "123456"; "abcdef"
PRINT using "###.##"; 2.3
PRINT using "**###.##"; 2.3
PRINT using "**$###.##"; 2.3
```

#### WRITE

Prints expressions on standard out.
It separates each expression with a comma.
Strings are surrounded with double quotes.

#### INPUT

Reads user input from standard in and assigns to variables.

Syntax:

```
INPUT [prompt ;] variables
```

Since PuffinBASIC is platform independent, user must press ENTER to mark the end of input.
After reading a line from standard in, INPUT will split the line using commas
into separate values (line a CSV line) and assign to each variable.
If the number of values don't match number of variables, the user will be
asked to enter the values again.

Example:

```
INPUT "Enter two numbers: "; A%, B%
```

#### LINE INPUT

Reads one line from standard in.

Syntax:

```
LINE INPUT [prompt ;] stringVariable
```

Example:

```
LINE INPUT "Enter a line: "; A$
```

### File Handling

#### Random Access Files

Random access file allows reading and writing data in fixed length records.
The default record length is 128.

Syntax:

```
OPEN "R", #filenum, filename[, recordlen]
OPEN filename FOR RANDOM AS #filenum LEN=recordlen
FIELD#filenum, int as variable, int as variable, ...
LSET variable = expr
...
PUT#filenum, recordnum
GET#filenum, recordnum
CLOSE#filenum
```

Example:

```
30 OPEN "R", #1, FILENAME$, 24
40 FIELD#1, 8 AS A$, 8 AS B$, 8 AS C$
50 FOR I% = 1 TO 5
60 LSET A$ = MKI$(I%)
70 LSET B$ = MKI$(I% + 1)
80 LSET C$ = MKI$(I% + 2)
90 PUT #1
100 PRINT LOC(1), LOF(1)
110 NEXT I%
120 FOR I% = 1 TO 5
130 GET #1, I% - 1
140 PRINT A$, B$, C$, LOC(1), LOF(1)
150 NEXT
160 CLOSE
```

#### Sequential Access Files

Syntax:

```
OPEN "O", #filenum, filename
OPEN "A", #filenum, filename
OPEN "I", #filenum, filename

OPEN filename FOR OUTPUT AS #filenum
OPEN filename FOR APPEND AS #filenum
OPEN filename FOR INPUT AS #filenum

WRITE#filenum, expr, expr, ...
PRINT#filenum, expr, expr, ...

CLOSE#filenum
```

When writing a sequential file, prefer using WRITE# over PRINT# statements.

Example: Writing a sequential file

```
20 OPEN "O", #1, FILENAME$
30 FOR I% = 1 TO 5
40 WRITE#1, "ABC" + STR$(I%), 123 + I%, 456@ + I%, 1.2 + I%
50 NEXT
60 FOR I% = 1 TO 5
70 PRINT#1, CHR$(34), "ABC" + STR$(I%), CHR$(34), ",", 123 + I%, ",", 456@ + I%, ",", 1.2 + I%
80 NEXT
90 CLOSE #1
```

Example: Reading a sequential file

```
100 OPEN FILENAME$ FOR INPUT AS #1
110 FOR I% = 1 TO 10
120 INPUT#1, A$, B%, C@, D#
130 PRINT A$, B%, C@, D#
140 NEXT
150 CLOSE
```

### DATE TIME

#### DATE$

System date can be read using DATE$ (like a variable).
Date can be set to a String (for the duration of program only).

Syntax:

```
v$ = DATE$
DATE$ = "YYYY-mm-dd"
```

#### TIME$

System time can be read using TIME$ (like a variable).
Time can be set to a String (for the duration of program only).

```
TIME$ = "HH:MM:SS"
v$ = TIME$
```

### MID$

### RANDOMIZE

Sets the random seed.

Syntax:

```
RANDOMIZE expr
RANDOMIZE TIMER
```

The expression is an Int64 expression.
TIMER uses the current time in seconds.

Example:

```
RANDOMIZE 1002
```

### SLEEP

Sleep for given number of milliseconds.

Syntax:

```
SLEEP n
```

## ARRAY

### ARRAYFILL

Fill an n-dimensional array with the given value.

Syntax:

``
ARRAYFILL arrayvariable, value
``

Example:

```
ARRAYFILL A%, 10
```

### ARRAYCOPY

Copy values in source array variable to values in destination array variable.
The data types and total number of elements in the array variables must match.

Syntax:

``
ARRAYCOPY srcarrayvariable, dstarrayvariable
``

Example:

```
ARRAYFILL A%, B%
```

### ARRAY1DCOPY

Copy value from source array variable to destination array variable.
Both must be 1D array.

srcOrigin is from where to start copy in the source.
dstOrigin is to where to start copy in the destination.
lengthToCopy is the number of elements to copy.

Syntax:

```
ARRAY1DCOPY srcvariable, srcOrigin, dstvariable, dstOrigin, lengthToCopy
```

Example:

```
ARRAY1DCOPY F%, 1, F%, 3, 2
```

### ARRAY1DSORT

Sorts (in-place) the values in the given 1-dimensional array variable.

Syntax:

``
ARRAY1DSORT arrayvariable
``

Example:

```
ARRAY1DSORT A%
```

### ARRAY2DSHIFTVER

Shift rows up or down by given n rows in the given 2D array.
The empty space (rows) is filled with 0 or empty string.
n >= 0 means shift rows down.
n < 0 means shift rows up.
This is a very efficient operation.

Syntax:

```
ARRAY2DSHIFTVER arrayvariable, n
```

Example:

```
ARRAY2DSHIFTVER D%, 2
ARRAY2DSHIFTVER D%, -3
```

### ARRAY2DSHIFTHOR

Shift columns up or down by given n columns in the given 2D array.
The empty space (columns) is filled with 0 or empty string.
n >= 0 means shift columns down.
n < 0 means shift columns up.
This operation should be faster than doing it with loops, but not as efficient as ARRAY2DSHIFTVER.

Syntax:

```
ARRAY2DSHIFTHOR arrayvariable, n
```

Example:

```
ARRAY2DSHIFTHOR D%, 2
ARRAY2DSHIFTHOR D%, -3
```

## Graphics

Use '--graphics' or '-g' to enable graphics mode.

### SCREEN

Create a window with the title and a drawing canvas of size wxh (width x height).
The window is not resizable.
Top left of the drawing canvas is 0,0 and bottom right is w,h.
If MANUALREPAINT is set, the drawing canvas is not repainted automatically,
and REPAINT must be invoked manually (and periodically in a game loop).

Syntax:

```
SCREEN title$, w, h[, MANUALREPAINT]
```

Example:

```
SCREEN "PuffinBASIC 2D Graphics", 800, 600
```

### COLOR

Sets foreground color in the graphics context
using red, green and blue color components.
Each color component is an Int32 ranging from 0 to 255.

Syntax:

```
COLOR r, g, b
```

Example:

```
COLOR 0, 255, 0
```

### FONT

Sets the font name with given options and font size.
options$ is a String: "i" means Italic, "b" means bold. 
Multiple options can be combined into a String.

Syntax:

```
FONT name$, options$, size
```

Example:

```
FONT "Georgia", "bi", 50
```

### DRAWSTR

Draws the given string at given position on the drawing canvas.

Syntax:

```
DRAWSTR text$, x, y
```

Example:

```
DRAWSTR "SAMPLE Text", 100, 400
```

### PSET

Draws a point on the given position.
An optional color can be specified.
If no color is specified, color is picked from the graphics context.

Syntax:

```
PSET (x, y) [, r, g, b]
```

Example:

```
PSET (100, 100)
```

### CIRCLE

Draws an oval at position x, y with radii of r1 and r2.
If start angle (degrees) and end angle (degrees) are specified,
an arc is draw (clockwise).
To fill the circl with foreground color, set options as "F".

Syntax:

```
CIRCLE (x, y), r1, r2[, start_angle?, end_angle?[, options]]
```

Example:

```
CIRCLE (100, 200), 50, 50
CIRCLE (100, 200), 50, 50, 0, 90
CIRCLE (100, 200), 50, 50, 90, 180, "F"
```

### LINE

Draw a line from position1 (x1, y1) to position2 (x2, y2).
Options can be "B" or "BF".
If "B" is used, a box is drawn.
If "BF" is used, a filled box is drawn.

Syntax:

```
LINE (x1, y1) - (x2, y2) [, options]
```

Example:

```
LINE (0, 0) - (10, 10), "BF"
```

### PAINT

Flood fills the drawing canvas starting at the given position with foreground color
until the given color boundary is hit.
Flood fill has no effect if called on a point which already has foreground color.
PAINT is a slow operation.
Prefer filling the shapes using "F" option.

Syntax:

```
PAINT (x, y), border_r, border_g, border_b
```

Example:

```
PAINT (155, 155), 255, 255, 255
```

### DRAW

Draw an arbitrary path. 
The path starts at middle of the screen.

Following instructions are supported:
```
Un:   up n pixels
Dn:   down n pixels
Ln:   left n pixels
Rn:   right n pixels
En:   (diagonal) up and right n pixels each
Fn:   (diagonal) down and right n pixels each
Gn:   (diagonal) down and left n pixels each
Hn:   (diagonal) up and left n pixels each
B:    pen up (i.e. move only);
        it can be added to any of above instructions.
N:    return to original position after drawing; 
        can be added to any of above instructions.
Mx,y: move to x,y (absolute or relative). 
        If x and y have +/- prefix, 
        move is relative to curren position, 
        otherwise, move is to absolute position.
```
The instructions are separated by a semi-colon.

Syntax:

```
DRAW path$
```

Example:

```
DRAW "U30; E30; R30; F30; DNB30; R30; M+30,+30; R30; R50"
```

### GET

Copy drawing canvas contents from x1,y1 to x2,y2 to given array variable.
The variable must be of Int32 type.
The array dimensions must match x2-x1,y2-y1.
x1,y1 is inclusive.
x2,y2 is exclusive.

x1,y1 and x2,y2 must be within the bounds of the drawing canvas.

Syntax:

```
GET (x1, y2) - (x2, y2), variable
```

Example:

```
DIM A%(32, 32)
GET (0, 0) - (32, 32), A%
```

### PUT

Copy array variable contents to the drawing canvas at x,y position.
The variable must be of Int32 type.
x,y must be within the bounds of the drawing canvas.

Mode can be XOR, OR, AND, PSET (overwrite), and MIX (overwrite opaque parts only).

Syntax:

```
PUT (x, y), variable[, mode]
```

Example:

```
DIM A%(32, 32)
GET (0, 0) - (32, 32), A%
PUT (100, 100), A%
```

### LOADIMG

Load an image into the given array variable.
The variable must be of Int32 type.
The array dimensions must match the image dimensions.
Common formats such as png, jpeg, gif, bmp are supported.

Syntax:

```
LOADIMG image, variable
```

Example:

```
DIM A%(32, 32)
LOADIMG "enemy1.png", A%
```

### SAVEIMG

Save an image from the given array variable.
The variable must be of Int32 type.
Common formats such as png, jpeg, gif, bmp are supported.

Syntax:

```
SAVEIMG image, variable
```

Example:

```
DIM A%(32, 32)
...
SAVEIMG "enemy1.png", A%
```

### INKEY$

Read one key pressed on keyboard.

ASCII characters are returned as single byte Strings.

Special characters, e.g. UP arrow, DOWN arrow, 
are returned as two byte String - the first byte is always byte 0.

Special Key Codes:
```
LEFT arrow:  CHR$(0) + CHR$(37)
UP arrow:    CHR$(0) + CHR$(38)
RIGHT arrow: CHR$(0) + CHR$(39)
DOWN arrow:  CHR$(0) + CHR$(40)
```

Syntax:

```
INKEY$
```

Example:

```
K$ = INKEY$

' Check Up Arrow
IF K$ = CHR$(0) + CHR$(38) THEN y2% = y% - 5
```

### CLS

Clear the screen.

Syntax:

```
CLS
```

### BEEP

Make a beeps sound.

Syntax:

```
BEEP
```

### REPAINT

Repaint the drawing canvas. Use this when automatic repaint is off.

Syntax:

```
REPAINT
```

### LOADWAV

Load sound (wav) file.

Syntax:

```
LOADWAV path, variable
```

### PLAYWAV

Play sound, loaded earlier via the LOADWAV statement.

Syntax:

```
PLAYWAV variable
```

### STOPWAV

Stop the playing sound.

Syntax:

```
STOPWAV variable
```

### LOOPWAV

Loop (continuously) the sound, loaded earlier via the LOADWAV statement.

Syntax:

```
LOOPWAV variable
```
