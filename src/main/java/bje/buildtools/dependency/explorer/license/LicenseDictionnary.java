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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class LicenseDictionnary {

    public interface Complement {
        LicenseFamily getLicense(final String name);
    }

    private static String cleanUp(final String key) {
        var low = key.toLowerCase();
        low = low.replace("license", "");
        low = low.replace("version", "");
        low = low.replace("public", "");
        low = low.replace("the", "");
        low = low.replace("-", " ");
        low = low.replace("/", " ");
        low = low.replace(" v1", "1");
        low = low.replace(" v2", "2");
        low = low.replace(" v3", "3");
        low = low.replace(" v4", "4");
        low = low.replace(" v5", "5");
        low = low.replace(" v6", "6");
        low = low.replace(" v7", "7");
        low = low.replace(" v8", "8");
        low = low.replace(" v9", "9");
        low = low.replace(" v0", "0");
        return low.replace("  ", " ");
    }

    private static double compute2(final String base, final String search) {
        final List<String> ba = Arrays.asList(base.split(" "));
        final List<String> sa = Arrays.asList(search.split(" "));
        var k = 0D;
        for (final String s : sa) {
            if (ba.contains(s)) {
                ++k;
            }
        }
        return k * (sa.size() / (double) ba.size());
    }

    private final Map<String, List<LicenseIdentity>> licenseMap = new TreeMap<>();

    private final List<LicenseIdentity> set = new ArrayList<>();

    LicenseDictionnary() {
        // block default constructor
    }

    public void add(final String name, final LicenseIdentity id) {
        var tmp = id;
        final var h = set.indexOf(tmp);
        if (h >= 0) {
            tmp = set.get(h);
        } else {
            set.add(tmp);
        }
        final var list = licenseMap.computeIfAbsent(name.toLowerCase(), n -> new ArrayList<>());
        list.add(tmp);

    }

    public void clear() {
        set.clear();
        licenseMap.clear();
    }

    private List<LicenseIdentity> get(final String name) {
        return licenseMap.get(name);
    }

    public List<LicenseIdentity> getLicense(final String name) {
        final var low = name.toLowerCase();
        var id = get(low);
        if (id == null) {
            id = match(low);
        }
        return id;
    }

    public Collection<LicenseIdentity> listIdentities() {
        return Collections.unmodifiableCollection(set);
    }

    private List<LicenseIdentity> match(final String name) {
        List<LicenseIdentity> id = null;
        final var map = new TreeMap<Double, List<LicenseIdentity>>();
        final var low = cleanUp(name);
        for (final Entry<String, List<LicenseIdentity>> e : licenseMap.entrySet()) {
            for (final LicenseIdentity ee : e.getValue()) {
                final var distance = Math.max(compute2(cleanUp(e.getKey()), low), compute2(cleanUp(ee.getName()), low));
                map.put(distance, Collections.singletonList(ee));
            }
        }

        final var key = map.isEmpty() ? null : map.lastKey();
        if (key != null && key > 0.5) {
            id = map.get(key);
        }
        if (id != null) {
            for (final LicenseIdentity li : id) {
                add(name, li);
            }
        }
        return id;
    }
}
