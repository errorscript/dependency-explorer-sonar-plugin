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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import bje.buildtools.dependency.explorer.data.Pom;
import bje.buildtools.dependency.explorer.data.POMType;
import bje.buildtools.dependency.explorer.util.ExplorationConfiguration;

public final class MavenExecutor {
    public static class PluginExecution {

        public final String[] command;
        public final String resultFile;
        public final ReportParser parser;

        public PluginExecution(final String aResultFile, final ReportParser aParser, final String... aCommand) {
            command = aCommand;
            resultFile = aResultFile;
            parser = aParser;
        }
    }

    @FunctionalInterface
    public interface ReportParser {
        boolean parse(final Path file, final Pom pom, final ExplorationConfiguration config)
                throws SAXException, IOException, ParserConfigurationException;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenExecutor.class);

    public static int invoke(final Path file, final MavenSettings msettings, final ExplorationConfiguration config,
            final String... command) throws IOException {
        final var mvn = config.mavenExecutablePath;
        final var cmds = new String[4 + command.length];
        cmds[0] = mvn;
        cmds[1] = "-nsu";
        cmds[2] = "-U";
        cmds[3] = "-Dmaven.repo.local=" + msettings.localRepository + "/repository";
        System.arraycopy(command, 0, cmds, 4, command.length);
        final var executable = toString(cmds);
        var pb = new ProcessBuilder().directory(file.getParent().toFile());
        LOGGER.debug(" >>> {}", executable);
        LOGGER.debug("     [{}]", file.getParent().toAbsolutePath());
        if (config.debug) {
            pb = pb.inheritIO();
        }
        final var p = pb.command(cmds).start();
        while (p.isAlive()) {
            try {
                Thread.sleep(100L);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return p.exitValue();
    }

    private static String toString(final String[] command) {
        final var sb = new StringBuilder(512);
        for (var i = 0; i < command.length; ++i) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(command[i]);
        }
        return sb.toString();
    }

    private final List<PluginExecution> plugins = new ArrayList<>();
    private final Pom pom;

    private final MavenSettings msettings;

    private final ExplorationConfiguration config;

    public MavenExecutor(final Pom aPom, final MavenSettings aMsettings,
            final ExplorationConfiguration aConfig) {
        pom = aPom;
        msettings = aMsettings;
        config = aConfig;
    }

    public void addPlugin(final ReportParser parser, final String resultFile, final String... command) {
        plugins.add(new PluginExecution(resultFile, parser, command));
    }

    public void analyse() throws SAXException, IOException, ParserConfigurationException {
        final var pomFile = Path.of(pom.getComponent().uri());
        if (POMType.MAIN == pom.getType()) {
            LOGGER.debug("Execute inscribed analysis for {}", pom.getName());
            final var command = buildCommand();
            try {
                invoke(pomFile, msettings, config, command);
            } catch (final IOException e) {
                LOGGER.warn("Maven execution ended wrongly for command {} with pom : {}", command,
                        pomFile.toAbsolutePath());
                return;
            }
        }
        LOGGER.debug("Parse files for inscribed analisys for {}", pom.getName());
        for (final PluginExecution p : plugins) {
            var done = false;
            final var report = pomFile.getParent().resolve(p.resultFile);
            done = analyze(p, done, report);
            if (!done) {
                LOGGER.debug("     No report files generated : {}", p.resultFile);
            }
        }
    }

    private boolean analyze(final PluginExecution p, boolean done, final Path report)
            throws SAXException, IOException, ParserConfigurationException {
        if (Files.exists(report)) {
            LOGGER.debug("     Read report file : {}", report.toAbsolutePath());
            if (p.parser.parse(report, pom, config)) {
                LOGGER.debug(" !!! Found data");
            }
            done = true;
        }
        return done;
    }

    private String[] buildCommand() {
        final List<String> list = new ArrayList<>();
        for (final PluginExecution plugin : plugins) {
            Collections.addAll(list, plugin.command);
        }
        return list.toArray(new String[list.size()]);
    }

}
