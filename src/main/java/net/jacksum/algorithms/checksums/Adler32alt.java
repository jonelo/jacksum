/*


  Jacksum 3.0.0 - a checksum utility in Java
  Copyright (c) 2001-2021 Dipl.-Inf. (FH) Johann N. Löfflmann,
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

// http://www.faqs.org/rfcs/rfc1950.html
//
// Adler-32 is composed of two sums accumulated per byte: s1 is
// the sum of all bytes, s2 is the sum of all s1 values. Both sums
// are done modulo 65521. s1 is initialized to 1, s2 to zero. The
// Adler-32 checksum is stored as s2*65536 + s1 in most-
// significant-byte first (network) order.

package net.jacksum.algorithms.checksums;

import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.formats.Encoding;

/**
 * A class that can be used to compute the Adler32 of a data stream (alternate).
 * This is a 100% Java implementation.
 */

public class Adler32alt extends AbstractChecksum {

    protected long value;
    private static final long BASE = 65521L; // largest prime smaller than 65536 (2^16)

    public Adler32alt() {
        super();
        bitWidth = 32;
        formatPreferences.setHashEncoding(Encoding.DEC);
        formatPreferences.setSizeWanted(true);
        formatPreferences.setSeparator(" ");
        value = 1L;
    }

    @Override
    public void reset() {
        value = 1L;
        length = 0;
    }

    @Override
    public void update(byte[] buffer, int offset, int len) {
        long s1 = value & 0xffff;
        long s2 = (value >> 16) & 0xffff;

        for (int n = offset; n < len + offset; n++) {
            s1 = (s1 + (buffer[n] & 0xff)) % BASE;
            s2 = (s2 + s1)                 % BASE;
        }

        value = (s2 << 16) | s1;
        length+=len;
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
