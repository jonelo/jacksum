/** 
 *******************************************************************************
 *
 * Jacksum 3.5.0 - a checksum utility in Java
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

import java.security.NoSuchAlgorithmException;
import net.jacksum.zzadopt.fr.cryptohash.Digest;
import net.jacksum.zzadopt.fr.cryptohash.ext.HashFactory;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.formats.Encoding;

/**
 * A wrapper class that can be used to compute digests from cryptohash.fr.
 */
public class MDCryptohashSphlib3 extends AbstractChecksum {

    private Digest md = null;
    private boolean virgin = true;
    private byte[] digest = null;

    /**
     * Creates new MDCryptohashFr
     *
     * @param arg the arg.
     * @throws java.security.NoSuchAlgorithmException in case the algorithm is not supported.
     */
    public MDCryptohashSphlib3(String arg) throws NoSuchAlgorithmException {
        length = 0;
        filename = null;
        formatPreferences.setSeparator(" ");
        formatPreferences.setHashEncoding(Encoding.HEX);

        md = HashFactory.getInstance(arg);
        if (md == null) {
            throw new NoSuchAlgorithmException(arg + " is an unknown algorithm.");
        }
        bitWidth = md.getDigestLength() * 8;
        virgin = true;
    }


    @Override
    public int getBlockSize() {
        return md.getBlockLength();
    }

    @Override
    public void reset() {
        md.reset();
        length = 0;
        virgin = true;
    }

    @Override
    public void update(byte[] buffer, int offset, int len) {
        md.update(buffer, offset, len);
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
