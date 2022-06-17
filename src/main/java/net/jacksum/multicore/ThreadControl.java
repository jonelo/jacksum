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
package net.jacksum.multicore;

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
