/*


  Jacksum 3.4.0 - a checksum utility in Java
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

/**
 *
 * @author johann
 */
public interface SelectorInterface {

    // ID, description    
    Map<String, String> getAvailableAlgorithms();

    // alias, ID
    Map<String, String> getAvailableAliases();

    void setName(String name);

    String getName();

    boolean doesMatch(String name);

    AbstractChecksum getImplementation(boolean alternate) throws NoSuchAlgorithmException;

    AbstractChecksum getPrimaryImplementation() throws NoSuchAlgorithmException;

    AbstractChecksum getAlternateImplementation() throws NoSuchAlgorithmException;

    boolean isActualAlternateImplementationUsed();
}
