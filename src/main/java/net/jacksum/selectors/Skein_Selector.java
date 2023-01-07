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

import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.algorithms.wrappers.SkeinWrapper;

/**
 *
 * @author johann
 */
public class Skein_Selector extends Selector {

    final static String SKEIN_256 = "skein-256";
    final static String SKEIN_512 = "skein-512";
    final static String SKEIN_1024 = "skein-1024";
    
    @Override
    public Map<String, String> getAvailableAlgorithms() {
        Map<String, String> map = new LinkedHashMap<>(300); // (32+64+128)/0.75=298.66
        for (int i = 8; i <= 256; i += 8) {
            map.put(SKEIN_256 + "-" + i, "Skein-256" + "-" + i);
        }
        for (int i = 8; i <= 512; i += 8) {
            map.put(SKEIN_512 + "-" + i, "Skein-512" + "-" + i);
        }
        for (int i = 8; i <= 1024; i += 8) {
            map.put(SKEIN_1024 + "-" + i, "Skein-1024" + "-" + i);
        }
        return map;
    }
    
    @Override
    public Map<String, String> getAvailableAliases() {
        Map<String, String> map = new LinkedHashMap<>(11); // ceil(8/0.75)
        map.put("skein-256", "skein-256-256");
        map.put("skein-512", "skein-512-512");
        map.put("skein-1024", "skein-1024-1024");
        map.put("skein256", "skein-256-256");
        map.put("skein512", "skein-512-512");
        map.put("skein1024", "skein-1024-1024");
        return map;
    }

    @Override
    public AbstractChecksum getPrimaryImplementation() throws NoSuchAlgorithmException {
        return new SkeinWrapper(name);
    }


}
