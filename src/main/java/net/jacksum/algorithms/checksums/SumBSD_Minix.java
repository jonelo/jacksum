/*


  Jacksum 4.0.2 - a checksum/hash tool written in Java
  Copyright (c) 2001-2026 Dipl.-Inf. (FH) Johann N. Löfflmann,
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

package net.jacksum.algorithms.checksums;

import net.jacksum.formats.Encoding;

/**
 * The BSD sum algorithm with the output format of the sum command on Minix: the checksum in
 * decimal without leading zeros, and the size in 512-byte blocks.
 */
public class SumBSD_Minix extends SumBSD {
    
    /**
     * Creates a new BSD sum with the output format of Minix.
     */
    public SumBSD_Minix() {
        super();
        formatPreferences.setHashEncoding(Encoding.DEC); // no leading zeros for the sum on Minix
        formatPreferences.setSizeAsByteBlocks(512);
        formatPreferences.setSizeWithPrintfFormatted(null);
    }

}
