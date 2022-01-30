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

package net.jacksum.algorithms.checksums;

import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.formats.Encoding;

public class Elf extends AbstractChecksum {

    private long value;

    public Elf() {
        super();
        bitWidth = 32;
        formatPreferences.setHashEncoding(Encoding.DEC);
        formatPreferences.setSizeWanted(true);
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
            value = (value << 4) + (bytes[i] & 0xFF);
            long ghash = value & 0xF0000000L;
            if(ghash != 0) {
               value ^= (ghash >>> 24);
            }
            value &= ~ghash;
        }
        this.length += length;
    }

    @Override
    public byte[] getByteArray() {
        long val = getValue();
        return new byte[]
        {(byte)((val>>24)&0xff),
         (byte)((val>>16)&0xff),
         (byte)((val>>8)&0xff),
         (byte)(val&0xff)};
    }

    @Override
    public long getValue() {
        return value;
    }

}
