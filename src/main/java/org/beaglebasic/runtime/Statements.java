package org.beaglebasic.runtime;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.beaglebasic.domain.BeagleBasicSymbolTable;
import org.beaglebasic.domain.STObjects;
import org.beaglebasic.domain.STObjects.STEntry;
import org.beaglebasic.domain.STObjects.STVariable;
import org.beaglebasic.error.BeagleBasicInternalError;
import org.beaglebasic.error.BeagleBasicRuntimeError;
import org.beaglebasic.file.BeagleBasicFile;
import org.beaglebasic.file.BeagleBasicFile.FileAccessMode;
import org.beaglebasic.file.BeagleBasicFile.FileOpenMode;
import org.beaglebasic.file.BeagleBasicFile.LockMode;
import org.beaglebasic.file.BeagleBasicFiles;
import org.beaglebasic.parser.BeagleBasicIR.Instruction;
import org.beaglebasic.runtime.Formatter.FormatterCache;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.beaglebasic.domain.BeagleBasicSymbolTable.NULL_ID;
import static org.beaglebasic.domain.STObjects.BeagleBasicDataType.DOUBLE;
import static org.beaglebasic.domain.STObjects.BeagleBasicDataType.FLOAT;
import static org.beaglebasic.domain.STObjects.BeagleBasicDataType.INT32;
import static org.beaglebasic.domain.STObjects.BeagleBasicDataType.INT64;
import static org.beaglebasic.domain.STObjects.BeagleBasicDataType.STRING;
import static org.beaglebasic.error.BeagleBasicRuntimeError.ErrorCode.DATA_TYPE_MISMATCH;
import static org.beaglebasic.error.BeagleBasicRuntimeError.ErrorCode.INDEX_OUT_OF_BOUNDS;
import static org.beaglebasic.error.BeagleBasicRuntimeError.ErrorCode.IO_ERROR;
import static org.beaglebasic.error.BeagleBasicRuntimeError.ErrorCode.OUT_OF_DATA;

public class Statements {

    public static final class ReadData {
        private final List<STEntry> data;
        private int cursor;

        public ReadData(List<STEntry> data) {
            this.data = data;
        }

        public STEntry next() {
            if (cursor < data.size()) {
                return data.get(cursor++);
            } else {
                throw new BeagleBasicRuntimeError(
                        OUT_OF_DATA,
                        "Out of data!"
                );
            }
        }

        public void restore() {
            cursor = 0;
        }
    }

    public static void print(
            PrintBuffer printBuffer,
            BeagleBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        printBuffer.appendAtCursor(symbolTable.get(instruction.op1).getValue().printFormat());
    }

    public static void write(
            PrintBuffer printBuffer,
            BeagleBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        printBuffer.appendAtCursor(symbolTable.get(instruction.op1).getValue().writeFormat());
    }

    public static void printusing(
            FormatterCache cache,
            PrintBuffer printBuffer,
            BeagleBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        var format = symbolTable.get(instruction.op1).getValue().getString();
        var formatter = cache.get(format);
        var value = symbolTable.get(instruction.op2).getValue();
        final String result;
        switch (value.getDataType()) {
            case INT32:
            case INT64:
                if (!formatter.supportsNumeric()) {
                    throw new BeagleBasicRuntimeError(
                            DATA_TYPE_MISMATCH,
                            "String formatter doesn't work with numeric type: " + format
                    );
                }
                result = formatter.format(value.getInt64()) + " ";
                break;
            case FLOAT:
            case DOUBLE:
                if (!formatter.supportsNumeric()) {
                    throw new BeagleBasicRuntimeError(
                            DATA_TYPE_MISMATCH,
                            "String formatter doesn't work with numeric type: " + format
                    );
                }
                result = formatter.format(value.getFloat64()) + " ";
                break;
            case STRING:
                if (!formatter.supportsString()) {
                    throw new BeagleBasicRuntimeError(
                            DATA_TYPE_MISMATCH,
                            "Numeric formatter doesn't work with string type: " + format
                    );
                }
                result = formatter.format(value.getString());
                break;
            default:
                throw new BeagleBasicInternalError(
                        "Unsupported data type: " + value.getDataType()
                );
        }
        printBuffer.appendAtCursor(result);
    }

    public static void flush(
            BeagleBasicFiles files,
            PrintBuffer printBuffer,
            BeagleBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        if (instruction.op2 == NULL_ID) {
            printBuffer.flush(files.sys);
        } else {
            var fileNumber = symbolTable.get(instruction.op2).getValue().getInt32();
            printBuffer.flush(files.get(fileNumber));
        }
    }

    public static void swap(BeagleBasicSymbolTable symbolTable, Instruction instruction) {
        var op1 = symbolTable.get(instruction.op1).getValue();
        var op2 = symbolTable.get(instruction.op2).getValue();
        var dt1 = op1.getDataType();
        var dt2 = op2.getDataType();

        if (dt1 == STRING && dt2 == STRING) {
            var tmp = op1.getString();
            op1.setString(op2.getString());
            op2.setString(tmp);
        } else {
            if (dt1 == DOUBLE || dt2 == DOUBLE) {
                var tmp = op1.getFloat64();
                op1.setFloat64(op2.getFloat64());
                op2.setFloat64(tmp);
            } else if (dt1 == INT64 || dt2 == INT64) {
                var tmp = op1.getInt64();
                op1.setInt64(op2.getInt64());
                op2.setInt64(tmp);
            } else if (dt1 == FLOAT || dt2 == FLOAT) {
                var tmp = op1.getFloat32();
                op1.setFloat32(op2.getFloat32());
                op2.setFloat32(tmp);
            } else {
                var tmp = op1.getInt32();
                op1.setInt32(op2.getInt32());
                op2.setInt32(tmp);
            }
        }
    }

    public static void lset(BeagleBasicSymbolTable symbolTable, Instruction instruction) {
        var destEntry = symbolTable.get(instruction.op1).getValue();

        var value = symbolTable.get(instruction.op2).getValue().getString();
        var valLen = value.length();

        var destLen = destEntry.getFieldLength();
        if (destLen == 0) {
            destLen = destEntry.getString().length();
            destEntry.setFieldLength(destLen);
        }

        final String result;
        if (valLen > destLen) {
            result = value.substring(0, destLen);
        } else if (valLen == destLen) {
            result = value;
        } else {
            byte[] bytes = new byte[destLen];
            System.arraycopy(value.getBytes(), 0, bytes, 0, valLen);
            java.util.Arrays.fill(bytes, valLen, destLen, (byte) ' ');
            result = new String(bytes);
        }
        destEntry.setString(result);
    }

    public static void rset(BeagleBasicSymbolTable symbolTable, Instruction instruction) {
        var destEntry = symbolTable.get(instruction.op1).getValue();

        var value = symbolTable.get(instruction.op2).getValue().getString();
        var valLen = value.length();

        var destLen = destEntry.getFieldLength();
        if (destLen == 0) {
            destLen = destEntry.getString().length();
            destEntry.setFieldLength(destLen);
        }

        final String result;
        if (valLen > destLen) {
            result = value.substring(0, destLen);
        } else if (valLen == destLen) {
            result = value;
        } else {
            byte[] bytes = new byte[destLen];
            int offset = destLen - valLen;
            Arrays.fill(bytes, 0, offset, (byte) ' ');
            System.arraycopy(value.getBytes(), 0, bytes, offset, valLen);
            result = new String(bytes);
        }
        destEntry.setString(result);
    }

    public static void open(
            BeagleBasicFiles files,
            BeagleBasicSymbolTable symbolTable,
            Instruction instr_fn_fn_0,
            Instruction instr_om_am_1,
            Instruction instr_lm_rl_2)
    {
        var fileName = symbolTable.get(instr_fn_fn_0.op1).getValue().getString();
        var fileNumber = symbolTable.get(instr_fn_fn_0.op2).getValue().getInt32();
        var fileOpenMode = FileOpenMode.valueOf(
                symbolTable.get(instr_om_am_1.op1).getValue().getString()
        );
        var fileAccessMode = FileAccessMode.valueOf(
                symbolTable.get(instr_om_am_1.op2).getValue().getString()
        );
        var fileLockMode = LockMode.valueOf(
                symbolTable.get(instr_lm_rl_2.op1).getValue().getString()
        );
        var recordLen = symbolTable.get(instr_lm_rl_2.op2).getValue().getInt32();

        files.open(
                fileNumber,
                fileName,
                fileOpenMode,
                fileAccessMode,
                recordLen
        );
    }

    public static void closeAll(BeagleBasicFiles files) {
        files.closeAll();
    }

    public static void close(
            BeagleBasicFiles files,
            BeagleBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        var fileNumber = symbolTable.get(instruction.op1).getValue().getInt32();
        files.get(fileNumber).close();
    }

    public static void field(
            BeagleBasicFiles files,
            BeagleBasicSymbolTable symbolTable,
            List<Instruction> fields,
            Instruction instruction)
    {
        var varList = new IntArrayList(fields.size());
        for (var instrI : fields) {
            var recordPartLen = symbolTable.get(instrI.op2).getValue().getInt32();
            symbolTable.get(instrI.op1).getValue().setFieldLength(recordPartLen);
            varList.add(instrI.op1);
        }
        var fileNumber = symbolTable.get(instruction.op1).getValue().getInt32();
        files.get(fileNumber).setFieldParams(
                symbolTable,
                varList
        );
    }

    public static void putf(
            BeagleBasicFiles files,
            BeagleBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        var fileNumber = symbolTable.get(instruction.op1).getValue().getInt32();
        Integer recordNumber = instruction.op2 == NULL_ID
                ? null
                : symbolTable.get(instruction.op2).getValue().getInt32();
        files.get(fileNumber).put(recordNumber, symbolTable);
    }

    public static void getf(
            BeagleBasicFiles files,
            BeagleBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        var fileNumber = symbolTable.get(instruction.op1).getValue().getInt32();
        Integer recordNumber = instruction.op2 == NULL_ID
                ? null
                : symbolTable.get(instruction.op2).getValue().getInt32();
        files.get(fileNumber).get(recordNumber, symbolTable);
    }

    public static void randomize(
            Random random,
            BeagleBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        var seed = symbolTable.get(instruction.op1).getValue().getInt64();
        random.setSeed(seed);
    }

    public static void randomizeTimer(Random random) {
        var seed = Instant.now().getEpochSecond();
        random.setSeed(seed);
    }

    public static void defint(
            BeagleBasicSymbolTable symbolTable,
            Instruction instruction) {
        var letter = symbolTable.get(instruction.op1).getValue().getString();
        symbolTable.setDefaultDataType(letter.charAt(0), INT32);
    }

    public static void deflng(
            BeagleBasicSymbolTable symbolTable,
            Instruction instruction) {
        var letter = symbolTable.get(instruction.op1).getValue().getString();
        symbolTable.setDefaultDataType(letter.charAt(0), INT64);
    }

    public static void defsng(
            BeagleBasicSymbolTable symbolTable,
            Instruction instruction) {
        var letter = symbolTable.get(instruction.op1).getValue().getString();
        symbolTable.setDefaultDataType(letter.charAt(0), FLOAT);
    }

    public static void defdbl(
            BeagleBasicSymbolTable symbolTable,
            Instruction instruction) {
        var letter = symbolTable.get(instruction.op1).getValue().getString();
        symbolTable.setDefaultDataType(letter.charAt(0), DOUBLE);
    }

    public static void defstr(
            BeagleBasicSymbolTable symbolTable,
            Instruction instruction) {
        var letter = symbolTable.get(instruction.op1).getValue().getString();
        symbolTable.setDefaultDataType(letter.charAt(0), STRING);
    }

    public static void input(
            BeagleBasicFiles files,
            BeagleBasicSymbolTable symbolTable,
            List<Instruction> instructions,
            Instruction instruction)
    {
        if (instruction.op1 != NULL_ID) {
            var prompt = symbolTable.get(instruction.op1).getValue().getString();
            files.sys.print(prompt);
        }
        final BeagleBasicFile file;
        if (instruction.op2 != NULL_ID) {
            var fileNumber = symbolTable.get(instruction.op2).getValue().getInt32();
            file = files.get(fileNumber);
        } else {
            file = files.sys;
        }

        CSVRecord record;
        boolean retry = false;
        do {
            if (retry) {
                System.err.println("?Redo from start");
            }
            CSVParser parser;
            try {
                parser = CSVParser.parse(file.readLine(), CSVFormat.DEFAULT);
            } catch (IOException e) {
                throw new BeagleBasicRuntimeError(
                        IO_ERROR,
                        "Failed to read inputs, error: " + e.getMessage()
                );
            }
            record = parser.iterator().next();
            retry = true;
        } while (record.size() != instructions.size());

        int i = 0;
        for (var instr0 : instructions) {
            var value = symbolTable.get(instr0.op1).getValue();
            switch (value.getDataType()) {
                case INT32:
                    value.setInt32(Integer.parseInt(record.get(i).trim()));
                    break;
                case INT64:
                    value.setInt64(Long.parseLong(record.get(i).trim()));
                    break;
                case FLOAT:
                    value.setFloat32(Float.parseFloat(record.get(i).trim()));
                    break;
                case DOUBLE:
                    value.setFloat64(Double.parseDouble(record.get(i).trim()));
                    break;
                case STRING:
                    value.setString(record.get(i).trim());
                    break;
            }
            ++i;
        }
    }

    public static void lineinput(
            BeagleBasicFiles files,
            BeagleBasicSymbolTable symbolTable,
            Instruction instr0,
            Instruction instruction)
    {
        if (instruction.op1 != NULL_ID) {
            var prompt = symbolTable.get(instruction.op1).getValue().getString();
            if (!prompt.isEmpty()) {
                files.sys.print(prompt);
            }
        }
        final BeagleBasicFile file;
        if (instruction.op2 != NULL_ID) {
            var fileNumber = symbolTable.get(instruction.op2).getValue().getInt32();
            file = files.get(fileNumber);
        } else {
            file = files.sys;
        }
        symbolTable.get(instr0.op1).getValue().setString(file.readLine());
    }

    public static void middlr(
            BeagleBasicSymbolTable symbolTable,
            Instruction instr0,
            Instruction instr) {
        var dest = symbolTable.get(instr0.op1).getValue();
        var n = symbolTable.get(instr0.op2).getValue().getInt32();
        var m = symbolTable.get(instr.op1).getValue().getInt32();
        var replacement = symbolTable.get(instr.op2).getValue().getString();
        String varValue = dest.getString();
        var varlen = varValue.length();
        String result;
        if (n <= 0) {
            throw new BeagleBasicRuntimeError(
                    INDEX_OUT_OF_BOUNDS,
                    "INSTR: expected n > 0, actual=" + n
            );
        } else if (n > varlen) {
            result = varValue;
        } else {
            if (m == -1 || m > replacement.length()) {
                m = replacement.length();
            }
            result = varValue.substring(0, n - 1)
                    + replacement.substring(0, Math.min(m, varlen - n + 1))
                    + varValue.substring(Math.min(n + m - 1, varlen - 1));
        }
        dest.setString(result);
    }

    public static void read(
            ReadData readData,
            BeagleBasicSymbolTable symbolTable,
            Instruction instruction)
    {
        var variable = symbolTable.get(instruction.op1);
        var data = readData.next();
        Types.assertBothStringOrNumeric(variable.getValue().getDataType(),
                data.getValue().getDataType(),
                () -> "Read Data mismatch for variable: "
                        + (((STVariable) variable).getVariable())
                        + " and data: "
                        + data.getValue().printFormat()
        );
        variable.getValue().assign(data.getValue());
    }
}
