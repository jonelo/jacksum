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

package net.jacksum.algorithms.crcs;

import net.jacksum.algorithms.checksums.Cksum;
import net.jacksum.formats.Encoding;

public class CRC32_MPEG2 extends Cksum implements CrcInfo {

    public CRC32_MPEG2() {
        super();
        formatPreferences.setHashEncoding(Encoding.DEC);
        formatPreferences.setSeparator(" ");
        formatPreferences.setFilesizeWanted(true);
        reset();
    }

    @Override
    public final void reset() {
        value = 0xFFFFFFFF;
        length = 0;
    }

    // this method is provided in the superclass, but 'value' is overridden
    // the MPEG2 CRC is just the raw value as below
    @Override
    public long getValue() {
       return (value & 0xFFFFFFFFL);
    }

    @Override
    public byte[] getPolyAsBytes() {
        return new byte[] {(byte)0x04, (byte)0xC1, (byte)0x1D, (byte)0xB7};
    }

    @Override
    public int getWidth() {
        return 32;
    }

    @Override
    public long getInitialValue() {
        return 0xFFFFFFFFL;
    }

    @Override
    public boolean isRefIn() {
        return false;
    }

    @Override
    public boolean isRefOut() {
        return false;
    }

    @Override
    public long getXorOut() {
        return 0;
    }
}
