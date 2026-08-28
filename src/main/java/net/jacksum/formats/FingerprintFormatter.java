/*


  Jacksum 4.0.1 - a checksum/hash tool written in Java
  Copyright (c) 2001-2026 Dipl.-Inf. (FH) Johann N. Löfflmann,
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
import net.loefflmann.sugar.util.GeneralString;

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
        return EncodingDecoding.encodeBytes(fingerprint, parameters.getEncoding(), parameters.getGrouping(), parameters.getGroupChar());
    }

    public String format(byte[] fingerprint, Encoding encoding) {
        return EncodingDecoding.encodeBytes(fingerprint, encoding, parameters.getGrouping(), parameters.getGroupChar());
    }
    
    /**
     * Replace tokens like "CHECKSUM{<code>number</code>,<code>encoding</code>}" in buf using a regex.
     * @param buf a StringBuilder.
     * @param checksum an AbstractChecksum object.
     * @param regex must define 2 groups, the entire token itself and the encoding, example: <code>"(#CHECKSUM\\{" + i + ",([^}]+)\\})"</code>
     */
    public static void resolveEncoding(StringBuilder buf, AbstractChecksum checksum, String regex) {
        resolveEncoding(buf, checksum, regex, null);
    }

    /**
     * Resolves the encoding of a hash value token. The hash value is a value, not a token,
     * so it is protected by the store if one is given, see TokenValueStore.
     *
     * @param buf the buffer with the format
     * @param checksum the checksum that provides the hash value
     * @param regex the regular expression of the token
     * @param store the store that protects the value, or null in order to insert it directly
     */
    public static void resolveEncoding(StringBuilder buf, AbstractChecksum checksum, String regex, TokenValueStore store) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(buf.toString());
        while (matcher.find()) {
                // the try block encloses one match only, so an unknown encoding leaves
                // just that token unresolved rather than aborting the whole loop
            try {
                String value = checksum.getValueFormatted(Encoding.string2Encoding(matcher.group(2)));
                GeneralString.replaceAllStrings(buf, matcher.group(1), store == null ? value : store.protect(value));
            } catch (IllegalArgumentException e) {
                System.err.printf("Jacksum: Error: %s%n", e.getMessage());
            }
        }
    }

    public static void replaceAliases(StringBuilder format) {
        GeneralString.replaceAllStrings(format, "#HASHES", "#CHECKSUM"); // just in case the user specifies just only one algorithm
        GeneralString.replaceAllStrings(format, "#ALGONAMES", "#ALGONAME"); // just in case the user specifies just only one algorithm
        GeneralString.replaceAllStrings(format, "#HASH", "#CHECKSUM");
        GeneralString.replaceAllStrings(format, "#FINGERPRINT", "#CHECKSUM");
        GeneralString.replaceAllStrings(format, "#DIGEST", "#CHECKSUM");
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
