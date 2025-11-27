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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class XMLMap {

    private final Map<String, Data> data;

    public XMLMap(final Map<String, Data> dataMap) {
        data = dataMap;
    }

    public Set<Entry<String, Data>> entrySet() {
        return data.entrySet();
    }

    public void forEach(final String key, final XMLMapper mapper) {
        final Map<Integer, Map<String, Data>> maps = new TreeMap<>();
        for (final Entry<String, Data> e : data.entrySet()) {
            if (e.getKey().startsWith(key)) {
                var i = 0;
                var m = e.getKey().substring(key.length());
                if (m.startsWith("(")) {
                    final var y = m.indexOf(")");
                    i = Integer.parseInt(m.substring(1, y));
                    m = m.substring(y + 1);
                }
                final var map = maps.computeIfAbsent(i, n -> new HashMap<>());
                map.put(m, e.getValue());
            }
        }
        for (final Map<String, Data> map : maps.values()) {
            mapper.map(new XMLMap(map));
        }

    }

    public String get(final String key) {
        final var dt = data.get(key);
        return dt == null ? null : dt.getText();
    }

    public Range getRange(final String key) {
        final var dt = data.get(key);
        return dt == null ? null : dt.getRange();
    }
}
