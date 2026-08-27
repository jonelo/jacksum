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
 * @author Johann N. Löfflmann
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

    /**
     * Best-effort delivery of the terminating marker to every queue. Unlike
     * {@link #enqueue(DataUnit)} this never aborts halfway through: if delivery
     * to one queue is interrupted it retries so that <em>each</em> Hasher
     * receives the marker and none is left blocked forever on {@code take()}.
     */
    private void enqueueToAll(DataUnit du) {
        boolean interrupted = false;
        for (BlockingQueue<DataUnit> queue : this.queues) {
            boolean delivered = false;
            while (!delivered) {
                try {
                    queue.put(du);
                    delivered = true;
                } catch (InterruptedException e) {
                    interrupted = true; // retry, this queue still needs the marker
                }
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    public long getTotal() {
        return total;
    }

    @Override
    public void run() {
        try (InputStream is = new BufferedInputStream(new FileInputStream(this.file))) {

            // readData() fills a whole buffer (or reaches EOF) and flags the last
            // unit itself, so there is always at least one unit and the final
            // (possibly empty) unit terminates the Hashers.
            DataUnit du = new DataUnit(AbstractChecksum.BUFFERSIZE);
            total += du.readData(is);
            enqueue(du);
            while (du.isNotLast()) {
                du = new DataUnit(AbstractChecksum.BUFFERSIZE);
                total += du.readData(is);
                enqueue(du);
            }

        } catch (Throwable ex) {
            // Publish the failure state BEFORE the terminating marker crosses the
            // queue. The only happens-before edge to the main thread runs through
            // queue.put -> queue.take -> future.get (the DataReader's own Future is
            // joined by ConcurrentHasher too, but ordering the writes first keeps
            // total/exceptionMessage visible regardless).
            total = -1;
            exceptionMessage = ex.getMessage();
            // Always enqueue the "last one marker", otherwise the Hashers would
            // block forever on take() and future.get() would hang the entire
            // process, e.g. while trying to read NTUSER.DAT on Microsoft Windows
            // (Der Prozess kann nicht auf die Datei zugreifen, da sie von einem
            // anderen Prozess verwendet wird).
            DataUnit marker = new DataUnit(1);
            marker.markAsLast();
            enqueueToAll(marker);
        }
    }

    private String exceptionMessage;
    public String getExceptionMessage() {
        return exceptionMessage;
    }
}
