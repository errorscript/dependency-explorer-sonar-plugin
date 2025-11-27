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

import static bje.buildtools.dependency.explorer.data.DependencyType.DEPENDENCY;
import static bje.buildtools.dependency.explorer.data.DependencyType.DEPENDENCY_MANAGEMENT;
import static bje.buildtools.dependency.explorer.data.DependencyType.PLUGIN;
import static bje.buildtools.dependency.explorer.data.DependencyType.PLUGIN_MANAGEMENT;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import bje.buildtools.dependency.explorer.data.Artifact;
import bje.buildtools.dependency.explorer.data.Pom;
import bje.buildtools.dependency.explorer.data.DependencyType;
import bje.buildtools.dependency.explorer.filter.Filter;
import bje.buildtools.dependency.explorer.maven.MavenExecutor;
import bje.buildtools.dependency.explorer.util.ExplorationConfiguration;
import bje.buildtools.dependency.explorer.util.Utils;
import bje.toolbox.xml.XMLMap;
import bje.toolbox.xml.XMLMapper;
import bje.toolbox.xml.XMLMappingHandler;
import bje.toolbox.xml.XMLMultiMappingHandler;

public class VersionUpdates {
    private abstract static class AbstractUpdateMapper implements XMLMapper {
        protected final ExplorationConfiguration conf;
        protected final Pom pom;
        protected final DependencyType source;

        public AbstractUpdateMapper(final DependencyType aSource, final Pom aPom,
                final ExplorationConfiguration aConf) {
            source = aSource;
            conf = aConf;
            pom = aPom;
        }
    }

    private static class ClassicReportMapper extends AbstractUpdateMapper {
        private static XMLMapper versionAdder(final String aGroupId, final String anArtifactId,
                final Filter exclusionFilter, final Pattern aPattern, final List<String> aList) {
            return m -> {
                final var version = m.get("").trim();
                if (aPattern.matcher(version.trim()).find()
                        && !exclusionFilter.isInFilter(aGroupId, anArtifactId, version)) {
                    aList.add(version);
                }
            };
        }

        private boolean used = false;

        public ClassicReportMapper(final DependencyType aSource, final Pom aPom,
                final ExplorationConfiguration aConf) {
            super(aSource, aPom, aConf);
        }

        public boolean isUsed() {
            return used;
        }

        @Override
        public void map(final XMLMap map) {
            final var groupId = map.get("/groupId");
            final var artifactId = map.get("/artifactId");
            final var version = map.get("/currentVersion");
            final var artifact = new Artifact(groupId, artifactId, version);
            final List<String> versions = new ArrayList<>();
            map.forEach("/incrementals/incremental", versionAdder(artifact.getGroupId(), artifact.getArtifactId(),
                    conf.exclusionFilter, conf.versionsPattern, versions));
            map.forEach("/minors/minor", versionAdder(artifact.getGroupId(), artifact.getArtifactId(),
                    conf.exclusionFilter, conf.versionsPattern, versions));
            map.forEach("/majors/major", versionAdder(artifact.getGroupId(), artifact.getArtifactId(),
                    conf.exclusionFilter, conf.versionsPattern, versions));
            pom.addDependency(artifact, versions, source);
            used = true;
        }
    }

    private static class PropertyReportMapper extends AbstractUpdateMapper {

        private static XMLMapper simpleVersionAdder(final Pattern aPattern, final List<String> aList) {
            return m -> {
                final var version = m.get("");
                final var matcher = aPattern.matcher(version);
                if (matcher.find()) {
                    aList.add(version);
                }
            };
        }

        private boolean used = false;

        public PropertyReportMapper(final Pom aPom, final ExplorationConfiguration aConf) {
            super(DEPENDENCY, aPom, aConf);
        }

        public boolean isUsed() {
            return used;
        }

        @Override
        public void map(final XMLMap map) {
            final var propertyName = map.get("/propertyName");
            final List<Artifact> llist = new ArrayList<>();
            map.forEach("/propertyAssociations/propertyAssociation", m -> {
                final var groupId = m.get("/groupId");
                final var artifactId = m.get("/artifactId");
                final var upd = new Artifact(groupId, artifactId, null);
                upd.setPropertyName(propertyName);
                llist.add(upd);
            });
            final List<String> versions = new ArrayList<>();
            map.forEach("/incrementals/incremental", simpleVersionAdder(conf.versionsPattern, versions));
            map.forEach("/minors/minor", simpleVersionAdder(conf.versionsPattern, versions));
            map.forEach("/majors/major", simpleVersionAdder(conf.versionsPattern, versions));
            for (final Artifact upd : llist) {
                pom.addDependency(upd, versions, source);
                used = true;
            }
        }
    }

    public static void analyse(final MavenExecutor executor, final ExplorationConfiguration config) {
        executor.addPlugin(VersionUpdates::parseDependencies, "target/dependency-updates-report.xml",
                "-DdependencyUpdatesReportFormats=xml", "versions:dependency-updates-report");
        executor.addPlugin(VersionUpdates::parseProperties, "target/property-updates-report.xml",
                "-DpropertyUpdatesReportFormats=xml", "versions:property-updates-report");
        if (config.parsePlugin) {
            executor.addPlugin(VersionUpdates::parsePlugins, "target/plugin-updates-report.xml",
                    "-DpluginUpdatesReportFormats=xml", "versions:plugin-updates-report");
        }
    }

    public static boolean parseDependencies(final Path file, final Pom pom,
            final ExplorationConfiguration config) throws SAXException, IOException, ParserConfigurationException {
        final var dependencySaver = new ClassicReportMapper(DEPENDENCY, pom, config);
        final var dependencyManagementSaver = new ClassicReportMapper(DEPENDENCY_MANAGEMENT, pom, config);
        final var saxParser = Utils.getSAXParser();
        saxParser.parse(Files.newInputStream(file),
                new XMLMultiMappingHandler(
                        new XMLMappingHandler(dependencySaver, "/DependencyUpdatesReport/dependencies/dependency"),
                        new XMLMappingHandler(dependencyManagementSaver,
                                "/DependencyUpdatesReport/dependencyManagements/dependencyManagement")));
        return dependencySaver.isUsed() || dependencyManagementSaver.isUsed();
    }

    public static boolean parsePlugins(final Path file, final Pom pom, final ExplorationConfiguration config)
            throws SAXException, IOException, ParserConfigurationException {
        final var pluginSaver = new ClassicReportMapper(PLUGIN, pom, config);
        final var pluginManagementSaver = new ClassicReportMapper(PLUGIN_MANAGEMENT, pom, config);
        final var saxParser = Utils.getSAXParser();
        saxParser.parse(Files.newInputStream(file),
                new XMLMultiMappingHandler(new XMLMappingHandler(pluginSaver, "/PluginUpdatesReport/plugins/plugin"),
                        new XMLMappingHandler(pluginManagementSaver,
                                "/PluginUpdatesReport/pluginManagements/pluginManagement")));
        return pluginSaver.isUsed() || pluginManagementSaver.isUsed();
    }

    public static boolean parseProperties(final Path file, final Pom pom, final ExplorationConfiguration config)
            throws SAXException, IOException, ParserConfigurationException {
        final var propSaver = new PropertyReportMapper(pom, config);
        final var saxParser = Utils.getSAXParser();
        saxParser.parse(Files.newInputStream(file),
                new XMLMappingHandler(propSaver, "/PropertyUpdatesReport/properties/property"));
        return propSaver.isUsed();
    }

    private VersionUpdates() {
        // Block default constructor
    }
}
