/*
 * Jacksum 3.7.0 - a checksum utility in Java
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

package net.jacksum.actions.info.hmacs;

import net.jacksum.statistics.Statistics;

import java.util.LinkedHashMap;
import java.util.Map;

public class HMACsActionStatistics extends Statistics {
    private int HMACsCount = 0;

    @Override
    public Map<String, Object> build() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("number of HMACSs", getHMACsCount());
        return map;
    }

    public int getHMACsCount() {
        return HMACsCount;
    }

    public void setHMACsCount(int count) {
        this.HMACsCount = count;
    }

    @Override
    public void reset() {
        HMACsCount = 0;
    }
}
