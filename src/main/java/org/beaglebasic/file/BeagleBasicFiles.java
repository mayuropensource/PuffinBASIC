package org.beaglebasic.file;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.beaglebasic.error.BeagleBasicRuntimeError;
import org.beaglebasic.file.BeagleBasicFile.FileAccessMode;
import org.beaglebasic.file.BeagleBasicFile.FileOpenMode;

import static org.beaglebasic.error.BeagleBasicRuntimeError.ErrorCode.ILLEGAL_FILE_ACCESS;

public class BeagleBasicFiles {

    public final BeagleBasicFile sys;
    private final Int2ObjectMap<BeagleBasicFile> files;

    public BeagleBasicFiles(BeagleBasicFile sys) {
        this.files = new Int2ObjectOpenHashMap<>();
        this.sys = sys;
    }

    public BeagleBasicFile open(
            int fileNumber,
            String filename,
            FileOpenMode openMode,
            FileAccessMode accessMode,
            int recordLen)
    {
        assertPositiveFileNumber(fileNumber);
        BeagleBasicFile file;
        if (openMode == FileOpenMode.RANDOM) {
            file = new BeagleBasicRandomAccessFile(
                    filename,
                    accessMode,
                    recordLen
            );
        } else if (openMode == FileOpenMode.INPUT) {
            file = new BeagleBasicSequentialAccessInputFile(filename);
        } else if (openMode == FileOpenMode.OUTPUT) {
            file = new BeagleBasicSequentialAccessOutputFile(filename, false);
        } else {
            file = new BeagleBasicSequentialAccessOutputFile(filename, true);
        }

        var existing = files.get(fileNumber);
        if (existing != null && existing.isOpen()) {
            throw new BeagleBasicRuntimeError(
                    ILLEGAL_FILE_ACCESS,
                    "FileNumber: " + fileNumber
                            + " is already open, cannot open another file: "
                            + filename + " with same file number."
            );
        }

        files.put(fileNumber, file);
        return file;
    }

    private void assertPositiveFileNumber(int fileNumber) {
        if (fileNumber < 0) {
            throw new BeagleBasicRuntimeError(
                    BeagleBasicRuntimeError.ErrorCode.ILLEGAL_FUNCTION_PARAM,
                    "File number: " + fileNumber + " cannot be negative"
            );
        }
    }

    public BeagleBasicFile get(int fileNumber) {
        assertPositiveFileNumber(fileNumber);
        var file = files.get(fileNumber);
        if (file == null) {
            throw new BeagleBasicRuntimeError(
                    ILLEGAL_FILE_ACCESS,
                    "Failed to find file for fileNumber: " + fileNumber
            );
        }
        return file;
    }

    public void closeAll() {
        for (var file : files.values()) {
            if (file.isOpen()) {
                file.close();
            }
        }
    }
}
