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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.sonar.api.batch.rule.Severity;

import bje.buildtools.dependency.explorer.TestUtil;
import bje.buildtools.dependency.explorer.data.Artifact;
import bje.buildtools.dependency.explorer.util.Constants;

class UnusedAnalyzerTest {

    @Test
    void test() throws IOException {
        final var prop = new Properties();
        final var input = TestUtil.loadFile();
        final var pom = TestUtil.createPomForUsageCheck(input);
        pom.fill(new Artifact("com.sparkjava", "spark-core", "2.9.4"));
        final var analyzer = new UnusedAnalyzer(TestUtil.getConfiguration(prop));
        final var result = analyzer.analyze(pom);
        final var it = result.iterator();
        assertTrue(it.hasNext());
        final var p = it.next();
        assertEquals("testGroup:testK", p.getGA());
        assertEquals(input, p.getComponent());
        assertEquals("Dependency testGroup:testK:1.0.0 is declared but not used in compile scope.", p.getDescription());
        assertEquals("spark-core", p.getModuleName());
        assertEquals(Constants.UNUSED_RULE_KEY, p.getRuleKey());
        assertEquals(Severity.MINOR, p.getSeverity());
        assertFalse(it.hasNext());

        final var sb = new StringBuilder(512);
        result.print(sb);
        assertEquals("""
                --------------------------------------------------------------------------------
                 UNUSED DEPENDENCY
                --------------------------------------------------------------------------------
                testGroup:testK:1.0.0 is declared but not used in COMPILE scope.
                """, sb.toString());
    }

}
