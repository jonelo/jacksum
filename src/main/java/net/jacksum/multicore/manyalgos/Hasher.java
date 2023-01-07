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

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import net.jacksum.algorithms.AbstractChecksum;

/**
 * Updates a set of AbstractChecksums. Reads the data from a queue.
 *
 * @author Federico Tello Gentile
 */
public class Hasher implements Runnable {

    private final List<AbstractChecksum> digests;
    private final BlockingQueue<DataUnit> queue;
    private int weight = 0;

    public Hasher(BlockingQueue<DataUnit> queue) {
        this.queue = queue;
        this.digests = new ArrayList<>();
    }

    public int getWeight() {
        return weight;
    }

    public void addMessageDigest(HashAlgorithm hash) throws NoSuchAlgorithmException {
        // System.out.println("---> "+hash.getName()+" "+hash.getWeight());
        this.weight += hash.getWeight();
        this.digests.add(hash.getChecksum());
    }

    @Override
    public void run() {
        try {
            DataUnit du;
            do {
                du = this.queue.take();
                for (AbstractChecksum md : this.digests) {
                    du.updateMessageDigest(md);
                }
            } while (du.isNotLast());
        } catch (InterruptedException iEx) {
            System.err.println(iEx.getMessage());
            iEx.printStackTrace(System.err);
        }
    }
}
