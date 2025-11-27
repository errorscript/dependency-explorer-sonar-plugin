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
package bje.buildtools.dependency.explorer.license;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LicenseFamilyTest {

    @Test
    void test() {
        final var family = new LicenseFamily("name");
        family.addIntegration("truc1", IntegrationType.INCLUDE);
        family.addIntegration("truc2", IntegrationType.INCLUDE);
        family.addIntegration("truc3", IntegrationType.EXCLUDE);
        family.addIntegration("truc4", IntegrationType.EXCLUDE);
        assertFalse(family.isCopyleft());
        family.setCopyleft(true);
        assertTrue(family.isCopyleft());
        family.addParameter("machin", "152");
        family.addParameter("by", "true");

        assertEquals("name", family.toString());
        assertEquals("name", family.getName());
        assertEquals(2, family.getParameters().size());
        assertEquals("152", family.getParameters().get("machin"));
        assertEquals("true", family.getParameters().get("by"));

        assertEquals(2, family.getIntegration(IntegrationType.INCLUDE).size());
        assertEquals("truc1", family.getIntegration(IntegrationType.INCLUDE).get(0));
        assertEquals("truc2", family.getIntegration(IntegrationType.INCLUDE).get(1));
        assertEquals(2, family.getIntegration(IntegrationType.EXCLUDE).size());
        assertEquals("truc3", family.getIntegration(IntegrationType.EXCLUDE).get(0));
        assertEquals("truc4", family.getIntegration(IntegrationType.EXCLUDE).get(1));

        final var same = new LicenseFamily("name");
        final var notsame = new LicenseFamily("name2");
        assertFalse(family.equals(null));
        assertFalse(family.equals(new Object()));
        assertEquals(family, same);
        assertNotEquals(family, notsame);
        assertEquals(family.hashCode(), same.hashCode());
        assertNotEquals(family.hashCode(), notsame.hashCode());

        assertEquals(0, family.compareTo(same));
        assertEquals(-1, family.compareTo(notsame));
    }

}
