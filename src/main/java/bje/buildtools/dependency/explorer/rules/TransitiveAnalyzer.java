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
import java.util.List;
import java.util.Map.Entry;
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
import bje.buildtools.dependency.explorer.data.Scope;
import bje.buildtools.dependency.explorer.util.Constants;
import bje.buildtools.dependency.explorer.util.Utils;

public class TransitiveAnalyzer implements Analyzer {
    private static class TransitiveIssue extends AbstractCulsteredProtoIssue {

        private final TransitiveResult upd;
        private final Severity severity;

        public TransitiveIssue(final TransitiveResult u, final Pom c, final Severity s) {
            super(c, u::getGroupId, u::getArtifactId);
            upd = u;
            severity =s;
        }

        @Override
        public String getDescription() {
            return Utils.createDescriptionForSources(upd.gav, upd.sources, " is used in ",
                    " scope, but is not directly declared.");
        }

        @Override
        public String getRuleKey() {
            return Constants.TRANSITIVE_RULE_KEY;
        }

        @Override
        public Severity getSeverity() {
            return severity;
        }
    }

    public static class TransitiveResult {

        private final String artifactId;
        private final String gav;
        private final String groupId;
        private final Scope sources;

        public TransitiveResult(final String aGroupId, final String anArtifactId, final String key, final Scope types) {
            gav = key;
            groupId = aGroupId;
            artifactId = anArtifactId;
            sources = types;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public String getGroupId() {
            return groupId;
        }
    }

    private final Severity severity;

    public TransitiveAnalyzer(final Configuration config) {
        severity = Severity.valueOf(config.get(Constants.TRANSITIVE_SEVERITY_PROPERTY)
                .orElse(Constants.TRANSITIVE_SEVERITY_DEFAULT));
    }

    @Override
    public Result analyze(final Pom pom) {
        final List<TransitiveResult> list = new ArrayList<>();
        for (final Entry<Scope, List<Artifact>> e : pom.getUndeclaredDependency().entrySet()) {
            for (final Artifact a : e.getValue()) {
                list.add(new TransitiveResult(a.getGroupId(), a.getArtifactId(), a.toGAV(), e.getKey()));
            }
        }
        final Set<ProtoIssue> set = new TreeSet<>();
        for (final TransitiveResult t : list) {
            set.add(new TransitiveIssue(t, pom,severity));
        }

        return new AbstractModifiableResult(set, pom) {

            @Override
            public void print(final Appendable out) throws IOException {
                out.append("--------------------------------------------------------------------------------\n");
                out.append(" USED TRANSITIVE DEPENDENCY\n");
                out.append("--------------------------------------------------------------------------------\n");
                for (final TransitiveResult version : list) {
                    out.append(version.gav);
                    out.append(" is used in ");
                    out.append(version.sources.name());
                    out.append(" scope, but is not directly declared.\n");
                }
            }
        };
    }
}
