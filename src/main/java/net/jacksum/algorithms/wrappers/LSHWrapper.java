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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import net.jacksum.zzadopt.kr.re.nsr.crypto.Hash;
import net.jacksum.zzadopt.kr.re.nsr.crypto.Hash.Algorithm;
import net.jacksum.formats.Encoding;
import net.jacksum.algorithms.AbstractChecksum;

public class LSHWrapper extends AbstractChecksum {

    private Hash lshHash = null;
    private boolean virgin = true;
    private byte[] digest = null;

    private static Map<Integer, Algorithm> map;

    public LSHWrapper(String input) throws NoSuchAlgorithmException {
        length = 0;
        filename = null;
        formatPreferences.setSeparator(" ");
        formatPreferences.setHashEncoding(Encoding.HEX);
        virgin = true;

        Pattern pattern = null;
        String expr = "^lsh-(\\d\\d\\d)-(\\d\\d\\d)$";

        try {
            pattern = Pattern.compile(expr, Pattern.CASE_INSENSITIVE);
        } catch (Exception e) {
            System.err.println(e.toString() + ": " + expr);
        }

        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            if (map == null) {
                map = new HashMap<>(6);
                map.put(256224, Algorithm.LSH256_224);
                map.put(256256, Algorithm.LSH256_256);
                map.put(512224, Algorithm.LSH512_224);
                map.put(512256, Algorithm.LSH512_256);
                map.put(512384, Algorithm.LSH512_384);
                map.put(512512, Algorithm.LSH512_512);
            }
            try {
                int bits = Integer.parseInt(matcher.group(1));
                int width = Integer.parseInt(matcher.group(2));

                Integer key = bits * 1000 + width;

                if (map.containsKey(key)) {
                    lshHash = Hash.getInstance(map.get(key));
                } else {
                    throw new NoSuchAlgorithmException(input + " is an invalid id for LSH.");
                }
                bitWidth = width;
                blocksize = lshHash.getBlockSize();
                return;
            } catch (IllegalArgumentException iae) {
                throw new NoSuchAlgorithmException(iae.getMessage());
            }
        }

        throw new NoSuchAlgorithmException(input + " is an invalid id for LSH. Must be in format lsh-<n>-<m>");
    }

    @Override
    public void reset() {
        lshHash.reset();
        length = 0;
        virgin = true;
    }

    @Override
    public void update(byte[] bytes, int offset, int length) {
        lshHash.update(bytes, offset, length*8); // <--- important! The Korean implementation takes the length in bits and not in bytes!
        this.length += length;
    }

    @Override
    public byte[] getByteArray() {
        if (virgin) {
            digest = lshHash.doFinal();
            virgin = false;
        }
        // we don't expose internal representations
        byte[] save = new byte[digest.length];
        System.arraycopy(digest, 0, save, 0, digest.length);
        return save;
    }
    
}
