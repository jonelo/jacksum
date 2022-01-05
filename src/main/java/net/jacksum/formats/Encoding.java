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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * The supported encodings.
 */
public enum Encoding {

    BIN("Binary"),
    DEC("Decimal"),
    OCT("Octal"),
    HEX("Hexadecimal (lowercase)"),
    HEX_UPPERCASE("Hexadecimal (uppercase)"),
    BASE16("Base16"),
    BASE32("Base32"),
    BASE32_NOPADDING("Base32 (no padding)"),
    BASE32HEX("Base32hex"),
    BASE32HEX_NOPADDING("Base32hex (no padding)"),
    BASE64("Base 64"),
    BASE64_NOPADDING("Base 64 (no padding)"),
    BASE64URL("Base 64 for URL"),
    BASE64URL_NOPADDING("Base 64 for URL (no padding)"),
    BUBBLEBABBLE("BubbleBabble"),
    ZBASE32("z-base-32"),
    
    DEC_FIXED_SIZE_WITH_LEADING_ZEROS("Decimal, fixed size with leading zeros");

    private final String description;
    private static final Map<String, Encoding> codeMap;

    static {
        codeMap = getCodesForAvailableEncodings();
    }

    Encoding(String description) {
        this.description = description;
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

    public static Map<String, Encoding> getCodesForAvailableEncodings() {
        Map<String, Encoding> map = new HashMap<>(20);
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
        String key = encoding.toLowerCase();
        if (codeMap.containsKey(key)) {
            return codeMap.get(key);
        } else {
            throw new IllegalArgumentException(String.format("Encoding \"%s\" is unknown.", encoding));
        }
    }
    
    public static String encoding2String(Encoding encoding) throws IllegalArgumentException {
        for (Map.Entry<String, Encoding> entry : codeMap.entrySet()) {
            if (entry.getValue().equals(encoding)) {
              return entry.getKey();
            }
        }
        throw new IllegalArgumentException(String.format("Encoding \"%s\" is unknown.", encoding));
    }
}
