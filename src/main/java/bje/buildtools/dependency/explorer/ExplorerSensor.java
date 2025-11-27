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

import static bje.buildtools.dependency.explorer.util.Constants.C_REPOSITORY_KEY;
import static bje.buildtools.dependency.explorer.util.Constants.C_SENSOR_NAME;
import static bje.buildtools.dependency.explorer.util.Constants.GENERAL_PRINT_DEFAULT;
import static bje.buildtools.dependency.explorer.util.Constants.GENERAL_PRINT_PROPERTY;
import static bje.buildtools.dependency.explorer.util.Constants.GENERAL_SKIP_DEFAULT;
import static bje.buildtools.dependency.explorer.util.Constants.GENRAL_SKIP_PROPERTY;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.Severity;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.measure.NewMeasure;
import org.sonar.api.config.Configuration;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.scanner.sensor.ProjectSensor;
import org.xml.sax.SAXException;

import bje.buildtools.dependency.explorer.data.Pom;
import bje.buildtools.dependency.explorer.data.ProtoIssue;
import bje.buildtools.dependency.explorer.data.Result;
import bje.buildtools.dependency.explorer.license.LicenseModel;
import bje.buildtools.dependency.explorer.license.LicenseParser;
import bje.buildtools.dependency.explorer.maven.MavenCrawler;
import bje.buildtools.dependency.explorer.rules.ProjectParser;
import bje.buildtools.dependency.explorer.util.ExplorationConfiguration;
import bje.buildtools.dependency.explorer.util.JsonReportGenerator;
import bje.buildtools.dependency.explorer.util.LoggerAppendable;

public class ExplorerSensor implements ProjectSensor {
    @FunctionalInterface
    public interface InputFileCreator {
        InputFile create(final String path);
    }

    @FunctionalInterface
    public interface IssueCreator {
        NewIssue create();
    }

    @FunctionalInterface
    public interface MeasureCreator<T extends Serializable> {
        NewMeasure<T> create();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ExplorerSensor.class);
    public static final AtomicReference<LicenseModel> LICENSE_MODEL = new AtomicReference<>();

    private static void addIssues(final IssueCreator creator, final boolean print, final List<Result> anResultList)
            throws IOException {
        for (final Result result : anResultList) {
            if (print) {
                try (final var a = new LoggerAppendable(LOGGER)) {
                    result.print(a);
                }
            }
            for (final ProtoIssue update : result) {
                final var severity = update.getSeverity();
                if (severity == Severity.INFO) {
                    continue;
                }
                LOGGER.debug("Issue --------------------------------------------------------------------------");
                LOGGER.debug("{} {}", update.getSeverity().name(), update.getRuleKey());
                LOGGER.debug("In module {} for artifact {}", update.getModuleName(), update.getGA());
                LOGGER.debug("Description : {}", update.getDescription());
                final var component = update.getComponent();
                final var issue = creator.create().forRule(RuleKey.of(C_REPOSITORY_KEY, update.getRuleKey()));
                final var desc = update.getDescription();
                final var range = update.getTextRange();
                if (range != null) {
                    final var r = range.start().line() + "," + range.start().lineOffset() + "-" + range.end().line()
                            + "," + range.end().lineOffset();
                    LOGGER.trace("Range for {} in {} : {}", update.getGA(), update.getModuleName(), r);
                    issue.at(issue.newLocation().on(component).at(range).message(desc));
                } else {
                    LOGGER.trace("No range for {} in {}", update.getGA(), update.getModuleName());
                    issue.at(issue.newLocation().on(component).message(desc));
                }
                issue.overrideSeverity(severity).save();
            }
        }
    }

    private static void uploadJsonReport(final MeasureCreator<String> aCreator, final List<JSONObject> anObjectList) {
        LOGGER.trace("Upload Dependency explorer JSON-Report");
        final var arr = new JSONArray();
        for (final JSONObject anObject : anObjectList) {
            arr.put(anObject);
        }
        aCreator.create().forMetric(ExplorerMetrics.REPORT).withValue(arr.toString()).save();
    }

    @Override
    public void describe(final SensorDescriptor aSensorDescriptor) {
        aSensorDescriptor.name(C_SENSOR_NAME);
    }

    public void execute(final Configuration configuration, final InputFileCreator aFileCreator,
            final IssueCreator anIssueCreator, final MeasureCreator<String> aMeasureCreator) {
        if (configuration.getBoolean(GENRAL_SKIP_PROPERTY).orElse(GENERAL_SKIP_DEFAULT)) {
            LOGGER.info("Dependency explorer - skipped");
        } else {
            LOGGER.info("Dependency explorer - Start - rev A");
            try {
                final var model = LicenseParser.init(configuration);
                LICENSE_MODEL.set(model);
            } catch (final ParserConfigurationException | SAXException | IOException e) {
                LOGGER.warn("Cannot instanciate license matrix", e);
            }
            final boolean print = configuration.getBoolean(GENERAL_PRINT_PROPERTY).orElse(GENERAL_PRINT_DEFAULT);
            final var generator = new JsonReportGenerator();
            try {
                final var config = ExplorationConfiguration.of(configuration);
                final var poms = MavenCrawler.compileFiles(aFileCreator, config);
                if (poms.length == 0) {
                    LOGGER.warn("Can't analyse this project, no pom.xml found");
                    return;
                }
                final var parser = new ProjectParser(configuration);
                for (final Pom pom : poms) {
                    if (print) {
                        try (final var a = new LoggerAppendable(LOGGER)) {
                            pom.printTree(a);
                        }
                    }
                }
                final var results = parser.parse(poms);
                addIssues(anIssueCreator, print, results);
                generator.append(results);
                uploadJsonReport(aMeasureCreator, generator.generate());
            } catch (final Exception e) {
                LOGGER.warn("Analysis aborted due to: " + e.getMessage(), e);
            }
            LOGGER.info("Dependency explorer - Stop");
        }
    }

    @Override
    public void execute(final SensorContext aSensorContext) {
        execute(aSensorContext.config(),
                t -> aSensorContext.fileSystem().inputFile(aSensorContext.fileSystem().predicates().hasRelativePath(t)),
                aSensorContext::newIssue, () -> aSensorContext.<String>newMeasure().on(aSensorContext.project()));
    }

    @Override
    public String toString() {
        return C_SENSOR_NAME;
    }
}
