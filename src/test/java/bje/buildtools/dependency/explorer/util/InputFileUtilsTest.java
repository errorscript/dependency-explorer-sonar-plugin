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
package bje.buildtools.dependency.explorer.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile.Status;
import org.sonar.api.batch.fs.InputFile.Type;

import bje.buildtools.dependency.explorer.util.InputFileUtils.DefaultTextPointer;

class InputFileUtilsTest {
    private static final ClassLoader classLoader = InputFileUtilsTest.class.getClassLoader();

    @SuppressWarnings("deprecation")
    @Test
    void test() throws IOException, URISyntaxException {
        final var source = Path.of(classLoader.getResource("test.txt").getFile());
        final var in = InputFileUtils.loadFile(source.toAbsolutePath());
        assertEquals(classLoader.getResource("test.txt").toURI(), in.uri());
        assertEquals("test.txt", in.filename());
        assertNull(in.key());
        assertTrue(in.isFile());
        assertEquals(source.toAbsolutePath().toString(), in.relativePath());
        assertEquals(source.toAbsolutePath().toString(), in.absolutePath());
        assertEquals(source.toFile(), in.file());
        assertEquals(source.toAbsolutePath().toString(), in.toString());
        assertEquals(source, in.path());
        assertNull(in.language());
        assertEquals(Type.MAIN, in.type());
        assertEquals(Status.CHANGED, in.status());

        var nb = 0;
        final var sb = new StringBuilder(512);
        try (var br = new BufferedReader(new InputStreamReader(in.inputStream()))) {
            var line = br.readLine();
            while (line != null) {
                if (nb > 0) {
                    sb.append("\n");
                }
                ++nb;
                sb.append(line);
                line = br.readLine();
            }
        }
        assertEquals(nb, in.lines());
        assertEquals(sb.toString(), in.contents());

        assertFalse(in.isEmpty());
        assertNull(in.selectLine(1));
        assertEquals(StandardCharsets.UTF_8, in.charset());
    }

    @Test
    void testTextUtillitaries() {
        final var source = Path.of(classLoader.getResource("test.txt").getFile());
        final var in = InputFileUtils.loadFile(source.toAbsolutePath());
        final var pointer = in.newPointer(1, 3);
        assertEquals(1, pointer.line());
        assertEquals(3, pointer.lineOffset());
        assertEquals(-1, pointer.compareTo(new DefaultTextPointer(2, 3)));
        assertEquals(0, pointer.compareTo(new DefaultTextPointer(1, 3)));
        assertEquals(1, pointer.compareTo(new DefaultTextPointer(1, 2)));

        final var range = in.newRange(1, 1, 2, 2);
        assertEquals(1, range.start().line());
        assertEquals(1, range.start().lineOffset());
        assertEquals(2, range.end().line());
        assertEquals(2, range.end().lineOffset());

        final var range2 = in.newRange(3, 0, 3, 3);
        assertFalse(range.overlap(range2));

        final var range3 = in.newRange(2, 0, 3, 3);
        assertTrue(range.overlap(range3));

        final var range4 = in.newRange(1, 0, 1, 3);
        assertFalse(range4.overlap(range3));

        final var range5 = in.newRange(1, 0, 2, 1);
        assertTrue(range5.overlap(range3));

    }

}
