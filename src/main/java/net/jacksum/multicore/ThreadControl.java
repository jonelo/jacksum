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
    private static final int THREADS_MAX = Runtime.getRuntime().availableProcessors();
    private static int threadsHashing = THREADS_MAX;
    private static int threadsReading = 1; // OSControl.isMacOS() ? THREADS_MAX : 1;

    public static int getThreadsMax() {
        return THREADS_MAX;
    }

    public static int getThreadsHashing() {
        return threadsHashing;
    }

    public static void setThreadsHashing(int threadsHashing) {
        ThreadControl.threadsHashing = threadsHashing;
    }

    public static int getThreadsReading() {
        return threadsReading;
    }

    public static void setThreadsReading(int threadsReading) {
        ThreadControl.threadsReading = threadsReading;
    }
}
