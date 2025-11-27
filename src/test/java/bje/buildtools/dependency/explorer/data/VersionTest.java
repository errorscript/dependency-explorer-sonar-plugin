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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

class VersionTest {

    @Test
    void compare() {
        final var v00 = new Version("1");
        final var v0 = new Version("1.2");
        final var v1 = new Version("1.2.3");
        final var v2 = new Version("1.2.4");
        final var v3 = new Version("1.4.3");
        final var v4 = new Version("4.2.3");
        final var v5 = new Version("1.2.3-SNAPSHOT");
        final var v6 = new Version("1.2.3.4");
        final var set = new TreeSet<>(Arrays.asList(v0, v00, v1, v2, v3, v4, v5, v6));
        final List<Version> list = Arrays.asList(v00, v0, v5, v1, v6, v2, v3, v4);
        final var is = set.iterator();
        final var il = list.iterator();
        while (is.hasNext() && il.hasNext()) {
            final var l = il.next();
            final var s = is.next();
            assertEquals(l, s);
        }
        assertFalse(is.hasNext());
        assertFalse(il.hasNext());

        assertEquals(0, v1.compareTo(v1));
        assertTrue(v1.compareTo(v2) < 0);
        assertTrue(v1.compareTo(v3) < 0);
        assertTrue(v1.compareTo(v4) < 0);
        assertTrue(v1.compareTo(v5) > 0);
        assertTrue(v1.compareTo(v6) < 0);

        assertTrue(new Version("1.2.2").compareTo(new Version("1.2.3-SNAPSHOT")) < 0);
        assertTrue(new Version("1.2.3-SNAPSHOT").compareTo(new Version("1.2.3")) < 0);
        assertTrue(new Version("1.2.2").compareTo(new Version("1.2.3")) < 0);
    }

    @Test
    void test() {
        final var v1 = new Version("1.2.3");
        assertEquals(1, v1.major);
        assertEquals(2, v1.minor);
        assertEquals(3, v1.patch);
        assertEquals("", v1.snap);
        assertEquals("", v1.other);

        final var v2 = new Version("1.2.3");
        final var v3 = new Version("1.2.4");
        final var v4 = new Version("1.4.3");
        final var v5 = new Version("4.2.3");

        final var v6 = new Version("1.2.3-SNAPSHOT");
        assertEquals("SNAPSHOT", v6.snap);
        assertEquals("", v6.other);
        final var v7 = new Version("1.2.3.4");
        assertEquals("", v7.snap);
        assertEquals("4", v7.other);

        assertEquals(v1, v2);
        assertNotEquals(v1, v3);
        assertNotEquals(v1, v4);
        assertNotEquals(v1, v5);
        assertNotEquals(v1, v6);
        assertNotEquals(v1, v7);

        assertEquals(v1.hashCode(), v2.hashCode());
        assertNotEquals(v1.hashCode(), v3.hashCode());
        assertNotEquals(v1.hashCode(), v4.hashCode());
        assertNotEquals(v1.hashCode(), v5.hashCode());
        assertNotEquals(v1.hashCode(), v6.hashCode());
        assertNotEquals(v1.hashCode(), v7.hashCode());

        assertEquals("0.0.0", new Version((String) null).toString());
    }

}
