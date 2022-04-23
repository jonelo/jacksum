/*


  Jacksum 3.3.0 - a checksum utility in Java
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
import net.jacksum.algorithms.wrappers.MDgnu;

/**
 *
 * @author johann
 */
public class Haval_Selector extends Selector {

    @Override
    public Map<String, String> getAvailableAlgorithms() {
        Map<String, String> map = new LinkedHashMap<>(20); // ceil(15/0.75)
        map.put("haval_128_3", "HAVAL 128 (3 rounds)");
        map.put("haval_128_4", "HAVAL 128 (4 rounds)");
        map.put("haval_128_5", "HAVAL 128 (5 rounds)");
        map.put("haval_160_3", "HAVAL 160 (3 rounds)");
        map.put("haval_160_4", "HAVAL 160 (4 rounds)");
        map.put("haval_160_5", "HAVAL 160 (5 rounds)");
        map.put("haval_192_3", "HAVAL 192 (3 rounds)");
        map.put("haval_192_4", "HAVAL 192 (4 rounds)");
        map.put("haval_192_5", "HAVAL 192 (5 rounds)");
        map.put("haval_224_3", "HAVAL 224 (3 rounds)");
        map.put("haval_224_4", "HAVAL 224 (4 rounds)");
        map.put("haval_224_5", "HAVAL 224 (5 rounds)");
        map.put("haval_256_3", "HAVAL 256 (3 rounds)");
        map.put("haval_256_4", "HAVAL 256 (4 rounds)");
        map.put("haval_256_5", "HAVAL 256 (5 rounds)");
        return map;
    }

    @Override
    public Map<String, String> getAvailableAliases() {
        Map<String, String> map = new LinkedHashMap<>(2); // ceil(1/0.75)
        map.put("haval", "haval_128_3");
        return map;
    }

    @Override
    public AbstractChecksum getPrimaryImplementation() throws NoSuchAlgorithmException {
        return new MDgnu(name);
    }

}
