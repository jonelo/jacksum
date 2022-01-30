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
import net.jacksum.algorithms.wrappers.LSHWrapper;

/**
 *
 * @author johann
 */
public class LSH_Selector extends Selector {

    private static Map<String, String> map = null;

    @Override
    public Map<String, String> getAvailableAlgorithms() {
        if (map == null) {
            map = new LinkedHashMap<>(6);
            map.put("lsh-256-224", "LSH-256-224");
            map.put("lsh-256-256", "LSH-256-256");
            map.put("lsh-512-224", "LSH-512-224");
            map.put("lsh-512-256", "LSH-512-256");
            map.put("lsh-512-384", "LSH-512-384");
            map.put("lsh-512-512", "LSH-512-512");
        }
        return map;
    }

    @Override
    public AbstractChecksum getPrimaryImplementation() throws NoSuchAlgorithmException {
        return new LSHWrapper(name);
    }

}
