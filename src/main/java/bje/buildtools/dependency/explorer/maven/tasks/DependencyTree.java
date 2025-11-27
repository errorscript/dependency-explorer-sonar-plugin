/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2025-2025 errorscript@gmail.com
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

import bje.buildtools.dependency.explorer.data.Artifact;
import bje.buildtools.dependency.explorer.data.Pom;
import bje.buildtools.dependency.explorer.maven.MavenExecutor;
import bje.buildtools.dependency.explorer.data.Dependency;
import bje.buildtools.dependency.explorer.util.ExplorationConfiguration;

public class DependencyTree {
    private static final int GROUPID = 0;
    private static final int ARTIFACTID = 1;
    private static final int TYPE = 2;
    private static final int VERSION_CLASSIFIER = 4;
    private static final int SCOPE_CLASSIFIER = 5;
    private static final int VERSION = 3;
    private static final int SCOPE = 4;

    public static void analyse(final MavenExecutor executor) {
        executor.addPlugin(DependencyTree::parse, "target/tree.txt", "dependency:tree",
                "-DoutputFile=target/tree.txt");
    }

    private static int computeLevel(final String l) {
        final var length = l.length();
        for (var i = 0; i < length; ++i) {
            if (!Character.isWhitespace(l.charAt(i))) {
                return i;
            }
        }
        return 0;
    }

    private static Dependency getDependency(final Pom pom, final Dependency previous, final String gav) {
        final var d = gav.split(":");
        final var group = d[GROUPID];
        final var artifact = d[ARTIFACTID];
        final var version = d.length > SCOPE_CLASSIFIER ? d[VERSION_CLASSIFIER] : d[VERSION];
        final var def = pom.addDependency(new Artifact(group, artifact, version), false);
        def.setType(d[TYPE]);
        def.setScope(d.length > SCOPE_CLASSIFIER ? d[SCOPE_CLASSIFIER] : d[SCOPE]);
        def.setEffectiveVersion(version);
        previous.addDependency(def);
        return def;
    }

    public static boolean parse(final Path file, final Pom pom, final ExplorationConfiguration config)
            throws IOException {
        var found = false;
        final var lines = Files.readAllLines(file);
        final Map<Integer, Dependency> stack = new TreeMap<>();
        stack.put(0, pom.getRoot());
        for (final var line : lines) {
            var l = line.replace("   ", " ");
            l = l.replace("+- ", " ");
            l = l.replace("|  ", " ");
            l = l.replace("\\- ", " ");
            final var level = computeLevel(l);
            if (level <= 0) {
                continue;
            }
            final var gav = l.substring(level);
            final var previous = stack.get(level - 1);
            final var dependency = getDependency(pom, previous, gav);
            stack.put(level, dependency);
            found = true;
        }
        return found;
    }

    private DependencyTree() {
        // Block default constructor
    }
}
