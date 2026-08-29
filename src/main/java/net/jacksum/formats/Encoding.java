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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The supported encodings.
 */
public enum Encoding {

    BIN("bin", "Binary"),
    DEC("dec", "Decimal"),
    OCT("oct", "Octal"),
    HEX("hex", "Hexadecimal (lowercase)"),
    HEX_UPPERCASE("hex-uppercase", "Hexadecimal (uppercase)"),
    BASE16("base16", "Base16"),
    BASE32("base32", "Base32"),
    BASE32_NOPADDING("base32-nopadding", "Base32 (no padding)"),
    BASE32HEX("base32hex", "Base32hex"),
    BASE32HEX_NOPADDING("base32hex-nopadding", "Base32hex (no padding)"),
    BASE64("base64", "Base64"),
    BASE64_NOPADDING("base64-nopadding", "Base64 (no padding)"),
    BASE64URL("base64url", "Base64 for URL"),
    BASE64URL_NOPADDING("base64url-nopadding", "Base64 for URL (no padding)"),
    BUBBLEBABBLE("bubblebabble", "BubbleBabble"),
    ZBASE32("z-base-32", "z-base-32"),
    Z85("z85", "Z85"),

    DEC_FIXED_SIZE_WITH_LEADING_ZEROS("dec-fixed-size-with-leading-zeros", "Decimal, fixed size with leading zeros");

    private final String code;
    private final String description;
    private static final Map<String, Encoding> codeMap;

    static {
        codeMap = getCodesForAvailableEncodings();
    }

    Encoding(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Returns the canonical code of the encoding. The canonical code is the
     * primary name that is understood by option -E and by the encoding
     * placeholders of option -F (e.g. #HASH{&lt;code&gt;}).
     *
     * @return the canonical code of the encoding
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the description of the encoding.
     *
     * @return the description of the encoding
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns whether the alphabet of the encoding is case-sensitive. Encodings
     * with a case-sensitive alphabet (e.g. Base64) require an exact match when
     * hash values are compared, all other encodings can be compared without
     * respecting the case.
     *
     * @return true if the alphabet of the encoding is case-sensitive
     */
    public boolean isCaseSensitive() {
        switch (this) {
            case BASE64:
            case BASE64_NOPADDING:
            case BASE64URL:
            case BASE64URL_NOPADDING:
            case Z85:
                return true;
            default:
                // all remaining encodings have an alphabet that consists of
                // digits and/or characters of just one case
                return false;
        }
    }

    /**
     * Returns whether both hash values are considered to be equal, respecting
     * the case-sensitivity of the given encoding (be tolerant if we can).
     *
     * @param hash the hash value
     * @param expected the expected hash value
     * @param encoding the encoding both hash values are encoded with
     * @return true if both hash values are considered to be equal
     */
    public static boolean hashesAreEqual(String hash, String expected, Encoding encoding) {
        if (hash == null || expected == null) {
            return false;
        }
        return (encoding == null || encoding.isCaseSensitive())
                ? hash.equals(expected)
                : hash.equalsIgnoreCase(expected);
    }

    public String toString() { return code; }

    /**
     * Returns all available encodings.
     *
     * @return all available encodings
     */
    public static Map<Encoding, String> getAvailableEncodings() {
        Map<Encoding, String> map = new EnumMap<>(Encoding.class);
        for (Encoding encoding : Encoding.values()) {
            if (!encoding.equals(DEC_FIXED_SIZE_WITH_LEADING_ZEROS)) { // not supported by the API, only for internal use (e.g. BSD sum)
                map.put(encoding, encoding.getDescription());
            }
        }
        return map;
    }

    /**
     * Returns all codes (canonical codes and aliases) that are understood by
     * option -E, mapped to the encoding they stand for.
     *
     * @return all codes that are understood by option -E
     */
    public static Map<String, Encoding> getCodesForAvailableEncodings() {
        Map<String, Encoding> map = new HashMap<>(24);
        map.put("bb", BUBBLEBABBLE);
        map.put("bubblebabble", BUBBLEBABBLE);
        map.put("hex", HEX);
        map.put("hex-lowercase", HEX);
        map.put("hexup", HEX_UPPERCASE); // legacy code for Jacksum 1.7.0
        map.put("hex-uppercase", HEX_UPPERCASE);
        map.put("dec", DEC);
        map.put("bin", BIN);
        map.put("oct", OCT);
        map.put("base16", BASE16);
        map.put("base32", BASE32);
        map.put("base32-nopadding", BASE32_NOPADDING);
        map.put("base32hex", BASE32HEX);
        map.put("base32hex-nopadding", BASE32HEX_NOPADDING);
        map.put("base64", BASE64);
        map.put("base64-nopadding", BASE64_NOPADDING);
        map.put("base64url", BASE64URL);
        map.put("base64url-nopadding", BASE64URL_NOPADDING);
        map.put("z-base-32", ZBASE32);
        map.put("z85", Z85);
        // not a user selectable encoding (see getAvailableEncodings), but it has
        // to be parsable, because it can occur in a generated command line, e.g.
        // in the header of a file that has been created with -a sum_bsd
        map.put("dec-fixed-size-with-leading-zeros", DEC_FIXED_SIZE_WITH_LEADING_ZEROS);
        return map;
    }

    /**
     * Returns the Encoding given by a String.
     *
     * @param encoding the encoding as String.
     * @return the Encoding given by a String.
     * @throws java.lang.IllegalArgumentException if encoding is not supported.
     */
    public static Encoding string2Encoding(String encoding) throws IllegalArgumentException {
        String key = encoding.toLowerCase(Locale.US);
        if (codeMap.containsKey(key)) {
            return codeMap.get(key);
        } else {
            throw new IllegalArgumentException(String.format("Encoding \"%s\" is unknown.", encoding));
        }
    }

    /**
     * Returns the canonical code of the encoding.
     *
     * @param encoding the encoding
     * @return the canonical code of the encoding
     * @throws java.lang.IllegalArgumentException if encoding is null.
     */
    public static String encoding2String(Encoding encoding) throws IllegalArgumentException {
        if (encoding == null) {
            throw new IllegalArgumentException("Encoding must not be null.");
        }
        return encoding.getCode();
    }
}
