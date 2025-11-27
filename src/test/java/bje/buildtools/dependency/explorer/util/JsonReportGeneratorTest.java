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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import bje.buildtools.dependency.explorer.JSONEqualityUtil;
import bje.buildtools.dependency.explorer.TestUtil;
import bje.buildtools.dependency.explorer.data.AbstractModifiableResult;
import bje.buildtools.dependency.explorer.data.Artifact;
import bje.buildtools.dependency.explorer.data.Pom;
import bje.buildtools.dependency.explorer.data.Dependency;
import bje.buildtools.dependency.explorer.data.DependencyType;
import bje.buildtools.dependency.explorer.data.POMType;
import bje.buildtools.dependency.explorer.data.Result;
import bje.buildtools.dependency.explorer.license.LicenseDefinition;
import bje.buildtools.dependency.explorer.license.LicenseIdentity;

class JsonReportGeneratorTest {

    private static LicenseDefinition license(final String... license) {
        final List<LicenseIdentity> ids = new ArrayList<>();
        final List<String> list = Arrays.asList(license);
        for (String n : list) {
            ids.add(new LicenseIdentity(n, null));
        }
        return LicenseDefinition.of(ids);
    }

    @Test
    void test() throws IOException {
        final var file = TestUtil.loadFile();
        final var pom = new Pom(file, POMType.MAIN);
        pom.fill(new Artifact("com.sparkjava", "spark-core", "2.9.4"));
        final var defMain = pom.addDependency(new Artifact("group", "main", "1.0.0"), Arrays.asList(),
                DependencyType.DEPENDENCY);
        defMain.setLicenses(
                new LicenseDefinition("BSD-3-Clause", Arrays.asList(new LicenseIdentity("BSD-3-Clause", null))));
        final var defA = pom.addDependency(new Artifact("group", "artifeactA", "1.0.0"), Arrays.asList("1.0.1"),
                DependencyType.DEPENDENCY);
        defA.addDependency(new Dependency(pom, new Artifact("group", "sub", "1.0.5")));
        final var art = new Artifact("group", "artifeactB", "5.2.6");
        art.setScope("test");
        final var defB = pom.addDependency(art, Arrays.asList(), DependencyType.DEPENDENCY);
        defB.setPropertyName("artifeactB.version");
        final var sub = new Dependency(pom, new Artifact("group", "sub", "1.0.6"));
        sub.setLicenses(license("MIT", "APL2"));
        defB.addDependency(sub);

        defMain.addDependency(defA);
        defMain.addDependency(defB);

        pom.updateRoot(defMain);

        final var print = "[{\"dependencies\":{\"spark-core\":[{\"artifactId\":\"main\",\"children\":[{\"artifactId\":\"artifeactA\",\"children\":[{\"artifactId\":\"sub\",\"groupId\":\"group\",\"level\":2,\"source\":\"DEPENDENCY\",\"version\":\"1.0.5\"}],\"groupId\":\"group\",\"lastVersion\":\"1.0.1\",\"level\":1,\"nextVersion\":\"1.0.1\",\"source\":\"DEPENDENCY\",\"version\":\"1.0.0\"},{\"artifactId\":\"artifeactB\",\"children\":[{\"artifactId\":\"sub\",\"groupId\":\"group\",\"level\":2,\"licenses\":[\"APL2\", \"MIT\"],\"source\":\"DEPENDENCY\",\"version\":\"1.0.6\"}],\"groupId\":\"group\",\"level\":1,\"propertyName\":\"artifeactB.version\",\"scope\":\"test\",\"source\":\"DEPENDENCY\",\"version\":\"5.2.6\"}],\"groupId\":\"group\",\"level\":0,\"licenses\":[\"BSD-3-Clause\"],\"source\":\"DEPENDENCY\",\"version\":\"1.0.0\"}]}}]"; //

        final var generator = new JsonReportGenerator();
        final Result res = new AbstractModifiableResult(Collections.emptySet(), pom) {

            @Override
            public void print(final Appendable out) throws IOException {
                // nothing
            }
        };
        final List<Result> results = Collections.singletonList(res);
        generator.append(results);
        final var result = generator.generate();
        final var arr = new JSONArray();
        for (final JSONObject o : result) {
            arr.put(o);
        }
        System.out.println(print);
        System.out.println("------------------------------------");
        System.out.println(arr.toString());
        assertTrue(JSONEqualityUtil.equality(new JSONArray(print), arr));

    }
}
