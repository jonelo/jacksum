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

/*

  The original CRC8.java is from the project
  http://sourceforge.net/projects/jflac
  (org.kc7bfi.jflac.util.CRC8.java)
  which is distributed under the LGPL

  the jFLAC is derived from the FLAC lib


  libFLAC - Free Lossless Audio Codec library
  Copyright (C) 2000,2001,2002,2003  Josh Coalson

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Library General Public
  License as published by the Free Software Foundation; either
  version 2 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Library General Public License for more details.

  You should have received a copy of the GNU Library General Public
  License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place - Suite 330,
  Boston, MA  02111-1307, USA.

 */

package net.jacksum.algorithms.crcs;

import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.formats.Encoding;

/**
 * A class that can be used to compute the Crc8 of a data stream.
 */
public class CRC8 extends AbstractChecksum {

    private byte value;
    // CRC-8, poly = x^8 + x^2 + x^1 + 1, init = 0
    // 1 0000 0111
    // 0111 0000 1

    private static final byte[] CRC8_TABLE =
        new byte[] {
            (byte) 0x00,
            (byte) 0x07,
            (byte) 0x0E,
            (byte) 0x09,
            (byte) 0x1C,
            (byte) 0x1B,
            (byte) 0x12,
            (byte) 0x15,
            (byte) 0x38,
            (byte) 0x3F,
            (byte) 0x36,
            (byte) 0x31,
            (byte) 0x24,
            (byte) 0x23,
            (byte) 0x2A,
            (byte) 0x2D,
            (byte) 0x70,
            (byte) 0x77,
            (byte) 0x7E,
            (byte) 0x79,
            (byte) 0x6C,
            (byte) 0x6B,
            (byte) 0x62,
            (byte) 0x65,
            (byte) 0x48,
            (byte) 0x4F,
            (byte) 0x46,
            (byte) 0x41,
            (byte) 0x54,
            (byte) 0x53,
            (byte) 0x5A,
            (byte) 0x5D,
            (byte) 0xE0,
            (byte) 0xE7,
            (byte) 0xEE,
            (byte) 0xE9,
            (byte) 0xFC,
            (byte) 0xFB,
            (byte) 0xF2,
            (byte) 0xF5,
            (byte) 0xD8,
            (byte) 0xDF,
            (byte) 0xD6,
            (byte) 0xD1,
            (byte) 0xC4,
            (byte) 0xC3,
            (byte) 0xCA,
            (byte) 0xCD,
            (byte) 0x90,
            (byte) 0x97,
            (byte) 0x9E,
            (byte) 0x99,
            (byte) 0x8C,
            (byte) 0x8B,
            (byte) 0x82,
            (byte) 0x85,
            (byte) 0xA8,
            (byte) 0xAF,
            (byte) 0xA6,
            (byte) 0xA1,
            (byte) 0xB4,
            (byte) 0xB3,
            (byte) 0xBA,
            (byte) 0xBD,
            (byte) 0xC7,
            (byte) 0xC0,
            (byte) 0xC9,
            (byte) 0xCE,
            (byte) 0xDB,
            (byte) 0xDC,
            (byte) 0xD5,
            (byte) 0xD2,
            (byte) 0xFF,
            (byte) 0xF8,
            (byte) 0xF1,
            (byte) 0xF6,
            (byte) 0xE3,
            (byte) 0xE4,
            (byte) 0xED,
            (byte) 0xEA,
            (byte) 0xB7,
            (byte) 0xB0,
            (byte) 0xB9,
            (byte) 0xBE,
            (byte) 0xAB,
            (byte) 0xAC,
            (byte) 0xA5,
            (byte) 0xA2,
            (byte) 0x8F,
            (byte) 0x88,
            (byte) 0x81,
            (byte) 0x86,
            (byte) 0x93,
            (byte) 0x94,
            (byte) 0x9D,
            (byte) 0x9A,
            (byte) 0x27,
            (byte) 0x20,
            (byte) 0x29,
            (byte) 0x2E,
            (byte) 0x3B,
            (byte) 0x3C,
            (byte) 0x35,
            (byte) 0x32,
            (byte) 0x1F,
            (byte) 0x18,
            (byte) 0x11,
            (byte) 0x16,
            (byte) 0x03,
            (byte) 0x04,
            (byte) 0x0D,
            (byte) 0x0A,
            (byte) 0x57,
            (byte) 0x50,
            (byte) 0x59,
            (byte) 0x5E,
            (byte) 0x4B,
            (byte) 0x4C,
            (byte) 0x45,
            (byte) 0x42,
            (byte) 0x6F,
            (byte) 0x68,
            (byte) 0x61,
            (byte) 0x66,
            (byte) 0x73,
            (byte) 0x74,
            (byte) 0x7D,
            (byte) 0x7A,
            (byte) 0x89,
            (byte) 0x8E,
            (byte) 0x87,
            (byte) 0x80,
            (byte) 0x95,
            (byte) 0x92,
            (byte) 0x9B,
            (byte) 0x9C,
            (byte) 0xB1,
            (byte) 0xB6,
            (byte) 0xBF,
            (byte) 0xB8,
            (byte) 0xAD,
            (byte) 0xAA,
            (byte) 0xA3,
            (byte) 0xA4,
            (byte) 0xF9,
            (byte) 0xFE,
            (byte) 0xF7,
            (byte) 0xF0,
            (byte) 0xE5,
            (byte) 0xE2,
            (byte) 0xEB,
            (byte) 0xEC,
            (byte) 0xC1,
            (byte) 0xC6,
            (byte) 0xCF,
            (byte) 0xC8,
            (byte) 0xDD,
            (byte) 0xDA,
            (byte) 0xD3,
            (byte) 0xD4,
            (byte) 0x69,
            (byte) 0x6E,
            (byte) 0x67,
            (byte) 0x60,
            (byte) 0x75,
            (byte) 0x72,
            (byte) 0x7B,
            (byte) 0x7C,
            (byte) 0x51,
            (byte) 0x56,
            (byte) 0x5F,
            (byte) 0x58,
            (byte) 0x4D,
            (byte) 0x4A,
            (byte) 0x43,
            (byte) 0x44,
            (byte) 0x19,
            (byte) 0x1E,
            (byte) 0x17,
            (byte) 0x10,
            (byte) 0x05,
            (byte) 0x02,
            (byte) 0x0B,
            (byte) 0x0C,
            (byte) 0x21,
            (byte) 0x26,
            (byte) 0x2F,
            (byte) 0x28,
            (byte) 0x3D,
            (byte) 0x3A,
            (byte) 0x33,
            (byte) 0x34,
            (byte) 0x4E,
            (byte) 0x49,
            (byte) 0x40,
            (byte) 0x47,
            (byte) 0x52,
            (byte) 0x55,
            (byte) 0x5C,
            (byte) 0x5B,
            (byte) 0x76,
            (byte) 0x71,
            (byte) 0x78,
            (byte) 0x7F,
            (byte) 0x6A,
            (byte) 0x6D,
            (byte) 0x64,
            (byte) 0x63,
            (byte) 0x3E,
            (byte) 0x39,
            (byte) 0x30,
            (byte) 0x37,
            (byte) 0x22,
            (byte) 0x25,
            (byte) 0x2C,
            (byte) 0x2B,
            (byte) 0x06,
            (byte) 0x01,
            (byte) 0x08,
            (byte) 0x0F,
            (byte) 0x1A,
            (byte) 0x1D,
            (byte) 0x14,
            (byte) 0x13,
            (byte) 0xAE,
            (byte) 0xA9,
            (byte) 0xA0,
            (byte) 0xA7,
            (byte) 0xB2,
            (byte) 0xB5,
            (byte) 0xBC,
            (byte) 0xBB,
            (byte) 0x96,
            (byte) 0x91,
            (byte) 0x98,
            (byte) 0x9F,
            (byte) 0x8A,
            (byte) 0x8D,
            (byte) 0x84,
            (byte) 0x83,
            (byte) 0xDE,
            (byte) 0xD9,
            (byte) 0xD0,
            (byte) 0xD7,
            (byte) 0xC2,
            (byte) 0xC5,
            (byte) 0xCC,
            (byte) 0xCB,
            (byte) 0xE6,
            (byte) 0xE1,
            (byte) 0xE8,
            (byte) 0xEF,
            (byte) 0xFA,
            (byte) 0xFD,
            (byte) 0xF4,
            (byte) 0xF3 };

    public CRC8() {
       super();
       bitWidth = 8;
       value = 0;
       formatPreferences.setHashEncoding(Encoding.DEC);
       formatPreferences.setSeparator(" ");
       formatPreferences.setFilesizeWanted(true);
    }

    @Override
    public void reset() {
        value = 0;
        length = 0;
    }

    @Override
    public void update(byte[] bytes, int offset, int length) {
        for (int i = offset; i < length + offset; i++) {
            value = CRC8_TABLE[(value ^ bytes[i]) &0xFF ];
        }
        this.length += length;
    }

    @Override
    public long getValue() {
        return (((long)value)&0xFFL);
    }

    @Override
    public byte[] getByteArray() {
        return new byte[]{ value };
    }

}

