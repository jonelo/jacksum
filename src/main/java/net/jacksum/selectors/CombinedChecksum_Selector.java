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
import java.util.Map;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.algorithms.CombinedChecksum;

/**
 *
 * @author johann
 */
public class CombinedChecksum_Selector extends Selector {

  
    @Override
    public boolean doesMatch(String name) {
        return (name.contains("+"));
    }

    @Override
    public AbstractChecksum getImplementation(boolean alternate) throws NoSuchAlgorithmException {
        return new CombinedChecksum(name.split("\\+"), alternate);
    }

    @Override
    public AbstractChecksum getPrimaryImplementation() throws NoSuchAlgorithmException {
        return getImplementation(PRIMARY);
    }

    @Override
    public Map<String, String> getAvailableAlgorithms() {
        return null;
    }


}
