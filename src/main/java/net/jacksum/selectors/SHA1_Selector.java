/*


  Jacksum 3.5.0 - a checksum utility in Java
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
import net.jacksum.algorithms.wrappers.MD;
import net.jacksum.algorithms.wrappers.MDgnu;

/**
 *
 * @author johann
 */
public class SHA1_Selector extends Selector {
    
    private static final String ID = "sha-1";
    
    @Override
    public Map<String, String> getAvailableAlgorithms() {
        Map<String, String> map = new LinkedHashMap<>(2); // ceil(1/0.75)
        map.put(ID, "SHA-1 (SHA-160)");
        return map;
    }
    
    @Override
    public Map<String, String> getAvailableAliases() {
        Map<String, String> map = new LinkedHashMap<>(8); // ceil(6/0.75)
        map.put("sha", ID);
        map.put("sha1", ID);
        map.put("sha1sum", ID);
        map.put("sha160", ID);
        map.put("sha-160", ID);
        map.put("dss1", ID);
        return map;
    }
    
    @Override
    public AbstractChecksum getPrimaryImplementation() throws NoSuchAlgorithmException {
        return new MD("SHA-1", 64);
    }

    @Override
    public AbstractChecksum getAlternateImplementation() throws NoSuchAlgorithmException {
        return new MDgnu(net.jacksum.zzadopt.gnu.crypto.Registry.SHA160_HASH);
    }
}
