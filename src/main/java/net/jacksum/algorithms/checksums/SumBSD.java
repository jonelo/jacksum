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

import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.formats.Encoding;


public class SumBSD extends AbstractChecksum {

    private long value;

    public SumBSD() {
        super();
        bitWidth = 16;
        formatPreferences.setHashEncoding(Encoding.DEC_FIXED_SIZE_WITH_LEADING_ZEROS);
        formatPreferences.setSeparator(" ");
        formatPreferences.setSizeWanted(true);
        formatPreferences.setSizeAsByteBlocks(1024);
        formatPreferences.setSizeWithPrintfFormatted("%5s"); // right-justified
    }

    @Override
    public void reset() {
        value = 0;
    }

    @Override
    public void update(byte[] bytes, int offset, int length) {
        for (int i = offset; i < length + offset; i++) {
            /** implemented from original GNU C source */
            value = (value >> 1) + ((value & 1) << 15);
            value += bytes[i] & 0xFF;
            value &= 0xffff;
        }
        this.length += length;
    }

/*    @Override
    public String toString() {
        long  kb = (length + 1023) / 1024;
        return (
             (formatPreferences.getEncoding() == null) ? GeneralString.decformat(getValue(),"00000") : getValueFormatted() )
             + formatPreferences.getSeparator()
             + GeneralString.right(kb, 5)
             + (isTimestampWanted() ? formatPreferences.getSeparator() + getTimestampFormatted() : "")
             + (isFilenameWanted() ? formatPreferences.getSeparator() + getFilename() : "");
    }*/

    @Override
    public byte[] getByteArray() {
        long val = getValue();
        return new byte[]
        {(byte)((val>>8)&0xff),
         (byte)(val&0xff)};
    }

    @Override
    public long getValue() {
        return value;
    }


}
