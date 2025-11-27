package bje.buildtools.dependency.explorer.maven.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import bje.buildtools.dependency.explorer.TestUtil;
import bje.buildtools.dependency.explorer.data.Artifact;
import bje.buildtools.dependency.explorer.data.Dependency;
import bje.buildtools.dependency.explorer.data.POMType;
import bje.buildtools.dependency.explorer.data.Pom;
import bje.buildtools.dependency.explorer.filter.FilterList;
import bje.buildtools.dependency.explorer.util.Constants;
import bje.buildtools.dependency.explorer.util.ExplorationConfiguration;

class DependencyTreeTest {

    @Test
    void test() throws IOException, URISyntaxException {
        final var classLoader = DependencyTree.class.getClassLoader();
        final var input = TestUtil.loadFile();
        final var pom = new Pom(input, POMType.MAIN);
        final var config = new ExplorationConfiguration(Pattern.compile(ExplorationConfiguration.REGEX_ALLOW_ALL),
                new FilterList(null), false, Constants.UPDATE_MAVEN_EXECUTABLE_PATH_DEFAULT, true);
        pom.updateRoot(new Dependency(pom, new Artifact("my", "test", "0.1.0")));
        final var dfile = Path.of(classLoader.getResource("tree.txt").toURI());
        assertTrue(DependencyTree.parse(dfile, pom, config));
        assertEquals(15, pom.getRoot().getChildren().size());
        assertEquals(54, pom.getMapDependencies().size());
    }

}
