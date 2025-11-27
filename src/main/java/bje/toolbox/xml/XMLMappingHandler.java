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

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLMappingHandler extends DefaultHandler {
    private Mapper factory;
    private Locator loc;
    private int previousColumn;
    private int previousLine;
    public final String startTag;
    private TagPath tag;
    private final XMLMapper saver;

    public XMLMappingHandler(final XMLMapper aSaver, final String startingTag) {
        saver = aSaver;
        startTag = startingTag;
        init();
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        final var line = previousLine;
        final var column = previousColumn;
        updateLocation();
        if (factory.isActive()) {
            factory.setText(new String(ch, start, length), new Range(line, column, previousLine, previousColumn - 2));
        }
    }

    @Override
    public void endDocument() throws SAXException {
        startDocument();
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        if (tag.flatten().equalsIgnoreCase(startTag)) {
            factory.saveElement();
        }
        if (factory.isActive()) {
            factory.unsetTag(qName);
        }
        tag.unset(qName, 0);
        updateLocation();
    }

    public void init() {
        factory = new Mapper(saver);
        tag = new TagPath();
        previousColumn = 0;
        previousLine = 0;
    }

    @Override
    public void setDocumentLocator(final Locator l) {
        loc = l;
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
            throws SAXException {
        final var line = previousLine;
        final var column = previousColumn;
        updateLocation();
        tag.set(qName, 0);
        if (factory.isActive()) {
            factory.setTag(qName);
        } else {
            if (tag.flatten().equalsIgnoreCase(startTag)) {
                factory.newElement();
            }
        }
        if (factory.isActive()) {
            final var limit = attributes.getLength();
            for (var i = 0; i < limit; ++i) {
                factory.setAttribute(attributes.getQName(i), attributes.getValue(i),
                        new Range(line, column - 1, previousLine, previousColumn));
            }
        }
    }

    private void updateLocation() {
        previousLine = loc == null ? 0 : loc.getLineNumber();
        previousColumn = loc == null ? 0 : loc.getColumnNumber();
    }
}
