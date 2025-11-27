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
package bje.toolbox.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;

class XmlReadingRangeTest {

    @Test
    void testPom() {
        final var prop = new TreeMap<String, String>();
        final var dep = new TreeMap<String, String>();
        prop.put("junit-version", "4.11");
        prop.put("language", "java");
        prop.put("project.build.sourceEncoding", "UTF-8");
        prop.put("maven.compiler.source", "1.8");
        prop.put("maven.compiler.target", "1.8");
        dep.put("junit", "junit");
        dep.put("javax.mail", "mail");
        dep.put("org.codehaus.groovy", "groovy");
        dep.put("org.slf4j", "slf4j-api");
        final var properties = new TreeMap<String, String>();
        final var rangeproperties = new TreeMap<String, Range>();
        final var dependencies = new TreeMap<String, String>();
        final var dependencyHandler = new XMLMappingHandler(map -> {
            final var groupId = map.get("/groupId");
            final var artifactId = map.get("/artifactId");
            dependencies.put(groupId, artifactId);
        }, "/project/dependencies/dependency");
        final var propertyHandler = new XMLMappingHandler(dataMap -> {
            for (final Entry<String, Data> entry : dataMap.entrySet()) {
                properties.put(entry.getKey().substring(1), entry.getValue().getText());
                rangeproperties.put(entry.getKey().substring(1), entry.getValue().getRange());
            }
        }, "/project/properties");
        final var classLoader = getClass().getClassLoader();
        final var file = Path.of(classLoader.getResource("toolbox/xml/range.xml").getFile());
        SAXUtils.parse(file, dependencyHandler, propertyHandler);
        assertEquals(new Range(12, 26, 12, 29), rangeproperties.get("maven.compiler.source"));
        assertEquals(new Range(13, 26, 13, 29), rangeproperties.get("maven.compiler.target"));
        assertEquals(dep, dependencies);
        assertEquals(prop, properties);
    }

}
