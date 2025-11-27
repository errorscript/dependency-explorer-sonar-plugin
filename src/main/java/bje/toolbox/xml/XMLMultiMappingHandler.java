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

public class XMLMultiMappingHandler extends DefaultHandler {
    private final XMLMappingHandler[] handlers;

    public XMLMultiMappingHandler(final XMLMappingHandler... aHandlerList) {
        handlers = aHandlerList;
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        for (final XMLMappingHandler handler : handlers) {
            handler.characters(ch, start, length);
        }
    }

    @Override
    public void endDocument() throws SAXException {
        for (final XMLMappingHandler handler : handlers) {
            handler.endDocument();
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        for (final XMLMappingHandler handler : handlers) {
            handler.endElement(uri, localName, qName);
        }
    }

    @Override
    public void setDocumentLocator(final Locator l) {
        for (final XMLMappingHandler handler : handlers) {
            handler.setDocumentLocator(l);
        }
    }

    @Override
    public void startDocument() throws SAXException {
        for (final XMLMappingHandler handler : handlers) {
            handler.startDocument();
        }
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
            throws SAXException {
        for (final XMLMappingHandler handler : handlers) {
            handler.startElement(uri, localName, qName, attributes);
        }
    }
}
