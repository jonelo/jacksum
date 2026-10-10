/*


  Jacksum 4.0.2 - a checksum/hash tool written in Java
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.loefflmann.sugar.util.GeneralString;

/**
 * Replaces sequence tokens in a format buffer by the encoded representation of
 * a byte sequence.
 *
 * @author Johann N. Loefflmann
 */
public class SequenceFormatter {

    /**
     * Creates a new {@code SequenceFormatter}.
     */
    public SequenceFormatter() {
    }

    /**
     * Resolves the encoding of a sequence token without protecting the encoded values.
     *
     * @param buf the buffer whose tokens are replaced in place
     * @param bytes the byte sequence to encode
     * @param grouping the number of characters per group (hex encodings only), or 0 for no grouping
     * @param groupChar the character that separates the groups
     * @param regex the regular expression that matches a token; group 1 must match the whole token
     *        and group 2 the name of the encoding
     */
    public static void resolveEncoding(StringBuilder buf, byte[] bytes, int grouping, Character groupChar, String regex) {
        resolveEncoding(buf, bytes, grouping, groupChar, regex, null);
    }

    /**
     * Resolves the encoding of a sequence token. The encoded sequence is a value, not a
     * token, so it is protected by the store if one is given, see TokenValueStore.
     * A token with an unknown encoding is left unresolved and an error is printed to
     * standard error.
     *
     * @param buf the buffer whose tokens are replaced in place
     * @param bytes the byte sequence to encode
     * @param grouping the number of characters per group (hex encodings only), or 0 for no grouping
     * @param groupChar the character that separates the groups
     * @param regex the regular expression that matches a token; group 1 must match the whole token
     *        and group 2 the name of the encoding
     * @param store the store that protects the encoded values, or null
     */
    public static void resolveEncoding(StringBuilder buf, byte[] bytes, int grouping, Character groupChar, String regex, TokenValueStore store) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(buf.toString());
        while (matcher.find()) {
                // the try block encloses one match only, so an unknown encoding leaves
                // just that token unresolved rather than aborting the whole loop
            try {
                String value = EncodingDecoding.encodeBytes(bytes, Encoding.string2Encoding(matcher.group(2)), grouping, groupChar);
                GeneralString.replaceAllStrings(buf, matcher.group(1), store == null ? value : store.protect(value));
            } catch (IllegalArgumentException e) {
                System.err.printf("Jacksum: Error: %s%n", e.getMessage());
            }
        }
    }
    
}
