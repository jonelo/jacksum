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
import net.jacksum.algorithms.wrappers.MDgnu;

/**
 *
 * @author johann
 */
public class Whirlpool1_Selector extends Selector {

    private final static String ID = "whirlpool1";
    private final static String DESCRIPTION = "Whirlpool-1";
        
    private final static Map<String, String> availableAlgorithms;
    private final static Map<String, String> availableAliases;

    static {
        availableAlgorithms = new LinkedHashMap<>(2); // ceil(1/0.75)
        availableAlgorithms.put(ID, DESCRIPTION);
        
        availableAliases = new LinkedHashMap<>(3); // ceil(2/0.75)
        availableAliases.put("whirlpool-l", ID);
        availableAliases.put("whirlpool-t", ID);
    }
    
    
    @Override
    public AbstractChecksum getPrimaryImplementation() throws NoSuchAlgorithmException {
        return new MDgnu(net.jacksum.zzadopt.gnu.crypto.Registry.WHIRLPOOL2001_HASH);
    }
    
    @Override
    public Map<String, String> getAvailableAlgorithms() {
        return availableAlgorithms;
    }

    @Override
    public Map<String, String> getAvailableAliases() {
        return availableAliases;
    }

}
