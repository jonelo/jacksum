/*
 * Jacksum 3.6.0 - a checksum utility in Java
 * Copyright (c) 2001-2023 Dipl.-Inf. (FH) Johann N. Löfflmann,
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

public class CommonHashStatistics extends Statistics {

    protected long filesRead;
    protected long errors;
    protected long bytesRead;

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

    public void put(Map<String, Object> map) {
        map.put("", "");
        map.put("total files read successfully", filesRead);
        map.put("total bytes read", bytesRead);
        map.put("total bytes read (human readable)", GeneralMath.formatByteCountHumanReadable(bytesRead, true));
        map.put("total file read errors", errors);
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
}
