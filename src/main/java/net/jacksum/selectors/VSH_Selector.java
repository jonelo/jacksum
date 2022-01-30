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
import net.jacksum.zzadopt.de.flexiprovider.core.md.VSH;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.algorithms.wrappers.MDflexiprovider;

/**
 *
 * @author johann
 */
public class VSH_Selector extends Selector {

    private static final String ID = "vsh";
    
    @Override
    public Map<String, String> getAvailableAlgorithms() {
        Map<String, String> map = new LinkedHashMap<>(1);
        map.put(ID, "VSH-1024 (Very Smooth Hash)");
        return map;
    }
    
    @Override
    public Map<String, String> getAvailableAliases() {
        Map<String, String> map = new LinkedHashMap<>(1);
        map.put("vsh-1024", ID);
        return map;
    }

    @Override
    public AbstractChecksum getPrimaryImplementation() throws NoSuchAlgorithmException {
        return new MDflexiprovider(VSH.ALG_NAME);
    }

}
