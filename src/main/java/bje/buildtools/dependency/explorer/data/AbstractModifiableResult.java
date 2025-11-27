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

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

public abstract class AbstractModifiableResult implements Result {
    private Set<ProtoIssue> issues;
    private final Pom pom;

    protected AbstractModifiableResult(final Set<ProtoIssue> list, final Pom c) {
        issues = list;
        pom = c;
    }

    @Override
    public Pom getPom() {
        return pom;
    }

    @Override
    public boolean isIssuePresent(final ProtoIssue issue) {
        for (final ProtoIssue iss : issues) {
            if (Objects.equals(iss.getRuleKey(), issue.getRuleKey())
                    && Objects.equals(iss.getSeverity(), issue.getSeverity())
                    && Objects.equals(iss.getDescription(), issue.getDescription())) {
                return true;
            }
        }
        return false;

    }

    @Override
    public Iterator<ProtoIssue> iterator() {
        return issues.iterator();
    }

    @Override
    public void updateIssues(final Set<ProtoIssue> list) {
        issues = list;
    }
}
