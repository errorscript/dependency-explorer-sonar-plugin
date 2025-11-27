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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

import bje.buildtools.dependency.explorer.license.LicenseDefinition;
import bje.buildtools.dependency.explorer.license.LicenseIdentity;

class DependencyTest {
    private void addVersions(final Set<Version> versions, final String... strings) {
        for (final String string : strings) {
            versions.add(new Version(string));
        }
    }

    @SafeVarargs
    private <T extends Comparable<T>> TreeSet<T> set(final T... b) {
        final var set = new TreeSet<T>();
        Collections.addAll(set, b);
        return set;
    }

    @Test
    void test() {
        final var a = new Dependency(null, new Artifact("group", "artifactId", "1.0.0"));
        final var b = new Dependency(null, new Artifact("group", "artifactId2", "1.0.1"));
        a.setSource(DependencyType.DEPENDENCY_MANAGEMENT);
        a.addDependency(b);
        assertEquals(set(b), a.getChildren());
        assertEquals(a, b.getParent());
        a.getVersions().add(new Version("1.5.6"));
        a.setLicenses(new LicenseDefinition("MIT", Arrays.asList(new LicenseIdentity("MIT", null))));
        assertEquals("group:artifactId:1.0.0 (DEPENDENCY_MANAGEMENT) -> [1.5.6] [License {name=MIT, family=null}]",
                a.toString());
    }

    @Test
    void testEquality() {
        final var a = new Dependency(null, new Artifact("group", "artifactId", "1.0.0"));
        a.setPropertyName("property");
        a.setSource(DependencyType.DEPENDENCY);
        addVersions(a.getVersions(), "1.0.1", "1.0.2", "1.1.0", "1.2.0", "2.0.0", "2.0.0", "3.0.1");

        final var b = new Dependency(null, new Artifact("group", "artifactId", "1.0.0"));
        b.setPropertyName("property");
        b.setSource(DependencyType.DEPENDENCY);
        addVersions(b.getVersions(), "1.0.1", "1.0.2", "1.1.0", "1.2.0", "2.0.0", "2.0.0", "3.0.1");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        b.setPropertyName("pom");
        assertEquals(a, b);
        assertNotEquals(a, null);
        assertEquals(a.hashCode(), b.hashCode());

        assertEquals("artifactId", a.getArtifactId());
        assertEquals("1.0.0", a.getVersion());
        assertEquals("group", a.getGroupId());
        assertEquals("3.0.1", a.getLastVersion().toString());
        assertEquals("1.0.1", a.getNextVersion().toString());
        assertEquals("property", a.getPropertyName());
        assertEquals(DependencyType.DEPENDENCY, a.getSource());

    }
}
