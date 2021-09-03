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

package net.jacksum.algorithms.checksums;

import java.security.NoSuchAlgorithmException;
import net.jacksum.algorithms.crcs.CrcGeneric;
import net.jacksum.formats.Encoding;

public class CRC16_Minix extends CrcGeneric {

    private final long[] table;

    public CRC16_Minix() throws NoSuchAlgorithmException {
        super(16, 0x1021, 0, false, false, 0);
        table = getTable();
       
        formatPreferences.setHashEncoding(Encoding.DEC_FIXED_SIZE_WITH_LEADING_ZEROS);
        formatPreferences.setSeparator(" ");
        formatPreferences.setSizeWanted(true);
        formatPreferences.setSizeWithPrintfFormatted("%6s"); // right-justified
    }

    /**
     * Updates the checksum with the specified byte Note that there is actual a
     * bug in the Minix crc implementation.See also
     * <a href="https://gforge.cs.vu.nl/gf/project/minix/tracker/?action=TrackerItemEdit&tracker_item_id;=151">here</a> and
     * <a href="https://gforge.cs.vu.nl/tracker/index.php?func=detail&aid;=151&group_id;=42&atid;=245">here</a>.
     * The update macro in Minix's crc implementation computes a wrong index for
     * the table access In order to implement the Minix CRC we need to implement
     * the bug as well!
     *
     * @param bytes byte array
     * @param offset offset in the byte array
     * @param length length
     */
    @Override
    public void update(byte[] bytes, int offset, int length) {
        for (int i = offset; i < length + offset; i++) {
            // in C:
            // #define updcrc(cp, crc) ( crctab[((crc >> 8) & 255) ^ cp] ^ (crc << 8) )
            // in Java:
            // value =  table[ ((int)((crc >>> 8) ^ b) & 0xff)  ]  ^ (crc << 8);

            // we implement the Minix crc bug
            // in C:
            // #define updcrc(cp, crc) ( crctab[(crc >> 8) & 255] ^ (crc << 8) ^ cp)
            //long crc = getValueInternal();
            value = table[((int) (value >>> 8)) & 0xff] ^ (value << 8) ^ bytes[i];
        }
        this.length += length;
    }

}
