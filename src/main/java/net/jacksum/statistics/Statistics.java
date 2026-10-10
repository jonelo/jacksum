/*


  Jacksum 4.0.2 - a checksum/hash tool written in Java
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

package net.jacksum.statistics;

import java.util.Map;

/**
 * The base class for statistics that can be printed to standard error.
 */
public abstract class Statistics {
    
    /**
     * Creates a new Statistics.
     */
    public Statistics() {
    }

    /**
     * Builds the statistics as key value pairs in the order they should be printed. An
     * empty key represents an empty line.
     *
     * @return the statistics
     */
    public abstract Map<String,Object> build();
    
    /**
     * Resets the statistics.
     */
    public abstract void reset();
    
    /**
     * Prints the statistics to standard error.
     */
    public void print() {
        StringBuilder buffer = new StringBuilder();
        print(buffer);
        System.err.print(buffer);
    }

    /**
     * Appends the statistics to a buffer, one line per entry, preceded by an empty line.
     *
     * @param buffer the buffer the statistics are appended to
     */
    public void print(StringBuilder buffer) {
        Map<String,Object> map = build();

        buffer.append(String.format("%n"));
        map.entrySet().forEach(entry -> {
            if (entry.getKey().length() > 0) {
                buffer.append(String.format("Jacksum: %s: %s%n", entry.getKey(), entry.getValue().toString()));
            } else {
                buffer.append(String.format("%n"));
            }
        });

    }
}
