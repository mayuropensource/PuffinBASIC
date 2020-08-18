package org.puffinbasic.runtime;

import org.puffinbasic.parser.PuffinBasicIR.Instruction;
import org.puffinbasic.parser.PuffinBasicIR.OpCode;
import org.puffinbasic.domain.PuffinBasicSymbolTable;
import org.puffinbasic.error.PuffinBasicInternalError;
import org.puffinbasic.error.PuffinBasicRuntimeError;

import static org.puffinbasic.error.PuffinBasicRuntimeError.ErrorCode.DIVISION_BY_ZERO;
import static org.puffinbasic.domain.STObjects.PuffinBasicDataType.DOUBLE;
import static org.puffinbasic.domain.STObjects.PuffinBasicDataType.FLOAT;
import static org.puffinbasic.domain.STObjects.PuffinBasicDataType.INT64;
import static org.puffinbasic.domain.STObjects.PuffinBasicDataType.STRING;

public class Operators {

    public static void unaryMinus(
            PuffinBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        var op1 = symbolTable.get(instruction.op1).getValue();
        var result = symbolTable.get(instruction.result).getValue();
        switch (op1.getDataType()) {
            case INT32:
                result.setInt32(-op1.getInt32());
                break;
            case INT64:
                result.setInt64(-op1.getInt64());
                break;
            case FLOAT:
                result.setFloat32(-op1.getFloat32());
                break;
            case DOUBLE:
                result.setFloat64(-op1.getFloat64());
                break;
            default:
                throw new PuffinBasicInternalError(
                        "Unary minus is not supported for data type: " + op1.getDataType()
                );
        }
    }

    public static void concat(
            PuffinBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        var v1 = symbolTable.get(instruction.op1).getValue().getString();
        var v2 = symbolTable.get(instruction.op2).getValue().getString();
        var result = symbolTable.get(instruction.result).getValue();
        result.setString(v1 + v2);
    }

    public static void arithmetic(
            PuffinBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        var opCode = instruction.opCode;
        var v1 = symbolTable.get(instruction.op1).getValue();
        var v2 = symbolTable.get(instruction.op2).getValue();
        var result = symbolTable.get(instruction.result).getValue();

        switch (opCode) {
            case MOD:
                result.setInt32(v1.getRoundedInt32() % v2.getRoundedInt32());
                return;
            case IDIV:
                result.setInt32(v1.getRoundedInt32() / v2.getRoundedInt32());
                return;
        }

        var dt = result.getDataType();

        if (dt == DOUBLE) {
            switch (opCode) {
                case ADD:
                    result.setFloat64(v1.getFloat64() + v2.getFloat64());
                    break;
                case SUB:
                    result.setFloat64(v1.getFloat64() - v2.getFloat64());
                    break;
                case MUL:
                    result.setFloat64(v1.getFloat64() * v2.getFloat64());
                    break;
                case FDIV: {
                    if (v2.getFloat64() == 0) {
                        throw new PuffinBasicRuntimeError(
                                DIVISION_BY_ZERO,
                                "Division by zero"
                        );
                    }
                    result.setFloat64(v1.getFloat64() / v2.getFloat64());
                }
                break;
                case EXP:
                    result.setFloat64(Math.pow(v1.getFloat64(), v2.getFloat64()));
                    break;
                default:
                    throw new PuffinBasicInternalError(
                            "Arithmetic op: " + opCode + " is not supported"
                    );
            }
        } else if (dt == INT64) {
            switch (opCode) {
                case ADD:
                    result.setInt64(v1.getInt64() + v2.getInt64());
                    break;
                case SUB:
                    result.setInt64(v1.getInt64() - v2.getInt64());
                    break;
                case MUL:
                    result.setInt64(v1.getInt64() * v2.getInt64());
                    break;
                case FDIV: {
                    if (v2.getInt64() == 0) {
                        throw new PuffinBasicRuntimeError(
                                DIVISION_BY_ZERO,
                                "Division by zero"
                        );
                    }
                    result.setInt64(v1.getInt64() / v2.getInt64());
                }
                break;
                case EXP:
                    result.setInt64((long) Math.pow(v1.getInt64(), v2.getInt64()));
                    break;
                default:
                    throw new PuffinBasicInternalError(
                            "Arithmetic op: " + opCode + " is not supported"
                    );
            }
        } else if (dt == FLOAT) {
            switch (opCode) {
                case ADD:
                    result.setFloat32(v1.getFloat32() + v2.getFloat32());
                    break;
                case SUB:
                    result.setFloat32(v1.getFloat32() - v2.getFloat32());
                    break;
                case MUL:
                    result.setFloat32(v1.getFloat32() * v2.getFloat32());
                    break;
                case FDIV: {
                    if (v2.getFloat32() == 0) {
                        throw new PuffinBasicRuntimeError(
                                DIVISION_BY_ZERO,
                                "Division by zero"
                        );
                    }
                    result.setFloat32(v1.getFloat32() / v2.getFloat32());
                }
                break;
                case EXP:
                    result.setFloat32((float) Math.pow(v1.getFloat32(), v2.getFloat32()));
                    break;
                default:
                    throw new PuffinBasicInternalError(
                            "Arithmetic op: " + opCode + " is not supported"
                    );
            }
        } else {
            switch (opCode) {
                case ADD:
                    result.setInt32(v1.getInt32() + v2.getInt32());
                    break;
                case SUB:
                    result.setInt32(v1.getInt32() - v2.getInt32());
                    break;
                case MUL:
                    result.setInt32(v1.getInt32() * v2.getInt32());
                    break;
                case FDIV: {
                    if (v2.getInt32() == 0) {
                        throw new PuffinBasicRuntimeError(
                                DIVISION_BY_ZERO,
                                "Division by zero"
                        );
                    }
                    result.setInt32(v1.getInt32() / v2.getInt32());
                }
                break;
                case EXP:
                    result.setInt32((int) Math.pow(v1.getInt32(), v2.getInt32()));
                    break;
                default:
                    throw new PuffinBasicInternalError(
                            "Arithmetic op: " + opCode + " is not supported"
                    );
            }
        }
    }

    public static void logical(PuffinBasicSymbolTable symbolTable, Instruction instruction) {
        var opCode = instruction.opCode;
        var v1 = symbolTable.get(instruction.op1).getValue().getInt64();
        var v2 = symbolTable.get(instruction.op2).getValue().getInt64();
        var result = symbolTable.get(instruction.result).getValue();

        if ((v1 == -1 || v1 == 0) && (v2 == -1 || v2 == 0)) {
            var b1 = v1 == -1;
            var b2 = v2 == -1;
            switch (opCode) {
                case AND:
                    result.setInt64(b1 && b2 ? -1 : 0);
                    break;
                case OR:
                    result.setInt64(b1 || b2 ? -1 : 0);
                    break;
                case XOR:
                    result.setInt64(b1 ^ b2 ? -1 : 0);
                    break;
                case EQV:
                    result.setInt64(b1 == b2 ? -1 : 0);
                    break;
                case IMP:
                    result.setInt64((!b1) || b2 ? -1 : 0);
                    break;
                default:
                    throw new PuffinBasicInternalError(
                            "Logical op: " + instruction.opCode + " is not supported"
                    );
            }
        } else {
            switch (opCode) {
                case AND:
                    result.setInt64(v1 & v2);
                    break;
                case OR:
                    result.setInt64(v1 | v2);
                    break;
                case XOR:
                    result.setInt64(v1 ^ v2);
                    break;
                case EQV:
                    result.setInt64(~(v1 ^ v2));
                    break;
                case IMP:
                    result.setInt64((~v1) | v2);
                    break;
                default:
                    throw new PuffinBasicInternalError(
                            "Logical op: " + instruction.opCode + " is not supported"
                    );
            }
        }
    }

    public static void relational(
            PuffinBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        var opCode = instruction.opCode;
        var e1 = symbolTable.get(instruction.op1).getValue();
        var e2 = symbolTable.get(instruction.op2).getValue();
        var dt1 = e1.getDataType();
        var dt2 = e2.getDataType();
        var result = symbolTable.get(instruction.result).getValue();
        final long longResult;
        if (dt1 == STRING && dt2 == STRING) {
            longResult = relational(opCode, e1.getString().compareTo(e2.getString()));
        } else {
            if (dt1 == DOUBLE || dt2 == DOUBLE) {
                longResult = relational(opCode, Double.compare(e1.getFloat64(), e2.getFloat64()));
            } else if (dt1 == INT64 || dt2 == INT64) {
                longResult = relational(opCode, Float.compare(e1.getInt64(), e2.getInt64()));
            } else if (dt1 == FLOAT || dt2 == FLOAT) {
                longResult = relational(opCode, Float.compare(e1.getFloat32(), e2.getFloat32()));
            } else {
                longResult = relational(opCode, Integer.compare(e1.getInt32(), e2.getInt32()));
            }
        }
        result.setInt64(longResult);
    }

    private static int relational(OpCode opCode, int cmp) {
        switch (opCode) {
            case LT: return cmp < 0 ? -1 : 0;
            case LE: return cmp <= 0 ? -1 : 0;
            case GT: return cmp > 0 ? -1 : 0;
            case GE: return cmp >= 0 ? -1 : 0;
            case EQ: return cmp == 0 ? -1 : 0;
            case NE: return cmp != 0 ? -1 : 0;
            default:
                throw new PuffinBasicInternalError(
                        "Relational op: " + opCode + " is not supported"
                );
        }
    }

    public static void unaryNot(
            PuffinBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        var v = symbolTable.get(instruction.op1).getValue().getInt64();
        var result = symbolTable.get(instruction.result).getValue();
        if (v == -1) {
            result.setInt64(0);
        } else if (v == 0) {
            result.setInt64(-1);
        } else {
            result.setInt64(~v);
        }
    }
}
