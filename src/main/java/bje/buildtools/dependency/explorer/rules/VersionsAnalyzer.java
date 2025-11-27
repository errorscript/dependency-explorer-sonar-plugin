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
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.sonar.api.batch.rule.Severity;
import org.sonar.api.config.Configuration;

import bje.buildtools.dependency.explorer.data.AbstractCulsteredProtoIssue;
import bje.buildtools.dependency.explorer.data.AbstractModifiableResult;
import bje.buildtools.dependency.explorer.data.Artifact;
import bje.buildtools.dependency.explorer.data.Pom;
import bje.buildtools.dependency.explorer.data.ProtoIssue;
import bje.buildtools.dependency.explorer.data.Result;
import bje.buildtools.dependency.explorer.data.UpdateLevel;
import bje.buildtools.dependency.explorer.data.Version;
import bje.buildtools.dependency.explorer.util.Constants;
import bje.buildtools.dependency.explorer.util.Utils;

public class VersionsAnalyzer implements Analyzer {
    public static class Caller implements Comparable<Caller> {

        private final String version;

        public Caller(final String baseVersion) {
            version = baseVersion;
        }

        @Override
        public int compareTo(final VersionsAnalyzer.Caller o) {
            if (o == null) {
                return -1;
            }
            final var v1 = version == null ? null : new Version(version);
            final var v2 = o.version == null ? null : new Version(o.version);
            return Utils.compare(v1, v2);
        }

        @Override
        public boolean equals(final Object o) {
            if (o instanceof final Caller c) {
                return Objects.equals(version, c.version);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(version);
        }
    }

    private static class VersionIssue extends AbstractCulsteredProtoIssue {
        private final Map<UpdateLevel, Severity> locSeverityMap;
        private final VersionResult upd;

        public VersionIssue(final VersionResult u, final Pom c, final Map<UpdateLevel, Severity> sMap) {
            super(c, u::getGroupId, u::getArtifactId);
            upd = u;
            locSeverityMap = sMap;
        }

        @Override
        public String getDescription() {
            final var sb = new StringBuilder(512);
            sb.append("Difference between version of ");
            sb.append(upd.ga);
            sb.append(" is ");
            switch (upd.maxLevel) {
            case MAJOR:
                sb.append("major and can lead to bytecode incompatibilities.");
                break;
            case MINOR:
                sb.append("minor and can lead to strange behaviour.");
                break;
            case NONE:
                return null;
            case PATCH:
                sb.append("minimal but can have vulnerabilities or other border effect.");
                break;
            }
            sb.append(" [");
            final var iter = upd.callers.iterator();
            while (iter.hasNext()) {
                sb.append(iter.next().version);
                if (iter.hasNext()) {
                    sb.append(", ");
                }
            }
            sb.append("]");
            return sb.toString();
        }

        @Override
        public String getGA() {
            return upd.ga;
        }

        @Override
        public String getRuleKey() {
            return Constants.COHERENCE_RULE_KEY;
        }

        @Override
        public Severity getSeverity() {
            return locSeverityMap.get(upd.maxLevel);
        }
    }

    public static class VersionReport extends AbstractModifiableResult {

        public static VersionReport of(final Pom c, final List<VersionResult> l,
                final Map<UpdateLevel, Severity> severityMap) {
            final Set<ProtoIssue> set = new TreeSet<>();
            for (final VersionResult t : l) {
                set.add(new VersionIssue(t, c, severityMap));
            }
            return new VersionReport(c, set, l);
        }

        private final List<VersionResult> list;

        public VersionReport(final Pom c, final Set<ProtoIssue> s, final List<VersionResult> l) {
            super(s, c);
            list = l;
        }

        @Override
        public void print(final Appendable out) throws IOException {
            // versions check
            out.append("--------------------------------------------------------------------------------\n");
            out.append(" MULTIPLE VERSIONS CHECK\n");
            out.append("--------------------------------------------------------------------------------\n");
            for (final VersionResult version : list) {
                out.append(version.ga);
                out.append("\n");
                for (final Caller v : version.callers) {
                    out.append("+- ");
                    out.append(v.version);
                    out.append("\n");
                }
            }
        }
    }

    public static class VersionResult {

        private final String artifactId;
        private final Set<Caller> callers = new TreeSet<>();
        private final String ga;
        private final String groupId;
        private UpdateLevel maxLevel;

        public VersionResult(final String aGroupId, final String anArtifactId, final String key) {
            ga = key;
            groupId = aGroupId;
            artifactId = anArtifactId;
        }

        public void add(final String baseVersion) {
            callers.add(new Caller(baseVersion));
        }

        public String getArtifactId() {
            return artifactId;
        }

        public String getGroupId() {
            return groupId;
        }

        public void setMaxLevel(final UpdateLevel aMaxLevel) {
            maxLevel = aMaxLevel;
        }
    }

    private static UpdateLevel computeMaxLevel(final VersionResult vr) {
        var maxLevel = UpdateLevel.NONE;
        Version previousVersion = null;
        for (final Caller call : vr.callers) {
            final var current = new Version(call.version);
            if (previousVersion != null) {
                final var level = UpdateLevel.diff(previousVersion, current);
                maxLevel = maxLevel.max(level);
                if (current.compareTo(previousVersion) > 0) {
                    previousVersion = current;
                }
            } else {
                previousVersion = current;
            }
        }
        return maxLevel;
    }

    private final Map<UpdateLevel, Severity> severityMap = new EnumMap<>(UpdateLevel.class);

    public VersionsAnalyzer(final Configuration config) {
        severityMap.put(UpdateLevel.MAJOR, Severity.valueOf(config.get(Constants.COHERENCE_MAJOR_SEVERITY_PROPERTY)
                .orElse(Constants.COHERENCE_MAJOR_SEVERITY_DEFAULT)));
        severityMap.put(UpdateLevel.MINOR, Severity.valueOf(config.get(Constants.COHERENCE_MINOR_SEVERITY_PROPERTY)
                .orElse(Constants.COHERENCE_MINOR_SEVERITY_DEFAULT)));
        severityMap.put(UpdateLevel.PATCH, Severity.valueOf(config.get(Constants.COHERENCE_PATCH_SEVERITY_PROPERTY)
                .orElse(Constants.COHERENCE_PATCH_SEVERITY_DEFAULT)));
        severityMap.put(UpdateLevel.NONE, Severity.INFO);
    }

    @Override
    public Result analyze(final Pom pom) {
        final List<VersionResult> list = new ArrayList<>();
        for (final Entry<Artifact, List<String>> e : pom.getVersionIncompatibility().entrySet()) {
            final var d = e.getKey();
            final var vr = new VersionResult(d.getGroupId(), d.getArtifactId(), d.toGA());
            for (final String s : e.getValue()) {
                vr.add(s);
            }
            final var maxLevel = computeMaxLevel(vr);
            vr.setMaxLevel(maxLevel);
            if (maxLevel != UpdateLevel.NONE) {
                list.add(vr);
            }
        }
        return VersionReport.of(pom, list, severityMap);
    }
}
