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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bje.buildtools.dependency.explorer.license.LicenseMatrix.Restriction;

public class LicenseModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(LicenseModel.class);
    private final LicenseDictionnary dictionnary = new LicenseDictionnary();
    private final LicenseMatrix matrix = new LicenseMatrix();

    public void addFamily(final LicenseFamily license) {
        matrix.add(license);
    }

    public void addIdentity(final String match, final LicenseIdentity license) {
        dictionnary.add(match, license);
    }

    public Compatibility compatibility(final LicenseDefinition rootLicenses,
            final Map<String, LicenseDefinition> versionMap, final String ga) {
        return matrix.compatibility(rootLicenses, versionMap, ga);
    }

    public Collection<LicenseFamily> getAllFamilies() {
        return matrix.getAllFamilies();
    }

    public LicenseFamily getFamily(final String in) {
        return matrix.getFamily(in);
    }

    public List<LicenseIdentity> getLicense(final String licenses) {
        return dictionnary.getLicense(licenses);
    }

    public LicenseFamily leastRestritive(final LicenseFamily a, final LicenseFamily b) {
        return matrix.leastRestritive(a, b);
    }

    public LicenseFamily mostRestritive(final LicenseFamily a, final LicenseFamily b) {
        return matrix.mostRestritive(a, b);
    }

    public LicenseFamily reduceLicense(final Set<LicenseIdentity> composition, final Restriction restrictor) {
        return matrix.reduceLicense(composition, restrictor);
    }

    public void validate() {
        for (final LicenseFamily fam : matrix.getAllFamilies()) {
            for (final var type : IntegrationType.values()) {
                for (final String s : fam.getIntegration(type)) {
                    if (matrix.getFamily(s) == null) {
                        LOGGER.warn("Unknown family '{}' for {} integration in '{}' family", s, type.name(),
                                fam.getName());
                    }
                }
            }
        }
        for (final LicenseIdentity id : dictionnary.listIdentities()) {
            if (!matrix.validateFamily(id.getFamily())) {
                LOGGER.warn("Unknown family '{}' for license '{}'", id.getFamily(), id.getName());
            }
        }
    }
}
