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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Arrays;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import bje.buildtools.dependency.explorer.TestUtil;
import bje.buildtools.dependency.explorer.license.LicenseModel;
import bje.buildtools.dependency.explorer.maven.PomParser;

class PomTest {

    @Test
    void test() throws IOException, SAXException, ParserConfigurationException {
        final var model = new LicenseModel();
        final var file = TestUtil.loadFile();
        final var pom = new Pom(file, POMType.MAIN);
        final var fp = PomParser.firstParse(pom);
        pom.fill(fp);
        PomParser.thirdParse(pom, model, true);
        final var defMain = pom.addDependency(new Artifact("group", "main", "1.0.0"), Arrays.asList(),
                DependencyType.DEPENDENCY);

        final var defA = pom.addDependency(new Artifact("group", "artifeactA", "1.0.0"), Arrays.asList("1.0.1"),
                DependencyType.DEPENDENCY);
        defA.addDependency(new Dependency(pom, new Artifact("group", "sub", "1.0.5")));
        final var defB = pom.addDependency(new Artifact("group", "artifeactB", "5.2.6"), Arrays.asList(),
                DependencyType.DEPENDENCY);
        defB.addDependency(new Dependency(pom, new Artifact("group", "sub", "1.0.6")));

        defMain.addDependency(defA);
        defMain.addDependency(defB);

        pom.updateRoot(defMain);

        final var print = """
                --------------------------------------------------------------------------------
                 DEPENDENCY TREE
                --------------------------------------------------------------------------------
                group:main:1.0.0
                +- group:artifeactA:1.0.0
                |  \\- group:sub:1.0.5
                \\- group:artifeactB:5.2.6
                   \\- group:sub:1.0.6

                """;
        final var sb = new StringBuilder(512);
        pom.printTree(sb);
        assertEquals(print, sb.toString());
    }

}
