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

import org.junit.jupiter.api.Test;

import bje.toolbox.xml.Range;

class ArtifactTest {

    @Test
    void test() {
        final var a = new Artifact("g", "a", "v");
        final var b = new Artifact("g", "a", "v");
        final var c = new Artifact("r", "a", "v");
        final var d = new Artifact("g", "r", "v");
        final var e = new Artifact("g", "a", "r");
        assertEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(a, d);
        assertNotEquals(a, e);
        a.setRange(new FiledRange(null, new Range(2, 3, 4, 5), "help"));
        assertEquals(a, b);
        assertEquals(new FiledRange(null, new Range(2, 3, 4, 5), "help"), a.getRange());
        assertEquals("g", a.getGroupId());
        assertEquals("a", a.getArtifactId());
        assertEquals("v", a.getVersion());

        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a.hashCode(), c.hashCode());
        assertNotEquals(a.hashCode(), d.hashCode());
        assertNotEquals(a.hashCode(), e.hashCode());

        assertEquals("g:a:v (2:3 - 4:5)", a.toString());
        assertEquals("g:a:v", b.toString());

        assertEquals("g:a:v", a.toGAV());
        assertEquals("g:a:v", b.toGAV());
    }

}
