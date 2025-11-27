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

import static bje.buildtools.dependency.explorer.util.Constants.COHERENCE_SKIP_DEFAULT;
import static bje.buildtools.dependency.explorer.util.Constants.COHERENCE_SKIP_PROPERTY;
import static bje.buildtools.dependency.explorer.util.Constants.LICENSE_SKIP_DEFAULT;
import static bje.buildtools.dependency.explorer.util.Constants.LICENSE_SKIP_PROPERTY;
import static bje.buildtools.dependency.explorer.util.Constants.TRANSITIVE_SKIP_DEFAULT;
import static bje.buildtools.dependency.explorer.util.Constants.TRANSITIVE_SKIP_PROPERTY;
import static bje.buildtools.dependency.explorer.util.Constants.UNUSED_SKIP_DEFAULT;
import static bje.buildtools.dependency.explorer.util.Constants.UNUSED_SKIP_PROPERTY;
import static bje.buildtools.dependency.explorer.util.Constants.UPDATE_SKIP_DEFAULT;
import static bje.buildtools.dependency.explorer.util.Constants.UPDATE_SKIP_PROPERTY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Configuration;

import bje.buildtools.dependency.explorer.data.Pom;
import bje.buildtools.dependency.explorer.data.POMType;
import bje.buildtools.dependency.explorer.data.ProtoIssue;
import bje.buildtools.dependency.explorer.data.Result;

public class ProjectParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectParser.class);

    private static void removeDuplicate(final Result main, final Result r) {
        if (main != null) {
            final Set<ProtoIssue> list = new TreeSet<>();
            for (final ProtoIssue issue : r) {
                if (!main.isIssuePresent(issue)) {
                    list.add(issue);
                }
            }
            r.updateIssues(list);
        }
    }

    private final List<Analyzer> analyzers = new ArrayList<>();

    public ProjectParser(final Configuration config) {
        if (!config.getBoolean(COHERENCE_SKIP_PROPERTY).orElse(COHERENCE_SKIP_DEFAULT)) {
            LOGGER.debug("Versions coherence analyzer is scheduled");
            analyzers.add(new VersionsAnalyzer(config));
        }
        if (!config.getBoolean(LICENSE_SKIP_PROPERTY).orElse(LICENSE_SKIP_DEFAULT)) {
            LOGGER.debug("Licenses analyzer is scheduled");
            analyzers.add(new LicensesAnalyser(config));
        }
        if (!config.getBoolean(UPDATE_SKIP_PROPERTY).orElse(UPDATE_SKIP_DEFAULT)) {
            LOGGER.debug("Version updates analyzer is scheduled");
            analyzers.add(new UpdatesAnalyzer(config));
        }
        if (!config.getBoolean(UNUSED_SKIP_PROPERTY).orElse(UNUSED_SKIP_DEFAULT)) {
            LOGGER.debug("Unused dependency analyzer is scheduled");
            analyzers.add(new UnusedAnalyzer(config));
        }
        if (!config.getBoolean(TRANSITIVE_SKIP_PROPERTY).orElse(TRANSITIVE_SKIP_DEFAULT)) {
            LOGGER.debug("Transitive dependency usage analyzer is scheduled");
            analyzers.add(new TransitiveAnalyzer(config));
        }
    }

    public List<Analyzer> getAnalyzer() {
        return Collections.unmodifiableList(analyzers);
    }

    public List<Result> parse(final Pom... compileFiles) {
        Pom mainPom = null;
        final List<Pom> poms = new ArrayList<>();
        for (final Pom pom : compileFiles) {
            if (pom.getType() == POMType.MAIN) {
                mainPom = pom;
            } else {
                poms.add(pom);
            }
        }

        final List<Result> list = new ArrayList<>();
        for (final Analyzer analyzer : analyzers) {
            final var main = analyzer.analyze(mainPom);
            if (main != null) {
                list.add(main);
            }
            for (final Pom pom : poms) {
                final var r = analyzer.analyze(pom);
                if (r != null) {
                    removeDuplicate(main, r);
                    list.add(r);
                }
            }
        }

        return list;
    }
}
