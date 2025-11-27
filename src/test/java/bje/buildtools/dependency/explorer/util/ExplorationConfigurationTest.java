/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2023-2025 errorscript@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package bje.buildtools.dependency.explorer.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import bje.buildtools.dependency.explorer.TestUtil;
import bje.buildtools.dependency.explorer.filter.FilterList;

class ExplorationConfigurationTest {

    private static final String CLASSIC = "1.6.0";
    private static final String FILTER = "g:a:*,f:h:2.1.3";
    private static final String NOT_CLASSIC = "2.5.1-SNAPSHOT";

    @Test
    void test() {
        final var prop = new Properties();
        final var config = TestUtil.getConfiguration(prop);

        var conf = ExplorationConfiguration.of(config);
        var f = new FilterList(null);
        assertEquals(f, conf.exclusionFilter);
        assertEquals(ExplorationConfiguration.REGEX_ONLY_CLASSIC, conf.versionsPattern.toString());

        prop.setProperty(Constants.FILTERING_ONLY_CLASSIC_VERSION_PROPERTY, Boolean.FALSE.toString());
        prop.setProperty(Constants.FILTERING_EXCLUSIONS_LIST_PROPERTY, FILTER);
        conf = ExplorationConfiguration.of(config);

        f = new FilterList(FILTER);
        assertEquals(f, conf.exclusionFilter);
        assertEquals(ExplorationConfiguration.REGEX_ALLOW_ALL, conf.versionsPattern.toString());

    }

    @Test
    void testRegex() {
        final var only = Pattern.compile(ExplorationConfiguration.REGEX_ONLY_CLASSIC);
        final var classicMatcher = only.matcher(CLASSIC);
        assertTrue(classicMatcher.find());
        final var notClassicMatcher = only.matcher(NOT_CLASSIC);
        assertFalse(notClassicMatcher.find());

        final var all = Pattern.compile(ExplorationConfiguration.REGEX_ALLOW_ALL);
        final var classicAllMatcher = all.matcher(CLASSIC);
        assertTrue(classicAllMatcher.find());
        final var notClassicAllMatcher = all.matcher(NOT_CLASSIC);
        assertTrue(notClassicAllMatcher.find());
    }

}
