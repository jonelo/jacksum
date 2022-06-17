/*

 Sugar for Java 3.0.0
 Copyright (C) 2001-2022  Dipl.-Inf. (FH) Johann N. Löfflmann,
 All Rights Reserved, https://johann.loefflmann.net

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 @author Johann N. Löfflmann

 */
package net.loefflmann.sugar.io;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;

public class BOM {

    public static final byte[] UTF_8 = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    public static final byte[] UTF_16BE = new byte[]{(byte) 0xFE, (byte) 0xFF};
    public static final byte[] UTF_16LE = new byte[]{(byte) 0xFF, (byte) 0xFE};
    public static final byte[] UTF_32BE = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF};
    public static final byte[] UTF_32LE = new byte[]{(byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x00};
    public static final byte[] GB18030 = new byte[]{(byte) 0x84, (byte) 0x31, (byte) 0x95, (byte) 0x33};

    private static String cutBOM(String line, Charset charset, byte[] BOM) {
        byte[] bytes = line.getBytes(charset);
        if (bytes.length >= BOM.length && Arrays.equals(Arrays.copyOf(bytes, BOM.length), BOM)) {
            return new String(Arrays.copyOfRange(bytes, BOM.length, bytes.length), charset);
        } else {
            return line;
        }
    }

    /**
     * Removes the Byte Order Mark (BOM) from the String if it is there.
     *
     * @param line a String that could contain a BOM.
     * @param charset the charset that determines the actual BOM.
     * @return a String without the BOM.
     */
    public static String cutBOM(String line, Charset charset) {
        String lineWithoutBOM = line;
        if (charset.equals(StandardCharsets.UTF_8)) {
            lineWithoutBOM = cutBOM(line, charset, BOM.UTF_8);
        } else if (charset.equals(StandardCharsets.UTF_16BE)) {
            lineWithoutBOM = cutBOM(line, charset, BOM.UTF_16BE);
        } else if (charset.equals(StandardCharsets.UTF_16LE)) {
            lineWithoutBOM = cutBOM(line, charset, BOM.UTF_16LE);
        } else if (charset.equals(Charset.forName("UTF-32BE"))) {
            lineWithoutBOM = cutBOM(line, charset, BOM.UTF_32BE);
        } else if (charset.equals(Charset.forName("UTF-32LE"))) {
            lineWithoutBOM = cutBOM(line, charset, BOM.UTF_32LE);
        } else if (charset.equals(Charset.forName("GB18030"))) {
            lineWithoutBOM = cutBOM(line, charset, BOM.GB18030);
        }
        return lineWithoutBOM;
    }

    public static byte[] getBOM(String charset) {
        Charset cs = Charset.forName(charset);
        switch (cs.name().toUpperCase(Locale.US)) {
            case "UTF-8": return BOM.UTF_8;
            case "UTF-16BE": return BOM.UTF_16BE;
            case "UTF-16LE": return BOM.UTF_16LE;
            case "UTF-32BE": return BOM.UTF_32BE;
            case "UTF-32LE": return BOM.UTF_32LE;
            case "GB18030": return BOM.GB18030;
            // no default, because only a few charsets define a BOM
        }
        return new byte[]{};
    }

    public static void writeBOM(byte[] bom) {
        System.out.write(bom, 0, bom.length);
    }

}
