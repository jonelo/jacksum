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

import net.jacksum.JacksumAPI;
import net.jacksum.actions.Action;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.cli.ExitCode;
import net.loefflmann.sugar.util.ExitException;

import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;

public class HMACsAction implements Action {

    private final HMACsActionParameters parameters;
    private HMACsActionStatistics statistics;

    public HMACsAction(HMACsActionParameters parameters) {
        this.parameters = parameters;
        this.statistics = new HMACsActionStatistics();
    }

    @Override
    public int perform() throws ExitException {

        Map<String, String> map = JacksumAPI.getAvailableAlgorithms();
        int hmacs = 0;
        StringBuilder buffer = new StringBuilder();
        Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
        if (parameters.isInfoMode()) {
            buffer.append(String.format("# %-20s     %4s  %3s  %3s  %3s%n", "HMAC id", "l", "B", "L", "T"));
        }
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            AbstractChecksum checksum;
            try {
                checksum = JacksumAPI.getChecksumInstance(entry.getKey());
                if (checksum != null && checksum.getBlockSize() > 0) {
                    if (parameters.isInfoMode()) {
                        int T = checksum.getSize() / 16;
                        int r = checksum.getSize() % 16;
                        if (r > 0) T++;
                        buffer.append(String.format("hmac:%-20s  %4s  %3s  %3s  %3s%n", entry.getKey(), checksum.getSize(), checksum.getBlockSize(), checksum.getSize() / 8, T));
                    } else {
                        buffer.append(String.format("hmac:%s%n", entry.getKey()));
                    }
                    hmacs++;
                }
            } catch (NoSuchAlgorithmException e) {
                // should not happen
                e.printStackTrace();
                throw new RuntimeException("INTERNAL ERROR");
            }
        }
        System.out.print(buffer);

        if (parameters.getVerbose().isSummary()) {
            statistics.setHMACsCount(hmacs);
            statistics.print();
        }

        return ExitCode.OK;
    }
}
