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

package net.jacksum.formats;

import net.jacksum.parameters.Sequence;
import net.loefflmann.sugar.encodings.Base32;
import net.loefflmann.sugar.encodings.BubbleBabble;
import net.loefflmann.sugar.encodings.Z85;
import net.loefflmann.sugar.encodings.ZBase32;
import net.loefflmann.sugar.util.ByteSequences;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class EncodingDecoding {
    public static String encodeBytes(byte[] bytes, Encoding encoding, int grouping, Character groupChar) {
        switch (encoding) {
            case HEX:
                return ByteSequences.format(bytes, false, grouping, groupChar);
            case HEX_UPPERCASE:
                return ByteSequences.format(bytes, true, grouping, groupChar);
            case BASE16:
                return ByteSequences.format(bytes, true, 0, groupChar);
            case BASE32:
                return new Base32(Base32.Alphabet.BASE32, Base32.Padding.PADDING, Base32.UpperLower.UPPERCASE).encode(bytes);
            case BASE32_NOPADDING:
                return new Base32(Base32.Alphabet.BASE32, Base32.Padding.NO_PADDING, Base32.UpperLower.UPPERCASE).encode(bytes);
            case BASE32HEX:
                return new Base32(Base32.Alphabet.BASE32HEX, Base32.Padding.PADDING, Base32.UpperLower.UPPERCASE).encode(bytes);
            case BASE32HEX_NOPADDING:
                return new Base32(Base32.Alphabet.BASE32HEX, Base32.Padding.NO_PADDING, Base32.UpperLower.UPPERCASE).encode(bytes);
            case BASE64:
                return Base64.getEncoder().encodeToString(bytes);
            case BASE64_NOPADDING:
                return Base64.getEncoder().withoutPadding().encodeToString(bytes);
            case BASE64URL:
                return Base64.getUrlEncoder().encodeToString(bytes);
            case BASE64URL_NOPADDING:
                return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            case BUBBLEBABBLE:
                return BubbleBabble.encode(bytes);
            case DEC:
                return new BigInteger(1, bytes).toString();
            case DEC_FIXED_SIZE_WITH_LEADING_ZEROS:
                if (bytes.length == 2) { // only supported for 2 bytes (e.g. BSD sum)
                    // put back the byte array to a long
                    int value = ByteSequences.twoByteArrayToInt(bytes);
                    return String.format("%05d", value);  // five, because 2^(2*8) = 65535 which are 5 digits max.
                    //ByteBuffer wrapped = ByteBuffer.wrap(bytes); // big-endian by default
                    //short num = wrapped.getShort(); // 1
                } else throw new UnsupportedOperationException("Encoding not supported for byte arrays of size " + bytes.length);
            case BIN:
                return ByteSequences.formatAsBits(bytes);
            case OCT: {
                BigInteger big = new BigInteger(1, bytes);
                return big.toString(8);
            }
            case ZBASE32: {
                return ZBase32.encodeToString(bytes);
            }
            case Z85: {
                return Z85.getInstance(Z85.Type.PADDING_IF_REQUIRED).encode(bytes);
            }
            default:
                return ByteSequences.format(bytes, false, 0, ' ');
        }
    }

    public static byte[] sequence2bytes(Sequence.Type type, String sequence)
            throws IllegalArgumentException {
        byte[] bytes;
        switch (type) {
            case TXT:
                bytes = ByteSequences.text2Bytes(sequence);
                break;
            case TXTF:
                bytes = ByteSequences.textf2Bytes(sequence);
                break;
            case DEC:
                bytes = ByteSequences.decText2Bytes(sequence);
                break;
            case HEX:
                bytes = ByteSequences.hexText2Bytes(sequence);
                break;
            case BIN:
                bytes = ByteSequences.binText2Bytes(sequence);
                break;
            case OCT:
                bytes = ByteSequences.octText2Bytes(sequence);
                break;
            case BASE32:
                bytes = new Base32(Base32.Alphabet.BASE32,
                        sequence.length() % 5 == 0 ? Base32.Padding.NO_PADDING : Base32.Padding.PADDING,
                     Base32.UpperLower.UPPERCASE).decode(sequence);
                if (bytes == null) throw new IllegalArgumentException("Invalid Base32 string.");
                break;
            case BASE32HEX:
                bytes = new Base32(Base32.Alphabet.BASE32HEX,
                        sequence.length() % 5 == 0 ? Base32.Padding.NO_PADDING : Base32.Padding.PADDING,
                        Base32.UpperLower.UPPERCASE).decode(sequence);
                if (bytes == null) throw new IllegalArgumentException("Invalid Base32hex string.");
                break;
            case BASE64:
                bytes = Base64.getDecoder().decode(sequence);
                break;
            case BASE64URL:
                bytes = Base64.getUrlDecoder().decode(sequence);
                break;
            case Z85:
                bytes = Z85.getInstance(Z85.Type.PADDING_IF_REQUIRED).decode(sequence);
                break;
            case FILE:
                try {
                    Path p = Path.of(sequence);
                    if (Files.exists(p)) {
                        if (Files.size(p) > 128 * 1024 * 1024) {
                            throw new IllegalArgumentException(String.format("File %s is greater than 128 MiB which exceeds the limit for option -q file:<file>", sequence));
                        }
                        bytes = Files.readAllBytes(p);
                    } else {
                        throw new IllegalArgumentException(String.format("File %s does not exist.", p));
                    }
                } catch (IOException ioe) {
                    throw new IllegalArgumentException(ioe.getMessage());
                }
                break;
            default:
                throw new IllegalArgumentException(String.format("Unknown sequence type: %s", type));
        }
        return bytes;
    }
}
