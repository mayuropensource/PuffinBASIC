grammar BeagleBasic;

prog
    : (line | NEWLINE)*
    ;

line
    : linenum stmtlist? comment? NEWLINE
    ;

linenum
    : DECIMAL
    ;

comment
    : COMMENT
    ;

COMMENT
    : (REM | APOSTROPHE) ~[\r\n]*
    ;

stmt
    : printusingstmt
    | printhashusingstmt
    | printstmt
    | printhashstmt
    | writestmt
    | writehashstmt
    | letstmt
    | ifstmt
    | forstmt
    | nextstmt
    | gotostmt
    | endstmt
    | deffnstmt
    | dimstmt
    | gosubstmt
    | returnstmt
    | whilestmt
    | wendstmt
    | lsetstmt
    | rsetstmt
    | swapstmt
    | open1stmt
    | open2stmt
    | closestmt
    | putstmt
    | getstmt
    | fieldstmt
    | inputstmt
    | inputhashstmt
    | lineinputstmt
    | lineinputhashstmt
    | readstmt
    | datastmt
    | restorestmt
    | randomizestmt
    | randomizetimerstmt
    | defintstmt
    | deflngstmt
    | defsngstmt
    | defdblstmt
    | defstrstmt
    | middlrstmt
    ;

expr
    : (PLUS | MINUS)? func      # ExprFunc
    | (PLUS | MINUS)? number    # ExprNumber
    | (PLUS | MINUS)? variable  # ExprVariable
    | LPAREN expr RPAREN        # ExprParen
    | string                    # ExprString
    | expr EXPONENT expr        # ExprExp
    | expr MUL expr             # ExprMul
    | expr FLOAT_DIV expr       # ExprFloatDiv
    | expr INT_DIV expr         # ExprIntDiv
    | expr MOD expr             # ExprMod
    | expr PLUS expr            # ExprPlus
    | expr MINUS expr           # ExprMinus
    | expr RELEQ expr           # ExprRelEq
    | expr RELNEQ expr          # ExprRelNeq
    | expr RELLT expr           # ExprRelLt
    | expr RELGT expr           # ExprRelGt
    | expr RELLE expr           # ExprRelLe
    | expr RELGE expr           # ExprRelGe
    | LOGNOT expr               # ExprLogNot
    | expr LOGAND expr          # ExprLogAnd
    | expr LOGOR expr           # ExprLogOr
    | expr LOGXOR expr          # ExprLogXor
    | expr LOGEQV expr          # ExprLogEqv
    | expr LOGIMP expr          # ExprLogImp
    ;

func
    : ABS  LPAREN expr RPAREN                               # FuncAbs
    | ASC  LPAREN expr RPAREN                               # FuncAsc
    | SIN  LPAREN expr RPAREN                               # FuncSin
    | COS  LPAREN expr RPAREN                               # FuncCos
    | TAN  LPAREN expr RPAREN                               # FuncTan
    | ATN  LPAREN expr RPAREN                               # FuncAtn
    | SQR  LPAREN expr RPAREN                               # FuncSqr
    | CINT LPAREN expr RPAREN                               # FuncCint
    | CLNG LPAREN expr RPAREN                               # FuncClng
    | CSNG LPAREN expr RPAREN                               # FuncCsng
    | CDBL LPAREN expr RPAREN                               # FuncCdbl
    | CHRDLR LPAREN expr RPAREN                             # FuncChrDlr
    | MKIDLR LPAREN expr RPAREN                             # FuncMkiDlr
    | MKLDLR LPAREN expr RPAREN                             # FuncMklDlr
    | MKSDLR LPAREN expr RPAREN                             # FuncMksDlr
    | MKDDLR LPAREN expr RPAREN                             # FuncMkdDlr
    | CVI LPAREN expr RPAREN                                # FuncCvi
    | CVL LPAREN expr RPAREN                                # FuncCvl
    | CVS LPAREN expr RPAREN                                # FuncCvs
    | CVD LPAREN expr RPAREN                                # FuncCvd
    | SPACEDLR LPAREN expr RPAREN                           # FuncSpaceDlr
    | STRDLR LPAREN expr RPAREN                             # FuncStrDlr
    | VAL LPAREN expr RPAREN                                # FuncVal
    | INT LPAREN expr RPAREN                                # FuncInt
    | FIX LPAREN expr RPAREN                                # FuncFix
    | LOG LPAREN expr RPAREN                                # FuncLog
    | LEN LPAREN expr RPAREN                                # FuncLen
    | HEXDLR LPAREN expr RPAREN                             # FuncHexDlr
    | OCTDLR LPAREN expr RPAREN                             # FuncOctDlr
    | RIGHTDLR LPAREN expr COMMA expr RPAREN                # FuncRightDlr
    | LEFTDLR LPAREN expr COMMA expr RPAREN                 # FuncLeftDlr
    | MIDDLR LPAREN expr COMMA expr (COMMA expr)? RPAREN    # FuncMidDlr
    | INSTR LPAREN expr COMMA expr (COMMA expr)? RPAREN     # FuncInstr
    | RND                                                   # FuncRnd
    | SGN LPAREN expr RPAREN                                # FuncSgn
    | TIMER                                                 # FuncTimer
    | STRINGDLR LPAREN expr COMMA expr RPAREN               # FuncStringDlr
    | EOFFN LPAREN expr RPAREN                              # FuncEof
    | LOC LPAREN expr RPAREN                                # FuncLoc
    | LOF LPAREN expr RPAREN                                # FuncLof
    | INPUTDLR LPAREN expr (COMMA HASH? expr)? RPAREN       # FuncInputDlr
    ;

gosubstmt
    : GOSUB linenum
    ;

returnstmt
    : RETURN linenum?
    ;

printhashusingstmt
    : PRINTHASH filenum=expr COMMA (USING format=expr SEMICOLON)? printlist
    ;

printusingstmt
    : PRINT USING format=expr SEMICOLON printlist
    ;

printhashstmt
    : PRINTHASH filenum=expr COMMA (USING format=expr SEMICOLON)? printlist
    ;

printstmt
    : (QUESTION | PRINT) printlist?
    ;

printlist
    : expr (COMMA | SEMICOLON | expr)*
    ;

writestmt
    : WRITE (expr (COMMA expr)*)?
    ;

writehashstmt
    : WRITEHASH filenum=expr COMMA expr (COMMA expr)*
    ;

letstmt
    : LET? variable RELEQ expr
    ;

variable
    : varname varsuffix? (LPAREN expr (COMMA expr)* RPAREN)?
    ;

varname
    : VARNAME
    ;

varsuffix
    : DOLLAR | PERCENT | AT | EXCLAMATION | HASH
    ;

ifstmt
    : IF expr COMMA? then COMMA? (ELSE elsestmt)?  # IfThenElse
    ;

then
    : (THEN (linenum | stmtlist)) | (GOTO linenum)
    ;

elsestmt
    : linenum | stmtlist
    ;

stmtlist
    : stmt (':' stmt)*
    ;

forstmt
    : FOR variable RELEQ expr TO expr (STEP expr)?
    ;

nextstmt
    : NEXT variable? (COMMA variable)*
    ;

gotostmt
    : GOTO linenum
    ;

endstmt
    : END
    ;

deffnstmt
    : DEF varname varsuffix? (LPAREN (variable (COMMA variable)*)? RPAREN)? RELEQ expr
    ;

dimstmt
    : DIM varname varsuffix? LPAREN DECIMAL (COMMA DECIMAL)* RPAREN
    ;

whilestmt
    : WHILE expr
    ;

wendstmt
    : WEND
    ;

lsetstmt
    : LSET variable RELEQ expr
    ;

rsetstmt
    : RSET variable RELEQ expr
    ;

swapstmt
    : SWAP variable COMMA variable
    ;

open1stmt
    : OPEN filemode1 COMMA HASH? filenum=DECIMAL COMMA filename=expr (COMMA reclen=expr)?
    ;

open2stmt
    : OPEN filename=expr (FOR filemode2)? (ACCESS access)? lock? AS HASH? filenum=DECIMAL (LEN RELEQ reclen=expr)?
    ;

closestmt
    : CLOSE (HASH? DECIMAL (COMMA HASH? DECIMAL)*)?
    ;

filemode1
    : STRING
    ;

filemode2
    : INPUT | OUTPUT | APPEND | RANDOM
    ;

access
    : READ | WRITE | READ WRITE
    ;

lock
    : SHARED | LOCK READ | LOCK WRITE | LOCK READ WRITE
    ;

putstmt
    : PUT HASH? filenum=DECIMAL (COMMA expr)?
    ;

getstmt
    : GET HASH? filenum=DECIMAL (COMMA expr)?
    ;

fieldstmt
    : FIELD HASH? filenum=expr COMMA DECIMAL AS variable (COMMA DECIMAL AS variable)*
    ;

inputstmt
    : INPUT SEMICOLON? (expr (SEMICOLON | COMMA)) variable (COMMA variable)*
    ;

inputhashstmt
    : INPUTHASH filenum=expr COMMA variable (COMMA variable)*
    ;

lineinputstmt
    : LINE INPUT SEMICOLON? (expr SEMICOLON)? variable
    ;

lineinputhashstmt
    : LINE INPUTHASH filenum=expr COMMA variable
    ;

readstmt
    : READ variable (COMMA variable)*
    ;

datastmt
    : DATA (str=STRING | number) (COMMA (str=STRING | number))*
    ;

restorestmt
    : RESTORE
    ;

randomizestmt
    : RANDOMIZE expr
    ;

randomizetimerstmt
    : RANDOMIZE TIMER
    ;

defintstmt
    : DEFINT LETTERRANGE (COMMA LETTERRANGE)*
    ;

deflngstmt
    : DEFLNG LETTERRANGE (COMMA LETTERRANGE)*
    ;

defsngstmt
    : DEFSNG LETTERRANGE (COMMA LETTERRANGE)*
    ;

defdblstmt
    : DEFDBL LETTERRANGE (COMMA LETTERRANGE)*
    ;

defstrstmt
    : DEFSTR LETTERRANGE (COMMA LETTERRANGE)*
    ;

middlrstmt
    : MIDDLR LPAREN variable COMMA expr (COMMA expr)? RPAREN RELEQ expr
    ;

LETTERRANGE
    : LETTER MINUS LETTER
    ;

LET
    : L E T
    ;

PRINT
    : P R I N T
    ;

PRINTHASH
    : P R I N T HASH
    ;

USING
    : U S I N G
    ;

IF
    : I F
    ;

THEN
    : T H E N
    ;

ELSE
    : E L S E
    ;

GOTO
    : G O T O
    ;

FOR
    : F O R
    ;

NEXT
    : N E X T
    ;

TO
    : T O
    ;

STEP
    : S T E P
    ;

REM
    : R E M
    ;

END
    : E N D
    ;


SIN
    : S I N
    ;

COS
    : C O S
    ;

TAN
    : T A N
    ;

ATN
    : A T N
    ;

SQR
    : S Q R
    ;

ABS
    : A B S
    ;

ASC
    : A S C
    ;

DEF
    : D E F
    ;

DIM
    : D I M
    ;

GOSUB
    : G O S U B
    ;

RETURN
    : R E T U R N
    ;

LSET
    : L S E T
    ;

RSET
    : R S E T
    ;

CINT
    : C I N T
    ;

CLNG
    : C L N G
    ;

CSNG
    : C S N G
    ;

CDBL
    : C D B L
    ;

CHRDLR
    : C H R DOLLAR
    ;

WHILE
    : W H I L E
    ;

WEND
    : W E N D
    ;

MKIDLR
    : M K I DOLLAR
    ;

MKLDLR
    : M K L DOLLAR
    ;

MKSDLR
    : M K S DOLLAR
    ;

MKDDLR
    : M K D DOLLAR
    ;

CVI
    : C V I
    ;

CVL
    : C V L
    ;

CVS
    : C V S
    ;

CVD
    : C V D
    ;

SPACEDLR
    : S P A C E DOLLAR
    ;

STRDLR
    : S T R DOLLAR
    ;

VAL
    : V A L
    ;

INT
    : I N T
    ;

FIX
    : F I X
    ;

LOG
    : L O G
    ;

LEN
    : L E N
    ;

RIGHTDLR
    : R I G H T DOLLAR
    ;

LEFTDLR
    : L E F T DOLLAR
    ;

MIDDLR
    : M I D DOLLAR
    ;

INSTR
    : I N S T R
    ;

HEXDLR
    : H E X DOLLAR
    ;

OCTDLR
    : O C T DOLLAR
    ;

RND
    : R N D
    ;

SGN
    : S G N
    ;

TIMER
    : T I M E R
    ;

STRINGDLR
    : S T R I N G DOLLAR
    ;

SWAP
    : S W A P
    ;

OPEN
    : O P E N
    ;

CLOSE
    : C L O S E
    ;

ACCESS
    : A C C E S S
    ;

AS
    : A S
    ;

LINE
    : L I N E
    ;

INPUT
    : I N P U T
    ;

INPUTHASH
    : I N P U T HASH
    ;

INPUTDLR
    : I N P U T DOLLAR
    ;

OUTPUT
    : O U T P U T
    ;

APPEND
    : A P P E N D
    ;

RANDOM
    : R A N D O M
    ;

RANDOMIZE
    : R A N D O M I Z E
    ;

READ
    : R E A D
    ;

WRITE
    : W R I T E
    ;

WRITEHASH
    : W R I T E HASH
    ;

SHARED
    : S H A R E D
    ;

LOCK
    : L O C K
    ;

PUT
    : P U T
    ;

GET
    : G E T
    ;

EOFFN
    : E O F
    ;

LOC
    : L O C
    ;

LOF
    : L O F
    ;

FIELD
    : F I E L D
    ;

DATA
    : D A T A
    ;

RESTORE
    : R E S T O R E
    ;

DEFINT
    : D E F I N T
    ;

DEFLNG
    : D E F L N G
    ;

DEFSNG
    : D E F S N G
    ;

DEFDBL
    : D E F D B L
    ;

DEFSTR
    : D E F S T R
    ;

string
    : STRING
    ;

STRING
    : '"' ~["\r\n]+ '"'
    ;

COMMA
    : ','
    ;

SEMICOLON
    : ';'
    ;

QUESTION
    : '?'
    ;

AT
    : '@'
    ;

DOLLAR
    : '$'
    ;

PERCENT
    : '%'
    ;

EXCLAMATION
    : '!'
    ;

HASH
    : '#'
    ;

APOSTROPHE
    : '\''
    ;

EXPONENT
    : '^'
    ;

FLOAT_DIV
    : '/'
    ;

INT_DIV
    : '\\'
    ;

MUL
    : '*'
    ;

LPAREN
    : '('
    ;

RPAREN
    : ')'
    ;

MOD
    : M O D
    ;

RELEQ
    : '='
    ;

RELNEQ
    : '<>'
    ;

RELGT
    : '>'
    ;

RELGE
    : '>='
    ;

RELLT
    : '<'
    ;

RELLE
    : '<='
    ;

LOGAND
    : A N D
    ;

LOGOR
    : O R
    ;

LOGNOT
    : N O T
    ;

LOGXOR
    : X O R
    ;

LOGEQV
    : E Q V
    ;

LOGIMP
    : I M P
    ;

VARNAME
    : LETTER (LETTER | DECIMAL)*
    ;

LETTER
    : [a-zA-Z]
    ;

number
    : integer | FLOAT | DOUBLE
    ;

integer
    : (DECIMAL | HEXADECIMAL | OCTAL) AT?
    ;

PLUS
    : '+'
    ;

MINUS
    : '-'
    ;

DECIMAL
    : DIGIT+
    ;

HEXADECIMAL
    : '&' H DECIMAL
    ;

OCTAL
    : '&' O? DECIMAL
    ;

FLOAT
    : ((DIGIT* '.' DIGIT+) (('e' | 'E') DIGIT+)? '!'?) | (DECIMAL '!')
    ;

DOUBLE
    : ((DIGIT* '.' DIGIT+) (('d' | 'D') DIGIT+)? '#'?) | (DECIMAL '#')
    ;

fragment DIGIT : [0-9] ;

fragment A : 'a' | 'A' ;
fragment B : 'b' | 'B' ;
fragment C : 'c' | 'C' ;
fragment D : 'd' | 'D' ;
fragment E : 'e' | 'E' ;
fragment F : 'f' | 'F' ;
fragment G : 'g' | 'G' ;
fragment H : 'h' | 'H' ;
fragment I : 'i' | 'I' ;
fragment J : 'j' | 'J' ;
fragment K : 'k' | 'K' ;
fragment L : 'l' | 'L' ;
fragment M : 'm' | 'M' ;
fragment N : 'n' | 'N' ;
fragment O : 'o' | 'O' ;
fragment P : 'p' | 'P' ;
fragment Q : 'q' | 'Q' ;
fragment R : 'r' | 'R' ;
fragment S : 's' | 'S' ;
fragment T : 't' | 'T' ;
fragment U : 'u' | 'U' ;
fragment V : 'v' | 'V' ;
fragment W : 'w' | 'W' ;
fragment X : 'x' | 'X' ;
fragment Y : 'y' | 'Y' ;
fragment Z : 'z' | 'Z' ;

NEWLINE
    : '\r'? '\n'
    ;

WS
    : (SPACE | TAB)+ -> channel(HIDDEN);

SPACE
    : ' '
    ;

TAB
    : '\t'
    ;
