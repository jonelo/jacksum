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

package net.jacksum.algorithms.checksums;

import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.formats.Encoding;

public class Sum8 extends AbstractChecksum {

    protected long value;

    public Sum8() {
        super();
        bitWidth = 8;
        formatPreferences.setHashEncoding(Encoding.DEC);
        formatPreferences.setFilesizeWanted(true);
        formatPreferences.setSeparator(" ");
    }

    @Override
    public void reset() {
        value = 0;
        length = 0;
    }

    @Override
    public void update(byte[] bytes, int offset, int length) {
        for (int i = offset; i < length + offset; i++) {
            value += bytes[i] & 0xFF;
        }
        this.length += length;
    }

    @Override
    public long getValue() {
        return value % 0x100; // 2^8
    }

    @Override
    public byte[] getByteArray() {
        return new byte[]
         {(byte)(getValue()&0xff)};
    }

}

/*
    Testvector from the PC Magazin 06/1996, p 237:

    decimal:
      36 211 163 4 109 192 58 247 47 92 => 135
    hex:
      24 D3 A3 04 6D C0 3A F7 2F 5C => 87

    check again with Jacksum 2.0.0
    jacksum -a sum8 -q dec:36,211,163,4,109,192,58,247,47,92
    135     10

 */
