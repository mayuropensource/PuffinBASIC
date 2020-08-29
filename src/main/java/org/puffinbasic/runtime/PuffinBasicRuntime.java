package org.puffinbasic.runtime;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;
import org.puffinbasic.error.PuffinBasicInternalError;
import org.puffinbasic.error.PuffinBasicRuntimeError;
import org.puffinbasic.file.PuffinBasicFiles;
import org.puffinbasic.file.SystemInputOutputFile;
import org.puffinbasic.parser.PuffinBasicIR;
import org.puffinbasic.parser.PuffinBasicIR.Instruction;
import org.puffinbasic.runtime.ArraysUtil.ArrayState;
import org.puffinbasic.runtime.Formatter.FormatterCache;
import org.puffinbasic.runtime.GraphicsRuntime.GraphicsState;
import org.puffinbasic.runtime.Statements.ReadData;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.puffinbasic.domain.PuffinBasicSymbolTable.NULL_ID;
import static org.puffinbasic.parser.PuffinBasicIR.OpCode.DATA;
import static org.puffinbasic.parser.PuffinBasicIR.OpCode.LABEL;

public class PuffinBasicRuntime {

    private final PuffinBasicIR ir;
    private PrintBuffer printBuffer;
    private ArrayState arrayState;
    private IntStack gosubReturnLabelStack;
    private int programCounter;
    private Random random;
    private Int2IntMap labelToInstrNum;
    private Int2IntMap lineNumToInstrNum;
    private List<Instruction> params;
    private FormatterCache formatterCache;
    private PuffinBasicFiles files;
    private ReadData readData;
    private final PrintStream out;
    private final Environment env;
    private GraphicsState graphicsState;
    private SoundState soundState;

    public PuffinBasicRuntime(PuffinBasicIR ir, PrintStream out, Environment env) {
        this.ir = ir;
        this.out = out;
        this.env = env;
    }

    private Int2IntMap computeLabelToInstructionNumber(List<Instruction> instructions) {
        Int2IntMap labelToInstrNum = new Int2IntOpenHashMap();
        for (int i = 0; i < instructions.size(); i++) {
            var instr = instructions.get(i);
            if (instr.opCode == LABEL) {
                labelToInstrNum.put(instr.op1, i);
            }
        }
        return labelToInstrNum;
    }

    private int getInstrNumForLabel(int id) {
        var instrNum = labelToInstrNum.getOrDefault(id, -1);
        if (instrNum == -1) {
            throw new PuffinBasicInternalError("Failed to find instruction# for label: " + id);
        }
        return instrNum;
    }

    private Int2IntMap computeLineNumberToInstructionNumber(List<Instruction> instructions) {
        var linenumToInstrNum = new Int2IntOpenHashMap();
        int instrNum = 0;
        for (var instruction : instructions) {
            int lineNumber = instruction.getInputRef().lineNumber;
            if (lineNumber >= 0) {
                linenumToInstrNum.putIfAbsent(lineNumber, instrNum);
            }
            ++instrNum;
        }
        return linenumToInstrNum;
    }

    private int getInstrNumForLineNumber(int lineNumber) {
        var instrNum = lineNumToInstrNum.getOrDefault(lineNumber, -1);
        if (instrNum == -1) {
            throw new PuffinBasicInternalError("Failed to find instruction# for line#: " + lineNumber);
        }
        return instrNum;
    }

    public void run() {
        var instructions = ir.getInstructions();
        this.labelToInstrNum = computeLabelToInstructionNumber(instructions);
        this.lineNumToInstrNum = computeLineNumberToInstructionNumber(instructions);
        this.printBuffer = new PrintBuffer();
        this.arrayState = new ArrayState();
        this.gosubReturnLabelStack = new IntArrayList();
        this.random = new Random();
        this.formatterCache = new FormatterCache();
        this.params = new ArrayList<>(4);
        this.files = new PuffinBasicFiles(new SystemInputOutputFile(System.in, out));
        this.readData = processDataInstructions(instructions);
        this.graphicsState = new GraphicsState();
        this.soundState = new SoundState();

        try {
            var numInstructions = instructions.size();
            boolean end = false;
            while (!end && programCounter < numInstructions) {
                var instruction = instructions.get(programCounter);
                try {
                    end = runInstruction(instruction);
                } catch (PuffinBasicRuntimeError e) {
                    throw new PuffinBasicRuntimeError(e, instruction, ir.getCodeStreamFor(instruction));
                }
            }
        } finally {
            GraphicsRuntime.end(graphicsState);
            soundState.close();
        }
    }

    private ReadData processDataInstructions(List<Instruction> instructions) {
        return new ReadData(instructions.stream().filter(i -> i.opCode == DATA).map(instruction ->
                ir.getSymbolTable().get(instruction.op1)
        ).collect(Collectors.toList()));
    }

    private boolean runInstruction(Instruction instruction) {
        int nextProgramCounter = programCounter + 1;

        switch (instruction.opCode) {
            case COMMENT:
                break;
            case VARIABLE:
                break;
            case VALUE:
                break;
            case ASSIGN:
            case COPY:
                Types.copy(ir.getSymbolTable(), instruction.op1, instruction.op2);
                break;
            case UNARY_MINUS:
                Operators.unaryMinus(ir.getSymbolTable(), instruction);
                break;
            case PRINT:
                Statements.print(printBuffer, ir.getSymbolTable(), instruction);
                break;
            case PRINTUSING:
                Statements.printusing(formatterCache, printBuffer, ir.getSymbolTable(), instruction);
                break;
            case FLUSH:
                Statements.flush(files, printBuffer, ir.getSymbolTable(), instruction);
                break;
            case RESET_ARRAY_IDX:
                ArraysUtil.resetIndex(arrayState, ir.getSymbolTable(), instruction);
                break;
            case SET_ARRAY_IDX:
                ArraysUtil.setIndex(arrayState, ir.getSymbolTable(), instruction);
                break;
            case ARRAYREF:
                ArraysUtil.arrayref(ir.getSymbolTable(), instruction);
                break;
            case LABEL:
                break;
            case GOTO_LINENUM: {
                var lineNumber = ir.getSymbolTable().get(instruction.op1).getValue().getInt32();
                nextProgramCounter = getInstrNumForLineNumber(lineNumber);
            }
                break;
            case GOTO_LABEL_IF: {
                if (ir.getSymbolTable().get(instruction.op1).getValue().getInt64() != 0) {
                    nextProgramCounter = getInstrNumForLabel(instruction.op2);
                }
            }
                break;
            case GOTO_LABEL:
                nextProgramCounter = getInstrNumForLabel(instruction.op1);
                break;
            case GOTO_CALLER:
                nextProgramCounter = ir.getSymbolTable().getCurrentScope().getCallerInstrId();
                break;
            case PUSH_RT_SCOPE:
                ir.getSymbolTable().pushRuntimeScope(instruction.op1, getInstrNumForLabel(instruction.op2));
                break;
            case POP_RT_SCOPE:
                ir.getSymbolTable().popScope();
                break;
            case PUSH_RETLABEL:
                gosubReturnLabelStack.push(instruction.op1);
                break;
            case RETURN: {
                if (instruction.op1 == NULL_ID) {
                    nextProgramCounter = getInstrNumForLabel(gosubReturnLabelStack.popInt());
                } else {
                    // Ignore label because we need to return to the lineNumber
                    gosubReturnLabelStack.popInt();
                    var lineNumber = ir.getSymbolTable().get(instruction.op1).getValue().getInt32();
                    nextProgramCounter = getInstrNumForLineNumber(lineNumber);
                }
            }
                break;
            case EXP:
                Operators.exp(ir.getSymbolTable(), instruction);
                break;
            case MUL:
                Operators.mul(ir.getSymbolTable(), instruction);
                break;
            case IDIV:
                Operators.idiv(ir.getSymbolTable(), instruction);
                break;
            case FDIV:
                Operators.fdiv(ir.getSymbolTable(), instruction);
                break;
            case ADD:
                Operators.add(ir.getSymbolTable(), instruction);
                break;
            case SUB:
                Operators.sub(ir.getSymbolTable(), instruction);
                break;
            case MOD:
                Operators.mod(ir.getSymbolTable(), instruction);
                break;
            case EQ:
                Operators.eq(ir.getSymbolTable(), instruction);
                break;
            case NE:
                Operators.ne(ir.getSymbolTable(), instruction);
                break;
            case LT:
                Operators.lt(ir.getSymbolTable(), instruction);
                break;
            case LE:
                Operators.le(ir.getSymbolTable(), instruction);
                break;
            case GT:
                Operators.gt(ir.getSymbolTable(), instruction);
                break;
            case GE:
                Operators.ge(ir.getSymbolTable(), instruction);
                break;
            case NOT:
                Operators.unaryNot(ir.getSymbolTable(), instruction);
                break;
            case AND:
                Operators.and(ir.getSymbolTable(), instruction);
                break;
            case OR:
                Operators.or(ir.getSymbolTable(), instruction);
                break;
            case XOR:
                Operators.xor(ir.getSymbolTable(), instruction);
                break;
            case EQV:
                Operators.eqv(ir.getSymbolTable(), instruction);
                break;
            case IMP:
                Operators.imp(ir.getSymbolTable(), instruction);
                break;
            case END:
                return true;
            case ABS:
                Functions.abs(ir.getSymbolTable(), instruction);
                break;
            case ASC:
                Functions.asc(ir.getSymbolTable(), instruction);
                break;
            case SIN:
                Functions.sin(ir.getSymbolTable(), instruction);
                break;
            case COS:
                Functions.cos(ir.getSymbolTable(), instruction);
                break;
            case TAN:
                Functions.tan(ir.getSymbolTable(), instruction);
                break;
            case ASIN:
                Functions.asin(ir.getSymbolTable(), instruction);
                break;
            case ACOS:
                Functions.acos(ir.getSymbolTable(), instruction);
                break;
            case ATN:
                Functions.atn(ir.getSymbolTable(), instruction);
                break;
            case SINH:
                Functions.sinh(ir.getSymbolTable(), instruction);
                break;
            case COSH:
                Functions.cosh(ir.getSymbolTable(), instruction);
                break;
            case TANH:
                Functions.tanh(ir.getSymbolTable(), instruction);
                break;
            case SQR:
                Functions.sqr(ir.getSymbolTable(), instruction);
                break;
            case LOG:
                Functions.log(ir.getSymbolTable(), instruction);
                break;
            case LOG10:
                Functions.log10(ir.getSymbolTable(), instruction);
                break;
            case LOG2:
                Functions.log2(ir.getSymbolTable(), instruction);
                break;
            case EEXP:
                Functions.exp(ir.getSymbolTable(), instruction);
                break;
            case TORAD:
                Functions.toRad(ir.getSymbolTable(), instruction);
                break;
            case TODEG:
                Functions.toDeg(ir.getSymbolTable(), instruction);
                break;
            case FLOOR:
                Functions.floor(ir.getSymbolTable(), instruction);
                break;
            case CEIL:
                Functions.ceil(ir.getSymbolTable(), instruction);
                break;
            case ROUND:
                Functions.round(ir.getSymbolTable(), instruction);
                break;
            case E:
                Functions.e(ir.getSymbolTable(), instruction);
                break;
            case PI:
                Functions.pi(ir.getSymbolTable(), instruction);
                break;
            case MIN:
                Functions.min(ir.getSymbolTable(), instruction);
                break;
            case MAX:
                Functions.max(ir.getSymbolTable(), instruction);
                break;
            case ARRAYFILL:
                ArraysUtil.arrayfill(ir.getSymbolTable(), instruction);
                break;
            case ARRAYCOPY:
                ArraysUtil.arrayCopy(ir.getSymbolTable(), instruction);
                break;
            case ARRAY1DMIN:
                ArraysUtil.array1dMin(ir.getSymbolTable(), instruction);
                break;
            case ARRAY1DMAX:
                ArraysUtil.array1dMax(ir.getSymbolTable(), instruction);
                break;
            case ARRAY1DMEAN:
                ArraysUtil.array1dMean(ir.getSymbolTable(), instruction);
                break;
            case ARRAY1DSUM:
                ArraysUtil.array1dSum(ir.getSymbolTable(), instruction);
                break;
            case ARRAY1DSTD:
                ArraysUtil.array1dStddev(ir.getSymbolTable(), instruction);
                break;
            case ARRAY1DMEDIAN:
                ArraysUtil.array1dMedian(ir.getSymbolTable(), instruction);
                break;
            case ARRAY1DPCT:
                ArraysUtil.array1dPercentile(ir.getSymbolTable(), instruction);
                break;
            case ARRAY1DSORT:
                ArraysUtil.array1dSort(ir.getSymbolTable(), instruction);
                break;
            case ARRAY1DBINSEARCH:
                ArraysUtil.array1dBinSearch(ir.getSymbolTable(), instruction);
                break;
            case ARRAY2DSHIFTVER:
                ArraysUtil.array2dShiftVertical(ir.getSymbolTable(), instruction);
                break;
            case ARRAY2DSHIFTHOR:
                ArraysUtil.array2dShiftHorizontal(ir.getSymbolTable(), instruction);
                break;
            case ARRAY1DCOPY: {
                if (params.size() != 2) {
                    throw new PuffinBasicInternalError("Expected 2 params, but found: " + params);
                }
                ArraysUtil.array1DCopy(ir.getSymbolTable(), params.get(0), params.get(1), instruction);
                params.clear();
            }
            break;
            case CINT:
                Functions.cint(ir.getSymbolTable(), instruction);
                break;
            case CLNG:
                Functions.clng(ir.getSymbolTable(), instruction);
                break;
            case CSNG:
                Functions.csng(ir.getSymbolTable(), instruction);
                break;
            case CDBL:
                Functions.cdbl(ir.getSymbolTable(), instruction);
                break;
            case CHRDLR:
                Functions.chrdlr(ir.getSymbolTable(), instruction);
                break;
            case CVI:
                Functions.cvi(ir.getSymbolTable(), instruction);
                break;
            case CVL:
                Functions.cvl(ir.getSymbolTable(), instruction);
                break;
            case CVS:
                Functions.cvs(ir.getSymbolTable(), instruction);
                break;
            case CVD:
                Functions.cvd(ir.getSymbolTable(), instruction);
                break;
            case MKIDLR:
                Functions.mkidlr(ir.getSymbolTable(), instruction);
                break;
            case MKLDLR:
                Functions.mkldlr(ir.getSymbolTable(), instruction);
                break;
            case MKSDLR:
                Functions.mksdlr(ir.getSymbolTable(), instruction);
                break;
            case MKDDLR:
                Functions.mkddlr(ir.getSymbolTable(), instruction);
                break;
            case SPACEDLR:
                Functions.spacedlr(ir.getSymbolTable(), instruction);
                break;
            case STRDLR:
                Functions.strdlr(ir.getSymbolTable(), instruction);
                break;
            case VAL:
                Functions.val(ir.getSymbolTable(), instruction);
                break;
            case INT:
                Functions.fnint(ir.getSymbolTable(), instruction);
                break;
            case FIX:
                Functions.fix(ir.getSymbolTable(), instruction);
                break;
            case LEN:
                Functions.len(ir.getSymbolTable(), instruction);
                break;
            case HEXDLR:
                Functions.hexdlr(ir.getSymbolTable(), instruction);
                break;
            case OCTDLR:
                Functions.octdlr(ir.getSymbolTable(), instruction);
                break;
            case LEFTDLR:
                Functions.leftdlr(ir.getSymbolTable(), instruction);
                break;
            case RIGHTDLR:
                Functions.rightdlr(ir.getSymbolTable(), instruction);
                break;
            case PARAM1:
            case PARAM2:
                params.add(instruction);
                break;
            case INSTR: {
                if (params.size() != 1) {
                    throw new PuffinBasicInternalError("Expected 1 param, but found: " + params);
                }
                Functions.instr(ir.getSymbolTable(), params.get(0), instruction);
                params.clear();
            }
                break;
            case MIDDLR: {
                if (params.size() != 1) {
                    throw new PuffinBasicInternalError("Expected 1 param, but found: " + params);
                }
                Functions.middlr(ir.getSymbolTable(), params.get(0), instruction);
                params.clear();
            }
                break;
            case MIDDLR_STMT: {
                if (params.size() != 1) {
                    throw new PuffinBasicInternalError("Expected 1 param, but found: " + params);
                }
                Statements.middlr(ir.getSymbolTable(), params.get(0), instruction);
                params.clear();
            }
            break;
            case OPEN: {
                if (params.size() != 2) {
                    throw new PuffinBasicInternalError("Expected 2 params, but found: " + params);
                }
                Statements.open(files, ir.getSymbolTable(), params.get(0), params.get(1), instruction);
                params.clear();
            }
                break;
            case CLOSE_ALL:
                Statements.closeAll(files);
                break;
            case CLOSE:
                Statements.close(files, ir.getSymbolTable(), instruction);
                break;
            case FIELD: {
                Statements.field(files, ir.getSymbolTable(), params, instruction);
                params.clear();
            }
                break;
            case HSB2RGB: {
                if (params.size() != 1) {
                    throw new PuffinBasicInternalError("Expected 1 param, but found: " + params);
                }
                GraphicsRuntime.hsb2rgb(ir.getSymbolTable(), params.get(0), instruction);
                params.clear();
            }
            break;
            case PUTF:
                Statements.putf(files, ir.getSymbolTable(), instruction);
                break;
            case GETF:
                Statements.getf(files, ir.getSymbolTable(), instruction);
                break;
            case LOC:
                Functions.loc(files, ir.getSymbolTable(), instruction);
                break;
            case LOF:
                Functions.lof(files, ir.getSymbolTable(), instruction);
                break;
            case EOF:
                Functions.eof(files, ir.getSymbolTable(), instruction);
                break;
            case RND:
                Functions.rnd(random, ir.getSymbolTable(), instruction);
                break;
            case RANDOMIZE:
                Statements.randomize(random, ir.getSymbolTable(), instruction);
                break;
            case RANDOMIZE_TIMER:
                Statements.randomizeTimer(random);
                break;
            case SGN:
                Functions.sgn(ir.getSymbolTable(), instruction);
                break;
            case LSET:
                Statements.lset(ir.getSymbolTable(), instruction);
                break;
            case RSET:
                Statements.rset(ir.getSymbolTable(), instruction);
                break;
            case TIMER:
                Functions.timer(ir.getSymbolTable(), instruction);
                break;
            case STRINGDLR:
                Functions.stringdlr(ir.getSymbolTable(), instruction);
                break;
            case SWAP:
                Statements.swap(ir.getSymbolTable(), instruction);
                break;
            case CONCAT:
                Operators.concat(ir.getSymbolTable(), instruction);
                break;
            case INPUTDLR:
                Functions.inputdlr(files, ir.getSymbolTable(), instruction);
                break;
            case INPUT: {
                Statements.input(files, ir.getSymbolTable(), params, instruction);
                params.clear();
            }
                break;
            case LINE_INPUT: {
                if (params.size() != 1) {
                    throw new PuffinBasicInternalError("Expected 1 param, but found: " + params);
                }
                Statements.lineinput(files, ir.getSymbolTable(), params.get(0), instruction);
                params.clear();
            }
            break;
            case WRITE:
                Statements.write(printBuffer, ir.getSymbolTable(), instruction);
                break;
            case DATA:
                break;
            case RESTORE:
                readData.restore();
                break;
            case READ:
                Statements.read(readData, ir.getSymbolTable(), instruction);
                break;
            case ENVIRONDLR:
                Functions.environdlr(env, ir.getSymbolTable(), instruction);
                break;
            case SLEEP:
                Statements.sleep(ir.getSymbolTable(), instruction);
                break;
            case SCREEN: {
                if (params.size() != 1) {
                    throw new PuffinBasicInternalError("Expected 1 param, but found: " + params);
                }
                GraphicsRuntime.screen(graphicsState, ir.getSymbolTable(), params.get(0), instruction);
                params.clear();
            }
            break;
            case REPAINT:
                GraphicsRuntime.repaint(graphicsState);
                break;
            case CIRCLE: {
                if (params.size() != 3) {
                        throw new PuffinBasicInternalError("Expected 3 params, but found: " + params);
                }
                GraphicsRuntime.circle(graphicsState, ir.getSymbolTable(), params, instruction);
                params.clear();
            }
            break;
            case LINE: {
                if (params.size() != 2) {
                    throw new PuffinBasicInternalError("Expected 2 params, but found: " + params);
                }
                GraphicsRuntime.line(graphicsState, ir.getSymbolTable(), params, instruction);
                params.clear();
            }
            break;
            case COLOR: {
                if (params.size() != 1) {
                    throw new PuffinBasicInternalError("Expected 1 params, but found: " + params);
                }
                GraphicsRuntime.color(graphicsState, ir.getSymbolTable(), params.get(0), instruction);
                params.clear();
            }
            break;
            case PAINT: {
                if (params.size() != 2) {
                    throw new PuffinBasicInternalError("Expected 2 params, but found: " + params);
                }
                GraphicsRuntime.paint(graphicsState, ir.getSymbolTable(), params, instruction);
                params.clear();
            }
            break;
            case PSET: {
                if (params.size() != 2) {
                    throw new PuffinBasicInternalError("Expected 2 params, but found: " + params);
                }
                GraphicsRuntime.pset(graphicsState, ir.getSymbolTable(), params, instruction);
                params.clear();
            }
            break;
            case GGET: {
                if (params.size() != 2) {
                    throw new PuffinBasicInternalError("Expected 2 params, but found: " + params);
                }
                GraphicsRuntime.get(graphicsState, ir.getSymbolTable(), params, instruction);
                params.clear();
            }
            break;
            case GPUT: {
                if (params.size() != 1) {
                    throw new PuffinBasicInternalError("Expected 1 param, but found: " + params);
                }
                GraphicsRuntime.put(graphicsState, ir.getSymbolTable(), params.get(0), instruction);
                params.clear();
            }
            break;
            case FONT: {
                if (params.size() != 1) {
                    throw new PuffinBasicInternalError("Expected 1 param, but found: " + params);
                }
                GraphicsRuntime.font(graphicsState, ir.getSymbolTable(), params.get(0), instruction);
                params.clear();
            }
            break;
            case DRAWSTR: {
                if (params.size() != 1) {
                    throw new PuffinBasicInternalError("Expected 1 param, but found: " + params);
                }
                GraphicsRuntime.drawstr(graphicsState, ir.getSymbolTable(), params.get(0), instruction);
                params.clear();
            }
            break;
            case LOADIMG:
                GraphicsRuntime.loadimg(graphicsState, ir.getSymbolTable(), instruction);
                break;
            case SAVEIMG:
                GraphicsRuntime.saveimg(graphicsState, ir.getSymbolTable(), instruction);
                break;
            case DRAW:
                GraphicsRuntime.draw(graphicsState, ir.getSymbolTable(), instruction);
                break;
            case INKEYDLR:
                GraphicsRuntime.inkeydlr(graphicsState, ir.getSymbolTable(), instruction);
                break;
            case CLS:
                GraphicsRuntime.cls(graphicsState);
                break;
            case BEEP:
                GraphicsRuntime.beep();
                break;
            case LOADWAV:
                GraphicsRuntime.loadwav(soundState, ir.getSymbolTable(), instruction);
                break;
            case PLAYWAV:
                GraphicsRuntime.playwav(soundState, ir.getSymbolTable(), instruction);
                break;
            case STOPWAV:
                GraphicsRuntime.stopwav(soundState, ir.getSymbolTable(), instruction);
                break;
            case LOOPWAV:
                GraphicsRuntime.loopwav(soundState, ir.getSymbolTable(), instruction);
                break;
        }

        this.programCounter = nextProgramCounter;
        return false;
    }
}
