package org.puffinbasic.runtime;

import org.puffinbasic.domain.PuffinBasicSymbolTable;
import org.puffinbasic.domain.STObjects.STFloat32ArrayValue;
import org.puffinbasic.domain.STObjects.STFloat64ArrayValue;
import org.puffinbasic.domain.STObjects.STInt32ArrayValue;
import org.puffinbasic.domain.STObjects.STInt64ArrayValue;
import org.puffinbasic.domain.STObjects.STStringArrayValue;
import org.puffinbasic.error.PuffinBasicRuntimeError;
import org.puffinbasic.parser.PuffinBasicIR.Instruction;

import static org.puffinbasic.error.PuffinBasicRuntimeError.ErrorCode.DATA_TYPE_MISMATCH;
import static org.puffinbasic.error.PuffinBasicRuntimeError.ErrorCode.ILLEGAL_FUNCTION_PARAM;

final class ArraysUtil {

    static final class ArrayState {
        private int dimIndex;

        int getAndIncrement() {
            return dimIndex++;
        }

        void reset() {
            dimIndex = 0;
        }

    }

    static void resetIndex(ArrayState state, PuffinBasicSymbolTable symbolTable, Instruction instruction) {
        state.reset();
        symbolTable.get(instruction.op1).getValue().resetArrayIndex();
    }

    static void setIndex(ArrayState state, PuffinBasicSymbolTable symbolTable, Instruction instruction) {
        int index = symbolTable.get(instruction.op2).getValue().getInt32();
        symbolTable.get(instruction.op1).getValue().setArrayIndex(state.getAndIncrement(), index);
    }

    static void arrayref(PuffinBasicSymbolTable symbolTable, Instruction instruction) {
        var index = symbolTable.get(instruction.op1).getValue().getArrayIndex1D();
        symbolTable.get(instruction.result).getValue().setArrayReferenceIndex1D(index);
    }

    static void arrayfill(PuffinBasicSymbolTable symbolTable, Instruction instruction) {
        var array = symbolTable.get(instruction.op1).getValue();
        var fill = symbolTable.get(instruction.op2).getValue();

        switch (fill.getDataType()) {
            case INT32:
                array.fill(fill.getInt32());
                break;
            case INT64:
                array.fill(fill.getInt64());
                break;
            case FLOAT:
                array.fill(fill.getFloat32());
                break;
            case DOUBLE:
                array.fill(fill.getFloat64());
                break;
            case STRING:
                array.fillString(fill.getString());
                break;
        }
    }

    static void arraycopy(PuffinBasicSymbolTable symbolTable, Instruction instruction) {
        var array1 = symbolTable.get(instruction.op1).getValue();
        var array2 = symbolTable.get(instruction.op2).getValue();
        if (array1.getDataType() != array2.getDataType()) {
            throw new PuffinBasicRuntimeError(
                    DATA_TYPE_MISMATCH,
                    "Array data type mismatch: " + array1.getDataType()
                            + " is not compatible with " + array2.getDataType()
            );
        }
        if (array1.getTotalLength() != array2.getTotalLength()) {
            throw new PuffinBasicRuntimeError(
                    ILLEGAL_FUNCTION_PARAM,
                    "Array length mismatch: " + array1.getTotalLength()
                            + " is not compatible with " + array2.getTotalLength()
            );
        }

        switch (array1.getDataType()) {
            case INT32: {
                int[] value = ((STInt32ArrayValue) array1).getValue();
                System.arraycopy(value, 0, ((STInt32ArrayValue) array2).getValue(), 0, value.length);
            }
                break;
            case INT64: {
                long[] value = ((STInt64ArrayValue) array1).getValue();
                System.arraycopy(
                        value, 0, ((STInt64ArrayValue) array2).getValue(), 0, value.length
                );
            }
                break;
            case FLOAT: {
                float[] value = ((STFloat32ArrayValue) array1).getValue();
                System.arraycopy(
                        value, 0, ((STFloat32ArrayValue) array2).getValue(), 0, value.length
                );
            }
                break;
            case DOUBLE: {
                double[] value = ((STFloat64ArrayValue) array1).getValue();
                System.arraycopy(
                        value, 0, ((STFloat64ArrayValue) array2).getValue(), 0, value.length
                );
            }
                break;
            case STRING: {
                String[] value = ((STStringArrayValue) array1).getValue();
                System.arraycopy(
                        value, 0, ((STStringArrayValue) array2).getValue(), 0, value.length
                );
            }
                break;
        }
    }
}
