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

package net.jacksum.algorithms.checksums;

import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.formats.Encoding;

public class Xor8 extends AbstractChecksum {

    private long value;

    public Xor8() {
        super();
        bitWidth = 8;
        value = 0;
        formatPreferences.setHashEncoding(Encoding.DEC);
        formatPreferences.setSeparator(" ");
        formatPreferences.setFilesizeWanted(true);
    }

    @Override
    public void update(byte[] bytes, int offset, int length) {
        for (int i = offset; i < length + offset; i++) {
            value ^= bytes[i] & 0xFF;
        }
        this.length += length;
    }

    @Override
    public void reset() {
        value = 0;
        length = 0;
    }

    @Override
    public long getValue() {
        return value;
    }

    @Override
    public byte[] getByteArray() {
        return new byte[]{(byte) (value & 0xff)};
    }
}

/*
 * Testvector from Motorola's GPS:
 * (http://www.motorola.com/ies/GPS/docs_pdf/checksum.pdf)
 *
 * hex: 45 61 01 => 25
 */
