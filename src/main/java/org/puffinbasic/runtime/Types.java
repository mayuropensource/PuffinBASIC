package org.puffinbasic.runtime;

import org.puffinbasic.domain.PuffinBasicSymbolTable;
import org.puffinbasic.domain.STObjects.PuffinBasicAtomTypeId;
import org.puffinbasic.domain.STObjects.STVariable;
import org.puffinbasic.error.PuffinBasicSemanticError;

import java.util.function.Supplier;

import static org.puffinbasic.domain.STObjects.PuffinBasicAtomTypeId.STRING;
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
        dst.setValue(src.getValue());
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
            PuffinBasicAtomTypeId dt, Supplier<String> line
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
            PuffinBasicAtomTypeId dt, Supplier<String> line
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
            PuffinBasicAtomTypeId dt1, PuffinBasicAtomTypeId dt2, Supplier<String> line
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
            PuffinBasicAtomTypeId dt1, PuffinBasicAtomTypeId dt2, Supplier<String> line
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

    public static PuffinBasicAtomTypeId upcast(
            PuffinBasicAtomTypeId dt1 , PuffinBasicAtomTypeId dt2, Supplier<String> line
    ) {
        assertNumeric(dt1, dt2, line);
        if (dt1 == PuffinBasicAtomTypeId.DOUBLE || dt2 == PuffinBasicAtomTypeId.DOUBLE) {
            return PuffinBasicAtomTypeId.DOUBLE;
        } else if (dt1 == PuffinBasicAtomTypeId.INT64 || dt2 == PuffinBasicAtomTypeId.INT64) {
            return PuffinBasicAtomTypeId.INT64;
        } else if (dt1 == PuffinBasicAtomTypeId.FLOAT || dt2 == PuffinBasicAtomTypeId.FLOAT) {
            return PuffinBasicAtomTypeId.FLOAT;
        } else {
            return PuffinBasicAtomTypeId.INT32;
        }
    }
}
