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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.fs.TextRange;

public class InputFileUtils {

    public static class DefaultRange implements TextRange {
        private final TextPointer end;
        private final TextPointer start;

        public DefaultRange(final TextPointer start, final TextPointer end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public TextPointer end() {
            return end;
        }

        @Override
        public boolean overlap(final TextRange another) {
            return end.compareTo(another.start()) > 0 || start.compareTo(another.end()) > 0;
        }

        @Override
        public TextPointer start() {
            return start;
        }
    }

    public static class DefaultTextPointer implements TextPointer {
        private final int line;
        private final int offest;

        public DefaultTextPointer(final int line, final int offset) {
            this.line = line;
            offest = offset;
        }

        @Override
        public int compareTo(final TextPointer o) {
            var i = Integer.compare(line, o.line());
            if (i == 0) {
                i = Integer.compare(offest, o.lineOffset());
            }
            return i;
        }

        @Override
        public int line() {
            return line;
        }

        @Override
        public int lineOffset() {
            return offest;
        }

    }

    public static InputFile loadFile(final Path file) {
        return new InputFile() {

            @Override
            public String absolutePath() {
                return file.toAbsolutePath().toString();
            }

            @Override
            public Charset charset() {
                return StandardCharsets.UTF_8;
            }

            @Override
            public String contents() throws IOException {
                return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            }

            @Override
            public File file() {
                return file.toFile();
            }

            @Override
            public String filename() {
                return file.getFileName().toString();
            }

            @Override
            public InputStream inputStream() throws IOException {
                return Files.newInputStream(file);
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean isFile() {
                return Files.isRegularFile(file);
            }

            @Override
            public String key() {
                return null;
            }

            @Override
            public String language() {
                return null;
            }

            @Override
            public int lines() {
                try (final var s = Files.lines(file, StandardCharsets.UTF_8)) {
                    return (int) s.count();
                } catch (final IOException e) {
                    return 0;
                }
            }

            @Override
            public String md5Hash() {
                try {
                    final var content = Files.readAllBytes(file);
                    final var digester = MessageDigest.getInstance("MD5");
                    final var digest = digester.digest(content);
                    return Utils.bytesToHex(digest);
                } catch (final IOException | NoSuchAlgorithmException e) {
                    return null;
                }
            }

            @Override
            public TextPointer newPointer(final int line, final int lineOffset) {
                return new DefaultTextPointer(line, lineOffset);
            }

            @Override
            public TextRange newRange(final int startLine, final int startLineOffset, final int endLine,
                    final int endLineOffset) {
                return newRange(new DefaultTextPointer(startLine, startLineOffset),
                        new DefaultTextPointer(endLine, endLineOffset));
            }

            @Override
            public TextRange newRange(final TextPointer start, final TextPointer end) {
                return new DefaultRange(start, end);
            }

            @Override
            public Path path() {
                return file;
            }

            @Override
            public String relativePath() {
                return file.toAbsolutePath().toString();
            }

            @Override
            public TextRange selectLine(final int line) {
                return null;
            }

            @Override
            public Status status() {
                return Status.CHANGED;
            }

            @Override
            public String toString() {
                return file.toString();
            }

            @Override
            public Type type() {
                return Type.MAIN;
            }

            @Override
            public URI uri() {
                return file.toUri();
            }

        };
    }

    public static InputFile loadFile(final String fileName) {
        final var file = Path.of(fileName);
        return loadFile(file);
    }

    private InputFileUtils() {
        // block default constructor
    }

}
