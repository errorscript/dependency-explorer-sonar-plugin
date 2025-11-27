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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import bje.buildtools.dependency.explorer.data.Artifact;
import bje.buildtools.dependency.explorer.data.Pom;
import bje.buildtools.dependency.explorer.maven.MavenExecutor;
import bje.buildtools.dependency.explorer.util.ExplorationConfiguration;

public class DependencyAnalysis {
    public interface ArtifactUse {
        void set(final Artifact a);
    }

    public static void analyse(final MavenExecutor executor) {
        executor.addPlugin(DependencyAnalysis::parse, "target/site/dependency-analysis.html",
                "dependency:analyze-report");
    }

    public static boolean parse(final Path file, final Pom pom, final ExplorationConfiguration config)
            throws IOException {
        final var document = Jsoup.parse(file.toFile());
        var found = false;
        found |= parseArray(document, pom::addEffectiveDependency, "a#Used_and_Declared_Dependencies",
                "a[name=\"Used_and_declared_dependencies\"]");
        found |= parseArray(document, pom::addUndeclaredDependency, "a#Used_but_Undeclared_Dependencies",
                "a[name=\"Used_but_undeclared_dependencies\"]");
        found |= parseArray(document, pom::addUnusedDependency, "a#Unused_but_Declared_Dependencies",
                "a[name=\"Unused_but_declared_dependencies\"]");
        return found;
    }

    private static boolean parseArray(final Document document, final ArtifactUse use, final String... tags) {
        var done = false;
        for (final String tag : tags) {
            final var e = document.select(tag);
            if (e.isEmpty()) {
                continue;
            }
            var p = e.get(0).parent();
            var tables = p == null ? new Elements() : p.getElementsByTag("table");
            if (tables.isEmpty()) {
                p = p == null ? null : p.parent();
                tables = p == null ? new Elements() : p.getElementsByTag("table");
            }
            if (!tables.isEmpty()) {
                done |= readTable(use, tables.get(0));
            }
        }
        return done;
    }

    private static boolean readTable(final ArtifactUse use, final Element table) {
        var done = false;
        final var rows = table.select("tr");
        final var length = rows.size();
        for (var i = 1; i < length; ++i) {
            final var row = rows.get(i);
            final var cols = row.select("td");
            final var groupId = cols.get(0).text();
            final var artifactId = cols.get(1).text();
            final var version = cols.get(2).text();
            final var scope = cols.get(3).text();
            final var type = cols.get(5).text();
            final var artifact = new Artifact(groupId, artifactId, version);
            artifact.setScope(scope);
            artifact.setType(type);
            use.set(artifact);
            done = true;
        }
        return done;
    }

    private DependencyAnalysis() {
        // Block default constructor
    }
}
