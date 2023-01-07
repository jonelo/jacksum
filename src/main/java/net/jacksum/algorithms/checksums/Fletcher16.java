/*


  Jacksum 3.5.0 - a checksum utility in Java
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

/**
 * A class that can be used to compute the Fletcher16 of a data stream.
 */
public class Fletcher16 extends AbstractChecksum {

    protected long value;
    private static final long BASE = 255L;

    public Fletcher16() {
        super();
        bitWidth = 16;
        value = 0L;
        formatPreferences.setHashEncoding(Encoding.DEC);
        formatPreferences.setFilesizeWanted(true);
        formatPreferences.setSeparator(" ");
    }

    @Override
    public void reset() {
        value = 0L;
        length = 0;
    }

    @Override
    public void update(byte[] buffer, int offset, int len) {
        long s1 = 0;
        long s2 = 0;

        for (int n = offset; n < len + offset; n++) {
            s1 = (s1 + (buffer[n] & 0xff)) % BASE;
            s2 = (s2 + s1) % BASE;
        }

        value = (s2 << 8) | s1;
        length += len;
    }

    @Override
    public byte[] getByteArray() {
        return new byte[]{(byte) ((value >> 8) & 0xff),
            (byte) (value & 0xff)};
    }

    @Override
    public long getValue() {
        return value;
    }

}
