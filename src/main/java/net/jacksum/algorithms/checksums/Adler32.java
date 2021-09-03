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

package net.jacksum.algorithms.checksums;

import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.formats.Encoding;

/**
 * A class that can be used to compute the Adler32 of a data stream.
 * This implementation uses the class java.util.zip.Adler32 from the Java Standard API.
 */

public class Adler32 extends AbstractChecksum {
    
    private final java.util.zip.Adler32 adler32;
    
    public Adler32() {
        super();
        bitWidth = 32;
        adler32 = new java.util.zip.Adler32();
        formatPreferences.setHashEncoding(Encoding.DEC);
        formatPreferences.setSizeWanted(true);
        formatPreferences.setSeparator(" ");
    }
    
    @Override
    public void reset() {
        adler32.reset();
        length = 0;
    }
    
    @Override
    public void update(byte[] buffer, int offset, int length) {
        adler32.update(buffer, offset, length);
        this.length += length;
    }
    
    @Override
    public void update(int b) {
        adler32.update(b);
        length++;
    }
    
    @Override
    public long getValue() {
        return adler32.getValue();
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
    
}
