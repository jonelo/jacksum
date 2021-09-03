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

package net.jacksum.algorithms.crcs;

import java.security.NoSuchAlgorithmException;


public class CRC32_PHP  extends CrcGeneric {
    
    public CRC32_PHP() throws NoSuchAlgorithmException{
       super (32, 0x04C11DB7, 0xFFFFFFFFL, false, false, 0xFFFFFFFFL);
    }
    
    @Override
    public byte[] getByteArray() {
        byte[] bytes = super.getByteArray();
        CrcGeneric.reverse(bytes);
        return bytes;
    }
}
