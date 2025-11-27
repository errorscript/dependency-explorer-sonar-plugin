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
package bje.buildtools.dependency.explorer.filter;

import java.util.Objects;

public class PatternFilter implements Filter {

    public static PatternFilter valueOf(final String value) {
        final var array = value.split(":");
        final var groupFilter = Pattern.valueOf(array.length > 0 ? array[0] : null);
        final var artifactFilter = Pattern.valueOf(array.length > 1 ? array[1] : null);
        final var versionFilter = Pattern.valueOf(array.length > 2 ? array[2] : null);
        return new PatternFilter(groupFilter, artifactFilter, versionFilter);
    }

    private final Pattern artifactPattern;
    private final Pattern groupPattern;
    private final Pattern versionPattern;

    private PatternFilter(final Pattern aGroupPattern, final Pattern anArtifactPattern, final Pattern aVersionPattern) {
        groupPattern = aGroupPattern;
        artifactPattern = anArtifactPattern;
        versionPattern = aVersionPattern;
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof final PatternFilter f) {
            return Objects.equals(groupPattern, f.groupPattern) && Objects.equals(artifactPattern, f.artifactPattern)
                    && Objects.equals(versionPattern, f.versionPattern);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupPattern, artifactPattern, versionPattern);
    }

    @Override
    public boolean isInFilter(final String aGroupId, final String anArtifactId, final String aVersion) {
        return groupPattern.isInPattern(aGroupId) && artifactPattern.isInPattern(anArtifactId)
                && versionPattern.isInPattern(aVersion);
    }

    @Override
    public String toString() {
        return groupPattern + ":" + artifactPattern + ":" + versionPattern;
    }
}
