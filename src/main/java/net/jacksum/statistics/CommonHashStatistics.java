/*
 * Jacksum 4.0.2 - a checksum/hash tool written in Java
 * Copyright (c) 2001-2026 Dipl.-Inf. (FH) Johann N. Löfflmann,
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
 */

package net.jacksum.statistics;

import net.loefflmann.sugar.math.GeneralMath;

import java.util.Map;

/**
 * Statistics that are common to all actions that hash files: the number of
 * files and bytes read, and the number of file read errors.
 */
public class CommonHashStatistics extends Statistics {

    /** The number of files that have been read successfully. */
    protected long filesRead;
    /** The number of file read errors. */
    protected long errors;
    /** The number of bytes that have been read. */
    protected long bytesRead;

    /**
     * Creates a new {@code CommonHashStatistics} with all counters set to zero.
     */
    public CommonHashStatistics() {
    }

    @Override
    public Map<String, Object> build() {
        return null;
    }

    @Override
    public void reset() {
        filesRead = 0;
        bytesRead = 0;
        errors = 0;
    }

    /**
     * Adds the common statistics to a map, preceded by an empty entry that
     * acts as a separator.
     *
     * @param map the map to add the statistics to
     */
    public void put(Map<String, Object> map) {
        map.put("", "");
        map.put("total files read successfully", filesRead);
        map.put("total bytes read", bytesRead);
        map.put("total bytes read (human readable)", GeneralMath.formatByteCountHumanReadable(bytesRead, true));
        map.put("total file read errors", errors);
    }


    /**
     * Gets the number of files that have been read successfully.
     *
     * @return the filesRead
     */
    public long getFilesRead() {
        return filesRead;
    }

    /**
     * Sets the number of files that have been read successfully.
     *
     * @param filesRead the filesRead to set
     */
    public void setFilesRead(long filesRead) {
        this.filesRead = filesRead;
    }

    /**
     * Gets the number of bytes that have been read.
     *
     * @return the bytesRead
     */
    public long getBytesRead() {
        return bytesRead;
    }

    /**
     * Sets the number of bytes that have been read.
     *
     * @param bytesRead the bytesRead to set
     */
    public void setBytesRead(long bytesRead) {
        this.bytesRead = bytesRead;
    }

    /**
     * Gets the number of file read errors.
     *
     * @return the errors
     */
    public long getErrors() {
        return errors;
    }

    /**
     * Sets the number of file read errors.
     *
     * @param errors the errors to set
     */
    public void setErrors(long errors) {
        this.errors = errors;
    }
}
