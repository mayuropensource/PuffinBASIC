package org.beaglebasic.parser;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.beaglebasic.antlr4.BeagleBasicBaseListener;
import org.beaglebasic.antlr4.BeagleBasicParser;
import org.beaglebasic.domain.STObjects;
import org.beaglebasic.domain.STObjects.STUDF;
import org.beaglebasic.domain.Variable;
import org.beaglebasic.domain.Variable.VariableName;
import org.beaglebasic.error.BeagleBasicInternalError;
import org.beaglebasic.error.BeagleBasicSemanticError;
import org.beaglebasic.file.BeagleBasicFile.FileAccessMode;
import org.beaglebasic.file.BeagleBasicFile.FileOpenMode;
import org.beaglebasic.file.BeagleBasicFile.LockMode;
import org.beaglebasic.parser.BeagleBasicIR.Instruction;
import org.beaglebasic.parser.BeagleBasicIR.OpCode;
import org.beaglebasic.runtime.Numbers;
import org.beaglebasic.runtime.Types;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.beaglebasic.domain.BeagleBasicSymbolTable.NULL_ID;
import static org.beaglebasic.domain.STObjects.BeagleBasicDataType.DOUBLE;
import static org.beaglebasic.domain.STObjects.BeagleBasicDataType.FLOAT;
import static org.beaglebasic.domain.STObjects.BeagleBasicDataType.INT32;
import static org.beaglebasic.domain.STObjects.BeagleBasicDataType.INT64;
import static org.beaglebasic.domain.STObjects.BeagleBasicDataType.STRING;
import static org.beaglebasic.error.BeagleBasicSemanticError.ErrorCode.BAD_ASSIGNMENT;
import static org.beaglebasic.error.BeagleBasicSemanticError.ErrorCode.DATA_TYPE_MISMATCH;
import static org.beaglebasic.error.BeagleBasicSemanticError.ErrorCode.FOR_WITHOUT_NEXT;
import static org.beaglebasic.error.BeagleBasicSemanticError.ErrorCode.INSUFFICIENT_UDF_ARGS;
import static org.beaglebasic.error.BeagleBasicSemanticError.ErrorCode.NEXT_WITHOUT_FOR;
import static org.beaglebasic.error.BeagleBasicSemanticError.ErrorCode.WHILE_WITHOUT_WEND;
import static org.beaglebasic.file.BeagleBasicFile.DEFAULT_RECORD_LEN;
import static org.beaglebasic.parser.LinenumberListener.parseLinenum;

/**
 * <PRE>
 * Functions
 * =========
 * ABS              done
 * ASC              done
 * ATN              done
 * CDBL             done
 * CHR$             done
 * CINT             done
 * CLNG             done
 * COS              done
 * CSNG             done
 * CVD              done
 * CVI              done
 * CVI              done
 * CVL              done
 * CVS              done
 * ENVIRON$         NA
 * EOF              done
 * EXP              done
 * EXTERR           NA
 * FIX              done
 * FRE              NA
 * HEX$             done
 * INP              NA
 * INPUT$           done
 * INSTR            done
 * INT              done
 * IOCTL$           NA
 * LEFT$            done
 * LEN              done
 * LOC              done
 * LOF              done
 * LOG              done
 * LPOS             NA
 * MID$             done
 * MKD$             done
 * MKI$             done
 * MKS$             done
 * MKL$             done
 * OCT$             done
 * PEEK             NA
 * PEN              graphics
 * PLAY             graphics
 * PMAP             graphics
 * POINT            graphics
 * POS              graphics
 * RND              done
 * RIGHT$           done
 * SCREEN           graphics
 * SGN              done
 * SIN              done
 * SPACE$           done
 * SPC              NA
 * SQR              done
 * STICK            graphics
 * STR$             done
 * STRING$          done
 * TAB              NA
 * TAN              done
 * TIMER            done
 * VAL              done
 * VARPTR           NA
 * VARPTR$          NA
 *
 * Statements
 * ==========
 * BEEP             graphics
 * CALL             NA
 * CHAIN            NA
 * CIRCLE           graphics
 * CLOSE            done
 * CLS              NA
 * COLOR            graphics
 * COM(n)           NA
 * COMMON           NA
 * DATA             done
 * DATE$            done
 * DEF FN           done
 * DEFINT           done
 * DEFDBL           done
 * DEFLNG           done
 * DEFSNG           done
 * DEFSTR           done
 * DEF SEG          NA
 * DEF USR          NA
 * DIM              done
 * DRAW             graphics
 * END              done
 * ENVIRON          NA
 * ERASE            NA
 * ERROR            NA
 * FIELD            done
 * FOR-NEXT         done
 * GET              done
 * GOSUB-RETURN     done
 * GOTO             done
 * IF-THEN-ELSE     done
 * INPUT            done (not compatible)
 * INPUT#           done
 * IOCTL            NA
 * KEY              graphics
 * KEY(n)           graphics
 * LET              done
 * LINE             graphics
 * LINE INPUT       done
 * LINE INPUT#      done
 * LOCATE           graphics
 * LOCK             NA
 * LPRINT           NA
 * LPRINT USING     NA
 * LSET             done
 * MID$             done
 * ON COM(n)        NA
 * ON KEY(n)        graphics
 * ON PEN(n)        graphics
 * ON PLAY(n)       graphics
 * ON STRIG(n)      graphics
 * ON TIMER(n)      graphics
 * ON ERROR GOTO    NA
 * ON-GOSUB         NA
 * ON-GOTO          NA
 * OPEN             done
 * OPEN COM(n)      NA
 * OPTION BASE      NA
 * OUT              NA
 * PALETTE          graphics
 * PALETTE USING    graphics
 * PEN              graphics
 * PLAY             graphics
 * POKE             NA
 * PRESET           graphics
 * PSET             graphics
 * PRINT            done
 * PRINT USING      done
 * PRINT#           done
 * PRINT# USING     done
 * PUT              done
 * RANDOMIZE        done
 * READ             done
 * REM              done
 * RESTORE          done
 * RESUME           NA
 * RSET             done
 * SCREEN           graphics
 * SHELL            NA
 * STOP             NA
 * STRIG            graphics
 * STRIG(n)         graphics
 * SWAP             done
 * TIME$            done
 * UNLOCK           NA
 * VIEW             graphics
 * VIEW PRINT       graphics
 * WAIT             NA
 * WHILE-WEND       done
 * WIDTH            NA
 * WINDOW           graphics
 * WRITE            done
 * WRTIE#           done
 * </PRE>
 */
public class BeagleBasicIRListener extends BeagleBasicBaseListener {

    private enum NumericOrString {
        NUMERIC,
        STRING
    }

    private final CharStream in;
    private final BeagleBasicIR ir;
    private final ParseTreeProperty<Instruction> nodeToInstruction;
    private final Object2ObjectMap<Variable, UDFState> udfStateMap;
    private final LinkedList<WhileLoopState> whileLoopStateList;
    private final LinkedList<ForLoopState> forLoopStateList;
    private final ParseTreeProperty<IfState> nodeToIfState;
    private int currentLineNumber;

    public BeagleBasicIRListener(CharStream in, BeagleBasicIR ir) {
        this.in = in;
        this.ir = ir;
        this.nodeToInstruction = new ParseTreeProperty<>();
        this.udfStateMap = new Object2ObjectOpenHashMap<>();
        this.whileLoopStateList = new LinkedList<>();
        this.forLoopStateList = new LinkedList<>();
        this.nodeToIfState = new ParseTreeProperty<>();
    }

    public void semanticCheckAfterParsing() {
        if (!whileLoopStateList.isEmpty()) {
            throw new BeagleBasicSemanticError(
                    WHILE_WITHOUT_WEND,
                    "<UNKNOWN LINE>",
                    "WHILE without WEND"
            );
        }
        if (!forLoopStateList.isEmpty()) {
            throw new BeagleBasicSemanticError(
                    FOR_WITHOUT_NEXT,
                    "<UNKNOWN LINE>",
                    "FOR without NEXT"
            );
        }
    }

    private String getCtxString(ParserRuleContext ctx) {
        return in.getText(new Interval(
                ctx.start.getStartIndex(), ctx.stop.getStopIndex()
        ));
    }

    private Instruction lookupInstruction(ParserRuleContext ctx) {
        var exprInstruction = nodeToInstruction.get(ctx);
        if (exprInstruction == null) {
            throw new BeagleBasicInternalError(
                    "Failed to find instruction for node: " + ctx.getText()
            );
        }
        return exprInstruction;
    }

    @Override
    public void enterLine(BeagleBasicParser.LineContext ctx) {
        this.currentLineNumber = parseLinenum(ctx.linenum().DECIMAL().getText());
    }

    //
    // Variable, Number, etc.
    //

    @Override
    public void exitNumber(BeagleBasicParser.NumberContext ctx) {
        final int id;
        if (ctx.integer() != null) {
            final boolean isLong = ctx.integer().AT() != null;
            final String strValue;
            if (ctx.integer().HEXADECIMAL() != null) {
                strValue = "0x" + ctx.integer().HEXADECIMAL().getText().substring(2);
            } else if (ctx.integer().OCTAL() != null) {
                var octalStr = ctx.integer().OCTAL().getText();
                strValue = "0" + (octalStr.startsWith("&o") || octalStr.startsWith("&O")
                        ? octalStr.substring(2) : octalStr.substring(1));
            } else {
                strValue = ctx.integer().DECIMAL().getText();
            }
            if (isLong) {
                id = ir.getSymbolTable().addTmp(INT64,
                        entry -> entry.getValue().setInt64(Numbers.parseInt64(strValue, () -> getCtxString(ctx))));

            } else {
                id = ir.getSymbolTable().addTmp(INT32,
                        entry -> entry.getValue().setInt32(Numbers.parseInt32(strValue, () -> getCtxString(ctx))));

            }
        } else if (ctx.FLOAT() != null) {
            var floatStr = ctx.FLOAT().getText();
            if (floatStr.endsWith("!")) {
                floatStr = floatStr.substring(0, floatStr.length() - 1);
            }
            var floatValue = Numbers.parseFloat32(floatStr, () -> getCtxString(ctx));
            id = ir.getSymbolTable().addTmp(FLOAT,
                    entry -> entry.getValue().setFloat32(floatValue));
        } else {
            var doubleStr = ctx.DOUBLE().getText();
            if (doubleStr.endsWith("#")) {
                doubleStr = doubleStr.substring(0, doubleStr.length() - 1);
            }
            var doubleValue = Numbers.parseFloat64(doubleStr, () -> getCtxString(ctx));
            id = ir.getSymbolTable().addTmp(DOUBLE,
                    entry -> entry.getValue().setFloat64(doubleValue));
        }

        var instr = ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.VALUE, id, NULL_ID, id
        );
        nodeToInstruction.put(ctx, instr);
    }

    @Override
    public void exitVariable(BeagleBasicParser.VariableContext ctx) {

        var varname = ctx.varname().VARNAME().getText();
        var varsuffix = ctx.varsuffix() != null ? ctx.varsuffix().getText() : null;
        var dataType = ir.getSymbolTable().getDataTypeFor(varname, varsuffix);
        var variableName = new VariableName(varname, dataType);

        int id = ir.getSymbolTable().addVariableOrUDF(
                variableName,
                variableName1 -> Variable.of(variableName1, false, () -> getCtxString(ctx)),
                (varId, varEntry) -> {
                    var variable = varEntry.getVariable();
            if (variable.isScalar()) {
                // Scalar
                if (!ctx.expr().isEmpty()) {
                    throw new BeagleBasicSemanticError(
                            BeagleBasicSemanticError.ErrorCode.SCALAR_VARIABLE_CANNOT_BE_INDEXED,
                            getCtxString(ctx),
                            "Scalar variable cannot be indexed: " + variable
                    );
                }
            } else if (variable.isArray()) {
                // Array
                ir.addInstruction(
                        currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                        OpCode.RESET_ARRAY_IDX, varId, NULL_ID, NULL_ID
                );

                for (var exprCtx : ctx.expr()) {
                    var exprInstr = lookupInstruction(exprCtx);
                    ir.addInstruction(
                            currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                            OpCode.SET_ARRAY_IDX, varId, exprInstr.result, NULL_ID
                    );
                }
            } else if (variable.isUDF()) {
                // UDF
                var udfEntry = (STUDF) varEntry;
                var udfState = udfStateMap.get(variable);

                // Create & Push Runtime scope
                var pushScopeInstr = ir.addInstruction(
                        currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                        OpCode.PUSH_RT_SCOPE, varId, NULL_ID, NULL_ID
                );
                // Copy caller params to Runtime scope
                if (ctx.expr().size() != udfEntry.getNumDeclaredParams()) {
                    throw new BeagleBasicSemanticError(
                            INSUFFICIENT_UDF_ARGS,
                            getCtxString(ctx),
                            variable + " expects " + udfEntry.getNumDeclaredParams() +
                                    ", #args passed: " + ctx.expr().size()
                    );
                }
                int i = 0;
                for (var exprCtx : ctx.expr()) {
                    var exprInstr = lookupInstruction(exprCtx);
                    var declParamId = udfEntry.getDeclaraedParam(i++);
                    ir.addInstruction(
                            currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                            OpCode.COPY, declParamId, exprInstr.result, declParamId
                    );
                }
                // GOTO labelFuncStart
                ir.addInstruction(
                        currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                        OpCode.GOTO_LABEL,
                        udfState.labelFuncStart.op1,
                        NULL_ID, NULL_ID
                );
                // LABEL caller return address
                var labelCallerReturn = ir.addInstruction(
                        currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                        OpCode.LABEL, ir.getSymbolTable().addLabel(), NULL_ID, NULL_ID
                );
                // Patch address of the caller
                pushScopeInstr.patchOp2(labelCallerReturn.op1);
                // Pop Runtime scope
                ir.addInstruction(
                        currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                        OpCode.POP_RT_SCOPE, varId, NULL_ID, NULL_ID
                );
            }
        });

        nodeToInstruction.put(ctx, ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.VARIABLE, id, NULL_ID, id
        ));
    }

    //
    // Expr
    //

    private void copyAndRegisterExprResult(ParserRuleContext ctx, Instruction instruction) {
        var copy = ir.getSymbolTable().addTmpCompatibleWith(instruction.result);
        nodeToInstruction.put(ctx, ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.COPY, copy, instruction.result, copy
        ));
    }

    @Override
    public void exitExprVariable(BeagleBasicParser.ExprVariableContext ctx) {
        var instruction = nodeToInstruction.get(ctx.variable());
        if (ctx.MINUS() != null) {
            if (ir.getSymbolTable().get(instruction.result).getValue().getDataType() == STRING) {
                throw new BeagleBasicSemanticError(
                        DATA_TYPE_MISMATCH,
                        getCtxString(ctx),
                        "Unary minus cannot be used with a String!"
                );
            }
            instruction = ir.addInstruction(
                    currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                    OpCode.UNARY_MINUS, instruction.result, NULL_ID,
                    ir.getSymbolTable().addTmpCompatibleWith(instruction.result)
            );
        }
        copyAndRegisterExprResult(ctx, instruction);
    }

    @Override
    public void exitExprNumber(BeagleBasicParser.ExprNumberContext ctx) {
        var instruction = nodeToInstruction.get(ctx.number());
        if (ctx.MINUS() != null) {
            instruction = ir.addInstruction(
                    currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                    OpCode.UNARY_MINUS, instruction.result, NULL_ID,
                    ir.getSymbolTable().addTmpCompatibleWith(instruction.result)
            );
        }
        copyAndRegisterExprResult(ctx, instruction);
    }

    @Override
    public void exitExprFunc(BeagleBasicParser.ExprFuncContext ctx) {
        var instruction = nodeToInstruction.get(ctx.func());
        if (ctx.MINUS() != null) {
            instruction = ir.addInstruction(
                    currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                    OpCode.UNARY_MINUS, instruction.result, NULL_ID,
                    ir.getSymbolTable().addTmpCompatibleWith(instruction.result)
            );
        }
        copyAndRegisterExprResult(ctx, instruction);
    }

    @Override
    public void exitExprString(BeagleBasicParser.ExprStringContext ctx) {
        var text = Types.unquote(ctx.string().STRING().getText());
        var id = ir.getSymbolTable().addTmp(STRING,
                entry -> entry.getValue().setString(text));
        copyAndRegisterExprResult(ctx, ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.VALUE, id, NULL_ID, id
        ));
    }

    @Override
    public void exitExprExp(BeagleBasicParser.ExprExpContext ctx) {
        addArithmeticOpExpr(ctx, OpCode.EXP, ctx.expr(0), ctx.expr(1));
    }

    @Override
    public void exitExprMul(BeagleBasicParser.ExprMulContext ctx) {
        addArithmeticOpExpr(ctx, OpCode.MUL, ctx.expr(0), ctx.expr(1));
    }

    @Override
    public void exitExprIntDiv(BeagleBasicParser.ExprIntDivContext ctx) {
        addArithmeticOpExpr(ctx, OpCode.IDIV, ctx.expr(0), ctx.expr(1));
    }

    @Override
    public void exitExprFloatDiv(BeagleBasicParser.ExprFloatDivContext ctx) {
        addArithmeticOpExpr(ctx, OpCode.FDIV, ctx.expr(0), ctx.expr(1));
    }

    @Override
    public void exitExprMod(BeagleBasicParser.ExprModContext ctx) {
        addArithmeticOpExpr(ctx, OpCode.MOD, ctx.expr(0), ctx.expr(1));
    }

    @Override
    public void exitExprPlus(BeagleBasicParser.ExprPlusContext ctx) {
        var expr1 = ctx.expr(0);
        var expr2 = ctx.expr(1);
        int instr1res = lookupInstruction(expr1).result;
        int instr2res = lookupInstruction(expr2).result;
        var dt1 = ir.getSymbolTable().get(instr1res).getValue().getDataType();
        var dt2 = ir.getSymbolTable().get(instr2res).getValue().getDataType();
        if (dt1 == STRING && dt2 == STRING) {
            nodeToInstruction.put(ctx, ir.addInstruction(
                    currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                    OpCode.CONCAT, instr1res, instr2res,
                    ir.getSymbolTable().addTmp(STRING, e -> {})
            ));
        } else {
            addArithmeticOpExpr(ctx, OpCode.ADD, expr1, expr2);
        }
    }

    @Override
    public void exitExprMinus(BeagleBasicParser.ExprMinusContext ctx) {
        addArithmeticOpExpr(ctx, OpCode.SUB, ctx.expr(0), ctx.expr(1));
    }

    private void addArithmeticOpExpr(
            ParserRuleContext parent, OpCode opCode, BeagleBasicParser.ExprContext exprLeft, BeagleBasicParser.ExprContext exprRight) {
        var exprL = lookupInstruction(exprLeft);
        var exprR = lookupInstruction(exprRight);
        var dt1 = ir.getSymbolTable().get(exprL.result).getValue().getDataType();
        var dt2 = ir.getSymbolTable().get(exprR.result).getValue().getDataType();
        Types.assertNumeric(dt1, dt2, () -> getCtxString(parent));
        var result = ir.getSymbolTable().addTmp(
                Types.upcast(dt1,
                        ir.getSymbolTable().get(exprR.result).getValue().getDataType(),
                        () -> getCtxString(parent)),
                e -> {});
        nodeToInstruction.put(parent, ir.addInstruction(
                currentLineNumber, parent.start.getStartIndex(), parent.stop.getStopIndex(),
                opCode, exprL.result, exprR.result, result
        ));
    }

    @Override
    public void exitExprRelEq(BeagleBasicParser.ExprRelEqContext ctx) {
        addRelationalOpExpr(ctx, OpCode.EQ, ctx.expr(0), ctx.expr(1));
    }

    @Override
    public void exitExprRelNeq(BeagleBasicParser.ExprRelNeqContext ctx) {
        addRelationalOpExpr(ctx, OpCode.NE, ctx.expr(0), ctx.expr(1));
    }

    @Override
    public void exitExprRelLt(BeagleBasicParser.ExprRelLtContext ctx) {
        addRelationalOpExpr(ctx, OpCode.LT, ctx.expr(0), ctx.expr(1));
    }

    @Override
    public void exitExprRelLe(BeagleBasicParser.ExprRelLeContext ctx) {
        addRelationalOpExpr(ctx, OpCode.LE, ctx.expr(0), ctx.expr(1));
    }

    @Override
    public void exitExprRelGt(BeagleBasicParser.ExprRelGtContext ctx) {
        addRelationalOpExpr(ctx, OpCode.GT, ctx.expr(0), ctx.expr(1));
    }

    @Override
    public void exitExprRelGe(BeagleBasicParser.ExprRelGeContext ctx) {
        addRelationalOpExpr(ctx, OpCode.GE, ctx.expr(0), ctx.expr(1));
    }

    private void addRelationalOpExpr(
            ParserRuleContext parent, OpCode opCode, BeagleBasicParser.ExprContext exprLeft, BeagleBasicParser.ExprContext exprRight) {
        var exprL = lookupInstruction(exprLeft);
        var exprR = lookupInstruction(exprRight);

        checkDataTypeMatch(exprL.result, exprR.result, () -> getCtxString(parent));
        var result = ir.getSymbolTable().addTmp(INT64, e -> {});
        nodeToInstruction.put(parent, ir.addInstruction(
                currentLineNumber, parent.start.getStartIndex(), parent.stop.getStopIndex(),
                opCode, exprL.result, exprR.result, result
        ));
    }

    @Override
    public void exitExprLogNot(BeagleBasicParser.ExprLogNotContext ctx) {
        var expr = lookupInstruction(ctx.expr());
        Types.assertNumeric(
                ir.getSymbolTable().get(expr.result).getValue().getDataType(),
                () -> getCtxString(ctx)
        );
        var result = ir.getSymbolTable().addTmp(INT64, e -> {});
        nodeToInstruction.put(ctx, ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.NOT, expr.result, NULL_ID, result
        ));
    }

    @Override
    public void exitExprLogAnd(BeagleBasicParser.ExprLogAndContext ctx) {
        addLogicalOpExpr(ctx, OpCode.AND, ctx.expr(0), ctx.expr(1));
    }

    @Override
    public void exitExprLogOr(BeagleBasicParser.ExprLogOrContext ctx) {
        addLogicalOpExpr(ctx, OpCode.OR, ctx.expr(0), ctx.expr(1));
    }

    @Override
    public void exitExprLogXor(BeagleBasicParser.ExprLogXorContext ctx) {
        addLogicalOpExpr(ctx, OpCode.XOR, ctx.expr(0), ctx.expr(1));
    }

    @Override
    public void exitExprLogEqv(BeagleBasicParser.ExprLogEqvContext ctx) {
        addLogicalOpExpr(ctx, OpCode.EQV, ctx.expr(0), ctx.expr(1));
    }

    @Override
    public void exitExprLogImp(BeagleBasicParser.ExprLogImpContext ctx) {
        addLogicalOpExpr(ctx, OpCode.IMP, ctx.expr(0), ctx.expr(1));
    }

    private void addLogicalOpExpr(
            ParserRuleContext parent, OpCode opCode, BeagleBasicParser.ExprContext exprLeft, BeagleBasicParser.ExprContext exprRight) {
        var exprL = lookupInstruction(exprLeft);
        var exprR = lookupInstruction(exprRight);
        Types.assertNumeric(
                ir.getSymbolTable().get(exprL.result).getValue().getDataType(),
                ir.getSymbolTable().get(exprR.result).getValue().getDataType(),
                () -> getCtxString(parent)
        );
        var result = ir.getSymbolTable().addTmp(INT64, e -> {});
        nodeToInstruction.put(parent, ir.addInstruction(
                currentLineNumber, parent.start.getStartIndex(), parent.stop.getStopIndex(),
                opCode, exprL.result, exprR.result, result
        ));
    }

    //
    // Functions
    //

    @Override
    public void exitFuncAbs(BeagleBasicParser.FuncAbsContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.ABS, ctx, ctx.expr(),
                NumericOrString.NUMERIC));
    }

    @Override
    public void exitFuncAsc(BeagleBasicParser.FuncAscContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.ASC, ctx, ctx.expr(),
                NumericOrString.STRING,
                ir.getSymbolTable().addTmp(INT32, c -> {})));
    }

    @Override
    public void exitFuncSin(BeagleBasicParser.FuncSinContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.SIN, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(DOUBLE, c -> {})));
    }

    @Override
    public void exitFuncCos(BeagleBasicParser.FuncCosContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.COS, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(DOUBLE, c -> {})));
    }

    @Override
    public void exitFuncTan(BeagleBasicParser.FuncTanContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.TAN, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(DOUBLE, c -> {})));
    }

    @Override
    public void exitFuncAtn(BeagleBasicParser.FuncAtnContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.ATN, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(DOUBLE, c -> {})));
    }

    @Override
    public void exitFuncSqr(BeagleBasicParser.FuncSqrContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.SQR, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(DOUBLE, c -> {})));
    }

    @Override
    public void exitFuncCint(BeagleBasicParser.FuncCintContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.CINT, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(INT32, c -> {})));
    }

    @Override
    public void exitFuncClng(BeagleBasicParser.FuncClngContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.CLNG, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(INT64, c -> {})));
    }

    @Override
    public void exitFuncCsng(BeagleBasicParser.FuncCsngContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.CSNG, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(FLOAT, c -> {})));
    }

    @Override
    public void exitFuncCdbl(BeagleBasicParser.FuncCdblContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.CDBL, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(DOUBLE, c -> {})));
    }

    @Override
    public void exitFuncCvi(BeagleBasicParser.FuncCviContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.CVI, ctx, ctx.expr(),
                NumericOrString.STRING,
                ir.getSymbolTable().addTmp(INT32, c -> {})));
    }

    @Override
    public void exitFuncCvl(BeagleBasicParser.FuncCvlContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.CVL, ctx, ctx.expr(),
                NumericOrString.STRING,
                ir.getSymbolTable().addTmp(INT64, c -> {})));
    }

    @Override
    public void exitFuncCvs(BeagleBasicParser.FuncCvsContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.CVS, ctx, ctx.expr(),
                NumericOrString.STRING,
                ir.getSymbolTable().addTmp(FLOAT, c -> {})));
    }

    @Override
    public void exitFuncCvd(BeagleBasicParser.FuncCvdContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.CVD, ctx, ctx.expr(),
                NumericOrString.STRING,
                ir.getSymbolTable().addTmp(DOUBLE, c -> {})));
    }

    @Override
    public void exitFuncMkiDlr(BeagleBasicParser.FuncMkiDlrContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.MKIDLR, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(STRING, c -> {})));
    }

    @Override
    public void exitFuncMklDlr(BeagleBasicParser.FuncMklDlrContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.MKLDLR, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(STRING, c -> {})));
    }

    @Override
    public void exitFuncMksDlr(BeagleBasicParser.FuncMksDlrContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.MKSDLR, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(STRING, c -> {})));
    }

    @Override
    public void exitFuncMkdDlr(BeagleBasicParser.FuncMkdDlrContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.MKDDLR, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(STRING, c -> {})));
    }

    @Override
    public void exitFuncSpaceDlr(BeagleBasicParser.FuncSpaceDlrContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.SPACEDLR, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(STRING, c -> {})));
    }

    @Override
    public void exitFuncStrDlr(BeagleBasicParser.FuncStrDlrContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.STRDLR, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(STRING, c -> {})));
    }

    @Override
    public void exitFuncVal(BeagleBasicParser.FuncValContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.VAL
                , ctx, ctx.expr(),
                NumericOrString.STRING,
                ir.getSymbolTable().addTmp(DOUBLE, c -> {})));
    }

    @Override
    public void exitFuncInt(BeagleBasicParser.FuncIntContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.INT, ctx, ctx.expr(),
                NumericOrString.NUMERIC));
    }

    @Override
    public void exitFuncFix(BeagleBasicParser.FuncFixContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.FIX, ctx, ctx.expr(),
                NumericOrString.NUMERIC));
    }

    @Override
    public void exitFuncLog(BeagleBasicParser.FuncLogContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.LOG, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(INT64, c -> {})));
    }

    @Override
    public void exitFuncLen(BeagleBasicParser.FuncLenContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.LEN, ctx, ctx.expr(),
                NumericOrString.STRING,
                ir.getSymbolTable().addTmp(INT32, c -> {})));
    }

    @Override
    public void exitFuncChrDlr(BeagleBasicParser.FuncChrDlrContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.CHRDLR, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(STRING, c -> {})));
    }

    @Override
    public void exitFuncHexDlr(BeagleBasicParser.FuncHexDlrContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.HEXDLR, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(STRING, c -> {})));
    }

    @Override
    public void exitFuncOctDlr(BeagleBasicParser.FuncOctDlrContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.OCTDLR, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(STRING, c -> {})));
    }

    @Override
    public void exitFuncLeftDlr(BeagleBasicParser.FuncLeftDlrContext ctx) {
        var xdlr = lookupInstruction(ctx.expr(0));
        var n = lookupInstruction(ctx.expr(1));
        Types.assertString(ir.getSymbolTable().get(xdlr.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        Types.assertNumeric(ir.getSymbolTable().get(n.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        nodeToInstruction.put(ctx, ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.LEFTDLR, xdlr.result, n.result,
                ir.getSymbolTable().addTmp(STRING, c -> {})));
    }

    @Override
    public void exitFuncRightDlr(BeagleBasicParser.FuncRightDlrContext ctx) {
        var xdlr = lookupInstruction(ctx.expr(0));
        var n = lookupInstruction(ctx.expr(1));
        Types.assertString(ir.getSymbolTable().get(xdlr.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        Types.assertNumeric(ir.getSymbolTable().get(n.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        nodeToInstruction.put(ctx, ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.RIGHTDLR, xdlr.result, n.result,
                ir.getSymbolTable().addTmp(STRING, c -> {})));
    }

    @Override
    public void exitFuncInstr(BeagleBasicParser.FuncInstrContext ctx) {
        int xdlr, ydlr, n;
        if (ctx.expr().size() == 3) {
            // n, x$, y$
            n = lookupInstruction(ctx.expr(0)).result;
            xdlr = lookupInstruction(ctx.expr(1)).result;
            ydlr = lookupInstruction(ctx.expr(2)).result;
            Types.assertNumeric(ir.getSymbolTable().get(n).getValue().getDataType(),
                    () -> getCtxString(ctx));
        } else {
            // x$, y$
            n = ir.getSymbolTable().addTmp(INT32, e -> e.getValue().setInt32(1));
            xdlr = lookupInstruction(ctx.expr(0)).result;
            ydlr = lookupInstruction(ctx.expr(1)).result;
        }
        Types.assertString(ir.getSymbolTable().get(xdlr).getValue().getDataType(),
                () -> getCtxString(ctx));
        Types.assertString(ir.getSymbolTable().get(ydlr).getValue().getDataType(),
                () -> getCtxString(ctx));
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.INSTR0, xdlr, ydlr, NULL_ID);
        nodeToInstruction.put(ctx, ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.INSTR, n, NULL_ID,
                ir.getSymbolTable().addTmp(INT32, c -> {})));
    }

    @Override
    public void exitFuncMidDlr(BeagleBasicParser.FuncMidDlrContext ctx) {
        int xdlr, n, m;
        if (ctx.expr().size() == 3) {
            // x$, n, m
            xdlr = lookupInstruction(ctx.expr(0)).result;
            n = lookupInstruction(ctx.expr(1)).result;
            m = lookupInstruction(ctx.expr(2)).result;
            Types.assertNumeric(ir.getSymbolTable().get(m).getValue().getDataType(),
                    () -> getCtxString(ctx));
        } else {
            // x$, n
            xdlr = lookupInstruction(ctx.expr(0)).result;
            n = lookupInstruction(ctx.expr(1)).result;
            m = ir.getSymbolTable().addTmp(INT32, e -> e.getValue().setInt32(Integer.MAX_VALUE));
        }
        Types.assertString(ir.getSymbolTable().get(xdlr).getValue().getDataType(),
                () -> getCtxString(ctx));
        Types.assertNumeric(ir.getSymbolTable().get(n).getValue().getDataType(),
                () -> getCtxString(ctx));
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.MIDDLR0, xdlr, n, NULL_ID);
        nodeToInstruction.put(ctx, ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.MIDDLR, m, NULL_ID,
                ir.getSymbolTable().addTmp(STRING, c -> {})));
    }

    @Override
    public void exitFuncRnd(BeagleBasicParser.FuncRndContext ctx) {
        nodeToInstruction.put(ctx, ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.RND, NULL_ID, NULL_ID,
                ir.getSymbolTable().addTmp(DOUBLE, c -> {})));
    }

    @Override
    public void exitFuncSgn(BeagleBasicParser.FuncSgnContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.SGN, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(INT32, c -> {})));
    }

    @Override
    public void exitFuncTimer(BeagleBasicParser.FuncTimerContext ctx) {
        nodeToInstruction.put(ctx, ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.TIMER, NULL_ID, NULL_ID,
                ir.getSymbolTable().addTmp(DOUBLE, c -> {})));
    }

    @Override
    public void exitFuncStringDlr(BeagleBasicParser.FuncStringDlrContext ctx) {
        int n = lookupInstruction(ctx.expr(0)).result;
        int jOrxdlr = lookupInstruction(ctx.expr(1)).result;
        Types.assertNumeric(ir.getSymbolTable().get(n).getValue().getDataType(),
                () -> getCtxString(ctx));
        nodeToInstruction.put(ctx, ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.STRINGDLR, n, jOrxdlr,
                ir.getSymbolTable().addTmp(STRING, c -> {})));
    }

    @Override
    public void exitFuncLoc(BeagleBasicParser.FuncLocContext ctx) {
        var fileNumber = lookupInstruction(ctx.expr());
        Types.assertNumeric(ir.getSymbolTable().get(fileNumber.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        nodeToInstruction.put(ctx, ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.LOC, fileNumber.result, NULL_ID,
                ir.getSymbolTable().addTmp(INT32, c -> {})));
    }

    @Override
    public void exitFuncLof(BeagleBasicParser.FuncLofContext ctx) {
        var fileNumber = lookupInstruction(ctx.expr());
        Types.assertNumeric(ir.getSymbolTable().get(fileNumber.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        nodeToInstruction.put(ctx, ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.LOF, fileNumber.result, NULL_ID,
                ir.getSymbolTable().addTmp(INT64, c -> {})));
    }

    @Override
    public void exitFuncEof(BeagleBasicParser.FuncEofContext ctx) {
        var fileNumber = lookupInstruction(ctx.expr());
        Types.assertNumeric(ir.getSymbolTable().get(fileNumber.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        nodeToInstruction.put(ctx, ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.EOF, fileNumber.result, NULL_ID,
                ir.getSymbolTable().addTmp(INT32, c -> {})));
    }

    @Override
    public void exitFuncInputDlr(BeagleBasicParser.FuncInputDlrContext ctx) {
        var x = lookupInstruction(ctx.expr(0));
        Types.assertNumeric(ir.getSymbolTable().get(x.result).getValue().getDataType(),
                () -> getCtxString(ctx));

        int fileNumberId;
        if (ctx.expr().size() == 2) {
            var fileNumber = lookupInstruction(ctx.expr(1));
            Types.assertNumeric(ir.getSymbolTable().get(fileNumber.result).getValue().getDataType(),
                    () -> getCtxString(ctx));
            fileNumberId = fileNumber.result;
        } else {
            fileNumberId = ir.getSymbolTable().addTmp(INT32, e -> e.getValue().setInt32(-1));
        }
        nodeToInstruction.put(ctx, ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.INPUTDLR, x.result, fileNumberId,
                ir.getSymbolTable().addTmp(STRING, c -> {})));
    }

    private Instruction addFuncWithExprInstruction(
            OpCode opCode, ParserRuleContext parent,
            BeagleBasicParser.ExprContext expr, NumericOrString numericOrString)
    {
        var exprInstruction = lookupInstruction(expr);
        assertNumericOrString(exprInstruction.result, parent, numericOrString);
        return ir.addInstruction(
                currentLineNumber, parent.start.getStartIndex(), parent.stop.getStopIndex(),
                opCode, exprInstruction.result, NULL_ID,
                ir.getSymbolTable().addTmpCompatibleWith(exprInstruction.result)
        );
    }

    private Instruction addFuncWithExprInstruction(
            OpCode opCode,
            ParserRuleContext parent,
            BeagleBasicParser.ExprContext expr,
            NumericOrString numericOrString,
            int result)
    {
        var exprInstruction = lookupInstruction(expr);
        assertNumericOrString(exprInstruction.result, parent, numericOrString);
        return ir.addInstruction(
                currentLineNumber, parent.start.getStartIndex(), parent.stop.getStopIndex(),
                opCode, exprInstruction.result, NULL_ID, result
        );
    }

    private void assertNumericOrString(int id, ParserRuleContext parent, NumericOrString numericOrString) {
        var dt = ir.getSymbolTable().get(id).getValue().getDataType();
        if (numericOrString == NumericOrString.NUMERIC) {
            Types.assertNumeric(dt, () -> getCtxString(parent));
        } else {
            Types.assertString(dt, () -> getCtxString(parent));
        }
    }

    //
    // Stmt
    //

    @Override
    public void exitLetstmt(BeagleBasicParser.LetstmtContext ctx) {
        var varname = ctx.variable().varname().VARNAME().getText();
        var varsuffix = ctx.variable().varsuffix() != null ? ctx.variable().varsuffix().getText() : null;
        var dataType = ir.getSymbolTable().getDataTypeFor(varname, varsuffix);
        var variableName = new VariableName(varname, dataType);

        var exprInstruction = lookupInstruction(ctx.expr());

        final int varId = ir.getSymbolTable().addVariableOrUDF(
                variableName,
                variableName1 -> Variable.of(variableName1, false, () -> getCtxString(ctx)),
                (id, varEntry) -> {
                    var variable = varEntry.getVariable();
                    if (variable.isUDF()) {
                        throw new BeagleBasicSemanticError(
                                BAD_ASSIGNMENT,
                                getCtxString(ctx),
                                "Can't assign to UDF: " + variable
                        );
                    }
            checkDataTypeMatch(varEntry.getValue(), exprInstruction.result, () -> getCtxString(ctx));
        });

        var assignInstruction = ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.ASSIGN, varId, exprInstruction.result, varId
        );
        nodeToInstruction.put(ctx, assignInstruction);
    }


    @Override
    public void exitPrintstmt(BeagleBasicParser.PrintstmtContext ctx) {
        handlePrintstmt(ctx, ctx.printlist().children, null);
    }

    @Override
    public void exitPrinthashstmt(BeagleBasicParser.PrinthashstmtContext ctx) {
        var fileNumber = lookupInstruction(ctx.filenum);
        handlePrintstmt(ctx, ctx.printlist().children, fileNumber);
    }

    private void handlePrintstmt(
            ParserRuleContext ctx,
            List<ParseTree> children,
            @Nullable Instruction fileNumber)
    {
        boolean endsWithNewline = true;
        for (ParseTree child : children) {
            if (child instanceof BeagleBasicParser.ExprContext) {
                var exprInstruction = lookupInstruction((BeagleBasicParser.ExprContext) child);
                ir.addInstruction(
                        currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                        OpCode.PRINT, exprInstruction.result, NULL_ID, NULL_ID
                );
                endsWithNewline = true;
            } else {
                endsWithNewline = false;
            }
        }

        if (endsWithNewline || fileNumber != null) {
            var newlineId = ir.getSymbolTable().addTmp(STRING,
                    entry -> entry.getValue().setString(System.lineSeparator()));
            ir.addInstruction(
                    currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                    OpCode.PRINT, newlineId, NULL_ID, NULL_ID
            );
        }

        final int fileNumberId;
        if (fileNumber != null) {
            Types.assertNumeric(ir.getSymbolTable().get(fileNumber.result).getValue().getDataType(),
                    () -> getCtxString(ctx));
            fileNumberId = fileNumber.result;
        } else {
            fileNumberId = NULL_ID;
        }
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.FLUSH, fileNumberId, NULL_ID, NULL_ID
        );
    }

    @Override
    public void exitPrintusingstmt(BeagleBasicParser.PrintusingstmtContext ctx) {
        handlePrintusing(ctx, ctx.format, ctx.printlist().children, null);
    }

    @Override
    public void exitPrinthashusingstmt(BeagleBasicParser.PrinthashusingstmtContext ctx) {
        var fileNumber = lookupInstruction(ctx.filenum);
        handlePrintusing(ctx, ctx.format, ctx.printlist().children, fileNumber);
    }

    private void handlePrintusing(
            ParserRuleContext ctx,
            BeagleBasicParser.ExprContext formatCtx,
            List<ParseTree> children,
            Instruction fileNumber)
    {
        var format = lookupInstruction(formatCtx);
        boolean endsWithNewline = true;
        for (ParseTree child : children) {
            if (child instanceof BeagleBasicParser.ExprContext) {
                var exprInstruction = lookupInstruction((BeagleBasicParser.ExprContext) child);
                ir.addInstruction(
                        currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                        OpCode.PRINTUSING, format.result, exprInstruction.result, NULL_ID
                );
                endsWithNewline = true;
            } else {
                endsWithNewline = false;
            }
        }
        if (endsWithNewline || fileNumber != null) {
            var newlineId = ir.getSymbolTable().addTmp(STRING,
                    entry -> entry.getValue().setString(System.lineSeparator()));
            ir.addInstruction(
                    currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                    OpCode.PRINT, newlineId, NULL_ID, NULL_ID
            );
        }

        final int fileNumberId;
        if (fileNumber != null) {
            Types.assertNumeric(ir.getSymbolTable().get(fileNumber.result).getValue().getDataType(),
                    () -> getCtxString(ctx));
            fileNumberId = fileNumber.result;
        } else {
            fileNumberId = NULL_ID;
        }

        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.FLUSH, fileNumberId, NULL_ID, NULL_ID
        );
    }

    @Override
    public void exitDimstmt(BeagleBasicParser.DimstmtContext ctx) {
        IntList dims = new IntArrayList(ctx.DECIMAL().size());
        for (var dimMax : ctx.DECIMAL()) {
            int dimSize = Numbers.parseInt32(dimMax.getText(), () -> getCtxString(ctx)) + 1;
            dims.add(dimSize);
        }
        var varname = ctx.varname().VARNAME().getText();
        var varsuffix = ctx.varsuffix() != null ? ctx.varsuffix().getText() : null;
        var dataType = ir.getSymbolTable().getDataTypeFor(varname, varsuffix);
        var variableName = new VariableName(varname, dataType);
        ir.getSymbolTable().addVariableOrUDF(
                variableName,
                variableName1 -> Variable.of(variableName1, true, () -> getCtxString(ctx)),
                (id, entry) -> entry.getValue().setArrayDimensions(dims));
    }

    @Override
    public void enterDeffnstmt(BeagleBasicParser.DeffnstmtContext ctx) {
        var varname = ctx.varname().getText();
        var varsuffix = ctx.varsuffix() != null ? ctx.varsuffix().getText() : null;
        var dataType = ir.getSymbolTable().getDataTypeFor(varname, varsuffix);
        var variableName = new VariableName(varname, dataType);

        ir.getSymbolTable().addVariableOrUDF(variableName,
                variableName1 -> Variable.of(variableName1, false, () -> getCtxString(ctx)),
                (varId, varEntry) -> {
                    var udfState = new UDFState();
                    udfStateMap.put(varEntry.getVariable(), udfState);

                    // GOTO postFuncDecl
                    udfState.gotoPostFuncDecl = ir.addInstruction(
                            currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                            OpCode.GOTO_LABEL,
                            ir.getSymbolTable().addGotoTarget(),
                            NULL_ID, NULL_ID
                    );
                    // LABEL FuncStart
                    udfState.labelFuncStart = ir.addInstruction(
                            currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                            OpCode.LABEL, ir.getSymbolTable().addLabel(), NULL_ID, NULL_ID
                    );
                    // Push child scope
                    ir.getSymbolTable().pushDeclarationScope(varId);
                });
    }

    @Override
    public void exitDeffnstmt(BeagleBasicParser.DeffnstmtContext ctx) {
        var varname = ctx.varname().getText();
        var varsuffix = ctx.varsuffix() != null ? ctx.varsuffix().getText() : null;
        var dataType = ir.getSymbolTable().getDataTypeFor(varname, varsuffix);
        var variableName = new VariableName(varname, dataType);

        ir.getSymbolTable().addVariableOrUDF(variableName,
                variableName1 -> Variable.of(variableName1, false, () -> getCtxString(ctx)),
                (varId, varEntry) -> {
                    var udfEntry = (STUDF) varEntry;
                    var udfState = udfStateMap.get(varEntry.getVariable());
                    for (BeagleBasicParser.VariableContext fnParamCtx : ctx.variable()) {
                        var fnParamInstr = lookupInstruction(fnParamCtx);
                        udfEntry.declareParam(fnParamInstr.result);
                    }

                    var exprInstr = lookupInstruction(ctx.expr());
                    checkDataTypeMatch(varId, exprInstr.result, () -> getCtxString(ctx));

                    // Copy expr to result
                    ir.addInstruction(
                            currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                            OpCode.COPY, varId, exprInstr.result, varId
                    );
                    // Pop declaration scope
                    ir.getSymbolTable().popScope();
                    // GOTO Caller
                    ir.addInstruction(
                            currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                            OpCode.GOTO_CALLER, NULL_ID, NULL_ID, NULL_ID
                    );
                    // LABEL postFuncDecl
                    var labelPostFuncDecl = ir.addInstruction(
                            currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                            OpCode.LABEL, ir.getSymbolTable().addLabel(), NULL_ID, NULL_ID
                    );
                    // Patch GOTO postFuncDecl
                    udfState.gotoPostFuncDecl.patchOp1(labelPostFuncDecl.op1);
                });
    }

    @Override
    public void exitEndstmt(BeagleBasicParser.EndstmtContext ctx) {
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.END, NULL_ID, NULL_ID,NULL_ID
        );
    }

    @Override
    public void enterWhilestmt(BeagleBasicParser.WhilestmtContext ctx) {
        var whileLoopState = new WhileLoopState();
        // LABEL beforeWhile
        whileLoopState.labelBeforeWhile = ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.LABEL, ir.getSymbolTable().addLabel(), NULL_ID, NULL_ID
        );
        whileLoopStateList.add(whileLoopState);
    }

    @Override
    public void exitWhilestmt(BeagleBasicParser.WhilestmtContext ctx) {
        var whileLoopState = whileLoopStateList.getLast();

        // expr()
        var expr = lookupInstruction(ctx.expr());

        // NOT expr()
        var notExpr = ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.NOT, expr.result, NULL_ID, ir.getSymbolTable().addTmp(INT64, e -> {})
        );

        // If expr is false, GOTO afterWend
        whileLoopState.gotoAfterWend = ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.GOTO_LABEL_IF, notExpr.result, ir.getSymbolTable().addLabel(), NULL_ID
        );
    }

    @Override
    public void exitWendstmt(BeagleBasicParser.WendstmtContext ctx) {
        if (whileLoopStateList.isEmpty()) {
            throw new BeagleBasicSemanticError(
                    BeagleBasicSemanticError.ErrorCode.WEND_WITHOUT_WHILE,
                    getCtxString(ctx),
                    "Wend without while");
        }
        var whileLoopState = whileLoopStateList.removeLast();
        // GOTO LABEL beforeWhile
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.GOTO_LABEL, whileLoopState.labelBeforeWhile.op1, NULL_ID, NULL_ID);
        // LABEL afterWend
        var labelAfterWend = ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.LABEL, ir.getSymbolTable().addLabel(), NULL_ID, NULL_ID
        );
        // Patch GOTO afterWend
        whileLoopState.gotoAfterWend.patchOp2(labelAfterWend.op1);
    }

    @Override
    public void exitForstmt(BeagleBasicParser.ForstmtContext ctx) {
        var var = lookupInstruction(ctx.variable());
        var init = lookupInstruction(ctx.expr(0));
        var end = lookupInstruction(ctx.expr(1));
        Types.assertNumeric(ir.getSymbolTable().get(init.result).getValue().getDataType(), () -> getCtxString(ctx));
        Types.assertNumeric(ir.getSymbolTable().get(end.result).getValue().getDataType(), () -> getCtxString(ctx));

        var forLoopState = new ForLoopState();
        forLoopState.variable = ((STObjects.STVariable) ir.getSymbolTable().get(var.result)).getVariable();

        // stepCopy = step or 1 (default)
        Instruction stepCopy;
        if (ctx.expr(2) != null) {
            var step = lookupInstruction(ctx.expr(2));
            Types.assertNumeric(ir.getSymbolTable().get(step.result).getValue().getDataType(), () -> getCtxString(ctx));
            var tmpStep = ir.getSymbolTable().addTmpCompatibleWith(step.result);
            stepCopy = ir.addInstruction(
                    currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                    OpCode.COPY, tmpStep, step.result, tmpStep
            );
        } else {
            var tmpStep = ir.getSymbolTable().addTmp(INT32, e -> e.getValue().setInt32(1));
            stepCopy = ir.addInstruction(
                    currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                    OpCode.VALUE, tmpStep, NULL_ID, tmpStep
            );
        }
        // var=init
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.ASSIGN, var.result, init.result, var.result
        );
        // endCopy=end
        var tmpEnd = ir.getSymbolTable().addTmpCompatibleWith(end.result);
        var endCopy = ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.ASSIGN, tmpEnd, end.result, tmpEnd
        );

        // GOTO LABEL CHECK
        var gotoLabelCheck = ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.GOTO_LABEL, ir.getSymbolTable().addGotoTarget(), NULL_ID, NULL_ID
        );

        // APPLY STEP
        // JUMP here from NEXT
        forLoopState.labelApplyStep = ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.LABEL, ir.getSymbolTable().addLabel(), NULL_ID, NULL_ID
        );
        var tmpAdd = ir.getSymbolTable().addTmpCompatibleWith(var.result);
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.ADD, var.result, stepCopy.result, tmpAdd
        );
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.ASSIGN, var.result, tmpAdd, var.result
        );

        // CHECK
        // If (step >= 0 and var > end) or (step < 0 and var < end) GOTO after "next"
        // step >= 0
        var labelCheck = ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.LABEL, ir.getSymbolTable().addLabel(), NULL_ID, NULL_ID
        );
        var zero = ir.getSymbolTable().addTmp(INT32, e -> {});
        var t1 = ir.getSymbolTable().addTmp(INT32, e -> {});
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.GE, stepCopy.result, zero, t1
        );
        // Patch GOTO LABEL Check
        gotoLabelCheck.patchOp1(labelCheck.op1);
        // var > end
        var t2 = ir.getSymbolTable().addTmp(INT32, e -> {});
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.GT, var.result, endCopy.result, t2
        );
        // (step >= 0 and var > end)
        var t3 = ir.getSymbolTable().addTmp(INT32, e -> {});
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.AND, t1, t2, t3
        );
        // step < 0
        var t4 = ir.getSymbolTable().addTmp(INT32, e -> {});
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.LT, stepCopy.result, zero, t4
        );
        // var < end
        var t5 = ir.getSymbolTable().addTmp(INT32, e -> {});
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.LT, var.result, endCopy.result, t5
        );
        // (step < 0 and var < end)
        var t6 = ir.getSymbolTable().addTmp(INT32, e -> {});
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.AND, t4, t5, t6
        );
        var t7 = ir.getSymbolTable().addTmp(INT32, e -> {});
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.OR, t3, t6, t7
        );
        // if (true) GOTO after NEXT
        // set linenumber on exitNext().
        forLoopState.gotoAfterNext = ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.GOTO_LABEL_IF, t7, ir.getSymbolTable().addLabel(), NULL_ID
        );

        forLoopStateList.add(forLoopState);
    }

    @Override
    public void exitNextstmt(BeagleBasicParser.NextstmtContext ctx) {
        List<ForLoopState> states = new ArrayList<>(1);
        if (ctx.variable().isEmpty()) {
            if (!forLoopStateList.isEmpty()) {
                states.add(forLoopStateList.removeLast());
            } else {
                throw new BeagleBasicSemanticError(
                        NEXT_WITHOUT_FOR,
                        getCtxString(ctx),
                        "NEXT without FOR"
                );
            }
        } else {
            for (var varCtx : ctx.variable()) {
                var varname = varCtx.varname().VARNAME().getText();
                var varsuffix = varCtx.varsuffix() != null ? varCtx.varsuffix().getText() : null;
                var dataType = ir.getSymbolTable().getDataTypeFor(varname, varsuffix);
                var variableName = new VariableName(varname, dataType);

                int id = ir.getSymbolTable().addVariableOrUDF(
                        variableName,
                        variableName1 -> Variable.of(variableName1, false, () -> getCtxString(ctx)),
                        (id1, e1) -> {});
                var variable = ((STObjects.STVariable) ir.getSymbolTable().get(id)).getVariable();

                var state = forLoopStateList.removeLast();
                if (state.variable.equals(variable)) {
                    states.add(state);
                } else {
                    throw new BeagleBasicSemanticError(
                            NEXT_WITHOUT_FOR,
                            getCtxString(ctx),
                            "Next " + variable + " without FOR"
                    );
                }
            }
        }

        for (ForLoopState state : states) {
            // GOTO APPLY STEP
            ir.addInstruction(
                    currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                    OpCode.GOTO_LABEL, state.labelApplyStep.op1, NULL_ID, NULL_ID
            );

            // LABEL afterNext
            var labelAfterNext = ir.addInstruction(
                    currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                    OpCode.LABEL, ir.getSymbolTable().addLabel(), NULL_ID, NULL_ID
            );
            state.gotoAfterNext.patchOp2(labelAfterNext.op1);
        }
    }

    /*
     * condition
     * GOTOIF condition labelBeforeThen
     * GOTO labelAfterThen|labelBeforeElse
     * labelBeforeThen
     * ThenStmts
     * GOTO labelAfterThen|labelAfterElse
     * labelAfterThen
     * ElseStmts
     * labelAfterElse
     */
    @Override
    public void enterIfThenElse(BeagleBasicParser.IfThenElseContext ctx) {
        nodeToIfState.put(ctx, new IfState());
    }

    @Override
    public void exitIfThenElse(BeagleBasicParser.IfThenElseContext ctx) {
        var ifState = nodeToIfState.get(ctx);
        boolean noElseStmt = ifState.labelBeforeElse == null;

        var condition = lookupInstruction(ctx.expr());
        // Patch IF true: condition
        ifState.gotoIfConditionTrue.patchOp1(condition.result);
        // Patch IF true: GOTO labelBeforeThen
        ifState.gotoIfConditionTrue.patchOp2(ifState.labelBeforeThen.op1);
        // Patch IF false: GOTO labelAfterThen|labelBeforeElse
        ifState.gotoIfConditionFalse.patchOp1(
                noElseStmt ? ifState.labelAfterThen.op1 : ifState.labelBeforeElse.op1
        );
        // Patch THEN: GOTO labelAfterThen|labelAfterElse
        ifState.gotoFromThenAfterIf.patchOp1(
                noElseStmt ? ifState.labelAfterThen.op1 : ifState.labelAfterElse.op1
        );
    }

    @Override
    public void enterThen(BeagleBasicParser.ThenContext ctx) {
        var ifState = nodeToIfState.get(ctx.getParent());
        // IF condition is true, GOTO labelBeforeThen
        ifState.gotoIfConditionTrue = ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.GOTO_LABEL_IF, ir.getSymbolTable().addGotoTarget(), NULL_ID, NULL_ID
        );
        // IF condition is false, GOTO labelAfterThen|labelBeforeElse
        ifState.gotoIfConditionFalse = ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.GOTO_LABEL, ir.getSymbolTable().addGotoTarget(), NULL_ID, NULL_ID
        );
        ifState.labelBeforeThen = ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.LABEL, ir.getSymbolTable().addLabel(), NULL_ID, NULL_ID
        );
    }

    @Override
    public void exitThen(BeagleBasicParser.ThenContext ctx) {
        // Add instruction for:
        // THEN GOTO linenum | THEN linenum
        if (ctx.linenum() != null) {
            var gotoLinenum = parseLinenum(ctx.linenum().getText());
            ir.addInstruction(
                    currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                    OpCode.GOTO_LINENUM, getGotoLineNumberOp1(gotoLinenum), NULL_ID, NULL_ID
            );
        }

        var ifState = nodeToIfState.get(ctx.getParent());
        // GOTO labelAfterThen|labelAfterElse
        ifState.gotoFromThenAfterIf = ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.GOTO_LABEL, ir.getSymbolTable().addLabel(),
                NULL_ID, NULL_ID
        );
        ifState.labelAfterThen = ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.LABEL, ir.getSymbolTable().addLabel(), NULL_ID, NULL_ID
        );
    }

    @Override
    public void enterElsestmt(BeagleBasicParser.ElsestmtContext ctx) {
        var ifState = nodeToIfState.get(ctx.getParent());
        ifState.labelBeforeElse = ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.LABEL, ir.getSymbolTable().addLabel(), NULL_ID, NULL_ID
        );
    }

    @Override
    public void exitElsestmt(BeagleBasicParser.ElsestmtContext ctx) {
        // Add instruction for:
        // ELSE linenum
        if (ctx.linenum() != null) {
            var gotoLinenum = parseLinenum(ctx.linenum().getText());
            ir.addInstruction(
                    currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                    OpCode.GOTO_LINENUM, getGotoLineNumberOp1(gotoLinenum), NULL_ID, NULL_ID
            );
        }
        var ifState = nodeToIfState.get(ctx.getParent());
        ifState.labelAfterElse = ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.LABEL, ir.getSymbolTable().addLabel(), NULL_ID, NULL_ID
        );
    }

    @Override
    public void exitGosubstmt(BeagleBasicParser.GosubstmtContext ctx) {
        var gotoLinenum = parseLinenum(ctx.linenum().getText());
        var pushReturnLabel = ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.PUSH_RETLABEL, ir.getSymbolTable().addGotoTarget(), NULL_ID, NULL_ID
        );
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.GOTO_LINENUM, getGotoLineNumberOp1(gotoLinenum), NULL_ID, NULL_ID
        );
        var labelReturn = ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.LABEL, ir.getSymbolTable().addLabel(), NULL_ID, NULL_ID
        );
        pushReturnLabel.patchOp1(labelReturn.op1);
    }

    @Override
    public void exitReturnstmt(BeagleBasicParser.ReturnstmtContext ctx) {
        if (ctx.linenum() != null) {
            var gotoLinenum = parseLinenum(ctx.linenum().getText());
            ir.addInstruction(
                    currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                    OpCode.RETURN, getGotoLineNumberOp1(gotoLinenum), NULL_ID, NULL_ID
            );
        } else {
            ir.addInstruction(
                    currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                    OpCode.RETURN, NULL_ID, NULL_ID, NULL_ID
            );
        }
    }

    @Override
    public void exitGotostmt(BeagleBasicParser.GotostmtContext ctx) {
        var gotoLinenum = parseLinenum(ctx.linenum().getText());
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.GOTO_LINENUM, getGotoLineNumberOp1(gotoLinenum), NULL_ID, NULL_ID
        );
    }

    @Override
    public void exitSwapstmt(BeagleBasicParser.SwapstmtContext ctx) {
        var var1 = lookupInstruction(ctx.variable(0));
        var var2 = lookupInstruction(ctx.variable(1));
        var dt1 = ir.getSymbolTable().get(var1.result).getValue().getDataType();
        var dt2 = ir.getSymbolTable().get(var2.result).getValue().getDataType();
        if (dt1 != dt2) {
            throw new BeagleBasicSemanticError(
                    DATA_TYPE_MISMATCH,
                    getCtxString(ctx),
                    dt1 + " doesn't match " + dt2
            );
        }
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.SWAP, var1.result, var2.result, NULL_ID
        );
    }

    @Override
    public void exitOpen1stmt(BeagleBasicParser.Open1stmtContext ctx) {
        var filenameInstr = lookupInstruction(ctx.filename);
        var fileOpenMode = getFileOpenMode(ctx.filemode1());
        var accessMode = getFileAccessMode(null);
        var lockMode = getLockMode(null);
        var fileNumber = Numbers.parseInt32(ctx.filenum.getText(), () -> getCtxString(ctx));
        var recordLenInstrId = ctx.reclen != null
                ? lookupInstruction(ctx.reclen).result
                : ir.getSymbolTable().addTmp(INT32, e -> e.getValue().setInt32(DEFAULT_RECORD_LEN));

        Types.assertString(ir.getSymbolTable().get(filenameInstr.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        Types.assertNumeric(ir.getSymbolTable().get(recordLenInstrId).getValue().getDataType(),
                () -> getCtxString(ctx));

        // fileName, fileNumber
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.OPEN_FN_FN_0,
                filenameInstr.result,
                ir.getSymbolTable().addTmp(INT32, e -> e.getValue().setInt32(fileNumber)),
                NULL_ID
        );
        // openMode, accessMode
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.OPEN_OM_AM_1,
                ir.getSymbolTable().addTmp(STRING, e -> e.getValue().setString(fileOpenMode.name())),
                ir.getSymbolTable().addTmp(STRING, e -> e.getValue().setString(accessMode.name())),
                NULL_ID
        );
        // lockMode, recordLen
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.OPEN_LM_RL_2,
                ir.getSymbolTable().addTmp(STRING, e -> e.getValue().setString(lockMode.name())),
                recordLenInstrId,
                NULL_ID
        );
    }

    @Override
    public void exitOpen2stmt(BeagleBasicParser.Open2stmtContext ctx) {
        var filenameInstr = lookupInstruction(ctx.filename);
        var fileOpenMode = getFileOpenMode(ctx.filemode2());
        var accessMode = getFileAccessMode(ctx.access());
        var lockMode = getLockMode(ctx.lock());
        var fileNumber = Numbers.parseInt32(ctx.filenum.getText(), () -> getCtxString(ctx));
        var recordLenInstrId = ctx.reclen != null
                ? lookupInstruction(ctx.reclen).result
                : ir.getSymbolTable().addTmp(INT32, e -> e.getValue().setInt32(DEFAULT_RECORD_LEN));

        Types.assertString(ir.getSymbolTable().get(filenameInstr.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        Types.assertNumeric(ir.getSymbolTable().get(recordLenInstrId).getValue().getDataType(),
                () -> getCtxString(ctx));

        // fileName, fileNumber
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.OPEN_FN_FN_0,
                filenameInstr.result,
                ir.getSymbolTable().addTmp(INT32, e -> e.getValue().setInt32(fileNumber)),
                NULL_ID
        );
        // openMode, accessMode
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.OPEN_OM_AM_1,
                ir.getSymbolTable().addTmp(STRING, e -> e.getValue().setString(fileOpenMode.name())),
                ir.getSymbolTable().addTmp(STRING, e -> e.getValue().setString(accessMode.name())),
                NULL_ID
        );
        // lockMode, recordLen
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.OPEN_LM_RL_2,
                ir.getSymbolTable().addTmp(STRING, e -> e.getValue().setString(lockMode.name())),
                recordLenInstrId,
                NULL_ID
        );
    }

    @Override
    public void exitClosestmt(BeagleBasicParser.ClosestmtContext ctx) {
        var fileNumbers = ctx.DECIMAL().stream().map(
            fileNumberCtx -> Numbers.parseInt32(fileNumberCtx.getText(), () -> getCtxString(ctx))
        ).collect(Collectors.toList());
        if (fileNumbers.isEmpty()) {
            ir.addInstruction(
                    currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                    OpCode.CLOSE_ALL,
                    NULL_ID,
                    NULL_ID,
                    NULL_ID
            );
        } else {
            fileNumbers.forEach(fileNumber ->
                ir.addInstruction(
                        currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                        OpCode.CLOSE,
                        ir.getSymbolTable().addTmp(INT32, e -> e.getValue().setInt32(fileNumber)),
                        NULL_ID,
                        NULL_ID
            ));
        }
    }

    @Override
    public void exitFieldstmt(BeagleBasicParser.FieldstmtContext ctx) {
        var fileNumberInstr = lookupInstruction(ctx.filenum);
        Types.assertNumeric(ir.getSymbolTable().get(fileNumberInstr.result).getValue().getDataType(),
                () -> getCtxString(ctx));

        var numEntries = ctx.variable().size();
        for (int i = 0; i < numEntries; i++) {
            var recordPartLen = Numbers.parseInt32(ctx.DECIMAL(i).getText(), () -> getCtxString(ctx));
            var varInstr = lookupInstruction(ctx.variable(i));
            var kind = ir.getSymbolTable().get(varInstr.result).getKind();
            assertVariable(kind, () -> getCtxString(ctx));
            ir.addInstruction(
                    currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                    OpCode.FIELD_I,
                    varInstr.result,
                    ir.getSymbolTable().addTmp(INT32, e -> e.getValue().setInt32(recordPartLen)),
                    NULL_ID
            );
        }
        // FileNumber, #fields
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.FIELD,
                fileNumberInstr.result,
                ir.getSymbolTable().addTmp(INT32, e -> e.getValue().setInt32(numEntries)),
                NULL_ID
        );
    }

    private void assertVariable(STObjects.STKind kind, Supplier<String> line) {
        if (kind != STObjects.STKind.VARIABLE) {
            throw new BeagleBasicSemanticError(
                    BeagleBasicSemanticError.ErrorCode.BAD_ARGUMENT,
                    line.get(),
                    "Expected variable, but found: " + kind
            );
        }
    }

    @Override
    public void exitPutstmt(BeagleBasicParser.PutstmtContext ctx) {
        var fileNumberInstr = Numbers.parseInt32(ctx.filenum.getText(), () -> getCtxString(ctx));
        final int exprId;
        if (ctx.expr() != null) {
            exprId = lookupInstruction(ctx.expr()).result;
            Types.assertNumeric(ir.getSymbolTable().get(exprId).getValue().getDataType(),
                    () -> getCtxString(ctx));
        } else {
            exprId = NULL_ID;
        }
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.PUTF,
                ir.getSymbolTable().addTmp(INT32, e -> e.getValue().setInt32(fileNumberInstr)),
                exprId,
                NULL_ID
        );
    }

    @Override
    public void exitMiddlrstmt(BeagleBasicParser.MiddlrstmtContext ctx) {
        var varInstr = lookupInstruction(ctx.variable());
        var nInstr = lookupInstruction(ctx.expr(0));
        var mInstrId = ctx.expr().size() == 3
                ? lookupInstruction(ctx.expr(1)).result
                : ir.getSymbolTable().addTmp(INT32, e -> e.getValue().setInt32(-1));
        var replacement = ctx.expr().size() == 3
                ? lookupInstruction(ctx.expr(2))
                : lookupInstruction(ctx.expr(1));

        Types.assertString(ir.getSymbolTable().get(varInstr.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        Types.assertString(ir.getSymbolTable().get(replacement.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        assertVariable(ir.getSymbolTable().get(varInstr.result).getKind(),
                () -> getCtxString(ctx));
        Types.assertNumeric(ir.getSymbolTable().get(nInstr.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        Types.assertNumeric(ir.getSymbolTable().get(mInstrId).getValue().getDataType(),
                () -> getCtxString(ctx));

        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.MIDDLR0, varInstr.result, nInstr.result, NULL_ID);
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.MIDDLR_STMT, mInstrId, replacement.result, NULL_ID);
    }

    @Override
    public void exitGetstmt(BeagleBasicParser.GetstmtContext ctx) {
        var fileNumberInstr = Numbers.parseInt32(ctx.filenum.getText(), () -> getCtxString(ctx));
        final int exprId;
        if (ctx.expr() != null) {
            exprId = lookupInstruction(ctx.expr()).result;
            Types.assertNumeric(ir.getSymbolTable().get(exprId).getValue().getDataType(),
                    () -> getCtxString(ctx));
        } else {
            exprId = NULL_ID;
        }
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.GETF,
                ir.getSymbolTable().addTmp(INT32, e -> e.getValue().setInt32(fileNumberInstr)),
                exprId,
                NULL_ID
        );
    }

    @Override
    public void exitRandomizestmt(BeagleBasicParser.RandomizestmtContext ctx) {
        var exprId = lookupInstruction(ctx.expr()).result;
        Types.assertNumeric(ir.getSymbolTable().get(exprId).getValue().getDataType(),
                () -> getCtxString(ctx));
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.RANDOMIZE, exprId, NULL_ID, NULL_ID
        );
    }

    @Override
    public void exitRandomizetimerstmt(BeagleBasicParser.RandomizetimerstmtContext ctx) {
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.RANDOMIZE_TIMER, NULL_ID, NULL_ID, NULL_ID
        );
    }

    @Override
    public void exitDefintstmt(BeagleBasicParser.DefintstmtContext ctx) {
        handleDefTypeStmt(ctx, ctx.LETTER(), ctx.LETTERRANGE(), OpCode.DEFINT);
    }

    @Override
    public void exitDeflngstmt(BeagleBasicParser.DeflngstmtContext ctx) {
        handleDefTypeStmt(ctx, ctx.LETTER(), ctx.LETTERRANGE(), OpCode.DEFLNG);
    }

    @Override
    public void exitDefsngstmt(BeagleBasicParser.DefsngstmtContext ctx) {
        handleDefTypeStmt(ctx, ctx.LETTER(), ctx.LETTERRANGE(), OpCode.DEFSNG);
    }

    @Override
    public void exitDefdblstmt(BeagleBasicParser.DefdblstmtContext ctx) {
        handleDefTypeStmt(ctx, ctx.LETTER(), ctx.LETTERRANGE(), OpCode.DEFDBL);
    }

    @Override
    public void exitDefstrstmt(BeagleBasicParser.DefstrstmtContext ctx) {
        handleDefTypeStmt(ctx, ctx.LETTER(), ctx.LETTERRANGE(), OpCode.DEFSTR);
    }

    @Override
    public void exitLsetstmt(BeagleBasicParser.LsetstmtContext ctx) {
        var varInstr = lookupInstruction(ctx.variable());
        var exprInstr = lookupInstruction(ctx.expr());

        var varEntry = ir.getSymbolTable().get(varInstr.result);
        assertVariable(varEntry.getKind(), () -> getCtxString(ctx));
        Types.assertString(varEntry.getValue().getDataType(),
                () -> getCtxString(ctx));
        Types.assertString(ir.getSymbolTable().get(exprInstr.result).getValue().getDataType(),
                () -> getCtxString(ctx));

        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.LSET, varInstr.result, exprInstr.result, NULL_ID
        );
    }

    @Override
    public void exitRsetstmt(BeagleBasicParser.RsetstmtContext ctx) {
        var varInstr = lookupInstruction(ctx.variable());
        var exprInstr = lookupInstruction(ctx.expr());

        var varEntry = ir.getSymbolTable().get(varInstr.result);
        assertVariable(varEntry.getKind(), () -> getCtxString(ctx));
        Types.assertString(varEntry.getValue().getDataType(),
                () -> getCtxString(ctx));
        Types.assertString(ir.getSymbolTable().get(exprInstr.result).getValue().getDataType(),
                () -> getCtxString(ctx));

        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.RSET, varInstr.result, exprInstr.result, NULL_ID
        );
    }

    @Override
    public void exitInputstmt(BeagleBasicParser.InputstmtContext ctx) {
        for (var varCtx : ctx.variable()) {
            var varInstr = lookupInstruction(varCtx);
            assertVariable(ir.getSymbolTable().get(varInstr.result).getKind(),
                    () -> getCtxString(ctx));
            ir.addInstruction(
                    currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                    OpCode.INPUT_VAR, varInstr.result, NULL_ID, NULL_ID
            );
        }

        int promptId;
        if (ctx.expr() != null) {
            promptId = lookupInstruction(ctx.expr()).result;
            Types.assertString(ir.getSymbolTable().get(promptId).getValue().getDataType(),
                    () -> getCtxString(ctx));
        } else {
            promptId = ir.getSymbolTable().addTmp(STRING, e -> e.getValue().setString("?"));
        }
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.INPUT, promptId, NULL_ID, NULL_ID
        );
    }

    @Override
    public void exitInputhashstmt(BeagleBasicParser.InputhashstmtContext ctx) {
        for (var varCtx : ctx.variable()) {
            var varInstr = lookupInstruction(varCtx);
            assertVariable(ir.getSymbolTable().get(varInstr.result).getKind(),
                    () -> getCtxString(ctx));
            ir.addInstruction(
                    currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                    OpCode.INPUT_VAR, varInstr.result, NULL_ID, NULL_ID
            );
        }

        var fileNumInstr = lookupInstruction(ctx.filenum);
        Types.assertNumeric(ir.getSymbolTable().get(fileNumInstr.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.INPUT, NULL_ID, fileNumInstr.result, NULL_ID
        );
    }

    @Override
    public void exitLineinputstmt(BeagleBasicParser.LineinputstmtContext ctx) {
        var varInstr = lookupInstruction(ctx.variable());
        assertVariable(ir.getSymbolTable().get(varInstr.result).getKind(),
                () -> getCtxString(ctx));
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.INPUT_VAR, varInstr.result, NULL_ID, NULL_ID
        );

        int promptId;
        if (ctx.expr() != null) {
            promptId = lookupInstruction(ctx.expr()).result;
            Types.assertString(ir.getSymbolTable().get(promptId).getValue().getDataType(),
                    () -> getCtxString(ctx));
        } else {
            promptId = ir.getSymbolTable().addTmp(STRING, e -> e.getValue().setString(""));
        }

        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.LINE_INPUT, promptId, NULL_ID, NULL_ID
        );
    }

    @Override
    public void exitLineinputhashstmt(BeagleBasicParser.LineinputhashstmtContext ctx) {
        var varInstr = lookupInstruction(ctx.variable());
        assertVariable(ir.getSymbolTable().get(varInstr.result).getKind(),
                () -> getCtxString(ctx));
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.INPUT_VAR, varInstr.result, NULL_ID, NULL_ID
        );

        var fileNumInstr = lookupInstruction(ctx.filenum);
        Types.assertNumeric(ir.getSymbolTable().get(fileNumInstr.result).getValue().getDataType(),
                () -> getCtxString(ctx));

        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.LINE_INPUT, NULL_ID, fileNumInstr.result, NULL_ID
        );
    }

    @Override
    public void exitWritestmt(BeagleBasicParser.WritestmtContext ctx) {
        handleWritestmt(ctx, ctx.expr(), null);
    }

    @Override
    public void exitWritehashstmt(BeagleBasicParser.WritehashstmtContext ctx) {
        var fileNumInstr = lookupInstruction(ctx.filenum);
        handleWritestmt(ctx, ctx.expr(), fileNumInstr);
    }

    public void handleWritestmt(
            ParserRuleContext ctx,
            List<BeagleBasicParser.ExprContext> exprs,
            @Nullable Instruction fileNumber) {
        int i = 0;
        for (var exprCtx : exprs) {
            var exprInstr = lookupInstruction(exprCtx);
            ir.addInstruction(
                    currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                    OpCode.WRITE, exprInstr.result, NULL_ID, NULL_ID
            );
            if (++i < exprs.size()) {
                var commaId = ir.getSymbolTable().addTmp(STRING,
                        entry -> entry.getValue().setString(","));
                ir.addInstruction(
                        currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                        OpCode.PRINT, commaId, NULL_ID, NULL_ID
                );
            }
        }

        var newlineId = ir.getSymbolTable().addTmp(STRING,
                entry -> entry.getValue().setString(System.lineSeparator()));
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.PRINT, newlineId, NULL_ID, NULL_ID
        );

        final int fileNumberId;
        if (fileNumber != null) {
            Types.assertNumeric(ir.getSymbolTable().get(fileNumber.result).getValue().getDataType(),
                    () -> getCtxString(ctx));
            fileNumberId = fileNumber.result;
        } else {
            fileNumberId = NULL_ID;
        }
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.FLUSH, fileNumberId, NULL_ID, NULL_ID
        );
    }

    @Override
    public void exitReadstmt(BeagleBasicParser.ReadstmtContext ctx) {
        for (var varCtx : ctx.variable()) {
            var varInstr = lookupInstruction(varCtx);
            assertVariable(ir.getSymbolTable().get(varInstr.result).getKind(),
                    () -> getCtxString(ctx));
            ir.addInstruction(
                    currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                    OpCode.READ, varInstr.result, NULL_ID, NULL_ID
            );
        }
    }

    @Override
    public void exitRestorestmt(BeagleBasicParser.RestorestmtContext ctx) {
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.RESTORE, NULL_ID, NULL_ID, NULL_ID
        );
    }

    @Override
    public void exitDatastmt(BeagleBasicParser.DatastmtContext ctx) {
        var children = ctx.children;
        for (int i = 1; i < children.size(); i += 2) {
            var child = children.get(i);
            int valueId;
            if (child instanceof BeagleBasicParser.NumberContext) {
                valueId = lookupInstruction((BeagleBasicParser.NumberContext) child).result;
            } else {
                var text = Types.unquote(child.getText());
                valueId = ir.getSymbolTable().addTmp(STRING, e -> e.getValue().setString(text));
            }
            ir.addInstruction(
                    currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                    OpCode.DATA, valueId, NULL_ID, NULL_ID
            );
        }
    }

    private void handleDefTypeStmt(
            ParserRuleContext parent,
            List<TerminalNode> letters,
            List<TerminalNode> letterRanges,
            OpCode opCode) {
        List<String> defs = new ArrayList<>();
        letters.stream().map(ParseTree::getText).forEach(l -> defs.add(l.substring(0, 1)));
        letterRanges.stream().map(ParseTree::getText).forEach(lr -> {
            for (char i = lr.charAt(0); i <= lr.charAt(2); i++) {
                defs.add(new String(new byte[]{(byte) i}));
            }
        });
        defs.forEach(def ->
                ir.addInstruction(
                        currentLineNumber, parent.start.getStartIndex(), parent.stop.getStopIndex(),
                        opCode,
                        ir.getSymbolTable().addTmp(STRING, e -> e.getValue().setString(def)),
                        NULL_ID, NULL_ID
                ));
    }

    private static FileOpenMode getFileOpenMode(BeagleBasicParser.Filemode1Context filemode1) {
        if (filemode1 == null || filemode1.getText().equalsIgnoreCase("r")) {
            return FileOpenMode.RANDOM;
        } else if (filemode1.getText().equalsIgnoreCase("i")) {
            return FileOpenMode.INPUT;
        } else if (filemode1.getText().equalsIgnoreCase("o")) {
            return FileOpenMode.OUTPUT;
        } else {
            return FileOpenMode.APPEND;
        }
    }

    private static FileOpenMode getFileOpenMode(BeagleBasicParser.Filemode2Context filemode2) {
        if (filemode2 == null || filemode2.RANDOM() != null) {
            return FileOpenMode.RANDOM;
        } else if (filemode2.INPUT() != null) {
            return FileOpenMode.INPUT;
        } else if (filemode2.OUTPUT() != null) {
            return FileOpenMode.OUTPUT;
        } else {
            return FileOpenMode.APPEND;
        }
    }

    private static FileAccessMode getFileAccessMode(BeagleBasicParser.AccessContext access) {
        if (access == null || (access.READ() != null && access.WRITE() != null)) {
            return FileAccessMode.READ_WRITE;
        } else if (access.READ() != null) {
            return FileAccessMode.READ_ONLY;
        } else {
            return FileAccessMode.WRITE_ONLY;
        }
    }

    private static LockMode getLockMode(BeagleBasicParser.LockContext lock) {
        if (lock == null) {
            return LockMode.DEFAULT;
        } else if (lock.SHARED() != null) {
            return LockMode.SHARED;
        } else if (lock.READ() != null && lock.WRITE() != null) {
            return LockMode.READ_WRITE;
        } else if (lock.READ() != null) {
            return LockMode.READ;
        } else {
            return LockMode.WRITE;
        }
    }

    private int getGotoLineNumberOp1(int lineNumber) {
        return ir.getSymbolTable().addTmp(INT32, e -> e.getValue().setInt32(lineNumber));
    }

    private void checkDataTypeMatch(int id1, int id2, Supplier<String> lineSupplier) {
        checkDataTypeMatch(ir.getSymbolTable().get(id1).getValue(), id2, lineSupplier);
    }

    private void checkDataTypeMatch(STObjects.STValue entry1, int id2, Supplier<String> lineSupplier) {
        var entry2 = ir.getSymbolTable().get(id2).getValue();
        if ((entry1.getDataType() == STRING && entry2.getDataType() != STRING) ||
                (entry1.getDataType() != STRING && entry2.getDataType() == STRING)) {
            throw new BeagleBasicSemanticError(
                    DATA_TYPE_MISMATCH,
                    lineSupplier.get(),
                    "Data type " + entry1.getDataType()
                            + " mismatches with " + entry2.getDataType()
            );
        }
    }

    private static final class UDFState {
        public Instruction gotoPostFuncDecl;
        public Instruction labelFuncStart;
    }

    private static final class WhileLoopState {
        public Instruction labelBeforeWhile;
        public Instruction gotoAfterWend;
    }

    private static final class ForLoopState {
        public Variable variable;
        public Instruction labelApplyStep;
        public Instruction gotoAfterNext;
    }

    private static final class IfState {
        public Instruction gotoIfConditionTrue;
        public Instruction gotoIfConditionFalse;
        public Instruction gotoFromThenAfterIf;
        public Instruction labelBeforeThen;
        public Instruction labelAfterThen;
        public Instruction labelBeforeElse;
        public Instruction labelAfterElse;
    }
}
