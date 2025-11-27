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
import java.nio.file.Path;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.xml.sax.SAXException;

import bje.buildtools.dependency.explorer.ExplorerSensor;
import bje.buildtools.dependency.explorer.data.Pom;
import bje.buildtools.dependency.explorer.maven.tasks.DependencyAnalysis;
import bje.buildtools.dependency.explorer.maven.tasks.DependencyTree;
import bje.buildtools.dependency.explorer.maven.tasks.ProjectInfoDependencies;
import bje.buildtools.dependency.explorer.maven.tasks.ProjectInfoDependencyConvergence;
import bje.buildtools.dependency.explorer.maven.tasks.VersionUpdates;
import bje.buildtools.dependency.explorer.data.Dependency;
import bje.buildtools.dependency.explorer.data.POMType;
import bje.buildtools.dependency.explorer.util.ExplorationConfiguration;
import bje.buildtools.dependency.explorer.util.InputFileUtils;

public class PomFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(PomFactory.class);
    private static final String DONE = "........................................................................... done";

    private static String getArtefactDirectoryPath(final String baseDirectoryPath, final String groupId,
            final String artifactId, final String artifactVersion) {
        return (baseDirectoryPath.endsWith("/") ? baseDirectoryPath : baseDirectoryPath + "/")
                + groupId.replace('.', '/') + "/" + artifactId + "/" + artifactVersion + "/";
    }

    private static String getArtefactFile(final String artifactId, final String artifactVersion) {
        return artifactId + "-" + artifactVersion + ".pom";
    }

    public static Pom resolve(final InputFile file, final MavenSettings msettings,
            final ExplorationConfiguration config) throws IOException, SAXException, ParserConfigurationException {
        return resolve(file, msettings, config, null, POMType.MAIN);
    }

    public static Pom resolve(final InputFile file, final MavenSettings msettings,
            final ExplorationConfiguration config, final Pom parent)
            throws IOException, SAXException, ParserConfigurationException {
        return resolve(file, msettings, config, parent, POMType.MODULE);
    }

    public static Pom resolve(final InputFile file, final MavenSettings msettings,
            final ExplorationConfiguration config, final Pom parent, final POMType type)
            throws IOException, SAXException, ParserConfigurationException {
        LOGGER.info("Creating POM for : {}", file);
        final var pom = new Pom(file, parent, type);
        LOGGER.trace("Read POM for parent and properties");
        final var fp = PomParser.firstParse(pom);
        LOGGER.trace("Create Maven resolver");
        if (parent == null && fp.getParent() != null) {
            final var parentPomDef = new Dependency(pom, fp.getParent());
            final var gav = parentPomDef.toGAV();
            LOGGER.debug("Loading parent pom : {}", gav);
            final var f = Path.of(getArtefactDirectoryPath(msettings.localRepository + "/repository",
                    parentPomDef.getGroupId(), parentPomDef.getArtifactId(), parentPomDef.getVersion()) + "/"
                    + getArtefactFile(parentPomDef.getArtifactId(), parentPomDef.getVersion()));
            if (f != null) {
                LOGGER.debug("Loading parent pom file : {}", f.toAbsolutePath());
                final var parentFile = InputFileUtils.loadFile(f);
                final var p = resolve(parentFile, msettings, config, null, POMType.PARENT);
                pom.setParent(p);
            }
        }
        pom.fill(fp);
        LOGGER.trace("Read POM for dependencies");
        PomParser.secondParse(pom, config.parsePlugin);
        if (POMType.MAIN == pom.getType() || POMType.MODULE == pom.getType()) {
            LOGGER.trace("Resolve all dependencies");
            final var executor = new MavenExecutor(pom, msettings, config);
            DependencyTree.analyse(executor);
            executor.analyse();
            pom.updateRoot();
        }
        PomParser.thirdParse(pom, ExplorerSensor.LICENSE_MODEL.get(), config.parsePlugin);
        if (POMType.PARENT != pom.getType()) {
            final var executor = new MavenExecutor(pom, msettings, config);
            LOGGER.trace("Inscribe dependencies project info reports : check transitive dependency, licenses");
            ProjectInfoDependencies.analyse(executor);
            LOGGER.trace("Inscribe analysis reports : check usage and declaration");
            DependencyAnalysis.analyse(executor);
            LOGGER.trace("Inscribe dependency convergence project info reports : check convergence");
            ProjectInfoDependencyConvergence.analyse(executor);
            LOGGER.trace("Inscribe updates reports : check available updates");
            VersionUpdates.analyse(executor, config);
            executor.analyse();
            LOGGER.trace(DONE);
        }
        LOGGER.trace("Created");
        return pom;
    }

    private PomFactory() {
        // block default constructor
    }
}
