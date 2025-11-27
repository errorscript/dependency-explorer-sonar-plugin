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
package bje.buildtools.dependency.explorer.license;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class LicenseDefinitionTest {
    private static LicenseDefinition set(final String name, final String... license) {
        final List<LicenseIdentity> ids = new ArrayList<>();
        final List<String> list = Arrays.asList(license);
        for (final String n : list) {
            ids.add(new LicenseIdentity(n, null));
        }
        return new LicenseDefinition(name, ids);
    }

    @Test
    void test() {
        final var a = set("a", "c", "b", "b");
        final var aa = set("a", "b", "c");
        final var b = set("a", "d");
        final var c = set("e", "b", "c");
        assertFalse(a.equals(null));
        assertFalse(a.equals(new Object()));
        assertEquals(a, aa);
        assertNotEquals(a, b);
        assertNotEquals(a, c);

        assertEquals(a.hashCode(), aa.hashCode());
        assertNotEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a.hashCode(), c.hashCode());

        assertEquals(2, a.getComposition().size());
        final var iter = a.getComposition().iterator();
        assertEquals(new LicenseIdentity("b", "truc"), iter.next());
        assertEquals(new LicenseIdentity("c", "machin"), iter.next());
        assertEquals("a", a.getName());
    }

}
