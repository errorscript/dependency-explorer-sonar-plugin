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
package bje.buildtools.dependency.explorer.filter;

import java.util.Objects;

public class WildcardStringPattern implements Pattern {
    public enum MatchType {
        END_MATCH, PLAIN_MATCH, SPLIT_MATCH, START_MATCH
    }

    private final String group1;
    private final String group2;
    private final MatchType type;

    public WildcardStringPattern(final String string) {
        final var i = string.indexOf("*");
        if (i != string.lastIndexOf("*")) {
            throw new IllegalArgumentException("Pattern filtering allow only one wildcard if any");
        }
        if (i >= 0) {
            if (string.startsWith("*")) {
                type = MatchType.END_MATCH;
                group1 = string.substring(1);
                group2 = null;
            } else if (string.endsWith("*")) {
                type = MatchType.START_MATCH;
                group1 = string.substring(0, string.length() - 1);
                group2 = null;
            } else {
                type = MatchType.SPLIT_MATCH;
                group1 = string.substring(0, i);
                group2 = string.substring(i + 1);
            }
        } else {
            type = MatchType.PLAIN_MATCH;
            group1 = string;
            group2 = null;
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof final WildcardStringPattern f) {
            return Objects.equals(group1, f.group1) && Objects.equals(group2, f.group2) && Objects.equals(type, f.type);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(group1, group2, type);
    }

    @Override
    public boolean isInPattern(final String value) {
        if (value != null) {
            return switch (type) {
            case END_MATCH -> value.endsWith(group1);
            case PLAIN_MATCH -> value.equals(group1);
            case SPLIT_MATCH -> value.length() >= group1.length() + group2.length() && value.startsWith(group1)
                    && value.endsWith(group2);
            case START_MATCH -> value.startsWith(group1);
            };
        }
        return false;
    }

    @Override
    public String toString() {
        return switch (type) {
        case END_MATCH -> "*" + group1;
        case SPLIT_MATCH -> group1 + "*" + group2;
        case START_MATCH -> group1 + "*";
        case PLAIN_MATCH -> group1;
        default -> "";
        };
    }
}
