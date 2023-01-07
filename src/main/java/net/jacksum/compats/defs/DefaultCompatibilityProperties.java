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

package net.jacksum.compats.defs;

import net.jacksum.JacksumAPI;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.formats.Encoding;
import net.jacksum.parameters.ParameterException;
import net.jacksum.parameters.Parameters;

public class DefaultCompatibilityProperties {

    public static CompatibilityProperties getDefaultCompatibilityProperties(Parameters parameters) throws ParameterException {
        CompatibilityProperties parserProperties;
        parserProperties = new CompatibilityProperties();

        parserProperties.setCompatName("generated-by-parameters");
        parserProperties.setCompatDescription("Parser generated by parameters");
        parserProperties.setHashAlgorithm(parameters.getAlgorithmIdentifier());

        AbstractChecksum ac = null;
        try {
            ac = JacksumAPI.getInstance(parameters);
            ac.setParameters(parameters);
        } catch (java.security.NoSuchAlgorithmException e) {
            new ParameterException(e.getMessage());
        }

        if (parameters.getEncoding() != null) {
            parserProperties.setHashEncoding(parameters.getEncoding().toString());
        } else {
                Encoding encoding = ac.getFormatPreferences().getEncoding();
                parserProperties.setHashEncoding(encoding.toString());
                parameters.setEncoding(encoding);
//            throw new ParameterException("Please specify -E <encoding>.");
        }

        if (!parameters.isFilesizeWantedSet()) {
            if (ac.getFormatPreferences().isFilesizeWanted()) {
                parameters.setFilesizeWanted(true);
            }
        }

        parserProperties.setIgnoreEmptyLines(true);
        if (parameters.getCommentChars() != null) {
            parserProperties.setIgnoreLinesStartingWithString(parameters.getCommentChars());
        } else {
            parserProperties.setIgnoreLinesStartingWithString("#");
        }

        if (parameters.isGnuEscapingSetByUser()) {
            parserProperties.setGnuEscapingEnabled(parameters.isGnuEscaping());
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

        String regexStart = "^";
        String regexGnuEscToken = "([\\\\]?)";
        String regexStrToken = "(.+?)";
        String regexIntToken = " *(\\d+)";  // matches also right-justified file sizes (e.g. crc16_minix)
        String regexFilename="[*]*(.*)$";

        // filesize is wanted, because there is a + sign, or --filesize on has been set
        if (parameters.getAlgorithmIdentifier().contains("+") || (parameters.isFilesizeWanted())) {

            if (parameters.isTimestampWanted()) {
                // hash, filesize, timestamp, and filename
                regexp = regexStart + regexGnuEscToken +
                        regexStrToken + separator +
                        regexIntToken + separator +
                        regexStrToken + separator +
                        regexFilename;
                parserProperties.setRegexpGnuEscapingPos(1);
                parserProperties.setRegexHashPos(2);
                parserProperties.setRegexpFilesizePos(3);
                parserProperties.setRegexpTimestampPos(4);
                parserProperties.setRegexpFilenamePos(5);
            } else {
                // hash, filesize, and filename
                regexp = regexStart + regexGnuEscToken +
                        regexStrToken + separator +
                        regexIntToken + separator +
                        regexFilename;
                parserProperties.setRegexpGnuEscapingPos(1);
                parserProperties.setRegexHashPos(2);
                parserProperties.setRegexpFilesizePos(3);
                parserProperties.setRegexpFilenamePos(4);
            }

        } else {
            if (parameters.isTimestampWanted()) {
                // hash, timestamp, and filename
                regexp = regexStart + regexGnuEscToken +
                        regexStrToken + separator +
                        regexStrToken + separator +
                        regexFilename;
                parserProperties.setRegexpGnuEscapingPos(1);
                parserProperties.setRegexHashPos(2);
                parserProperties.setRegexpTimestampPos(3);
                parserProperties.setRegexpFilenamePos(4);
            } else {
                // hash, and filename
                regexp = regexStart + regexGnuEscToken +
                        regexStrToken + separator +
                        regexFilename;
                parserProperties.setRegexpGnuEscapingPos(1);
                parserProperties.setRegexHashPos(2);
                parserProperties.setRegexpFilenamePos(3);
            }
        }

        parserProperties.setRegexp(regexp);
        return parserProperties;
    }
}
