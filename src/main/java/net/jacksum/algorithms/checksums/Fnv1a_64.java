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

package net.jacksum.algorithms.checksums;

public class Fnv1a_64 extends Fnv1_64 {

    public Fnv1a_64() {
        super();
        bitWidth = 64;
    }

    @Override
    public void update(byte[] bytes, int offset, int length) {
        for (int i = offset; i < length + offset; i++) {
            value ^= (bytes[i] & 0xFF);
            // value = value * PRIME; // won't work for 64 bit
            // therefore we use bit shift to the left, because
            // x << k == x*2^k
            // PRIME = 10000000000000000000000000000000110110011
            value += (value << 1) + (value << 4) + (value << 5) +
                    (value << 7) + (value << 8) + (value << 40);
        }
        this.length += length;
    }

}