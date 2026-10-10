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

import net.jacksum.actions.io.verify.ListFilter;


/**
 * Statistics on files that have been checked against a check file: the
 * number of matches, mismatches, new files, missing files, and files with errors.
 */
public class StatisticsOnCheckedFiles extends CommonHashStatistics {

    private long matches;
    private long mismatches;
    private long newFiles;
    private long missingFiles;
    private long filesWithErrors;

    private ListFilter listFilter;

    /**
     * Creates a new StatisticsOnCheckedFiles.
     */
    public StatisticsOnCheckedFiles() {
    }

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
        if (listFilter.isFilterError()) {
           map.put("files with errors (ERROR)", filesWithErrors);
        }
        if (listFilter.isAll()) {
            map.put("strict check", (mismatches + newFiles + missingFiles + filesWithErrors) == 0 ? "PASSED" : "FAILED");
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
        filesWithErrors = 0;
    }

    /**
     * Returns the number of files that could not be verified.
     *
     * @return the number of files that are listed in the check file and that exist, but that
     * could not be verified
     */
    public long getFilesWithErrors() {
        return filesWithErrors;
    }

    /**
     * Sets the number of files that could not be verified.
     *
     * @param filesWithErrors the number of files that could not be verified
     */
    public void setFilesWithErrors(long filesWithErrors) {
        this.filesWithErrors = filesWithErrors;
    }

    /**
     * Returns the number of files that are listed in the check file, but that do not exist.
     *
     * @return the removedFiles
     */
    public long getMissingFiles() {
        return missingFiles;
    }

    /**
     * Sets the number of files that are listed in the check file, but that do not exist.
     *
     * @param missingFiles the removedFiles to set
     */
    public void setMissingFiles(long missingFiles) {
        this.missingFiles = missingFiles;
    }

    /**
     * Returns the number of files that are not listed in the check file.
     *
     * @return the newFiles
     */
    public long getNewFiles() {
        return newFiles;
    }

    /**
     * Sets the number of files that are not listed in the check file.
     *
     * @param newFiles the newFiles to set
     */
    public void setNewFiles(long newFiles) {
        this.newFiles = newFiles;
    }

    /**
     * Returns the number of files whose verification succeeded.
     *
     * @return the okCount
     */
    public long getMatches() {
        return matches;
    }

    /**
     * Sets the number of files whose verification succeeded.
     *
     * @param matches the okCount to set
     */
    public void setMatches(long matches) {
        this.matches = matches;
    }

    /**
     * Returns the number of files whose verification failed.
     *
     * @return the mismatchCount
     */
    public long getMismatches() {
        return mismatches;
    }

    /**
     * Sets the number of files whose verification failed.
     *
     * @param mismatches the mismatchCount to set
     */
    public void setMismatches(long mismatches) {
        this.mismatches = mismatches;
    }
    

    /**
     * Returns the filter that controls which statuses are reported.
     *
     * @return the listFilter
     */
    public ListFilter getListFilter() {
        return listFilter;
    }

    /**
     * Sets the filter that controls which statuses are reported.
     *
     * @param listFilter the listFilter to set
     */
    public void setListFilter(ListFilter listFilter) {
        this.listFilter = listFilter;
    }

}
