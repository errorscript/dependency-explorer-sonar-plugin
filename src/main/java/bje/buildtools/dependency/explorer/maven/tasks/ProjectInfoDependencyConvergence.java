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

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import bje.buildtools.dependency.explorer.data.Artifact;
import bje.buildtools.dependency.explorer.data.Pom;
import bje.buildtools.dependency.explorer.maven.MavenExecutor;
import bje.buildtools.dependency.explorer.util.ExplorationConfiguration;

public class ProjectInfoDependencyConvergence {
    public static void analyse(final MavenExecutor executor) {
        executor.addPlugin(ProjectInfoDependencyConvergence::parse,
                "target/reports/dependency-convergence.html", "project-info-reports:dependency-convergence");
    }

    public static boolean parse(final Path file, final Pom pom, final ExplorationConfiguration config)
            throws IOException {
        final var document = Jsoup.parse(file.toFile());
        return parseArray(pom, document);
    }

    private static boolean parseArray(final Pom pom, final Document document) {
        final var e = document.select("section section");
        if (e.isEmpty()) {
            return false;
        }
        var done = false;
        final var size = e.size();
        for (var j = 0; j < size; ++j) {
            final var ga = e.get(j).getElementsByTag("h3").text();
            final var tables = e.get(j).select("table table");
            final var table = tables.get(0);
            final var rows = table.select("tr");
            final List<String> versions = new ArrayList<>();
            final var length = rows.size();
            for (var i = 0; i < length; ++i) {
                final var row = rows.get(i);
                final var cols = row.select("td");
                final var version = cols.get(0).text();
                if (!version.contains("-SNAPSHOT")) {
                    versions.add(version);
                }
            }
            if (!versions.isEmpty()) {
                final var r = ga.split(":");
                pom.addVersionIncompatibility(new Artifact(r[0], r[1], null), versions);
                done = true;
            }
        }
        return done;
    }

    private ProjectInfoDependencyConvergence() {
        // Block default constructor
    }

}
