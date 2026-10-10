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

    private long matchedLines;
    private long notMatchedLines;

    /**
     * Creates a new HashStringsActionStatistics.
     */
    public HashStringsActionStatistics() {
    }

    @Override
    public Map<String, Object> build() {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("total lines read", getTotalLines());
        map.put("ignored comment lines", getIgnoredLines());
        map.put("ignored empty lines", getEmptyLines());
        map.put("","");
        map.put("total lines hashed", getHashedLines());
        if (getMatchedLines() >= 0 && getNotMatchedLines() >= 0) {
            map.put("total lines matched", getMatchedLines());
            map.put("total lines not matched", getNotMatchedLines());
        }
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
        matchedLines = 0;
        notMatchedLines = 0;
    }

    /**
     * Returns the total number of lines read.
     *
     * @return the total number of lines read
     */
    public long getTotalLines() {
        return totalLines;
    }

    /**
     * Sets the total number of lines read.
     *
     * @param totalLines the totalLines to set
     */
    public void setTotalLines(long totalLines) {
        this.totalLines = totalLines;
    }

    /**
     * Returns the number of lines ignored because they are comments.
     *
     * @return the number of ignored comment lines
     */
    public long getIgnoredLines() {
        return ignoredLines;
    }

    /**
     * Sets the number of lines ignored because they are comments.
     *
     * @param ignoredLines the ignoredLines to set
     */
    public void setIgnoredLines(long ignoredLines) {
        this.ignoredLines = ignoredLines;
    }

    /**
     * Returns the number of lines hashed.
     *
     * @return the number of hashed lines
     */
    public long getHashedLines() {
        return hashedLines;
    }

    /**
     * Sets the number of lines hashed.
     *
     * @param hashedLines the number of hashed lines
     */
    public void setHashedLines(long hashedLines) {
        this.hashedLines = hashedLines;
    }

    /**
     * Returns the number of empty lines ignored.
     *
     * @return the number of ignored empty lines
     */
    public long getEmptyLines() {
        return emptyLines;
    }

    /**
     * Sets the number of empty lines ignored.
     *
     * @param emptyLines the number of ignored empty lines
     */
    public void setEmptyLines(long emptyLines) {
        this.emptyLines = emptyLines;
    }

    /**
     * Returns the number of lines whose hash value matched the expected one.
     *
     * @return the number of matched lines
     */
    public long getMatchedLines() {
        return matchedLines;
    }

    /**
     * Sets the number of lines whose hash value matched the expected one.
     *
     * @param matchedLines the number of matched lines
     */
    public void setMatchedLines(long matchedLines) {
        this.matchedLines = matchedLines;
    }

    /**
     * Returns the number of lines whose hash value did not match the
     * expected one.
     *
     * @return the number of lines that did not match
     */
    public long getNotMatchedLines() {
        return notMatchedLines;
    }

    /**
     * Sets the number of lines whose hash value did not match the
     * expected one.
     *
     * @param notMatchedLines the number of lines that did not match
     */
    public void setNotMatchedLines(long notMatchedLines) {
        this.notMatchedLines = notMatchedLines;
    }
}
