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
package bje.buildtools.dependency.explorer.maven;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import bje.buildtools.dependency.explorer.TestUtil;
import bje.buildtools.dependency.explorer.data.Pom;
import bje.buildtools.dependency.explorer.data.POMType;
import bje.buildtools.dependency.explorer.license.LicenseParser;

class POMParserTest {

    @Test
    void test() throws IOException, SAXException, ParserConfigurationException {
        final var input = TestUtil.loadFile();
        final var model = LicenseParser.init(TestUtil.getConfiguration(new Properties()));
        final var c = new Pom(input, POMType.MAIN);
        PomParser.fullParse(c, model);

        assertEquals("spark-core", c.getName());
        assertEquals(3, c.getModules().size());
        assertEquals("sub-module-1", c.getModules().get(0));
        assertEquals("sub-module-2", c.getModules().get(1));
        assertEquals("sub-module-3", c.getModules().get(2));

        testList(Arrays.asList("io.moquette:moquette-broker", "org.codehaus.groovy:groovy",
                "org.junit.jupiter:junit-jupiter-engine", "org.mockito:mockito-core",
                "org.mockito:mockito-junit-jupiter"), c.getMapDependencies().keySet());
        testList(Arrays.asList("org.apache.maven.plugins:maven-antrun-plugin",
                "org.apache.maven.plugins:maven-dependency-plugin", "org.apache.maven.plugins:maven-enforcer-plugin"),
                c.getMapPlugins().keySet());
        testList(Arrays.asList("${mockito.version}"), c.getPropertiesLocation().keySet());

        final var tr = c.getTextRange("org.codehaus.groovy", "groovy");
        assertEquals(48, tr.start().line());
        assertEquals(13, tr.start().lineOffset());
        assertEquals(48, tr.end().line());
        assertEquals(19, tr.end().lineOffset());
    }

    private void testList(final List<String> asList, final Set<String> properties) {
        final var ai = asList.iterator();
        final var bi = properties.iterator();
        while (ai.hasNext() && bi.hasNext()) {
            assertEquals(ai.next(), bi.next());
        }
        assertFalse(ai.hasNext());
        assertFalse(bi.hasNext());
    }

}
