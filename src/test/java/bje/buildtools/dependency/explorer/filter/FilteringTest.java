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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FilteringTest {
    @Test
    void test() {
        final var fl = new FilterList("""
                org:art:3.4.5,\
                ib*:date:3.4.5,\
                *ib:date:3.4.5,\
                i*i:date:3.4.5,\
                org:date:*,\
                net::""");
        assertFalse(fl.isInFilter("com", "truc", "5.0.1"));
        assertFalse(fl.isInFilter("org1", "art", "3.4.5"));
        assertFalse(fl.isInFilter("org", "art1", "3.4.5"));
        assertFalse(fl.isInFilter("org", "art", "3.4.4"));
        assertTrue(fl.isInFilter("org", "art", "3.4.5"));

        assertTrue(fl.isInFilter("ibm", "date", "3.4.5"));
        assertFalse(fl.isInFilter("imm", "date", "3.4.5"));
        assertFalse(fl.isInFilter("ibm", "date1", "3.4.5"));
        assertFalse(fl.isInFilter("ibm", "date", "3.4.6"));

        assertTrue(fl.isInFilter("aib", "date", "3.4.5"));
        assertFalse(fl.isInFilter("ain", "date", "3.4.5"));
        assertFalse(fl.isInFilter("aib", "date1", "3.4.5"));
        assertFalse(fl.isInFilter("aib", "date", "3.4.6"));

        assertTrue(fl.isInFilter("iri", "date", "3.4.5"));
        assertTrue(fl.isInFilter("ii", "date", "3.4.5"));
        assertFalse(fl.isInFilter("i", "date", "3.4.5"));
        assertFalse(fl.isInFilter("ibm", "date", "3.4.6"));

        assertTrue(fl.isInFilter("org", "date", "3.4.5"));
        assertFalse(fl.isInFilter("org", "true", "3.4.5"));
        assertFalse(fl.isInFilter("com", "date", "3.4.5"));

        assertTrue(fl.isInFilter("net", "date", "3.4.5"));

        assertFalse(fl.isInFilter(null, "date", "3.4.5"));
    }

    @Test
    void testError() {
        assertThrowsExactly(IllegalArgumentException.class, () -> new FilterList("org:da*te*:*"));
    }

    @Test
    void testFilterAll() {
        final var fl = new FilterList("::");
        assertTrue(fl.isInFilter("com", "truc", "5.0.1"));
    }

    @Test
    void testToString() {
        final var base = """
                org:art:3.4.5,\
                ib*:date:3.4.5,\
                *ib:date:3.4.5,\
                i*i:date:3.4.5,\
                org:date:*,\
                net::""";
        assertEquals(base, new FilterList(base).toString());
    }

}
