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
package net.jacksum.statistics;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Johann
 */
public class StatisticsBytes extends Statistics {

    private long bytes = 0;
    
    /**
     * @return the bytes
     */
    public long getBytes() {
        return bytes;
    }

    /**
     * @param bytes the bytes to set
     */
    public void setBytes(long bytes) {
        this.bytes = bytes;
    }

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
