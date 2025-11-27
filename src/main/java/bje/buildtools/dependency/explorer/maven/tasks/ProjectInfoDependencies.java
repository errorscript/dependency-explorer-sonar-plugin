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
import java.util.Arrays;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import bje.buildtools.dependency.explorer.ExplorerSensor;
import bje.buildtools.dependency.explorer.data.Artifact;
import bje.buildtools.dependency.explorer.data.Pom;
import bje.buildtools.dependency.explorer.data.Scope;
import bje.buildtools.dependency.explorer.license.LicenseDefinition;
import bje.buildtools.dependency.explorer.maven.MavenExecutor;
import bje.buildtools.dependency.explorer.util.ExplorationConfiguration;

public class ProjectInfoDependencies {
    public static void analyse(final MavenExecutor executor) {
        executor.addPlugin(ProjectInfoDependencies::parse, "target/reports/dependencies.html",
                "project-info-reports:dependencies");
    }

    public static boolean parse(final Path file, final Pom pom, final ExplorationConfiguration config)
            throws IOException {
        final var document = Jsoup.parse(file.toFile());
        var found = false;
        for (final String s : Arrays.asList("Project_Dependencies_", "Project_Transitive_Dependencies_")) {
            for (final Scope scope : Scope.values()) {
                found |= parseArray(pom, document, "a#" + s + scope.name().toLowerCase(), scope);
            }
        }
        return found;
    }

    private static boolean parseArray(final Pom pom, final Document document, final String tag, final Scope scope) {
        final var e = document.select(tag);
        if (e.isEmpty()) {
            return false;
        }
        var done = false;
        final var p = e.get(0).parent();
        final var tables = p == null ? new Elements() : p.getElementsByTag("table");
        if (!tables.isEmpty()) {
            done |= parseArray(pom, scope, tables.get(0));
        }
        return done;
    }

    private static boolean parseArray(final Pom pom, final Scope scope, final Element table) {
        var done = false;
        final var rows = table.select("tr");
        final var length = rows.size();
        for (var i = 1; i < length; ++i) {
            final var row = rows.get(i);
            final var cols = row.select("td");
            final var groupId = cols.get(0).text();
            final var artifactId = cols.get(1).text();
            final var n = cols.size() == 5 ? 0 : 1;
            final var version = cols.get(2).text();
            final var type = cols.get(3 + n).text();
            final var licenses = cols.get(4 + n).text();
            final var artifact = new Artifact(groupId, artifactId, version);
            artifact.setScope(scope.name());
            artifact.setType(type);
            final var def = pom.addDependency(artifact, false);
            final var li = ExplorerSensor.LICENSE_MODEL.get().getLicense(licenses);
            if (def != null && li != null && !li.isEmpty()) {
                def.setLicenses(new LicenseDefinition(licenses, li));
            }
            done = true;
        }
        return done;
    }

    private ProjectInfoDependencies() {
        // Block default constructor
    }

}
