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

import static bje.buildtools.dependency.explorer.util.Constants.COHERENCE_RULE_KEY;
import static bje.buildtools.dependency.explorer.util.Constants.C_LANGUAGE_KEY;
import static bje.buildtools.dependency.explorer.util.Constants.C_REPOSITORY_KEY;
import static bje.buildtools.dependency.explorer.util.Constants.LICENSE_RULE_KEY;
import static bje.buildtools.dependency.explorer.util.Constants.TRANSITIVE_RULE_KEY;
import static bje.buildtools.dependency.explorer.util.Constants.UNUSED_RULE_KEY;
import static bje.buildtools.dependency.explorer.util.Constants.UPDATE_RULE_KEY;

import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;

public class ExplorerProfile implements BuiltInQualityProfilesDefinition {

    @Override
    public void define(final Context aContext) {
        final var dependencyCheckWay = aContext.createBuiltInQualityProfile("Dependencyexplorer", C_LANGUAGE_KEY);
        dependencyCheckWay.activateRule(C_REPOSITORY_KEY, UPDATE_RULE_KEY);
        dependencyCheckWay.activateRule(C_REPOSITORY_KEY, COHERENCE_RULE_KEY);
        dependencyCheckWay.activateRule(C_REPOSITORY_KEY, LICENSE_RULE_KEY);
        dependencyCheckWay.activateRule(C_REPOSITORY_KEY, UNUSED_RULE_KEY);
        dependencyCheckWay.activateRule(C_REPOSITORY_KEY, TRANSITIVE_RULE_KEY);
        dependencyCheckWay.done();
    }
}
