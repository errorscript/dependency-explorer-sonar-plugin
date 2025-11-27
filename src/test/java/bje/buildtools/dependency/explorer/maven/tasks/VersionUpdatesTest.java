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
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import bje.buildtools.dependency.explorer.TestUtil;
import bje.buildtools.dependency.explorer.data.Artifact;
import bje.buildtools.dependency.explorer.data.Dependency;
import bje.buildtools.dependency.explorer.data.POMType;
import bje.buildtools.dependency.explorer.data.Pom;
import bje.buildtools.dependency.explorer.data.Version;
import bje.buildtools.dependency.explorer.filter.FilterList;
import bje.buildtools.dependency.explorer.util.Constants;
import bje.buildtools.dependency.explorer.util.ExplorationConfiguration;

class VersionUpdatesTest {

    private void addVerions(final NavigableSet<Version> versions, final List<String> majors) {
        for (final String s : majors) {
            versions.add(new Version(s));
        }
    }

    private void isIn(final Dependency update, final Map<String, Dependency> dependencyUpdates) {
        for (final Dependency upd : dependencyUpdates.values()) {
            if (Objects.equals(update.getGroupId(), upd.getGroupId())
                    && Objects.equals(update.getArtifactId(), upd.getArtifactId())
                    && Objects.equals(update.getVersion(), upd.getVersion())
                    && Objects.equals(update.getNextVersion(), upd.getNextVersion())
                    && Objects.equals(update.getLastVersion(), upd.getLastVersion())
                    && Objects.equals(update.getVersions(), upd.getVersions())
                    && Objects.equals(update.getPropertyName(), upd.getPropertyName())) {
                return;
            }
        }
        fail();
    }

    @Test
    void testDependency() throws SAXException, IOException, ParserConfigurationException, URISyntaxException {
        final var classLoader = VersionUpdatesTest.class.getClassLoader();
        final var input = TestUtil.loadFile();
        final var pom = new Pom(input, POMType.MAIN);
        final var config = new ExplorationConfiguration(Pattern.compile(ExplorationConfiguration.REGEX_ALLOW_ALL),
                new FilterList(null), false, Constants.UPDATE_MAVEN_EXECUTABLE_PATH_DEFAULT, true);

        final var dfile = Path.of(classLoader.getResource("dependency-updates-report.xml").toURI());
        assertTrue(VersionUpdates.parseDependencies(dfile, pom, config));
        final var pfile = Path.of(classLoader.getResource("plugin-updates-report.xml").toURI());
        assertTrue(VersionUpdates.parsePlugins(pfile, pom, config));
        final var rfile = Path.of(classLoader.getResource("property-updates-report.xml").toURI());
        assertTrue(VersionUpdates.parseProperties(rfile, pom, config));

        assertEquals(16, pom.getMapDependencies().size());
        assertEquals(8, pom.getMapPlugins().size());
        isIn(update(pom, "test.group", "dependency-with-minor", "4.0.0", null, Arrays.asList(),
                Arrays.asList("4.1.0", "4.2.0", "4.3.0"), Arrays.asList()), pom.getMapDependencies());
        isIn(update(pom, "test.group", "dependencymanagement-with-incremental", "4.2.2", null,
                Arrays.asList("4.2.3"), Arrays.asList(), Arrays.asList()), pom.getMapDependencies());

        isIn(update(pom, "test.group", "plugin-with-none", "3.2.0", null, Arrays.asList(), Arrays.asList(),
                Arrays.asList()), pom.getMapPlugins());
        isIn(update(pom, "test.group", "pluginmanagement-with-major", "1.3", null, Arrays.asList(), Arrays.asList(),
                Arrays.asList("3.0.0", "3.1.0")), pom.getMapPlugins());

        isIn(update(pom, "test.group", "property-artifact-major-1", null, "property.with.major", Arrays.asList(),
                Arrays.asList(), Arrays.asList("2.0.0", "2.1.0", "2.1.1", "2.2.0", "2.2.1", "2.3.0")),
                pom.getMapDependencies());

    }

    private Dependency update(final Pom pom, final String groupId, final String artifactId,
            final String currentVersion, final String propertyName, final List<String> incrementals,
            final List<String> minors, final List<String> majors) {
        final var upd = new Dependency(pom, new Artifact(groupId, artifactId, currentVersion));
        addVerions(upd.getVersions(), incrementals);
        addVerions(upd.getVersions(), minors);
        addVerions(upd.getVersions(), majors);
        upd.setPropertyName(propertyName);
        return upd;
    }

}
