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

import java.util.List;
import java.util.Objects;

public class Range implements Comparable<Range> {
    public static void replace(final List<String> list, final Range range, final String replacement) {
        if (range.getLineStart() == range.getLineStop()) {
            final var line = list.get(range.getLineStart() - 1);
            final var start = line.substring(0, range.getPositionStart() - 1);
            final var end = line.substring(range.getPositionStop() - 1);
            list.set(range.getLineStart() - 1, start + replacement + end);
        } else {
            var i = range.getLineStart();
            var line = list.remove(range.getLineStart() - 1);
            final var start = line.substring(0, range.getPositionStart() - 1);
            ++i;
            while (i < range.getLineStop()) {
                list.remove(range.getLineStart() - 1);
                ++i;
            }
            line = list.remove(range.getLineStart() - 1);
            final var end = line.substring(range.getPositionStop() + 1);
            list.add(range.getLineStart() - 1, start + replacement + end);
        }
    }

    private final int lineStart;
    private final int lineStop;
    private final int positionStart;

    private final int positionStop;

    public Range(final int aLineStart, final int aPositionStart, final int aLineStop, final int aPositionStop) {
        lineStart = aLineStart;
        positionStart = aPositionStart;
        lineStop = aLineStop;
        positionStop = aPositionStop;
    }

    @Override
    public int compareTo(final Range o) {
        var i = Integer.compare(lineStart, o.lineStart);
        if (i == 0) {
            i = Integer.compare(positionStart, o.positionStart);
        }
        if (i == 0) {
            i = Integer.compare(lineStop, o.lineStop);
        }
        if (i == 0) {
            i = Integer.compare(positionStop, o.positionStop);
        }
        return i;
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof final Range r) {
            return lineStart == r.lineStart && positionStart == r.positionStart && lineStop == r.lineStop
                    && positionStop == r.positionStop;
        }
        return false;
    }

    public int getLineStart() {
        return lineStart;
    }

    public int getLineStop() {
        return lineStop;
    }

    public int getPositionStart() {
        return positionStart;
    }

    public int getPositionStop() {
        return positionStop;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineStart, positionStart, lineStop, positionStop);
    }

    @Override
    public String toString() {
        return lineStart + ":" + positionStart + " - " + lineStop + ":" + positionStop;
    }
}
