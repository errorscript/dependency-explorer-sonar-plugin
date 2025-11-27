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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.Marker;

class LoggerAppendableTest {
    public abstract static class AbstractLogger implements Logger {
        @Override
        public void debug(final Marker marker, final String msg) {
            debug(msg);
        }

        @Override
        public void debug(final Marker marker, final String format, final Object arg) {
            debug(format);
        }

        @Override
        public void debug(final Marker marker, final String format, final Object... arguments) {
            debug(format);
        }

        @Override
        public void debug(final Marker marker, final String format, final Object arg1, final Object arg2) {
            debug(format);
        }

        @Override
        public void debug(final Marker marker, final String msg, final Throwable t) {
            debug(msg);
        }

        @Override
        public void debug(final String pattern, final Object arg) {
            debug(pattern);
        }

        @Override
        public void debug(final String msg, final Object... args) {
            debug(msg);
        }

        @Override
        public void debug(final String msg, final Object arg1, final Object arg2) {
            debug(msg);
        }

        @Override
        public void debug(final String msg, final Throwable t) {
            debug(msg);
        }

        @Override
        public void error(final Marker marker, final String msg) {
            error(msg);

        }

        @Override
        public void error(final Marker marker, final String format, final Object arg) {
            error(format);
        }

        @Override
        public void error(final Marker marker, final String format, final Object... arguments) {
            error(format);

        }

        @Override
        public void error(final Marker marker, final String format, final Object arg1, final Object arg2) {
            error(format);

        }

        @Override
        public void error(final Marker marker, final String msg, final Throwable t) {
            error(msg);

        }

        @Override
        public void error(final String msg, final Object arg) {
            error(msg);
        }

        @Override
        public void error(final String msg, final Object... args) {
            error(msg);
        }

        @Override
        public void error(final String msg, final Object arg1, final Object arg2) {
            error(msg);
        }

        @Override
        public void error(final String msg, final Throwable thrown) {
            error(msg);
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public void info(final Marker marker, final String msg) {
            info(msg);
        }

        @Override
        public void info(final Marker marker, final String format, final Object arg) {
            info(format);
        }

        @Override
        public void info(final Marker marker, final String format, final Object... arguments) {
            info(format);
        }

        @Override
        public void info(final Marker marker, final String format, final Object arg1, final Object arg2) {
            info(format);
        }

        @Override
        public void info(final Marker marker, final String msg, final Throwable t) {
            info(msg);
        }

        @Override
        public void info(final String msg, final Object arg) {
            info(msg);
        }

        @Override
        public void info(final String msg, final Object... args) {
            info(msg);
        }

        @Override
        public void info(final String msg, final Object arg1, final Object arg2) {
            info(msg);
        }

        @Override
        public void info(final String msg, final Throwable t) {
            info(msg);
        }

        @Override
        public boolean isDebugEnabled() {
            return false;
        }

        @Override
        public boolean isDebugEnabled(final Marker marker) {
            return false;
        }

        @Override
        public boolean isErrorEnabled() {
            return false;
        }

        @Override
        public boolean isErrorEnabled(final Marker marker) {
            return false;
        }

        @Override
        public boolean isInfoEnabled() {
            return false;
        }

        @Override
        public boolean isInfoEnabled(final Marker marker) {
            return false;
        }

        @Override
        public boolean isTraceEnabled() {
            return false;
        }

        @Override
        public boolean isTraceEnabled(final Marker marker) {
            return false;
        }

        @Override
        public boolean isWarnEnabled() {
            return false;
        }

        @Override
        public boolean isWarnEnabled(final Marker marker) {
            return false;
        }

        @Override
        public void trace(final Marker marker, final String msg) {
            trace(msg);
        }

        @Override
        public void trace(final Marker marker, final String format, final Object arg) {
            trace(format);
        }

        @Override
        public void trace(final Marker marker, final String format, final Object... argArray) {
            trace(format);
        }

        @Override
        public void trace(final Marker marker, final String format, final Object arg1, final Object arg2) {
            trace(format);
        }

        @Override
        public void trace(final Marker marker, final String msg, final Throwable t) {
            trace(msg);
        }

        @Override
        public void trace(final String msg) {
            trace(msg);
        }

        @Override
        public void trace(final String pattern, final Object arg) {
            trace(pattern);
        }

        @Override
        public void trace(final String msg, final Object... args) {
            trace(msg);
        }

        @Override
        public void trace(final String msg, final Object arg1, final Object arg2) {
            trace(msg);
        }

        @Override
        public void trace(final String msg, final Throwable t) {
            trace(msg);
        }

        @Override
        public void warn(final Marker marker, final String msg) {
            warn(msg);
        }

        @Override
        public void warn(final Marker marker, final String format, final Object arg) {
            warn(format);
        }

        @Override
        public void warn(final Marker marker, final String format, final Object... arguments) {
            warn(format);
        }

        @Override
        public void warn(final Marker marker, final String format, final Object arg1, final Object arg2) {
            warn(format);
        }

        @Override
        public void warn(final Marker marker, final String msg, final Throwable t) {
            warn(msg);

        }

        @Override
        public void warn(final String msg) {
            warn(msg);
        }

        @Override
        public void warn(final String msg, final Object arg) {
            warn(msg);
        }

        @Override
        public void warn(final String msg, final Object... args) {
            warn(msg);
        }

        @Override
        public void warn(final String msg, final Object arg1, final Object arg2) {
            warn(msg);
        }

        @Override
        public void warn(final String msg, final Throwable throwable) {
            warn(msg);
        }
    }

    @Test
    void test() throws IOException {
        final var ref = new AtomicReference<String>();
        final var a = new LoggerAppendable(new AbstractLogger() {

            @Override
            public void debug(final String msg) {
                fail();
            }

            @Override
            public void error(final String msg) {
                fail();
            }

            @Override
            public void info(final String msg) {
                ref.set(msg);
            }

            @Override
            public void trace(final String msg) {
                fail();
            }

            @Override
            public void warn(final String msg) {
                fail();
            }

        });

        final var b = a.append('t');
        assertSame(a, b);
        assertNull(ref.get());
        final var c = b.append("ut");
        assertSame(a, c);
        assertNull(ref.get());
        final var d = c.append("toto", 1, 2);
        assertSame(a, d);
        assertNull(ref.get());
        a.close();
        assertEquals("tuto", ref.get());

    }

}
