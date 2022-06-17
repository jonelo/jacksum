/*


  Jacksum 3.4.0 - a checksum utility in Java
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

public class Fnv1_32 extends Fnv0_32 {  

    protected final int INIT = 0x811c9dc5;

    public Fnv1_32() {
        super();
        bitWidth = 32;
        value = INIT;
    }

    @Override
    public void reset() {
        value = INIT;
        length = 0;
    }

}
