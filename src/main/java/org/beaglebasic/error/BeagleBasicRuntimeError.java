package org.beaglebasic.error;

import org.beaglebasic.parser.BeagleBasicIR.Instruction;

public class BeagleBasicRuntimeError extends RuntimeException {

    public enum ErrorCode {
        ARRAY_INDEX_OUT_OF_BOUNDS,
        INDEX_OUT_OF_BOUNDS,
        DIVISION_BY_ZERO,
        ILLEGAL_FUNCTION_PARAM,
        DATA_OUT_OF_RANGE,
        IO_ERROR,
        DATA_TYPE_MISMATCH,
        ILLEGAL_FILE_ACCESS,
        OUT_OF_DATA,
    }

    private final ErrorCode errorCode;

    public BeagleBasicRuntimeError(ErrorCode errorCode, String message) {
        super("[" + errorCode + "] " + message);
        this.errorCode = errorCode;
    }

    public BeagleBasicRuntimeError(
            BeagleBasicRuntimeError cause,
            Instruction instruction,
            String line)
    {
        super(cause.getMessage() + System.lineSeparator()
                + "Line: " + instruction.inputRef + System.lineSeparator()
                + line, cause);
        this.errorCode = cause.errorCode;
    }
}
