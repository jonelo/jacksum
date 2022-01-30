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

public class Sum56 extends Sum8 {

    public Sum56() {
        super();
        bitWidth = 56;
        value = 0;
    }

    @Override
    public long getValue() {
        return value % 0x100000000000000L; // 2^56
    }
    
    @Override
    public void update(byte[] bytes, int offset, int length) {
        for (int i = offset; i < length + offset; i++) {
            value += bytes[i] & 0xFF;
        }
        // important, because otherwise we could get an overflow
        // if the filesize is 2^56 bytes, full of 0xFF's, value could
        // become 2^64, and that exceeds the Java Max long, which is 2^63
        // (a signed long), so we can avoid that by dividing value by
        // 2^56 after each update call but hey, who is going to calc a
        // sum56 on 65536 Tebibytes ? :-/
        value = getValue();
        this.length += length;
    }

    @Override
    public byte[] getByteArray() {
        long val = getValue();
        return new byte[]{
            (byte) ((val >> 48) & 0xff),
            (byte) ((val >> 40) & 0xff),
            (byte) ((val >> 32) & 0xff),
            (byte) ((val >> 24) & 0xff),
            (byte) ((val >> 16) & 0xff),
            (byte) ((val >> 8) & 0xff),
            (byte) (val & 0xff)};
    }

}
