/*


  Jacksum 3.2.0 - a checksum utility in Java
  Copyright (c) 2001-2022 Dipl.-Inf. (FH) Johann N. Löfflmann,
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
import net.jacksum.algorithms.wrappers.Blake2bWrapper;

/**
 *
 * @author johann
 */
public class Blake2b_Selector extends Selector {

    private static Map<String, String> algos;
    private static Map<String, String> aliases;

    private static void _fillMap(Map<String, String> map, String keyPrefix, String valuePrefix) {
        for (int i = 8; i <= 512; i += 8) {
            map.put(keyPrefix + i, valuePrefix + i);
        }
    }

    @Override
    public Map<String, String> getAvailableAlgorithms() {
        if (algos == null) {
            algos = new LinkedHashMap<>(64);
            _fillMap(algos, "blake2b-", "BLAKE2b-");
        }
        return algos;
    }

    @Override
    public Map<String, String> getAvailableAliases() {
        if (aliases == null) {
            aliases = new LinkedHashMap<>(66);
            aliases.put("blake2b", "blake2b-512");
            _fillMap(aliases, "b2sum-", "blake2b-");
            aliases.put("b2sum", "blake2b-512");
        }
        return aliases;
    }

    @Override
    public AbstractChecksum getPrimaryImplementation() throws NoSuchAlgorithmException {
        return new Blake2bWrapper(name);
    }

}
