package org.puffinbasic.runtime;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
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
import java.util.List;

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

    static void dim(PuffinBasicSymbolTable symbolTable, List<Instruction> params, Instruction instruction) {
        IntList dims = new IntArrayList(params.size());
        for (var param : params) {
            dims.add(symbolTable.get(param.op1).getValue().getInt32());
        }
        symbolTable.get(instruction.op1).getValue().setArrayDimensions(dims);
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

    static void arrayCopy(PuffinBasicSymbolTable symbolTable, Instruction instruction) {
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

    static void array2dShiftVertical(
            PuffinBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        var array = symbolTable.get(instruction.op1).getValue();
        var shift = symbolTable.get(instruction.op2).getValue().getInt32();
        var dims = array.getArrayDimensions();
        // Arrays are row-major.
        var dim1 = dims.getInt(0);
        var dim2 = dims.getInt(1);
        var n = array.getTotalLength();
        var delta = (Math.abs(shift) % dim1) * dim2;
        int src0, dst0, len = n - delta, fillSrc0;
        if (shift > 0) {
            src0 = 0;
            dst0 = delta;
            fillSrc0 = 0;
        } else {
            src0 = delta;
            dst0 = 0;
            fillSrc0 = n - delta;
        }

        switch (array.getDataType()) {
            case INT32: {
                int[] value = ((STInt32ArrayValue) array).getValue();
                System.arraycopy(value, src0, value, dst0, len);
                Arrays.fill(value, fillSrc0, fillSrc0 + delta, 0);
            }
            break;
            case INT64: {
                long[] value = ((STInt64ArrayValue) array).getValue();
                System.arraycopy(value, src0, value, dst0, len);
                Arrays.fill(value, fillSrc0, fillSrc0 + delta, 0);
            }
            break;
            case FLOAT: {
                float[] value = ((STFloat32ArrayValue) array).getValue();
                System.arraycopy(value, src0, value, dst0, len);
                Arrays.fill(value, fillSrc0, fillSrc0 + delta, 0);
            }
            break;
            case DOUBLE: {
                double[] value = ((STFloat64ArrayValue) array).getValue();
                System.arraycopy(value, src0, value, dst0, len);
                Arrays.fill(value, fillSrc0, fillSrc0 + delta, 0);
            }
            break;
            case STRING: {
                String[] value = ((STStringArrayValue) array).getValue();
                System.arraycopy(value, src0, value, dst0, len);
                Arrays.fill(value, fillSrc0, fillSrc0 + delta, "");
            }
            break;
            default:
                throwUnsupportedType(array.getDataType());
        }
    }

    static void array2dShiftHorizontal(
            PuffinBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        var array = symbolTable.get(instruction.op1).getValue();
        var shift = symbolTable.get(instruction.op2).getValue().getInt32();
        var dims = array.getArrayDimensions();
        // Arrays are row-major.
        var dim1 = dims.getInt(0);
        var dim2 = dims.getInt(1);
        var n = array.getTotalLength();
        var delta = Math.abs(shift) % dim2;
        int src0, dst0, len = dim2 - delta, fillSrc0;
        if (shift > 0) {
            src0 = 0;
            dst0 = delta;
            fillSrc0 = 0;
        } else {
            src0 = delta;
            dst0 = 0;
            fillSrc0 = dim2 - delta;
        }

        switch (array.getDataType()) {
            case INT32: {
                int[] value = ((STInt32ArrayValue) array).getValue();
                if (shift >= 0) {
                    for (int dc = dst0 + len - 1, sc = src0 + len - 1; dc >= dst0; dc--, sc--) {
                        for (int r = 0; r < dim1; r++) {
                            int dr = r * dim2;
                            value[dr + dc] = value[dr + sc];
                        }
                    }
                } else {
                    for (int dc = dst0, sc = src0; dc < dst0 + len; dc++, sc++) {
                        for (int r = 0; r < dim1; r++) {
                            int dr = r * dim2;
                            value[dr + dc] = value[dr + sc];
                        }
                    }
                }
                for (int c = fillSrc0; c < fillSrc0 + delta; c++) {
                    for (int r = 0; r < dim1; r++) {
                        value[r * dim2 + c] = 0;
                    }
                }
            }
            break;
            case INT64: {
                long[] value = ((STInt64ArrayValue) array).getValue();
                if (shift >= 0) {
                    for (int dc = dst0 + len - 1, sc = src0 + len - 1; dc >= dst0; dc--, sc--) {
                        for (int r = 0; r < dim1; r++) {
                            int dr = r * dim2;
                            value[dr + dc] = value[dr + sc];
                        }
                    }
                } else {
                    for (int dc = dst0, sc = src0; dc < dst0 + len; dc++, sc++) {
                        for (int r = 0; r < dim1; r++) {
                            int dr = r * dim2;
                            value[dr + dc] = value[dr + sc];
                        }
                    }
                }
                for (int c = fillSrc0; c < fillSrc0 + delta; c++) {
                    for (int r = 0; r < dim1; r++) {
                        value[r * dim2 + c] = 0;
                    }
                }
            }
            break;
            case FLOAT: {
                float[] value = ((STFloat32ArrayValue) array).getValue();
                if (shift >= 0) {
                    for (int dc = dst0 + len - 1, sc = src0 + len - 1; dc >= dst0; dc--, sc--) {
                        for (int r = 0; r < dim1; r++) {
                            int dr = r * dim2;
                            value[dr + dc] = value[dr + sc];
                        }
                    }
                } else {
                    for (int dc = dst0, sc = src0; dc < dst0 + len; dc++, sc++) {
                        for (int r = 0; r < dim1; r++) {
                            int dr = r * dim2;
                            value[dr + dc] = value[dr + sc];
                        }
                    }
                }
                for (int c = fillSrc0; c < fillSrc0 + delta; c++) {
                    for (int r = 0; r < dim1; r++) {
                        value[r * dim2 + c] = 0;
                    }
                }
            }
            break;
            case DOUBLE: {
                double[] value = ((STFloat64ArrayValue) array).getValue();
                if (shift >= 0) {
                    for (int dc = dst0 + len - 1, sc = src0 + len - 1; dc >= dst0; dc--, sc--) {
                        for (int r = 0; r < dim1; r++) {
                            int dr = r * dim2;
                            value[dr + dc] = value[dr + sc];
                        }
                    }
                } else {
                    for (int dc = dst0, sc = src0; dc < dst0 + len; dc++, sc++) {
                        for (int r = 0; r < dim1; r++) {
                            int dr = r * dim2;
                            value[dr + dc] = value[dr + sc];
                        }
                    }
                }
                for (int c = fillSrc0; c < fillSrc0 + delta; c++) {
                    for (int r = 0; r < dim1; r++) {
                        value[r * dim2 + c] = 0;
                    }
                }
            }
            break;
            case STRING: {
                String[] value = ((STStringArrayValue) array).getValue();
                if (shift >= 0) {
                    for (int dc = dst0 + len - 1, sc = src0 + len - 1; dc >= dst0; dc--, sc--) {
                        for (int r = 0; r < dim1; r++) {
                            int dr = r * dim2;
                            value[dr + dc] = value[dr + sc];
                        }
                    }
                } else {
                    for (int dc = dst0, sc = src0; dc < dst0 + len; dc++, sc++) {
                        for (int r = 0; r < dim1; r++) {
                            int dr = r * dim2;
                            value[dr + dc] = value[dr + sc];
                        }
                    }
                }
                for (int c = fillSrc0; c < fillSrc0 + delta; c++) {
                    for (int r = 0; r < dim1; r++) {
                        value[r * dim2 + c] = "";
                    }
                }
            }
            break;
            default:
                throwUnsupportedType(array.getDataType());
        }
    }

    static void array1DCopy(
            PuffinBasicSymbolTable symbolTable,
            Instruction i0,
            Instruction i1,
            Instruction instruction)
    {
        var src = symbolTable.get(i0.op1).getValue();
        var src0 = symbolTable.get(i0.op2).getValue().getInt32();
        var dst = symbolTable.get(i1.op1).getValue();
        var dst0 = symbolTable.get(i1.op2).getValue().getInt32();
        var len = symbolTable.get(instruction.op1).getValue().getInt32();
        if (src.getDataType() != dst.getDataType()) {
            throw new PuffinBasicRuntimeError(
                    DATA_TYPE_MISMATCH,
                    "Array data type mismatch: " + src.getDataType()
                            + " is not compatible with " + dst.getDataType()
            );
        }
        if (src.getNumArrayDimensions() != 1 && dst.getNumArrayDimensions() != 1) {
            throw new PuffinBasicRuntimeError(
                    ILLEGAL_FUNCTION_PARAM,
                    "Array #dim!=1 : src=" + src.getNumArrayDimensions()
                            + " and dst=" + dst.getNumArrayDimensions()
            );
        }
        if (src0 < 0 || src0 >= src.getTotalLength()|| dst0 < 0 || len < 0 || dst0 + len > dst.getTotalLength()) {
            throw new PuffinBasicRuntimeError(
                    ILLEGAL_FUNCTION_PARAM,
                    "Bad params: srcOrigin=" + src0
                            + " dstOrigin=" + dst0
                            + " len=" + len
                            + " srcArraySize=" + src.getTotalLength()
                            + " dstArraySize=" + dst.getTotalLength()
            );
        }

        switch (src.getDataType()) {
            case INT32: {
                int[] value = ((STInt32ArrayValue) src).getValue();
                System.arraycopy(value, src0, ((STInt32ArrayValue) dst).getValue(), dst0, len);
            }
            break;
            case INT64: {
                long[] value = ((STInt64ArrayValue) src).getValue();
                System.arraycopy(
                        value, src0, ((STInt64ArrayValue) dst).getValue(), dst0, len
                );
            }
            break;
            case FLOAT: {
                float[] value = ((STFloat32ArrayValue) src).getValue();
                System.arraycopy(
                        value, src0, ((STFloat32ArrayValue) dst).getValue(), dst0, len
                );
            }
            break;
            case DOUBLE: {
                double[] value = ((STFloat64ArrayValue) src).getValue();
                System.arraycopy(
                        value, src0, ((STFloat64ArrayValue) dst).getValue(), dst0, len
                );
            }
            break;
            case STRING: {
                String[] value = ((STStringArrayValue) src).getValue();
                System.arraycopy(
                        value, src0, ((STStringArrayValue) dst).getValue(), dst0, len
                );
            }
            break;
            default:
                throwUnsupportedType(src.getDataType());
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

    static void array1dMin(PuffinBasicSymbolTable symbolTable, Instruction instruction) {
        var array = symbolTable.get(instruction.op1).getValue();
        var result = symbolTable.get(instruction.result).getValue();
        switch (array.getDataType()) {
            case INT32: {
                int[] value = ((STInt32ArrayValue) array).getValue();
                var min = Integer.MAX_VALUE;
                for (var v : value) {
                    if (v < min) {
                        min = v;
                    }
                }
                result.setInt32(min);
            }
                break;
            case INT64: {
                long[] value = ((STInt64ArrayValue) array).getValue();
                var min = Long.MAX_VALUE;
                for (var v : value) {
                    if (v < min) {
                        min = v;
                    }
                }
                result.setInt64(min);
            }
                break;
            case FLOAT: {
                float[] value = ((STFloat32ArrayValue) array).getValue();
                var min = Float.MAX_VALUE;
                for (var v : value) {
                    if (v < min) {
                        min = v;
                    }
                }
                result.setFloat32(min);
            }
                break;
            case DOUBLE: {
                double[] value = ((STFloat64ArrayValue) array).getValue();
                var min = Double.MAX_VALUE;
                for (var v : value) {
                    if (v < min) {
                        min = v;
                    }
                }
                result.setFloat64(min);
            }
                break;
            default:
                throwUnsupportedType(array.getDataType());
        }
    }

    static void array1dMax(PuffinBasicSymbolTable symbolTable, Instruction instruction) {
        var array = symbolTable.get(instruction.op1).getValue();
        var result = symbolTable.get(instruction.result).getValue();
        switch (array.getDataType()) {
            case INT32: {
                int[] value = ((STInt32ArrayValue) array).getValue();
                var max = Integer.MIN_VALUE;
                for (var v : value) {
                    if (v > max) {
                        max = v;
                    }
                }
                result.setInt32(max);
            }
            break;
            case INT64: {
                long[] value = ((STInt64ArrayValue) array).getValue();
                var max = Long.MIN_VALUE;
                for (var v : value) {
                    if (v > max) {
                        max = v;
                    }
                }
                result.setInt64(max);
            }
            break;
            case FLOAT: {
                float[] value = ((STFloat32ArrayValue) array).getValue();
                var max = Float.MIN_VALUE;
                for (var v : value) {
                    if (v > max) {
                        max = v;
                    }
                }
                result.setFloat32(max);
            }
            break;
            case DOUBLE: {
                double[] value = ((STFloat64ArrayValue) array).getValue();
                var max = Double.MIN_VALUE;
                for (var v : value) {
                    if (v > max) {
                        max = v;
                    }
                }
                result.setFloat64(max);
            }
            break;
            default:
                throwUnsupportedType(array.getDataType());
        }
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
