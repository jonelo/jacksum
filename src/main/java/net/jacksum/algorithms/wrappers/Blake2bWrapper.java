/** 
 *******************************************************************************
 *
 * Jacksum 3.3.0 - a checksum utility in Java
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
import net.jacksum.formats.Encoding;
//import net.jacksum.zzadopt.org.bouncycastle.crypto.digests.Blake2bDigest;
import org.bouncycastle.crypto.digests.Blake2bDigest;

public class Blake2bWrapper extends MDbouncycastle {

    public Blake2bWrapper(String input) throws NoSuchAlgorithmException {
        length = 0;
        filename = null;
        formatPreferences.setSeparator(" ");
        formatPreferences.setHashEncoding(Encoding.HEX);
        virgin = true;

        
        Pattern pattern = null;       
        String expr = "^(blake2b|b2sum)$";

        try {
            pattern = Pattern.compile(expr, Pattern.CASE_INSENSITIVE);
        } catch (Exception e) {
            System.err.println(e.toString() + ": " + expr);
        }

        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            try {
                this.bitWidth = 512;
                md = new Blake2bDigest(bitWidth);
                return;
            } catch (IllegalArgumentException iae) {
                throw new NoSuchAlgorithmException(iae.getMessage());
            }
        }        
        
        expr = "^(blake2b-|b2sum-)(\\d+)$";

        try {
            pattern = Pattern.compile(expr, Pattern.CASE_INSENSITIVE);
        } catch (Exception e) {
            System.err.println(e.toString() + ": " + expr);
        }

        matcher = pattern.matcher(input);

        if (matcher.find()) {
            try {
                int bits = Integer.parseInt(matcher.group(2));
                md = new Blake2bDigest(bits);
                bitWidth = bits;
                return;
            } catch (IllegalArgumentException iae) {
                throw new NoSuchAlgorithmException(iae.getMessage());
            }
        }

        throw new NoSuchAlgorithmException(input + " is an invalid id for Blake2. Must be in format blake2b-<n>");
    }

}
