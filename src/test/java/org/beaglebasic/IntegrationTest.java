package org.beaglebasic;

import org.beaglebasic.BeagleBasicInterpreterMain.UserOptions;
import org.beaglebasic.error.BeagleBasicRuntimeError;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import static org.beaglebasic.BeagleBasicInterpreterMain.interpretAndRun;
import static org.beaglebasic.error.BeagleBasicRuntimeError.ErrorCode.IO_ERROR;
import static org.junit.Assert.assertEquals;

public class IntegrationTest {

    @Test
    public void testForLoop() {
        runTest("forloop.bas", "forloop.bas.output");
    }

    @Test
    public void testNestedForLoop() {
        runTest("nested_forloop.bas", "nested_forloop.bas.output");
    }

    @Test
    public void testScalarVariable() {
        runTest("scalar_var.bas", "scalar_var.bas.output");
    }

    @Test
    public void testArrayVariable() {
        runTest("array_var.bas", "array_var.bas.output");
    }

    private void runTest(String source, String output) {
        var bos = new ByteArrayOutputStream();
        var out = new PrintStream(bos);
        interpretAndRun(
                UserOptions.ofTest(),
                loadSourceCodeFromResource(source),
                out);
        out.close();

        assertEquals(
                loadOutputFromResource(output),
                new String(bos.toByteArray())
        );
    }

    private String loadSourceCodeFromResource(String filename) {
        return BeagleBasicInterpreterMain.loadSource(
                getClass().getClassLoader().getResource(filename).getFile());
    }

    private String loadOutputFromResource(String filename) {
        return loadOutput(
                getClass().getClassLoader().getResource(filename).getFile());
    }

    private static String loadOutput(String filename) {
        try (InputStream in = new BufferedInputStream(new FileInputStream(filename))) {
            return new String(in.readAllBytes());
        } catch (IOException e) {
            throw new BeagleBasicRuntimeError(
                    IO_ERROR,
                    "Failed to read source code: " + filename + ", error: " + e.getMessage()
            );
        }
    }
}
