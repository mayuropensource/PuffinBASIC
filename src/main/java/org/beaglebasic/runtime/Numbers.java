package org.beaglebasic.runtime;

import org.beaglebasic.error.BeagleBasicSemanticError;

import java.util.function.Supplier;

import static org.beaglebasic.error.BeagleBasicSemanticError.ErrorCode.BAD_NUMBER;

public class Numbers {

    public static int parseInt32(String value, Supplier<String> lineSupplier) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new BeagleBasicSemanticError(
                    BAD_NUMBER,
                    lineSupplier.get(),
                    "Failed to parse number as int32: " + value
            );
        }
    }

    public static int parseInt32(String value, int base, Supplier<String> lineSupplier) {
        try {
            return Integer.parseInt(value, base);
        } catch (NumberFormatException e) {
            throw new BeagleBasicSemanticError(
                    BAD_NUMBER,
                    lineSupplier.get(),
                    "Failed to parse number as int32: " + value
            );
        }
    }

    public static long parseInt64(String value, Supplier<String> lineSupplier) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new BeagleBasicSemanticError(
                    BAD_NUMBER,
                    lineSupplier.get(),
                    "Failed to parse number as int64: " + value
            );
        }
    }

    public static long parseInt64(String value, int base, Supplier<String> lineSupplier) {
        try {
            return Long.parseLong(value, base);
        } catch (NumberFormatException e) {
            throw new BeagleBasicSemanticError(
                    BAD_NUMBER,
                    lineSupplier.get(),
                    "Failed to parse number as int64: " + value
            );
        }
    }

    public static float parseFloat32(String value, Supplier<String> lineSupplier) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            throw new BeagleBasicSemanticError(
                    BAD_NUMBER,
                    lineSupplier.get(),
                    "Failed to parse number as float32: " + value
            );
        }
    }

    public static double parseFloat64(String value, Supplier<String> lineSupplier) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new BeagleBasicSemanticError(
                    BAD_NUMBER,
                    lineSupplier.get(),
                    "Failed to parse number as float64: " + value
            );
        }
    }
}
