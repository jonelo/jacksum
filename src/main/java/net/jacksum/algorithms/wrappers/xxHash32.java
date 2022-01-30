/** 
 *******************************************************************************
 *
 * Jacksum 3.2.0 - a checksum utility in Java
 * Copyright (c) 2001-2022 Dipl.-Inf. (FH) Johann N. Löfflmann,
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

package net.jacksum.algorithms.wrappers;

import net.jacksum.zzadopt.org.apache.commons.codec.digest.XXHash32;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.formats.Encoding;


public class xxHash32 extends AbstractChecksum {

    public XXHash32 hash;
    
    public xxHash32() {
        hash = new XXHash32();
        bitWidth = 32;
        formatPreferences.setHashEncoding(Encoding.HEX);
        formatPreferences.setSeparator(" ");
    }
    
    @Override
    public void reset() {
        hash.reset();
    }

    @Override
    public void update(byte[] bytes, int offset, int length) {
        hash.update(bytes, offset, length);
        this.length += length;
    }
    

    @Override
    public byte[] getByteArray() {
        long val = hash.getValue();
        return new byte[]
        {(byte)((val>>24)&0xff),
         (byte)((val>>16)&0xff),
         (byte)((val>>8)&0xff),
         (byte)(val&0xff)};
    }
    
}
