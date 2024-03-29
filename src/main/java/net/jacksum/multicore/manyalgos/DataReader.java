/*


  Jacksum 3.7.0 - a checksum utility in Java
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
/*

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

 */
package net.jacksum.multicore.manyalgos;

import java.io.*;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import net.jacksum.algorithms.AbstractChecksum;

/**
 * Reads the file and puts its data in several queues for processing by Hashers.
 * There's one queue per processor.
 * 
 * @author Federico Tello Gentile
 */
public class DataReader implements Runnable {

    private final Collection<BlockingQueue<DataUnit>> queues;
    private final File file;
    private long total = 0L;

    public DataReader(File file, Collection<BlockingQueue<DataUnit>> queues) {
        this.queues = queues;
        this.file = file;
    }

    private void enqueue(DataUnit du) throws InterruptedException {
        for (BlockingQueue<DataUnit> queue : this.queues) {
            queue.put(du);
        }
    }

    public long getTotal() {
        return total;
    }

    @Override
    public void run() {
        try (InputStream is = new BufferedInputStream(new FileInputStream(this.file))) {

            DataUnit du = new DataUnit(AbstractChecksum.BUFFERSIZE);
            int read = du.readData(is);
            while (read > 0) {
                total += read;
                enqueue(du);
                du = new DataUnit(AbstractChecksum.BUFFERSIZE);
                read = du.readData(is);
            }
            if (read == -1) {
                // enqueue last one in case the last one is zero bytes
                du = new DataUnit(1);
                du.setLength(0);
                enqueue(du);
            }

        } catch (IOException | InterruptedException ex) {
            // enqueue the "last one marker", otherwise the future.get() method will hang the entire process
            // e.g. while trying to read the NTUSER.DAT on Microsoft Windows
            // (Der Prozess kann nicht auf die Datei zugreifen, da sie von einem anderen Prozess verwendet wird)
            DataUnit du = new DataUnit(1);
            du.setLength(0);
            try {
                enqueue(du);
                total = -1;
                exceptionMessage = ex.getMessage();
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
                e.printStackTrace(System.err);
            }
        }
    }

    private String exceptionMessage;
    public String getExceptionMessage() {
        return exceptionMessage;
    }
}
