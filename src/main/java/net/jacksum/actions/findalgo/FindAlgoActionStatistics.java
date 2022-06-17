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
package net.jacksum.actions.findalgo;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;
import net.jacksum.statistics.Statistics;


public class FindAlgoActionStatistics extends Statistics {

    
    private int found = 0;
    private BigInteger searched = BigInteger.ZERO;
    
    
    @Override
    public Map<String, Object> build() {
        Map<String, Object> map = new LinkedHashMap<>();        
        map.put("algorithms tested", searched);
        map.put("algorithms found", found);
        return map;    }

    @Override
    public void reset() {
       found = 0;
       searched = BigInteger.ZERO;
    }

    /**
     * @return the found
     */
    public int getFound() {
        return found;
    }

    /**
     * @param found the found to set
     */
    public void setFound(int found) {
        this.found = found;
    }

    /**
     * @return the searched
     */
    public BigInteger getSearched() {
        return searched;
    }

    /**
     * @param searched the searched to set
     */
    public void setSearched(BigInteger searched) {
        this.searched = searched;
    }

}
