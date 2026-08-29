/*
 * Jacksum 4.0.1 - a checksum/hash tool written in Java
 * Copyright (c) 2001-2026 Dipl.-Inf. (FH) Johann N. Löfflmann,
 * All Rights Reserved, <https://jacksum.net>.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <https://www.gnu.org/licenses/>.
 */

package net.jacksum.parameters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.jacksum.formats.EncodingDecoding;

public class Sequence implements Serializable {

    private static final long serialVersionUID = 1865077037563918778L;

    public enum Type {
        TXT("txt"), TXTF("txtf"), DEC("dec"), HEX("hex"), BIN("bin"), OCT("oct"),
        BASE32("base32"), BASE32HEX("base32hex"), BASE64("base64"), BASE64URL("base64url"),
        Z85("z85"), ZBASE32("z-base-32"), BUBBLEBABBLE("bubblebabble"),
        READLINE("readline"), PASSWORD("password"), FILE("file");

        private final String code;

        Type(String code) {
            this.code = code;
        }

        /**
         * Returns the code of the type, it is the indicator that has to be used
         * with option -q, e.g. "z-base-32" for the sequence -q z-base-32:&lt;seq&gt;
         *
         * @return the code of the type
         */
        public String getCode() {
            return code;
        }

        @Override
        public String toString() {
            return code;
        }
    }

    private Type type = null;

    private String payload = null;

    public Sequence(String string) {
        setSequence(string);
    }

    public Sequence(Type type, String payload) {
        setSequence(type, payload);
    }

    /**
     * Constructs a Sequence whose byte representation is given explicitly.
     *
     * It is used for a text whose bytes cannot be derived from the text alone, because
     * they depend on a character set that only the caller knows, see --string-list and
     * --charset-string-list. The payload keeps the text, so that asString() and the
     * output of a file name resp. a message stay unchanged.
     *
     * @param type the type of the sequence
     * @param payload the text of the sequence
     * @param bytes the byte representation of that text
     */
    public Sequence(Type type, String payload, byte[] bytes) {
        setSequence(type, payload);
        this.bytes = bytes;
    }

    public Sequence(Type type, byte[] payload) {
        setSequence(type, payload);
    }

    public Type getType() { return type; }

    public String getPayload() {
        return payload;
    }

    public byte[] asBytes() {
        if (bytes != null) { // given explicitly, or kept by an earlier call
            return bytes;
        }
        if (type.equals(Type.PASSWORD) || type.equals(Type.READLINE)) {
            //return new byte[]{};
            return enteredFromConsole;
        } else {
            // The result is kept, because the payload cannot change without
            // setSequence() being called, and because the parameter check decodes the
            // sequence once in order to report a malformed one before any work is done.
            // Without this, a sequence of the type file: would be read twice.
            bytes = EncodingDecoding.sequence2bytes(type, payload);
            return bytes;
        }
    }

    public String asString() {
        if (type.equals(Type.PASSWORD) || type.equals(Type.READLINE)) {
            return this.type.getCode();
        } else {
            return String.format("%s:%s", this.type.getCode(), payload);
        }
    }

    private byte[] enteredFromConsole;

    // the byte representation of the sequence, if it has been given explicitly
    private byte[] bytes;

    private void setSequence(Type type, byte[] payload) throws IllegalArgumentException {
        if (!type.equals(Type.PASSWORD) && !type.equals(Type.READLINE)) throw new IllegalArgumentException("Internal error: only type password or readline are allowed.");
        this.type = type;
        enteredFromConsole = payload;
    }

    private void setSequence(Type type, String payload) throws IllegalArgumentException {
        this.payload = payload;
        this.bytes = null; // a new payload invalidates what asBytes() has kept
        // is it a valid type?
        for (Type t : Type.values()) {
            if (t.equals(type)) {
                this.type = type;
                return;
            }
        }
        this.type = Type.HEX;
    }

    /**
     * Returns the types that are written as a prefix, e.g. "txt, txtf, dec, ... and file".
     *
     * <p>The types readline and password are left out, because they are given on their own
     * and not as a prefix in front of a sequence.</p>
     *
     * @return the supported types, for a message
     */
    private static String supportedTypes() {
        StringBuilder sb = new StringBuilder();
        List<String> codes = new ArrayList<>();
        for (Type t : Type.values()) {
            if (!t.equals(Type.READLINE) && !t.equals(Type.PASSWORD)) {
                codes.add(t.getCode());
            }
        }
        for (int i = 0; i < codes.size(); i++) {
            if (i > 0) {
                sb.append(i == codes.size() - 1 ? " and " : ", ");
            }
            sb.append(codes.get(i));
        }
        return sb.toString();
    }

    public void setSequence(String sequence) throws IllegalArgumentException {
        String indicator = sequence.toLowerCase(Locale.US);

        if (indicator.equals("password")) {
            setSequence(Type.PASSWORD, enteredFromConsole);
        } else
        if (indicator.equals("readline")) {
            setSequence(Type.READLINE, enteredFromConsole);
        } else {
            for (Type t : Type.values()) {
                String code = t.getCode();
                if (indicator.startsWith(code+":")) {
                    setSequence(t, sequence.substring(code.length()+1));
                    return;
                }
            }
            // A sequence without a type is read as hex, as documented. A hex sequence
            // consists of the digits 0-9 and a-f only, so it can never contain a colon:
            // an input of the form <word>:<rest> that has not matched any type above is
            // therefore a type that has been mistyped, and not a hex value.
            int colon = indicator.indexOf(':');
            if (colon == 0) {
                throw new IllegalArgumentException(String.format(
                        "A sequence type is expected in front of the colon. Supported types are %s.",
                        supportedTypes()));
            }
            if (colon > 0) {
                throw new IllegalArgumentException(String.format(
                        "\"%s\" is not a known sequence type. Supported types are %s.",
                        sequence.substring(0, colon), supportedTypes()));
            }
            // hex is the default
            setSequence(Type.HEX, sequence);
        }
    }

}
