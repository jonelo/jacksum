/*
 * Jacksum 3.6.0 - a checksum utility in Java
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

package net.jacksum.algorithms.checksums;

import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.formats.Encoding;
import net.loefflmann.sugar.util.ByteSequences;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrngHash extends AbstractChecksum implements PrngHashInfo {

    protected long value;

    public long getMultiplier() {
        return multiplier;
    }

    protected long multiplier;

    public long getInitValue() {
        return initValue;
    }

    public long getAdd() {
        return add;
    }

    protected long initValue;
    protected long add;

    private final static long DEFAULT_INIT = 0x811c9dc5L;
    private final static long DEFAULT_ADD = 0L;
    private final static long DEFAULT_MULTIPLIER = 0x01000193L;

    private void sharedInit() {
        bitWidth = 32;
        formatPreferences.setHashEncoding(Encoding.DEC);
        formatPreferences.setFilesizeWanted(true);
        formatPreferences.setSeparator(" ");
    }

    public PrngHash() {
        this(DEFAULT_INIT, DEFAULT_MULTIPLIER, DEFAULT_ADD);
    }

    public PrngHash(long initValue, long multiplier, long add) throws IllegalArgumentException{
        super();
        sharedInit();

        this.initValue = initValue;
        this.multiplier = multiplier;
        this.add = add;
        checkValues();

        value = initValue;
    }

    private void checkValues() throws IllegalArgumentException {
        if ((initValue & 0xFFFFFFFFL) != initValue) throw new IllegalArgumentException("The init value of PRNG Hash is larger than 32 bit.");
        if ((multiplier & 0xFFFFFFFFL) != multiplier) throw new IllegalArgumentException("The multiplier value of PRNG Hash is larger than 32 bit.");
        if ((add & 0xFFFFFFFFL) != add) throw new IllegalArgumentException("The add value of the PRNG hash is larger than 32 bit.");
    }

    private long parse(String input, String id, long defaultValue) throws IllegalArgumentException {
        long value = defaultValue;
        // init as hex
        Pattern pattern = Pattern.compile(id+"\\s*=\\s*0x([\\da-fA-F]+)");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find() && matcher.groupCount() == 1) {
            value = Long.valueOf(matcher.group(1), 16);
        } else {
            // init as decimal
            pattern = Pattern.compile(id+"\\s*=\\s*([\\d]+)");
            matcher = pattern.matcher(input);
            if (matcher.find() && matcher.groupCount() == 1) {
                value = Long.valueOf(matcher.group(1));
            }
        }
        return value;
    }

    /**
     * 32 bit PRNG (pseudo random number generator) hash.
     * @param input a string containing the properties init, mpy, and add
     */
    public PrngHash(String input) throws IllegalArgumentException {
        this();
        this.initValue = parse(input,"init", DEFAULT_INIT);
        this.multiplier = parse(input,"mpy", DEFAULT_MULTIPLIER);
        if (multiplier == DEFAULT_MULTIPLIER) {
            this.multiplier = parse(input,"multiplier", DEFAULT_MULTIPLIER);
        }
        this.add = parse(input,"add", DEFAULT_ADD);
        checkValues();

        name = makeName();
        value = initValue;
    }

    private String makeName() {
        StringBuilder builder = new StringBuilder();
        String initString = "";
        String multiplierString = "";
        String addString = "";

        boolean empty = true;
        if (initValue != DEFAULT_INIT) {
            builder.append("init=");
            if (initValue == 0) {
                initString = "0";
            } else {
                initString = String.format("0x%s", ByteSequences.hexformat(initValue, 8));
            }
            builder.append(initString);
            empty = false;
        }

        if (multiplier != DEFAULT_MULTIPLIER) {
            if (!empty) {
                builder.append(",mpy=");
            } else {
                builder.append("mpy=");
            }
            if (multiplier == 0) {
                multiplierString = "0";
            } else {
                multiplierString = String.format("0x%s", ByteSequences.hexformat(multiplier, 8));
            }
            builder.append(multiplierString);
            empty = false;
        }

        if (add != DEFAULT_ADD) {
            if (!empty) {
                builder.append(",add=");
            } else {
                builder.append("add=");
            }
            addString = String.format("0x%s", ByteSequences.hexformat(add, 8));
            builder.append(addString);
        }
        if (builder.toString().length()==0) {
            return "prng";
        }
        return builder.toString();
    }

    @Override
    public void reset() {
        value = 0;
        length = 0;
    }

    @Override
    public void update(byte[] buffer, int offset, int len) {
        for (int i = 0; i < len; i++) {
            value = (value * multiplier + add + (buffer[offset + i]&0xFF)) & 0xFFFFFFFFL;
        }
        length += len;
    }

    @Override
    public long getValue() {
        return value & 0xFFFFFFFFL;
    }

    @Override
    public byte[] getByteArray() {
        long val = getValue();
        return new byte[]
                {(byte)((val>>>24)&0xff),
                 (byte)((val>>>16)&0xff),
                 (byte)((val>>>8)&0xff),
                 (byte)(val&0xff)};
    }

}
