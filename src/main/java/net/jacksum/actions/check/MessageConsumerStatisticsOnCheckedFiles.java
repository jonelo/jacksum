/**
 *******************************************************************************
 *
 * Jacksum 3.0.0 - a checksum utility in Java
 * Copyright (c) 2001-2021 Dipl.-Inf. (FH) Johann N. Löfflmann,
 * All Rights Reserved, <https://jacksum.net>.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <https://www.gnu.org/licenses/>.
 *
 *******************************************************************************
 */
package net.jacksum.actions.check;

import java.util.LinkedHashMap;
import java.util.Map;
import net.jacksum.statistics.Statistics;
import org.n16n.sugar.math.GeneralMath;


public class MessageConsumerStatisticsOnCheckedFiles extends Statistics {


    private long matches;
    private long mismatches;
    private long newFiles;
    private long missingFiles;
    private long filesRead;
    private long bytesRead;
    private long errors;
    
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
        map.put("", "");
        map.put("total files read", filesRead);
        map.put("total bytes read", bytesRead);
        map.put("total bytes read (human readable)", GeneralMath.formatByteCountHumanReadable(bytesRead, true));
        map.put("total file read errors", errors);
        return map;
    }

    @Override
    public void reset() {
        matches = 0;
        mismatches = 0;
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
     * @return the bytesRead
     */
    public long getBytesRead() {
        return bytesRead;
    }

    /**
     * @param bytesRead the bytesRead to set
     */
    public void setBytesRead(long bytesRead) {
        this.bytesRead = bytesRead;
    }

    /**
     * @return the filesRead
     */
    public long getFilesRead() {
        return filesRead;
    }

    /**
     * @param filesRead the filesRead to set
     */
    public void setFilesRead(long filesRead) {
        this.filesRead = filesRead;
    }

    /**
     * @return the errors
     */
    public long getErrors() {
        return errors;
    }

    /**
     * @param errors the errors to set
     */
    public void setErrors(long errors) {
        this.errors = errors;
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
