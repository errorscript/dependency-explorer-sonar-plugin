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
package bje.buildtools.dependency.explorer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.json.JSONArray;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bje.buildtools.dependency.explorer.TestUtil.IssueRepository;
import bje.buildtools.dependency.explorer.TestUtil.IssueResult;
import bje.buildtools.dependency.explorer.TestUtil.LocalIssue;
import bje.buildtools.dependency.explorer.TestUtil.LocalIssueLocation;
import bje.buildtools.dependency.explorer.TestUtil.MeasureRepository;
import bje.buildtools.dependency.explorer.maven.MavenExecutor;
import bje.buildtools.dependency.explorer.maven.MavenSettings;
import bje.buildtools.dependency.explorer.util.ExplorationConfiguration;
import bje.buildtools.dependency.explorer.util.InputFileUtils;

class GlobalTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalTest.class);

    private static final ClassLoader classLoader = GlobalTest.class.getClassLoader();

    private static boolean manualCheck = true;

    private static final List<IssueResult> results = Arrays.asList(
            new IssueResult("UnusedDependency", "MINOR", "com.formdev:flatlaf:jar:3.6.1"),
            new IssueResult("UnusedDependency", "MINOR", "io.moquette:moquette-broker:jar:0.16"),
            new IssueResult("UnusedDependency", "MINOR", "org.codehaus.groovy:groovy:jar:3.0.15"),
            new IssueResult("UnusedDependency", "MINOR", "org.eclipse.paho:org.eclipse.paho.client.mqttv3:jar:1.2.4"),
            new IssueResult("UnusedDependency", "MINOR", "org.junit.jupiter:junit-jupiter-engine:jar:5.9.2"),
            new IssueResult("UnusedDependency", "MINOR", "org.mockito:mockito-core:jar:5.1.1"),
            new IssueResult("UnusedDependency", "MINOR", "org.mockito:mockito-junit-jupiter:jar:5.1.1"),
            new IssueResult("UnusedDependency", "MINOR", "org.rxtx:rxtx:jar:2.1.7"),
            new IssueResult("UsingIncoherentVersionnedDependency", "MINOR",
                    "com.fasterxml.jackson.core:jackson-annotations"),
            new IssueResult("UsingIncoherentVersionnedDependency", "MINOR",
                    "com.fasterxml.jackson.core:jackson-databind"),
            new IssueResult("UsingIncoherentVersionnedDependency", "MINOR", "io.dropwizard.metrics:metrics-core"),
            new IssueResult("UsingIncoherentVersionnedDependency", "MINOR", "org.slf4j:slf4j-api"),
            new IssueResult("UsingOutdatedDependency", "MINOR", "org.mockito:mockito-core:5.1.1"),
            new IssueResult("UsingOutdatedDependency", "MINOR", "org.mockito:mockito-junit-jupiter:5.1.1"),
            new IssueResult("UsingOutdatedDependency", "MAJOR", "org.junit.jupiter:junit-jupiter-engine:5.9.2"));

    private static final String SEPARATOR = FileSystems.getDefault().getSeparator();

    private static final Path tmpDir = Path.of(System.getProperty("java.io.tmpdir"));

    @AfterAll
    static void cleanUp() throws IOException {
        final var targetDir = tmpDir.resolve("dependency-explorer-sonar-plugin");
        TestUtil.deleteRecursively(targetDir);
    }

    @BeforeAll
    static void createFiles() throws IOException {
        cleanUp();
        final var mc = System.getenv("manualCheck");
        if (mc != null) {
            manualCheck = Boolean.parseBoolean(mc);
        }
        final var mainDir = Path.of(classLoader.getResource("mainProject").getFile());
        final var otherDir = Path.of(classLoader.getResource("otherProject").getFile());
        final var targetDir = tmpDir.resolve("dependency-explorer-sonar-plugin");
        Files.createDirectories(targetDir);
        TestUtil.copy(mainDir, targetDir);
        TestUtil.copy(otherDir, targetDir);
        LOGGER.info("Use directory {}", targetDir.toAbsolutePath());
    }

    @Test
    void main() throws Exception {
        final var measures = test("mainProject");

        final var loc = measures.getMeasure(ExplorerMetrics.REPORT).get(0);
        final var result = new JSONArray(loc.getValue());

        final var resFile = Path.of(classLoader.getResource("data.json").getFile());
        final var res = new String(Files.readAllBytes(resFile), StandardCharsets.UTF_8);
        if (manualCheck) {
            assertEquals(res, result.toString() + "\n");
        }
        assertTrue(JSONEqualityUtil.equality(new JSONArray(res), result));
    }

    @Test
    void other() throws Exception {
        test("otherProject");
    }

    private MeasureRepository test(final String project) throws IOException {
        final var sensor = new ExplorerSensor();
        final var prop = new Properties();
        final var conf = TestUtil.getConfiguration(prop);
        final var config = ExplorationConfiguration.of(conf);
        final var msettings = MavenSettings.fromCommandLine();
        final var baseFile = Path.of(tmpDir.toAbsolutePath().toString() + SEPARATOR + "dependency-explorer-sonar-plugin"
                + SEPARATOR + project + SEPARATOR + "pom.xml");
        assertEquals(0, MavenExecutor.invoke(baseFile, msettings, config, "install"),
                "Can't install test project");
        final var issues = new IssueRepository();
        final var measures = new MeasureRepository();
        sensor.execute(conf,
                t -> InputFileUtils.loadFile(tmpDir.toAbsolutePath().toString() + SEPARATOR
                        + "dependency-explorer-sonar-plugin" + SEPARATOR + project + SEPARATOR + t),
                issues::newIssue, measures::newMeasure);

        issues.sort();
        for (final LocalIssue ir : issues.getIssues()) {
            final var loc = (LocalIssueLocation) ir.getPrimaryLocation();
            LOGGER.info("{} - {} - {} - {}", ir.getRuleKey().rule(), ir.getSeverity().name(), loc.getMessage(),
                    loc.getComponent());
        }
        assertEquals(results.size(), issues.getSize());
        final var ia = issues.getIssues().iterator();
        final var ib = results.iterator();
        while (ia.hasNext() && ib.hasNext()) {
            final var li = ia.next();
            final var ri = ib.next();
            assertEquals(ri.key, li.getRuleKey().rule());
            assertEquals(ri.severity, li.getSeverity().name());
            final var loc = (LocalIssueLocation) li.getPrimaryLocation();
            assertTrue(loc.getMessage().contains(ri.desc), "missing " + ri.desc + " in " + loc.getMessage());
        }
        assertFalse(ia.hasNext());
        assertFalse(ib.hasNext());
        assertEquals(1, measures.getSize());
        return measures;
    }
}
