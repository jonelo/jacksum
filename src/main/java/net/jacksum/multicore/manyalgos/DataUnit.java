/*


  Jacksum 4.0.1 - a checksum/hash tool written in Java
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
 * @author Johann N. Löfflmann
 */
public class DataUnit {

    private final byte[] bytes;
    private int length;
    private boolean last;

    public DataUnit(int length) {
        this.bytes = new byte[length];
    }

    /**
     * Reads data from the stream until the internal buffer is completely filled
     * or the end of the stream is reached. Relying on a single {@code is.read()}
     * would be incorrect: the {@link InputStream} contract allows a short read
     * that returns fewer bytes than requested without indicating end of stream.
     * A DataUnit is flagged as the last one only when a genuine end of stream
     * (read returns -1) is observed before the buffer could be filled.
     *
     * @param is the stream to read from
     * @return the number of bytes read into this unit (0 at end of stream)
     * @throws IOException if an I/O error occurs
     */
    public int readData(InputStream is) throws IOException {
        int off = 0;
        int read;
        while (off < this.bytes.length && (read = is.read(this.bytes, off, this.bytes.length - off)) != -1) {
            off += read;
        }
        this.length = off;
        this.last = off < this.bytes.length; // buffer not filled => end of stream reached
        return off;
    }

    public boolean isNotLast() {
        return !this.last;
    }

    public void updateMessageDigest(AbstractChecksum md) {
        md.update(this.bytes, 0, this.length);
    }

    /**
     * Marks this unit as the terminating (last) unit carrying no data. Used to
     * inject an end-of-stream marker into the worker queues on the error path.
     */
    public void markAsLast() {
        this.length = 0;
        this.last = true;
    }
}
