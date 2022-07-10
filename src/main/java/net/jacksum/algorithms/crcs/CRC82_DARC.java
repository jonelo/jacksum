/*
 * Jacksum 3.2.0 - a checksum utility in Java
 * Copyright (c) 2001-2022 Dipl.-Inf. (FH) Johann N. Löfflmann,
 * All Rights Reserved, <https://jacksum.net>.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <https://www.gnu.org/licenses/>.
 */

package net.jacksum.algorithms.crcs;

import net.jacksum.algorithms.AbstractChecksum;

// Credit: this code has been translated from C to Java by Johann N. Löfflmann.
// The original C code has been written by Mark Adler who placed the code
// into the public domain on June 17, 2017.
// see also https://stackoverflow.com/questions/44606008/crc-82-darc-function-in-c-sharp

// CRC definition:
// width=82 poly=0x0308c0111011401440411 init=0 refin=true refout=true xorout=0
// check=0x09ea83f625023801fd612 name="CRC-82/DARC"
public class CRC82_DARC extends AbstractChecksum {

    private long[] crc = new long[2];

    private long POLYHIGH = 0x22080L;
    private long POLYLOW = 0x8a00a2022200c430L;

    @Override
    public void reset() {
        // initialize crc[0..1] with the CRC-82/DARC of an empty message.
        crc[0] = crc[1] = 0;
    }

    // Update crc[0..1] with the CRC-82/DARC of the length bytes at buf.
    // The low 64 bits of the CRC are in crc[0], and the high 18 bits of the CRC
    // are in the low 18 bits of crc[1]. The remaining bits of crc[1] are always
    // zero.
    @Override
    public void update(byte[] bytes, int offset, int length) {
        long cl = crc[0], ch = crc[1] & 0x3ffffL;
        for (int i = 0; i < length; i++) {
            cl ^= (int)(bytes[i] & 0xFF);
            for (int k = 0; k < 8; k++) {
                long low = cl & 1;
                cl = (cl >>> 1) | (ch << 63);
                ch >>>= 1;
                if (low==1) {
                    cl ^= POLYLOW;
                    ch ^= POLYHIGH;
                }
            }
        }
        crc[0] = cl;
        crc[1] = ch;
    }

    @Override
    public byte[] getByteArray() {
        return new byte[]
            {
             // the high 18 bits of the CRC are in the low 18 bits of crc[1]
             (byte)((crc[1]>>16)&0b11),
             (byte)((crc[1]>>8)&0xff),
             (byte)(crc[1]&0xff),

             // The low 64 bits of the CRC are in crc[0]
             (byte)((crc[0]>>56)&0xff),
             (byte)((crc[0]>>48)&0xff),
             (byte)((crc[0]>>40)&0xff),
             (byte)((crc[0]>>32)&0xff),
             (byte)((crc[0]>>24)&0xff),
             (byte)((crc[0]>>16)&0xff),
             (byte)((crc[0]>>8)&0xff),
             (byte)(crc[0]&0xff)};
    }
}
