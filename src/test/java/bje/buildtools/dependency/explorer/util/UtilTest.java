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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UtilTest {
    @Test
    void byteToHex() {
        assertNull(Utils.bytesToHex(null));
        assertEquals("ffde", Utils.bytesToHex(new byte[] { -1, -34 }));
        assertEquals("abcd", Utils.bytesToHex(new byte[] { -85, -51 }));
        assertEquals("1234", Utils.bytesToHex(new byte[] { 18, 52 }));
    }

    @Test
    void compare() {
        assertEquals(-1, Utils.compare("a", "b"));
        assertEquals(0, Utils.compare("a", "a"));
        assertEquals(1, Utils.compare("a", null));
        assertEquals(-1, Utils.compare(null, "b"));
        assertEquals(0, Utils.compare(null, null));
    }

    @Test
    void test() {
        assertFalse(Utils.isEmptyOrNull("a"));
        assertTrue(Utils.isEmptyOrNull(null));
        assertTrue(Utils.isEmptyOrNull(""));
    }

}
