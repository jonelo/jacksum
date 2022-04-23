/*


  Jacksum 3.3.0 - a checksum utility in Java
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

package net.jacksum.algorithms.crcs;
/*
  A class that can be used to compute the CRC-32C of a data stream.
  This implementation uses the class java.util.zip.CRC32C from the Java Standard API.
 */

import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.formats.Encoding;

public class CRC32C extends AbstractChecksum {

    private final java.util.zip.CRC32C crc32c;

    public CRC32C() {
        super();
        formatPreferences.setHashEncoding(Encoding.DEC);
        formatPreferences.setSeparator(" ");
        formatPreferences.setFilesizeWanted(true);
        bitWidth = 32;
        crc32c = new java.util.zip.CRC32C();
    }

    @Override
    public void reset() {
        crc32c.reset();
        length = 0;
    }

    @Override
    public void update(byte[] buffer, int offset, int len) {
        crc32c.update(buffer, offset, len);
        length += len;
    }

    @Override
    public void update(int integer) {
        crc32c.update(integer);
        length++;
    }

    @Override
    public void update(byte b) {
        update(b & 0xFF);
    }

    @Override
    public long getValue() {
        return crc32c.getValue();
    }

    @Override
    public byte[] getByteArray() {
        long val = crc32c.getValue();
        return new byte[]
        {(byte)((val>>24)&0xff),
         (byte)((val>>16)&0xff),
         (byte)((val>>8)&0xff),
         (byte)(val&0xff)};
    }

}
