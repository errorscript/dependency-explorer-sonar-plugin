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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

class XmlReadingTest {

    private static class ListElement {

        private String checksum;
        private String downloadLink;
        private String fileName;
        private String fileType;
        private String id;
        private String publicationDate;
        private String publishedInstrumentFileId;
        private String root;
        private String timestamp;
        private String version;

        public String getChecksum() {
            return checksum;
        }

        public String getDownloadLink() {
            return downloadLink;
        }

        public String getFileName() {
            return fileName;
        }

        public String getFileType() {
            return fileType;
        }

        public String getId() {
            return id;
        }

        public String getPublicationDate() {
            return publicationDate;
        }

        public String getPublishedInstrumentFileId() {
            return publishedInstrumentFileId;
        }

        public String getRoot() {
            return root;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getVersion() {
            return version;
        }

        public void setChecksum(final String string) {
            checksum = string;
        }

        public void setDownloadLink(final String string) {
            downloadLink = string;
        }

        public void setFileName(final String string) {
            fileName = string;
        }

        public void setFileType(final String string) {
            fileType = string;
        }

        public void setId(final String string) {
            id = string;
        }

        public void setPublicationDate(final String string) {
            publicationDate = string;
        }

        public void setPublishedInstrumentFileId(final String string) {
            publishedInstrumentFileId = string;
        }

        public void setRoot(final String string) {
            root = string;
        }

        public void setTimestamp(final String string) {
            timestamp = string;
        }

        public void setVersion(final String string) {
            version = string;
        }
    }

    private static class ListingSaver implements XMLMapper {
        private final List<ListElement> list = new ArrayList<>();

        public List<ListElement> getElements() {
            return list;
        }

        private String key(final String string, final int nb) {
            return string + (nb == 0 ? "" : "(" + nb + ")");
        }

        @Override
        public void map(final XMLMap map) {
            final var elt = new ListElement();
            map.forEach("/str", m -> {
                final var name = m.get(":name");
                final var value = m.get("");
                if ("checksum".equals(name)) {
                    elt.setChecksum(value);
                }
                if ("download_link".equals(name)) {
                    elt.setDownloadLink(value);
                }
                if ("id".equals(name)) {
                    elt.setId(value);
                }
                if ("published_instrument_file_id".equals(name)) {
                    elt.setPublishedInstrumentFileId(value);
                }
                if ("_root_".equals(name)) {
                    elt.setRoot(value);
                }
                if ("file_name".equals(name)) {
                    elt.setFileName(value);
                }
                if ("file_type".equals(name)) {
                    elt.setFileType(value);
                }
            });
            var name = map.get("/date:name");
            var nb = 0;
            while (name != null) {
                if ("publication_date".equals(name)) {
                    elt.setPublicationDate(map.get(key("/date", nb)));
                }
                if ("timestamp".equals(name)) {
                    elt.setTimestamp(map.get(key("/date", nb)));
                }
                ++nb;
                name = map.get(key("/date", nb) + ":name");
            }
            name = map.get("/long:name");
            if ("_version_".equals(name)) {
                elt.setVersion(map.get("/long"));
            }
            list.add(elt);
        }

    }

    private static class RefData {

        private String authiority;
        private String from;
        private String id;
        private String issr;
        private String venue;
        private String venueId;

        public String getAuthiority() {
            return authiority;
        }

        public String getFrom() {
            return from;
        }

        public String getId() {
            return id;
        }

        public String getIssr() {
            return issr;
        }

        public String getVenue() {
            return venue;
        }

        public String getVenueId() {
            return venueId;
        }

        public void setAythiority(final String string) {
            authiority = string;
        }

        public void setFrom(final String string) {
            from = string;
        }

        public void setId(final String string) {
            id = string;
        }

        public void setIssr(final String string) {
            issr = string;
        }

        public void setVenue(final String string) {
            venue = string;
        }

        public void setVenueId(final String string) {
            venueId = string;
        }

    }

    private static class RefSaver implements XMLMapper {
        private final List<RefData> list = new ArrayList<>();

        public List<RefData> getElements() {
            return list;
        }

        @Override
        public void map(final XMLMap map) {
            final var elt = new RefData();
            elt.setId(map.get("/FinInstrmGnlAttrbts/Id"));
            elt.setIssr(map.get("/Issr"));
            elt.setVenueId(map.get("/TradgVnRltdAttrbts/Id"));
            elt.setAythiority(map.get("/TechAttrbts/RlvntCmptntAuthrty"));
            elt.setFrom(map.get("/TechAttrbts/PblctnPrd/FrDt"));
            elt.setVenue(map.get("/TechAttrbts/RlvntTradgVn"));
            list.add(elt);
        }
    }

    private static final Path FILE = Path.of(System.getProperty("java.io.tmpdir"), "test.pom");

    @BeforeAll
    @AfterAll
    static void delete() throws IOException {
        if (Files.exists(FILE)) {
            Files.delete(FILE);
        }
    }

    private RefData ref(final String id, final String issr, final String venueId, final String authiority,
            final String from, final String venue) {
        final var data = new RefData();
        data.id = id;
        data.issr = issr;
        data.venueId = venueId;
        data.authiority = authiority;
        data.from = from;
        data.venue = venue;
        return data;
    }

    private void test(final RefData ref, final RefData elt) {
        assertEquals(ref.getId(), elt.getId());
        assertEquals(ref.getIssr(), elt.getIssr());
        assertEquals(ref.getVenueId(), elt.getVenueId());
        assertEquals(ref.getAuthiority(), elt.getAuthiority());
        assertEquals(ref.getFrom(), elt.getFrom());
        assertEquals(ref.getVenue(), elt.getVenue());
    }

    @Test
    void testCases() throws IOException {
        final var prop = new TreeMap<String, String>();
        prop.put("truc", "1528");
        final var vers = "1.0.0";
        final var version = new AtomicReference<String>(null);
        final var properties = new TreeMap<String, String>();

        final var handler1 = new XMLMappingHandler(map -> {
            version.set(map.get(":version"));
            assertNull(map.get("/truc"));
            assertNull(map.get("/truc"));
            assertNull(map.getRange("/truc"));
        }, "/project");
        final var handler2 = new XMLMappingHandler(dataMap -> {
            final var scope = dataMap.get(":scope");
            if ("all".equals(scope)) {
                properties.put(dataMap.get("/key"), dataMap.get("/value"));
            }
        }, "/project/properties/property");

        final var xml1 = """
                <project version="1.0.0">
                  <name>Test</name>
                  <properties>
                  <property scope="all">
                  <key>truc</key>
                  <value>1528</value>
                  <property scope="local">
                  <key>pory</key>
                  <value>8891</value>
                  </property>
                  </properties>
                </project>
                """;

        final InputStream is = new ByteArrayInputStream(xml1.getBytes(StandardCharsets.UTF_8));
        assertThrows(SAXReadingException.class, () -> SAXUtils.parse(is, handler1, handler2));

        handler1.init();
        handler2.init();
        final var xml2 = """
                <project  version = "1.0.0" >
                   <name>Test</name>
                   <properties>
                   <property scope="all">
                   <key>truc</key>
                   <value>1528</value>
                   </property>
                   <property scope="local">
                   <key>pory</key>
                   <value>8891</value>
                   </property>
                   </properties>
                </project>
                """;

        SAXUtils.parse(new ByteArrayInputStream(xml2.getBytes(StandardCharsets.UTF_8)), handler1, handler2);
        assertEquals(vers, version.get());
        assertEquals(prop, properties);

        delete();
        assertThrows(SAXReadingException.class, () -> SAXUtils.parse(FILE, handler1, handler2));
    }

    @Test
    void testDoc1() {
        final var saver = new ListingSaver();
        final var classLoader = getClass().getClassLoader();
        final var xmlFile = Path.of(classLoader.getResource("toolbox/xml/doc1.xml").getFile());
        SAXUtils.parse(xmlFile, new XMLMappingHandler(saver, "/response/result/doc"));
        final var elts = saver.getElements();
        assertEquals(2, elts.size());
        var elt = elts.get(0);
        assertEquals("19f7405c0d8a156be5c70ee12010125b", elt.getChecksum());
        assertEquals("http://firds.esma.europa.eu/firds/FULINS_C_20200523_01of01.zip", elt.getDownloadLink());
        assertEquals("2020-05-23T00:00:00Z", elt.getPublicationDate());
        assertEquals("35928", elt.getId());
        assertEquals("35928", elt.getPublishedInstrumentFileId());
        assertEquals("35928", elt.getRoot());
        assertEquals("FULINS_C_20200523_01of01.zip", elt.getFileName());
        assertEquals("FULINS", elt.getFileType());
        assertEquals("1671620012447629323", elt.getVersion());
        assertEquals("2020-07-08T04:04:32.662Z", elt.getTimestamp());

        elt = elts.get(1);
        assertEquals("1519a0eb55bb0b9fa56dabcaa4c35039", elt.getChecksum());
        assertEquals("http://firds.esma.europa.eu/firds/FULINS_D_20200523_01of03.zip", elt.getDownloadLink());
        assertEquals("2020-05-23T00:00:00Z", elt.getPublicationDate());
        assertEquals("35929", elt.getId());
        assertEquals("35929", elt.getPublishedInstrumentFileId());
        assertEquals("35929", elt.getRoot());
        assertEquals("FULINS_D_20200523_01of03.zip", elt.getFileName());
        assertEquals("FULINS", elt.getFileType());
        assertEquals("1671620012447629324", elt.getVersion());
        assertEquals("2020-07-08T04:04:32.662Z", elt.getTimestamp());
    }

    @Test
    void testDoc2() throws ParserConfigurationException, SAXException, IOException {
        final var factory = SAXParserFactory.newInstance();
        final var saxParser = factory.newSAXParser();
        final var saver = new RefSaver();
        final var classLoader = getClass().getClassLoader();
        final var xmlFile = Path.of(classLoader.getResource("toolbox/xml/doc2.xml").getFile());
        saxParser.parse(Files.newInputStream(xmlFile),
                new XMLMappingHandler(saver, "/BizData/Pyld/Document/FinInstrmRptgRefDataRpt/RefData"));
        final var elts = saver.getElements();
        assertEquals(10, elts.size());

        final List<RefData> res = Arrays.asList(
                ref("JE00B23SZL05", "54930015FR83PHHI2P68", "XMSM", "IE", "2018-10-15", "XMSM"),
                ref("CA17039A1066", "549300SV1MXLTZ8EK490", "BERB", "DE", "2019-11-09", "BERB"),
                ref("CA17039A1066", "549300SV1MXLTZ8EK490", "LIQU", "DE", "2019-11-09", "BERB"),
                ref("CA7669101031", "549300S8HVUSEU3JBY60", "BAAD", "DE", "2020-04-30", "MUNB"),
                ref("CA7669101031", "549300S8HVUSEU3JBY60", "BERB", "DE", "2020-02-18", "MUNB"),
                ref("CA7669101031", "549300S8HVUSEU3JBY60", "FRAB", "DE", "2020-02-18", "MUNB"),
                ref("CA7669101031", "549300S8HVUSEU3JBY60", "LIQU", "DE", "2020-02-18", "MUNB"),
                ref("CA7669101031", "549300S8HVUSEU3JBY60", "MUNB", "DE", "2020-02-18", "MUNB"),
                ref("CA7669101031", "549300S8HVUSEU3JBY60", "MUND", "DE", "2020-04-30", "MUNB"),
                ref("CA83179X1087", "549300HKEP2IYWZ14H97", "BERB", "DE", "2019-11-09", "BERB"));
        for (var i = 0; i < 10; ++i) {
            test(res.get(i), elts.get(i));
        }
    }

    @Test
    void testPom() throws IOException {
        final var pom = """
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                  <modelVersion>4.0.0</modelVersion>
                  <artifactId>test</artifactId>
                  <packaging>jar</packaging>
                  <name>Test</name>
                  <properties>
                    <junit-version>4.11</junit-version>
                    <language>java</language>
                	<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                    <maven.compiler.source>1.8</maven.compiler.source>
                    <maven.compiler.target>1.8</maven.compiler.target>
                  </properties>
                  <dependencyManagement>
                    <dependency>
                      <groupId>javax.servlet</groupId>
                      <artifactId>servlet-api</artifactId>
                      <version>2.5</version>
                    </dependency>
                  </dependencyManagement>
                  <dependencies>
                    <dependency>
                      <groupId>junit</groupId>
                      <artifactId>junit</artifactId>
                      <version>${junit-version}</version>
                      <scope>test</scope>
                    </dependency>
                    <dependency>
                      <groupId>javax.mail</groupId>
                      <artifactId>mail</artifactId>
                      <version>1.4.7</version>
                    </dependency>
                    <dependency>
                      <groupId>org.codehaus.groovy</groupId>
                      <artifactId>groovy</artifactId>
                      <version>2.4.12</version>
                      <exclusions>
                        <exclusion>
                          <artifactId>junit</artifactId>
                          <groupId>junit</groupId>
                        </exclusion>
                        <exclusion>
                          <artifactId>xpp3_min</artifactId>
                          <groupId>xpp3</groupId>
                        </exclusion>
                      </exclusions>
                    </dependency>
                    <dependency>
                      <groupId>org.slf4j</groupId>
                      <artifactId>slf4j-api</artifactId>
                      <version>1.7.26</version>
                    </dependency>
                  </dependencies>
                </project>
                """;
        try (var fw = Files.newBufferedWriter(FILE)) {
            fw.write(pom);
        }

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
                final var tag = entry.getKey();
                properties.put(tag.substring(1), entry.getValue().getText());
                rangeproperties.put(tag.substring(1), entry.getValue().getRange());
            }
        }, "/project/properties");
        SAXUtils.parse(FILE, dependencyHandler, propertyHandler);
        assertEquals(new Range(10, 28, 10, 31), rangeproperties.get("maven.compiler.source"));
        assertEquals(new Range(11, 28, 11, 31), rangeproperties.get("maven.compiler.target"));
        assertEquals(dep, dependencies);
        assertEquals(prop, properties);
    }

}
