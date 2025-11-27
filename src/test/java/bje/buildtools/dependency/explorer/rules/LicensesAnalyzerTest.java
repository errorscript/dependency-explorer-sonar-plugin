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
import bje.buildtools.dependency.explorer.license.LicenseDefinition;
import bje.buildtools.dependency.explorer.license.LicenseIdentity;
import bje.buildtools.dependency.explorer.license.LicenseParser;
import bje.buildtools.dependency.explorer.util.Constants;

class LicensesAnalyzerTest {

    private static LicenseDefinition set(final List<LicenseIdentity> list) {
        return LicenseDefinition.of(list);
    }

    @Test
    void test() throws IOException, SAXException, ParserConfigurationException {
        final var prop = new Properties();
        final var config = TestUtil.getConfiguration(prop);
        final var model = LicenseParser.init(config);
        ExplorerSensor.LICENSE_MODEL.set(model);

        final var file = TestUtil.loadFile();
        final var pom = new Pom(file, POMType.MAIN);
        pom.fill(new Artifact("com.sparkjava", "spark-core", "2.9.4"));
        final var defMain = pom.addDependency(new Artifact("group", "main", "1.0.0"), Arrays.asList(),
                DependencyType.DEPENDENCY);
        defMain.setLicenses(set(model.getLicense("MIT")));

        final var defA = pom.addDependency(new Artifact("group", "artifeactA", "1.0.0"), Arrays.asList("1.0.1"),
                DependencyType.DEPENDENCY);
        defA.setLicenses(set(model.getLicense("GPL-1.0")));
        defMain.addDependency(defA);

        final var defB = pom.addDependency(new Artifact("group", "spark-core", "1.0.0"), Arrays.asList("1.0.1"),
                DependencyType.DEPENDENCY);
        defB.setLicenses(set(model.getLicense("APSL-2.0")));
        defMain.addDependency(defB);

        pom.updateRoot(defMain);

        final var analyzer = new LicensesAnalyser(config);
        final var result = analyzer.analyze(pom);
        final var it = result.iterator();
        assertTrue(it.hasNext());
        var p = it.next();

        assertEquals("group:artifeactA", p.getGA());
        assertEquals(file, p.getComponent());
        assertEquals(
                "Dependency group:artifeactA is not compatible with project license (MIT) :\n  - 1.0.0 : GPL-1.0 (Copyleft-like)\n",
                p.getDescription());
        assertEquals("spark-core", p.getModuleName());
        assertEquals(Constants.LICENSE_RULE_KEY, p.getRuleKey());
        assertEquals(Severity.MINOR, p.getSeverity());

        assertTrue(it.hasNext());

        p = it.next();

        assertEquals("group:spark-core", p.getGA());
        assertEquals(file, p.getComponent());
        assertEquals(
                "Dependency group:spark-core is not compatible with project license (MIT) :\n  - 1.0.0 : APSL-2.0 (OSI)\n",
                p.getDescription());
        assertEquals("spark-core", p.getModuleName());
        assertEquals(Constants.LICENSE_RULE_KEY, p.getRuleKey());
        assertEquals(Severity.MINOR, p.getSeverity());

        assertFalse(it.hasNext());

        final var sb = new StringBuilder(512);
        result.print(sb);
        assertEquals("""
                --------------------------------------------------------------------------------
                 LICENSES CHECK
                --------------------------------------------------------------------------------
                group:artifeactA
                +- 1.0.0  [GPL-1.0]
                group:spark-core
                +- 1.0.0  [APSL-2.0]
                """, sb.toString());
    }

}
