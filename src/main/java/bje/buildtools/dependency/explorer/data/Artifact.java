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
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bje.buildtools.dependency.explorer.util.Utils;

public class Artifact implements Comparable<Artifact> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Artifact.class);
    protected final String artifactId;
    protected String effectiveVersion;
    protected final String groupId;
    protected String propertyName;
    protected FiledRange range;
    protected String scope;
    protected String type;
    protected final String version;

    public Artifact(final String aGroupId, final String anArtifactId, final String aBaseVersion) {
        groupId = aGroupId;
        artifactId = anArtifactId;
        version = aBaseVersion;
    }

    @Override
    public int compareTo(final Artifact o) {
        var i = getGroupId().compareTo(o.getGroupId());
        if (i == 0) {
            i = getArtifactId().compareTo(o.getArtifactId());
        }
        if (i == 0) {
            i = Utils.compare(getEffectiveVersion(), o.getEffectiveVersion());
        }
        return i;
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof final Artifact a) {
            return Objects.equals(groupId, a.groupId) && Objects.equals(artifactId, a.artifactId)
                    && Objects.equals(version, a.version);
        }
        return false;
    }

    public void fillGAV(final Appendable sb) {
        try {
            sb.append(groupId);
            sb.append(":");
            sb.append(artifactId);
            sb.append(":");
            if (type != null) {
                sb.append(type);
                sb.append(":");
            }
            sb.append(version);
        } catch (final IOException e) {
            LOGGER.trace("Building GAV", e);
        }
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getEffectiveVersion() {
        return effectiveVersion;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public FiledRange getRange() {
        return range;
    }

    public String getScope() {
        return scope;
    }

    public String getType() {
        return type;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId, version);
    }

    public void setEffectiveVersion(final String property) {
        effectiveVersion = property;
    }

    public void setPropertyName(final String version2) {
        if (version2 != null) {
            propertyName = version2;
        }
    }

    public void setRange(final FiledRange aRange) {
        range = aRange;
    }

    public void setScope(final String s) {
        scope = s;
    }

    public void setType(final String ptype) {
        type = ptype;
    }

    public String toGA() {
        return groupId + ":" + artifactId;
    }

    public String toGAV() {
        final var sb = new StringBuilder(512);
        fillGAV(sb);
        return sb.toString();
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder(512);
        fillGAV(sb);
        if (range != null) {
            sb.append(" (");
            sb.append(range.getRange().toString());
            sb.append(")");
        }
        if (scope != null) {
            sb.append(" ");
            sb.append(scope);
        }
        return sb.toString();
    }
}