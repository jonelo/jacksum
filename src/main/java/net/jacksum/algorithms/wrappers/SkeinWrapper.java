/** 
 *******************************************************************************
 *
 * Jacksum 3.5.0 - a checksum utility in Java
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
 *
 *******************************************************************************
 */

package net.jacksum.algorithms.wrappers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.NoSuchAlgorithmException;
import net.jacksum.formats.Encoding;
import org.bouncycastle.crypto.digests.SkeinDigest;

public class SkeinWrapper extends MDbouncycastle {

    public SkeinWrapper(String input) throws NoSuchAlgorithmException {
        length = 0;
        filename = null;
        formatPreferences.setSeparator(" ");
        formatPreferences.setHashEncoding(Encoding.HEX);
        virgin = true;

        Pattern pattern = null;
        String expr = "^skein-(\\d+)$";

        try {
            pattern = Pattern.compile(expr, Pattern.CASE_INSENSITIVE);
        } catch (Exception e) {
            System.err.println(e.toString() + ": " + expr);
        }

        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            try {
                int bits = initStateSize(Integer.parseInt(matcher.group(1)));
                md = new SkeinDigest(bits, bits);
                bitWidth = bits;
                return;
            } catch (IllegalArgumentException iae) {
                throw new NoSuchAlgorithmException(iae.getMessage());
            }
        }

        expr = "^skein-(\\d+)-(\\d+)$";

        try {
            pattern = Pattern.compile(expr, Pattern.CASE_INSENSITIVE);
        } catch (Exception e) {
            System.err.println(e.toString() + ": " + expr);
        }

        matcher = pattern.matcher(input);

        if (matcher.find()) {
            try {
                int stateSizeBits = initStateSize(Integer.parseInt(matcher.group(1)));
                int digestSizeBits = initDigestSize(Integer.parseInt(matcher.group(2)));
                md = new SkeinDigest(stateSizeBits, digestSizeBits);
                bitWidth = digestSizeBits;
                return;
            } catch (IllegalArgumentException iae) {
                throw new NoSuchAlgorithmException(iae.getMessage());
            }
        }

        throw new NoSuchAlgorithmException(input + " is an invalid description for Skein. Must be in format skein-<256|512|1024>[-<length>]");
    }

    private int initStateSize(int stateSize) {
        switch (stateSize) {
            case 256:
            case 512:
            case 1024:
                //this.stateSizeBits = stateSize;
                return stateSize;
                //break;
            default:
                throw new IllegalArgumentException("stateSize must be one of 256, 512, 1024.");
        }
    }

    private int initDigestSize(int digestSize) throws IllegalArgumentException {
        if ((digestSize > 0) && (digestSize < Integer.MAX_VALUE) && (digestSize % 8 == 0)) {
            return digestSize;
        } else
        throw new IllegalArgumentException("digestSize must be > 0 and a multiple of 8.");
    }

}
