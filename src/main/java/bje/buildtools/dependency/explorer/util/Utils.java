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

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import bje.buildtools.dependency.explorer.data.Scope;

public class Utils {
    @FunctionalInterface
    public interface Getter<T> {
        String get(final T t);
    }

    private static final char[] hexArray = "0123456789abcdef".toCharArray();
    private static final int MASK = 0x0F;
    private static final int SHIFT = 4;

    public static String bytesToHex(final byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        final var hexChars = new char[bytes.length * 2];
        for (var j = 0; j < bytes.length; ++j) {
            final var v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> SHIFT];
            hexChars[j * 2 + 1] = hexArray[v & MASK];
        }
        return new String(hexChars);
    }

    public static <A extends Comparable<A>> int compare(final A a, final A b) {
        if (a == null) {
            return b == null ? 0 : -1;
        }
        return b == null ? 1 : a.compareTo(b);
    }

    public static <T extends Comparable<T>> int compareIterable(final Iterable<T> a, final Iterable<T> b) {
        final var aa = a.iterator();
        final var bb = b.iterator();
        while (aa.hasNext() && bb.hasNext()) {
            final var i = aa.next().compareTo(bb.next());
            if (i != 0) {
                return i;
            }
        }
        if (aa.hasNext()) {
            return 1;
        }
        return bb.hasNext() ? -1 : 0;
    }

    public static String createDescriptionForSources(final String gav, final Scope sources, final String a,
            final String c) {
        final var sb = new StringBuilder(512);
        sb.append("Dependency ");
        sb.append(gav);
        sb.append(a);
        sb.append(sources.name().toLowerCase());
        sb.append(c);
        return sb.toString();
    }

    public static SAXParser getSAXParser() throws ParserConfigurationException, SAXException {
        final var factory = SAXParserFactory.newInstance();
        // to be compliant, completely disable DOCTYPE declaration:
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return factory.newSAXParser();
    }

    public static boolean isEmptyOrNull(final String s) {
        return s == null || s.isEmpty();
    }

    public static <T> Set<String> toSet(final Collection<T> licenses, final Getter<T> object) {
        final Set<String> s = new TreeSet<>();
        for (final T t : licenses) {
            s.add(object.get(t));
        }
        return s;
    }

    private Utils() {
        // block default constructor
    }
}
