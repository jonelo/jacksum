/*

  Jacksum 3.8.0 - a checksum utility in Java
  Copyright (c) 2001-2024 Dipl.-Inf. (FH) Johann N. Löfflmann,
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
package net.jacksum.algorithms.md;

/*
 * Original source (MIT license aka X11 license):
 * https://github.com/romulusae/romulus/blob/master/Implementations/software/ref/Romulus-H/skinny_reference.c
 *
 * Date: 11 December 2015
 * Contact: Thomas Peyrin - thomas.peyrin@gmail.com
 * Modified on 04 May 2021 by Mustafa Khairallah - Modified the code
 * to implement only the SKINNY-128-384+ encryption version of Skinny for
 * Romulus v1.3, the NIST LwC finalist.
 * mustafa.khairallah@ntu.edu.sg
 *
 * Date: 24 March 2024
 * This source has been translated from C to Java by Johann N. Loefflmann and
 * it is released under the conditions of both the original MIT license and the
 * GPLv3+ - jonelo@jonelo.de
 */

/*
 * This file includes only the encryption function of SKINNY-128-384+ as required by Romulus-v1.3
 */

public class Skinny_128_384_plus {

    private static final boolean DEBUG = false;

    // Skinny-128-384+ parameters: 128-bit block, 384-bit tweakey and 40 rounds
    private static final int BLOCK_SIZE = 128;
    private static final int TWEAKEY_SIZE = 384;
    private static final int N_RNDS = 40;

    // Packing of data is done as follows (state[i][j] stands for row i and column j):
    // 0  1  2  3
    // 4  5  6  7
    // 8  9 10 11
    //12 13 14 15

    // 8-bit Sbox
    private static final byte[] sbox_8 = {
            (byte) 0x65, (byte) 0x4c, (byte) 0x6a, (byte) 0x42, (byte) 0x4b, (byte) 0x63, (byte) 0x43, (byte) 0x6b,
            (byte) 0x55, (byte) 0x75, (byte) 0x5a, (byte) 0x7a, (byte) 0x53, (byte) 0x73, (byte) 0x5b, (byte) 0x7b,
            (byte) 0x35, (byte) 0x8c, (byte) 0x3a, (byte) 0x81, (byte) 0x89, (byte) 0x33, (byte) 0x80, (byte) 0x3b,
            (byte) 0x95, (byte) 0x25, (byte) 0x98, (byte) 0x2a, (byte) 0x90, (byte) 0x23, (byte) 0x99, (byte) 0x2b,
            (byte) 0xe5, (byte) 0xcc, (byte) 0xe8, (byte) 0xc1, (byte) 0xc9, (byte) 0xe0, (byte) 0xc0, (byte) 0xe9,
            (byte) 0xd5, (byte) 0xf5, (byte) 0xd8, (byte) 0xf8, (byte) 0xd0, (byte) 0xf0, (byte) 0xd9, (byte) 0xf9,
            (byte) 0xa5, (byte) 0x1c, (byte) 0xa8, (byte) 0x12, (byte) 0x1b, (byte) 0xa0, (byte) 0x13, (byte) 0xa9,
            (byte) 0x05, (byte) 0xb5, (byte) 0x0a, (byte) 0xb8, (byte) 0x03, (byte) 0xb0, (byte) 0x0b, (byte) 0xb9,
            (byte) 0x32, (byte) 0x88, (byte) 0x3c, (byte) 0x85, (byte) 0x8d, (byte) 0x34, (byte) 0x84, (byte) 0x3d,
            (byte) 0x91, (byte) 0x22, (byte) 0x9c, (byte) 0x2c, (byte) 0x94, (byte) 0x24, (byte) 0x9d, (byte) 0x2d,
            (byte) 0x62, (byte) 0x4a, (byte) 0x6c, (byte) 0x45, (byte) 0x4d, (byte) 0x64, (byte) 0x44, (byte) 0x6d,
            (byte) 0x52, (byte) 0x72, (byte) 0x5c, (byte) 0x7c, (byte) 0x54, (byte) 0x74, (byte) 0x5d, (byte) 0x7d,
            (byte) 0xa1, (byte) 0x1a, (byte) 0xac, (byte) 0x15, (byte) 0x1d, (byte) 0xa4, (byte) 0x14, (byte) 0xad,
            (byte) 0x02, (byte) 0xb1, (byte) 0x0c, (byte) 0xbc, (byte) 0x04, (byte) 0xb4, (byte) 0x0d, (byte) 0xbd,
            (byte) 0xe1, (byte) 0xc8, (byte) 0xec, (byte) 0xc5, (byte) 0xcd, (byte) 0xe4, (byte) 0xc4, (byte) 0xed,
            (byte) 0xd1, (byte) 0xf1, (byte) 0xdc, (byte) 0xfc, (byte) 0xd4, (byte) 0xf4, (byte) 0xdd, (byte) 0xfd,
            (byte) 0x36, (byte) 0x8e, (byte) 0x38, (byte) 0x82, (byte) 0x8b, (byte) 0x30, (byte) 0x83, (byte) 0x39,
            (byte) 0x96, (byte) 0x26, (byte) 0x9a, (byte) 0x28, (byte) 0x93, (byte) 0x20, (byte) 0x9b, (byte) 0x29,
            (byte) 0x66, (byte) 0x4e, (byte) 0x68, (byte) 0x41, (byte) 0x49, (byte) 0x60, (byte) 0x40, (byte) 0x69,
            (byte) 0x56, (byte) 0x76, (byte) 0x58, (byte) 0x78, (byte) 0x50, (byte) 0x70, (byte) 0x59, (byte) 0x79,
            (byte) 0xa6, (byte) 0x1e, (byte) 0xaa, (byte) 0x11, (byte) 0x19, (byte) 0xa3, (byte) 0x10, (byte) 0xab,
            (byte) 0x06, (byte) 0xb6, (byte) 0x08, (byte) 0xba, (byte) 0x00, (byte) 0xb3, (byte) 0x09, (byte) 0xbb,
            (byte) 0xe6, (byte) 0xce, (byte) 0xea, (byte) 0xc2, (byte) 0xcb, (byte) 0xe3, (byte) 0xc3, (byte) 0xeb,
            (byte) 0xd6, (byte) 0xf6, (byte) 0xda, (byte) 0xfa, (byte) 0xd3, (byte) 0xf3, (byte) 0xdb, (byte) 0xfb,
            (byte) 0x31, (byte) 0x8a, (byte) 0x3e, (byte) 0x86, (byte) 0x8f, (byte) 0x37, (byte) 0x87, (byte) 0x3f,
            (byte) 0x92, (byte) 0x21, (byte) 0x9e, (byte) 0x2e, (byte) 0x97, (byte) 0x27, (byte) 0x9f, (byte) 0x2f,
            (byte) 0x61, (byte) 0x48, (byte) 0x6e, (byte) 0x46, (byte) 0x4f, (byte) 0x67, (byte) 0x47, (byte) 0x6f,
            (byte) 0x51, (byte) 0x71, (byte) 0x5e, (byte) 0x7e, (byte) 0x57, (byte) 0x77, (byte) 0x5f, (byte) 0x7f,
            (byte) 0xa2, (byte) 0x18, (byte) 0xae, (byte) 0x16, (byte) 0x1f, (byte) 0xa7, (byte) 0x17, (byte) 0xaf,
            (byte) 0x01, (byte) 0xb2, (byte) 0x0e, (byte) 0xbe, (byte) 0x07, (byte) 0xb7, (byte) 0x0f, (byte) 0xbf,
            (byte) 0xe2, (byte) 0xca, (byte) 0xee, (byte) 0xc6, (byte) 0xcf, (byte) 0xe7, (byte) 0xc7, (byte) 0xef,
            (byte) 0xd2, (byte) 0xf2, (byte) 0xde, (byte) 0xfe, (byte) 0xd7, (byte) 0xf7, (byte) 0xdf, (byte) 0xff
    };

    // ShiftAndSwitchRows permutation
    private static final byte[] P = {0, 1, 2, 3, 7, 4, 5, 6, 10, 11, 8, 9, 13, 14, 15, 12};

    // Tweakey permutation
    private static final byte[] TWEAKEY_P = {9, 15, 8, 13, 10, 14, 12, 11, 0, 1, 2, 3, 4, 5, 6, 7};

    // round constants
    private static final byte[] RC = {
            (byte) 0x01, (byte) 0x03, (byte) 0x07, (byte) 0x0F, (byte) 0x1F, (byte) 0x3E, (byte) 0x3D, (byte) 0x3B, (byte) 0x37, (byte) 0x2F,
            (byte) 0x1E, (byte) 0x3C, (byte) 0x39, (byte) 0x33, (byte) 0x27, (byte) 0x0E, (byte) 0x1D, (byte) 0x3A, (byte) 0x35, (byte) 0x2B,
            (byte) 0x16, (byte) 0x2C, (byte) 0x18, (byte) 0x30, (byte) 0x21, (byte) 0x02, (byte) 0x05, (byte) 0x0B, (byte) 0x17, (byte) 0x2E,
            (byte) 0x1C, (byte) 0x38, (byte) 0x31, (byte) 0x23, (byte) 0x06, (byte) 0x0D, (byte) 0x1B, (byte) 0x36, (byte) 0x2D, (byte) 0x1A
    };

    // debug
    private static void displayMatrix(byte[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.printf("%02x", matrix[i][j]);
            }
        }
    }

    // debug
    public static void displayCipherState(byte[][] state, byte[][][] keyCells) {
        System.out.print("S = ");
        displayMatrix(state);
        for (int k = 0; k < keyCells.length; k++) {
            System.out.print(" - TK" + (k + 1) + " = ");
            displayMatrix(keyCells[k]);
        }
    }


    // Extract and apply the subtweakey to the internal state (must be the two top rows XORed together), then update the tweakey state
    public static void addKey(byte[][] state, byte[][][] keyCells) {
        int i, j, k;
        byte pos;
        byte[][][] keyCellsTmp = new byte[3][4][4];

        // Apply the subtweakey to the internal state
        for (i = 0; i <= 1; i++) {
            for (j = 0; j < 4; j++) {
                state[i][j] ^= keyCells[0][i][j] ^ keyCells[1][i][j] ^ keyCells[2][i][j];
            }
        }

        // Update the subtweakey states with the permutation
        for (k = 0; k < (TWEAKEY_SIZE / BLOCK_SIZE); k++) {
            for (i = 0; i < 4; i++) {
                for (j = 0; j < 4; j++) {
                    // Application of the TWEAKEY permutation
                    pos = TWEAKEY_P[j + 4 * i];
                    keyCellsTmp[k][i][j] = keyCells[k][pos >> 2][pos & 0x3];
                }
            }
        }

        // Update the subtweakey states with the LFSRs
        for (k = 0; k < (TWEAKEY_SIZE / BLOCK_SIZE); k++) {
            for (i = 0; i <= 1; i++) {
                for (j = 0; j < 4; j++) {
                    // Application of LFSRs for TK updates
                    if (k == 1) {
                        keyCellsTmp[k][i][j] = (byte) (((keyCellsTmp[k][i][j] << 1) & 0xFE) ^ ((keyCellsTmp[k][i][j] >> 7) & 0x01) ^ ((keyCellsTmp[k][i][j] >> 5) & 0x01));
                    } else if (k == 2) {
                        keyCellsTmp[k][i][j] = (byte) (((keyCellsTmp[k][i][j] >> 1) & 0x7F) ^ ((keyCellsTmp[k][i][j] << 7) & 0x80) ^ ((keyCellsTmp[k][i][j] << 1) & 0x80));
                    }
                }
            }
        }

        for (k = 0; k < (TWEAKEY_SIZE / BLOCK_SIZE); k++) {
            for (i = 0; i < 4; i++) {
                for (j = 0; j < 4; j++) {
                    keyCells[k][i][j] = keyCellsTmp[k][i][j];
                }
            }
        }
    }

    // Apply the constants: using a LFSR counter on 6 bits, we XOR the 6 bits to the first 6 bits of the internal state
    public static void addConstants(byte[][] state, int r) {
        state[0][0] ^= (RC[r] & 0xF);
        state[1][0] ^= ((RC[r] >> 4) & 0x3);
        state[2][0] ^= 0x2;
    }

    // apply the 8-bit Sbox
    public static void subCell8(byte[][] state) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                state[i][j] = sbox_8[(byte) (state[i][j]) & 0xFF];
            }
        }
    }

    // Apply the ShiftRows function
    public static void shiftRows(byte[][] state) {
        byte[][] stateTmp = new byte[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                // Application of the ShiftRows permutation
                int pos = P[j + 4 * i];
                stateTmp[i][j] = state[pos >> 2][pos & 0x3];
            }
        }

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                state[i][j] = stateTmp[i][j];
            }
        }
    }

    // Apply the linear diffusion matrix
    // M =
    // 1 0 1 1
    // 1 0 0 0
    // 0 1 1 0
    // 1 0 1 0
    public static void mixColumn(byte[][] state) {
        for (int j = 0; j < 4; j++) {
            state[1][j] ^= state[2][j];
            state[2][j] ^= state[0][j];
            state[3][j] ^= state[2][j];

            byte temp = state[3][j];
            state[3][j] = state[2][j];
            state[2][j] = state[1][j];
            state[1][j] = state[0][j];
            state[0][j] = temp;
        }
    }

    // encryption function of Skinny-128-384+
    public static void enc(byte[] input, byte[] userkey) {
        byte[][] state = new byte[4][4];
        byte[][][] keyCells = new byte[3][4][4];
        int i;

        for (i = 0; i < 16; i++) {
            state[i >> 2][i & 0x3] = (byte) (input[i] & 0xFF);
            keyCells[0][i >> 2][i & 0x3] = (byte) (userkey[i] & 0xFF);
            keyCells[1][i >> 2][i & 0x3] = (byte) (userkey[i + 16] & 0xFF);
            keyCells[2][i >> 2][i & 0x3] = (byte) (userkey[i + 32] & 0xFF);
        }

        if (DEBUG) {
            System.out.print("ENC - initial state:                 ");
            displayCipherState(state, keyCells);
            System.out.println();
        }

        for (i = 0; i < N_RNDS; i++) {
            subCell8(state);
            if (DEBUG) {
                System.out.print("ENC - round " + i + " - after SubCell:      ");
                displayCipherState(state, keyCells);
                System.out.println();
            }
            addConstants(state, i);
            if (DEBUG) {
                System.out.print("ENC - round " + i + " - after AddConstants: ");
                displayCipherState(state, keyCells);
                System.out.println();
            }
            addKey(state, keyCells);
            if (DEBUG) {
                System.out.print("ENC - round " + i + " - after AddKey:       ");
                displayCipherState(state, keyCells);
                System.out.println();
            }
            shiftRows(state);
            if (DEBUG) {
                System.out.print("ENC - round " + i + " - after ShiftRows:    ");
                displayCipherState(state, keyCells);
                System.out.println();
            }
            mixColumn(state);
            if (DEBUG) {
                System.out.print("ENC - round " + i + " - after MixColumn:    ");
                displayCipherState(state, keyCells);
                System.out.println();
                System.out.println();
            }
        } // The last subtweakey should not be added

        if (DEBUG) {
            System.out.print("ENC - final state:                   ");
            displayCipherState(state, keyCells);
            System.out.println();
        }

        for (i = 0; i < 16; i++)
            input[i] = (byte) (state[i >> 2][i & 0x3] & 0xFF);
    }

}