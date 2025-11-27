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
import java.util.Arrays;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.sonar.api.batch.rule.Severity;

import bje.buildtools.dependency.explorer.TestUtil;
import bje.buildtools.dependency.explorer.data.Artifact;
import bje.buildtools.dependency.explorer.data.Pom;
import bje.buildtools.dependency.explorer.data.POMType;
import bje.buildtools.dependency.explorer.util.Constants;

class VersionsAnalyzerTest {

    private void test(final Properties prop, final String second, final Severity severity, final String desc,
            final String print) throws IOException {

        final var file = TestUtil.loadFile();
        final var pom = new Pom(file, POMType.MAIN);
        pom.fill(new Artifact("com.sparkjava", "spark-core", "2.9.4"));
        pom.addVersionIncompatibility(new Artifact("group", "sub", null), Arrays.asList("1.0.5", second));

        final var analyzer = new VersionsAnalyzer(TestUtil.getConfiguration(prop));
        final var result = analyzer.analyze(pom);
        final var it = result.iterator();
        if (severity != null) {
            assertTrue(it.hasNext());
            final var p = it.next();

            assertEquals("group:sub", p.getGA());
            assertEquals(file, p.getComponent());
            assertEquals(desc, p.getDescription());
            assertEquals("spark-core", p.getModuleName());
            assertEquals(Constants.COHERENCE_RULE_KEY, p.getRuleKey());
            assertEquals(severity, p.getSeverity());
        }
        assertFalse(it.hasNext());

        final var sb = new StringBuilder(512);
        result.print(sb);
        assertEquals(print, sb.toString());
    }

    @Test
    void testAll() throws IOException {
        final var prop = new Properties();
        test(prop, "1.0.6", Severity.MINOR,
                "Difference between version of group:sub is minimal but can have vulnerabilities or other border effect. [1.0.5, 1.0.6]",
                """
                        --------------------------------------------------------------------------------
                         MULTIPLE VERSIONS CHECK
                        --------------------------------------------------------------------------------
                        group:sub
                        +- 1.0.5
                        +- 1.0.6
                        """);
        test(prop, "1.2.0", Severity.MINOR,
                "Difference between version of group:sub is minor and can lead to strange behaviour. [1.0.5, 1.2.0]",
                """
                        --------------------------------------------------------------------------------
                         MULTIPLE VERSIONS CHECK
                        --------------------------------------------------------------------------------
                        group:sub
                        +- 1.0.5
                        +- 1.2.0
                        """);
        test(prop, "2.0.0", Severity.MAJOR,
                "Difference between version of group:sub is major and can lead to bytecode incompatibilities. [1.0.5, 2.0.0]",
                """
                        --------------------------------------------------------------------------------
                         MULTIPLE VERSIONS CHECK
                        --------------------------------------------------------------------------------
                        group:sub
                        +- 1.0.5
                        +- 2.0.0
                        """);
    }

}
