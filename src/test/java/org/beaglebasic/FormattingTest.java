package org.beaglebasic;

import org.beaglebasic.runtime.Formatter.NumberFormatter;
import org.junit.Test;

public class FormattingTest {
    @Test
    public void testFormat() {
        System.out.println(new NumberFormatter("**$##.##").format(-21.2));
    }
}
