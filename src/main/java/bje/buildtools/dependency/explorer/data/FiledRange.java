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

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;

import bje.toolbox.xml.Range;

public class FiledRange {
    private final InputFile file;
    private final Range range;
    private final String text;

    public FiledRange(final InputFile aFile, final Range aRange, final String aText) {
        file = aFile;
        range = aRange;
        text = aText;
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof final FiledRange f) {
            return Objects.equals(file, f.file) && Objects.equals(range, f.range) && Objects.equals(text, f.text);
        }
        return false;
    }

    public InputFile getFile() {
        return file;
    }

    public Range getRange() {
        return range;
    }

    public String getText() {
        return text;
    }

    public TextRange getTextRange() {
        return file.newRange(file.newPointer(range.getLineStart(), range.getPositionStart() - 1),
                file.newPointer(range.getLineStop(), range.getPositionStop() - 1));
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, range, text);
    }
}
