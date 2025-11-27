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
import java.util.Set;
import java.util.TreeSet;

import org.sonar.api.batch.rule.Severity;
import org.sonar.api.config.Configuration;

import bje.buildtools.dependency.explorer.data.AbstractCulsteredProtoIssue;
import bje.buildtools.dependency.explorer.data.AbstractModifiableResult;
import bje.buildtools.dependency.explorer.data.Pom;
import bje.buildtools.dependency.explorer.data.Dependency;
import bje.buildtools.dependency.explorer.data.ProtoIssue;
import bje.buildtools.dependency.explorer.data.Result;
import bje.buildtools.dependency.explorer.data.UpdateLevel;
import bje.buildtools.dependency.explorer.data.Version;
import bje.buildtools.dependency.explorer.util.Constants;

public class UpdatesAnalyzer implements Analyzer {
    private static class UpdateIssue extends AbstractCulsteredProtoIssue {
        private final Updates upd;
        private final Map<UpdateLevel, Severity> locSeverityMap;

        public UpdateIssue(final Pom c, final Updates u, final Map<UpdateLevel, Severity> smap) {
            super(c, u.child::getGroupId, u.child::getArtifactId);
            upd = u;
            locSeverityMap = smap;
        }

        @Override
        public String getDescription() {
            final var sb = new StringBuilder(512);
            switch (UpdateLevel.diff(upd.child.getVersion(), upd.last)) {
            case PATCH:
                sb.append("Patch ");
                break;
            case MINOR:
                sb.append("Minor ");
                break;
            case MAJOR:
                sb.append("Major ");
                break;
            case NONE:
            default:
                sb.append("No ");
                break;
            }
            sb.append("update available for dependency ");
            sb.append(upd.child.toGAeV());
            switch (upd.child.getSource()) {
            case DEPENDENCY_MANAGEMENT:
                sb.append(" (see dependency management).");
                break;
            case PLUGIN:
                sb.append(" (see plugin).");
                break;
            case PLUGIN_MANAGEMENT:
                sb.append(" (see plugin management).");
                break;

            case DEPENDENCY:
            default:
                sb.append(".");
                break;
            }
            if (upd.child.getPropertyName() != null) {
                sb.append(" This dependency use a property : \"");
                sb.append(upd.child.getPropertyName());
                sb.append("\".");
            }
            if (upd.next != null) {
                sb.append(" Next version is ");
                sb.append(upd.next.toString());
                sb.append(".");
            }
            if (upd.last != null) {
                sb.append(" Latest version is ");
                sb.append(upd.last.toString());
                sb.append(".");
            }
            return sb.toString();
        }

        @Override
        public String getGA() {
            return upd.child.toGA();
        }

        @Override
        public String getRuleKey() {
            return Constants.UPDATE_RULE_KEY;
        }

        @Override
        public Severity getSeverity() {
            return locSeverityMap.get(UpdateLevel.diff(upd.child.getVersion(), upd.last));
        }
    }

    public static class Updates {

        private final Dependency child;
        private final Version last;
        private final Version next;

        public Updates(final Dependency aChild, final Version aNext, final Version aLast) {
            child = aChild;
            next = aNext;
            last = aLast;
        }
    }

    private final Map<UpdateLevel, Severity> severityMap = new EnumMap<>(UpdateLevel.class);

    public UpdatesAnalyzer(final Configuration config) {
        severityMap.put(UpdateLevel.MAJOR, Severity.valueOf(
                config.get(Constants.UPDATE_MAJOR_SEVERITY_PROPERTY).orElse(Constants.UPDATE_MAJOR_SEVERITY_DEFAULT)));
        severityMap.put(UpdateLevel.MINOR, Severity.valueOf(
                config.get(Constants.UPDATE_MINOR_SEVERITY_PROPERTY).orElse(Constants.UPDATE_MINOR_SEVERITY_DEFAULT)));
        severityMap.put(UpdateLevel.PATCH, Severity.valueOf(
                config.get(Constants.UPDATE_PATCH_SEVERITY_PROPERTY).orElse(Constants.UPDATE_PATCH_SEVERITY_DEFAULT)));
        severityMap.put(UpdateLevel.NONE, Severity.INFO);
    }

    @Override
    public Result analyze(final Pom pom) {
        final List<Updates> updates = new ArrayList<>();
        for (final Dependency child : pom.getRoot().getChildren()) {
            final var v = child.getUpdatesVersions();
            if (v != null && !v.isEmpty()) {
                updates.add(new Updates(child, v.first(), v.last()));
            }
        }
        final Set<ProtoIssue> set = new TreeSet<>();
        for (final Updates t : updates) {
            set.add(new UpdateIssue(pom, t, severityMap));
        }

        return new AbstractModifiableResult(set, pom) {

            @Override
            public void print(final Appendable out) throws IOException {
                out.append("--------------------------------------------------------------------------------\n");
                out.append(" UPDATES CHECK\n");
                out.append("--------------------------------------------------------------------------------\n");
                for (final Updates updt : updates) {
                    updt.child.fillGAV(out);
                    out.append(" -> next : ");
                    out.append(updt.next.toString());
                    out.append(", last : ");
                    out.append(updt.last.toString());
                    out.append("\n");
                }
            }
        };
    }

}
