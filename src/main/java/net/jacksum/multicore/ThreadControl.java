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
package net.jacksum.multicore;

/**
 * The process wide defaults for the number of threads.
 *
 * <p>The values are the starting point for a new
 * {@link net.jacksum.parameters.Parameters} object only. A run is described by its
 * Parameters object, and that object is the authority for the run: it does not write
 * its values back here, so a run cannot change the defaults of a later run in the same
 * JVM. A program that embeds Jacksum can set the defaults for all of its runs here,
 * before it creates its first Parameters object.</p>
 */
public class ThreadControl {
    private static final int THREADS_MIN = 1;
    private static final int THREADS_LIMIT = 65536;
    private static final int THREADS_MAX = Runtime.getRuntime().availableProcessors();
    private static int threadsHashing = THREADS_MAX;
    private static int threadsReading = 1; // OSControl.isMacOS() ? THREADS_MAX : 1;

    /**
     * Returns the smallest number of threads that is supported.
     *
     * @return the smallest number of threads that is supported
     */
    public static int getThreadsMin() {
        return THREADS_MIN;
    }

    /**
     * Returns the largest number of threads that is supported. More threads than
     * that cannot improve the throughput on any machine, but they can exhaust the
     * memory of the JVM, so a larger value is rejected rather than honored.
     *
     * @return the largest number of threads that is supported
     */
    public static int getThreadsLimit() {
        return THREADS_LIMIT;
    }

    public static int getThreadsMax() {
        return THREADS_MAX;
    }

    public static int getThreadsHashing() {
        return threadsHashing;
    }

    /**
     * Sets the default number of hashing threads for all Parameters objects that
     * are created after this call.
     *
     * @param threadsHashing the number of threads
     * @throws IllegalArgumentException if the value is not in the range that
     * {@link #getThreadsMin()} and {@link #getThreadsLimit()} describe
     */
    public static void setThreadsHashing(int threadsHashing) {
        checkRange(threadsHashing);
        ThreadControl.threadsHashing = threadsHashing;
    }

    public static int getThreadsReading() {
        return threadsReading;
    }

    /**
     * Sets the default number of reading threads for all Parameters objects that
     * are created after this call.
     *
     * @param threadsReading the number of threads
     * @throws IllegalArgumentException if the value is not in the range that
     * {@link #getThreadsMin()} and {@link #getThreadsLimit()} describe
     */
    public static void setThreadsReading(int threadsReading) {
        checkRange(threadsReading);
        ThreadControl.threadsReading = threadsReading;
    }

    /**
     * Checks whether a number of threads is supported. A value outside the range
     * would not just be pointless, it would stop the engine from working at all,
     * see net.jacksum.multicore.manyfiles.MessageWorker.
     *
     * @param threads the number of threads
     * @throws IllegalArgumentException if the value is not in the range that
     * {@link #getThreadsMin()} and {@link #getThreadsLimit()} describe
     */
    public static void checkRange(int threads) {
        if (threads < THREADS_MIN || threads > THREADS_LIMIT) {
            throw new IllegalArgumentException(
                    String.format("The number of threads has to be a value between %d and %d, but it is %d.",
                            THREADS_MIN, THREADS_LIMIT, threads));
        }
    }
}
