/*


  Jacksum 3.0.0 - a checksum utility in Java
  Copyright (c) 2001-2021 Dipl.-Inf. (FH) Johann N. Löfflmann,
  All Rights Reserved, <https://jacksum.net>.

  This program is free software: you can redistribute it and/or modify it under
  the terms of the GNU General Public License as published by the Free Software
  Foundation, either version 3 of the License, or (at your option) any later
  version.

  This program is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  details.

  You should have received a copy of the GNU General Public License along with
  this program. If not, see <https://www.gnu.org/licenses/>.


 */
package net.jacksum.formats;

import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.parameters.base.FingerprintFormatParameters;
import org.n16n.sugar.encodings.Base32;
import org.n16n.sugar.encodings.BubbleBabble;
import org.n16n.sugar.encodings.ZBase32;
import org.n16n.sugar.util.ByteSequences;
import org.n16n.sugar.util.GeneralString;

import java.math.BigInteger;
import java.util.Base64;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Johann N. Loefflmann
 */
public class FingerprintFormatter implements FingerprintFormatParameters {
    
    private final FingerprintFormatParameters parameters;

    public FingerprintFormatter(FingerprintFormatParameters fingerprintFormatParameters) {
        this.parameters = fingerprintFormatParameters;
    }
    
    public String format(byte[] fingerprint) {
        return encodeBytes(fingerprint, parameters.getEncoding(), parameters.getGrouping(), parameters.getGroupChar());
    }

    public String format(byte[] fingerprint, Encoding encoding) {
        return encodeBytes(fingerprint, encoding, parameters.getGrouping(), parameters.getGroupChar());
    }
    
    /**
     * Replace tokens like "CHECKSUM{<code>number</code>,<code>encoding</code>}" in buf using a regex.
     * @param buf a StringBuilder.
     * @param checksum an AbstractChecksum object.
     * @param regex must define 2 groups, the entire token itself and the encoding, example: <code>"(#CHECKSUM\\{" + i + ",([^}]+)\\})"</code>
     */
    public static void resolveEncoding(StringBuilder buf, AbstractChecksum checksum, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(buf.toString());
        try {
            while (matcher.find()) {
                GeneralString.replaceAllStrings(buf, matcher.group(1), checksum.getValueFormatted(Encoding.string2Encoding(matcher.group(2))));
            }
        } catch (IllegalArgumentException e) {
            System.err.println(e);
        }
    }



    public static void replaceAliases(StringBuilder format) {
        GeneralString.replaceAllStrings(format, "#HASH", "#CHECKSUM");
        GeneralString.replaceAllStrings(format, "#FINGERPRINT", "#CHECKSUM");
        GeneralString.replaceAllStrings(format, "#DIGEST", "#CHECKSUM");
    }
    
    public static String encodeBytes(byte[] bytes, Encoding encoding, int grouping, Character groupChar) {
        
        switch (encoding) {               
            case HEX:
                return ByteSequences.format(bytes, false, grouping, groupChar);
            case HEX_UPPERCASE:
                return ByteSequences.format(bytes, true, grouping, groupChar);
            case BASE16:
                return ByteSequences.format(bytes, true, 0, groupChar);
            case BASE32:
                return new Base32(Base32.Alphabet.BASE32, Base32.Padding.PADDING, Base32.UpperLower.UPPERCASE).encode(bytes);
            case BASE32_NOPADDING:
                return new Base32(Base32.Alphabet.BASE32, Base32.Padding.NO_PADDING, Base32.UpperLower.UPPERCASE).encode(bytes);
            case BASE32HEX:
                return new Base32(Base32.Alphabet.BASE32HEX, Base32.Padding.PADDING, Base32.UpperLower.UPPERCASE).encode(bytes);
            case BASE32HEX_NOPADDING:
                return new Base32(Base32.Alphabet.BASE32HEX, Base32.Padding.NO_PADDING, Base32.UpperLower.UPPERCASE).encode(bytes);
            case BASE64:
                return Base64.getEncoder().encodeToString(bytes);
            case BASE64URL:
                return Base64.getUrlEncoder().encodeToString(bytes);
            case BUBBLEBABBLE:
                return BubbleBabble.encode(bytes);
            case DEC:
                return new BigInteger(1, bytes).toString();
            case DEC_FIXED_SIZE_WITH_LEADING_ZEROS:
                if (bytes.length == 2) { // only supported for 2 bytes (e.g. BSD sum)
                    // put back the byte array to a long
                    int value = ByteSequences.twoByteArrayToInt(bytes);
                    return String.format("%05d", value);  // five, because 2^(2*8) = 65535 which are 5 digits max.
                    //ByteBuffer wrapped = ByteBuffer.wrap(bytes); // big-endian by default
                    //short num = wrapped.getShort(); // 1
                } else throw new UnsupportedOperationException("Encoding not supported for byte arrays of size " + bytes.length);
            case BIN:
                return ByteSequences.formatAsBits(bytes);
            case OCT: {
                BigInteger big = new BigInteger(1, bytes);
                return big.toString(8);
            }
            case ZBASE32: {
                return ZBase32.encodeToString(bytes);
            }
            default:
                return ByteSequences.format(bytes, false, 0, ' ');
        }
    }

    @Override
    public boolean isEncodingSet() {
        return parameters.isEncodingSet();
    }

    @Override
    public Encoding getEncoding() {
        return parameters.getEncoding();
    }

    @Override
    public boolean isGroupingSet() {
        return parameters.isGroupingSet();
    }

    @Override
    public int getGrouping() {
        return parameters.getGrouping();
    }

    @Override
    public boolean isGroupCharSet() {
        return parameters.isGroupCharSet();
    }

    @Override
    public Character getGroupChar() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
