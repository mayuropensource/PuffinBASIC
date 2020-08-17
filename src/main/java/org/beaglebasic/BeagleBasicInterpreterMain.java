package org.beaglebasic;

import com.google.common.base.Strings;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.beaglebasic.antlr4.BeagleBasicLexer;
import org.beaglebasic.antlr4.BeagleBasicParser;
import org.beaglebasic.domain.BeagleBasicSymbolTable;
import org.beaglebasic.error.BeagleBasicRuntimeError;
import org.beaglebasic.error.BeagleBasicSyntaxError;
import org.beaglebasic.parser.BeagleBasicIR;
import org.beaglebasic.parser.BeagleBasicIRListener;
import org.beaglebasic.parser.LinenumberListener;
import org.beaglebasic.parser.LinenumberListener.ThrowOnDuplicate;
import org.beaglebasic.runtime.BeagleBasicRuntime;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

import static org.beaglebasic.error.BeagleBasicRuntimeError.ErrorCode.IO_ERROR;
import static org.beaglebasic.parser.LinenumberListener.ThrowOnDuplicate.LOG;
import static org.beaglebasic.parser.LinenumberListener.ThrowOnDuplicate.THROW;

public final class BeagleBasicInterpreterMain {

    public static void main(String... args) {
        var userOptions = parseCommandLineArgs(args);

        Instant t0 = Instant.now();
        var sourceCode = loadSource(userOptions.filename);
        logTimeTaken("LOAD", t0, userOptions.timing);

        interpretAndRun(userOptions, sourceCode, System.out);
    }

    private static UserOptions parseCommandLineArgs(String... args) {
        var parser = ArgumentParsers
                .newFor("BeagleBasic")
                .build();
        parser.addArgument("-d", "--logduplicate")
                .help("Log error on duplicate")
                .action(Arguments.storeTrue());
        parser.addArgument("-l", "--list")
                .help("Print Sorted Source Code")
                .action(Arguments.storeTrue());
        parser.addArgument("-i", "--ir")
                .help("Print IR")
                .action(Arguments.storeTrue());
        parser.addArgument("-t", "--timing")
                .help("Print timing")
                .action(Arguments.storeTrue());
        parser.addArgument("file").nargs(1);
        Namespace res = null;
        try {
            res = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }
        if (res == null) {
            throw new IllegalStateException();
        }

        return new UserOptions(
                res.getBoolean("logduplicate"),
                res.getBoolean("list"),
                res.getBoolean("ir"),
                res.getBoolean("timing"),
                (String) res.getList("file").get(0)
        );
    }

    public static String loadSource(String filename) {
        var sb = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get(filename), StandardCharsets.US_ASCII)) {
            stream.forEach(s -> sb.append(s).append(System.lineSeparator()));
        } catch (IOException e) {
            throw new BeagleBasicRuntimeError(
                    IO_ERROR,
                    "Failed to read source code: " + filename + ", error: " + e.getMessage()
            );
        }
        return sb.toString();
    }

    public static void interpretAndRun(
            UserOptions userOptions,
            String sourceCode,
            PrintStream out)
    {
        Instant t1 = Instant.now();
        var sortedInput = syntaxCheckAndSortByLineNumber(sourceCode,
                userOptions.logOnDuplicate ? LOG : THROW);
        if (sortedInput.isEmpty()) {
            throw new BeagleBasicSyntaxError(
                    "Failed to parse source code! Check if a linenumber is missing");
        }
        logTimeTaken("SORT", t1, userOptions.timing);

        log("LIST", userOptions.listSourceCode);
        log(sortedInput, userOptions.listSourceCode);

        Instant t2 = Instant.now();
        var ir = generateIR(sortedInput);
        logTimeTaken("IR", t2, userOptions.timing);
        log("IR", userOptions.printIR);
        if (userOptions.printIR) {
            int i = 0;
            for (var instruction : ir.getInstructions()) {
                log(i++ + ": " + instruction, true);
            }
        }

        log("RUN", userOptions.timing);
        Instant t3 = Instant.now();
        run(ir, out);
        logTimeTaken("RUN", t3, userOptions.timing);
    }

    private static void log(String s, boolean log) {
        if (log) {
            System.out.println(s);
        }
    }

    private static void logTimeTaken(String tag, Instant t1, boolean log) {
        var duration  = Duration.between(t1, Instant.now());
        var timeSec = duration.getSeconds() + duration.getNano() / 1000_000_000.0;
        log("[" + tag + "] time taken = " + timeSec + " s", log);
    }

    private static void run(BeagleBasicIR ir, PrintStream out) {
        var runtime = new BeagleBasicRuntime(ir, out);
        runtime.run();
    }

    private static BeagleBasicIR generateIR(String input) {
        var in = CharStreams.fromString(input);
        var lexer = new BeagleBasicLexer(in);
        var tokens = new CommonTokenStream(lexer);
        var parser = new BeagleBasicParser(tokens);
        var tree = parser.prog();
        var walker = new ParseTreeWalker();
        var symbolTable = new BeagleBasicSymbolTable();
        var ir = new BeagleBasicIR(in, symbolTable);
        var irListener = new BeagleBasicIRListener(in, ir);
        walker.walk(irListener, tree);
        irListener.semanticCheckAfterParsing();
        return ir;
    }

    private static String syntaxCheckAndSortByLineNumber(String input, ThrowOnDuplicate throwOnDuplicate) {
        var in = CharStreams.fromString(input);
        var syntaxErrorListener = new ThrowingErrorListener(input);
        var lexer = new BeagleBasicLexer(in);
        lexer.removeErrorListeners();
        lexer.addErrorListener(syntaxErrorListener);
        var tokens = new CommonTokenStream(lexer);
        var parser = new BeagleBasicParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(syntaxErrorListener);
        var tree = parser.prog();
        var walker = new ParseTreeWalker();
        var linenumListener = new LinenumberListener(in, throwOnDuplicate);
        walker.walk(linenumListener, tree);
        return linenumListener.getSortedCode();
    }

    private static final class ThrowingErrorListener extends BaseErrorListener {

        private final String input;

        public ThrowingErrorListener(String input) {
            this.input = input;
        }

        @Override
        public void syntaxError(
                Recognizer<?, ?> recognizer,
                Object offendingSymbol,
                int line,
                int charPositionInLine,
                String msg,
                RecognitionException e)
        {
            var lineIndex = line - 1;
            var lines = input.split(System.lineSeparator());
            String inputLine;
            if (lineIndex >= 0 && lineIndex < lines.length) {
                inputLine = lines[lineIndex];
                if (charPositionInLine >= 0 && charPositionInLine <= inputLine.length()) {
                    inputLine = inputLine + System.lineSeparator()
                            + Strings.repeat(" ", Math.max(0, charPositionInLine)) + '^';
                }
            } else {
                inputLine = "<LINE OUT OF RANGE>";
            }
            throw new BeagleBasicSyntaxError(
                    "[" + line + ":" + charPositionInLine + "] " + msg + System.lineSeparator()
                    + inputLine
            );
        }

    }

    public static final class UserOptions {

        public static UserOptions ofTest() {
            return new UserOptions(
                    false, false, false, false, null
            );
        }

        public final boolean logOnDuplicate;
        public final boolean listSourceCode;
        public final boolean printIR;
        public final boolean timing;
        public final String filename;

        public UserOptions(
                boolean logOnDuplicate,
                boolean listSourceCode,
                boolean printIR,
                boolean timing,
                String filename)
        {
            this.logOnDuplicate = logOnDuplicate;
            this.listSourceCode = listSourceCode;
            this.printIR = printIR;
            this.timing = timing;
            this.filename = filename;
        }
    }

}

