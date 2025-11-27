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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeMap;

import org.sonar.api.batch.fs.InputComponent;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.rule.Severity;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.batch.sensor.issue.NewMessageFormatting;
import org.sonar.api.batch.sensor.issue.fix.NewQuickFix;
import org.sonar.api.batch.sensor.measure.NewMeasure;
import org.sonar.api.config.Configuration;
import org.sonar.api.issue.impact.SoftwareQuality;
import org.sonar.api.measures.Metric;
import org.sonar.api.rule.RuleKey;

import bje.buildtools.dependency.explorer.data.Artifact;
import bje.buildtools.dependency.explorer.data.Pom;
import bje.buildtools.dependency.explorer.data.DependencyType;
import bje.buildtools.dependency.explorer.data.POMType;
import bje.buildtools.dependency.explorer.util.Utils;

public class TestUtil {
    public static class IssueRepository {
        private final List<LocalIssue> list = new ArrayList<>();

        public void add(final LocalIssue issue) {
            list.add(issue);
        }

        public List<LocalIssue> getIssues() {
            return list;
        }

        public int getSize() {
            return list.size();
        }

        public NewIssue newIssue() {
            return new LocalIssue(this::add);
        }

        public void sort() {
            Collections.sort(list);
        }
    }

    public static class IssueResult {

        public final String desc;
        public final String key;
        public final String severity;

        public IssueResult(final String string, final String string2, final String string3) {
            key = string;
            severity = string2;
            desc = string3;
        }

    }

    public static class LocalIssue implements NewIssue, Comparable<LocalIssue> {

        private final List<LocalIssueLocation> flowLocations = new ArrayList<>();
        private Double gap;
        private LocalIssueLocation primaryLocation;
        private boolean quickFixAvailable;
        private String ruleDescriptionContextKey;
        private RuleKey ruleKey;
        private final Saver<LocalIssue> saver;
        private LocalIssueLocation secondaryLocation;
        private Severity severity;

        public LocalIssue(final Saver<LocalIssue> aSaver) {
            saver = aSaver;
        }

        @Override
        public NewIssue addFlow(final Iterable<NewIssueLocation> aFlowLocations) {
            final var iter = aFlowLocations.iterator();
            while (iter.hasNext()) {
                flowLocations.add((LocalIssueLocation) iter.next());
            }
            return this;
        }

        @Override
        public NewIssue addFlow(final Iterable<NewIssueLocation> flowLocations, final FlowType flowType,
                final String flowDescription) {
            return this;
        }

        @Override
        public NewIssue addInternalTag(final String tag) {
            return this;
        }

        @Override
        public NewIssue addInternalTags(final Collection<String> tags) {
            return this;
        }

        @Override
        public NewIssue addLocation(final NewIssueLocation aSecondaryLocation) {
            secondaryLocation = (LocalIssueLocation) aSecondaryLocation;
            return this;
        }

        @Override
        public NewIssue addQuickFix(final NewQuickFix newQuickFix) {
            return this;
        }

        @Override
        public NewIssue at(final NewIssueLocation aPrimaryLocation) {
            primaryLocation = (LocalIssueLocation) aPrimaryLocation;
            return this;
        }

        @Override
        public int compareTo(final LocalIssue o) {
            var i = Utils.compare(ruleKey, o.ruleKey);
            if (i == 0) {
                i = Utils.compare(severity, o.severity);
            }
            if (i == 0) {
                i = Utils.compare(ruleDescriptionContextKey, o.ruleDescriptionContextKey);
            }
            if (i == 0) {
                i = Utils.compare(primaryLocation, o.primaryLocation);
            }
            return i;
        }

        @Override
        public boolean equals(final Object o) {
            if (o instanceof final LocalIssue l) {
                return Objects.equals(ruleKey, l.ruleKey) && Objects.equals(severity, l.severity)
                        && Objects.equals(ruleDescriptionContextKey, l.ruleDescriptionContextKey)
                        && Objects.equals(primaryLocation, l.primaryLocation);
            }
            return false;
        }

        @Override
        public NewIssue forRule(final RuleKey aRuleKey) {
            ruleKey = aRuleKey;
            return this;
        }

        @Override
        public NewIssue gap(final Double aGap) {
            gap = aGap;
            return this;
        }

        public List<LocalIssueLocation> getFlowLocations() {
            return flowLocations;
        }

        public Double getGap() {
            return gap;
        }

        public NewIssueLocation getPrimaryLocation() {
            return primaryLocation;
        }

        public String getRuleDescriptionContextKey() {
            return ruleDescriptionContextKey;
        }

        public RuleKey getRuleKey() {
            return ruleKey;
        }

        public NewIssueLocation getSecondaryLocation() {
            return secondaryLocation;
        }

        public Severity getSeverity() {
            return severity;
        }

        public boolean isQuickFixAvailable() {
            return quickFixAvailable;
        }

        @Override
        public LocalIssueLocation newLocation() {
            return new LocalIssueLocation();
        }

        @Override
        public NewQuickFix newQuickFix() {
            return null;
        }

        @Override
        public NewIssue overrideImpact(final SoftwareQuality softwareQuality,
                final org.sonar.api.issue.impact.Severity severity) {
            return this;
        }

        @Override
        public NewIssue overrideSeverity(final Severity aSeverity) {
            severity = aSeverity;
            return this;
        }

        @Override
        public void save() {
            saver.save(this);
        }

        @Override
        public NewIssue setCodeVariants(final Iterable<String> codeVariants) {
            return this;
        }

        @Override
        public NewIssue setInternalTags(final Collection<String> tags) {
            return this;
        }

        @Override
        public NewIssue setQuickFixAvailable(final boolean aQuickFixAvailable) {
            quickFixAvailable = aQuickFixAvailable;
            return this;
        }

        @Override
        public NewIssue setRuleDescriptionContextKey(final String aRuleDescriptionContextKey) {
            ruleDescriptionContextKey = aRuleDescriptionContextKey;
            return this;
        }
    }

    public static class LocalIssueLocation implements NewIssueLocation, Comparable<LocalIssueLocation> {

        private InputComponent component;
        private TextRange location;
        private String message;

        @Override
        public NewIssueLocation at(final TextRange aLocation) {
            location = aLocation;
            return this;
        }

        @Override
        public int compareTo(final LocalIssueLocation o) {
            return Utils.compare(message, o.message);
        }

        public InputComponent getComponent() {
            return component;
        }

        public TextRange getLocation() {
            return location;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public NewIssueLocation message(final String aMessage) {
            message = aMessage;
            return this;
        }

        @Override
        public NewIssueLocation message(final String message, final List<NewMessageFormatting> newMessageFormatting) {
            return this;
        }

        @Override
        public NewMessageFormatting newMessageFormatting() {
            return null;
        }

        @Override
        public NewIssueLocation on(final InputComponent aComponent) {
            component = aComponent;
            return this;
        }
    }

    public static class LocalMeasure implements NewMeasure<String> {
        private org.sonar.api.batch.measure.Metric<String> metric;
        private final Saver<LocalMeasure> saver;
        private String value;
        private InputComponent component;

        public LocalMeasure(final Saver<LocalMeasure> aSaver) {
            saver = aSaver;
        }

        @Override
        public NewMeasure<String> forMetric(final org.sonar.api.batch.measure.Metric<String> aMetric) {
            metric = aMetric;
            return this;
        }

        public String getComponentName() {
            return component == null ? null : component.toString();
        }

        public String getValue() {
            return value;
        }

        @Override
        public NewMeasure<String> on(final InputComponent aComponent) {
            component = aComponent;
            return this;
        }

        @Override
        public void save() {
            saver.save(this);
        }

        @Override
        public NewMeasure<String> withValue(final String aValue) {
            value = aValue;
            return this;
        }
    }

    public static class MeasureRepository {
        private final Map<String, List<LocalMeasure>> map = new TreeMap<>();

        public List<LocalMeasure> getMeasure(final Metric<String> report) {
            final var list = map.get(report.key());
            Collections.sort(list, (a, b) -> {
                var i = Utils.compare(a.getComponentName(), b.getComponentName());
                if (i == 0) {
                    i = Utils.compare(a.getValue(), b.getValue());
                }
                return i;
            });
            return list;
        }

        public int getSize() {
            var sum = 0;
            for (final List<LocalMeasure> l : map.values()) {
                sum += l.size();
            }
            return sum;
        }

        public NewMeasure<String> newMeasure() {
            return new LocalMeasure(t -> {
                final var l = map.computeIfAbsent(t.metric.key(), k -> new ArrayList<>());
                l.add(t);
            });
        }
    }

    @FunctionalInterface
    public interface Saver<T> {
        void save(T t);
    }

    public static void copy(final Path f, final Path t) throws IOException {
        if (Files.isDirectory(f)) {
            final var tt = t.resolve(f.getFileName());
            Files.createDirectories(tt);
            try (var stream = Files.list(f)) {
                for (final Path ff : stream.toArray(Path[]::new)) {
                    copy(ff, tt);
                }
            }
        } else {
            final var tf = t.resolve(f.getFileName());
            flow(f, tf);
        }
    }

    public static Pom createPomForUsageCheck(final InputFile input) {
        final var pom = new Pom(input, POMType.MAIN);
        final var defMain = pom.addDependency(new Artifact("group", "main", "1.0.0"), Arrays.asList(),
                DependencyType.DEPENDENCY);
        final var defTest = pom.addDependency(new Artifact("testGroup", "testA", "1.4.3"), Arrays.asList(),
                DependencyType.DEPENDENCY);
        final var defTransitive = pom.addDependency(new Artifact("testGroup", "transB", "4.7.0"), Arrays.asList(),
                DependencyType.DEPENDENCY);
        final var defTransitive2 = pom.addDependency(new Artifact("testGroup", "transC", "2.1.1"), Arrays.asList(),
                DependencyType.DEPENDENCY);
        final var defTest2 = pom.addDependency(new Artifact("testGroup", "testK", "1.0.0"), Arrays.asList(),
                DependencyType.DEPENDENCY);
        defMain.addDependency(defTest);
        defTest.addDependency(defTransitive);
        defTest.addDependency(defTransitive2);
        defMain.addDependency(defTest2);
        pom.updateRoot(defMain);

        pom.addEffectiveDependency(new Artifact("testGroup", "testA", "1.4.3"));
        pom.addEffectiveDependency(new Artifact("testGroup", "testK", "1.0.0"));

        pom.addUndeclaredDependency(new Artifact("testGroup", "transB", "4.7.0"));
        final var a = new Artifact("testGroup", "transC", "2.1.1");
        a.setScope("test");
        pom.addUndeclaredDependency(a);

        pom.addUnusedDependency(new Artifact("testGroup", "testK", "1.0.0"));

        return pom;
    }

    public static void deleteRecursively(final Path dir) throws IOException {
        if (Files.notExists(dir) || !Files.isDirectory(dir)) {
            return;
        }
        try (var ds = Files.newDirectoryStream(dir)) {
            for (final Path file : ds) {
                if (Files.isDirectory(file)) {
                    deleteRecursively(file);
                } else {
                    Files.delete(file);
                }
            }
        }
        Files.delete(dir);
    }

    public static void flow(final Path a, final Path b) throws IOException {
        try (final Reader reader = Files.newBufferedReader(a); final Writer writer = Files.newBufferedWriter(b)) {
            final var buf = new char[4096];
            var numRead = reader.read(buf);
            while (numRead != -1) {
                writer.write(buf, 0, numRead);
                numRead = reader.read(buf);
            }
        }
    }

    public static Configuration getConfiguration(final Properties prop) {
        return new Configuration() {

            @Override
            public Optional<String> get(final String key) {
                final var c = prop.getProperty(key);
                return c == null ? Optional.empty() : Optional.of(c);
            }

            @Override
            public Optional<Boolean> getBoolean(final String key) {
                final var c = prop.getProperty(key);
                return c == null ? Optional.empty() : Optional.of(Boolean.parseBoolean(c));
            }

            @Override
            public String[] getStringArray(final String key) {
                final var c = prop.getProperty(key);
                if (c == null) {
                    return new String[0];
                }
                return c.split(",");
            }

            @Override
            public boolean hasKey(final String key) {
                return prop.containsKey(key);
            }
        };
    }

    public static InputFile loadFile() throws IOException {
        final var mockedList = mock(InputFile.class);
        final var classLoader = TestUtil.class.getClassLoader();
        when(mockedList.inputStream()).thenAnswer(t -> classLoader.getResourceAsStream("test-pom.xml"));
        when(mockedList.uri()).thenAnswer(t -> classLoader.getResource("test-pom.xml").toURI());

        doAnswer(invocation -> {
            final var args = invocation.getArguments();
            final int line = (Integer) args[0];
            final int offset = (Integer) args[1];
            return new TextPointer() {

                @Override
                public int compareTo(final TextPointer o) {
                    final var i = Integer.compare(line, o.line());
                    if (i == 0) {
                        return Integer.compare(offset, o.lineOffset());
                    }
                    return 0;
                }

                @Override
                public int line() {
                    return line;
                }

                @Override
                public int lineOffset() {
                    return offset;
                }
            };
        }).when(mockedList).newPointer(anyInt(), anyInt());
        doAnswer(invocation -> {
            final var args = invocation.getArguments();
            final var start = (TextPointer) args[0];
            final var end = (TextPointer) args[1];
            return new TextRange() {

                @Override
                public TextPointer end() {
                    return end;
                }

                @Override
                public boolean overlap(final TextRange another) {
                    return false;
                }

                @Override
                public TextPointer start() {
                    return start;
                }
            };
        }).when(mockedList).newRange(any(TextPointer.class), any(TextPointer.class));
        return mockedList;
    }

    public static <S> void testList(final Iterable<S> mainImports, final Iterable<S> mainImportsResults) {
        final var it = mainImports.iterator();
        final var ir = mainImportsResults.iterator();
        while (it.hasNext() && ir.hasNext()) {
            assertEquals(ir.next(), it.next());
        }
        assertFalse(it.hasNext());
        assertFalse(ir.hasNext());
    }

    private TestUtil() {
        // block default constructor
    }
}
