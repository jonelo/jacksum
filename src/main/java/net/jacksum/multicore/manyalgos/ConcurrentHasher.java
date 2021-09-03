/** 
 *******************************************************************************
 *
 * Jacksum 3.0.0 - a checksum utility in Java
 * Copyright (c) 2001-2021 Dipl.-Inf. (FH) Johann N. Löfflmann,
 * All Rights Reserved, <https://jacksum.net>.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <https://www.gnu.org/licenses/>.
 *
 *******************************************************************************
 */

/**
 * ****************************************************************************
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 ****************************************************************************
 */
package net.jacksum.multicore.manyalgos;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * Sets up the concurrent execution and distributes hash algorithms among
 * Hashers based on their weight.
 *
 * @author Federico Tello Gentile
 */
public class ConcurrentHasher {

    private static final int QUEUE_CAPACITY = 1024;
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    private static Hasher minWeight(Hasher[] hashers) {
        Hasher answer = hashers[0];
        for (Hasher h : hashers) {
            if (h.getWeight() < answer.getWeight()) {
                answer = h;
            }
        }
        return answer;
    }

    public void updateHashes(File src, List<HashAlgorithm> hashes) {
        try {

            final int workingThreads = Math.max(1, Math.min(THREAD_COUNT, hashes.size()));

            // One queue per worker
            final List<BlockingQueue<DataUnit>> queues =
                    new ArrayList<>(workingThreads);

            final List<Runnable> tasks = new ArrayList<>(workingThreads);

            // One worker per processor
            final Hasher[] workers = new Hasher[workingThreads];

            // create queues and workers
            for (int i = 0; i < workingThreads; i++) {
                BlockingQueue<DataUnit> queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY, true);
                queues.add(queue);
                workers[i] = new Hasher(queue);
                tasks.add(workers[i]);
            }

            // LPT-Algorithm (Longest Processing Time)
            // https://en.wikipedia.org/wiki/Multiprocessor_scheduling
            if (THREAD_COUNT > 1) {
                Collections.sort(hashes);
            }
            for (HashAlgorithm hash : hashes) {
                minWeight(workers).addMessageDigest(hash);
            }

            final ExecutorService pool = Executors.newFixedThreadPool(workers.length + 1);
            pool.submit(new DataReader(src, queues));

            List<Future<?>> futures = new ArrayList<>(tasks.size());

            for (Runnable task : tasks) {
                futures.add(pool.submit(task));
            }
            for (Future<?> f : futures) {
                f.get();
            }
            pool.shutdown();

        } catch (InterruptedException | NoSuchAlgorithmException | ExecutionException ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }
}
