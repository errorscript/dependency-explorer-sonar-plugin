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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

public class SAXUtils {
    public static void parse(final InputStream is, final XMLMappingHandler... handlers) {
        try {
            final var factory = SAXParserFactory.newInstance();
            final var saxParser = factory.newSAXParser();
            saxParser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // Compliance with security
            saxParser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); // compliance with security
            saxParser.parse(is, new XMLMultiMappingHandler(handlers));
        } catch (final IOException | SAXException | ParserConfigurationException e) {
            throw new SAXReadingException("Error reading xml", e);
        }
    }

    public static void parse(final Path settings, final XMLMappingHandler... handlers) {
        try {
            parse(Files.newInputStream(settings), handlers);
        } catch (final IOException e) {
            throw new SAXReadingException("Error reading file " + settings, e);
        }
    }

    private SAXUtils() {
        // block default constructor
    }
}
