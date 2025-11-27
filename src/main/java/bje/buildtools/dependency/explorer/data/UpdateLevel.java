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

public enum UpdateLevel {
    MAJOR(3), MINOR(2), NONE(0), PATCH(1);

    public static UpdateLevel diff(final String a, final String b) {
        return diff(new Version(a), new Version(b));
    }

    public static UpdateLevel diff(final String a, final Version b) {
        return diff(new Version(a), b);
    }

    public static UpdateLevel diff(final Version a, final String b) {
        return diff(a, new Version(b));
    }

    public static UpdateLevel diff(final Version a, final Version b) {
        if (a.major < b.major) {
            return MAJOR;
        }
        if (a.minor < b.minor) {
            return MINOR;
        }
        if (a.patch < b.patch || !Objects.equals(a.snap, b.snap) || !Objects.equals(a.other, b.other)) {
            return PATCH;
        }
        return NONE;
    }

    private final int weight;

    UpdateLevel(final int w) {
        weight = w;
    }

    public UpdateLevel max(final UpdateLevel level) {

        return level.weight > weight ? level : this;
    }
}
