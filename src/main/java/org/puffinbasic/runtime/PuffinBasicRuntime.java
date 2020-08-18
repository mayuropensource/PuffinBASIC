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
import org.puffinbasic.runtime.Arrays.ArrayState;
import org.puffinbasic.runtime.Formatter.FormatterCache;
import org.puffinbasic.runtime.Statements.ReadData;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.puffinbasic.domain.PuffinBasicSymbolTable.NULL_ID;
import static org.puffinbasic.parser.PuffinBasicIR.OpCode.DATA;
import static org.puffinbasic.parser.PuffinBasicIR.OpCode.FIELD_I;
import static org.puffinbasic.parser.PuffinBasicIR.OpCode.INPUT_VAR;
import static org.puffinbasic.parser.PuffinBasicIR.OpCode.INSTR0;
import static org.puffinbasic.parser.PuffinBasicIR.OpCode.LABEL;
import static org.puffinbasic.parser.PuffinBasicIR.OpCode.MIDDLR0;
import static org.puffinbasic.parser.PuffinBasicIR.OpCode.OPEN_FN_FN_0;
import static org.puffinbasic.parser.PuffinBasicIR.OpCode.OPEN_OM_AM_1;

public class PuffinBasicRuntime {

    private final PuffinBasicIR ir;
    private PrintBuffer printBuffer;
    private ArrayState arrayState;
    private IntStack gosubReturnLabelStack;
    private int programCounter;
    private Random random;
    private Int2IntMap labelToInstrNum;
    private Int2IntMap lineNumToInstrNum;
    private List<Instruction> instr0;
    private FormatterCache formatterCache;
    private PuffinBasicFiles files;
    private ReadData readData;
    private final PrintStream out;

    public PuffinBasicRuntime(PuffinBasicIR ir, PrintStream out) {
        this.ir = ir;
        this.out = out;
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

    public Int2IntMap computeLineNumberToInstructionNumber(List<Instruction> instructions) {
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
        this.instr0 = new ArrayList<>(4);
        this.files = new PuffinBasicFiles(new SystemInputOutputFile(System.in, out));
        this.readData = processDataInstructions(instructions);

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
                Arrays.resetIndex(arrayState, ir.getSymbolTable(), instruction);
                break;
            case SET_ARRAY_IDX:
                Arrays.setIndex(arrayState, ir.getSymbolTable(), instruction);
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
            case MUL:
            case IDIV:
            case FDIV:
            case ADD:
            case SUB:
            case MOD:
                Operators.arithmetic(ir.getSymbolTable(), instruction);
                break;
            case EQ:
            case NE:
            case LT:
            case LE:
            case GT:
            case GE:
                Operators.relational(ir.getSymbolTable(), instruction);
                break;
            case NOT:
                Operators.unaryNot(ir.getSymbolTable(), instruction);
                break;
            case AND:
            case OR:
            case XOR:
            case EQV:
            case IMP:
                Operators.logical(ir.getSymbolTable(), instruction);
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
            case ATN:
                Functions.atn(ir.getSymbolTable(), instruction);
                break;
            case SQR:
                Functions.sqr(ir.getSymbolTable(), instruction);
                break;
            case LOG:
                Functions.log(ir.getSymbolTable(), instruction);
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
            case MIDDLR0:
            case INSTR0:
            case OPEN_FN_FN_0:
            case OPEN_OM_AM_1:
            case FIELD_I:
            case INPUT_VAR:
                instr0.add(instruction);
                break;
            case INSTR: {
                if (instr0.size() != 1 || instr0.get(0).opCode != INSTR0) {
                    throw new PuffinBasicInternalError("Bad/null instr0: " + instr0.get(0) + ", expected: " + INSTR0);
                }
                Functions.instr(ir.getSymbolTable(), instr0.get(0), instruction);
                instr0.clear();
            }
                break;
            case MIDDLR: {
                if (instr0.size() != 1 || instr0.get(0).opCode != MIDDLR0) {
                    throw new PuffinBasicInternalError("Bad/null instr0: " + instr0.get(0) + ", expected: " + MIDDLR0);
                }
                Functions.middlr(ir.getSymbolTable(), instr0.get(0), instruction);
                instr0.clear();
            }
                break;
            case MIDDLR_STMT: {
                if (instr0.size() != 1 || instr0.get(0).opCode != MIDDLR0) {
                    throw new PuffinBasicInternalError("Bad/null instr0: " + instr0.get(0) + ", expected: " + MIDDLR0);
                }
                Statements.middlr(ir.getSymbolTable(), instr0.get(0), instruction);
                instr0.clear();
            }
            break;
            case OPEN_LM_RL_2: {
                if (instr0.size() != 2
                        || (instr0.get(0).opCode != OPEN_FN_FN_0
                        && instr0.get(1).opCode != OPEN_OM_AM_1)) {
                    throw new PuffinBasicInternalError(
                            "Bad/null instr0: " + instr0.get(0) + ", " + instr0.get(1)
                                    + ", expected: " + OPEN_FN_FN_0 + " and " + OPEN_OM_AM_1);
                }
                Statements.open(files, ir.getSymbolTable(), instr0.get(0), instr0.get(1), instruction);
                instr0.clear();
            }
                break;
            case CLOSE_ALL:
                Statements.closeAll(files);
                break;
            case CLOSE:
                Statements.close(files, ir.getSymbolTable(), instruction);
                break;
            case FIELD: {
                for (var instrI : instr0) {
                    if (instrI.opCode != FIELD_I)
                    throw new PuffinBasicInternalError(
                            "Bad/null instr0: " + instrI + ", expected: " + FIELD_I);
                }
                Statements.field(files, ir.getSymbolTable(), new ArrayList<>(instr0), instruction);
                instr0.clear();
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
                for (var instrI : instr0) {
                    if (instrI.opCode != INPUT_VAR)
                        throw new PuffinBasicInternalError(
                                "Bad/null instr0: " + instrI + ", expected: " + INPUT_VAR);
                }
                Statements.input(files, ir.getSymbolTable(), new ArrayList<>(instr0), instruction);
                instr0.clear();
            }
                break;
            case LINE_INPUT: {
                if (instr0.size() != 1 || instr0.get(0).opCode != INPUT_VAR) {
                    throw new PuffinBasicInternalError(
                            "Bad/null instr0: " + instr0.get(0) + ", expected: " + INPUT_VAR);
                }
                Statements.lineinput(files, ir.getSymbolTable(), instr0.get(0), instruction);
                instr0.clear();
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
        }

        this.programCounter = nextProgramCounter;
        return false;
    }
}
