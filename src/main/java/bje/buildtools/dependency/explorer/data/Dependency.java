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

import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import bje.buildtools.dependency.explorer.license.LicenseDefinition;
import bje.buildtools.dependency.explorer.util.Utils;

public class Dependency extends Artifact {
    private static int compareDefinitions(final Set<Dependency> a, final Set<Dependency> b) {
        final var aa = a.iterator();
        final var bb = b.iterator();
        while (aa.hasNext() || bb.hasNext()) {
            final var i = aa.next().compareTo(bb.next());
            if (i != 0) {
                return i;
            }
        }
        if (aa.hasNext()) {
            return 1;
        }
        return bb.hasNext() ? -1 : 0;
    }

    private final Set<Dependency> children = new TreeSet<>();
    private LicenseDefinition licenses;
    private final Set<String> packages = new TreeSet<>();
    private Dependency parent;
    private final Pom project;
    private DependencyType source = DependencyType.DEPENDENCY;

    private final NavigableSet<Version> versions = new TreeSet<>();

    public Dependency(final Pom pom, final Artifact anArtifact) {
        super(anArtifact.getGroupId(), anArtifact.getArtifactId(), anArtifact.getVersion());
        range = anArtifact.getRange();
        scope = anArtifact.getScope();
        effectiveVersion = anArtifact.getEffectiveVersion();
        propertyName = anArtifact.getPropertyName();
        type = anArtifact.getType();
        project = pom;
    }

    public void addDependency(final Dependency node) {
        if (node != null) {
            Dependency toDelete = null;
            final var ev = new Version(node.getEffectiveVersion());
            for (final Dependency def : children) {
                if (def.isSameArtefact(node)) {
                    if (ev.compareTo(new Version(def.getEffectiveVersion())) <= 0) {
                        return;
                    }
                    toDelete = def;
                    break;
                }
            }
            if (toDelete != null) {
                children.remove(toDelete);
            }
            node.setParent(this);
            children.add(node);
        }
    }

    @Override
    public int compareTo(final Artifact o) {
        var i = super.compareTo(o);
        if (i != 0) {
            return i;
        }
        if (o instanceof final Dependency d) {
            i = source.compareTo(d.source);
            if (i == 0) {
                i = compareDefinitions(children, d.children);
            }
            if (i == 0) {
                i = Utils.compareIterable(packages, d.packages);
            }
            if (i == 0) {
                i = Utils.compareIterable(versions, d.versions);
            }
        }
        return i;
    }

    public void declarePackage(final String s) {
        packages.add(s);
    }

    @Override
    public boolean equals(final Object o) {
        return super.equals(o);
    }

    public Set<Dependency> getChildren() {
        return children;
    }

    @Override
    public String getEffectiveVersion() {
        if (effectiveVersion == null && version != null) {
            if (version.startsWith("${")) {
                return project.resolveProperty(version);
            }
            return version;
        }
        return effectiveVersion;
    }

    public Version getLastVersion() {
        final var set = getUpdatesVersions();
        if (set.isEmpty()) {
            return null;
        }
        return set.last();
    }

    public LicenseDefinition getLicenses() {
        return licenses;
    }

    public Version getNextVersion() {
        final var set = getUpdatesVersions();
        if (set.isEmpty()) {
            return null;
        }
        return set.first();
    }

    public Set<String> getPackages() {
        return packages;
    }

    public Dependency getParent() {
        return parent;
    }

    @Override
    public String getPropertyName() {
        if (propertyName == null) {
            if (version != null && version.startsWith("${")) {
                return version;
            }
            return null;
        }
        return propertyName;
    }

    public DependencyType getSource() {
        return source;
    }

    public SortedSet<Version> getUpdatesVersions() {
        return versions.tailSet(new Version(getEffectiveVersion()));
    }

    @Override
    public String getVersion() {
        if (version != null && version.startsWith("${")) {
            return getEffectiveVersion();
        }
        return version;
    }

    public NavigableSet<Version> getVersions() {
        return versions;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    private boolean isSameArtefact(final Dependency node) {
        return Objects.equals(groupId, node.groupId) && Objects.equals(artifactId, node.artifactId);
    }

    private void printLicense(final StringBuilder sb) {
        if (licenses != null && !licenses.getComposition().isEmpty()) {
            sb.append(" [");
            final var iter = licenses.getComposition().iterator();
            while (iter.hasNext()) {
                sb.append(iter.next());
                if (iter.hasNext()) {
                    sb.append(", ");
                }
            }
            sb.append("]");
        }
    }

    private void printVersion(final StringBuilder sb) {
        if (!versions.isEmpty()) {
            sb.append(" [");
            final var iter = versions.iterator();
            while (iter.hasNext()) {
                sb.append(iter.next());
                if (iter.hasNext()) {
                    sb.append(", ");
                }
            }
            sb.append("]");
        }
    }

    public void setLicenses(final LicenseDefinition l) {
        if (licenses == null || licenses.getComposition().isEmpty()) {
            licenses = l;
        }
    }

    private void setParent(final Dependency myDependency) {
        parent = myDependency;
    }

    public void setSource(final DependencyType dependencyManagement) {
        source = dependencyManagement;
    }

    public String toGAeV() {
        final var sb = new StringBuilder(512);
        sb.append(groupId);
        sb.append(":");
        sb.append(artifactId);
        sb.append(":");
        sb.append(getEffectiveVersion());
        return sb.toString();
    }

    public String toGAPeV() {
        final var sb = new StringBuilder(512);
        sb.append(groupId);
        sb.append(":");
        sb.append(artifactId);
        sb.append(":jar:");
        if (type != null) {
            sb.append(type);
            sb.append(":");
        }
        sb.append(getEffectiveVersion());
        return sb.toString();
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder(512);
        fillGAV(sb);
        sb.append(" (");
        sb.append(source.name());
        sb.append(")");
        if (!versions.isEmpty() || licenses != null && !licenses.getComposition().isEmpty()) {
            sb.append(" ->");
        }
        printVersion(sb);
        printLicense(sb);
        return sb.toString();
    }
}
