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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

class LicenseMatrixTest {

    private static LicenseDefinition set(final LicenseIdentity... license) {
        return LicenseDefinition.of(Arrays.asList(license));
    }

    private LicenseIdentity license(final LicenseModel model, final String string) {
        return model.getLicense(string).get(0);
    }

    @Test
    void test() throws ParserConfigurationException, SAXException, IOException {
        final var localXml = """
                <licenses>\
                    <licenseFamilies>\
                        <licenseFamily>\
                            <name>MIT-like</name>\
                            <integration>\
                                <include>\
                                    <code>MIT-like</code>\
                                </include>\
                            </integration>\
                            <parameter>\
                                <copyleft>false</copyleft>\
                            </parameter>\
                        </licenseFamily>\
                        <licenseFamily>\
                            <name>BSD-like</name>\
                            <integration>\
                                <include>\
                                    <code>MIT-like</code>\
                                    <code>BSD-like</code>\
                                    <code>Public-domain</code>\
                                </include>\
                            </integration>\
                            <parameter>\
                                <copyleft>false</copyleft>\
                            </parameter>\
                        </licenseFamily>\
                    </licenseFamilies>\
                    <licenseIdentities>\
                        <licenseIdentity>\
                            <name>BSD</name>\
                            <family>BSD-like</family>\
                        </licenseIdentity>\
                        <licenseIdentity>\
                            <name>BSD-3-Clause</name>\
                            <family>BSD-like</family>\
                        </licenseIdentity>\
                        <licenseIdentity>\
                            <name>MIT</name>\
                            <family>MIT-like</family>\
                        </licenseIdentity>\
                    </licenseIdentities>\
                </licenses>""";

        final var model = new LicenseModel();
        LicenseParser.parse(model, new StringReader(localXml));
        model.validate();

        final Map<String, LicenseDefinition> map = new TreeMap<>();
        map.put("g:a:v", set(license(model, "bsd-3-clause")));
        final var comp = model.compatibility(set(license(model, "MIT")), map, "artifact name");
        assertTrue(comp.isProblematic());
        assertEquals(
                "Dependency artifact name is not compatible with project license (MIT) :\n  - g:a:v : BSD-3-Clause (BSD-like)\n",
                comp.getDescription());

        final Map<String, LicenseDefinition> map2 = new TreeMap<>();
        map2.put("g:a:v", set(license(model, "MIT")));
        final var comp2 = model.compatibility(set(license(model, "bsd-3-clause")), map2, "artifact name");
        assertFalse(comp2.isProblematic());
        assertEquals("Dependency artifact name is compatible license wise.", comp2.getDescription());

        final var licenses = set(license(model, "MIT"), license(model, "bsd"));
        final var a = model.reduceLicense(licenses.getComposition(), model::leastRestritive);
        assertEquals("BSD-like", a.getName());
        final var b = model.reduceLicense(licenses.getComposition(), model::mostRestritive);
        assertEquals("MIT-like", b.getName());
    }

    @Test
    void testFamilies() {
        final var matrix = new LicenseMatrix();
        final var a = new LicenseFamily("a");
        matrix.add(a);
        final var b = new LicenseFamily("b");
        b.addIntegration("a", IntegrationType.INCLUDE);
        matrix.add(b);
        final var c = new LicenseFamily("c");
        c.addIntegration("a", IntegrationType.INCLUDE);
        c.setCopyleft(true);
        matrix.add(c);
        final var d = new LicenseFamily("d");
        d.addIntegration("a", IntegrationType.INCLUDE);
        d.addParameter("truc", "machin");
        matrix.add(d);

        assertEquals(a, matrix.leastRestritive(a, null));
        assertEquals(b, matrix.leastRestritive(null, b));
        assertEquals(b, matrix.leastRestritive(a, b));
        assertEquals(b, matrix.leastRestritive(b, a));
        assertEquals(b, matrix.leastRestritive(b, c));
        assertEquals(b, matrix.leastRestritive(c, b));
        assertEquals(b, matrix.leastRestritive(b, d));
        assertEquals(b, matrix.leastRestritive(d, b));

        assertEquals(a, matrix.mostRestritive(a, null));
        assertEquals(b, matrix.mostRestritive(null, b));
        assertEquals(a, matrix.mostRestritive(a, b));
        assertEquals(a, matrix.mostRestritive(b, a));
        assertEquals(c, matrix.mostRestritive(b, c));
        assertEquals(c, matrix.mostRestritive(c, b));
        assertEquals(d, matrix.mostRestritive(b, d));
        assertEquals(d, matrix.mostRestritive(d, b));

    }

    @Test
    void testSimple() throws ParserConfigurationException, SAXException, IOException {
        final var xml = """
                <licenses>\
                    <licenseFamilies>\
                        <licenseFamily>\
                            <name>Test</name>\
                            <integration>\
                                <include>\
                                    <code>gpl</code>\
                                </include>\
                            </integration>\
                            <parameter>\
                                <copyleft>true</copyleft>\
                                <by>true</by>\
                            </parameter>\
                        </licenseFamily>\
                    </licenseFamilies>\
                </licenses>""";
        final var model = new LicenseModel();
        LicenseParser.parse(model, new StringReader(xml));

        assertFalse(model.getAllFamilies().isEmpty());
        final var iterator = model.getAllFamilies().iterator();
        assertTrue(iterator.hasNext());
        final var def = iterator.next();
        assertEquals("Test", def.getName());
        assertTrue(def.isCopyleft());
        assertEquals(1, def.getParameters().size());
        assertEquals("true", def.getParameters().get("by"));
        final var l = def.getIntegration(IntegrationType.INCLUDE);
        assertEquals(1, l.size());
        assertEquals("gpl", l.get(0));
        assertTrue(def.getIntegration(IntegrationType.EXCLUDE).isEmpty());
        assertFalse(iterator.hasNext());
    }
}
