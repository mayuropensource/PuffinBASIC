package org.puffinbasic.runtime;

import org.puffinbasic.domain.PuffinBasicSymbolTable;
import org.puffinbasic.domain.STObjects.PuffinBasicDataType;
import org.puffinbasic.error.PuffinBasicInternalError;
import org.puffinbasic.error.PuffinBasicRuntimeError;
import org.puffinbasic.parser.PuffinBasicIR.Instruction;
import org.puffinbasic.parser.PuffinBasicIR.OpCode;

import static org.puffinbasic.domain.STObjects.PuffinBasicDataType.DOUBLE;
import static org.puffinbasic.domain.STObjects.PuffinBasicDataType.FLOAT;
import static org.puffinbasic.domain.STObjects.PuffinBasicDataType.INT32;
import static org.puffinbasic.domain.STObjects.PuffinBasicDataType.INT64;
import static org.puffinbasic.domain.STObjects.PuffinBasicDataType.STRING;
import static org.puffinbasic.error.PuffinBasicRuntimeError.ErrorCode.DIVISION_BY_ZERO;

final class Operators {

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

    public static void leftShift(
            PuffinBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        var v1 = symbolTable.get(instruction.op1).getValue();
        var v2 = symbolTable.get(instruction.op2).getValue();
        var result = symbolTable.get(instruction.result).getValue();
        if (v1.getDataType() == INT32 && v2.getDataType() == INT32) {
            result.setInt32(v1.getRoundedInt32() << v2.getRoundedInt32());
        } else {
            result.setInt64(v1.getRoundedInt64() << v2.getRoundedInt64());
        }
    }

    public static void rightShift(
            PuffinBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        var v1 = symbolTable.get(instruction.op1).getValue();
        var v2 = symbolTable.get(instruction.op2).getValue();
        var result = symbolTable.get(instruction.result).getValue();
        if (v1.getDataType() == INT32 && v2.getDataType() == INT32) {
            result.setInt32(v1.getRoundedInt32() >> v2.getRoundedInt32());
        } else {
            result.setInt64(v1.getRoundedInt64() >> v2.getRoundedInt64());
        }
    }

    public static void mod(
            PuffinBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        var v1 = symbolTable.get(instruction.op1).getValue();
        var v2 = symbolTable.get(instruction.op2).getValue();
        var result = symbolTable.get(instruction.result).getValue();
        if (v1.getDataType() == INT32 && v2.getDataType() == INT32) {
            result.setInt32(v1.getRoundedInt32() % v2.getRoundedInt32());
        } else {
            result.setInt64(v1.getRoundedInt64() % v2.getRoundedInt64());
        }
    }

    public static void idiv(
            PuffinBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        var v1 = symbolTable.get(instruction.op1).getValue();
        var v2 = symbolTable.get(instruction.op2).getValue();
        var result = symbolTable.get(instruction.result).getValue();
        if (v1.getDataType() == INT32 && v2.getDataType() == INT32) {
            if (v2.getRoundedInt32() == 0) {
                throw new PuffinBasicRuntimeError(
                        DIVISION_BY_ZERO,
                        "Division by zero"
                );
            }
            result.setInt32(v1.getRoundedInt32() / v2.getRoundedInt32());
        } else {
            if (v2.getRoundedInt64() == 0) {
                throw new PuffinBasicRuntimeError(
                        DIVISION_BY_ZERO,
                        "Division by zero"
                );
            }
            result.setInt64(v1.getRoundedInt64() / v2.getRoundedInt64());
        }
    }

    private static void throwIllegalDataTypeError(OpCode opCode, PuffinBasicDataType dt) {
        throw new PuffinBasicInternalError(
                "Illegal data type: " + opCode + " cannot be used with " + dt
        );
    }

    public static void addInt32(
            PuffinBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        var v1 = symbolTable.get(instruction.op1).getValue();
        var v2 = symbolTable.get(instruction.op2).getValue();
        var result = symbolTable.get(instruction.result).getValue();
        result.setInt32(v1.getInt32() + v2.getInt32());
    }

    public static void addInt64(
            PuffinBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        var v1 = symbolTable.get(instruction.op1).getValue();
        var v2 = symbolTable.get(instruction.op2).getValue();
        var result = symbolTable.get(instruction.result).getValue();
        result.setInt64(v1.getInt64() + v2.getInt64());
    }

    public static void addFloat32(
            PuffinBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        var v1 = symbolTable.get(instruction.op1).getValue();
        var v2 = symbolTable.get(instruction.op2).getValue();
        var result = symbolTable.get(instruction.result).getValue();
        result.setFloat32(v1.getFloat32() + v2.getFloat32());
    }

    public static void addFloat64(
            PuffinBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        var v1 = symbolTable.get(instruction.op1).getValue();
        var v2 = symbolTable.get(instruction.op2).getValue();
        var result = symbolTable.get(instruction.result).getValue();
        result.setFloat64(v1.getFloat64() + v2.getFloat64());
    }

    public static void sub(
            PuffinBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        var opCode = instruction.opCode;
        var v1 = symbolTable.get(instruction.op1).getValue();
        var v2 = symbolTable.get(instruction.op2).getValue();
        var result = symbolTable.get(instruction.result).getValue();
        var dt = result.getDataType();

        switch (dt) {
            case INT32:
                result.setInt32(v1.getInt32() - v2.getInt32());
                break;
            case INT64:
                result.setInt64(v1.getInt64() - v2.getInt64());
                break;
            case FLOAT:
                result.setFloat32(v1.getFloat32() - v2.getFloat32());
                break;
            case DOUBLE:
                result.setFloat64(v1.getFloat64() - v2.getFloat64());
                break;
            default:
                throwIllegalDataTypeError(opCode, dt);
                break;
        }
    }

    public static void mul(
            PuffinBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        var opCode = instruction.opCode;
        var v1 = symbolTable.get(instruction.op1).getValue();
        var v2 = symbolTable.get(instruction.op2).getValue();
        var result = symbolTable.get(instruction.result).getValue();
        var dt = result.getDataType();

        switch (dt) {
            case INT32:
                result.setInt32(v1.getInt32() * v2.getInt32());
                break;
            case INT64:
                result.setInt64(v1.getInt64() * v2.getInt64());
                break;
            case FLOAT:
                result.setFloat32(v1.getFloat32() * v2.getFloat32());
                break;
            case DOUBLE:
                result.setFloat64(v1.getFloat64() * v2.getFloat64());
                break;
            default:
                throwIllegalDataTypeError(opCode, dt);
                break;
        }
    }

    public static void fdiv(
            PuffinBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        var opCode = instruction.opCode;
        var v1 = symbolTable.get(instruction.op1).getValue();
        var v2 = symbolTable.get(instruction.op2).getValue();
        var result = symbolTable.get(instruction.result).getValue();
        var dt = result.getDataType();

        switch (dt) {
            case INT32:
                if (v2.getInt32() == 0) {
                    throw new PuffinBasicRuntimeError(
                            DIVISION_BY_ZERO,
                            "Division by zero"
                    );
                }
                result.setInt32(v1.getInt32() / v2.getInt32());
                break;
            case INT64:
                if (v2.getInt64() == 0) {
                    throw new PuffinBasicRuntimeError(
                            DIVISION_BY_ZERO,
                            "Division by zero"
                    );
                }
                result.setInt64(v1.getInt64() / v2.getInt64());
                break;
            case FLOAT:
                if (v2.getFloat32() == 0) {
                    throw new PuffinBasicRuntimeError(
                            DIVISION_BY_ZERO,
                            "Division by zero"
                    );
                }
                result.setFloat32(v1.getFloat32() / v2.getFloat32());
                break;
            case DOUBLE:
                if (v2.getFloat64() == 0) {
                    throw new PuffinBasicRuntimeError(
                            DIVISION_BY_ZERO,
                            "Division by zero"
                    );
                }
                result.setFloat64(v1.getFloat64() / v2.getFloat64());
                break;
            default:
                throwIllegalDataTypeError(opCode, dt);
                break;
        }
    }

    public static void exp(
            PuffinBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        var opCode = instruction.opCode;
        var v1 = symbolTable.get(instruction.op1).getValue();
        var v2 = symbolTable.get(instruction.op2).getValue();
        var result = symbolTable.get(instruction.result).getValue();
        var dt = result.getDataType();

        switch (dt) {
            case INT32:
                result.setInt32((int) Math.pow(v1.getInt32(), v2.getInt32()));
                break;
            case INT64:
                result.setInt64((long) Math.pow(v1.getInt64(), v2.getInt64()));
                break;
            case FLOAT:
                result.setFloat32((float) Math.pow(v1.getFloat32(), v2.getFloat32()));
                break;
            case DOUBLE:
                result.setFloat64(Math.pow(v1.getFloat64(), v2.getFloat64()));
                break;
            default:
                throwIllegalDataTypeError(opCode, dt);
                break;
        }
    }

    public static void and(PuffinBasicSymbolTable symbolTable, Instruction instruction) {
        var v1 = symbolTable.get(instruction.op1).getValue().getInt64();
        var v2 = symbolTable.get(instruction.op2).getValue().getInt64();
        var result = symbolTable.get(instruction.result).getValue();

        if ((v1 == -1 || v1 == 0) && (v2 == -1 || v2 == 0)) {
            var b1 = v1 == -1;
            var b2 = v2 == -1;
            result.setInt64(b1 && b2 ? -1 : 0);
        } else {
            result.setInt64(v1 & v2);
        }
    }

    public static void or(PuffinBasicSymbolTable symbolTable, Instruction instruction) {
        var v1 = symbolTable.get(instruction.op1).getValue().getInt64();
        var v2 = symbolTable.get(instruction.op2).getValue().getInt64();
        var result = symbolTable.get(instruction.result).getValue();

        if ((v1 == -1 || v1 == 0) && (v2 == -1 || v2 == 0)) {
            var b1 = v1 == -1;
            var b2 = v2 == -1;
            result.setInt64(b1 || b2 ? -1 : 0);
        } else {
            result.setInt64(v1 | v2);
        }
    }

    public static void xor(PuffinBasicSymbolTable symbolTable, Instruction instruction) {
        var v1 = symbolTable.get(instruction.op1).getValue().getInt64();
        var v2 = symbolTable.get(instruction.op2).getValue().getInt64();
        var result = symbolTable.get(instruction.result).getValue();

        if ((v1 == -1 || v1 == 0) && (v2 == -1 || v2 == 0)) {
            var b1 = v1 == -1;
            var b2 = v2 == -1;
            result.setInt64(b1 ^ b2 ? -1 : 0);
        } else {
            result.setInt64(v1 ^ v2);
        }
    }

    public static void eqv(PuffinBasicSymbolTable symbolTable, Instruction instruction) {
        var v1 = symbolTable.get(instruction.op1).getValue().getInt64();
        var v2 = symbolTable.get(instruction.op2).getValue().getInt64();
        var result = symbolTable.get(instruction.result).getValue();

        if ((v1 == -1 || v1 == 0) && (v2 == -1 || v2 == 0)) {
            var b1 = v1 == -1;
            var b2 = v2 == -1;
            result.setInt64(b1 == b2 ? -1 : 0);
        } else {
            result.setInt64(~(v1 ^ v2));
        }
    }

    public static void imp(PuffinBasicSymbolTable symbolTable, Instruction instruction) {
        var v1 = symbolTable.get(instruction.op1).getValue().getInt64();
        var v2 = symbolTable.get(instruction.op2).getValue().getInt64();
        var result = symbolTable.get(instruction.result).getValue();

        if ((v1 == -1 || v1 == 0) && (v2 == -1 || v2 == 0)) {
            var b1 = v1 == -1;
            var b2 = v2 == -1;
            result.setInt64((!b1) || b2 ? -1 : 0);
        } else {
            result.setInt64((~v1) | v2);
        }
    }

    public static void lt(
            PuffinBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        var e1 = symbolTable.get(instruction.op1).getValue();
        var e2 = symbolTable.get(instruction.op2).getValue();
        var dt1 = e1.getDataType();
        var dt2 = e2.getDataType();
        var result = symbolTable.get(instruction.result).getValue();
        final long longResult;
        if (dt1 == STRING && dt2 == STRING) {
            longResult = e1.getString().compareTo(e2.getString()) < 0 ? -1 : 0;
        } else {
            if (dt1 == DOUBLE || dt2 == DOUBLE) {
                longResult = Double.compare(e1.getFloat64(), e2.getFloat64()) < 0 ? -1 : 0;
            } else if (dt1 == INT64 || dt2 == INT64) {
                longResult = e1.getInt64() < e2.getInt64() ? -1 : 0;
            } else if (dt1 == FLOAT || dt2 == FLOAT) {
                longResult = Float.compare(e1.getFloat32(), e2.getFloat32()) < 0 ? -1 : 0;
            } else {
                longResult = e1.getInt32() < e2.getInt32() ? -1 : 0;
            }
        }
        result.setInt64(longResult);
    }

    public static void le(
            PuffinBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        var e1 = symbolTable.get(instruction.op1).getValue();
        var e2 = symbolTable.get(instruction.op2).getValue();
        var dt1 = e1.getDataType();
        var dt2 = e2.getDataType();
        var result = symbolTable.get(instruction.result).getValue();
        final long longResult;
        if (dt1 == STRING && dt2 == STRING) {
            longResult = e1.getString().compareTo(e2.getString()) <= 0 ? -1 : 0;
        } else {
            if (dt1 == DOUBLE || dt2 == DOUBLE) {
                longResult = Double.compare(e1.getFloat64(), e2.getFloat64()) <= 0 ? -1 : 0;
            } else if (dt1 == INT64 || dt2 == INT64) {
                longResult = e1.getInt64() <= e2.getInt64() ? -1 : 0;
            } else if (dt1 == FLOAT || dt2 == FLOAT) {
                longResult = Float.compare(e1.getFloat32(), e2.getFloat32()) <= 0 ? -1 : 0;
            } else {
                longResult = e1.getInt32() <= e2.getInt32() ? -1 : 0;
            }
        }
        result.setInt64(longResult);
    }

    public static void gt(
            PuffinBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        var e1 = symbolTable.get(instruction.op1).getValue();
        var e2 = symbolTable.get(instruction.op2).getValue();
        var dt1 = e1.getDataType();
        var dt2 = e2.getDataType();
        var result = symbolTable.get(instruction.result).getValue();
        final long longResult;
        if (dt1 == STRING && dt2 == STRING) {
            longResult = e1.getString().compareTo(e2.getString()) > 0 ? -1 : 0;
        } else {
            if (dt1 == DOUBLE || dt2 == DOUBLE) {
                longResult = Double.compare(e1.getFloat64(), e2.getFloat64()) > 0 ? -1 : 0;
            } else if (dt1 == INT64 || dt2 == INT64) {
                longResult = e1.getInt64() > e2.getInt64() ? -1 : 0;
            } else if (dt1 == FLOAT || dt2 == FLOAT) {
                longResult = Float.compare(e1.getFloat32(), e2.getFloat32()) > 0 ? -1 : 0;
            } else {
                longResult = e1.getInt32() > e2.getInt32() ? -1 : 0;
            }
        }
        result.setInt64(longResult);
    }

    public static void ge(
            PuffinBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        var e1 = symbolTable.get(instruction.op1).getValue();
        var e2 = symbolTable.get(instruction.op2).getValue();
        var dt1 = e1.getDataType();
        var dt2 = e2.getDataType();
        var result = symbolTable.get(instruction.result).getValue();
        final long longResult;
        if (dt1 == STRING && dt2 == STRING) {
            longResult = e1.getString().compareTo(e2.getString()) > 0 ? -1 : 0;
        } else {
            if (dt1 == DOUBLE || dt2 == DOUBLE) {
                longResult = Double.compare(e1.getFloat64(), e2.getFloat64()) > 0 ? -1 : 0;
            } else if (dt1 == INT64 || dt2 == INT64) {
                longResult = e1.getInt64() > e2.getInt64() ? -1 : 0;
            } else if (dt1 == FLOAT || dt2 == FLOAT) {
                longResult = Float.compare(e1.getFloat32(), e2.getFloat32()) > 0 ? -1 : 0;
            } else {
                longResult = e1.getInt32() > e2.getInt32() ? -1 : 0;
            }
        }
        result.setInt64(longResult);
    }

    public static void eq(
            PuffinBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        var e1 = symbolTable.get(instruction.op1).getValue();
        var e2 = symbolTable.get(instruction.op2).getValue();
        var dt1 = e1.getDataType();
        var dt2 = e2.getDataType();
        var result = symbolTable.get(instruction.result).getValue();
        final long longResult;
        if (dt1 == STRING && dt2 == STRING) {
            longResult = e1.getString().equals(e2.getString()) ? -1 : 0;
        } else {
            if (dt1 == DOUBLE || dt2 == DOUBLE) {
                longResult = Double.compare(e1.getFloat64(), e2.getFloat64()) == 0 ? -1 : 0;
            } else if (dt1 == INT64 || dt2 == INT64) {
                longResult = e1.getInt64() == e2.getInt64() ? -1 : 0;
            } else if (dt1 == FLOAT || dt2 == FLOAT) {
                longResult = Float.compare(e1.getFloat32(), e2.getFloat32()) == 0 ? -1 : 0;
            } else {
                longResult = e1.getInt32() == e2.getInt32() ? -1 : 0;
            }
        }
        result.setInt64(longResult);
    }

    public static void ne(
            PuffinBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        var e1 = symbolTable.get(instruction.op1).getValue();
        var e2 = symbolTable.get(instruction.op2).getValue();
        var dt1 = e1.getDataType();
        var dt2 = e2.getDataType();
        var result = symbolTable.get(instruction.result).getValue();
        final long longResult;
        if (dt1 == STRING && dt2 == STRING) {
            longResult = !e1.getString().equals(e2.getString()) ? -1 : 0;
        } else {
            if (dt1 == DOUBLE || dt2 == DOUBLE) {
                longResult = Double.compare(e1.getFloat64(), e2.getFloat64()) != 0 ? -1 : 0;
            } else if (dt1 == INT64 || dt2 == INT64) {
                longResult = e1.getInt64() != e2.getInt64() ? -1 : 0;
            } else if (dt1 == FLOAT || dt2 == FLOAT) {
                longResult = Float.compare(e1.getFloat32(), e2.getFloat32()) != 0 ? -1 : 0;
            } else {
                longResult = e1.getInt32() != e2.getInt32() ? -1 : 0;
            }
        }
        result.setInt64(longResult);
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
