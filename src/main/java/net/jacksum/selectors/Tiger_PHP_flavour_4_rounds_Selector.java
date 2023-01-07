/*


  Jacksum 3.5.0 - a checksum utility in Java
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

import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.algorithms.wrappers.MD;
import net.jacksum.algorithms.wrappers.MDbouncycastle;

import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Johann N. Löfflmann
 */
public class Tiger_PHP_flavour_4_rounds_Selector extends Selector {
    private static final String ID_192_4 = "tiger-192-4-php";
    private static final String ID_160_4 = "tiger-160-4-php";
    private static final String ID_128_4 = "tiger-128-4-php";
    
    @Override
    public Map<String, String> getAvailableAlgorithms() {
        Map<String, String> map = new LinkedHashMap<>(4); // ceil(3/0.75)
        map.put(ID_192_4, "PHP's tiger192,4");
        map.put(ID_160_4, "PHP's tiger160,4");
        map.put(ID_128_4, "PHP's tiger128,4");
        return map;
    }
    
    @Override
    public Map<String, String> getAvailableAliases() {
        Map<String, String> map = new LinkedHashMap<>(4); // ceil(3/0.75)
        map.put("tiger_192_4_php", ID_192_4);
        map.put("tiger_160_4_php", ID_160_4);
        map.put("tiger_128_4_php", ID_128_4);
        return map;
    }

    @Override
    public AbstractChecksum getPrimaryImplementation() throws NoSuchAlgorithmException {
        return new MDbouncycastle(name);
    }

}
