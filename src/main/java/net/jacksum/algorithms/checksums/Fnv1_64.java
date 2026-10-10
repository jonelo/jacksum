/*


  Jacksum 4.0.2 - a checksum/hash tool written in Java
  Copyright (c) 2001-2026 Dipl.-Inf. (FH) Johann N. Löfflmann,
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

/**
 * The 64-bit Fowler-Noll-Vo hash function FNV-1 (with the FNV offset basis as init value).
 */
public class Fnv1_64 extends Fnv0_64 {

    /**
     * The init value, i.e. the 64-bit FNV offset basis.
     */
    protected final long INIT = 0xcbf29ce484222325L;

    /**
     * Creates a new FNV-1 (64 bits) instance.
     */
    public Fnv1_64() {
        super();
        bitWidth = 64;
        value = INIT;
    }

    @Override
    public void reset() {
        value = INIT;
        length = 0;
    }

}
