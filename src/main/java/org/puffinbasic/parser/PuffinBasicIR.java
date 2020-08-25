package org.puffinbasic.parser;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.misc.Interval;
import org.jetbrains.annotations.NotNull;
import org.puffinbasic.domain.PuffinBasicSymbolTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PuffinBasicIR {

    public enum OpCode {
        COMMENT("comment"),
        VARIABLE("var"),
        VALUE("val"),
        ASSIGN(":="),
        COPY("<-"),
        UNARY_MINUS("u-"),
        PRINT("?"),
        PRINTUSING("?f"),
        FLUSH("flush"),
        RESET_ARRAY_IDX("resetArrIdx"),
        SET_ARRAY_IDX("setArrIdx"),
        GOTO_LINENUM("goto"),
        GOTO_LABEL("gotoLabel"),
        GOTO_LABEL_IF("gotoLabelIf"),
        GOTO_CALLER("gotoCaller"),
        LABEL("label"),
        PUSH_RT_SCOPE("pushRtScope"),
        POP_RT_SCOPE("popRtScope"),
        END("end"),
        RETURN("ret"),
        PUSH_RETLABEL("pushRetLabel"),
        SWAP("swap"),
        EXP("^"),
        MUL("*"),
        IDIV("/"),
        FDIV("\\"),
        ADD("+"),
        CONCAT("concat"),
        SUB("-"),
        MOD("mod"),
        EQ("="),
        NE("<>"),
        LT("<"),
        LE("<="),
        GT(">"),
        GE(">="),
        NOT("not"),
        AND("and"),
        OR("or"),
        XOR("xor"),
        EQV("eqv"),
        IMP("imp"),
        ABS("abs"),
        ASC("asc"),
        SIN("sin"),
        COS("cos"),
        TAN("tan"),
        ASIN("asin"),
        ACOS("acos"),
        ATN("atn"),
        SINH("sinh"),
        COSH("cosh"),
        TANH("tanh"),
        SQR("sqr"),
        EEXP("exp"),
        CINT("cint"),
        CLNG("clng"),
        CSNG("csng"),
        CDBL("cdbl"),
        CHRDLR("chr$"),
        CVI("cvi"),
        CVL("cvl"),
        CVS("cvs"),
        CVD("cvd"),
        MKIDLR("mki$"),
        MKLDLR("mkl$"),
        MKSDLR("mks$"),
        MKDDLR("mkd$"),
        SPACEDLR("space$"),
        STRDLR("str$"),
        VAL("val"),
        INT("int"),
        FIX("fix"),
        LOG("log"),
        LOG10("log10"),
        TORAD("torad"),
        TODEG("todeg"),
        FLOOR("floor"),
        CEIL("ceil"),
        ROUND("round"),
        E("e"),
        PI("pi"),
        MIN("min"),
        MAX("max"),
        ARRAYFILL("arrayfill"),
        ARRAY1DMIN("array1dmin"),
        ARRAY1DMAX("array1dmax"),
        ARRAY1DMEAN("array1dmean"),
        ARRAY1DSUM("array1dsum"),
        ARRAY1DSTD("array1dstd"),
        ARRAY1DMEDIAN("array1dmedian"),
        ARRAY1DPCT("array1dpct"),
        ARRAY1DSORT("array1dsort"),
        ARRAY1DBINSEARCH("array1dbinsearch"),
        ARRAY1DCOPYSRC("array1dcopysrc"),
        ARRAY1DCOPYDST("array1dcopydst"),
        ARRAY1DCOPY("array1dcopy"),
        ARRAYCOPY("arraycopy"),
        ARRAY2DSHIFTHOR("array2dshifthor"),
        ARRAY2DSHIFTVER("array2dshiftver"),
        LEN("len"),
        HEXDLR("hex$"),
        OCTDLR("oct$"),
        LEFTDLR("left$"),
        RIGHTDLR("right$"),
        INSTR0("instr0"),
        INSTR("instr"),
        MIDDLR0("mid$0"),
        MIDDLR("mid$"),
        MIDDLR_STMT("mid$_stmt"),
        RND("rnd"),
        SGN("sgn"),
        TIMER("timer"),
        STRINGDLR("string$"),
        OPEN_FN_FN_0("open_fn_fn_0"),
        OPEN_OM_AM_1("open_om_am_1"),
        OPEN_LM_RL_2("open_lm_rl_2"),
        CLOSE_ALL("close_all"),
        CLOSE("close"),
        FIELD_I("field_i"),
        FIELD("field"),
        PUTF("putf"),
        GETF("getf"),
        LOC("loc"),
        LOF("lof"),
        EOF("eof"),
        RANDOMIZE("randomize"),
        RANDOMIZE_TIMER("randomize_timer"),
        LSET("lset"),
        RSET("rset"),
        INPUTDLR("input$"),
        INPUT_VAR("input_var"),
        INPUT("input"),
        LINE_INPUT("line_input"),
        WRITE("write"),
        RESTORE("restore"),
        DATA("data"),
        READ("read"),
        ENVIRONDLR("environ$"),
        SCREEN0("screen0"),
        SCREEN("screen"),
        REPAINT("repaint"),
        CIRCLE_XY("circle_xy"),
        CIRCLE_SE("circle_se"),
        CIRCLE_FILL("circle_se"),
        CIRCLE("circle"),
        SLEEP("sleep"),
        LINE_x1y1("line_x1y1"),
        LINE_x2y2("line_x2y2"),
        LINE("line"),
        COLOR_RG("color_rg"),
        COLOR("color"),
        INKEYDLR("inkey$"),
        PAINT_RG("paint_rg"),
        PAINT_B("paint_b"),
        PAINT("paint"),
        PSET_RG("pset_rg"),
        PSET_B("pset_b"),
        PSET("pset"),
        GPUT_XY("gput_xy"),
        GPUT("gput"),
        GGET_X1Y1("gget_x1y1"),
        GGET_X2Y2("gget_x2y2"),
        GGET("gget"),
        LOADIMG("loadimg"),
        DRAWSTR_XY("drawstr_xy"),
        DRAWSTR("drawstr"),
        DRAW("draw"),
        FONT_SS("font_ss"),
        FONT("font"),
        CLS("cls"),
        BEEP("beep"),
        ARRAYREF("arrayref"),
        ;

        public final String repr;

        OpCode(String repr) {
            this.repr = repr;
        }
    }

    private final CharStream in;
    private final PuffinBasicSymbolTable symbolTable;
    private final List<Instruction> instructions;

    public PuffinBasicIR(CharStream in, PuffinBasicSymbolTable symbolTable) {
        this.in = in;
        this.symbolTable = symbolTable;
        this.instructions = new ArrayList<>();
    }

    public String getCodeStreamFor(Instruction instruction) {
        return in.getText(new Interval(instruction.inputRef.inputStartIndex, instruction.inputRef.inputStopIndex));
    }

    public List<Instruction> getInstructions() {
        return new ArrayList<>(instructions);
    }

    public Instruction addInstruction(
            int linenum, int startIndex, int stopIndex,
            @NotNull OpCode opCode, int op1, int op2, int result)
    {
        var instruction = new Instruction(
                new InputRef(linenum, startIndex, stopIndex), opCode, op1, op2, result);
        instructions.add(instruction);
        return instruction;
    }

    public PuffinBasicSymbolTable getSymbolTable() {
        return symbolTable;
    }

    public static final class InputRef {
        public final int lineNumber;
        public final int inputStartIndex;
        public final int inputStopIndex;

        public InputRef(int lineNumber, int inputStartIndex, int inputStopIndex) {
            this.lineNumber = lineNumber;
            this.inputStartIndex = inputStartIndex;
            this.inputStopIndex = inputStopIndex;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InputRef other = (InputRef) o;
            return lineNumber == other.lineNumber &&
                    inputStartIndex == other.inputStartIndex &&
                    inputStopIndex == other.inputStopIndex;
        }

        @Override
        public int hashCode() {
            return Objects.hash(lineNumber, inputStartIndex, inputStopIndex);
        }

        @Override
        public String toString() {
            return "[" + lineNumber + "(" + inputStartIndex + "-" + inputStopIndex + ")]";
        }
    }

    public static final class Instruction {
        public final InputRef inputRef;
        public final OpCode opCode;
        public int op1;
        public int op2;
        public final int result;

        public Instruction(InputRef inputRef, OpCode opCode, int op1, int op2, int result) {
            this.inputRef = inputRef;
            this.opCode = opCode;
            this.op1 = op1;
            this.op2 = op2;
            this.result = result;
        }

        public InputRef getInputRef() {
            return inputRef;
        }

        public void patchOp1(int op1) {
            this.op1 = op1;
        }

        public void patchOp2(int op2) {
            this.op2 = op2;
        }

        @Override
        public String toString() {
            return String.format(
                    "[%4d]\t%4s\t%4s %4s %4s",
                    inputRef.lineNumber, opCode.repr, op1, op2, result);
        }
    }
}
