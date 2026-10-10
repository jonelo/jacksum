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

package net.jacksum.algorithms.crcs;

import net.jacksum.formats.Encoding;
import java.security.NoSuchAlgorithmException;

/**
 * A class that can be used to compute the CRC-64/NVME of a data stream.
 */
public class CRC64_NVME extends CrcGeneric {
    /**
     * Creates a new CRC-64/NVME checksum object.
     *
     * @throws NoSuchAlgorithmException if the underlying CRC parameters are invalid
     */
    public CRC64_NVME() throws NoSuchAlgorithmException {
        super(64, 0xad93d23594c93659L, 0xffffffffffffffffL, true, true, 0xffffffffffffffffL);
        formatPreferences.setHashEncoding(Encoding.HEX);
        formatPreferences.setFilesizeWanted(false);
    }
}
