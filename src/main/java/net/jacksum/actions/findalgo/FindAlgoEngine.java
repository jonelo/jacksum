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
package net.jacksum.actions.findalgo;

import java.math.BigInteger;
import net.jacksum.parameters.ParameterException;

/**
 *
 * @author Johann N. Loefflmann
 */
public interface FindAlgoEngine {    
    
    /**
     * Finds algorithms producing width bits
     * @param width the width in bits that an algorithm produces
     * @throws ParameterException if the parameter is invalid.
     */
    void find(int width) throws ParameterException;
    
    BigInteger getSearched();
    
    long getFound();
}
