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
import net.jacksum.algorithms.checksums.SumPlan9;
import net.jacksum.algorithms.crcs.CrcGeneric;

/**
 *
 * @author Johann N. Löfflmann
 */
public class CRC32fddi_Selector extends Selector {

    private static final String ID = "crc32_fddi";
    
    @Override
    public Map<String, String> getAvailableAlgorithms() {
        Map<String, String> map = new LinkedHashMap<>(2); // ceil(1/0.75)
        map.put(ID, "CRC-32 (FDDI)");
        return map;
    }
    
    @Override
    public Map<String, String> getAvailableAliases() {
        Map<String, String> map = new LinkedHashMap<>(2); // ceil(1/0.75)
        map.put("sum_plan9", ID);
        return map;
    }
    
    @Override
    public AbstractChecksum getPrimaryImplementation() throws NoSuchAlgorithmException {
        return new SumPlan9();
    }

    @Override
    public AbstractChecksum getAlternateImplementation() throws NoSuchAlgorithmException {
        return new CrcGeneric(32, 0x04C11DB7L, 0, true, true, 0, true, new byte[]{(byte)0xCC, (byte)0x55, (byte)0xCC, (byte)0x55});
    }
    
}
