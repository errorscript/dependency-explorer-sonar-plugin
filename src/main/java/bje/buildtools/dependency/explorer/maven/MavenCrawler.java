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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import bje.buildtools.dependency.explorer.ExplorerSensor.InputFileCreator;
import bje.buildtools.dependency.explorer.data.Pom;
import bje.buildtools.dependency.explorer.util.ExplorationConfiguration;

public class MavenCrawler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MavenCrawler.class);

    public static Pom[] compileFiles(final InputFileCreator creator, final ExplorationConfiguration config)
            throws IOException, SAXException, ParserConfigurationException {
        final var msettings = MavenSettings.fromCommandLine();
        final List<Pom> files = new ArrayList<>();
        final var component = creator.create("pom.xml");
        if (component != null && component.isFile()) {
            LOGGER.debug("Load pom for {}", component);
            final var pom = PomFactory.resolve(component, msettings, config);
            files.add(pom);
            for (final String pathPrefix : pom.getModules()) {
                final var subComponent = creator.create(pathPrefix + "/pom.xml");
                if (subComponent != null) {
                    LOGGER.debug("Load pom for {}", subComponent);
                    files.add(PomFactory.resolve(subComponent, msettings, config, pom));
                }
            }
        } else {
            LOGGER.warn("No pom.xml found");
        }
        return files.toArray(new Pom[files.size()]);
    }

    private MavenCrawler() {
        // block default constructor
    }
}
