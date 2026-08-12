/*


  Jacksum 4.0.0 - a checksum/hash tool written in Java
  Copyright (c) 2001-2026 Dipl.-Inf. (FH) Johann N. Löfflmann,
  All Rights Reserved, <https://jacksum.net>.

  This program is free software: you can redistribute it and/or modify it under
  the terms of the GNU General Public License as published by the Free Software
  Foundation, either version 3 of the License, or (at your option) any later
  version.

  This program is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  details.

  You should have received a copy of the GNU General Public License along with
  this program. If not, see <https://www.gnu.org/licenses/>.


 */
package net.jacksum.selectors;

import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.algorithms.md.MD6;
import net.jacksum.algorithms.wrappers.MD;

/**
 *
 * @author johann
 */
public class MD6_Selector extends Selector {

    private static final String ID_PREFIX = "md6-";
    private static final String ALIAS = "md6";
    // the number of data bytes that the compression function of MD6 takes
    private static final int BLOCK_BYTES = 512;
    // the digest length that a plain "md6" stands for, as used by md6sum
    private static final int DEFAULT_LENGTH = 256;

    private static Map<String, String> algos;
    private static Map<String, String> aliases;

    private static void _fillMap(Map<String, String> map, String keyPrefix, String valuePrefix) {
        for (int i = 8; i <= MD6.MAX_D; i += 8) {
            map.put(keyPrefix + i, valuePrefix + i);
        }
    }

    @Override
    public Map<String, String> getAvailableAlgorithms() {
        if (algos == null) {
            algos = new LinkedHashMap<>(86); // ceil(64/0.75)
            _fillMap(algos, ID_PREFIX, "MD6-");
        }
        return algos;
    }

    @Override
    public Map<String, String> getAvailableAliases() {
        if (aliases == null) {
            aliases = new LinkedHashMap<>(2); // ceil(1/0.75)
            aliases.put(ALIAS, ID_PREFIX + DEFAULT_LENGTH);
        }
        return aliases;
    }

    @Override
    public AbstractChecksum getPrimaryImplementation() throws NoSuchAlgorithmException {
        try {
            return new MD(new MD6(lengthOf(name)), BLOCK_BYTES);
        } catch (IllegalArgumentException iae) {
            throw new NoSuchAlgorithmException(iae.getMessage());
        }
    }

    /**
     * Returns the digest length in bits that an MD6 algorithm ID asks for.
     *
     * @param id an MD6 algorithm ID; both the primary form (md6-256) and the
     * alias md6 are understood
     * @return the digest length in bits
     * @throws NoSuchAlgorithmException if the ID carries no valid length
     */
    private static int lengthOf(String id) throws NoSuchAlgorithmException {
        if (id.equals(ALIAS)) {
            return DEFAULT_LENGTH;
        }
        try {
            return Integer.parseInt(id.substring(ID_PREFIX.length()));
        } catch (NumberFormatException | IndexOutOfBoundsException nfe) {
            throw new NoSuchAlgorithmException(
                    id + " is an invalid id for MD6. Must be in format md6-<n>");
        }
    }

}
