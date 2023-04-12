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

package net.jacksum.algorithms.checksums;

import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.formats.Encoding;

public class Fnv0_64 extends AbstractChecksum {

    // protected final long PRIME = 0x100000001b3L;
    protected long value;

    public Fnv0_64() {
        super();
        bitWidth = 64;
        formatPreferences.setHashEncoding(Encoding.HEX);
        formatPreferences.setFilesizeWanted(false);
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
            // value = value * PRIME; // won't work for 64 bit
            // therefore we use bit shift to the left, because
            // x << k == x*2^k
            // PRIME = 10000000000000000000000000000000110110011
            value += (value << 1) + (value << 4) + (value << 5) +
                    (value << 7) + (value << 8) + (value << 40);
            value ^= (bytes[i] & 0xFF);
        }
        this.length += length;
    }

    @Override
    public long getValue() {
        return value;
    }

    @Override
    public byte[] getByteArray() {
        long val = getValue();
        return new byte[]{
                (byte) ((val >>> 56) & 0xff),
                (byte) ((val >> 48) & 0xff),
                (byte) ((val >> 40) & 0xff),
                (byte) ((val >> 32) & 0xff),
                (byte) ((val >> 24) & 0xff),
                (byte) ((val >> 16) & 0xff),
                (byte) ((val >> 8) & 0xff),
                (byte) (val & 0xff)};
    }


}
