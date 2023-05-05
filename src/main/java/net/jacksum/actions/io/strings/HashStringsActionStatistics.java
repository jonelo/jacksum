/*


  Jacksum 3.7.0 - a checksum utility in Java
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
package net.jacksum.actions.io.strings;

import net.jacksum.statistics.StatisticsBytes;
import net.loefflmann.sugar.math.GeneralMath;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The statistics for the Hash Strings Action.
 * @author Johann N. Löfflmann
 */
public class HashStringsActionStatistics extends StatisticsBytes {

    private long totalLines;
    private long ignoredLines;

    private long emptyLines;
    private long hashedLines;

    @Override
    public Map<String, Object> build() {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("total lines read", getTotalLines());
        map.put("total lines hashed", getHashedLines());
        map.put("ignored comment lines", getIgnoredLines());
        map.put("ignored empty lines", getEmptyLines());
        map.put("total bytes hashed", getBytes());
        map.put("total bytes hashed (human readable)", GeneralMath.formatByteCountHumanReadable(getBytes(), true));
        return map;
    }

    @Override
    public void reset() {
        setBytes(0);
        totalLines = 0;
        setHashedLines(0);
        ignoredLines = 0;
    }

    public long getTotalLines() {
        return totalLines;
    }

    /**
     * @param totalLines the totalLines to set
     */
    public void setTotalLines(long totalLines) {
        this.totalLines = totalLines;
    }

    public long getIgnoredLines() {
        return ignoredLines;
    }

    /**
     * @param ignoredLines the ignoredLines to set
     */
    public void setIgnoredLines(long ignoredLines) {
        this.ignoredLines = ignoredLines;
    }

    public long getHashedLines() {
        return hashedLines;
    }

    public void setHashedLines(long hashedLines) {
        this.hashedLines = hashedLines;
    }

    public long getEmptyLines() {
        return emptyLines;
    }

    public void setEmptyLines(long emptyLines) {
        this.emptyLines = emptyLines;
    }
}
