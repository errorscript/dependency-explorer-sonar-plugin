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

import org.sonar.api.rule.RuleStatus;
import org.sonar.api.rule.Severity;
import org.sonar.api.server.rule.RulesDefinition;

public class ExplorerRuleDefinition implements RulesDefinition {

    private static final String CLUMSY = "clumsy";

    @Override
    public void define(final Context aContext) {
        final var repo = aContext.createRepository(C_REPOSITORY_KEY, C_LANGUAGE_KEY);
        repo.setName("Dependencies errors");

        final var updtRule = repo.createRule(UPDATE_RULE_KEY);
        updtRule.addTags("security", "vulnerability", "obsolete");
        updtRule.setName("Using outdated dependencies");
        updtRule.setSeverity(Severity.MAJOR);
        updtRule.setStatus(RuleStatus.READY);
        updtRule.addOwaspTop10(OwaspTop10Version.Y2021, OwaspTop10.A9);
        updtRule.setHtmlDescription(
                "<p>Dependencies with outdated version augment risks of vulnerabilities and technological debt.</p>");

        final var lcnsRule = repo.createRule(LICENSE_RULE_KEY);
        lcnsRule.addTags("legal");
        lcnsRule.setName("Using incompatible licenced dependencies");
        lcnsRule.setSeverity(Severity.INFO);
        lcnsRule.setStatus(RuleStatus.READY);
        lcnsRule.addOwaspTop10(OwaspTop10Version.Y2021, OwaspTop10.A9);
        lcnsRule.setHtmlDescription("<p>Dependencies with incompatible license pose a legal threat to a project.</p>");

        final var chrnRule = repo.createRule(COHERENCE_RULE_KEY);
        chrnRule.addTags(CLUMSY);
        chrnRule.setName("Using missmatch transient dependencies versions");
        chrnRule.setSeverity(Severity.INFO);
        chrnRule.setStatus(RuleStatus.READY);
        chrnRule.addOwaspTop10(OwaspTop10Version.Y2021, OwaspTop10.A9);
        chrnRule.setHtmlDescription("<p>Transient dependencies with different versions is a risk at runtime.</p>");

        final var unsdRule = repo.createRule(UNUSED_RULE_KEY);
        unsdRule.addTags(CLUMSY);
        unsdRule.setName("Dependency is not used and might be removed");
        unsdRule.setSeverity(Severity.INFO);
        unsdRule.setStatus(RuleStatus.READY);
        unsdRule.addOwaspTop10(OwaspTop10Version.Y2021, OwaspTop10.A9);
        unsdRule.setHtmlDescription(
                "<p>Unused dependency adds to your bom, can cause transient versions incompatibilities, without adding anything to your program. Unused dependency must be removed from the POM.</p>");

        final var trstvRule = repo.createRule(TRANSITIVE_RULE_KEY);
        trstvRule.addTags(CLUMSY);
        trstvRule.setName("Use of transitive dependency");
        trstvRule.setSeverity(Severity.INFO);
        trstvRule.setStatus(RuleStatus.READY);
        trstvRule.addOwaspTop10(OwaspTop10Version.Y2021, OwaspTop10.A9);
        trstvRule.setHtmlDescription(
                "<p>Use of transitive dependency is dangerous as you are not in direct control of the version used. Transitive dependency used must be declared in the POM.</p>");

        repo.done();
    }
}
