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

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import bje.buildtools.dependency.explorer.util.InputFileUtils;
import bje.toolbox.xml.Range;

class FiledRangeTest {
    private static final ClassLoader classLoader = FiledRangeTest.class.getClassLoader();

    @Test
    void test() {
        final var in1 = InputFileUtils.loadFile(Path.of(classLoader.getResource("test.txt").getFile()));
        final var in2 = InputFileUtils.loadFile(Path.of(classLoader.getResource("data.json").getFile()));

        final var ra = new FiledRange(in1, new Range(1, 0, 3, 17), "text");
        final var rb = new FiledRange(in1, new Range(1, 0, 3, 17), "text");
        final var rc = new FiledRange(in2, new Range(1, 0, 3, 17), "text");
        final var rd = new FiledRange(in1, new Range(3, 0, 3, 17), "text");
        final var re = new FiledRange(in1, new Range(1, 0, 3, 17), "truc");

        assertEquals(in1, ra.getFile());
        assertEquals(new Range(1, 0, 3, 17), ra.getRange());
        assertEquals("text", ra.getText());
        assertEquals(ra, rb);
        assertNotEquals(ra, rc);
        assertNotEquals(ra, rc);
        assertNotEquals(ra, rd);
        assertNotEquals(ra, re);
        assertNotEquals(ra, new Object());

        assertEquals(ra.hashCode(), rb.hashCode());
        assertNotEquals(ra.hashCode(), rc.hashCode());
        assertNotEquals(ra.hashCode(), rc.hashCode());
        assertNotEquals(ra.hashCode(), rd.hashCode());
        assertNotEquals(ra.hashCode(), re.hashCode());
    }

}
