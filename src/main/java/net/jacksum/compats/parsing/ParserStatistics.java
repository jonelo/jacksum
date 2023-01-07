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
package net.jacksum.compats.parsing;

import java.util.LinkedHashMap;
import java.util.Map;
import net.jacksum.statistics.Statistics;

public class ParserStatistics extends Statistics {

    
    private int totalLines;
    private int properlyFormattedLines;
    private int improperlyFormattedLines;
    private int ignoredLines;

    @Override
    public Map<String, Object> build() {
        Map<String, Object> map = new LinkedHashMap<>();

        // if there is not even one valid or invalid entry in the file (and maybe some ignored lines), it is still
        // considered to be a correct file
        double percent = (getProperlyFormattedLines()+getImproperlyFormattedLines() == 0) ?
                100 : getProperlyFormattedLines() * 100.0 / (getProperlyFormattedLines()+getImproperlyFormattedLines());
        map.put("total lines in check file", getTotalLines());
        map.put("improperly formatted lines in check file", getImproperlyFormattedLines());
        map.put("properly formatted lines in check file", getProperlyFormattedLines());
        map.put("ignored lines (empty lines and comments)", getIgnoredLines());
        map.put("correctness of check file", String.format("%.2f %%", percent).replace(',', '.'));
        return map;
    }


    @Override
    public void reset() {
        totalLines = 0;
        improperlyFormattedLines = 0;
    }
    
    /**
     * @return the totalLines
     */
    public int getTotalLines() {
        return totalLines;
    }

    /**
     * @param totalLines the totalLines to set
     */
    public void setTotalLines(int totalLines) {
        this.totalLines = totalLines;
    }

    /**
     * @return the improperlyFormattedLines
     */
    public int getImproperlyFormattedLines() {
        return improperlyFormattedLines;
    }

    /**
     * @param improperlyFormattedLines the improperlyFormattedLines to set
     */
    public void setImproperlyFormattedLines(int improperlyFormattedLines) {
        this.improperlyFormattedLines = improperlyFormattedLines;
    }

    /**
     * @return the properlyFormattedLines
     */
    public int getProperlyFormattedLines() {
        return properlyFormattedLines;
    }

    /**
     * @param properlyFormattedLines the properlyFormattedLines to set
     */
    public void setProperlyFormattedLines(int properlyFormattedLines) {
        this.properlyFormattedLines = properlyFormattedLines;
    }

    /**
     * @return the ignoredLines
     */
    public int getIgnoredLines() {
        return ignoredLines;
    }

    /**
     * @param ignoredLines the ignoredLines to set
     */
    public void setIgnoredLines(int ignoredLines) {
        this.ignoredLines = ignoredLines;
    }
}
