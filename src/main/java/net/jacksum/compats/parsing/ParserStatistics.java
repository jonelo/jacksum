/*


  Jacksum 4.0.1 - a checksum/hash tool written in Java
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
package net.jacksum.compats.parsing;

import java.util.LinkedHashMap;
import java.util.Map;
import net.jacksum.statistics.Statistics;

public class ParserStatistics extends Statistics {

    
    private int totalLines;
    private int properlyFormattedLines;
    private int improperlyFormattedLines;
    private int ignoredLines;
    private int duplicateEntries;
    // duplicates are only counted if they are replaced at all, see Parser.isReplaceDuplicateFilenames();
    // a wanted list (--wanted-list) keeps duplicates on purpose, so they are not reported there
    private boolean duplicateEntriesCounted;
    // the same parser reads a check file (-c) and a wanted list (-w), so the labels have to name
    // the list that has actually been read, see also setListNoun()
    private final static String DEFAULT_LIST_NOUN = "check file";
    private String listNoun = DEFAULT_LIST_NOUN;

    @Override
    public Map<String, Object> build() {
        Map<String, Object> map = new LinkedHashMap<>();

        // if there is not even one valid or invalid entry in the file (and maybe some ignored lines), it is still
        // considered to be a correct file
        double percent = (getProperlyFormattedLines()+getImproperlyFormattedLines() == 0) ?
                100 : getProperlyFormattedLines() * 100.0 / (getProperlyFormattedLines()+getImproperlyFormattedLines());
        map.put(String.format("total lines in %s", listNoun), getTotalLines());
        map.put(String.format("improperly formatted lines in %s", listNoun), getImproperlyFormattedLines());
        map.put(String.format("properly formatted lines in %s", listNoun), getProperlyFormattedLines());
        if (isDuplicateEntriesCounted()) {
            map.put(String.format("duplicate entries in %s", listNoun), getDuplicateEntries());
        }
        map.put("ignored lines (empty lines and comments)", getIgnoredLines());
        map.put(String.format("correctness of %s", listNoun), String.format("%.2f %%", percent).replace(',', '.'));
        return map;
    }


    @Override
    public void reset() {
        totalLines = 0;
        properlyFormattedLines = 0;
        improperlyFormattedLines = 0;
        ignoredLines = 0;
        duplicateEntries = 0;
        duplicateEntriesCounted = false;
        listNoun = DEFAULT_LIST_NOUN;
    }

    /**
     * Sets the noun that the labels use for the list that has been read, e.g. "wanted list".
     * The default is "check file".
     *
     * @param listNoun the noun for the list that has been read
     */
    public void setListNoun(String listNoun) {
        this.listNoun = listNoun;
    }

    /**
     * @return the noun that the labels use for the list that has been read
     */
    public String getListNoun() {
        return listNoun;
    }

    /**
     * @return the number of entries that have been replaced by a later entry, because both
     * entries refer to the same file
     */
    public int getDuplicateEntries() {
        return duplicateEntries;
    }

    /**
     * @param duplicateEntries the number of duplicate entries to set
     */
    public void setDuplicateEntries(int duplicateEntries) {
        this.duplicateEntries = duplicateEntries;
    }

    /**
     * @return true if duplicate entries are counted at all, see also the option --wanted-list
     */
    public boolean isDuplicateEntriesCounted() {
        return duplicateEntriesCounted;
    }

    /**
     * @param duplicateEntriesCounted whether duplicate entries are counted at all
     */
    public void setDuplicateEntriesCounted(boolean duplicateEntriesCounted) {
        this.duplicateEntriesCounted = duplicateEntriesCounted;
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
