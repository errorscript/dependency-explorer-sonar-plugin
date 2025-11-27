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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenSettings {

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenSettings.class);

    private static String discoverLocalRepo() {
        var localRepo = System.getProperty("maven.repo.local");
        if (localRepo != null) {
            final var f = Path.of(localRepo);
            if (!Files.exists(f) || !Files.isDirectory(f)) {
                localRepo = null;
            }
        }
        if (localRepo == null) {
            var udir = Path.of(System.getProperty("user.dir"), ".m2");
            if (!Files.exists(udir) || !Files.isDirectory(udir)) {
                udir = Path.of(System.getProperty("user.home"), ".m2");
            }
            localRepo = udir.toAbsolutePath().toString();
        }
        LOGGER.debug("Using maven repo {}", localRepo);
        return localRepo;
    }

    public static MavenSettings fromCommandLine() {
        String globalSettings = null;
        String userSettings = null;
        var handle = ProcessHandle.current();
        while (handle != null) {
            final var commandArgs = handle.info().commandLine();
            if (commandArgs.isPresent()) {
                LOGGER.debug("Current command {}", commandArgs.get());
                try (final var scanner = new Scanner(commandArgs.get())) {
                    while (scanner.hasNext()) {
                        final var part = scanner.next();
                        switch (part) {
                        case "-gs", "--global-settings":
                            globalSettings = scanner.next();
                            LOGGER.debug("Using global settings {}", globalSettings);
                            break;
                        case "-s", "--settings":
                            userSettings = scanner.next();
                            LOGGER.debug("Using user settings {}", userSettings);
                            break;
                        default:
                        }
                    }
                }
            }
            final var opt = handle.parent();
            if (!opt.isPresent()) {
                break;
            }
            handle = opt.get();
        }
        final var localRepo = discoverLocalRepo();
        return new MavenSettings(globalSettings, userSettings, localRepo);
    }

    final String globalSettings;
    final String localRepository;
    final String userSettings;

    public MavenSettings(final String aGlobalSettings, final String aUserSetttings, final String aLocalRepository) {
        globalSettings = aGlobalSettings;
        userSettings = aUserSetttings;
        localRepository = aLocalRepository;
    }
}