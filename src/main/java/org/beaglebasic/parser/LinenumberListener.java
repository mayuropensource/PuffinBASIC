package org.beaglebasic.parser;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.misc.Interval;
import org.beaglebasic.antlr4.BeagleBasicBaseListener;
import org.beaglebasic.antlr4.BeagleBasicParser;
import org.beaglebasic.error.BeagleBasicSyntaxError;
import org.jetbrains.annotations.NotNull;

public class LinenumberListener extends BeagleBasicBaseListener {

    public enum ThrowOnDuplicate {
        THROW,
        LOG
    }

    private final CharStream input;
    private final ThrowOnDuplicate throwOnDuplicate;
    private final Int2ObjectSortedMap<String> sortedLines;

    public LinenumberListener(
            @NotNull CharStream input,
            @NotNull ThrowOnDuplicate throwOnDuplicate)
    {
        this.input = Preconditions.checkNotNull(input);
        this.throwOnDuplicate = Preconditions.checkNotNull(throwOnDuplicate);
        this.sortedLines = new Int2ObjectAVLTreeMap<>();
    }

    public String getSortedCode() {
        return String.join("", sortedLines.values());
    }

    @Override
    public void exitLine(BeagleBasicParser.LineContext ctx) {
        final int linenum = parseLinenum(ctx.linenum().DECIMAL().getText());
        final String line = input.getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        var oldLine = sortedLines.put(linenum, line);
        if (oldLine != null) {
            var message = "Duplicate line number!" + System.lineSeparator() +
                    "OLD:" + System.lineSeparator() +
                    oldLine +
                    "NEW:" + System.lineSeparator() +
                    line;
            if (throwOnDuplicate == ThrowOnDuplicate.THROW) {
                throw new BeagleBasicSyntaxError(message);
            } else {
                System.err.println(message);
            }
        }
    }

    static int parseLinenum(String txt) {
        try {
            return Integer.parseInt(txt);
        } catch (NumberFormatException e) {
            throw new BeagleBasicSyntaxError("Bad line number: '" + txt + "'");
        }
    }
}
