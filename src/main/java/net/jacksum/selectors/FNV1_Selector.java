/*


  Jacksum 3.6.0 - a checksum utility in Java
  Copyright (c) 2001-2023 Dipl.-Inf. (FH) Johann N. Löfflmann,
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
import net.jacksum.algorithms.checksums.Fnv1_32;
import net.jacksum.algorithms.checksums.Fnv1_n;

/**
 *
 * @author johann
 */
public class FNV1_Selector extends Selector {

    @Override
    public Map<String, String> getAvailableAlgorithms() {
        Map<String, String> map = new LinkedHashMap<>(8); // ceil(6/0.75)
        map.put("fnv-1_32", "FNV-1 (32 bits)");
        map.put("fnv-1_64", "FNV-1 (64 bits)");
        map.put("fnv-1_128", "FNV-1 (128 bits)");
        map.put("fnv-1_256", "FNV-1 (256 bits)");
        map.put("fnv-1_512", "FNV-1 (512 bits)");
        map.put("fnv-1_1024", "FNV-1 (1024 bits)");
        return map;
    }

    @Override
    public AbstractChecksum getPrimaryImplementation() throws NoSuchAlgorithmException {
        String bits = name.substring(6);
        if (bits.equals("32")) {
            // use this specific implementation if possible since it is optimzed
            return new Fnv1_32();
        } else {
            // Fnv1_n is much slower than the specific Fnv1_32
            return new Fnv1_n(bits);
        }
    }

}
