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

package net.jacksum.algorithms.wrappers;

import java.security.NoSuchAlgorithmException;
import net.jacksum.zzadopt.gnu.crypto.hash.HashFactory;
import net.jacksum.zzadopt.gnu.crypto.hash.IMessageDigest;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.formats.Encoding;


/**
 * A wrapper class that can be used to compute HAVAL, MD2, MD4, MD5, RIPEMD128, RIPEMD160,
 * SHA1, SHA256, SHA384, SHA512, TIGER, WHIRLPOOL-1 (provided by GNU Crypto) and
 * SHA0, SHA224, TIGER128, TIGER160, TIGER2, WHIRLPOOL-0 and WHIRLPOOL (provided by jonelo).
 */
public class MDgnu extends AbstractChecksum {

    private IMessageDigest md = null;
    private boolean virgin = true;
    private byte[] digest = null;

    /** Creates new MDgnu
     * @param arg the name of the algorithm.
     * @throws java.security.NoSuchAlgorithmException if there is no such algorithm.
     */
    public MDgnu(String arg) throws NoSuchAlgorithmException {
        length = 0;
        filename = null;
        formatPreferences.setSeparator(" ");
        formatPreferences.setHashEncoding(Encoding.HEX);

        md = HashFactory.getInstance(arg);
        if (md == null) throw new NoSuchAlgorithmException(arg + " is an unknown algorithm.");
        bitWidth = md.hashSize() * 8;
        virgin = true;
    }


    @Override
    public int getBlockSize() {
       return md.blockSize();
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
        length += len;
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
