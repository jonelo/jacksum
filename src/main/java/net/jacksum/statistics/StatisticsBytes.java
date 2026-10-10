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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Statistics that count the total number of bytes read.
 *
 * @author Johann
 */
public class StatisticsBytes extends Statistics {

    private long bytes = 0;

    /**
     * Creates a new {@code StatisticsBytes} with the byte counter set to zero.
     */
    public StatisticsBytes() {
    }
    
    /**
     * Gets the number of bytes that have been read.
     *
     * @return the bytes
     */
    public long getBytes() {
        return bytes;
    }

    /**
     * Sets the number of bytes that have been read.
     *
     * @param bytes the bytes to set
     */
    public void setBytes(long bytes) {
        this.bytes = bytes;
    }

    /**
     * Adds a number of bytes to the counter.
     *
     * @param bytes the number of bytes to add
     */
    public void addBytes(long bytes) {
        this.bytes += bytes;
    }
    
    @Override
    public void reset() {
        bytes = 0;
    }

    
    @Override
    public Map<String, Object> build() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("total bytes read", bytes);
        return map;
    }


}
