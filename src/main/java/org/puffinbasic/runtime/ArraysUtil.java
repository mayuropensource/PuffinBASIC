package org.puffinbasic.runtime;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.puffinbasic.domain.PuffinBasicSymbolTable;
import org.puffinbasic.domain.STObjects.STFloat32ArrayValue;
import org.puffinbasic.domain.STObjects.STFloat64ArrayValue;
import org.puffinbasic.domain.STObjects.STInt32ArrayValue;
import org.puffinbasic.domain.STObjects.STInt64ArrayValue;
import org.puffinbasic.domain.STObjects.STStringArrayValue;
import org.puffinbasic.domain.STObjects.STValue;
import org.puffinbasic.error.PuffinBasicRuntimeError;
import org.puffinbasic.parser.PuffinBasicIR.Instruction;

import java.util.Arrays;

import static org.puffinbasic.error.PuffinBasicRuntimeError.ErrorCode.DATA_TYPE_MISMATCH;
import static org.puffinbasic.error.PuffinBasicRuntimeError.ErrorCode.ILLEGAL_FUNCTION_PARAM;
import static org.puffinbasic.runtime.Functions.throwUnsupportedType;

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
            default:
                throwUnsupportedType(fill.getDataType());
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
            default:
                throwUnsupportedType(array1.getDataType());
        }
    }

    static void array1dSort(PuffinBasicSymbolTable symbolTable, Instruction instruction) {
        var array = symbolTable.get(instruction.op1).getValue();

        switch (array.getDataType()) {
            case INT32:
                Arrays.sort(((STInt32ArrayValue) array).getValue());
                break;
            case INT64:
                Arrays.sort(((STInt64ArrayValue) array).getValue());
                break;
            case FLOAT:
                Arrays.sort(((STFloat32ArrayValue) array).getValue());
                break;
            case DOUBLE:
                Arrays.sort(((STFloat64ArrayValue) array).getValue());
                break;
            case STRING:
                Arrays.sort(((STStringArrayValue) array).getValue());
                break;
            default:
                throwUnsupportedType(array.getDataType());
        }
    }

    static void array1dBinSearch(PuffinBasicSymbolTable symbolTable, Instruction instruction) {
        var array = symbolTable.get(instruction.op1).getValue();
        var search = symbolTable.get(instruction.op2).getValue();
        var result = symbolTable.get(instruction.result).getValue();
        var index = -1;
        switch (array.getDataType()) {
            case INT32:
                index = Arrays.binarySearch(((STInt32ArrayValue) array).getValue(), search.getInt32());
                break;
            case INT64:
                index = Arrays.binarySearch(((STInt64ArrayValue) array).getValue(), search.getInt64());
                break;
            case FLOAT:
                index = Arrays.binarySearch(((STFloat32ArrayValue) array).getValue(), search.getFloat32());
                break;
            case DOUBLE:
                index = Arrays.binarySearch(((STFloat64ArrayValue) array).getValue(), search.getFloat64());
                break;
            case STRING:
                index = Arrays.binarySearch(((STStringArrayValue) array).getValue(), search.getString());
                break;
            default:
                throwUnsupportedType(array.getDataType());
        }
        result.setInt32(index);
    }

    static void array1dMean(PuffinBasicSymbolTable symbolTable, Instruction instruction) {
        var array = symbolTable.get(instruction.op1).getValue();
        var result = symbolTable.get(instruction.result).getValue();
        var stats = array1dSummaryStats(array);
        result.setFloat64(stats.getMean());
    }

    static void array1dStddev(PuffinBasicSymbolTable symbolTable, Instruction instruction) {
        var array = symbolTable.get(instruction.op1).getValue();
        var result = symbolTable.get(instruction.result).getValue();
        var stats = array1dSummaryStats(array);
        result.setFloat64(Math.sqrt(stats.getVariance()));
    }

    static void array1dSum(PuffinBasicSymbolTable symbolTable, Instruction instruction) {
        var array = symbolTable.get(instruction.op1).getValue();
        var result = symbolTable.get(instruction.result).getValue();
        var stats = array1dSummaryStats(array);
        result.setFloat64(stats.getSum());
    }

    static void array1dMedian(PuffinBasicSymbolTable symbolTable, Instruction instruction) {
        var array = symbolTable.get(instruction.op1).getValue();
        var result = symbolTable.get(instruction.result).getValue();
        var stats = array1dDescriptiveStats(array);
        result.setFloat64(stats.getPercentile(50));
    }

    static void array1dPercentile(PuffinBasicSymbolTable symbolTable, Instruction instruction) {
        var array = symbolTable.get(instruction.op1).getValue();
        var pct = symbolTable.get(instruction.op2).getValue().getFloat64();
        if (pct < 0 || pct > 100) {
            throw new PuffinBasicRuntimeError(
                    PuffinBasicRuntimeError.ErrorCode.DATA_OUT_OF_RANGE,
                    "Percentile value out of range: " + pct
            );
        }
        var result = symbolTable.get(instruction.result).getValue();
        var stats = array1dDescriptiveStats(array);
        result.setFloat64(stats.getPercentile(pct));
    }

    private static SummaryStatistics array1dSummaryStats(STValue array) {
        var stats = new SummaryStatistics();
        switch (array.getDataType()) {
            case INT32: {
                int[] value = ((STInt32ArrayValue) array).getValue();
                for (int v : value) {
                    stats.addValue(v);
                }
            }
            break;
            case INT64: {
                long[] value = ((STInt64ArrayValue) array).getValue();
                for (long v : value) {
                    stats.addValue(v);
                }
            }
            break;
            case FLOAT: {
                float[] value = ((STFloat32ArrayValue) array).getValue();
                for (float v : value) {
                    stats.addValue(v);
                }
            }
            break;
            case DOUBLE: {
                double[] value = ((STFloat64ArrayValue) array).getValue();
                for (double v : value) {
                    stats.addValue(v);
                }
            }
            break;
            default:
                throwUnsupportedType(array.getDataType());
        }
        return stats;
    }

    private static DescriptiveStatistics array1dDescriptiveStats(STValue array) {
        var stats = new DescriptiveStatistics();
        switch (array.getDataType()) {

            case INT32: {
                int[] value = ((STInt32ArrayValue) array).getValue();
                for (int v : value) {
                    stats.addValue(v);
                }
            }
            break;
            case INT64: {
                long[] value = ((STInt64ArrayValue) array).getValue();
                for (long v : value) {
                    stats.addValue(v);
                }
            }
            break;
            case FLOAT: {
                float[] value = ((STFloat32ArrayValue) array).getValue();
                for (float v : value) {
                    stats.addValue(v);
                }
            }
            break;
            case DOUBLE: {
                double[] value = ((STFloat64ArrayValue) array).getValue();
                for (double v : value) {
                    stats.addValue(v);
                }
            }
            break;
            default:
                throwUnsupportedType(array.getDataType());
        }
        return stats;

    }
}
