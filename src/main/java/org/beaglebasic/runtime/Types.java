package org.beaglebasic.runtime;

import org.beaglebasic.domain.BeagleBasicSymbolTable;
import org.beaglebasic.error.BeagleBasicSemanticError;
import org.beaglebasic.domain.STObjects.BeagleBasicDataType;

import java.util.function.Supplier;

import static org.beaglebasic.domain.STObjects.BeagleBasicDataType.STRING;
import static org.beaglebasic.error.BeagleBasicSemanticError.ErrorCode.DATA_TYPE_MISMATCH;

public class Types {

    public static void copy(
            BeagleBasicSymbolTable symbolTable,
            int to,
            int from)
    {
        var toEntry = symbolTable.get(to);
        var fromEntry = symbolTable.get(from);
        toEntry.getValue().assign(fromEntry.getValue());
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
            BeagleBasicDataType dt, Supplier<String> line
    ) {
        if (dt != STRING) {
            throw new BeagleBasicSemanticError(
                    DATA_TYPE_MISMATCH,
                    line.get(),
                    "Expected String type but found: " + dt
            );
        }
    }

    public static void assertNumeric(
            BeagleBasicDataType dt, Supplier<String> line
    ) {
        if (dt == STRING) {
            throw new BeagleBasicSemanticError(
                    DATA_TYPE_MISMATCH,
                    line.get(),
                    "Expected numeric type but found String!"
            );
        }
    }

    public static void assertNumeric(
            BeagleBasicDataType dt1, BeagleBasicDataType dt2, Supplier<String> line
    ) {
        if (dt1 == STRING || dt2 == STRING) {
            throw new BeagleBasicSemanticError(
                    DATA_TYPE_MISMATCH,
                    line.get(),
                    "Expected numeric type but found String!"
            );
        }
    }

    public static void assertBothStringOrNumeric(
            BeagleBasicDataType dt1, BeagleBasicDataType dt2, Supplier<String> line
    ) {
        if ((dt1 != STRING || dt2 != STRING)
                && (dt1 == STRING || dt2 == STRING))
        {
                    throw new BeagleBasicSemanticError(
                            DATA_TYPE_MISMATCH,
                            line.get(),
                            "Expected either both numeric or both string type but found: "
                                + dt1 + " and " + dt2
                    );
                }
    }

    public static BeagleBasicDataType upcast(
            BeagleBasicDataType dt1 , BeagleBasicDataType dt2, Supplier<String> line
    ) {
        assertNumeric(dt1, dt2, line);
        if (dt1 == BeagleBasicDataType.DOUBLE || dt2 == BeagleBasicDataType.DOUBLE) {
            return BeagleBasicDataType.DOUBLE;
        } else if (dt1 == BeagleBasicDataType.INT64 || dt2 == BeagleBasicDataType.INT64) {
            return BeagleBasicDataType.INT64;
        } else if (dt1 == BeagleBasicDataType.FLOAT || dt2 == BeagleBasicDataType.FLOAT) {
            return BeagleBasicDataType.FLOAT;
        } else {
            return BeagleBasicDataType.INT32;
        }
    }
}
