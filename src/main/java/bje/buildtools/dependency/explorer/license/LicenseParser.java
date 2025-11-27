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

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Configuration;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import bje.buildtools.dependency.explorer.util.Constants;
import bje.buildtools.dependency.explorer.util.Utils;
import bje.toolbox.xml.Data;
import bje.toolbox.xml.XMLMapper;
import bje.toolbox.xml.XMLMappingHandler;
import bje.toolbox.xml.XMLMultiMappingHandler;

public class LicenseParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(LicenseParser.class);

    private static final XMLMapper createFamiliesSaver(final LicenseModel model) {
        return map -> {
            final var family = map.get("/name");
            if (family != null && !family.isEmpty()) {
                final var license = new LicenseFamily(family);
                map.forEach("/integration/include/code",
                        m -> license.addIntegration(m.get(""), IntegrationType.INCLUDE));
                map.forEach("/integration/exclude/code",
                        m -> license.addIntegration(m.get(""), IntegrationType.EXCLUDE));
                map.forEach("/parameter", m -> {
                    for (final Entry<String, Data> entry : m.entrySet()) {
                        final var name = entry.getKey();
                        final var value = entry.getValue().getText();
                        if ("/copyleft".equals(name)) {
                            license.setCopyleft(Boolean.parseBoolean(value));
                        } else {
                            license.addParameter(name.substring(1), value);
                        }
                    }
                });
                model.addFamily(license);
            }
        };
    }

    private static final XMLMapper createIdentitiesSaver(final LicenseModel model) {
        return map -> {
            final var name = map.get("/name");
            final var family = map.get("/family");
            if (family == null || family.isEmpty()) {
                LOGGER.warn("No family '{}' for license '{}'", family, name);
            }
            final var license = new LicenseIdentity(name, family);
            model.addIdentity(name, license);
            map.forEach("/matching/match", m -> {
                final var match = m.get("");
                model.addIdentity(match, license);
            });
        };
    }

    public static LicenseModel init(final Configuration config)
            throws ParserConfigurationException, SAXException, IOException {
        final var model = new LicenseModel();
        var f = config.get(Constants.LICENSE_DEFINITION_PATH_PROPERTY);
        if (f.isEmpty()) {
            final var classLoader = LicenseMatrix.class.getClassLoader();
            LicenseParser.parse(model, new InputStreamReader(classLoader.getResourceAsStream("licenses-oss.xml")));
        } else {
            LicenseParser.parse(model, new FileReader(f.get()));
        }
        f = config.get(Constants.LICENSE_DEFINITION_PROPERTY);
        if (f.isPresent()) {
            LicenseParser.parse(model, new StringReader(f.get()));
        }
        model.validate();
        return model;
    }

    public static void parse(final LicenseModel model, final Reader reader)
            throws ParserConfigurationException, SAXException, IOException {
        final var families = new XMLMappingHandler(createFamiliesSaver(model),
                "/licenses/licenseFamilies/licenseFamily");
        final var identities = new XMLMappingHandler(createIdentitiesSaver(model),
                "/licenses/licenseIdentities/licenseIdentity");
        final var saxParser = Utils.getSAXParser();
        saxParser.parse(new InputSource(reader), new XMLMultiMappingHandler(families, identities));
    }

    private LicenseParser() {
        // Block default constructor
    }
}
