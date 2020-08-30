grammar PuffinBasic;

prog
    : (line | NEWLINE)*
    ;

line
    : linenum? stmtlist? comment? NEWLINE
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
    | ifthenbeginstmt
    | elsebeginstmt
    | endifstmt
    | forstmt
    | nextstmt
    | gotostmt
    | gotolabelstmt
    | endstmt
    | deffnstmt
    | dimstmt
    | gosubstmt
    | gosublabelstmt
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
    | sleepstmt
    | screenstmt
    | circlestmt
    | linestmt
    | colorstmt
    | paintstmt
    | psetstmt
    | drawstmt
    | graphicsgetstmt
    | graphicsputstmt
    | fontstmt
    | drawstrstmt
    | loadimgstmt
    | saveimgstmt
    | clsstmt
    | beepstmt
    | repaintstmt
    | arrayfillstmt
    | arraycopystmt
    | array1dcopystmt
    | array1dsortstmt
    | array2dshifthorstmt
    | array2dshiftverstmt
    | loadwavstmt
    | playwavstmt
    | stopwavstmt
    | loopwavstmt
    | labelstmt
    | refstmt
    | func
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
    | expr BWLSFT expr          # ExprBitwiseLeftShift
    | expr BWRSFT expr          # ExprBitwiseRightShift
    ;

func
    : ABS  LPAREN expr RPAREN                               # FuncAbs
    | ASC  LPAREN expr RPAREN                               # FuncAsc
    | SIN  LPAREN expr RPAREN                               # FuncSin
    | COS  LPAREN expr RPAREN                               # FuncCos
    | TAN  LPAREN expr RPAREN                               # FuncTan
    | ASIN  LPAREN expr RPAREN                              # FuncASin
    | ACOS  LPAREN expr RPAREN                              # FuncACos
    | ATN  LPAREN expr RPAREN                               # FuncAtn
    | SINH  LPAREN expr RPAREN                              # FuncSinh
    | COSH  LPAREN expr RPAREN                              # FuncCosh
    | TANH  LPAREN expr RPAREN                              # FuncTanh
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
    | LOG10 LPAREN expr RPAREN                              # FuncLog10
    | LOG2 LPAREN expr RPAREN                               # FuncLog2
    | EXP LPAREN expr RPAREN                                # FuncExp
    | TORAD LPAREN expr RPAREN                              # FuncToRad
    | TODEG LPAREN expr RPAREN                              # FuncToDeg
    | CEIL LPAREN expr RPAREN                               # FuncCeil
    | FLOOR LPAREN expr RPAREN                              # FuncFloor
    | ROUND LPAREN expr RPAREN                              # FuncRound
    | MIN LPAREN expr COMMA expr RPAREN                     # FuncMin
    | MAX LPAREN expr COMMA expr RPAREN                     # FuncMax
    | PI LPAREN RPAREN                                      # FuncPI
    | EULERE LPAREN RPAREN                                  # FuncE
    | LEN LPAREN expr RPAREN                                # FuncLen
    | HEXDLR LPAREN expr RPAREN                             # FuncHexDlr
    | OCTDLR LPAREN expr RPAREN                             # FuncOctDlr
    | RIGHTDLR LPAREN expr COMMA expr RPAREN                # FuncRightDlr
    | LEFTDLR LPAREN expr COMMA expr RPAREN                 # FuncLeftDlr
    | MIDDLR LPAREN expr COMMA expr (COMMA expr)? RPAREN    # FuncMidDlr
    | INSTR LPAREN expr COMMA expr (COMMA expr)? RPAREN     # FuncInstr
    | RND                                                   # FuncRnd
    | SGN LPAREN expr RPAREN                                # FuncSgn
    | ENVIRONDLR LPAREN expr RPAREN                         # FuncEnvironDlr
    | TIMER                                                 # FuncTimer
    | STRINGDLR LPAREN expr COMMA expr RPAREN               # FuncStringDlr
    | EOFFN LPAREN expr RPAREN                              # FuncEof
    | LOC LPAREN expr RPAREN                                # FuncLoc
    | LOF LPAREN expr RPAREN                                # FuncLof
    | INPUTDLR LPAREN expr (COMMA HASH? expr)? RPAREN       # FuncInputDlr
    | INKEYDLR                                              # FuncInkeyDlr
    | ARRAY1DMIN LPAREN variable RPAREN                     # FuncArray1DMin
    | ARRAY1DMAX LPAREN variable RPAREN                     # FuncArray1DMax
    | ARRAY1DMEAN LPAREN variable RPAREN                    # FuncArray1DMean
    | ARRAY1DSUM LPAREN variable RPAREN                     # FuncArray1DSum
    | ARRAY1DSTD LPAREN variable RPAREN                     # FuncArray1DStd
    | ARRAY1DMEDIAN LPAREN variable RPAREN                  # FuncArray1DMedian
    | ARRAY1DPCT LPAREN variable COMMA p=expr RPAREN        # FuncArray1DPct
    | ARRAY1DBINSEARCH LPAREN variable COMMA expr RPAREN    # FuncArray1DBinSearch
    | HSB2RGB LPAREN expr COMMA expr COMMA expr RPAREN      # FuncHsb2Rgb
    | DICT varsuffix varsuffix LPAREN expr EQGT expr (COMMA expr EQGT expr)* RPAREN # FuncDictCreate
    | DICTGET LPAREN id=expr COMMA key=expr COMMA def=expr RPAREN                   # FuncDictGet
    | DICTCONTAINSKEY LPAREN id=expr COMMA key=expr RPAREN                          # FuncDictContainsKey
    | DICTPUT LPAREN id=expr COMMA key=expr COMMA value=expr RPAREN                 # FuncDictPut
    | DICTCLEAR LPAREN id=expr RPAREN                                               # FuncDictClear
    | DICTSIZE LPAREN id=expr RPAREN                                                # FuncDictSize
    | SET varsuffix LPAREN expr (COMMA expr)* RPAREN                                # FuncSetCreate
    | SETADD LPAREN id=expr COMMA value=expr RPAREN                                 # FuncSetAdd
    | SETCONTAINS LPAREN id=expr COMMA value=expr RPAREN                            # FuncSetContains
    | SETCLEAR LPAREN id=expr RPAREN                                                # FuncSetClear
    | SETSIZE LPAREN id=expr RPAREN                                                 # FuncSetSize
    | MOUSEMOVEDX LPAREN RPAREN                             # FuncMouseMovedX
    | MOUSEMOVEDY LPAREN RPAREN                             # FuncMouseMovedY
    | MOUSEDRAGGEDX LPAREN RPAREN                           # FuncMouseDraggedX
    | MOUSEDRAGGEDY LPAREN RPAREN                           # FuncMouseDraggedY
    | MOUSEBUTTONCLICKED LPAREN RPAREN                      # FuncMouseButtonClicked
    | MOUSEBUTTONPRESSED LPAREN RPAREN                      # FuncMouseButtonPressed
    | MOUSEBUTTONRELEASED LPAREN RPAREN                     # FuncMouseButtonReleased
    ;

gosubstmt
    : GOSUB linenum
    ;

gosublabelstmt
    : GOSUB string
    ;

returnstmt
    : RETURN linenum?
    ;

printhashusingstmt
    : PRINTHASH filenum=expr COMMA USING format=expr SEMICOLON printlist
    ;

printusingstmt
    : PRINT USING format=expr SEMICOLON printlist
    ;

printhashstmt
    : PRINTHASH filenum=expr COMMA printlist
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

ifstmt
    : IF expr COMMA? then COMMA? (ELSE elsestmt)?  # IfThenElse
    ;

then
    : (THEN (linenum | stmtlist)) | (GOTO linenum)
    ;

elsestmt
    : linenum | stmtlist
    ;

ifthenbeginstmt
    : IF expr THEN BEGIN
    ;

elsebeginstmt
    : ELSE BEGIN
    ;

endifstmt
    : END IF
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

gotolabelstmt
    : GOTO string
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

sleepstmt
    : SLEEP expr
    ;

screenstmt
    : SCREEN title=expr COMMA w=expr COMMA h=expr (COMMA mr=MANUAL_REPAINT)?
    ;

repaintstmt
    : REPAINT
    ;

circlestmt
    // CIRCLE (x, y), r1, r2, start, end, "F"
    : CIRCLE LPAREN x=expr COMMA y=expr RPAREN COMMA r1=expr COMMA r2=expr
        (COMMA s=expr? COMMA e=expr? COMMA? fill=expr?)?
    ;

linestmt
    : LINE LPAREN x1=expr COMMA y1=expr RPAREN MINUS LPAREN x2=expr COMMA y2=expr RPAREN
        (COMMA bf=expr)?
    ;

colorstmt
    : COLOR r=expr COMMA g=expr COMMA b=expr
    ;

paintstmt
    // PAINT (x, y), r, g, b
    : PAINT LPAREN x=expr COMMA y=expr RPAREN COMMA r=expr COMMA g=expr COMMA b=expr
    ;

psetstmt
    : PSET LPAREN x=expr COMMA y=expr RPAREN (COMMA r=expr COMMA g=expr COMMA b=expr)?
    ;

drawstmt
    : DRAW expr
    ;

graphicsgetstmt
    : GET LPAREN x1=expr COMMA y1=expr RPAREN MINUS LPAREN x2=expr COMMA y2=expr RPAREN
        COMMA variable
    ;

graphicsputstmt
    : PUT LPAREN x=expr COMMA y=expr RPAREN COMMA variable (COMMA action=expr)?
    ;

fontstmt
    : FONT name=expr COMMA style=expr COMMA size=expr
    ;

drawstrstmt
    : DRAWSTR str=expr COMMA x=expr COMMA y=expr
    ;

loadimgstmt
    : LOADIMG path=expr COMMA variable
    ;

saveimgstmt
    : SAVEIMG path=expr COMMA variable
    ;

clsstmt
    : CLS
    ;

beepstmt
    : BEEP
    ;

arrayfillstmt
    : ARRAYFILL variable COMMA expr
    ;

arraycopystmt
    : ARRAYCOPY src=variable COMMA dst=variable
    ;

array1dsortstmt
    : ARRAY1DSORT variable
    ;

array1dcopystmt
    : ARRAY1DCOPY src=variable COMMA src0=expr COMMA dst=variable COMMA dst0=expr COMMA len=expr
    ;

array2dshifthorstmt
    : ARRAY2DSHIFTHOR variable COMMA step=expr
    ;

array2dshiftverstmt
    : ARRAY2DSHIFTVER variable COMMA step=expr
    ;

loadwavstmt
    : LOADWAV path=expr COMMA variable
    ;

playwavstmt
    : PLAYWAV variable
    ;

stopwavstmt
    : STOPWAV variable
    ;

loopwavstmt
    : LOOPWAV variable
    ;

labelstmt
    : LABEL name=string
    ;

refstmt
    : REF src=variable EQGT dst=variable
    ;

DICT
    : D I C T
    ;

DICTGET
    : D I C T G E T
    ;

DICTCONTAINSKEY
    : D I C T C O N T A I N S K E Y
    ;

DICTPUT
    : D I C T P U T
    ;

DICTCLEAR
    : D I C T C L E A R
    ;

DICTSIZE
    : D I C T S I Z E
    ;

SET
    : S E T
    ;

SETADD
    : S E T A D D
    ;

SETCONTAINS
    : S E T C O N T A I N S
    ;

SETCLEAR
    : S E T C L E A R
    ;

SETSIZE
    : S E T S I Z E
    ;

REF
    : R E F
    ;

EQGT
    : '=' '>'
    ;

DEFAULT
    : D E F A U L T
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

ENVIRONDLR
    : E N V I R O N DOLLAR
    ;

SCREEN
    : S C R E E N
    ;

CIRCLE
    : C I R C L E
    ;

SLEEP
    : S L E E P
    ;

COLOR
    : C O L O R
    ;

INKEYDLR
    : I N K E Y DOLLAR
    ;

PAINT
    : P A I N T
    ;

PSET
    : P S E T
    ;

DRAW
    : D R A W
    ;

FONT
    : F O N T
    ;

DRAWSTR
    : D R A W S T R
    ;

LOADIMG
    : L O A D I M G
    ;

SAVEIMG
    : S A V E I M G
    ;

LOADWAV
    : L O A D W A V
    ;

PLAYWAV
    : P L A Y W A V
    ;

STOPWAV
    : S T O P W A V
    ;

LOOPWAV
    : L O O P W A V
    ;

CLS
    : C L S
    ;

BEEP
    : B E E P
    ;

MANUAL_REPAINT
    : M A N U A L R E P A I N T
    ;

REPAINT
    : R E P A I N T
    ;

ASIN
    : A S I N
    ;

ACOS
    : A C O S
    ;

SINH
    : S I N H
    ;

COSH
    : C O S H
    ;

TANH
    : T A N H
    ;

EULERE
    : E U L E R E
    ;

PI
    : P I
    ;

MIN
    : M I N
    ;

MAX
    : M A X
    ;

FLOOR
    : F L O O R
    ;

CEIL
    : C E I L
    ;

ROUND
    : R O U N D
    ;

LOG10
    : L O G '1' '0'
    ;

LOG2
    : L O G '2'
    ;

EXP
    : E X P
    ;

TORAD
    : T O R A D
    ;

TODEG
    : T O D E G
    ;

TRUE
    : T R U E
    ;

FALSE
    : F A L S E
    ;

ARRAYFILL
    : A R R A Y F I L L
    ;

ARRAY1DMIN
    : A R R A Y '1' D M I N
    ;

ARRAY1DMAX
    : A R R A Y '1' D M A X
    ;

ARRAY1DMEAN
    : A R R A Y '1' D M E A N
    ;

ARRAY1DSUM
    : A R R A Y '1' D S U M
    ;

ARRAY1DSTD
    : A R R A Y '1' D S T D
    ;

ARRAY1DMEDIAN
    : A R R A Y '1' D M E D I A N
    ;

ARRAY1DPCT
    : A R R A Y '1' D P C T
    ;

ARRAY1DSORT
    : A R R A Y '1' D S O R T
    ;

ARRAY1DBINSEARCH
    : A R R A Y '1' D B I N S E A R C H
    ;

ARRAYCOPY
    : A R R A Y C O P Y
    ;

ARRAY1DCOPY
    : A R R A Y '1' D C O P Y
    ;

ARRAY2DSHIFTHOR
    : A R R A Y '2' D S H I F T H O R
    ;

ARRAY2DSHIFTVER
    : A R R A Y '2' D S H I F T V E R
    ;

HSB2RGB
    : H S B '2' R G B
    ;

LABEL
    : L A B E L
    ;

BEGIN
    : B E G I N
    ;

MOUSEMOVEDX
    : M O U S E M O V E D X
    ;

MOUSEMOVEDY
    : M O U S E M O V E D Y
    ;

MOUSEDRAGGEDX
    : M O U S E D R A G G E D X
    ;

MOUSEDRAGGEDY
    : M O U S E D R A G G E D Y
    ;

MOUSEBUTTONCLICKED
    : M O U S E B U T T O N C L I C K E D
    ;

MOUSEBUTTONPRESSED
    : M O U S E B U T T O N P R E S S E D
    ;

MOUSEBUTTONRELEASED
    : M O U S E B U T T O N R E L E A S E D
    ;

string
    : STRING
    ;

STRING
    : '"' ~["\r\n]* '"'
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

BWRSFT
    : '>' '>'
    ;

BWLSFT
    : '<' '<'
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
    : (DECIMAL | HEXADECIMAL | OCTAL) (PERCENT | AT | HASH | EXCLAMATION)?
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
