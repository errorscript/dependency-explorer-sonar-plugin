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
package bje.buildtools.dependency.explorer.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONObject;

import bje.buildtools.dependency.explorer.data.Dependency;
import bje.buildtools.dependency.explorer.data.Pom;
import bje.buildtools.dependency.explorer.data.ProtoIssue;
import bje.buildtools.dependency.explorer.data.Result;
import bje.buildtools.dependency.explorer.license.LicenseIdentity;

public class JsonReportGenerator {
    private static void add(final JSONObject obj, final String key, final Object value) {
        if (value != null) {
            obj.put(key, value.toString());
        }
    }

    private static JSONObject fillDependency(final String name, final Dependency definition, final int level,
            final List<Result> results) {
        final var obj = new JSONObject();
        obj.put("level", level);
        obj.put("groupId", definition.getGroupId());
        obj.put("artifactId", definition.getArtifactId());
        obj.put("version", definition.getEffectiveVersion());
        add(obj, "scope", definition.getScope());
        add(obj, "nextVersion", definition.getNextVersion());
        add(obj, "lastVersion", definition.getLastVersion());
        add(obj, "source", definition.getSource());
        add(obj, "propertyName", definition.getPropertyName());
        if (definition.getLicenses() != null) {
            final var array = new JSONArray();
            for (final LicenseIdentity l : definition.getLicenses().getComposition()) {
                array.put(l.getName());
            }
            if (!array.isEmpty()) {
                obj.put("licenses", array);
            }
        }
        if (!definition.getChildren().isEmpty()) {
            final var arr = new JSONArray();
            for (final Dependency child : definition.getChildren()) {
                arr.put(fillDependency(name, child, level + 1, results));
            }
            obj.put("children", arr);
        }
        return obj;
    }

    private final Map<String, JSONArray> dependenciesObj = new TreeMap<>();
    private final Map<String, JSONArray> issuesObj = new TreeMap<>();

    private final Set<Pom> names = new TreeSet<>();

    public void append(final List<Result> results) {
        Collections.sort(results);
        for (final Result issues : results) {
            final var pom = issues.getPom();
            if (!names.contains(pom)) {
                names.add(pom);
                final var arr = new JSONArray();
                arr.put(fillDependency(pom.getName(), pom.getRoot(), 0, results));
                dependenciesObj.put(pom.getName(), arr);
            }
            final var array = issuesObj.computeIfAbsent(pom.getName(), n -> new JSONArray());
            for (final ProtoIssue issue : issues) {
                final var obj = new JSONObject();
                obj.put("severity", issue.getSeverity().name());
                obj.put("module", issue.getModuleName());
                obj.put("artifact", issue.getGA());
                obj.put("description", issue.getDescription());
                array.put(obj);
            }
        }
    }

    public List<JSONObject> generate() {
        final List<JSONObject> list = new ArrayList<>();
        for (final Pom pom : names) {
            final var name = pom.getName();
            final var obj = new JSONObject();

            final var o = new JSONObject();
            o.put(name, dependenciesObj.get(name));
            obj.put("dependencies", o);
            list.add(obj);
        }
        return list;
    }

}
