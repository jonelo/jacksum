/*
 * Jacksum 3.7.0 - a checksum utility in Java
 * Copyright (c) 2001-2023 Dipl.-Inf. (FH) Johann N. Löfflmann,
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
        TXT, TXTF, DEC, HEX, BIN, OCT, BASE32, BASE32HEX, BASE64, BASE64URL, Z85, READLINE, PASSWORD, FILE
    }

    private Type type = null;

    private String payload = null;

    public Sequence(String string) {
        setSequence(string);
    }

    public Sequence(Type type, String payload) {
        setSequence(type, payload);
    }

    public Type getType() { return type; }

    public String getPayload() {
        return payload;
    }

    public byte[] asBytes() {
        if (type.equals(Type.PASSWORD) || type.equals(Type.READLINE)) {
            return new byte[]{};
        } else {
            return EncodingDecoding.sequence2bytes(type, payload);
        }
    }

    public String asString() {
        if (type.equals(Type.PASSWORD) || type.equals(Type.READLINE)) {
            return this.type.toString().toLowerCase();
        } else {
            return String.format("%s:%s", this.type.toString().toLowerCase(), payload);
        }
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
        String indicator = sequence.toLowerCase();

        if (indicator.equals("password")) {
            setSequence(Type.PASSWORD, null);
        } else
        if (indicator.equals("readline")) {
            setSequence(Type.READLINE, null);
        } else {
            for (Type t : Type.values()) {
                String code = t.toString().toLowerCase(Locale.US);
                if (indicator.startsWith(code+":") && !code.equals(Type.PASSWORD) && !code.equals(Type.READLINE)) {
                    setSequence(t, sequence.substring(code.length()+1));
                    return;
                }
            }
            // hex is the default
            setSequence(Type.HEX, sequence);
        }
    }

}
