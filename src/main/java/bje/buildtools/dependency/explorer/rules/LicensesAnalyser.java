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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.sonar.api.batch.rule.Severity;
import org.sonar.api.config.Configuration;

import bje.buildtools.dependency.explorer.ExplorerSensor;
import bje.buildtools.dependency.explorer.data.AbstractCulsteredProtoIssue;
import bje.buildtools.dependency.explorer.data.AbstractModifiableResult;
import bje.buildtools.dependency.explorer.data.Pom;
import bje.buildtools.dependency.explorer.data.Dependency;
import bje.buildtools.dependency.explorer.data.ProtoIssue;
import bje.buildtools.dependency.explorer.data.Result;
import bje.buildtools.dependency.explorer.license.Compatibility;
import bje.buildtools.dependency.explorer.license.LicenseDefinition;
import bje.buildtools.dependency.explorer.license.LicenseIdentity;
import bje.buildtools.dependency.explorer.util.Constants;

public class LicensesAnalyser implements Analyzer {
    private static class LicenseIssue extends AbstractCulsteredProtoIssue {

        private final LicenseResult upd;
        private final Severity problemSeverity;

        public LicenseIssue(final Pom c, final LicenseResult u, final Severity severity) {
            super(c, u::getGroupId, u::getArtifactId);
            upd = u;
            problemSeverity = severity;
        }

        @Override
        public String getDescription() {
            return upd.getCompatibility().getDescription();
        }

        @Override
        public String getRuleKey() {
            return Constants.LICENSE_RULE_KEY;
        }

        @Override
        public Severity getSeverity() {
            if (upd.getCompatibility().isProblematic()) {
                return problemSeverity;
            }
            return Severity.INFO;
        }
    }

    public static class LicenseResult implements Comparable<LicenseResult> {

        private final String artifactId;
        private Compatibility compatibility;
        private final String gav;
        private final String groupId;
        private final LicenseDefinition rootLicenses;
        private final Map<String, LicenseDefinition> versionMap = new TreeMap<>();

        public LicenseResult(final String aGroupId, final String anArtifactId, final String key,
                final LicenseDefinition aRootLicenses) {
            gav = key;
            groupId = aGroupId;
            artifactId = anArtifactId;
            rootLicenses = aRootLicenses;
        }

        public void addLicenses(final String baseVersion, final LicenseDefinition licenses) {
            if (licenses != null) {
                versionMap.put(baseVersion, licenses);
            }
        }

        @Override
        public int compareTo(final LicenseResult o) {
            return gav.compareTo(o.gav);
        }

        @Override
        public boolean equals(final Object o) {
            if (o instanceof final LicenseResult l) {
                return Objects.equals(gav, l.gav);
            }
            return false;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public Compatibility getCompatibility() {
            if (compatibility == null) {
                compatibility = ExplorerSensor.LICENSE_MODEL.get().compatibility(rootLicenses, versionMap,
                        groupId + ":" + artifactId);
            }
            return compatibility;
        }

        public String getGav() {
            return gav;
        }

        public String getGroupId() {
            return groupId;
        }

        public Set<Entry<String, LicenseDefinition>> getLicencesByVersion() {
            return versionMap.entrySet();
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(gav);
        }
    }

    public static class LicenseResults extends AbstractModifiableResult {
        public static LicenseResults of(final Pom c, final Set<LicenseResult> aList, final Severity s) {
            final Set<ProtoIssue> set = new TreeSet<>();
            for (final LicenseResult r : aList) {
                set.add(new LicenseIssue(c, r,s ));
            }
            return new LicenseResults(c, set, aList);
        }

        private final Set<LicenseResult> resultList;

        private LicenseResults(final Pom c, final Set<ProtoIssue> s, final Set<LicenseResult> aList) {
            super(s, c);
            resultList = aList;
        }

        @Override
        public void print(final Appendable out) throws IOException {
            out.append("--------------------------------------------------------------------------------\n");
            out.append(" LICENSES CHECK\n");
            out.append("--------------------------------------------------------------------------------\n");
            for (final LicenseResult licence : resultList) {
                out.append(licence.groupId + ":" + licence.getArtifactId());
                out.append("\n");
                for (final Entry<String, LicenseDefinition> n : licence.getLicencesByVersion()) {
                    out.append("+- ");
                    out.append(n.getKey());
                    out.append("  [");
                    var second = false;
                    for (final LicenseIdentity license : n.getValue().getComposition()) {
                        if (second) {
                            out.append(", ");
                        }
                        second = true;
                        out.append(license.getName());
                    }
                    out.append("]\n");
                }
            }

        }
    }

    private final Severity problemSeverity;

    public LicensesAnalyser(final Configuration config) {
        problemSeverity = Severity.valueOf(config.get(Constants.COHERENCE_PATCH_SEVERITY_PROPERTY)
                .orElse(Constants.COHERENCE_PATCH_SEVERITY_DEFAULT));
    }

    private static LicenseResult computeLicences(final LicenseDefinition rootLicenses, final List<Dependency> l) {
        LicenseResult lr = null;
        for (final Dependency d : l) {
            if (d.getVersion() != null) {
                if (lr == null) {
                    lr = new LicenseResult(d.getGroupId(), d.getArtifactId(), d.toGAeV(), rootLicenses);
                }
                lr.addLicenses(d.getVersion(), d.getLicenses());
            }
        }
        return lr;
    }

    @Override
    public Result analyze(final Pom pom) {
        final var rootLicenses = pom.getRoot().getLicenses();
        final Set<LicenseResult> list = new TreeSet<>();
        for (final Entry<String, List<Dependency>> e : pom.getDoublons().entrySet()) {
            final var l = e.getValue();
            if (!l.isEmpty()) {
                final var lr = computeLicences(rootLicenses, l);
                if (lr != null && lr.getCompatibility().isProblematic()) {
                    list.add(lr);
                }
            }
        }

        return LicenseResults.of(pom, list, problemSeverity);
    }
}
