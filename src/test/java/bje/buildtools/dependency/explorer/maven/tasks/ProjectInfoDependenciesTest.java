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
package bje.buildtools.dependency.explorer.maven.tasks;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import bje.buildtools.dependency.explorer.ExplorerSensor;
import bje.buildtools.dependency.explorer.TestUtil;
import bje.buildtools.dependency.explorer.data.POMType;
import bje.buildtools.dependency.explorer.data.Pom;
import bje.buildtools.dependency.explorer.filter.FilterList;
import bje.buildtools.dependency.explorer.license.LicenseIdentity;
import bje.buildtools.dependency.explorer.license.LicenseModel;
import bje.buildtools.dependency.explorer.license.LicenseParser;
import bje.buildtools.dependency.explorer.util.Constants;
import bje.buildtools.dependency.explorer.util.ExplorationConfiguration;

class ProjectInfoDependenciesTest {

    private static void exist(final LicenseModel model, final Pom pom, final String groupId,
            final String artifactId, final String licenses, final String... formated) {
        final var def = pom.getAnyDependency(groupId, artifactId);
        final var license = model.getLicense(licenses);
        if (formated == null) {
            assertNull(license);
            assertNull(def.getLicenses());
        } else {
            var i = 0;
            for (final LicenseIdentity li : def.getLicenses().getComposition()) {
                if (li.getName().equals(formated[i])) {
                    return;
                }
                ++i;
            }
            fail(groupId + " " + artifactId + " " + licenses + " " + formated);
        }
    }

    @Test
    void test() throws IOException, URISyntaxException, ParserConfigurationException, SAXException {
        final var model = LicenseParser.init(TestUtil.getConfiguration(new Properties()));
        ExplorerSensor.LICENSE_MODEL.set(model);
        final var classLoader = VersionUpdatesTest.class.getClassLoader();
        final var input = TestUtil.loadFile();
        final var pom = new Pom(input, POMType.MAIN);
        final var config = new ExplorationConfiguration(Pattern.compile(ExplorationConfiguration.REGEX_ALLOW_ALL),
                new FilterList(null), false, Constants.UPDATE_MAVEN_EXECUTABLE_PATH_DEFAULT, true);

        final var dfile = Path.of(classLoader.getResource("reports/dependencies.html").toURI());
        ProjectInfoDependencies.parse(dfile, pom, config);

        exist(model, pom, "org.awaitility", "awaitility", "Apache 2.0", "Apache-2.0");
        exist(model, pom, "org.junit.jupiter", "junit-jupiter-engine", "Eclipse Public License v2.0", "EPL-2.0");
        exist(model, pom, "org.mockito", "mockito-core", "MIT", "MIT");
        exist(model, pom, "org.mockito", "mockito-junit-jupiter", "MIT", "MIT");
        exist(model, pom, "com.formdev", "flatlaf", "The Apache License, Version 2.0", "Apache-2.0");
        exist(model, pom, "com.ibm.icu", "icu4j", "Unicode/ICU License", "ICU");
        exist(model, pom, "commons-codec", "commons-codec", "Apache License, Version 2.0", "Apache-2.0");
        exist(model, pom, "commons-logging", "commons-logging", "The Apache Software License, Version 2.0",
                "Apache-2.0");
        exist(model, pom, "io.dropwizard.metrics", "metrics-core", "Apache License 2.0", "Apache-2.0");
        exist(model, pom, "javax.activation", "activation",
                "Common Development and Distribution License (CDDL) v1.0", "CDDL-1.0");
        exist(model, pom, "javax.mail", "mail", "CDDLGPLv2+CE", "CDDL-1.0", "GPL-2.0-with-classpath-exception");
        exist(model, pom, "org.apache.httpcomponents", "fluent-hc", "Apache License, Version 2.0", "Apache-2.0");
        exist(model, pom, "org.apache.httpcomponents", "httpclient", "Apache License, Version 2.0", "Apache-2.0");
        exist(model, pom, "org.apache.httpcomponents", "httpcore", "Apache License, Version 2.0", "Apache-2.0");
        exist(model, pom, "org.apache.logging.log4j", "log4j-api", "Apache-2.0", "Apache-2.0");
        exist(model, pom, "org.apache.logging.log4j", "log4j-core", "Apache-2.0", "Apache-2.0");
        exist(model, pom, "org.slf4j", "slf4j-api", "MIT License", "MIT");
        exist(model, pom, "com.github.vandeseer", "easytable", "MIT License", "MIT");
        exist(model, pom, "commons-io", "commons-io", "Apache-2.0", "Apache-2.0");
        exist(model, pom, "info.picocli", "picocli", "The Apache Software License, version 2.0", "Apache-2.0");
        exist(model, pom, "net.bytebuddy", "byte-buddy", "Apache License, Version 2.0", "Apache-2.0");
        exist(model, pom, "net.bytebuddy", "byte-buddy-agent", "Apache License, Version 2.0", "Apache-2.0");
        exist(model, pom, "org.apache.pdfbox", "fontbox", "Apache-2.0", "Apache-2.0");
        exist(model, pom, "org.apache.pdfbox", "pdfbox", "Apache-2.0", "Apache-2.0");
        exist(model, pom, "org.apache.pdfbox", "pdfbox-debugger", "Apache-2.0", "Apache-2.0");
        exist(model, pom, "org.apache.pdfbox", "pdfbox-io", "Apache-2.0", "Apache-2.0");
        exist(model, pom, "org.apache.pdfbox", "pdfbox-tools", "Apache-2.0", "Apache-2.0");
        exist(model, pom, "org.apiguardian", "apiguardian-api", "The Apache License, Version 2.0", "Apache-2.0");
        exist(model, pom, "org.hamcrest", "hamcrest", "BSD Licence 3", "BSD-3-Clause");
        exist(model, pom, "org.jfree", "jfreechart", "GNU Lesser General Public Licence", "LGPL-3.0");
        exist(model, pom, "org.junit.jupiter", "junit-jupiter", "Eclipse Public License v2.0", "EPL-2.0");
        exist(model, pom, "org.junit.jupiter", "junit-jupiter-api", "Eclipse Public License v2.0", "EPL-2.0");
        exist(model, pom, "org.junit.jupiter", "junit-jupiter-params", "Eclipse Public License v2.0", "EPL-2.0");
        exist(model, pom, "org.junit.platform", "junit-platform-commons", "Eclipse Public License v2.0", "EPL-2.0");
        exist(model, pom, "org.junit.platform", "junit-platform-engine", "Eclipse Public License v2.0", "EPL-2.0");
        exist(model, pom, "org.objenesis", "objenesis", "Apache License, Version 2.0", "Apache-2.0");
        exist(model, pom, "org.opentest4j", "opentest4j", "The Apache License, Version 2.0", "Apache-2.0");
    }

}
