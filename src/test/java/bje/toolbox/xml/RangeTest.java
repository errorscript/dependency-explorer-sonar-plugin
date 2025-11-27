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
package bje.toolbox.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RangeTest {
    public static class Change implements Comparable<Change> {
        public final Range range;
        public final String text;

        public Change(final String t, final Range r) {
            range = r;
            text = t;
        }

        @Override
        public int compareTo(final Change o) {
            return -range.compareTo(o.range);
        }
    }

    Path source;

    @BeforeEach
    void setUp() throws Exception {
        final var in = getClass().getClassLoader().getResourceAsStream("toolbox/xml/range/range.txt");
        source = Files.createTempFile("range-test", ".txt");
        Files.copy(in, source, StandardCopyOption.REPLACE_EXISTING);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (source != null && Files.exists(source)) {
            Files.delete(source);
        }
    }

    @Test
    void testCompare() {
        final var a = new Range(1, 1, 2, 69);
        final var b = new Range(2, 1, 2, 69);
        final var c = new Range(1, 5, 2, 69);
        final var d = new Range(1, 1, 6, 69);
        final var e = new Range(1, 1, 2, 85);
        assertEquals(0, a.compareTo(a));
        assertEquals(-1, a.compareTo(b));
        assertEquals(-1, a.compareTo(c));
        assertEquals(-1, a.compareTo(d));
        assertEquals(-1, a.compareTo(e));
    }

    @Test
    void testEdition() throws IOException {
        final Map<String, String> updateMap = new HashMap<>();

        updateMap.put("4.5.6", "1.0.0");
        updateMap.put("4.5.7", "1.1.0");
        updateMap.put("4.5.8", "2.3.4");
        updateMap.put("4.5.9", "2.1.0");
        final List<Change> list = new ArrayList<>();
        final var handlerA = new XMLMappingHandler(map -> {
            for (final Entry<String, Data> entry : map.entrySet()) {
                list.add(new Change(entry.getValue().getText(), entry.getValue().getRange()));
            }
        }, "/project/properties");
        final var handlerB = new XMLMappingHandler(map -> {
            list.add(new Change(map.get(""), map.getRange("")));
        }, "/project/versions/version");
        SAXUtils.parse(source, handlerA, handlerB);
        assertEquals(4, list.size());
        Collections.sort(list);
        final var slist = Files.readAllLines(source, StandardCharsets.UTF_8);
        for (final Change change : list) {
            final var newText = updateMap.get(change.text.trim());
            Range.replace(slist, change.range, newText);
        }
        Files.write(source, slist);
        final var alist = Files.readAllLines(source);
        final var iter = alist.iterator();
        final var in = getClass().getClassLoader().getResourceAsStream("toolbox/xml/range/result.txt");
        final var br = new BufferedReader(new InputStreamReader(in));
        var line = br.readLine();
        while (line != null) {
            if (iter.hasNext()) {
                final var str = iter.next();
                assertEquals(line, str);
            }
            line = br.readLine();
        }
        assertFalse(iter.hasNext());
    }

    @Test
    void testEquals() {
        final var a = new Range(1, 1, 2, 69);
        final var b = new Range(2, 1, 2, 69);
        final var c = new Range(1, 5, 2, 69);
        final var d = new Range(1, 1, 6, 69);
        final var e = new Range(1, 1, 2, 85);
        final var f = new Range(1, 1, 2, 69);
        assertFalse(a.equals(null));
        assertFalse(a.equals(new Object()));
        assertNotEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(a, d);
        assertNotEquals(a, e);
        assertEquals(a, f);
    }

    @Test
    void testHashCode() {
        final var a = new Range(1, 1, 2, 69);
        final var b = new Range(2, 1, 2, 69);
        final var c = new Range(1, 5, 2, 69);
        final var d = new Range(1, 1, 6, 69);
        final var e = new Range(1, 1, 2, 85);
        final var f = new Range(1, 1, 2, 69);
        assertFalse(a.equals(null));
        assertFalse(a.equals(new Object()));
        assertNotEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a.hashCode(), c.hashCode());
        assertNotEquals(a.hashCode(), d.hashCode());
        assertNotEquals(a.hashCode(), e.hashCode());
        assertEquals(a.hashCode(), f.hashCode());
    }

}
