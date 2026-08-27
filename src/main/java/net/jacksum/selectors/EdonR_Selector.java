/*


  Jacksum 4.0.1 - a checksum/hash tool written in Java
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
import net.jacksum.algorithms.md.EdonR;
import net.jacksum.algorithms.wrappers.MD;

/**
 *
 * @author johann
 */
public class EdonR_Selector extends Selector {

    private static final String ID_PREFIX = "edonr";
    private static final int[] LENGTHS = {224, 256, 384, 512};
    // the number of data bytes that the compression function of Edon-R takes;
    // Edon-R 224 and 256 work on 32 bit words, Edon-R 384 and 512 on 64 bit words
    private static final int BLOCK_BYTES_32 = 64;
    private static final int BLOCK_BYTES_64 = 128;

    private static Map<String, String> algos;
    private static Map<String, String> aliases;

    @Override
    public Map<String, String> getAvailableAlgorithms() {
        if (algos == null) {
            algos = new LinkedHashMap<>(6); // ceil(4/0.75)
            for (int length : LENGTHS) {
                algos.put(ID_PREFIX + length, "Edon-R " + length);
            }
        }
        return algos;
    }

    @Override
    public Map<String, String> getAvailableAliases() {
        if (aliases == null) {
            aliases = new LinkedHashMap<>(16); // ceil(12/0.75)
            for (int length : LENGTHS) {
                String id = ID_PREFIX + length;
                aliases.put("edonr-" + length, id);
                aliases.put("edon-r" + length, id);
                aliases.put("edon-r-" + length, id);
            }
        }
        return aliases;
    }

    @Override
    public AbstractChecksum getPrimaryImplementation() throws NoSuchAlgorithmException {
        int length = lengthOf(name);
        return new MD(new EdonR(length),
                length <= 256 ? BLOCK_BYTES_32 : BLOCK_BYTES_64);
    }

    /**
     * Returns the digest length in bits that an Edon-R algorithm ID asks for.
     *
     * @param id an Edon-R algorithm ID; both the primary form (edonr512) and the
     * aliases (edonr-512, edon-r512, edon-r-512) are understood
     * @return the digest length in bits
     * @throws NoSuchAlgorithmException if the ID carries no valid length
     */
    private static int lengthOf(String id) throws NoSuchAlgorithmException {
        for (int length : LENGTHS) {
            String suffix = String.valueOf(length);
            if (id.endsWith(suffix)) {
                return length;
            }
        }
        throw new NoSuchAlgorithmException(
                id + " is an invalid id for Edon-R. Must be in format edonr<n> "
                + "where n is 224, 256, 384 or 512");
    }

}
