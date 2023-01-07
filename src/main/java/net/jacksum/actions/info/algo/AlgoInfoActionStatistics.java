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
package net.jacksum.actions.info.algo;

import java.util.LinkedHashMap;
import java.util.Map;
import net.jacksum.statistics.Statistics;


public class AlgoInfoActionStatistics extends Statistics {

    /**
     * @return the algorithmCount
     */
    public int getAlgorithmCount() {
        return algorithmCount;
    }

    /**
     * @param algorithmCount the algorithmCount to set
     */
    public void setAlgorithmCount(int algorithmCount) {
        this.algorithmCount = algorithmCount;
    }

    private int algorithmCount;
    
    @Override
    public Map<String, Object> build() {
        Map<String, Object> map = new LinkedHashMap<>();        
        map.put("number of algorithms filtered", getAlgorithmCount());
        return map;    }

    @Override
    public void reset() {
        setAlgorithmCount(0);
    }
    
}
