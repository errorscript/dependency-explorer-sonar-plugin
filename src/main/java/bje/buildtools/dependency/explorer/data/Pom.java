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
package bje.buildtools.dependency.explorer.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;

import bje.buildtools.dependency.explorer.license.LicenseDefinition;
import bje.buildtools.dependency.explorer.maven.PomParser.FirstParse;

public class Pom implements Comparable<Pom> {

    private static void print(final Appendable out, final int deep, final Dependency root, final String prefix,
            final String level) throws IOException {
        out.append(prefix + level + root.toGAV() + "\n");
        var next = level.startsWith("\\") ? "   " : "|  ";
        if (deep == 0) {
            next = "";
        }
        final var iter = root.getChildren().iterator();
        while (iter.hasNext()) {
            final var child = iter.next();
            if (iter.hasNext()) {
                print(out, deep + 1, child, prefix + next, "+- ");
            } else {
                print(out, deep + 1, child, prefix + next, "\\- ");
            }
        }
    }

    private final InputFile component;
    private final Map<String, Dependency> dependencyMap = new TreeMap<>();
    private Map<String, List<Dependency>> doublons;
    private final Map<Scope, List<Artifact>> effectiveArtifacts = new EnumMap<>(Scope.class);
    private final Map<Scope, List<Artifact>> undeclaredArtifacts = new EnumMap<>(Scope.class);
    private final Map<Scope, List<Artifact>> unusedArtifacts = new EnumMap<>(Scope.class);
    private final List<String> modules = new ArrayList<>();
    private String name;
    private Pom parent;
    private final Map<String, Dependency> pluginMap = new TreeMap<>();
    private final Map<String, FiledRange> propertyRange = new TreeMap<>();
    private Dependency root;
    private final POMType type;
    private final Map<Artifact, List<String>> incompatibility = new HashMap<>();

    public Pom(final InputFile aComponent, final Pom aParent, final POMType aType)
            throws IllegalStateException {
        component = aComponent;
        type = aType;
        name = null;
        parent = aParent;
        root = new Dependency(this, new Artifact(null, null, null));
    }

    public Pom(final InputFile aComponent, final POMType type) throws IllegalStateException {
        this(aComponent, null, type);
    }

    public Dependency addDependency(final Artifact anArtifact, final boolean management) {
        return addDependency(anArtifact, Collections.emptyList(),
                management ? DependencyType.DEPENDENCY_MANAGEMENT : DependencyType.DEPENDENCY);
    }

    public Dependency addDependency(final Artifact artifact, final List<String> versions, final DependencyType source) {
        final var map = switch (source) {
        case DEPENDENCY, DEPENDENCY_MANAGEMENT -> dependencyMap;
        case PLUGIN, PLUGIN_MANAGEMENT -> pluginMap;
        default -> null;
        };
        if (map != null) {
            var def = map.get(artifact.toGA());
            if (def == null || def.getVersion() == null) {
                if (!source.isManaged()) {
                    def = getAnyDependency(artifact.toGA());
                }
                if (def == null) {
                    def = new Dependency(this, artifact);
                    def.setSource(source);
                }
                map.putIfAbsent(def.toGA(), def);
            }
            for (final String s : versions) {
                def.getVersions().add(new Version(s));
            }
            if (artifact.getVersion() != null && artifact.getVersion().startsWith("${")) {
                def.setPropertyName(artifact.getVersion());
            }
            return def;
        }
        return null;
    }

    public void addEffectiveDependency(final Artifact artifact) {
        final var list = effectiveArtifacts.computeIfAbsent(Scope.of(artifact.scope), s -> new ArrayList<>());
        list.add(artifact);
    }

    public void addModule(final String module) {
        modules.add(module);
    }

    public Dependency addPlugin(final Artifact anArtifact, final boolean management) {
        final var def = new Dependency(this, anArtifact);
        def.setSource(management ? DependencyType.PLUGIN_MANAGEMENT : DependencyType.PLUGIN);
        pluginMap.putIfAbsent(def.toGA(), def);
        return def;
    }

    public void addPropertyLocation(final String substring, final FiledRange data) {
        if (substring.startsWith("${")) {
            propertyRange.putIfAbsent(substring, data);
        } else {
            propertyRange.putIfAbsent("${" + substring + "}", data);
        }
    }

    public void addUndeclaredDependency(final Artifact artifact) {
        final var list = undeclaredArtifacts.computeIfAbsent(Scope.of(artifact.scope), s -> new ArrayList<>());
        list.add(artifact);
    }

    public void addUnusedDependency(final Artifact artifact) {
        final var list = unusedArtifacts.computeIfAbsent(Scope.of(artifact.scope), s -> new ArrayList<>());
        list.add(artifact);
    }

    public void addVersionIncompatibility(final Artifact def, final List<String> versions) {
        incompatibility.put(def, versions);
    }

    @Override
    public int compareTo(final Pom o) {
        return name.compareTo(o.name);
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof final Pom c) {
            return Objects.equals(name, c.name);
        }
        return false;
    }

    public void fill(final Artifact fp) {
        name = fp.getArtifactId();
        var version = fp.getVersion();
        if (version != null && version.startsWith("${")) {
            version = resolveProperty(fp.getVersion());
        }
        if (version == null) {
            version = parent.getRoot().getVersion();
        }
        var group = fp.getGroupId();
        if (group == null) {
            group = parent.getRoot().getGroupId();
        }
        root = new Dependency(this, new Artifact(group, name, version));
    }

    public void fill(final FirstParse fp) {
        fill(new Artifact(fp.getGroupId(), fp.getArtifactId(), fp.getVersion()));
    }

    public Dependency getAnyDependency(final String ga) {
        if (ga.equals(root.toGA())) {
            return root;
        }
        var def = dependencyMap.get(ga);
        if (def == null) {
            def = pluginMap.get(ga);
        }
        if (parent != null && (def == null || def.getVersion() == null)) {
            final var def2 = parent.getAnyDependency(ga);
            if (def2 != null) {
                def = def2;
            }
        }
        return def;
    }

    public Dependency getAnyDependency(final String groupId, final String artifactId) {
        final var ga = groupId + ":" + artifactId;
        return getAnyDependency(ga);
    }

    public InputFile getComponent() {
        return component;
    }

    public Map<String, List<Dependency>> getDoublons() {
        return doublons == null ? Collections.emptyMap() : doublons;
    }

    public Map<Scope, List<Artifact>> getEffectiveDependency() {
        return effectiveArtifacts;
    }

    private LicenseDefinition getLicenses() {
        return root.getLicenses();
    }

    public Map<String, Dependency> getMapDependencies() {
        return dependencyMap;
    }

    public Map<String, Dependency> getMapPlugins() {
        return pluginMap;
    }

    public List<String> getModules() {
        return modules;
    }

    public String getName() {
        return name;
    }

    public Map<String, FiledRange> getPropertiesLocation() {
        return propertyRange;
    }

    public Dependency getRoot() {
        return root;
    }

    private TextRange getTextRange(final Dependency def) {
        FiledRange range = null;
        final var prop = def.getPropertyName();
        if (prop != null) {
            final var d = propertyRange.get(prop);
            if (d == null) {
                range = def.range;
            } else {
                range = d;
            }
        } else {
            range = def.range;
        }
        if (range != null && range.getFile().equals(component)) {
            return range.getTextRange();
        }
        return null;
    }

    public TextRange getTextRange(final String groupId, final String artifactId) {
        final var def = getAnyDependency(groupId, artifactId);
        if (def != null) {
            return getTextRange(def);
        }
        return null;
    }

    public POMType getType() {
        return type;
    }

    public Map<Scope, List<Artifact>> getUndeclaredDependency() {
        return undeclaredArtifacts;
    }

    public Map<Scope, List<Artifact>> getUnusedDependency() {
        return unusedArtifacts;
    }

    public Map<Artifact, List<String>> getVersionIncompatibility() {
        return incompatibility;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    private Map<String, List<Dependency>> prepareMap() {
        final Map<String, List<Dependency>> doublonsMap = new TreeMap<>();
        final Deque<Dependency> stack = new LinkedList<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            final var m = stack.poll();
            final var list = doublonsMap.computeIfAbsent(m.getGroupId() + ":" + m.getArtifactId(),
                    k -> new ArrayList<>());
            list.add(m);
            stack.addAll(m.getChildren());
        }
        return doublonsMap;
    }

    public void printTree(final Appendable out) throws IOException {
        out.append("--------------------------------------------------------------------------------\n");
        out.append(" DEPENDENCY TREE\n");
        out.append("--------------------------------------------------------------------------------\n");
        print(out, 0, root, "", "");
        out.append("\n");
    }

    public String resolveProperty(final String version) {
        if ("${project.version}".equals(version)) {
            return root.getVersion();
        }
        final var dt = propertyRange.get(version);
        if (dt != null) {
            return dt.getText();
        }
        if (parent != null) {
            return parent.resolveProperty(version);
        }
        return null;
    }

    public void setParent(final Pom p) {
        parent = p;
    }

    public void updateRoot() {
        if (parent != null && root.getLicenses() == null) {
            root.setLicenses(parent.getLicenses());
        }
        doublons = prepareMap();
    }

    public void updateRoot(final Dependency defMain) {
        root = defMain;
        updateRoot();
    }

}
