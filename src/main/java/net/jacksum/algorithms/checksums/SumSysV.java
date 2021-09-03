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

// implemented in Java from original GNU C source

import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.formats.Encoding;


public class SumSysV extends AbstractChecksum {

    private long value;

    public SumSysV() {
        super();
        bitWidth = 16;
        formatPreferences.setHashEncoding(Encoding.DEC);
        formatPreferences.setSeparator(" ");
        formatPreferences.setSizeWanted(true);
        formatPreferences.setSizeAsByteBlocks(512);        
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
        long r = (value & 0xffff) + (((value & 0xffffffff) >> 16) & 0xffff);
        value = (r & 0xffff) + (r >> 16);
        return value;
    }


    @Override
    public byte[] getByteArray() {
        long val = getValue();
        return new byte[]
        {(byte)((val>>8)&0xff),
         (byte)(val&0xff)};
    }

}
