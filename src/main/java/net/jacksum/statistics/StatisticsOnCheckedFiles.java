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

import java.util.LinkedHashMap;
import java.util.Map;

import net.jacksum.actions.io.verify.ListFilter;


public class StatisticsOnCheckedFiles extends CommonHashStatistics {

    private long matches;
    private long mismatches;
    private long newFiles;
    private long missingFiles;

    private ListFilter listFilter;

    @Override
    public Map<String, Object> build() {
        Map<String, Object> map = new LinkedHashMap<>();

        if (listFilter.isFilterOk()) {
           map.put("matches (OK)", matches);
        }
        if (listFilter.isFilterFailed()) {
           map.put("mismatches (FAILED)", mismatches);
        }
        if (listFilter.isFilterNew()) {
           map.put("new files (NEW)", newFiles);
        }
        if (listFilter.isFilterMissing()) {
           map.put("missing files (MISSING)", missingFiles);
        }
        super.put(map);
        return map;
    }

    @Override
    public void reset() {
        super.reset();
        matches = 0;
        mismatches = 0;
        newFiles = 0;
        missingFiles = 0;
    }

    /**
     * @return the removedFiles
     */
    public long getMissingFiles() {
        return missingFiles;
    }

    /**
     * @param missingFiles the removedFiles to set
     */
    public void setMissingFiles(long missingFiles) {
        this.missingFiles = missingFiles;
    }

    /**
     * @return the newFiles
     */
    public long getNewFiles() {
        return newFiles;
    }

    /**
     * @param newFiles the newFiles to set
     */
    public void setNewFiles(long newFiles) {
        this.newFiles = newFiles;
    }

    /**
     * @return the okCount
     */
    public long getMatches() {
        return matches;
    }

    /**
     * @param matches the okCount to set
     */
    public void setMatches(long matches) {
        this.matches = matches;
    }

    /**
     * @return the mismatchCount
     */
    public long getMismatches() {
        return mismatches;
    }

    /**
     * @param mismatches the mismatchCount to set
     */
    public void setMismatches(long mismatches) {
        this.mismatches = mismatches;
    }
    

    /**
     * @return the listFilter
     */
    public ListFilter getListFilter() {
        return listFilter;
    }

    /**
     * @param listFilter the listFilter to set
     */
    public void setListFilter(ListFilter listFilter) {
        this.listFilter = listFilter;
    }

}
