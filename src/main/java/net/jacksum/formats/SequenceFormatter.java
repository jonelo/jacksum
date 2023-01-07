/*


  Jacksum 3.5.0 - a checksum utility in Java
  Copyright (c) 2001-2023 Dipl.-Inf. (FH) Johann N. Löfflmann,
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
 *
 * @author Johann N. Loefflmann
 */
public class SequenceFormatter {

    public static void resolveEncoding(StringBuilder buf, byte[] bytes, int grouping, Character groupChar, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(buf.toString());
        try {
            while (matcher.find()) {
                GeneralString.replaceAllStrings(buf, matcher.group(1), FingerprintFormatter.encodeBytes(bytes, Encoding.string2Encoding(matcher.group(2)), grouping, groupChar));
            }
        } catch (IllegalArgumentException e) {
            System.err.println(e);
        }
    }
    
}
