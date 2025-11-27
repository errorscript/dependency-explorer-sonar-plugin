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
package bje.buildtools.dependency.explorer;

import static bje.buildtools.dependency.explorer.util.Constants.COHERENCE_MAJOR_SEVERITY_DEFAULT;
import static bje.buildtools.dependency.explorer.util.Constants.COHERENCE_MAJOR_SEVERITY_PROPERTY;
import static bje.buildtools.dependency.explorer.util.Constants.COHERENCE_MINOR_SEVERITY_DEFAULT;
import static bje.buildtools.dependency.explorer.util.Constants.COHERENCE_MINOR_SEVERITY_PROPERTY;
import static bje.buildtools.dependency.explorer.util.Constants.COHERENCE_PATCH_SEVERITY_DEFAULT;
import static bje.buildtools.dependency.explorer.util.Constants.COHERENCE_PATCH_SEVERITY_PROPERTY;
import static bje.buildtools.dependency.explorer.util.Constants.COHERENCE_SKIP_PROPERTY;
import static bje.buildtools.dependency.explorer.util.Constants.COHERENCE_SUB_CATEGORY;
import static bje.buildtools.dependency.explorer.util.Constants.FILTERING_EXCLUSIONS_LIST_PROPERTY;
import static bje.buildtools.dependency.explorer.util.Constants.FILTERING_ONLY_CLASSIC_VERSION_DEFAULT;
import static bje.buildtools.dependency.explorer.util.Constants.FILTERING_ONLY_CLASSIC_VERSION_PROPERTY;
import static bje.buildtools.dependency.explorer.util.Constants.FILTERING_SUB_CATEGORY;
import static bje.buildtools.dependency.explorer.util.Constants.GENERAL_PRINT_PROPERTY;
import static bje.buildtools.dependency.explorer.util.Constants.GENERAL_SKIP_BUILD_PLUGIN_DEFAULT;
import static bje.buildtools.dependency.explorer.util.Constants.GENERAL_SKIP_BUILD_PLUGIN_PROPERTY;
import static bje.buildtools.dependency.explorer.util.Constants.GENERAL_SKIP_DEFAULT;
import static bje.buildtools.dependency.explorer.util.Constants.GENERAL_SUB_CATEGORY;
import static bje.buildtools.dependency.explorer.util.Constants.GENRAL_SKIP_PROPERTY;
import static bje.buildtools.dependency.explorer.util.Constants.LICENSE_DEFINITION_PATH_PROPERTY;
import static bje.buildtools.dependency.explorer.util.Constants.LICENSE_DEFINITION_PROPERTY;
import static bje.buildtools.dependency.explorer.util.Constants.LICENSE_SEVERITY_PROPERTY;
import static bje.buildtools.dependency.explorer.util.Constants.LICENSE_SKIP_DEFAULT;
import static bje.buildtools.dependency.explorer.util.Constants.LICENSE_SKIP_PROPERTY;
import static bje.buildtools.dependency.explorer.util.Constants.LICENSE_SUB_CATEGORY;
import static bje.buildtools.dependency.explorer.util.Constants.TRANSITIVE_SEVERITY_PROPERTY;
import static bje.buildtools.dependency.explorer.util.Constants.TRANSITIVE_SKIP_DEFAULT;
import static bje.buildtools.dependency.explorer.util.Constants.TRANSITIVE_SKIP_PROPERTY;
import static bje.buildtools.dependency.explorer.util.Constants.TRANSITIVE_SUB_CATEGORY;
import static bje.buildtools.dependency.explorer.util.Constants.UNUSED_SEVERITY_PROPERTY;
import static bje.buildtools.dependency.explorer.util.Constants.UNUSED_SKIP_DEFAULT;
import static bje.buildtools.dependency.explorer.util.Constants.UNUSED_SKIP_PROPERTY;
import static bje.buildtools.dependency.explorer.util.Constants.UNUSED_SUB_CATEGORY;
import static bje.buildtools.dependency.explorer.util.Constants.UPDATE_MAJOR_SEVERITY_DEFAULT;
import static bje.buildtools.dependency.explorer.util.Constants.UPDATE_MAJOR_SEVERITY_PROPERTY;
import static bje.buildtools.dependency.explorer.util.Constants.UPDATE_MAVEN_EXECUTABLE_PATH_DEFAULT;
import static bje.buildtools.dependency.explorer.util.Constants.UPDATE_MAVEN_EXECUTABLE_PATH_PROPERTY;
import static bje.buildtools.dependency.explorer.util.Constants.UPDATE_MINOR_SEVERITY_DEFAULT;
import static bje.buildtools.dependency.explorer.util.Constants.UPDATE_MINOR_SEVERITY_PROPERTY;
import static bje.buildtools.dependency.explorer.util.Constants.UPDATE_PATCH_SEVERITY_DEFAULT;
import static bje.buildtools.dependency.explorer.util.Constants.UPDATE_PATCH_SEVERITY_PROPERTY;
import static bje.buildtools.dependency.explorer.util.Constants.UPDATE_SKIP_PROPERTY;
import static bje.buildtools.dependency.explorer.util.Constants.UPDATE_SUB_CATEGORY;
import static org.sonar.api.PropertyType.BOOLEAN;
import static org.sonar.api.PropertyType.STRING;
import static org.sonar.api.config.PropertyDefinition.ConfigScope.PROJECT;
import static org.sonar.api.rule.Severity.BLOCKER;
import static org.sonar.api.rule.Severity.CRITICAL;
import static org.sonar.api.rule.Severity.INFO;
import static org.sonar.api.rule.Severity.MAJOR;
import static org.sonar.api.rule.Severity.MINOR;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.config.PropertyDefinition;

public class ExplorerConfiguration {
    public static List<PropertyDefinition> getPropertyDefinitions() {
        return Arrays.asList(
                // GENERAL - SKIP
                PropertyDefinition.builder(GENRAL_SKIP_PROPERTY).onConfigScopes(PROJECT)
                        .subCategory(GENERAL_SUB_CATEGORY).name("Skip")
                        .description("When enabled this plugin is skipped.")
                        .defaultValue(Boolean.toString(GENERAL_SKIP_DEFAULT)).type(BOOLEAN).build(),
                // GENERAL - SKIP PLUGIN
                PropertyDefinition.builder(GENERAL_SKIP_BUILD_PLUGIN_PROPERTY).onConfigScopes(PROJECT)
                        .subCategory(GENERAL_SUB_CATEGORY).name("Skip plugin analyse")
                        .description("When enabled analysis only see dependencies and not plugins.")
                        .defaultValue(Boolean.toString(GENERAL_SKIP_BUILD_PLUGIN_DEFAULT)).type(BOOLEAN).build(),
                // GENERAL - PRINT
                PropertyDefinition.builder(GENERAL_PRINT_PROPERTY).onConfigScopes(PROJECT)
                        .subCategory(GENERAL_SUB_CATEGORY).name("Print")
                        .description("Print details during project analysis.")
                        .defaultValue(Boolean.toString(GENERAL_SKIP_DEFAULT)).type(BOOLEAN).build(),
                // LICENSE - SKIP
                PropertyDefinition.builder(LICENSE_SKIP_PROPERTY).onConfigScopes(PROJECT)
                        .subCategory(LICENSE_SUB_CATEGORY).name("Skip license rule").description("Skip licenses check.")
                        .defaultValue(Boolean.toString(LICENSE_SKIP_DEFAULT)).type(BOOLEAN).build(),
                // LICENSE - DATA
                PropertyDefinition.builder(LICENSE_DEFINITION_PROPERTY).onConfigScopes(PROJECT)
                        .subCategory(LICENSE_SUB_CATEGORY).name("Licenses definition")
                        .description("Additionnal license definition, in XML format.").type(STRING).build(),
                // LICENSE - PATH
                PropertyDefinition.builder(LICENSE_DEFINITION_PATH_PROPERTY).onConfigScopes(PROJECT)
                        .subCategory(LICENSE_SUB_CATEGORY).name("License definition path")
                        .description("License definition file path in project.").type(STRING).build(),

                // LICENSE - SEVERITY
                PropertyDefinition.builder(LICENSE_SEVERITY_PROPERTY).onConfigScopes(PROJECT)
                        .subCategory(LICENSE_SUB_CATEGORY).name("License incompatibility severity")
                        .description("Severity of license incompatibility detection.").type(STRING).build(),
                // COHERENCE - SKIP
                PropertyDefinition.builder(COHERENCE_SKIP_PROPERTY).onConfigScopes(PROJECT)
                        .subCategory(COHERENCE_SUB_CATEGORY).name("Skip coherence rule")
                        .description("Skip coherence check.").defaultValue(Boolean.toString(LICENSE_SKIP_DEFAULT))
                        .type(BOOLEAN).build(),
                // COHERENCE - PATCH
                PropertyDefinition.builder(COHERENCE_PATCH_SEVERITY_PROPERTY).onConfigScopes(PROJECT)
                        .subCategory(COHERENCE_SUB_CATEGORY).name("Patch level")
                        .description("Severity of an incoherence on patch level.")
                        .defaultValue(COHERENCE_PATCH_SEVERITY_DEFAULT).type(STRING)
                        .options(Arrays.asList(INFO, MINOR, MAJOR, CRITICAL, BLOCKER)).build(),
                // COHERENCE - MINOR
                PropertyDefinition.builder(COHERENCE_MINOR_SEVERITY_PROPERTY).onConfigScopes(PROJECT)
                        .subCategory(COHERENCE_SUB_CATEGORY).name("Minor level")
                        .description("Severity of an incoherence on minor level.")
                        .defaultValue(COHERENCE_MINOR_SEVERITY_DEFAULT).type(STRING)
                        .options(Arrays.asList(INFO, MINOR, MAJOR, CRITICAL, BLOCKER)).build(),
                // COHERENCE - MAJOR
                PropertyDefinition.builder(COHERENCE_MAJOR_SEVERITY_PROPERTY).onConfigScopes(PROJECT)
                        .subCategory(COHERENCE_SUB_CATEGORY).name("Major level")
                        .description("Severity of an incoherence on major level.")
                        .defaultValue(COHERENCE_MAJOR_SEVERITY_DEFAULT).type(STRING)
                        .options(Arrays.asList(INFO, MINOR, MAJOR, CRITICAL, BLOCKER)).build(),
                // UPDATE - SKIP
                PropertyDefinition.builder(UPDATE_SKIP_PROPERTY).onConfigScopes(PROJECT)
                        .subCategory(UPDATE_SUB_CATEGORY).name("Skip updates rule").description("Skip update check.")
                        .defaultValue(Boolean.toString(LICENSE_SKIP_DEFAULT)).type(BOOLEAN).build(),
                // UPDATE - PATCH
                PropertyDefinition.builder(UPDATE_PATCH_SEVERITY_PROPERTY).onConfigScopes(PROJECT)
                        .subCategory(UPDATE_SUB_CATEGORY).name("Patch level")
                        .description("Severity of a missing patch update.").defaultValue(UPDATE_PATCH_SEVERITY_DEFAULT)
                        .type(STRING).options(Arrays.asList(INFO, MINOR, MAJOR, CRITICAL, BLOCKER)).build(),
                // UPDATE - MINOR
                PropertyDefinition.builder(UPDATE_MINOR_SEVERITY_PROPERTY).onConfigScopes(PROJECT)
                        .subCategory(UPDATE_SUB_CATEGORY).name("Minor level")
                        .description("Severity of a missing minor update.").defaultValue(UPDATE_MINOR_SEVERITY_DEFAULT)
                        .type(STRING).options(Arrays.asList(INFO, MINOR, MAJOR, CRITICAL, BLOCKER)).build(),
                // UPDATE - MAJOR
                PropertyDefinition.builder(UPDATE_MAJOR_SEVERITY_PROPERTY).onConfigScopes(PROJECT)
                        .subCategory(UPDATE_SUB_CATEGORY).name("Major level")
                        .description("Severity of a missing major update.").defaultValue(UPDATE_MAJOR_SEVERITY_DEFAULT)
                        .type(STRING).options(Arrays.asList(INFO, MINOR, MAJOR, CRITICAL, BLOCKER)).build(),
                // UPDATE - MAVEN PATH
                PropertyDefinition.builder(UPDATE_MAVEN_EXECUTABLE_PATH_PROPERTY).onConfigScopes(PROJECT)
                        .subCategory(UPDATE_SUB_CATEGORY).name("Maven path")
                        .description("Maven executable path, use to generate versions updates reports.")
                        .defaultValue(UPDATE_MAVEN_EXECUTABLE_PATH_DEFAULT).type(STRING)
                        .options(Arrays.asList(INFO, MINOR, MAJOR, CRITICAL, BLOCKER)).build(),
                // UNUSED - SKIP
                PropertyDefinition.builder(UNUSED_SKIP_PROPERTY).onConfigScopes(PROJECT)
                        .subCategory(UNUSED_SUB_CATEGORY).name("Skip unused rule")
                        .description("Skip unused dependency check.")
                        .defaultValue(Boolean.toString(UNUSED_SKIP_DEFAULT)).type(BOOLEAN).build(),
                // UNUSED - SEVERITY
                PropertyDefinition.builder(UNUSED_SEVERITY_PROPERTY).onConfigScopes(PROJECT)
                        .subCategory(UNUSED_SUB_CATEGORY).name("Unused severity")
                        .description("Severity of unused library detection.").type(STRING).build(),
                // TRANSITIVE - SKIP
                PropertyDefinition.builder(TRANSITIVE_SKIP_PROPERTY).onConfigScopes(PROJECT)
                        .subCategory(TRANSITIVE_SUB_CATEGORY).name("Skip transitive rule")
                        .description("Skip transitive dependency usage check.")
                        .defaultValue(Boolean.toString(TRANSITIVE_SKIP_DEFAULT)).type(BOOLEAN).build(),
                // TRANSITIVE - SEVERITY
                PropertyDefinition.builder(TRANSITIVE_SEVERITY_PROPERTY).onConfigScopes(PROJECT)
                        .subCategory(TRANSITIVE_SUB_CATEGORY).name("Transitivity severity")
                        .description("Severity of transitive library detection.").type(STRING).build(),
                // FILTERING - EXCLUSION_LIST
                PropertyDefinition.builder(FILTERING_EXCLUSIONS_LIST_PROPERTY).onConfigScopes(PROJECT)
                        .subCategory(FILTERING_SUB_CATEGORY).name("Exclusions")
                        .description(
                                """
                                        Exclusions list of dependencies to be reported as an issue. The list is composed of filter separated by comma. \
                                        Each filter is in the form of [groupId]:[artifactId]:[version] where each pattern segment is optional and supports full\
                                         and partial wildcards. Empty pattern segment is treated as an implicit wildcard. Only one wildcard by part.""")
                        .defaultValue("").type(STRING).build(),
                // FILTERING - ONLY_CLASSIC
                PropertyDefinition.builder(FILTERING_ONLY_CLASSIC_VERSION_PROPERTY).onConfigScopes(PROJECT)
                        .subCategory(FILTERING_SUB_CATEGORY).name("Only classic version")
                        .description("When enabled, only classic versions are read (only x.y.z number).")
                        .defaultValue(Boolean.toString(FILTERING_ONLY_CLASSIC_VERSION_DEFAULT)).type(BOOLEAN).build()
        //
        );
    }

    private ExplorerConfiguration() {
        // do nothing
    }
}
