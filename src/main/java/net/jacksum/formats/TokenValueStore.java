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

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps the values that have been substituted into a format, so that a value cannot be
 * mistaken for a token by the substitutions that follow it.
 *
 * The tokens of a format (see option -F) are resolved step by step, and the order of those
 * steps is load bearing: #HASHES has to be aliased before #HASH, a specific encoding
 * pattern has to be resolved before the generic one, and a bare #CHECKSUM resp. #ALGONAME
 * has to be resolved last. A value must not take part in the steps that follow it though,
 * otherwise a file name such as "#QUOTE" would be replaced rather than being printed.
 *
 * Instead of writing a value into the buffer directly, protect() stores it and returns a
 * placeholder that no token can look like. Once every step is done, resolve() replaces all
 * placeholders by the values they stand for, in a single pass, so a value is never scanned
 * again. The order of the steps stays exactly as it was.
 *
 * This class is an implementation detail of the formatter. It is public only because
 * AbstractChecksum.preFormat() lives in a different package and has to pass the store on.
 */
public class TokenValueStore {

    // A placeholder consists of this character, the index of the value, and this character
    // again. U+0000 cannot occur in a format that has been passed on the command line, in a
    // file name (neither POSIX nor Microsoft Windows allow it), in an encoded hash value, or
    // in a formatted timestamp, so a value can never contain a placeholder.
    private static final char DELIMITER = '\0';

    private final List<String> values = new ArrayList<>();

    /**
     * Removes the placeholder delimiter from a format, so that the format itself cannot
     * forge a placeholder.
     *
     * @param format a format, may be null
     * @return the format without any placeholder delimiter
     */
    public static String sanitize(String format) {
        if (format == null || format.indexOf(DELIMITER) < 0) {
            return format;
        }
        return format.replace(String.valueOf(DELIMITER), "");
    }

    /**
     * Stores a value and returns the placeholder that stands for it.
     *
     * @param value the value, null is treated like an empty String
     * @return the placeholder that has to be written into the buffer instead of the value
     */
    public String protect(String value) {
        values.add(value == null ? "" : value);
        return DELIMITER + Integer.toString(values.size() - 1) + DELIMITER;
    }

    /**
     * Replaces all placeholders in the buffer by the values they stand for. The buffer is
     * processed in one pass from left to right, so a value that has been inserted is never
     * looked at again.
     *
     * @param buffer the buffer with the placeholders
     */
    public void resolve(StringBuilder buffer) {
        if (values.isEmpty()) {
            return;
        }
        StringBuilder result = new StringBuilder(buffer.length());
        int pos = 0;
        while (pos < buffer.length()) {
            char c = buffer.charAt(pos);
            if (c == DELIMITER) {
                int end = buffer.indexOf(String.valueOf(DELIMITER), pos + 1);
                if (end > pos) {
                    int index = parseIndex(buffer, pos + 1, end);
                    if (index >= 0 && index < values.size()) {
                        result.append(values.get(index));
                        pos = end + 1;
                        continue;
                    }
                }
            }
            result.append(c);
            pos++;
        }
        buffer.setLength(0);
        buffer.append(result);
    }

    /**
     * Returns the index that is stored between two delimiters, or -1 if there is none.
     */
    private static int parseIndex(StringBuilder buffer, int from, int to) {
        if (from >= to) {
            return -1;
        }
        int index = 0;
        for (int i = from; i < to; i++) {
            char c = buffer.charAt(i);
            if (c < '0' || c > '9') {
                return -1;
            }
            index = index * 10 + (c - '0');
        }
        return index;
    }
}
