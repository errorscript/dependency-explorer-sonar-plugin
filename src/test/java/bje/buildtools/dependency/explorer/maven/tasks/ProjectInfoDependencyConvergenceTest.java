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

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import bje.buildtools.dependency.explorer.TestUtil;
import bje.buildtools.dependency.explorer.data.Artifact;
import bje.buildtools.dependency.explorer.data.POMType;
import bje.buildtools.dependency.explorer.data.Pom;
import bje.buildtools.dependency.explorer.filter.FilterList;
import bje.buildtools.dependency.explorer.util.Constants;
import bje.buildtools.dependency.explorer.util.ExplorationConfiguration;

class ProjectInfoDependencyConvergenceTest {

    @Test
    void testDependency() throws IOException, URISyntaxException {
        final var classLoader = VersionUpdatesTest.class.getClassLoader();
        final var input = TestUtil.loadFile();
        final var pom = new Pom(input, POMType.MAIN);
        final var config = new ExplorationConfiguration(Pattern.compile(ExplorationConfiguration.REGEX_ALLOW_ALL),
                new FilterList(null), false, Constants.UPDATE_MAVEN_EXECUTABLE_PATH_DEFAULT, true);

        final var dfile = Path.of(classLoader.getResource("reports/dependency-convergence.html").toURI());
        ProjectInfoDependencyConvergence.parse(dfile, pom, config);

        final var map = pom.getVersionIncompatibility();
        assertEquals(2, map.size());
        var l = map.get(new Artifact("org.junit.jupiter", "junit-jupiter-api", null));
        Collections.sort(l);
        assertEquals(Arrays.asList("5.10.1", "5.11.4"), l);
        l = map.get(new Artifact("org.junit.jupiter", "junit-jupiter-engine", null));
        Collections.sort(l);
        assertEquals(Arrays.asList("5.10.1", "5.11.4"), l);

    }
}
