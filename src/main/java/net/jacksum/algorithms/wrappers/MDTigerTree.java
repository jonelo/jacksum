/** 
 *******************************************************************************
 *
 * Jacksum 3.4.0 - a checksum utility in Java
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.formats.Encoding;

/**
 * A wrapper class that can be used to compute TigerTree
 */
public class MDTigerTree extends AbstractChecksum {

    private MessageDigest md = null;
    private boolean virgin = true;
    private byte[] digest = null;

    public MDTigerTree(String arg) throws NoSuchAlgorithmException {
        // value=0; we don't use value, we use md
        length = 0;
        filename = null;
        formatPreferences.setSeparator(" ");
        formatPreferences.setHashEncoding(Encoding.BASE32_NOPADDING);

        virgin = true;
        md = new TigerTree(arg);
        bitWidth = md.getDigestLength() * 8;
    }

    @Override
    public void reset() {
        md.reset();
        length = 0;
        virgin = true;
    }

    @Override
    public void update(byte[] buffer, int offset, int len) {
        md.update(buffer,offset,len);
        length+=len;
    }

    @Override
    public void update(byte b) {
        md.update(b);
        length++;
    }

    @Override
    public void update(int b) {
        update((byte)(b & 0xFF));
    }

    @Override
    public byte[] getByteArray() {
        if (virgin) {
            digest = md.digest();
            virgin = false;
        }
        // we don't expose internal representations
        byte[] save = new byte[digest.length];
        System.arraycopy(digest, 0, save, 0, digest.length);
        return save;
    }

}
