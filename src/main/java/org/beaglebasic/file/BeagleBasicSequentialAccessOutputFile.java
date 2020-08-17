package org.beaglebasic.file;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.IntList;
import org.beaglebasic.domain.BeagleBasicSymbolTable;
import org.beaglebasic.error.BeagleBasicRuntimeError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import static org.beaglebasic.error.BeagleBasicRuntimeError.ErrorCode.ILLEGAL_FILE_ACCESS;
import static org.beaglebasic.error.BeagleBasicRuntimeError.ErrorCode.IO_ERROR;

public class BeagleBasicSequentialAccessOutputFile implements BeagleBasicFile {

    private final String filename;
    private final PrintStream out;
    private long bytesAccessed;
    private BeagleBasicFile.FileState fileState;
    private String lastLine;

    public BeagleBasicSequentialAccessOutputFile(
            @NotNull String filename, boolean append)
    {
        Preconditions.checkNotNull(filename);

        this.filename = filename;
        this.bytesAccessed = 0;

        try {
            this.out = new PrintStream(new BufferedOutputStream(new FileOutputStream(filename, append)));
        } catch (FileNotFoundException e) {
            throw new BeagleBasicRuntimeError(
                    IO_ERROR,
                    "Failed to open file '" + filename + "' for writing, error: "
                            + e.getMessage()
            );
        }

        this.fileState = BeagleBasicFile.FileState.OPEN;
    }

    @Override
    public void setFieldParams(BeagleBasicSymbolTable symbolTable, IntList recordParts) {
        throw getIllegalAccess();
    }

    @Override
    public int getCurrentRecordNumber() {
        return (int) (bytesAccessed / BeagleBasicFile.DEFAULT_RECORD_LEN);
    }

    @Override
    public long getFileSizeInBytes() {
        return 0;
    }

    @Override
    public String readLine() {
        throw getIllegalAccess();
    }

    @Override
    public byte[] readBytes(int n) {
        throw getIllegalAccess();
    }

    @Override
    public void print(String s) {
        bytesAccessed += s.length();
        out.print(s);
    }

    @Override
    public void writeByte(byte b) {
        bytesAccessed++;
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
        throw getIllegalAccess();
    }

    @Override
    public void get(@Nullable Integer recordNumber, BeagleBasicSymbolTable symbolTable) {
        throw getIllegalAccess();
    }

    private BeagleBasicRuntimeError getIllegalAccess() {
        return new BeagleBasicRuntimeError(
                ILLEGAL_FILE_ACCESS,
                "Not implemented for SequentialAccessOutputFile!"
        );
    }

    @Override
    public boolean isOpen() {
        return fileState == BeagleBasicFile.FileState.OPEN;
    }

    @Override
    public void close() {
        assertOpen();
        try {
            this.out.close();
        } catch (Exception e) {
            throw new BeagleBasicRuntimeError(
                    IO_ERROR,
                    "Failed to close file '" + filename + "', error: " + e.getMessage()
            );
        }
        this.fileState = BeagleBasicFile.FileState.CLOSED;
    }

    private void assertOpen() {
        if (!isOpen()) {
            throw new BeagleBasicRuntimeError(
                    ILLEGAL_FILE_ACCESS,
                    "File " + filename + " is not open!"
            );
        }
    }
}
