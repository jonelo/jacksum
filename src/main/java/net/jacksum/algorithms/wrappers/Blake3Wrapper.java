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

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.jacksum.zzadopt.io.github.rctcwyvrn.blake3.Blake3;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.formats.Encoding;

public class Blake3Wrapper extends AbstractChecksum {

    private Blake3 hasher = null;
    private boolean virgin = true;
    private byte[] digest = null;

    public Blake3Wrapper(String input) throws NoSuchAlgorithmException {

        length = 0;
        filename = null;
        formatPreferences.setSeparator(" ");
        formatPreferences.setHashEncoding(Encoding.HEX);
        virgin = true;

        Pattern pattern = null;
        String expr = "^(blake3|b3sum)$";

        try {
            pattern = Pattern.compile(expr, Pattern.CASE_INSENSITIVE);
        } catch (Exception e) {
            System.err.println(e.toString() + ": " + expr);
        }

        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            try {
                this.bitWidth = 256;
                hasher = Blake3.newInstance();
                return;
            } catch (IllegalArgumentException iae) {
                throw new NoSuchAlgorithmException(iae.getMessage());
            }
        }

        throw new NoSuchAlgorithmException(input + " is an invalid id for Blake3.");
    }

    @Override
    public void reset() {
        hasher.reset();
        length = 0;
        virgin = true;
    }

    @Override
    public void update(byte[] bytes, int offset, int length) {
        hasher.update(Arrays.copyOfRange(bytes, offset, length));
        this.length += length;
    }

    @Override
    public byte[] getByteArray() {
        if (virgin) {
            digest = hasher.digest();
            virgin = false;
        }
        // we don't expose internal representations
        byte[] save = new byte[digest.length];
        System.arraycopy(digest, 0, save, 0, digest.length);
        return save;
    }

}
