/*
 * Jacksum 3.7.0 - a checksum utility in Java
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
 */

package net.jacksum.selectors;

import net.jacksum.HashFunctionFactory;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.algorithms.HMAC;

import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class HMAC_Selector extends Selector {
    @Override
    public boolean doesMatch(String name) {
        return name.startsWith("hmac:") || name.startsWith("hmac-");
    }

    @Override
    public AbstractChecksum getImplementation(boolean alternate) throws NoSuchAlgorithmException {
        String withoutPrefix = name.substring(5);

        Pattern pattern = Pattern.compile(":(\\d+)$");
        Matcher matcher = pattern.matcher(withoutPrefix);

        int requestedOutputLengthInBits = -1;
        if (matcher.find()) {
            requestedOutputLengthInBits = Integer.parseInt(matcher.group(1));
            withoutPrefix = withoutPrefix.substring(0, withoutPrefix.length()-matcher.group(1).length()-1);
        }

        HMAC hmac = new HMAC(withoutPrefix);
        if (requestedOutputLengthInBits > 0) {
            hmac.setOutputLengthInBits(requestedOutputLengthInBits);
        }
        hmac.init(HashFunctionFactory.getKey());
        return hmac;
    }

    @Override
    public AbstractChecksum getPrimaryImplementation() throws NoSuchAlgorithmException {
        return getImplementation(PRIMARY);
    }

    @Override
    public Map<String, String> getAvailableAlgorithms() {
        return null;
    }


}
