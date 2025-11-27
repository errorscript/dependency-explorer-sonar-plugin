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
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import bje.buildtools.dependency.explorer.data.Artifact;
import bje.buildtools.dependency.explorer.data.Pom;
import bje.buildtools.dependency.explorer.data.Dependency;
import bje.buildtools.dependency.explorer.data.DependencyType;
import bje.buildtools.dependency.explorer.data.FiledRange;
import bje.buildtools.dependency.explorer.license.LicenseDefinition;
import bje.buildtools.dependency.explorer.license.LicenseIdentity;
import bje.buildtools.dependency.explorer.license.LicenseModel;
import bje.buildtools.dependency.explorer.util.Utils;
import bje.toolbox.xml.Data;
import bje.toolbox.xml.XMLMap;
import bje.toolbox.xml.XMLMapper;
import bje.toolbox.xml.XMLMappingHandler;
import bje.toolbox.xml.XMLMultiMappingHandler;

public class PomParser {
    @FunctionalInterface
    public interface ArtifactAdder {
        Dependency add(final Artifact artifact, final boolean management);
    }

    public static class ArtifactMapper implements XMLMapper {
        private final ArtifactAdder adder;
        private final SimpleArtifactAdder effectiveAdder;
        protected Pom file;
        protected DependencyType type;

        public ArtifactMapper(final Pom aFile, final DependencyType aType, final ArtifactAdder anAdder,
                final SimpleArtifactAdder anEffectiveAdder) {
            file = aFile;
            type = aType;
            adder = anAdder;
            effectiveAdder = anEffectiveAdder;
        }

        @Override
        public void map(final XMLMap map) {
            final var optional = map.get(OPTIONAL);
            if (Boolean.TRUE.equals(Boolean.valueOf(optional))) {
                return;
            }
            final var groupId = map.get(GROUP_ID);
            final var artifactId = map.get(ARTIFACT_ID);
            final var version = map.get(VERSION);
            final var ptype = map.get(TYPE);
            final var art = new Artifact(groupId, artifactId, version);
            art.setType(ptype);
            final var scope = map.get("/scope");
            if (scope != null) {
                art.setScope(scope);
            }
            art.setRange(new FiledRange(file.getComponent(), map.getRange(VERSION), version));
            if (version != null && version.startsWith("${")) {
                art.setEffectiveVersion(file.resolveProperty(version));
                art.setPropertyName(version);
            } else if (version == null && !type.isManaged()) {
                final var mdef = file.getAnyDependency(groupId, artifactId);
                art.setEffectiveVersion(mdef.getEffectiveVersion());
            } else {
                art.setEffectiveVersion(version);
            }
            final var def = adder.add(art, type.isManaged());
            if (type == DependencyType.DEPENDENCY) {
                file.getRoot().addDependency(def);
            }
            if (!type.isManaged()) {
                effectiveAdder.add(art);
            }
        }
    }

    public static class FirstParse {

        private Artifact parent;
        private String groupId;
        private String artifactId;
        private String version;

        public String getArtifactId() {
            return artifactId;
        }

        public String getGroupId() {
            return groupId;
        }

        public Artifact getParent() {
            return parent;
        }

        public String getVersion() {
            return version;
        }

        public void setArtifactId(final String string) {
            artifactId = string;
        }

        public void setGroupId(final String string) {
            groupId = string;
        }

        public void setPOMParent(final String groupId, final String artifactId, final String version) {
            parent = new Artifact(groupId, artifactId, version);
        }

        public void setVersion(final String string) {
            version = string;
        }

    }

    public static class PropertyMapper implements XMLMapper {

        protected Pom file;

        public PropertyMapper(final Pom aFile) {
            file = aFile;
        }

        @Override
        public void map(final XMLMap map) {
            for (final Entry<String, Data> entry : map.entrySet()) {
                file.addPropertyLocation(entry.getKey().substring(1),
                        new FiledRange(file.getComponent(), entry.getValue().getRange(), entry.getValue().getText()));
            }
        }

    }

    @FunctionalInterface
    public interface SimpleArtifactAdder {
        void add(final Artifact artifact);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(PomParser.class);
    private static final String ARTIFACT_ID = "/artifactId";
    private static final String GROUP_ID = "/groupId";
    private static final String OPTIONAL = "/optional";
    private static final String TYPE = "/type";

    private static final String VERSION = "/version";

    public static FirstParse firstParse(final Pom file)
            throws SAXException, IOException, ParserConfigurationException {
        final var fp = new FirstParse();
        final var saxParser = Utils.getSAXParser();
        final var parentParser = new XMLMappingHandler(m -> {
            final var groupId = m.get(GROUP_ID);
            final var artifactId = m.get(ARTIFACT_ID);
            final var version = m.get(VERSION);
            fp.setPOMParent(groupId, artifactId, version);
        }, "/project/parent");
        final var artifactParser = new XMLMappingHandler(m -> {
            fp.setGroupId(m.get(GROUP_ID));
            fp.setArtifactId(m.get(ARTIFACT_ID));
            fp.setVersion(m.get(VERSION));
        }, "/project");
        final var propParser = new XMLMappingHandler(new PropertyMapper(file), "/project/properties");
        final var handler = new XMLMultiMappingHandler(parentParser, propParser, artifactParser);
        try (final var is = file.getComponent().inputStream()) {
            saxParser.parse(is, handler);
        }
        return fp;
    }

    public static void fullParse(final Pom file, final LicenseModel model)
            throws SAXException, IOException, ParserConfigurationException {
        final var fp = firstParse(file);
        file.fill(fp);
        secondParse(file, true);
        thirdParse(file, model, true);
    }

    public static void secondParse(final Pom file, final boolean parsePlugin)
            throws SAXException, IOException, ParserConfigurationException {
        final var saxParser = Utils.getSAXParser();
        final var moduleParser = new XMLMappingHandler(n -> file.addModule(n.get("")), "/project/modules/module");
        final var depMgmtParser = new XMLMappingHandler(new ArtifactMapper(file, DependencyType.DEPENDENCY_MANAGEMENT,
                file::addDependency, file::addEffectiveDependency),
                "/project/dependencyManagement/dependencies/dependency");
        var handler = new XMLMultiMappingHandler(depMgmtParser, moduleParser);
        if (parsePlugin) {
            final var pluginMgmtParser = new XMLMappingHandler(
                    new ArtifactMapper(file, DependencyType.PLUGIN_MANAGEMENT, file::addPlugin, a -> {
                    }), "/project/build/pluginManagement/plugins/plugin");
            handler = new XMLMultiMappingHandler(depMgmtParser, pluginMgmtParser, moduleParser);
        }
        try (final var is = file.getComponent().inputStream()) {
            saxParser.parse(is, handler);
        }
    }

    public static void thirdParse(final Pom file, final LicenseModel model, final boolean parsePlugin)
            throws SAXException, IOException, ParserConfigurationException {
        final var saxParser = Utils.getSAXParser();
        final List<LicenseIdentity> licences = new ArrayList<>();
        final var licenseParser = new XMLMappingHandler(n -> {
            var g = n.get("/name");
            if (g != null && !g.isEmpty()) {
                if (g.startsWith("${")) {
                    final var gg = file.resolveProperty(g);
                    LOGGER.warn("Resolve property for license {} -> {}", g, gg);
                    g = gg;
                }
                final var licenses = model.getLicense(g);
                if (licenses != null) {
                    licences.addAll(licenses);
                } else {
                    LOGGER.warn("No license found for {}", g);
                }
            }
        }, "/project/licenses/license");
        final var depParser = new XMLMappingHandler(
                new ArtifactMapper(file, DependencyType.DEPENDENCY, file::addDependency, file::addEffectiveDependency),
                "/project/dependencies/dependency");
        var handler = new XMLMultiMappingHandler(licenseParser, depParser);
        if (parsePlugin) {
            final var pluginParser = new XMLMappingHandler(
                    new ArtifactMapper(file, DependencyType.PLUGIN, file::addPlugin, a -> {
                    }), "/project/build/plugins/plugin");
            handler = new XMLMultiMappingHandler(licenseParser, depParser, pluginParser);
        }
        try (final var is = file.getComponent().inputStream()) {
            saxParser.parse(is, handler);
        }
        file.getRoot().setLicenses(LicenseDefinition.of(licences));
    }
}
