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
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Test;
import org.sonar.api.batch.rule.Severity;
import org.xml.sax.SAXException;

import bje.buildtools.dependency.explorer.ExplorerSensor;
import bje.buildtools.dependency.explorer.TestUtil;
import bje.buildtools.dependency.explorer.data.Artifact;
import bje.buildtools.dependency.explorer.data.Pom;
import bje.buildtools.dependency.explorer.data.DependencyType;
import bje.buildtools.dependency.explorer.data.POMType;
import bje.buildtools.dependency.explorer.license.LicenseParser;
import bje.buildtools.dependency.explorer.maven.PomParser;
import bje.buildtools.dependency.explorer.util.Constants;

class UpdatesAnalyzerTest {
    private void test(final Properties prop, final List<String> list, final DependencyType type,
            final Severity severity, final String desc, final String print)
            throws IOException, SAXException, ParserConfigurationException {
        final var config = TestUtil.getConfiguration(prop);
        final var model = LicenseParser.init(config);
        ExplorerSensor.LICENSE_MODEL.set(model);
        final var file = TestUtil.loadFile();
        final var pom = new Pom(file, POMType.MAIN);
        PomParser.fullParse(pom, model);
        final var defMain = pom.addDependency(new Artifact("group", "main", "1.0.0"), Arrays.asList(), type);

        final var defA = pom.addDependency(new Artifact("group", "artifeactA", "1.0.0"), list, type);
        defA.setPropertyName("property.name");

        defMain.addDependency(defA);

        pom.updateRoot(defMain);

        final var analyzer = new UpdatesAnalyzer(config);
        final var result = analyzer.analyze(pom);
        final var it = result.iterator();
        if (severity != null) {
            assertTrue(it.hasNext());
            final var p = it.next();
            assertEquals("group:artifeactA", p.getGA());
            assertEquals(file, p.getComponent());
            assertEquals(desc, p.getDescription());
            assertEquals("spark-core", p.getModuleName());
            assertEquals(Constants.UPDATE_RULE_KEY, p.getRuleKey());
            assertEquals(severity, p.getSeverity());
        }

        assertFalse(it.hasNext());

        final var sb = new StringBuilder(512);
        result.print(sb);
        assertEquals(print, sb.toString());
    }

    @Test
    void testAll() throws IOException, SAXException, ParserConfigurationException {
        final var prop = new Properties();
        test(prop, Arrays.asList("1.0.1", "1.0.2"), DependencyType.DEPENDENCY, Severity.INFO,
                "Patch update available for dependency group:artifeactA:1.0.0. This dependency use a property : \"property.name\". Next version is 1.0.1. Latest version is 1.0.2.",
                """
                        --------------------------------------------------------------------------------
                         UPDATES CHECK
                        --------------------------------------------------------------------------------
                        group:artifeactA:1.0.0 -> next : 1.0.1, last : 1.0.2
                        """);
        test(prop, Arrays.asList("1.0.1", "1.0.2"), DependencyType.DEPENDENCY_MANAGEMENT, Severity.INFO,
                "Patch update available for dependency group:artifeactA:1.0.0 (see dependency management). This dependency use a property : \"property.name\". Next version is 1.0.1. Latest version is 1.0.2.",
                """
                        --------------------------------------------------------------------------------
                         UPDATES CHECK
                        --------------------------------------------------------------------------------
                        group:artifeactA:1.0.0 -> next : 1.0.1, last : 1.0.2
                        """

        );
        test(prop, Arrays.asList("1.0.1", "1.0.2"), DependencyType.PLUGIN, Severity.INFO,
                "Patch update available for dependency group:artifeactA:1.0.0 (see plugin). This dependency use a property : \"property.name\". Next version is 1.0.1. Latest version is 1.0.2.",
                """
                        --------------------------------------------------------------------------------
                         UPDATES CHECK
                        --------------------------------------------------------------------------------
                        group:artifeactA:1.0.0 -> next : 1.0.1, last : 1.0.2
                        """);
        test(prop, Arrays.asList("1.0.1", "1.0.2"), DependencyType.PLUGIN_MANAGEMENT, Severity.INFO,
                "Patch update available for dependency group:artifeactA:1.0.0 (see plugin management). This dependency use a property : \"property.name\". Next version is 1.0.1. Latest version is 1.0.2.",
                """
                        --------------------------------------------------------------------------------
                         UPDATES CHECK
                        --------------------------------------------------------------------------------
                        group:artifeactA:1.0.0 -> next : 1.0.1, last : 1.0.2
                        """);
        test(prop, Arrays.asList("1.0.1", "1.0.2"), DependencyType.DEPENDENCY, Severity.INFO,
                "Patch update available for dependency group:artifeactA:1.0.0. This dependency use a property : \"property.name\". Next version is 1.0.1. Latest version is 1.0.2.",
                """
                        --------------------------------------------------------------------------------
                         UPDATES CHECK
                        --------------------------------------------------------------------------------
                        group:artifeactA:1.0.0 -> next : 1.0.1, last : 1.0.2
                        """);
        test(prop, Arrays.asList("1.0.1", "1.5.2"), DependencyType.DEPENDENCY, Severity.MINOR,
                "Minor update available for dependency group:artifeactA:1.0.0. This dependency use a property : \"property.name\". Next version is 1.0.1. Latest version is 1.5.2.",
                """
                        --------------------------------------------------------------------------------
                         UPDATES CHECK
                        --------------------------------------------------------------------------------
                        group:artifeactA:1.0.0 -> next : 1.0.1, last : 1.5.2
                        """);
        test(prop, Arrays.asList("1.0.1", "2.5.2"), DependencyType.DEPENDENCY, Severity.MAJOR,
                "Major update available for dependency group:artifeactA:1.0.0. This dependency use a property : \"property.name\". Next version is 1.0.1. Latest version is 2.5.2.",
                """
                        --------------------------------------------------------------------------------
                         UPDATES CHECK
                        --------------------------------------------------------------------------------
                        group:artifeactA:1.0.0 -> next : 1.0.1, last : 2.5.2
                        """);
        test(prop, Arrays.asList(), DependencyType.DEPENDENCY, null, null, """
                --------------------------------------------------------------------------------
                 UPDATES CHECK
                --------------------------------------------------------------------------------
                """);
    }

}
