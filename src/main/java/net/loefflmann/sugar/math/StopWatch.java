/*

  Sugar for Java 1.6.0
  Copyright (C) 2001-2023  Dipl.-Inf. (FH) Johann N. Löfflmann,
  All Rights Reserved, https://johann.loefflmann.net

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

  @author Johann N. Löfflmann

 */
package net.loefflmann.sugar.math;

public class StopWatch {

    private long begin = 0;
    private long end = 0;

    /**
     * A simple stop watch object. You have to call start() and stop()
     * explicitly.
     */
    public StopWatch() {
    }

    public void start() {
        // http://blogs.sun.com/dholmes/entry/inside_the_hotspot_vm_clocks
        // If you are interested in measuring/calculating elapsed time,
        // then always use System.nanoTime(). On most systems it will give a
        // resolution on the order of microseconds.

        begin = System.nanoTime();
    }

    public void stop() {
        end = System.nanoTime();
    }

    public long getDurationInMs() {
        if (end == 0) {
            stop();
        }
        return (end - begin) / 1000000L;
    }

    public String getDurarionAsString() {
        long ms = getDurationInMs();
        return GeneralMath.duration(ms);
    }

}
