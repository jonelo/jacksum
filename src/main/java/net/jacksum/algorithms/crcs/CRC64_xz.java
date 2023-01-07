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

package net.jacksum.algorithms.crcs;

import java.security.NoSuchAlgorithmException;
import net.jacksum.formats.Encoding;

public class CRC64_xz extends CrcGeneric {

    public CRC64_xz() throws NoSuchAlgorithmException {
        super(64, 0x42f0e1eba9ea3693L, 0xffffffffffffffffL, true, true, 0xffffffffffffffffL);
        formatPreferences.setHashEncoding(Encoding.HEX);
        formatPreferences.setFilesizeWanted(false);
    }
    
    // Testvectors at https://golang.org/src/hash/crc64/crc64_test.go

}
