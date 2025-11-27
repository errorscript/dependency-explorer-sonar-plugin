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
package bje.buildtools.dependency.explorer.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Properties;

import org.junit.jupiter.api.Test;

import bje.buildtools.dependency.explorer.TestUtil;
import bje.buildtools.dependency.explorer.util.Constants;

class ProjectParserTest {

    @Test
    void testDefault() {
        final var prop = new Properties();
        final var config = TestUtil.getConfiguration(prop);
        final var p = new ProjectParser(config);
        final var iter = p.getAnalyzer().iterator();
        assertEquals(VersionsAnalyzer.class, iter.next().getClass());
        assertEquals(LicensesAnalyser.class, iter.next().getClass());
        assertEquals(UpdatesAnalyzer.class, iter.next().getClass());
        assertEquals(UnusedAnalyzer.class, iter.next().getClass());
        assertEquals(TransitiveAnalyzer.class, iter.next().getClass());
        assertFalse(iter.hasNext());
    }

    @Test
    void testNoAnalyzers() {
        final var prop = new Properties();
        prop.setProperty(Constants.COHERENCE_SKIP_PROPERTY, "true");
        prop.setProperty(Constants.LICENSE_SKIP_PROPERTY, "true");
        prop.setProperty(Constants.UPDATE_SKIP_PROPERTY, "true");
        prop.setProperty(Constants.UNUSED_SKIP_PROPERTY, "true");
        prop.setProperty(Constants.TRANSITIVE_SKIP_PROPERTY, "true");
        final var config = TestUtil.getConfiguration(prop);
        final var p = new ProjectParser(config);
        final var iter = p.getAnalyzer().iterator();
        assertFalse(iter.hasNext());
    }

}
