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
        if (bytes != null) { // the bytes have been given explicitly
            return bytes;
        }
        if (type.equals(Type.PASSWORD) || type.equals(Type.READLINE)) {
            //return new byte[]{};
            return enteredFromConsole;
        } else {
            return EncodingDecoding.sequence2bytes(type, payload);
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
        // is it a valid type?
        for (Type t : Type.values()) {
            if (t.equals(type)) {
                this.type = type;
                return;
            }
        }
        this.type = Type.HEX;
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
            // hex is the default
            setSequence(Type.HEX, sequence);
        }
    }

}
