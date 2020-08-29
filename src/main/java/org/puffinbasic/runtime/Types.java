package org.puffinbasic.runtime;

import org.puffinbasic.domain.PuffinBasicSymbolTable;
import org.puffinbasic.domain.STObjects.PuffinBasicDataType;
import org.puffinbasic.domain.STObjects.STVariable;
import org.puffinbasic.error.PuffinBasicSemanticError;

import java.util.function.Supplier;

import static org.puffinbasic.domain.STObjects.PuffinBasicDataType.STRING;
import static org.puffinbasic.error.PuffinBasicSemanticError.ErrorCode.DATA_TYPE_MISMATCH;

public class Types {

    public static void copy(
            PuffinBasicSymbolTable symbolTable,
            int to,
            int from)
    {
        var toEntry = symbolTable.get(to);
        var fromEntry = symbolTable.get(from);
        toEntry.getValue().assign(fromEntry.getValue());
    }

    public static void varref(PuffinBasicSymbolTable symbolTable, int op1, int op2) {
        var src = (STVariable) symbolTable.get(op1);
        var dst = (STVariable) symbolTable.get(op2);
        dst.setValue(src);
    }

    public static String unquote(String txt) {
        if (txt == null || txt.isEmpty()) {
            return txt;
        } else {
            if (txt.length() > 1 && txt.charAt(0) == '"' && txt.charAt(txt.length() - 1) == '"') {
                return txt.substring(1, txt.length() - 1);
            } else {
                return "";
            }
        }
    }

    public static void assertString(
            PuffinBasicDataType dt, Supplier<String> line
    ) {
        if (dt != STRING) {
            throw new PuffinBasicSemanticError(
                    DATA_TYPE_MISMATCH,
                    line.get(),
                    "Expected String type but found: " + dt
            );
        }
    }

    public static void assertNumeric(
            PuffinBasicDataType dt, Supplier<String> line
    ) {
        if (dt == STRING) {
            throw new PuffinBasicSemanticError(
                    DATA_TYPE_MISMATCH,
                    line.get(),
                    "Expected numeric type but found String!"
            );
        }
    }

    public static void assertNumeric(
            PuffinBasicDataType dt1, PuffinBasicDataType dt2, Supplier<String> line
    ) {
        if (dt1 == STRING || dt2 == STRING) {
            throw new PuffinBasicSemanticError(
                    DATA_TYPE_MISMATCH,
                    line.get(),
                    "Expected numeric type but found String!"
            );
        }
    }

    public static void assertBothStringOrNumeric(
            PuffinBasicDataType dt1, PuffinBasicDataType dt2, Supplier<String> line
    ) {
        if ((dt1 != STRING || dt2 != STRING)
                && (dt1 == STRING || dt2 == STRING))
        {
                    throw new PuffinBasicSemanticError(
                            DATA_TYPE_MISMATCH,
                            line.get(),
                            "Expected either both numeric or both string type but found: "
                                + dt1 + " and " + dt2
                    );
                }
    }

    public static PuffinBasicDataType upcast(
            PuffinBasicDataType dt1 , PuffinBasicDataType dt2, Supplier<String> line
    ) {
        assertNumeric(dt1, dt2, line);
        if (dt1 == PuffinBasicDataType.DOUBLE || dt2 == PuffinBasicDataType.DOUBLE) {
            return PuffinBasicDataType.DOUBLE;
        } else if (dt1 == PuffinBasicDataType.INT64 || dt2 == PuffinBasicDataType.INT64) {
            return PuffinBasicDataType.INT64;
        } else if (dt1 == PuffinBasicDataType.FLOAT || dt2 == PuffinBasicDataType.FLOAT) {
            return PuffinBasicDataType.FLOAT;
        } else {
            return PuffinBasicDataType.INT32;
        }
    }
}
