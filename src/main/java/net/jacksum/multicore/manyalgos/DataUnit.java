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
/*

  This program is free software; you can redistribute it and/or modify it under
  the terms of the GNU General Public License as published by the Free Software
  Foundation; either version 2 of the License, or (at your option) any later
  version.

  This program is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  details.

  You should have received a copy of the GNU General Public License along with
  this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  Place - Suite 330, Boston, MA 02111-1307, USA.


 */
package net.jacksum.multicore.manyalgos;

import java.io.IOException;
import java.io.InputStream;
import net.jacksum.algorithms.AbstractChecksum;

/**
 *
 * @author Federico Tello Gentile
 */
public class DataUnit {

    private final byte[] bytes;
    private int length;

    public DataUnit(int length) {
        this.bytes = new byte[length];
    }

    public int readData(InputStream is) throws IOException {
        this.length = is.read(this.bytes);
        return this.length;
    }

    public boolean isNotLast() {
        return this.length == this.bytes.length;
    }

    public void updateMessageDigest(AbstractChecksum md) {
        md.update(this.bytes, 0, this.length);
    }

    public void setLength(int length) {
        this.length = length;
    }
}
