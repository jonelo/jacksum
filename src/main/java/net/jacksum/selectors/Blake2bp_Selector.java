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
import net.jacksum.algorithms.wrappers.Blake2bpWrapper;

/**
 *
 * @author johann
 */
public class Blake2bp_Selector extends Selector {

    private static Map<String, String> algos;
    private static Map<String, String> aliases;

    @Override
    public Map<String, String> getAvailableAlgorithms() {
        if (algos == null) {
            algos = new LinkedHashMap<>(2); // ceil(1/0.75)
            algos.put("blake2bp", "BLAKE2bp");
        }
        return algos;
    }

    @Override
    public Map<String, String> getAvailableAliases() {
        if (aliases == null) {
            aliases = new LinkedHashMap<>(2); // ceil(1/0.75)
            aliases.put("blake2bp-512", "blake2bp");
        }
        return aliases;
    }

    @Override
    public AbstractChecksum getPrimaryImplementation() throws NoSuchAlgorithmException {
        return new Blake2bpWrapper(name);
    }

}
