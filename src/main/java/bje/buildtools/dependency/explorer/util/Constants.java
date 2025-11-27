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

import org.sonar.api.rule.Severity;

public final class Constants {
    public static final String C_LANGUAGE_KEY = "dependencyexplorer";
    public static final String C_REPOSITORY_KEY = "DependencyExplorer";
    public static final String C_SENSOR_NAME = "Dependency-Explorer";

    // GENERAL
    public static final Boolean GENERAL_PRINT_DEFAULT = Boolean.FALSE;
    public static final String GENERAL_PRINT_PROPERTY = "sonar.dependency.explorer.print";
    public static final Boolean GENERAL_SKIP_BUILD_PLUGIN_DEFAULT = Boolean.TRUE;
    public static final String GENERAL_SKIP_BUILD_PLUGIN_PROPERTY = "sonar.dependency.explorer.skip.plugin";
    public static final Boolean GENERAL_SKIP_DEFAULT = Boolean.FALSE;
    public static final String GENERAL_SUB_CATEGORY = "General";
    public static final String GENRAL_SKIP_PROPERTY = "sonar.dependency.explorer.skip";
    
    // FILTERING
    public static final String FILTERING_EXCLUSIONS_LIST_PROPERTY = "sonar.dependency.explorer.list.exclusions";
    public static final Boolean FILTERING_ONLY_CLASSIC_VERSION_DEFAULT = Boolean.TRUE;
    public static final String FILTERING_ONLY_CLASSIC_VERSION_PROPERTY = "sonar.dependency.explorer.onlyMainVersion";
    public static final String FILTERING_SUB_CATEGORY = "Filtering";
    
    // RULES
    
    // COHERENCES RULE
    public static final String COHERENCE_MAJOR_SEVERITY_DEFAULT = Severity.MAJOR;
    public static final String COHERENCE_MAJOR_SEVERITY_PROPERTY = "sonar.dependency.explorer.coherence.major";
    public static final String COHERENCE_MINOR_SEVERITY_DEFAULT = Severity.MINOR;
    public static final String COHERENCE_MINOR_SEVERITY_PROPERTY = "sonar.dependency.explorer.coherence.minor";
    public static final String COHERENCE_PATCH_SEVERITY_DEFAULT = Severity.MINOR;
    public static final String COHERENCE_PATCH_SEVERITY_PROPERTY = "sonar.dependency.explorer.coherence.patch";
    public static final String COHERENCE_RULE_KEY = "UsingIncoherentVersionnedDependency";
    public static final Boolean COHERENCE_SKIP_DEFAULT = Boolean.FALSE;
    public static final String COHERENCE_SKIP_PROPERTY = "sonar.dependency.explorer.coherence.skip";
    public static final String COHERENCE_SUB_CATEGORY = "Coherence rule";

    // LICENSES RULE
    public static final String LICENSE_SEVERITY_DEFAULT = Severity.BLOCKER;
    public static final String LICENSE_SEVERITY_PROPERTY = "sonar.dependency.explorer.license.severity";
    public static final String LICENSE_DEFINITION_PATH_PROPERTY = "sonar.dependency.explorer.license.matrix";
    public static final String LICENSE_DEFINITION_PROPERTY = "sonar.dependency.explorer.license.match";
    public static final String LICENSE_RULE_KEY = "UsingIncompatibleLicencedDependency";
    public static final Boolean LICENSE_SKIP_DEFAULT = Boolean.FALSE;
    public static final String LICENSE_SKIP_PROPERTY = "sonar.dependency.explorer.licences.skip";
    public static final String LICENSE_SUB_CATEGORY = "License rule";

    // TRANSITIVE RULE
    public static final String TRANSITIVE_SEVERITY_DEFAULT = Severity.MINOR;
    public static final String TRANSITIVE_SEVERITY_PROPERTY = "sonar.dependency.explorer.transitive.severity";
    public static final String TRANSITIVE_RULE_KEY = "UsingTransitiveDependency";
    public static final Boolean TRANSITIVE_SKIP_DEFAULT = Boolean.FALSE;
    public static final String TRANSITIVE_SKIP_PROPERTY = "sonar.dependency.explorer.transitive.skip";
    public static final String TRANSITIVE_SUB_CATEGORY = "Transitive rule";

    // UNUSED RULE
    public static final String UNUSED_SEVERITY_DEFAULT = Severity.MINOR;
    public static final String UNUSED_SEVERITY_PROPERTY = "sonar.dependency.explorer.unused.severity";
    public static final String UNUSED_RULE_KEY = "UnusedDependency";
    public static final Boolean UNUSED_SKIP_DEFAULT = Boolean.FALSE;
    public static final String UNUSED_SKIP_PROPERTY = "sonar.dependency.explorer.unused.skip";
    public static final String UNUSED_SUB_CATEGORY = "Unused rule";

    // UPDATES RULE
    public static final String UPDATE_MAJOR_SEVERITY_DEFAULT = Severity.MAJOR;
    public static final String UPDATE_MAJOR_SEVERITY_PROPERTY = "sonar.dependency.explorer.updates.major";
    public static final String UPDATE_MAVEN_EXECUTABLE_PATH_DEFAULT = "mvn";
    public static final String UPDATE_MAVEN_EXECUTABLE_PATH_PROPERTY = "sonar.dependency.explorer.updates.maven";
    public static final String UPDATE_MINOR_SEVERITY_DEFAULT = Severity.MINOR;
    public static final String UPDATE_MINOR_SEVERITY_PROPERTY = "sonar.dependency.explorer.updates.minor";
    public static final String UPDATE_PATCH_SEVERITY_DEFAULT = Severity.INFO;
    public static final String UPDATE_PATCH_SEVERITY_PROPERTY = "sonar.dependency.explorer.updates.patch";
    public static final String UPDATE_RULE_KEY = "UsingOutdatedDependency";
    public static final Boolean UPDATE_SKIP_DEFAULT = Boolean.FALSE;
    public static final String UPDATE_SKIP_PROPERTY = "sonar.dependency.explorer.updates.skip";
    public static final String UPDATE_SUB_CATEGORY = "Update rule";

    private Constants() {
        // block default constructor
    }
}
