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
package bje.buildtools.dependency.explorer.license;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LicenseFamily implements Comparable<LicenseFamily> {
    private boolean copyleft = false;
    private final List<String> integrationExclude = new ArrayList<>();
    private final List<String> integrationInclude = new ArrayList<>();
    private final String name;
    private final Map<String, String> parameters = new HashMap<>();

    public LicenseFamily(final String string) {
        name = string;
    }

    public void addIntegration(final String string, final IntegrationType include) {
        switch (include) {
        case EXCLUDE:
            integrationExclude.add(string);
            break;
        case INCLUDE:
        default:
            integrationInclude.add(string);
            break;
        }
    }

    public void addParameter(final String key, final String value) {
        parameters.put(key, value);
    }

    @Override
    public int compareTo(final LicenseFamily o) {
        return name.compareTo(o.name);
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof final LicenseFamily f) {
            return name.equals(f.name);
        }
        return false;
    }

    public List<String> getIntegration(final IntegrationType include) {
        return switch (include) {
        case EXCLUDE -> integrationExclude;
        case INCLUDE -> integrationInclude;
        };
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public boolean isCopyleft() {
        return copyleft;
    }

    public void setCopyleft(final boolean value) {
        copyleft = value;
    }

    @Override
    public String toString() {
        return name;
    }
}
