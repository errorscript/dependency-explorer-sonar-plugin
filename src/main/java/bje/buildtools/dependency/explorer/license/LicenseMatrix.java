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

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class LicenseMatrix {

    public interface Restriction {
        LicenseFamily restrict(final LicenseFamily a, final LicenseFamily b);
    }

    public record LicenseCompatibility(LicenseDefinition definition, LicenseFamily family) {
    }

    private final Map<String, LicenseFamily> model = new TreeMap<>();

    LicenseMatrix() {
        // block default constructor
    }

    public void add(final LicenseFamily license) {
        model.put(license.getName(), license);
    }

    public void clear() {
        model.clear();
    }

    public Compatibility compatibility(final LicenseDefinition licenses,
            final Map<String, LicenseDefinition> versionMap, final String ga) {
        final Map<String, LicenseCompatibility> result = new TreeMap<>();
        final var projectDef = reduceLicense(licenses.getComposition(), this::mostRestritive);
        for (final Entry<String, LicenseDefinition> entry : versionMap.entrySet()) {
            final var dependencyDef = reduceLicense(entry.getValue().getComposition(), this::leastRestritive);
            if (projectDef != null && dependencyDef != null && !isCompatible(projectDef, dependencyDef)) {
                result.put(entry.getKey(), new LicenseCompatibility(entry.getValue(), dependencyDef));
            }
        }
        return new Compatibility(ga, licenses.getName(), result);
    }

    public Collection<LicenseFamily> getAllFamilies() {
        return model.values();
    }

    public LicenseFamily getFamily(final String d) {
        return model.get(d);
    }

    public boolean isCompatible(final LicenseFamily projectDef, final LicenseFamily def) {
        return isCompatible(projectDef, def, new TreeSet<>());
    }

    private boolean isCompatible(final LicenseFamily projectDef, final LicenseFamily def,
            final Set<LicenseFamily> previous) {
        if (def == null) {
            return false;
        }
        if (projectDef == def) {
            return true;
        }
        for (final String ex : projectDef.getIntegration(IntegrationType.EXCLUDE)) {
            if (Objects.equals(ex, def.getName())) {
                return false;
            }
        }
        for (final String in : projectDef.getIntegration(IntegrationType.INCLUDE)) {
            if (Objects.equals(in, def.getName())) {
                return true;
            }
            final var din = getFamily(in);
            if (din != null && !previous.contains(din)) {
                final var pp = new TreeSet<>(previous);
                pp.add(din);
                if (isCompatible(din, def, pp)) {
                    return true;
                }
            }
        }
        return false;
    }

    public LicenseFamily leastRestritive(final LicenseFamily a, final LicenseFamily b) {
        if (a == null) {
            return b;
        }
        if (b == null || isCompatible(a, b)) {
            return a;
        }
        if (isCompatible(b, a)) {
            return b;
        }
        // copylest is restrictive
        if (a.isCopyleft()) {
            if (!b.isCopyleft()) {
                return b;
            }
        } else {
            if (b.isCopyleft()) {
                return a;
            }
        }
        // fewer parameter means fewer conditions to be met, so least restrictive
        final var i = Integer.compare(a.getParameters().size(), b.getParameters().size());
        if (i > 0) {
            return b;
        }
        // default send the first one
        return a;
    }

    public LicenseFamily match(final LicenseIdentity l) {
        return model.get(l.getFamily());
    }

    public LicenseFamily mostRestritive(final LicenseFamily a, final LicenseFamily b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        if (isCompatible(a, b)) {
            return b;
        }
        if (isCompatible(b, a)) {
            return a;
        }
        // copylest is restrictive
        if (a.isCopyleft()) {
            if (!b.isCopyleft()) {
                return a;
            }
        } else {
            if (b.isCopyleft()) {
                return b;
            }
        }
        // fewer parameter means fewer conditions to be met, so least restrictive
        final var i = Integer.compare(a.getParameters().size(), b.getParameters().size());
        if (i < 0) {
            return b;
        }
        // default send the first one
        return a;
    }

    public LicenseFamily reduceLicense(final Collection<LicenseIdentity> licenses, final Restriction restrictor) {
        LicenseFamily projectDef = null;
        for (final LicenseIdentity l : licenses) {
            if (l != null && l.getFamily() != null && !l.getFamily().isEmpty()) {
                final var def = match(l);
                if (projectDef == null) {
                    projectDef = def;
                } else {
                    projectDef = restrictor.restrict(projectDef, def);
                }
            }
        }
        return projectDef;
    }

    public boolean validateFamily(final String family) {
        return model.containsKey(family);
    }
}
