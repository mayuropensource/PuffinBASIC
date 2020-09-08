package org.puffinbasic.parser;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.misc.Interval;
import org.jetbrains.annotations.NotNull;
import org.puffinbasic.antlr4.PuffinBasicBaseListener;
import org.puffinbasic.antlr4.PuffinBasicParser;
import org.puffinbasic.error.PuffinBasicSyntaxError;

import java.util.concurrent.atomic.AtomicInteger;

public class LinenumberListener extends PuffinBasicBaseListener {

    public enum ThrowOnDuplicate {
        THROW,
        LOG
    }

    private final AtomicInteger linenumGenerator;
    private final CharStream input;
    private final ThrowOnDuplicate throwOnDuplicate;
    private final Int2ObjectSortedMap<String> sortedLines;
    private int numLinenum;
    private int numNoLinenum;
    private int numStmtWithLinenum;

    public LinenumberListener(
            @NotNull CharStream input,
            @NotNull ThrowOnDuplicate throwOnDuplicate)
    {
        this.linenumGenerator = new AtomicInteger();
        this.input = Preconditions.checkNotNull(input);
        this.throwOnDuplicate = Preconditions.checkNotNull(throwOnDuplicate);
        this.sortedLines = new Int2ObjectAVLTreeMap<>();
    }

    public String getSortedCode() {
        checkLinenumberMode();
        return String.join("", sortedLines.values());
    }

    private void checkLinenumberMode() {
        if (numLinenum > 0 && numNoLinenum > 0) {
            throw new PuffinBasicSyntaxError(
                    "Cannot mix linenumber and no-linenumber mode!"
            );
        }
        if (numNoLinenum > 0) {
            if (numStmtWithLinenum > 0) {
                throw new PuffinBasicSyntaxError(
                        "GOTO/GOSUB/RETURN linenumber cannot be used in no-linenumber mode!"
                );
            }
        }
    }

    @Override
    public void exitLine(PuffinBasicParser.LineContext ctx) {
        final String line = input.getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));

        final int linenum;
        if (ctx.linenum() != null) {
            linenum = parseLinenum(ctx.linenum().DECIMAL().getText());
            numLinenum++;
        } else {
            linenum = linenumGenerator.incrementAndGet();
            numNoLinenum++;
        }

        var oldLine = sortedLines.put(linenum, line);
        if (oldLine != null) {
            var message = "Duplicate line number!" + System.lineSeparator() +
                    "OLD:" + System.lineSeparator() +
                    oldLine +
                    "NEW:" + System.lineSeparator() +
                    line;
            if (throwOnDuplicate == ThrowOnDuplicate.THROW) {
                throw new PuffinBasicSyntaxError(message);
            } else {
                System.err.println(message);
            }
        }
    }

    @Override
    public void exitGosubstmt(PuffinBasicParser.GosubstmtContext ctx) {
        numStmtWithLinenum++;
    }

    @Override
    public void exitGotostmt(PuffinBasicParser.GotostmtContext ctx) {
        numStmtWithLinenum++;
    }

    @Override
    public void exitThen(PuffinBasicParser.ThenContext ctx) {
        if (ctx.linenum() != null) {
            numStmtWithLinenum++;
        }
    }

    @Override
    public void exitElsestmt(PuffinBasicParser.ElsestmtContext ctx) {
        if (ctx.linenum() != null) {
            numStmtWithLinenum++;
        }
    }

    static int parseLinenum(String txt) {
        try {
            return Integer.parseInt(txt);
        } catch (NumberFormatException e) {
            throw new PuffinBasicSyntaxError("Bad line number: '" + txt + "'");
        }
    }
}
