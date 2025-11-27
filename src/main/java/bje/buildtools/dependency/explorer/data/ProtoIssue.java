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

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.rule.Severity;

import bje.buildtools.dependency.explorer.util.Utils;

public interface ProtoIssue extends Comparable<ProtoIssue> {
    @Override
    default int compareTo(final ProtoIssue o) {
        var i = Utils.compare(getRuleKey(), o.getRuleKey());
        if (i == 0) {
            i = Utils.compare(getGA(), o.getGA());
            if (i == 0) {
                i = Utils.compare(getLevel(), o.getLevel());
                if (i == 0) {
                    i = Utils.compare(getModuleName(), o.getModuleName());
                    if (i == 0) {
                        i = Utils.compare(getSeverity(), o.getSeverity());
                        if (i == 0) {
                            i = Utils.compare(getDescription(), o.getDescription());
                        }
                    }
                }
            }
        }
        return i;
    }

    InputFile getComponent();

    String getDescription();

    String getGA();

    default int getLevel() {
        return -1;
    }

    String getModuleName();

    String getRuleKey();

    Severity getSeverity();

    TextRange getTextRange();
}
