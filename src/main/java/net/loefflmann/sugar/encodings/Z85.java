/*
   Copy from
   https://github.com/Samourai-Wallet/ExtLibJ/blob/develop/java/com/samourai/wallet/util/Z85.java
   on 2023-05-16, released in the public domain, see also
   https://github.com/Samourai-Wallet/ExtLibJ/blob/develop/LICENSE-ExtLibJ

   modifications:
   - 2023-05-19 by jonelo: added types STRICT and PADDING_IF_REQUIRED
 */

package net.loefflmann.sugar.encodings;

/**
 * Encodes and decodes data with the Z85 encoding (ZeroMQ Base-85).
 */
public class Z85 {

    /** The regular expression that matches a string of Z85 characters. */
    public static final String Z85_REGEX = "^[0-9A-Za-z\\.\\-:\\+\\=\\^!\\/\\*\\?&<>\\(\\)\\[\\]\\{\\}\\@%\\$\\#]+$";

    private static Z85 instanceStrict = null;
    private static Z85 instancePaddingIfRequired = null;

    private static final int[] decoders = new int[]{
            0x00, 0x44, 0x00, 0x54, 0x53, 0x52, 0x48, 0x00, 0x4B, 0x4C, 0x46, 0x41, 0x00, 0x3F, 0x3E, 0x45,
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x40, 0x00, 0x49, 0x42, 0x4A, 0x47,
            0x51, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2A, 0x2B, 0x2C, 0x2D, 0x2E, 0x2F, 0x30, 0x31, 0x32,
            0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3A, 0x3B, 0x3C, 0x3D, 0x4D, 0x00, 0x4E, 0x43, 0x00,
            0x00, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18,
            0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F, 0x20, 0x21, 0x22, 0x23, 0x4F, 0x00, 0x50, 0x00, 0x00
    };

    private static char[] encoders = new char[]{
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D',
            'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
            'Y', 'Z', '.', '-', ':', '+', '=', '^', '!', '/', '*', '?', '&', '<', '>', '(', ')', '[', ']', '{',
            '}', '@', '%', '$', '#'
    };

    private Z85() {

    }

    private Z85(Type type) {
        this.type = type;
    }

    /** The type of a Z85 instance. */
    public enum Type { /** The input length must be a multiple of 4 bytes, the encoded length a multiple of 5 characters. */ STRICT, /** Input of any length is accepted, padding is applied if required. */ PADDING_IF_REQUIRED }

    private Type type = Type.STRICT;

    /**
     * Returns the type of this instance.
     *
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the shared instance for the given type.
     *
     * @param type the type
     * @return the instance for the type
     * @throws IllegalArgumentException if the type is not supported
     */
    public static Z85 getInstance(Type type) throws IllegalArgumentException {

        switch (type) {
            case STRICT:
                if (instanceStrict == null) {
                    instanceStrict = new Z85(Type.STRICT);
                }
                return instanceStrict;
            case PADDING_IF_REQUIRED:
                if (instancePaddingIfRequired == null) {
                    instancePaddingIfRequired = new Z85(Type.PADDING_IF_REQUIRED);
                }
                return instancePaddingIfRequired;
            default: throw new IllegalArgumentException(String.format("Z85: Type %s is not supported.", type));
        }
    }

    /**
     * Decodes a Z85 encoded string.
     *
     * @param s the Z85 encoded string
     * @return the decoded bytes, an empty array if s is empty
     * @throws IllegalArgumentException if s is not valid Z85 encoded
     */
    public byte[] decode(String s) throws IllegalArgumentException {
        if (s.isEmpty()) {
            return new byte[0]; // nothing to decode
        }
        try {
            checkZ85(s);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("Z85 decoding error: %s", e.getMessage()));
        }
        int remainder = s.length() % 5;
        int padding = 5 - (remainder == 0 ? 5 : remainder);
        for (int p = 0; p < padding; p++) {
            s += encoders[encoders.length - 1];
        }
        int length = s.length();
        byte[] ret = new byte[(length * 4 / 5) - padding];
        int index = 0;
        long value = 0;
        for (int i = 0; i < length; i++) {
            int code = s.charAt(i) - 32;
            value = value * 85 + decoders[code];
            if ((i + 1) % 5 == 0) {
                // 85^5 is bigger than 2^32, so not every combination of five characters
                // of the Z85 alphabet is a valid encoding of four bytes
                if (value > 0xFFFFFFFFL) {
                    throw new IllegalArgumentException(
                            String.format("Z85 decoding error: the five characters \"%s\" do not encode four bytes, the value is out of range.",
                                    s.substring(i - 4, i + 1)));
                }
                int div = 256 * 256 * 256;
                while (div >= 1) {
                    if (index < ret.length) {
                        ret[index++] = (byte) ((value / div) % 256);
                    }
                    div /= 256;
                }
                value = 0;
            }
        }

        return ret;
    }

    /**
     * Encodes bytes with Z85.
     *
     * @param bytes the bytes to encode
     * @return the Z85 encoded string
     * @throws IllegalArgumentException if the type is {@code STRICT} and the
     * length of the input is not a multiple of 4 bytes
     */
    public String encode(byte[] bytes) throws IllegalArgumentException {

        int remainder = bytes.length % 4;
        if (type == Type.STRICT && remainder > 0) {
            throw new IllegalArgumentException("Z85 encoding error: the length of the input must be a multiple of 4 bytes.");
        }

        int padding = (remainder > 0) ? 4 - remainder : 0;
        StringBuilder ret = new StringBuilder();
        long value = 0;
        for (int i = 0; i < bytes.length + padding; i++) {
            boolean isPadding = i >= bytes.length;
            value = value * 256 + (isPadding ? 0 : bytes[i] & 0xFF);
            if ((i + 1) % 4 == 0) {
                int div = 85 * 85 * 85 * 85;
                for (int j = 5; j > 0; j--) {
                    if (!isPadding || j > padding) {
                        int code = (int) ((value / div) % 85);
                        ret.append(encoders[code]);
                    }
                    div /= 85;
                }
                value = 0;
            }
        }

        return ret.toString();
    }

    /**
     * Checks whether a string is valid Z85 encoded.
     * @param s the string to check
     * @return true if the string is valid Z85 encoded.
     */
    public boolean isZ85(String s) {
        if (type == Type.STRICT) {
            return s.length() % 5 == 0 && s.matches(Z85_REGEX);
        } else {
            return s.matches(Z85_REGEX);
        }
    }

    /**
     * Checks whether a string is valid Z85 encoded.
     * @param s the string to check
     * @throws IllegalArgumentException if s contains a character that is not part of the Z85 alphabet or
     * (dependent on the instance type) if the length of s is not a multiple of five characters.
     */
    public void checkZ85(String s) throws IllegalArgumentException {
        if (!s.matches(Z85_REGEX)) {
            throw new IllegalArgumentException("The encoded string contains a character that is not part of the Z85 alphabet.");
        }

        if (type == Type.STRICT && s.length() % 5 > 0) {
            throw new IllegalArgumentException("The length of the encoded string must be a multiple of five characters.");
        }
    }


}