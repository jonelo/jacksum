/*


  Jacksum 3.5.0 - a checksum utility in Java
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

public class Sum12 extends Sum8 {

    public Sum12() {
        super();
        bitWidth = 12;
        value = 0;
    }
    
    @Override
    public long getValue() {
        return value % 0x1000; // 2^12
    }
    
    @Override
    public byte[] getByteArray() {
        long val = getValue();
        return new byte[]
        {(byte)((val>>8)&0x0f),
         (byte)(val&0xff)};
    }

}

