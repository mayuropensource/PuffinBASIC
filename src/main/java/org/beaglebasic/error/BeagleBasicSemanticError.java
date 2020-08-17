package org.beaglebasic.error;

public class BeagleBasicSemanticError extends RuntimeException {

    public enum ErrorCode {
        ARRAY_VARIABLE_CANNOT_STARTWITH_FN,
        SCALAR_VARIABLE_CANNOT_BE_INDEXED,
        BAD_NUMBER,
        BAD_ASSIGNMENT,
        DATA_TYPE_MISMATCH,
        INSUFFICIENT_UDF_ARGS,
        WEND_WITHOUT_WHILE,
        WHILE_WITHOUT_WEND,
        NEXT_WITHOUT_FOR,
        FOR_WITHOUT_NEXT,
        BAD_ARGUMENT,
    }

    public BeagleBasicSemanticError(
            ErrorCode errorCode, String line, String message)
    {
        super(
                "[" + errorCode + "] " + message + System.lineSeparator() +
                "LINE:" + System.lineSeparator() +
                line
        );
    }
}
