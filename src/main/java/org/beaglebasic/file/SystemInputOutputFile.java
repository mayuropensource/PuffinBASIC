package org.beaglebasic.file;

import it.unimi.dsi.fastutil.ints.IntList;
import org.beaglebasic.domain.BeagleBasicSymbolTable;
import org.beaglebasic.error.BeagleBasicRuntimeError;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.beaglebasic.error.BeagleBasicRuntimeError.ErrorCode.ILLEGAL_FILE_ACCESS;
import static org.beaglebasic.error.BeagleBasicRuntimeError.ErrorCode.IO_ERROR;

public class SystemInputOutputFile implements BeagleBasicFile {

    private final BufferedReader in;
    private final PrintStream out;

    public SystemInputOutputFile(
            InputStream in,
            PrintStream out)
    {
        this.in = new BufferedReader(new InputStreamReader(in));
        this.out = out;
    }

    @Override
    public void setFieldParams(BeagleBasicSymbolTable symbolTable, IntList recordParts) {
        throw new BeagleBasicRuntimeError(
                ILLEGAL_FILE_ACCESS,
                "Not supported for System IN/OUT!"
        );
    }

    @Override
    public int getCurrentRecordNumber() {
        return 0;
    }

    @Override
    public long getFileSizeInBytes() {
        return 0;
    }

    @Override
    public byte[] readBytes(int n) {
        byte[] line = readLine().getBytes(StandardCharsets.US_ASCII);
        if (n >= line.length) {
            return line;
        } else {
            byte[] copy = new byte[Math.min(n, line.length)];
            System.arraycopy(line, 0, copy, 0, n);
            return copy;
        }
    }

    @Override
    public String readLine() {
        try {
            return in.readLine().stripTrailing();
        } catch (IOException e) {
            throw new BeagleBasicRuntimeError(
                    IO_ERROR,
                    "Failed to read line!"
            );
        }
    }

    @Override
    public void print(String s) {
        out.print(s);
    }

    @Override
    public void writeByte(byte b) {
        try {
            out.write((char) b);
        } catch (Exception e) {
            throw new BeagleBasicRuntimeError(
                    IO_ERROR,
                    "Failed to write buffer to output, error: " + e.getMessage()
            );
        }
    }

    @Override
    public boolean eof() {
        return false;
    }

    @Override
    public void put(@Nullable Integer recordNumber, BeagleBasicSymbolTable symbolTable) {
        throw new BeagleBasicRuntimeError(
                ILLEGAL_FILE_ACCESS,
                "Not supported for System IN/OUT!"
        );
    }

    @Override
    public void get(@Nullable Integer recordNumber, BeagleBasicSymbolTable symbolTable) {
        throw new BeagleBasicRuntimeError(
                ILLEGAL_FILE_ACCESS,
                "Not supported for System IN/OUT!"
        );
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void close() {
        throw new BeagleBasicRuntimeError(
                ILLEGAL_FILE_ACCESS,
                "Not supported for System IN/OUT!"
        );
    }
}
