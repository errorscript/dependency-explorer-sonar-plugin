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
import java.util.TreeMap;

public class Mapper {
    private final Map<String, Data> dataMap = new TreeMap<>();
    private final Map<String, Integer> elementNumber = new HashMap<>();
    private boolean isActive = false;
    private final XMLMapper storer;
    private final TagPath tag = new TagPath();

    public Mapper(final XMLMapper aSaver) {
        storer = aSaver;
    }

    public boolean isActive() {
        return isActive;
    }

    public void newElement() {
        dataMap.clear();
        elementNumber.clear();
        tag.clear();
        isActive = true;
    }

    public void saveElement() {
        storer.map(new XMLMap(dataMap));
        isActive = false;
    }

    public void setAttribute(final String qName, final String value, final Range range) {
        dataMap.put(tag.flatten() + ":" + qName, new Data(value, range));
    }

    public void setTag(final String qName) {
        final var i = elementNumber.computeIfAbsent(tag.flatten(qName), q -> 0);
        tag.set(qName, i);
    }

    public void setText(final String string, final Range range) {
        if (!string.trim().isEmpty()) {
            final var n = new Data(string.trim(), range);
            dataMap.merge(tag.flatten(), n, Data::merge);
        }
    }

    public void unsetTag(final String qName) {
        final var path = tag.flattenPrevious(qName);
        var i = elementNumber.get(path);
        tag.unset(qName, i);
        ++i;
        elementNumber.put(path, i);
    }
}
