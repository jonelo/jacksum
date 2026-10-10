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

package net.jacksum.algorithms.crcs;

import java.security.NoSuchAlgorithmException;
import net.jacksum.formats.Encoding;

/**
 * The CRC-64 with the ISO polynomial as implemented by the Go package hash/crc64
 * (width 64, poly 0x1B, init and xorout 0xFFFFFFFFFFFFFFFF, reflected in and out).
 */
public class CRC64_GO_ISO extends CrcGeneric {

    /**
     * Creates a new CRC-64/GO-ISO.
     *
     * @throws NoSuchAlgorithmException if the CRC parameters are rejected by {@link CrcGeneric}
     */
    public CRC64_GO_ISO() throws NoSuchAlgorithmException {
        super(64, 0x1bL, 0xffffffffffffffffL, true, true, 0xffffffffffffffffL);
        formatPreferences.setHashEncoding(Encoding.HEX);
        formatPreferences.setFilesizeWanted(false);
    }

    // Testvectors at https://golang.org/src/hash/crc64/crc64_test.go
}
