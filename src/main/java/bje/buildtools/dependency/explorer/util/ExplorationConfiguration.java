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

import static bje.buildtools.dependency.explorer.util.Constants.FILTERING_EXCLUSIONS_LIST_PROPERTY;
import static bje.buildtools.dependency.explorer.util.Constants.FILTERING_ONLY_CLASSIC_VERSION_DEFAULT;
import static bje.buildtools.dependency.explorer.util.Constants.FILTERING_ONLY_CLASSIC_VERSION_PROPERTY;
import static bje.buildtools.dependency.explorer.util.Constants.GENERAL_SKIP_BUILD_PLUGIN_DEFAULT;
import static bje.buildtools.dependency.explorer.util.Constants.GENERAL_SKIP_BUILD_PLUGIN_PROPERTY;
import static bje.buildtools.dependency.explorer.util.Constants.UPDATE_MAVEN_EXECUTABLE_PATH_DEFAULT;
import static bje.buildtools.dependency.explorer.util.Constants.UPDATE_MAVEN_EXECUTABLE_PATH_PROPERTY;

import java.util.regex.Pattern;

import org.sonar.api.config.Configuration;

import bje.buildtools.dependency.explorer.filter.Filter;
import bje.buildtools.dependency.explorer.filter.FilterList;

public class ExplorationConfiguration {
    public static final String REGEX_ALLOW_ALL = "(.*)";
    public static final String REGEX_ONLY_CLASSIC = "^(\\d+\\.\\d+\\.\\d+)$";
    public static final Pattern CLASSIC_PATTERN = Pattern.compile(REGEX_ONLY_CLASSIC);
    public static final Pattern ALL_PATTERN = Pattern.compile(REGEX_ALLOW_ALL);

    public static ExplorationConfiguration of(final Configuration aConfiguration) {
        final var mvn = aConfiguration.get(UPDATE_MAVEN_EXECUTABLE_PATH_PROPERTY)
                .orElse(UPDATE_MAVEN_EXECUTABLE_PATH_DEFAULT);
        final boolean skipPlugin = aConfiguration.getBoolean(GENERAL_SKIP_BUILD_PLUGIN_PROPERTY)
                .orElse(GENERAL_SKIP_BUILD_PLUGIN_DEFAULT);
        final boolean onlyMainVersions = aConfiguration.getBoolean(FILTERING_ONLY_CLASSIC_VERSION_PROPERTY)
                .orElse(FILTERING_ONLY_CLASSIC_VERSION_DEFAULT);
        final Filter filter = new FilterList(aConfiguration.get(FILTERING_EXCLUSIONS_LIST_PROPERTY).orElse(null));
        if (onlyMainVersions) {
            return new ExplorationConfiguration(CLASSIC_PATTERN, filter, skipPlugin, mvn, true);
        }
        return new ExplorationConfiguration(ALL_PATTERN, filter, skipPlugin, mvn, true);
    }

    public final boolean debug;
    public final Filter exclusionFilter;
    public final String mavenExecutablePath;
    public final boolean parsePlugin;
    public final Pattern versionsPattern;

    public ExplorationConfiguration(final Pattern aVersionPattern, final Filter filter, final boolean skipPlugin,
            final String mavenExecutable, final boolean isDebug) {
        versionsPattern = aVersionPattern;
        exclusionFilter = filter;
        parsePlugin = !skipPlugin;
        mavenExecutablePath = mavenExecutable;
        debug = isDebug;
    }

}