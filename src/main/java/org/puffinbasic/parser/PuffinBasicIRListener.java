package org.puffinbasic.parser;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.Nullable;
import org.puffinbasic.antlr4.PuffinBasicBaseListener;
import org.puffinbasic.antlr4.PuffinBasicParser;
import org.puffinbasic.domain.STObjects;
import org.puffinbasic.domain.STObjects.PuffinBasicDataType;
import org.puffinbasic.domain.STObjects.STKind;
import org.puffinbasic.domain.STObjects.STUDF;
import org.puffinbasic.domain.STObjects.STVariable;
import org.puffinbasic.domain.Variable;
import org.puffinbasic.domain.Variable.VariableName;
import org.puffinbasic.error.PuffinBasicInternalError;
import org.puffinbasic.error.PuffinBasicSemanticError;
import org.puffinbasic.file.PuffinBasicFile.FileAccessMode;
import org.puffinbasic.file.PuffinBasicFile.FileOpenMode;
import org.puffinbasic.file.PuffinBasicFile.LockMode;
import org.puffinbasic.parser.PuffinBasicIR.Instruction;
import org.puffinbasic.parser.PuffinBasicIR.OpCode;
import org.puffinbasic.runtime.Numbers;
import org.puffinbasic.runtime.Types;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.puffinbasic.domain.PuffinBasicSymbolTable.NULL_ID;
import static org.puffinbasic.domain.STObjects.PuffinBasicDataType.DOUBLE;
import static org.puffinbasic.domain.STObjects.PuffinBasicDataType.FLOAT;
import static org.puffinbasic.domain.STObjects.PuffinBasicDataType.INT32;
import static org.puffinbasic.domain.STObjects.PuffinBasicDataType.INT64;
import static org.puffinbasic.domain.STObjects.PuffinBasicDataType.STRING;
import static org.puffinbasic.error.PuffinBasicSemanticError.ErrorCode.BAD_ARGUMENT;
import static org.puffinbasic.error.PuffinBasicSemanticError.ErrorCode.BAD_ASSIGNMENT;
import static org.puffinbasic.error.PuffinBasicSemanticError.ErrorCode.DATA_TYPE_MISMATCH;
import static org.puffinbasic.error.PuffinBasicSemanticError.ErrorCode.FOR_WITHOUT_NEXT;
import static org.puffinbasic.error.PuffinBasicSemanticError.ErrorCode.INSUFFICIENT_UDF_ARGS;
import static org.puffinbasic.error.PuffinBasicSemanticError.ErrorCode.NEXT_WITHOUT_FOR;
import static org.puffinbasic.error.PuffinBasicSemanticError.ErrorCode.NOT_DEFINED;
import static org.puffinbasic.error.PuffinBasicSemanticError.ErrorCode.WHILE_WITHOUT_WEND;
import static org.puffinbasic.file.PuffinBasicFile.DEFAULT_RECORD_LEN;
import static org.puffinbasic.parser.LinenumberListener.parseLinenum;
import static org.puffinbasic.runtime.Types.unquote;

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
 * RND              done
 * RIGHT$           done
 * SGN              done
 * SIN              done
 * SPACE$           done
 * SPC              NA
 * SQR              done
 * STR$             done
 * STRING$          done
 * TAB              NA
 * TAN              done
 * TIMER            done
 * VAL              done
 * VARPTR           NA
 * VARPTR$          NA
 *
 * PEN              NA
 * PLAY             graphics
 * PMAP             graphics
 * POINT            graphics
 * POS              graphics
 * SCREEN           NA
 * STICK            NA
 *
 * Statements
 * ==========
 * CALL             NA
 * CHAIN            NA
 * CLOSE            done
 * CLS              NA
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
 * LET              done
 * LINE INPUT       done
 * LINE INPUT#      done
 * LOCK             NA
 * LPRINT           NA
 * LPRINT USING     NA
 * LSET             done
 * MID$             done
 * ON ERROR GOTO    NA
 * ON-GOSUB         NA
 * ON-GOTO          NA
 * OPEN             done
 * OPEN COM(n)      NA
 * OPTION BASE      NA
 * OUT              NA
 * ON COM(n)        NA
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
 * SHELL            NA
 * STOP             NA
 * SWAP             done
 * TIME$            done
 * UNLOCK           NA
 * WAIT             NA
 * WHILE-WEND       done
 * WIDTH            NA
 * WRITE            done
 * WRTIE#           done
 *
 * SCREEN           done
 * CIRCLE           done
 * COLOR            done
 * LINE             done
 * PAINT            done
 * DRAW             graphics
 * LOCATE           graphics
 * BEEP             NA
 * KEY              NA
 * KEY(n)           NA
 * ON KEY(n)        NA
 * ON PEN(n)        NA
 * ON PLAY(n)       NA
 * ON STRIG(n)      NA
 * ON TIMER(n)      NA
 * PALETTE          NA
 * PALETTE USING    NA
 * PEN              NA
 * PLAY             graphics
 * PSET             graphics
 * POKE             NA
 * PRESET           NA
 * STRIG            NA
 * STRIG(n)         NA
 * VIEW             NA
 * VIEW PRINT       NA
 * WINDOW           NA
 * </PRE>
 */
public class PuffinBasicIRListener extends PuffinBasicBaseListener {

    private enum NumericOrString {
        NUMERIC,
        STRING
    }

    private final CharStream in;
    private final PuffinBasicIR ir;
    private final boolean graphics;
    private final ParseTreeProperty<Instruction> nodeToInstruction;
    private final Object2ObjectMap<Variable, UDFState> udfStateMap;
    private final LinkedList<WhileLoopState> whileLoopStateList;
    private final LinkedList<ForLoopState> forLoopStateList;
    private final ParseTreeProperty<IfState> nodeToIfState;
    private int currentLineNumber;
    private final ObjectSet<VariableName> varDefined;

    public PuffinBasicIRListener(CharStream in, PuffinBasicIR ir, boolean graphics) {
        this.in = in;
        this.ir = ir;
        this.graphics = graphics;
        this.nodeToInstruction = new ParseTreeProperty<>();
        this.udfStateMap = new Object2ObjectOpenHashMap<>();
        this.whileLoopStateList = new LinkedList<>();
        this.forLoopStateList = new LinkedList<>();
        this.nodeToIfState = new ParseTreeProperty<>();
        this.varDefined = new ObjectOpenHashSet<>();
    }

    public void semanticCheckAfterParsing() {
        if (!whileLoopStateList.isEmpty()) {
            throw new PuffinBasicSemanticError(
                    WHILE_WITHOUT_WEND,
                    "<UNKNOWN LINE>",
                    "WHILE without WEND"
            );
        }
        if (!forLoopStateList.isEmpty()) {
            throw new PuffinBasicSemanticError(
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
            throw new PuffinBasicInternalError(
                    "Failed to find instruction for node: " + ctx.getText()
            );
        }
        return exprInstruction;
    }

    @Override
    public void enterLine(PuffinBasicParser.LineContext ctx) {
        this.currentLineNumber = parseLinenum(ctx.linenum().DECIMAL().getText());
    }

    //
    // Variable, Number, etc.
    //

    @Override
    public void exitNumber(PuffinBasicParser.NumberContext ctx) {
        final int id;
        if (ctx.integer() != null) {
            final boolean isLong = ctx.integer().AT() != null;
            final boolean isDouble = ctx.integer().HASH() != null;
            final boolean isFloat = ctx.integer().EXCLAMATION() != null;
            final String strValue;
            final int base;
            if (ctx.integer().HEXADECIMAL() != null) {
                strValue = ctx.integer().HEXADECIMAL().getText().substring(2);
                base = 16;
            } else if (ctx.integer().OCTAL() != null) {
                var octalStr = ctx.integer().OCTAL().getText();
                strValue = (octalStr.startsWith("&O") ? octalStr.substring(2) : octalStr.substring(1));
                base = 8;
            } else {
                strValue = ctx.integer().DECIMAL().getText();
                base = 10;
            }
            if (isLong || isDouble) {
                long parsed = Numbers.parseInt64(strValue, base, () -> getCtxString(ctx));
                id = ir.getSymbolTable().addTmp(isLong ? INT64 : DOUBLE,
                        entry -> entry.getValue().setInt64(parsed));
            } else {
                id = ir.getSymbolTable().addTmp(isFloat ? FLOAT : INT32,
                        entry -> entry.getValue().setInt32(Numbers.parseInt32(strValue, base, () -> getCtxString(ctx))));
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
    public void exitVariable(PuffinBasicParser.VariableContext ctx) {

        var varname = ctx.varname().VARNAME().getText();
        var varsuffix = ctx.varsuffix() != null ? ctx.varsuffix().getText() : null;
        var dataType = ir.getSymbolTable().getDataTypeFor(varname, varsuffix);
        var variableName = new VariableName(varname, dataType);
        var idHolder = new AtomicInteger();

        ir.getSymbolTable()
            .addVariableOrUDF(
                variableName,
                variableName1 -> Variable.of(variableName1, false, () -> getCtxString(ctx)),
                (varId, varEntry) -> {
                  var variable = varEntry.getVariable();
                  idHolder.set(varId);
                  if (variable.isScalar()) {
                    // Scalar
                    if (!ctx.expr().isEmpty()) {
                      throw new PuffinBasicSemanticError(
                          PuffinBasicSemanticError.ErrorCode.SCALAR_VARIABLE_CANNOT_BE_INDEXED,
                          getCtxString(ctx),
                          "Scalar variable cannot be indexed: " + variable);
                    }
                  } else if (variable.isArray()) {
                    if (!ctx.expr().isEmpty()) {
                      // Array
                      ir.addInstruction(
                          currentLineNumber,
                          ctx.start.getStartIndex(),
                          ctx.stop.getStopIndex(),
                          OpCode.RESET_ARRAY_IDX,
                          varId,
                          NULL_ID,
                          NULL_ID);

                      for (var exprCtx : ctx.expr()) {
                        var exprInstr = lookupInstruction(exprCtx);
                        ir.addInstruction(
                            currentLineNumber,
                            ctx.start.getStartIndex(),
                            ctx.stop.getStopIndex(),
                            OpCode.SET_ARRAY_IDX,
                            varId,
                            exprInstr.result,
                            NULL_ID);
                      }
                      var refId = ir.getSymbolTable().addArrayReference(varEntry);
                      ir.addInstruction(
                          currentLineNumber,
                          ctx.start.getStartIndex(),
                          ctx.stop.getStopIndex(),
                          OpCode.ARRAYREF,
                          varId,
                          refId,
                          refId);
                      idHolder.set(refId);
                    }
                  } else if (variable.isUDF()) {
                    // UDF
                    var udfEntry = (STUDF) varEntry;
                    var udfState = udfStateMap.get(variable);

                    // Create & Push Runtime scope
                    var pushScopeInstr =
                        ir.addInstruction(
                            currentLineNumber,
                            ctx.start.getStartIndex(),
                            ctx.stop.getStopIndex(),
                            OpCode.PUSH_RT_SCOPE,
                            varId,
                            NULL_ID,
                            NULL_ID);
                    // Copy caller params to Runtime scope
                    if (ctx.expr().size() != udfEntry.getNumDeclaredParams()) {
                      throw new PuffinBasicSemanticError(
                          INSUFFICIENT_UDF_ARGS,
                          getCtxString(ctx),
                          variable
                              + " expects "
                              + udfEntry.getNumDeclaredParams()
                              + ", #args passed: "
                              + ctx.expr().size());
                    }
                    int i = 0;
                    for (var exprCtx : ctx.expr()) {
                      var exprInstr = lookupInstruction(exprCtx);
                      var declParamId = udfEntry.getDeclaraedParam(i++);
                      ir.addInstruction(
                          currentLineNumber,
                          ctx.start.getStartIndex(),
                          ctx.stop.getStopIndex(),
                          OpCode.COPY,
                          declParamId,
                          exprInstr.result,
                          declParamId);
                    }
                    // GOTO labelFuncStart
                    ir.addInstruction(
                        currentLineNumber,
                        ctx.start.getStartIndex(),
                        ctx.stop.getStopIndex(),
                        OpCode.GOTO_LABEL,
                        udfState.labelFuncStart.op1,
                        NULL_ID,
                        NULL_ID);
                    // LABEL caller return address
                    var labelCallerReturn =
                        ir.addInstruction(
                            currentLineNumber,
                            ctx.start.getStartIndex(),
                            ctx.stop.getStopIndex(),
                            OpCode.LABEL,
                            ir.getSymbolTable().addLabel(),
                            NULL_ID,
                            NULL_ID);
                    // Patch address of the caller
                    pushScopeInstr.patchOp2(labelCallerReturn.op1);
                    // Pop Runtime scope
                    ir.addInstruction(
                        currentLineNumber,
                        ctx.start.getStartIndex(),
                        ctx.stop.getStopIndex(),
                        OpCode.POP_RT_SCOPE,
                        varId,
                        NULL_ID,
                        NULL_ID);
                  }
                });

        var refId = idHolder.get();
        nodeToInstruction.put(ctx, ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.VARIABLE, refId, NULL_ID, refId
        ));
    }

    //
    // Expr
    //

    private void copyAndRegisterExprResult(ParserRuleContext ctx, Instruction instruction, boolean shouldCopy) {
        if (shouldCopy) {
            var copy = ir.getSymbolTable().addTmpCompatibleWith(instruction.result);
            instruction = ir.addInstruction(
                    currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                    OpCode.COPY, copy, instruction.result, copy
            );
        }
        nodeToInstruction.put(ctx, instruction);
    }

    @Override
    public void exitExprVariable(PuffinBasicParser.ExprVariableContext ctx) {
        var instruction = nodeToInstruction.get(ctx.variable());
        var varEntry = ir.getSymbolTable().get(instruction.result);
        boolean copy = (varEntry instanceof STVariable) && ((STVariable) varEntry).getVariable().isUDF();
        if (ctx.MINUS() != null) {
            if (ir.getSymbolTable().get(instruction.result).getValue().getDataType() == STRING) {
                throw new PuffinBasicSemanticError(
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
            copy = true;
        }
        copyAndRegisterExprResult(ctx, instruction, copy);
    }

    @Override
    public void exitExprParen(PuffinBasicParser.ExprParenContext ctx) {
        nodeToInstruction.put(ctx, lookupInstruction(ctx.expr()));
    }

    @Override
    public void exitExprNumber(PuffinBasicParser.ExprNumberContext ctx) {
        var instruction = nodeToInstruction.get(ctx.number());
        if (ctx.MINUS() != null) {
            instruction = ir.addInstruction(
                    currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                    OpCode.UNARY_MINUS, instruction.result, NULL_ID,
                    ir.getSymbolTable().addTmpCompatibleWith(instruction.result)
            );
        }
        copyAndRegisterExprResult(ctx, instruction, false);
    }

    @Override
    public void exitExprFunc(PuffinBasicParser.ExprFuncContext ctx) {
        var instruction = nodeToInstruction.get(ctx.func());
        if (ctx.MINUS() != null) {
            instruction = ir.addInstruction(
                    currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                    OpCode.UNARY_MINUS, instruction.result, NULL_ID,
                    ir.getSymbolTable().addTmpCompatibleWith(instruction.result)
            );
        }
        copyAndRegisterExprResult(ctx, instruction, false);
    }

    @Override
    public void exitExprString(PuffinBasicParser.ExprStringContext ctx) {
        var text = unquote(ctx.string().STRING().getText());
        var id = ir.getSymbolTable().addTmp(STRING,
                entry -> entry.getValue().setString(text));
        copyAndRegisterExprResult(ctx, ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.VALUE, id, NULL_ID, id
        ), false);
    }

    @Override
    public void exitExprExp(PuffinBasicParser.ExprExpContext ctx) {
        addArithmeticOpExpr(ctx, OpCode.EXP, ctx.expr(0), ctx.expr(1));
    }

    @Override
    public void exitExprMul(PuffinBasicParser.ExprMulContext ctx) {
        addArithmeticOpExpr(ctx, OpCode.MUL, ctx.expr(0), ctx.expr(1));
    }

    @Override
    public void exitExprIntDiv(PuffinBasicParser.ExprIntDivContext ctx) {
        addArithmeticOpExpr(ctx, OpCode.IDIV, ctx.expr(0), ctx.expr(1));
    }

    @Override
    public void exitExprFloatDiv(PuffinBasicParser.ExprFloatDivContext ctx) {
        addArithmeticOpExpr(ctx, OpCode.FDIV, ctx.expr(0), ctx.expr(1));
    }

    @Override
    public void exitExprMod(PuffinBasicParser.ExprModContext ctx) {
        addArithmeticOpExpr(ctx, OpCode.MOD, ctx.expr(0), ctx.expr(1));
    }

    @Override
    public void exitExprPlus(PuffinBasicParser.ExprPlusContext ctx) {
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
    public void exitExprMinus(PuffinBasicParser.ExprMinusContext ctx) {
        addArithmeticOpExpr(ctx, OpCode.SUB, ctx.expr(0), ctx.expr(1));
    }

    private void addArithmeticOpExpr(
            ParserRuleContext parent, OpCode opCode, PuffinBasicParser.ExprContext exprLeft, PuffinBasicParser.ExprContext exprRight) {
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
    public void exitExprRelEq(PuffinBasicParser.ExprRelEqContext ctx) {
        addRelationalOpExpr(ctx, OpCode.EQ, ctx.expr(0), ctx.expr(1));
    }

    @Override
    public void exitExprRelNeq(PuffinBasicParser.ExprRelNeqContext ctx) {
        addRelationalOpExpr(ctx, OpCode.NE, ctx.expr(0), ctx.expr(1));
    }

    @Override
    public void exitExprRelLt(PuffinBasicParser.ExprRelLtContext ctx) {
        addRelationalOpExpr(ctx, OpCode.LT, ctx.expr(0), ctx.expr(1));
    }

    @Override
    public void exitExprRelLe(PuffinBasicParser.ExprRelLeContext ctx) {
        addRelationalOpExpr(ctx, OpCode.LE, ctx.expr(0), ctx.expr(1));
    }

    @Override
    public void exitExprRelGt(PuffinBasicParser.ExprRelGtContext ctx) {
        addRelationalOpExpr(ctx, OpCode.GT, ctx.expr(0), ctx.expr(1));
    }

    @Override
    public void exitExprRelGe(PuffinBasicParser.ExprRelGeContext ctx) {
        addRelationalOpExpr(ctx, OpCode.GE, ctx.expr(0), ctx.expr(1));
    }

    private void addRelationalOpExpr(
            ParserRuleContext parent, OpCode opCode, PuffinBasicParser.ExprContext exprLeft, PuffinBasicParser.ExprContext exprRight) {
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
    public void exitExprLogNot(PuffinBasicParser.ExprLogNotContext ctx) {
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
    public void exitExprLogAnd(PuffinBasicParser.ExprLogAndContext ctx) {
        addLogicalOpExpr(ctx, OpCode.AND, ctx.expr(0), ctx.expr(1));
    }

    @Override
    public void exitExprLogOr(PuffinBasicParser.ExprLogOrContext ctx) {
        addLogicalOpExpr(ctx, OpCode.OR, ctx.expr(0), ctx.expr(1));
    }

    @Override
    public void exitExprLogXor(PuffinBasicParser.ExprLogXorContext ctx) {
        addLogicalOpExpr(ctx, OpCode.XOR, ctx.expr(0), ctx.expr(1));
    }

    @Override
    public void exitExprLogEqv(PuffinBasicParser.ExprLogEqvContext ctx) {
        addLogicalOpExpr(ctx, OpCode.EQV, ctx.expr(0), ctx.expr(1));
    }

    @Override
    public void exitExprLogImp(PuffinBasicParser.ExprLogImpContext ctx) {
        addLogicalOpExpr(ctx, OpCode.IMP, ctx.expr(0), ctx.expr(1));
    }

    private void addLogicalOpExpr(
            ParserRuleContext parent, OpCode opCode, PuffinBasicParser.ExprContext exprLeft, PuffinBasicParser.ExprContext exprRight) {
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
    public void exitFuncAbs(PuffinBasicParser.FuncAbsContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.ABS, ctx, ctx.expr(),
                NumericOrString.NUMERIC));
    }

    @Override
    public void exitFuncAsc(PuffinBasicParser.FuncAscContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.ASC, ctx, ctx.expr(),
                NumericOrString.STRING,
                ir.getSymbolTable().addTmp(INT32, c -> {})));
    }

    @Override
    public void exitFuncSin(PuffinBasicParser.FuncSinContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.SIN, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(DOUBLE, c -> {})));
    }

    @Override
    public void exitFuncCos(PuffinBasicParser.FuncCosContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.COS, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(DOUBLE, c -> {})));
    }

    @Override
    public void exitFuncTan(PuffinBasicParser.FuncTanContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.TAN, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(DOUBLE, c -> {})));
    }

    @Override
    public void exitFuncAtn(PuffinBasicParser.FuncAtnContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.ATN, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(DOUBLE, c -> {})));
    }

    @Override
    public void exitFuncSqr(PuffinBasicParser.FuncSqrContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.SQR, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(DOUBLE, c -> {})));
    }

    @Override
    public void exitFuncCint(PuffinBasicParser.FuncCintContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.CINT, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(INT32, c -> {})));
    }

    @Override
    public void exitFuncClng(PuffinBasicParser.FuncClngContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.CLNG, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(INT64, c -> {})));
    }

    @Override
    public void exitFuncCsng(PuffinBasicParser.FuncCsngContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.CSNG, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(FLOAT, c -> {})));
    }

    @Override
    public void exitFuncCdbl(PuffinBasicParser.FuncCdblContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.CDBL, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(DOUBLE, c -> {})));
    }

    @Override
    public void exitFuncCvi(PuffinBasicParser.FuncCviContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.CVI, ctx, ctx.expr(),
                NumericOrString.STRING,
                ir.getSymbolTable().addTmp(INT32, c -> {})));
    }

    @Override
    public void exitFuncCvl(PuffinBasicParser.FuncCvlContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.CVL, ctx, ctx.expr(),
                NumericOrString.STRING,
                ir.getSymbolTable().addTmp(INT64, c -> {})));
    }

    @Override
    public void exitFuncCvs(PuffinBasicParser.FuncCvsContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.CVS, ctx, ctx.expr(),
                NumericOrString.STRING,
                ir.getSymbolTable().addTmp(FLOAT, c -> {})));
    }

    @Override
    public void exitFuncCvd(PuffinBasicParser.FuncCvdContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.CVD, ctx, ctx.expr(),
                NumericOrString.STRING,
                ir.getSymbolTable().addTmp(DOUBLE, c -> {})));
    }

    @Override
    public void exitFuncMkiDlr(PuffinBasicParser.FuncMkiDlrContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.MKIDLR, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(STRING, c -> {})));
    }

    @Override
    public void exitFuncMklDlr(PuffinBasicParser.FuncMklDlrContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.MKLDLR, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(STRING, c -> {})));
    }

    @Override
    public void exitFuncMksDlr(PuffinBasicParser.FuncMksDlrContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.MKSDLR, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(STRING, c -> {})));
    }

    @Override
    public void exitFuncMkdDlr(PuffinBasicParser.FuncMkdDlrContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.MKDDLR, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(STRING, c -> {})));
    }

    @Override
    public void exitFuncSpaceDlr(PuffinBasicParser.FuncSpaceDlrContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.SPACEDLR, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(STRING, c -> {})));
    }

    @Override
    public void exitFuncStrDlr(PuffinBasicParser.FuncStrDlrContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.STRDLR, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(STRING, c -> {})));
    }

    @Override
    public void exitFuncVal(PuffinBasicParser.FuncValContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.VAL
                , ctx, ctx.expr(),
                NumericOrString.STRING,
                ir.getSymbolTable().addTmp(DOUBLE, c -> {})));
    }

    @Override
    public void exitFuncInt(PuffinBasicParser.FuncIntContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.INT, ctx, ctx.expr(),
                NumericOrString.NUMERIC));
    }

    @Override
    public void exitFuncFix(PuffinBasicParser.FuncFixContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.FIX, ctx, ctx.expr(),
                NumericOrString.NUMERIC));
    }

    @Override
    public void exitFuncLog(PuffinBasicParser.FuncLogContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.LOG, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(DOUBLE, c -> {})));
    }

    @Override
    public void exitFuncLen(PuffinBasicParser.FuncLenContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.LEN, ctx, ctx.expr(),
                NumericOrString.STRING,
                ir.getSymbolTable().addTmp(INT32, c -> {})));
    }

    @Override
    public void exitFuncChrDlr(PuffinBasicParser.FuncChrDlrContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.CHRDLR, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(STRING, c -> {})));
    }

    @Override
    public void exitFuncHexDlr(PuffinBasicParser.FuncHexDlrContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.HEXDLR, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(STRING, c -> {})));
    }

    @Override
    public void exitFuncOctDlr(PuffinBasicParser.FuncOctDlrContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.OCTDLR, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(STRING, c -> {})));
    }

    @Override
    public void exitFuncLeftDlr(PuffinBasicParser.FuncLeftDlrContext ctx) {
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
    public void exitFuncRightDlr(PuffinBasicParser.FuncRightDlrContext ctx) {
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
    public void exitFuncInstr(PuffinBasicParser.FuncInstrContext ctx) {
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
    public void exitFuncMidDlr(PuffinBasicParser.FuncMidDlrContext ctx) {
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
    public void exitFuncRnd(PuffinBasicParser.FuncRndContext ctx) {
        nodeToInstruction.put(ctx, ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.RND, NULL_ID, NULL_ID,
                ir.getSymbolTable().addTmp(DOUBLE, c -> {})));
    }

    @Override
    public void exitFuncSgn(PuffinBasicParser.FuncSgnContext ctx) {
        nodeToInstruction.put(ctx, addFuncWithExprInstruction(OpCode.SGN, ctx, ctx.expr(),
                NumericOrString.NUMERIC,
                ir.getSymbolTable().addTmp(INT32, c -> {})));
    }

    @Override
    public void exitFuncTimer(PuffinBasicParser.FuncTimerContext ctx) {
        nodeToInstruction.put(ctx, ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.TIMER, NULL_ID, NULL_ID,
                ir.getSymbolTable().addTmp(DOUBLE, c -> {})));
    }

    @Override
    public void exitFuncStringDlr(PuffinBasicParser.FuncStringDlrContext ctx) {
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
    public void exitFuncLoc(PuffinBasicParser.FuncLocContext ctx) {
        var fileNumber = lookupInstruction(ctx.expr());
        Types.assertNumeric(ir.getSymbolTable().get(fileNumber.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        nodeToInstruction.put(ctx, ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.LOC, fileNumber.result, NULL_ID,
                ir.getSymbolTable().addTmp(INT32, c -> {})));
    }

    @Override
    public void exitFuncLof(PuffinBasicParser.FuncLofContext ctx) {
        var fileNumber = lookupInstruction(ctx.expr());
        Types.assertNumeric(ir.getSymbolTable().get(fileNumber.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        nodeToInstruction.put(ctx, ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.LOF, fileNumber.result, NULL_ID,
                ir.getSymbolTable().addTmp(INT64, c -> {})));
    }

    @Override
    public void exitFuncEof(PuffinBasicParser.FuncEofContext ctx) {
        var fileNumber = lookupInstruction(ctx.expr());
        Types.assertNumeric(ir.getSymbolTable().get(fileNumber.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        nodeToInstruction.put(ctx, ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.EOF, fileNumber.result, NULL_ID,
                ir.getSymbolTable().addTmp(INT32, c -> {})));
    }

    @Override
    public void exitFuncEnvironDlr(PuffinBasicParser.FuncEnvironDlrContext ctx) {
        var expr = lookupInstruction(ctx.expr());
        Types.assertString(ir.getSymbolTable().get(expr.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        nodeToInstruction.put(ctx, ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.ENVIRONDLR, expr.result, NULL_ID,
                ir.getSymbolTable().addTmp(STRING, c -> {})));
    }

    @Override
    public void exitFuncInputDlr(PuffinBasicParser.FuncInputDlrContext ctx) {
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

    @Override
    public void exitFuncInkeyDlr(PuffinBasicParser.FuncInkeyDlrContext ctx) {
        assertGraphics();
        nodeToInstruction.put(ctx, ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.INKEYDLR, NULL_ID, NULL_ID,
                ir.getSymbolTable().addTmp(STRING, c -> {})));
    }

    private Instruction addFuncWithExprInstruction(
            OpCode opCode, ParserRuleContext parent,
            PuffinBasicParser.ExprContext expr, NumericOrString numericOrString)
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
            PuffinBasicParser.ExprContext expr,
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
    public void exitComment(PuffinBasicParser.CommentContext ctx) {
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.COMMENT, NULL_ID, NULL_ID, NULL_ID
        );
    }

    @Override
    public void exitLetstmt(PuffinBasicParser.LetstmtContext ctx) {
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
                        throw new PuffinBasicSemanticError(
                                BAD_ASSIGNMENT,
                                getCtxString(ctx),
                                "Can't assign to UDF: " + variable
                        );
                    }
            checkDataTypeMatch(varEntry.getValue(), exprInstruction.result, () -> getCtxString(ctx));
        });
        var varInstr = lookupInstruction(ctx.variable());

        var assignInstruction = ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.ASSIGN, varInstr.result, exprInstruction.result, varInstr.result
        );
        nodeToInstruction.put(ctx, assignInstruction);

        varDefined.add(variableName);
    }


    @Override
    public void exitPrintstmt(PuffinBasicParser.PrintstmtContext ctx) {
        handlePrintstmt(ctx, ctx.printlist().children, null);
    }

    @Override
    public void exitPrinthashstmt(PuffinBasicParser.PrinthashstmtContext ctx) {
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
            if (child instanceof PuffinBasicParser.ExprContext) {
                var exprInstruction = lookupInstruction((PuffinBasicParser.ExprContext) child);
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
    public void exitPrintusingstmt(PuffinBasicParser.PrintusingstmtContext ctx) {
        handlePrintusing(ctx, ctx.format, ctx.printlist().children, null);
    }

    @Override
    public void exitPrinthashusingstmt(PuffinBasicParser.PrinthashusingstmtContext ctx) {
        var fileNumber = lookupInstruction(ctx.filenum);
        handlePrintusing(ctx, ctx.format, ctx.printlist().children, fileNumber);
    }

    private void handlePrintusing(
            ParserRuleContext ctx,
            PuffinBasicParser.ExprContext formatCtx,
            List<ParseTree> children,
            Instruction fileNumber)
    {
        var format = lookupInstruction(formatCtx);
        boolean endsWithNewline = true;
        for (ParseTree child : children) {
            if (child instanceof PuffinBasicParser.ExprContext) {
                var exprInstruction = lookupInstruction((PuffinBasicParser.ExprContext) child);
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
    public void exitDimstmt(PuffinBasicParser.DimstmtContext ctx) {
        IntList dims = new IntArrayList(ctx.DECIMAL().size());
        for (var dimMax : ctx.DECIMAL()) {
            int dimSize = Numbers.parseInt32(dimMax.getText(), () -> getCtxString(ctx));
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
        varDefined.add(variableName);
    }

    @Override
    public void enterDeffnstmt(PuffinBasicParser.DeffnstmtContext ctx) {
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
    public void exitDeffnstmt(PuffinBasicParser.DeffnstmtContext ctx) {
        var varname = ctx.varname().getText();
        var varsuffix = ctx.varsuffix() != null ? ctx.varsuffix().getText() : null;
        var dataType = ir.getSymbolTable().getDataTypeFor(varname, varsuffix);
        var variableName = new VariableName(varname, dataType);

        ir.getSymbolTable().addVariableOrUDF(variableName,
                variableName1 -> Variable.of(variableName1, false, () -> getCtxString(ctx)),
                (varId, varEntry) -> {
                    var udfEntry = (STUDF) varEntry;
                    var udfState = udfStateMap.get(varEntry.getVariable());
                    for (PuffinBasicParser.VariableContext fnParamCtx : ctx.variable()) {
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
    public void exitEndstmt(PuffinBasicParser.EndstmtContext ctx) {
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.END, NULL_ID, NULL_ID,NULL_ID
        );
    }

    @Override
    public void enterWhilestmt(PuffinBasicParser.WhilestmtContext ctx) {
        var whileLoopState = new WhileLoopState();
        // LABEL beforeWhile
        whileLoopState.labelBeforeWhile = ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.LABEL, ir.getSymbolTable().addLabel(), NULL_ID, NULL_ID
        );
        whileLoopStateList.add(whileLoopState);
    }

    @Override
    public void exitWhilestmt(PuffinBasicParser.WhilestmtContext ctx) {
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
    public void exitWendstmt(PuffinBasicParser.WendstmtContext ctx) {
        if (whileLoopStateList.isEmpty()) {
            throw new PuffinBasicSemanticError(
                    PuffinBasicSemanticError.ErrorCode.WEND_WITHOUT_WHILE,
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
    public void exitForstmt(PuffinBasicParser.ForstmtContext ctx) {
        var var = lookupInstruction(ctx.variable());
        var init = lookupInstruction(ctx.expr(0));
        var end = lookupInstruction(ctx.expr(1));
        Types.assertNumeric(ir.getSymbolTable().get(init.result).getValue().getDataType(), () -> getCtxString(ctx));
        Types.assertNumeric(ir.getSymbolTable().get(end.result).getValue().getDataType(), () -> getCtxString(ctx));

        var forLoopState = new ForLoopState();
        forLoopState.variable = ((STVariable) ir.getSymbolTable().get(var.result)).getVariable();

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
    public void exitNextstmt(PuffinBasicParser.NextstmtContext ctx) {
        List<ForLoopState> states = new ArrayList<>(1);
        if (ctx.variable().isEmpty()) {
            if (!forLoopStateList.isEmpty()) {
                states.add(forLoopStateList.removeLast());
            } else {
                throw new PuffinBasicSemanticError(
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
                var variable = ((STVariable) ir.getSymbolTable().get(id)).getVariable();

                var state = forLoopStateList.removeLast();
                if (state.variable.equals(variable)) {
                    states.add(state);
                } else {
                    throw new PuffinBasicSemanticError(
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
    public void enterIfThenElse(PuffinBasicParser.IfThenElseContext ctx) {
        nodeToIfState.put(ctx, new IfState());
    }

    @Override
    public void exitIfThenElse(PuffinBasicParser.IfThenElseContext ctx) {
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
    public void enterThen(PuffinBasicParser.ThenContext ctx) {
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
    public void exitThen(PuffinBasicParser.ThenContext ctx) {
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
    public void enterElsestmt(PuffinBasicParser.ElsestmtContext ctx) {
        var ifState = nodeToIfState.get(ctx.getParent());
        ifState.labelBeforeElse = ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.LABEL, ir.getSymbolTable().addLabel(), NULL_ID, NULL_ID
        );
    }

    @Override
    public void exitElsestmt(PuffinBasicParser.ElsestmtContext ctx) {
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
    public void exitGosubstmt(PuffinBasicParser.GosubstmtContext ctx) {
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
    public void exitReturnstmt(PuffinBasicParser.ReturnstmtContext ctx) {
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
    public void exitGotostmt(PuffinBasicParser.GotostmtContext ctx) {
        var gotoLinenum = parseLinenum(ctx.linenum().getText());
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.GOTO_LINENUM, getGotoLineNumberOp1(gotoLinenum), NULL_ID, NULL_ID
        );
    }

    @Override
    public void exitSwapstmt(PuffinBasicParser.SwapstmtContext ctx) {
        var var1 = lookupInstruction(ctx.variable(0));
        var var2 = lookupInstruction(ctx.variable(1));
        var dt1 = ir.getSymbolTable().get(var1.result).getValue().getDataType();
        var dt2 = ir.getSymbolTable().get(var2.result).getValue().getDataType();
        if (dt1 != dt2) {
            throw new PuffinBasicSemanticError(
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
    public void exitOpen1stmt(PuffinBasicParser.Open1stmtContext ctx) {
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
    public void exitOpen2stmt(PuffinBasicParser.Open2stmtContext ctx) {
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
    public void exitClosestmt(PuffinBasicParser.ClosestmtContext ctx) {
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
    public void exitFieldstmt(PuffinBasicParser.FieldstmtContext ctx) {
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

    private void assertVariable(STKind kind, Supplier<String> line) {
        if (kind != STKind.VARIABLE && kind != STKind.ARRAYREF) {
            throw new PuffinBasicSemanticError(
                    BAD_ARGUMENT,
                    line.get(),
                    "Expected variable, but found: " + kind
            );
        }
    }

    private void assertVariableDefined(VariableName variableName, Supplier<String> line) {
        if (!varDefined.contains(variableName)) {
            throw new PuffinBasicSemanticError(
                    NOT_DEFINED,
                    line.get(),
                    "Variable: " + variableName + " used before it is defined!"
            );
        }
    }

    @Override
    public void exitPutstmt(PuffinBasicParser.PutstmtContext ctx) {
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
    public void exitMiddlrstmt(PuffinBasicParser.MiddlrstmtContext ctx) {
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
        assertVariableDefined(((STVariable) ir.getSymbolTable().get(varInstr.result)).getVariable().getVariableName(),
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
    public void exitGetstmt(PuffinBasicParser.GetstmtContext ctx) {
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
    public void exitRandomizestmt(PuffinBasicParser.RandomizestmtContext ctx) {
        var exprId = lookupInstruction(ctx.expr()).result;
        Types.assertNumeric(ir.getSymbolTable().get(exprId).getValue().getDataType(),
                () -> getCtxString(ctx));
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.RANDOMIZE, exprId, NULL_ID, NULL_ID
        );
    }

    @Override
    public void exitRandomizetimerstmt(PuffinBasicParser.RandomizetimerstmtContext ctx) {
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.RANDOMIZE_TIMER, NULL_ID, NULL_ID, NULL_ID
        );
    }

    @Override
    public void exitDefintstmt(PuffinBasicParser.DefintstmtContext ctx) {
        handleDefTypeStmt(ctx.LETTERRANGE(), INT32);
    }

    @Override
    public void exitDeflngstmt(PuffinBasicParser.DeflngstmtContext ctx) {
        handleDefTypeStmt(ctx.LETTERRANGE(), INT64);
    }

    @Override
    public void exitDefsngstmt(PuffinBasicParser.DefsngstmtContext ctx) {
        handleDefTypeStmt(ctx.LETTERRANGE(), FLOAT);
    }

    @Override
    public void exitDefdblstmt(PuffinBasicParser.DefdblstmtContext ctx) {
        handleDefTypeStmt(ctx.LETTERRANGE(), DOUBLE);
    }

    @Override
    public void exitDefstrstmt(PuffinBasicParser.DefstrstmtContext ctx) {
        handleDefTypeStmt(ctx.LETTERRANGE(), STRING);
    }

    @Override
    public void exitLsetstmt(PuffinBasicParser.LsetstmtContext ctx) {
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
    public void exitRsetstmt(PuffinBasicParser.RsetstmtContext ctx) {
        var varInstr = lookupInstruction(ctx.variable());
        var exprInstr = lookupInstruction(ctx.expr());

        var varEntry = ir.getSymbolTable().get(varInstr.result);
        assertVariable(varEntry.getKind(), () -> getCtxString(ctx));
        assertVariableDefined(((STVariable) varEntry).getVariable().getVariableName(),
                () -> getCtxString(ctx));
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
    public void exitInputstmt(PuffinBasicParser.InputstmtContext ctx) {
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
    public void exitInputhashstmt(PuffinBasicParser.InputhashstmtContext ctx) {
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
    public void exitLineinputstmt(PuffinBasicParser.LineinputstmtContext ctx) {
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
    public void exitLineinputhashstmt(PuffinBasicParser.LineinputhashstmtContext ctx) {
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
    public void exitWritestmt(PuffinBasicParser.WritestmtContext ctx) {
        handleWritestmt(ctx, ctx.expr(), null);
    }

    @Override
    public void exitWritehashstmt(PuffinBasicParser.WritehashstmtContext ctx) {
        var fileNumInstr = lookupInstruction(ctx.filenum);
        handleWritestmt(ctx, ctx.expr(), fileNumInstr);
    }

    public void handleWritestmt(
            ParserRuleContext ctx,
            List<PuffinBasicParser.ExprContext> exprs,
            @Nullable Instruction fileNumber) {
        // if fileNumber != null, skip first instruction
        for (int i = fileNumber == null ? 0 : 1; i < exprs.size(); i++) {
            var exprCtx = exprs.get(i);
            var exprInstr = lookupInstruction(exprCtx);
            ir.addInstruction(
                    currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                    OpCode.WRITE, exprInstr.result, NULL_ID, NULL_ID
            );
            if (i + 1 < exprs.size()) {
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
    public void exitReadstmt(PuffinBasicParser.ReadstmtContext ctx) {
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
    public void exitRestorestmt(PuffinBasicParser.RestorestmtContext ctx) {
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.RESTORE, NULL_ID, NULL_ID, NULL_ID
        );
    }

    @Override
    public void exitDatastmt(PuffinBasicParser.DatastmtContext ctx) {
        var children = ctx.children;
        for (int i = 1; i < children.size(); i += 2) {
            var child = children.get(i);
            int valueId;
            if (child instanceof PuffinBasicParser.NumberContext) {
                valueId = lookupInstruction((PuffinBasicParser.NumberContext) child).result;
            } else {
                var text = unquote(child.getText());
                valueId = ir.getSymbolTable().addTmp(STRING, e -> e.getValue().setString(text));
            }
            ir.addInstruction(
                    currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                    OpCode.DATA, valueId, NULL_ID, NULL_ID
            );
        }
    }

    // GraphicsRuntime


    @Override
    public void exitScreenstmt(PuffinBasicParser.ScreenstmtContext ctx) {
        assertGraphics();

        var title = lookupInstruction(ctx.expr(0));
        var w = lookupInstruction(ctx.expr(1));
        var h = lookupInstruction(ctx.expr(2));
        var manualRepaint = ctx.mr != null;

        Types.assertString(ir.getSymbolTable().get(title.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        Types.assertNumeric(ir.getSymbolTable().get(w.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        Types.assertNumeric(ir.getSymbolTable().get(h.result).getValue().getDataType(),
                () -> getCtxString(ctx));

        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.SCREEN0, w.result, h.result, NULL_ID
        );
        var repaint = ir.getSymbolTable().addTmp(INT32, e -> e.getValue().setInt32(
                manualRepaint ? 0 : -1));
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.SCREEN, title.result, repaint, NULL_ID
        );
    }

    @Override
    public void exitRepaintstmt(PuffinBasicParser.RepaintstmtContext ctx) {
        assertGraphics();
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.REPAINT, NULL_ID, NULL_ID, NULL_ID
        );
    }

    @Override
    public void exitCirclestmt(PuffinBasicParser.CirclestmtContext ctx) {
        assertGraphics();

        var x = lookupInstruction(ctx.x);
        var y = lookupInstruction(ctx.y);
        var r1 = lookupInstruction(ctx.r1);
        var r2 = lookupInstruction(ctx.r2);
        var s = ctx.s != null ? lookupInstruction(ctx.s) : null;
        var e = ctx.e != null ? lookupInstruction(ctx.e) : null;
        var fill = ctx.fill != null ? lookupInstruction(ctx.fill) : null;
        Types.assertNumeric(ir.getSymbolTable().get(x.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        Types.assertNumeric(ir.getSymbolTable().get(y.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        Types.assertNumeric(ir.getSymbolTable().get(r1.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        Types.assertNumeric(ir.getSymbolTable().get(r2.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        if (s != null) {
            Types.assertNumeric(ir.getSymbolTable().get(s.result).getValue().getDataType(),
                    () -> getCtxString(ctx));
        }
        if (e != null) {
            Types.assertNumeric(ir.getSymbolTable().get(e.result).getValue().getDataType(),
                    () -> getCtxString(ctx));
        }
        if (fill != null) {
            Types.assertString(ir.getSymbolTable().get(fill.result).getValue().getDataType(),
                    () -> getCtxString(ctx));
        }
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.CIRCLE_XY, x.result, y.result, NULL_ID
        );
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.CIRCLE_SE,
                s != null ? s.result : NULL_ID,
                e != null ? e.result : NULL_ID,
                NULL_ID
        );
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.CIRCLE_FILL,
                fill != null ? fill.result : NULL_ID,
                NULL_ID,
                NULL_ID
        );
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.CIRCLE, r1.result, r2.result, NULL_ID
        );
    }

    @Override
    public void exitLinestmt(PuffinBasicParser.LinestmtContext ctx) {
        assertGraphics();

        var x1 = lookupInstruction(ctx.x1);
        var y1 = lookupInstruction(ctx.y1);
        var x2 = lookupInstruction(ctx.x2);
        var y2 = lookupInstruction(ctx.y2);

        Types.assertNumeric(ir.getSymbolTable().get(x1.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        Types.assertNumeric(ir.getSymbolTable().get(y1.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        Types.assertNumeric(ir.getSymbolTable().get(x2.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        Types.assertNumeric(ir.getSymbolTable().get(y2.result).getValue().getDataType(),
                () -> getCtxString(ctx));

        Instruction bf = null;
        if (ctx.bf != null){
            bf = lookupInstruction(ctx.bf);
            Types.assertString(ir.getSymbolTable().get(bf.result).getValue().getDataType(),
                    () -> getCtxString(ctx));
        }
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.LINE_x1y1, x1.result, y1.result, NULL_ID
        );
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.LINE_x2y2, x2.result, y2.result, NULL_ID
        );
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.LINE, bf != null ? bf.result : NULL_ID, NULL_ID, NULL_ID
        );
    }

    @Override
    public void exitColorstmt(PuffinBasicParser.ColorstmtContext ctx) {
        var r = lookupInstruction(ctx.r);
        var g = lookupInstruction(ctx.g);
        var b = lookupInstruction(ctx.b);
        Types.assertNumeric(ir.getSymbolTable().get(r.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        Types.assertNumeric(ir.getSymbolTable().get(g.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        Types.assertNumeric(ir.getSymbolTable().get(b.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.COLOR_RG, r.result, g.result, NULL_ID
        );
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.COLOR, b.result, NULL_ID, NULL_ID
        );
    }

    @Override
    public void exitPaintstmt(PuffinBasicParser.PaintstmtContext ctx) {
        assertGraphics();

        var x = lookupInstruction(ctx.x);
        var y = lookupInstruction(ctx.y);
        var r = lookupInstruction(ctx.r);
        var g = lookupInstruction(ctx.g);
        var b = lookupInstruction(ctx.b);

        Types.assertNumeric(ir.getSymbolTable().get(x.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        Types.assertNumeric(ir.getSymbolTable().get(y.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        Types.assertNumeric(ir.getSymbolTable().get(r.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        Types.assertNumeric(ir.getSymbolTable().get(g.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        Types.assertNumeric(ir.getSymbolTable().get(b.result).getValue().getDataType(),
                () -> getCtxString(ctx));

        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.PAINT_RG, r.result, g.result, NULL_ID
        );
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.PAINT_B, b.result, NULL_ID, NULL_ID
        );
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.PAINT, x.result, y.result, NULL_ID
        );
    }

    @Override
    public void exitPsetstmt(PuffinBasicParser.PsetstmtContext ctx) {
        assertGraphics();

        var x = lookupInstruction(ctx.x);
        var y = lookupInstruction(ctx.y);

        int rId = NULL_ID, gId = NULL_ID, bId = NULL_ID;
        if (ctx.r != null) {
            rId = lookupInstruction(ctx.r).result;
            Types.assertNumeric(ir.getSymbolTable().get(rId).getValue().getDataType(),
                    () -> getCtxString(ctx));
        }
        if (ctx.g != null) {
            gId = lookupInstruction(ctx.g).result;
            Types.assertNumeric(ir.getSymbolTable().get(gId).getValue().getDataType(),
                    () -> getCtxString(ctx));
        }
        if (ctx.b != null) {
            bId = lookupInstruction(ctx.b).result;
            Types.assertNumeric(ir.getSymbolTable().get(bId).getValue().getDataType(),
                    () -> getCtxString(ctx));
        }

        Types.assertNumeric(ir.getSymbolTable().get(x.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        Types.assertNumeric(ir.getSymbolTable().get(y.result).getValue().getDataType(),
                () -> getCtxString(ctx));

        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.PSET_RG, rId, gId, NULL_ID
        );
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.PSET_B, bId, NULL_ID, NULL_ID
        );
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.PSET, x.result, y.result, NULL_ID
        );
    }

    @Override
    public void exitGraphicsgetstmt(PuffinBasicParser.GraphicsgetstmtContext ctx) {
        assertGraphics();

        var x1 = lookupInstruction(ctx.x1);
        var y1 = lookupInstruction(ctx.y1);
        var x2 = lookupInstruction(ctx.x2);
        var y2 = lookupInstruction(ctx.y2);
        var varInstr = lookupInstruction(ctx.variable());

        Types.assertNumeric(ir.getSymbolTable().get(x1.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        Types.assertNumeric(ir.getSymbolTable().get(y1.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        Types.assertNumeric(ir.getSymbolTable().get(x2.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        Types.assertNumeric(ir.getSymbolTable().get(y2.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        assertVariable(ir.getSymbolTable().get(varInstr.result).getKind(),
                () -> getCtxString(ctx));
        assertVariableDefined(((STVariable) ir.getSymbolTable().get(varInstr.result)).getVariable().getVariableName(),
                () -> getCtxString(ctx));

        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.GGET_X1Y1, x1.result, y1.result, NULL_ID
        );
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.GGET_X2Y2, x2.result, y2.result, NULL_ID
        );
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.GGET, varInstr.result, NULL_ID, NULL_ID
        );
    }

    @Override
    public void exitGraphicsputstmt(PuffinBasicParser.GraphicsputstmtContext ctx) {
        assertGraphics();

        var x = lookupInstruction(ctx.x);
        var y = lookupInstruction(ctx.y);
        var varInstr = lookupInstruction(ctx.variable());
        var action = ctx.action != null ? lookupInstruction(ctx.action) : null;

        Types.assertNumeric(ir.getSymbolTable().get(x.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        Types.assertNumeric(ir.getSymbolTable().get(y.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        assertVariable(ir.getSymbolTable().get(varInstr.result).getKind(),
                () -> getCtxString(ctx));
        assertVariableDefined(((STVariable) ir.getSymbolTable().get(varInstr.result)).getVariable().getVariableName(),
                () -> getCtxString(ctx));
        if (action != null) {
            Types.assertString(ir.getSymbolTable().get(action.result).getValue().getDataType(),
                    () -> getCtxString(ctx));
        }

        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.GPUT_XY, x.result, y.result, NULL_ID
        );
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.GPUT,
                action != null ? action.result : NULL_ID,
                varInstr.result,
                NULL_ID
        );
    }

    @Override
    public void exitDrawstmt(PuffinBasicParser.DrawstmtContext ctx) {
        assertGraphics();

        var str = lookupInstruction(ctx.expr());
        Types.assertString(ir.getSymbolTable().get(str.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.DRAW, str.result, NULL_ID, NULL_ID
        );
    }

    @Override
    public void exitFontstmt(PuffinBasicParser.FontstmtContext ctx) {
        assertGraphics();

        var name = lookupInstruction(ctx.name);
        var style = lookupInstruction(ctx.style);
        var size = lookupInstruction(ctx.size);

        Types.assertString(ir.getSymbolTable().get(style.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        Types.assertNumeric(ir.getSymbolTable().get(size.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        Types.assertString(ir.getSymbolTable().get(name.result).getValue().getDataType(),
                () -> getCtxString(ctx));

        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.FONT_SS, style.result, size.result, NULL_ID
        );
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.FONT, name.result, NULL_ID, NULL_ID
        );
    }

    @Override
    public void exitDrawstrstmt(PuffinBasicParser.DrawstrstmtContext ctx) {
        var str = lookupInstruction(ctx.str);
        var x = lookupInstruction(ctx.x);
        var y = lookupInstruction(ctx.y);

        Types.assertNumeric(ir.getSymbolTable().get(x.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        Types.assertNumeric(ir.getSymbolTable().get(y.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        Types.assertString(ir.getSymbolTable().get(str.result).getValue().getDataType(),
                () -> getCtxString(ctx));

        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.DRAWSTR_XY, x.result, y.result, NULL_ID
        );
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.DRAWSTR, str.result, NULL_ID, NULL_ID
        );
    }

    @Override
    public void exitLoadimgstmt(PuffinBasicParser.LoadimgstmtContext ctx) {
        assertGraphics();

        var path = lookupInstruction(ctx.path);
        var varInstr = lookupInstruction(ctx.variable());

        Types.assertString(ir.getSymbolTable().get(path.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        assertVariable(ir.getSymbolTable().get(varInstr.result).getKind(),
                () -> getCtxString(ctx));
        assertVariableDefined(((STVariable) ir.getSymbolTable().get(varInstr.result)).getVariable().getVariableName(),
                () -> getCtxString(ctx));

        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.LOADIMG, path.result, varInstr.result, NULL_ID
        );
    }

    @Override
    public void exitClsstmt(PuffinBasicParser.ClsstmtContext ctx) {
        assertGraphics();
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.CLS, NULL_ID, NULL_ID, NULL_ID
        );
    }

    @Override
    public void exitSleepstmt(PuffinBasicParser.SleepstmtContext ctx) {
        var millis = lookupInstruction(ctx.expr());
        Types.assertNumeric(ir.getSymbolTable().get(millis.result).getValue().getDataType(),
                () -> getCtxString(ctx));
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.SLEEP, millis.result, NULL_ID, NULL_ID
        );
    }

    @Override
    public void exitBeepstmt(PuffinBasicParser.BeepstmtContext ctx) {
        assertGraphics();
        ir.addInstruction(
                currentLineNumber, ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                OpCode.BEEP, NULL_ID, NULL_ID, NULL_ID
        );
    }

    private void assertGraphics() {
        if (!graphics) {
            throw new PuffinBasicInternalError(
                    "GraphicsRuntime is not enabled!"
            );
        }
    }

    private void handleDefTypeStmt(
            List<TerminalNode> letterRanges,
            PuffinBasicDataType dataType) {
        List<Character> defs = new ArrayList<>();
        letterRanges.stream().map(ParseTree::getText).forEach(lr -> {
            for (char i = lr.charAt(0); i <= lr.charAt(2); i++) {
                defs.add(i);
            }
        });
        defs.forEach(def -> ir.getSymbolTable().setDefaultDataType(def, dataType));
    }

    private static FileOpenMode getFileOpenMode(PuffinBasicParser.Filemode1Context filemode1) {
        var mode = filemode1 != null ? unquote(filemode1.getText()) : null;
        if (mode == null || mode.equalsIgnoreCase("r")) {
            return FileOpenMode.RANDOM;
        } else if (mode.equalsIgnoreCase("i")) {
            return FileOpenMode.INPUT;
        } else if (mode.equalsIgnoreCase("o")) {
            return FileOpenMode.OUTPUT;
        } else {
            return FileOpenMode.APPEND;
        }
    }

    private static FileOpenMode getFileOpenMode(PuffinBasicParser.Filemode2Context filemode2) {
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

    private static FileAccessMode getFileAccessMode(PuffinBasicParser.AccessContext access) {
        if (access == null || (access.READ() != null && access.WRITE() != null)) {
            return FileAccessMode.READ_WRITE;
        } else if (access.READ() != null) {
            return FileAccessMode.READ_ONLY;
        } else {
            return FileAccessMode.WRITE_ONLY;
        }
    }

    private static LockMode getLockMode(PuffinBasicParser.LockContext lock) {
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
            throw new PuffinBasicSemanticError(
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
