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

import java.util.Objects;

import bje.buildtools.dependency.explorer.util.Utils;

public class Version implements Comparable<Version> {
    private static class Parts {

        public static Parts of(final String string) {
            final var r = string.indexOf("-");
            var toInt = string;
            String other = null;
            if (r > 0) {
                toInt = string.substring(0, r);
                other = string.substring(r + 1);
            }
            return new Parts(Integer.parseInt(toInt), other);
        }

        public final int intValue;

        public final String stringValue;

        private Parts(final int parseInt, final String other) {
            intValue = parseInt;
            stringValue = other;
        }
    }

    private static String append(final String lsnap, final String stringValue) {
        if (lsnap != null) {
            if (stringValue == null) {
                return lsnap;
            }
            return lsnap + "-" + stringValue;
        }
        return stringValue;
    }

    public final int major;
    public final int minor;
    public final String other;
    public final int patch;
    public final String snap;

    public Version(final String s) {
        final var ss = s == null ? new String[0] : s.split("\\.");
        var lmajor = 0;
        var lminor = 0;
        var lpatch = 0;
        String lsnap = null;
        final var t = new StringBuilder(512);
        for (var i = 0; i < ss.length; ++i) {
            switch (i) {
            case 0:
                var p = Parts.of(ss[0]);
                lmajor = p.intValue;
                lsnap = append(lsnap, p.stringValue);
                break;
            case 1:
                p = Parts.of(ss[1]);
                lminor = p.intValue;
                lsnap = append(lsnap, p.stringValue);
                break;
            case 2:
                p = Parts.of(ss[2]);
                lpatch = p.intValue;
                lsnap = append(lsnap, p.stringValue);
                break;
            default:
                if (i > 3) {
                    t.append(".");
                }
                t.append(ss[i]);
            }
        }
        other = t.toString();
        major = lmajor;
        minor = lminor;
        patch = lpatch;
        snap = lsnap == null ? "" : lsnap;

    }

    @Override
    public int compareTo(final Version o) {
        var i = Integer.compare(major, o.major);
        if (i == 0) {
            i = Integer.compare(minor, o.minor);
            if (i == 0) {
                i = Integer.compare(patch, o.patch);
                if (i == 0) {
                    i = Utils.compare(other, o.other);
                    if (i == 0) {
                        i = -Utils.compare(snap, o.snap);
                    }
                }
            }
        }
        return i;
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof final Version v) {
            return major == v.major && minor == v.minor && patch == v.patch && Objects.equals(other, v.other)
                    && Objects.equals(snap, v.snap);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch, other, snap);
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch + (Utils.isEmptyOrNull(snap) ? "" : "-" + snap)
                + (Utils.isEmptyOrNull(other) ? "" : "." + other);
    }
}
