/*


  Jacksum 3.2.0 - a checksum utility in Java
  Copyright (c) 2001-2022 Dipl.-Inf. (FH) Johann N. Löfflmann,
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

package net.jacksum.compats.defs;

import net.jacksum.parameters.ParameterException;
import net.jacksum.parameters.Parameters;

public class DefaultCompatibilityProperties {

    public static CompatibilityProperties getDefaultCompatibilityProperties(Parameters parameters) throws ParameterException {
        CompatibilityProperties parserProperties;
        parserProperties = new CompatibilityProperties();

        parserProperties.setCompatName("generated-by-parameters");
        parserProperties.setCompatDescription("Parser generated by parameters");
        parserProperties.setHashAlgorithm(parameters.getAlgorithmIdentifier());

        if (parameters.getEncoding() != null) {
            parserProperties.setHashEncoding(parameters.getEncoding().toString());
        } else {
            throw new ParameterException("Please specify -E <encoding>.");
        }

        parserProperties.setIgnoreEmptyLines(true);
        if (parameters.getCommentChars() != null) {
            parserProperties.setIgnoreLinesStartingWithString(parameters.getCommentChars());
        } else {
            parserProperties.setIgnoreLinesStartingWithString("#");
        }

        // regular expression
        String regexp;
        // example: the dot has a meaning in regex, so escape all dots by replacing all dots with
        // "\\." in the regex which is "\\\\." as a Java String
        // and there are many more regex characters ...
        String separator = parameters.getSeparator();
        if (separator == null) separator = " ";
        separator = separator.replaceAll("\\\\", "\\\\\\\\") // escape the \ at first
                .replaceAll("\\.", "\\\\.")
                .replaceAll("\\^", "\\\\^")
                .replaceAll("\\$", "\\\\$")
                .replaceAll("\\*", "\\\\*")
                .replaceAll("\\+", "\\\\+")
                .replaceAll("\\-", "\\\\-")
                .replaceAll("\\?", "\\\\?")
                .replaceAll("\\(", "\\\\(")
                .replaceAll("\\)", "\\\\)")
                .replaceAll("\\(", "\\\\(")
                .replaceAll("\\[", "\\\\[")
                .replaceAll("\\]", "\\\\]")
                .replaceAll("\\{", "\\\\{")
                .replaceAll("\\}", "\\\\}")
                .replaceAll("\\|", "\\\\|")
                .replaceAll("\\/", "\\\\/");

        String regex1stToken = "^(.+?)";
        String regexStrToken = "(.+?)";
        String regexIntToken = "(\\d+)";
        String regexFilename="[*]*(.*)$";

        // filesize is wanted, because there is a + sign
        if (parameters.getAlgorithmIdentifier().contains("+")) {

            if (parameters.isTimestampWanted()) {
                // hash, filesize, timestamp, and filename
                regexp = regex1stToken + separator +
                        regexIntToken + separator +
                        regexStrToken + separator +
                        regexFilename;
                parserProperties.setRegexHashPos(1);
                parserProperties.setRegexpFilesizePos(2);
                parserProperties.setRegexpTimestampPos(3);
                parserProperties.setRegexpFilenamePos(4);
            } else {
                // hash, filesize, and filename
                regexp = regex1stToken + separator +
                        regexIntToken + separator +
                        regexFilename;
                parserProperties.setRegexHashPos(1);
                parserProperties.setRegexpFilesizePos(2);
                parserProperties.setRegexpFilenamePos(3);
            }

        } else {
            if (parameters.isTimestampWanted()) {
                // hash, timestamp, and filename
                regexp = regex1stToken + separator +
                        regexStrToken + separator +
                        regexFilename;
                parserProperties.setRegexHashPos(1);
                parserProperties.setRegexpTimestampPos(2);
                parserProperties.setRegexpFilenamePos(3);
            } else {
                // hash, and filename
                regexp = regex1stToken + separator +
                        regexFilename;
                parserProperties.setRegexHashPos(1);
                parserProperties.setRegexpFilenamePos(2);
            }
        }

        parserProperties.setRegexp(regexp);
        return parserProperties;
    }
}
