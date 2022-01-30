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
import java.util.HashMap;
import java.util.Map;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.algorithms.wrappers.MDCryptohashSphlib3;

/**
 *
 * @author johann
 */
public class JH384_Selector extends Selector {

    private static final String ID = "jh384";
    
    @Override
    public Map<String, String> getAvailableAlgorithms() {
        Map<String, String> map = new HashMap<>(1);
        map.put(ID, "JH384");
        return map;
    }
    
    @Override
    public Map<String, String> getAvailableAliases() {
        Map<String, String> map = new HashMap<>(1);
        map.put("jh-384", ID);
        return map;
    }

    @Override
    public AbstractChecksum getPrimaryImplementation() throws NoSuchAlgorithmException {
        return new MDCryptohashSphlib3(net.jacksum.zzadopt.fr.cryptohash.ext.Registry.JH384);
    }

}