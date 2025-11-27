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
package bje.buildtools.dependency.explorer.maven.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import bje.buildtools.dependency.explorer.TestUtil;
import bje.buildtools.dependency.explorer.data.Artifact;
import bje.buildtools.dependency.explorer.data.POMType;
import bje.buildtools.dependency.explorer.data.Pom;
import bje.buildtools.dependency.explorer.data.Scope;
import bje.buildtools.dependency.explorer.filter.FilterList;
import bje.buildtools.dependency.explorer.util.Constants;
import bje.buildtools.dependency.explorer.util.ExplorationConfiguration;

class DependencyAnalysisTest {

    private Artifact a(final String g, final String a, final String v) {
        return new Artifact(g, a, v);
    }

    private void equals(final List<Artifact> l, final Artifact... as) {
        final Set<Artifact> test = new TreeSet<>(l);
        final Set<Artifact> res = new TreeSet<>();
        Collections.addAll(res, as);
        TestUtil.testList(test, res);
    }

    @Test
    void testDependency() throws IOException, URISyntaxException {
        final var classLoader = VersionUpdatesTest.class.getClassLoader();
        final var input = TestUtil.loadFile();
        final var pom = new Pom(input, POMType.MAIN);
        final var config = new ExplorationConfiguration(Pattern.compile(ExplorationConfiguration.REGEX_ALLOW_ALL),
                new FilterList(null), false, Constants.UPDATE_MAVEN_EXECUTABLE_PATH_DEFAULT, true);

        final var dfile = Path.of(classLoader.getResource("reports/dependency-analysis.html").toURI());
        assertTrue(DependencyAnalysis.parse(dfile, pom, config));

        final var eff = pom.getEffectiveDependency();
        assertEquals(2, eff.size());
        var l = eff.get(Scope.COMPILE);
        equals(l, a("bje.modrics", "modrics-core", "0.0.19-20250411.235743-14"), //
                a("bje.modrics", "modrics-transform", "0.0.19-20250411.235743-14"), //
                a("bje.jardin", "jardin-base", "0.0.50-SNAPSHOT"), //
                a("bje.toolbox", "toolbox-httpserver", "0.1.22-SNAPSHOT"), //
                a("bje.toolbox", "toolbox-reader", "0.1.22-SNAPSHOT"), //
                a("bje.toolbox", "toolbox-monitoring", "0.1.22-SNAPSHOT"), //
                a("bje.hardware", "hardware-api", "0.0.29-20250412.000136-14"), //
                a("bje.docstore", "docstore-metrics", "0.0.19-20250412.000008-12"));//

        l = eff.get(Scope.TEST);
        equals(l, a("bje.toolbox", "toolbox-test", "0.1.22-SNAPSHOT"), //
                a("bje.jardin", "jardin-test", "0.0.50-SNAPSHOT"), //
                a("bje.hardware", "hardware-mqtt", "0.0.29-20250412.000136-14"), //
                a("bje.jardin", "jardin-metrics-herbert", "0.0.50-SNAPSHOT"), //
                a("org.awaitility", "awaitility", "4.2.2")); //

        final var und = pom.getUndeclaredDependency();
        assertEquals(2, und.size());

        l = und.get(Scope.COMPILE);
        equals(l, a("bje.toolbox", "toolbox-api", "0.1.22-SNAPSHOT"), //
                a("bje.toolbox", "toolbox-core", "0.1.22-SNAPSHOT"), //
                a("io.dropwizard.metrics", "metrics-core", "4.2.30"));//
        l = und.get(Scope.TEST);
        equals(l, a("org.junit.jupiter", "junit-jupiter-api", "5.11.4"), //
                a("org.hamcrest", "hamcrest", "2.1")); //
        final var unu = pom.getUnusedDependency();
        assertEquals(1, unu.size());
        l = unu.get(Scope.TEST);
        equals(l, a("bje.toolbox", "toolbox-report", "0.1.22-SNAPSHOT"), //
                a("org.junit.jupiter", "junit-jupiter-engine", "5.11.4"), //
                a("org.mockito", "mockito-core", "5.15.2"), //
                a("org.mockito", "mockito-junit-jupiter", "5.15.2")); //

    }

}
