/*


  Jacksum 3.6.0 - a checksum utility in Java
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

import java.util.Map;

public abstract class Statistics {
    
    public abstract Map<String,Object> build();
    
    public abstract void reset();
    
    public void print() {
        StringBuilder buffer = new StringBuilder();
        print(buffer);
        System.err.print(buffer);
    }

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
