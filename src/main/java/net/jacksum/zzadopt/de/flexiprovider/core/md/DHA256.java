/*
 * Copyright (c) 1998-2003 by The FlexiProvider Group,
 *                            Technische Universitaet Darmstadt 
 *
 * For conditions of usage and distribution please refer to the
 * file COPYING in the root directory of this package.
 *
 */

package net.jacksum.zzadopt.de.flexiprovider.core.md;

import net.jacksum.zzadopt.de.flexiprovider.api.MessageDigest;
import net.jacksum.zzadopt.de.flexiprovider.common.util.BigEndianConversions;

/**
 * This is an implementaion of the DHA-256 Message digest
 * 
 * DHA-256 is a 256-bit hash and is meant to provide 128 bits of security
 * against collision attacks.
 * 
 * @author Paul Nguentcheu
 */
public class DHA256 extends MessageDigest {

	/**
	 * The algorithm name.
	 */
	public static final String ALG_NAME = "DHA256";

	/**
	 * Initial hash value H<sup>(0)</sup>
	 * 
	 * These were obtained by taking the fractional parts of the square roots of
	 * the first eight primes.
	 */
	private static final int[] H0 = { 0x6a09e667, 0xbb67ae85, 0x3c6ef372,
			0xa54ff53a, 0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19 };

	/**
	 * Constant words K<sub>0...63</sub>
	 * 
	 * These are the first thirty-two bits of the fractional parts of the cube
	 * roots of the first sixty-four primes.
	 */
	private static final int[] K = { 0x428a2f98, 0x71374491, 0xb5c0fbcf,
			0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
			0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74,
			0x80deb1fe, 0x9bdc06a7, 0xc19bf174, 0xe49b69c1, 0xefbe4786,
			0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc,
			0x76f988da, 0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7,
			0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967, 0x27b70a85,
			0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb,
			0x81c2c92e, 0x92722c85, 0xa2bfe8a1, 0xa81a664b, 0xc24b8b70,
			0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
			0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3,
			0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3, 0x748f82ee, 0x78a5636f,
			0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7,
			0xc67178f2, };

	/**
	 * Length of the DHA-256 message digest in bytes
	 */
	private static final int DHA256_DIGEST_LENGTH = 32;

	/**
	 * Input buffer
	 */
	private byte[] buffer;

	/**
	 * Counter for the number of bytes of the message
	 */
	private long count;

	/**
	 * Contains the digest value after complete message has been processed
	 */
	private int[] H = new int[8];

	/**
	 * Default constructor
	 */
	public DHA256() {
		buffer = new byte[64];
		reset();
	}

	/**
	 * @return the digest length in bytes
	 */
	public int getDigestLength() {
		return DHA256_DIGEST_LENGTH;
	}

	/**
	 * Update the digest using the specified array of bytes, starting at the
	 * specified offset.
	 * 
	 * @param input
	 *            the byte array to use for the update
	 * @param inOff
	 *            the offset to start from in the byte array
	 * @param inLen
	 *            the number of bytes to use
	 */
	public synchronized void update(byte[] input, int inOff, int inLen) {
		int bufOffset = ((int) count) & 63;
		int copyLen;

		while (inLen > 0) {
			copyLen = 64 - bufOffset;
			copyLen = (inLen > copyLen) ? copyLen : inLen;
			System.arraycopy(input, inOff, buffer, bufOffset, copyLen);
			inLen -= copyLen;
			inOff += copyLen;
			count += copyLen;
			bufOffset = (bufOffset + copyLen) & 63;

			if (bufOffset == 0) {
				processBlock();
			}
		}
	}

	/**
	 * Update the digest using the specified byte.
	 * 
	 * @param input
	 *            the byte to use for the update
	 */
	public synchronized void update(byte input) {
		buffer[(int) count & 63] = input;

		if ((int) (count & 63) == 63) {
			processBlock();
		}
		count++;
	}

	/**
	 * Complete the hash computation by performing final operations such as
	 * padding.
	 * 
	 * @return the digest value
	 */
	public synchronized byte[] digest() {
		pad();

		byte[] digestValue = new byte[DHA256_DIGEST_LENGTH];
		for (int i = 0; i < 8; i++) {
			BigEndianConversions.I2OSP(H[i], digestValue, i << 2);
		}

		reset();

		return digestValue;
	}

	/**
	 * Reset the digest objects to its initial state.
	 */
	public void reset() {
		H[0] = H0[0];
		H[1] = H0[1];
		H[2] = H0[2];
		H[3] = H0[3];
		H[4] = H0[4];
		H[5] = H0[5];
		H[6] = H0[6];
		H[7] = H0[7];

		count = 0;
	}

	/**
	 * Compute the hash value of the current block and store it in H
	 */
	private synchronized void processBlock() {
		int T1, T2;
		int[] W = new int[64];
		int a, b, c, d, e, f, g, h;

		a = H[0];
		b = H[1];
		c = H[2];
		d = H[3];
		e = H[4];
		f = H[5];
		g = H[6];
		h = H[7];

		// Calculate expanded message blocks (big endian)
		W[0] = BigEndianConversions.OS2IP(buffer, 0);
		W[1] = BigEndianConversions.OS2IP(buffer, 4);
		W[2] = BigEndianConversions.OS2IP(buffer, 8);
		W[3] = BigEndianConversions.OS2IP(buffer, 12);
		W[4] = BigEndianConversions.OS2IP(buffer, 16);
		W[5] = BigEndianConversions.OS2IP(buffer, 20);
		W[6] = BigEndianConversions.OS2IP(buffer, 24);
		W[7] = BigEndianConversions.OS2IP(buffer, 28);
		W[8] = BigEndianConversions.OS2IP(buffer, 32);
		W[9] = BigEndianConversions.OS2IP(buffer, 36);
		W[10] = BigEndianConversions.OS2IP(buffer, 40);
		W[11] = BigEndianConversions.OS2IP(buffer, 44);
		W[12] = BigEndianConversions.OS2IP(buffer, 48);
		W[13] = BigEndianConversions.OS2IP(buffer, 52);
		W[14] = BigEndianConversions.OS2IP(buffer, 56);
		W[15] = BigEndianConversions.OS2IP(buffer, 60);

		// for(int i = 16; i < 64; i++) {
		// W[i] = sigma1(W[i-15]) + W[i-9] + sigma0(W[i-1]) + W[i-16];}

		W[16] = sigma1(W[1]) + W[7] + sigma0(W[15]) + W[0];
		W[17] = sigma1(W[2]) + W[8] + sigma0(W[16]) + W[1];
		W[18] = sigma1(W[3]) + W[9] + sigma0(W[17]) + W[2];
		W[19] = sigma1(W[4]) + W[10] + sigma0(W[18]) + W[3];
		W[20] = sigma1(W[5]) + W[11] + sigma0(W[19]) + W[4];
		W[21] = sigma1(W[6]) + W[12] + sigma0(W[20]) + W[5];
		W[22] = sigma1(W[7]) + W[13] + sigma0(W[21]) + W[6];
		W[23] = sigma1(W[8]) + W[14] + sigma0(W[22]) + W[7];
		W[24] = sigma1(W[9]) + W[15] + sigma0(W[23]) + W[8];
		W[25] = sigma1(W[10]) + W[16] + sigma0(W[24]) + W[9];
		W[26] = sigma1(W[11]) + W[17] + sigma0(W[25]) + W[10];
		W[27] = sigma1(W[12]) + W[18] + sigma0(W[26]) + W[11];
		W[28] = sigma1(W[13]) + W[19] + sigma0(W[27]) + W[12];
		W[29] = sigma1(W[14]) + W[20] + sigma0(W[28]) + W[13];
		W[30] = sigma1(W[15]) + W[21] + sigma0(W[29]) + W[14];
		W[31] = sigma1(W[16]) + W[22] + sigma0(W[30]) + W[15];
		W[32] = sigma1(W[17]) + W[23] + sigma0(W[31]) + W[16];
		W[33] = sigma1(W[18]) + W[24] + sigma0(W[32]) + W[17];
		W[34] = sigma1(W[19]) + W[25] + sigma0(W[33]) + W[18];
		W[35] = sigma1(W[20]) + W[26] + sigma0(W[34]) + W[19];
		W[36] = sigma1(W[21]) + W[27] + sigma0(W[35]) + W[20];
		W[37] = sigma1(W[22]) + W[28] + sigma0(W[36]) + W[21];
		W[38] = sigma1(W[23]) + W[29] + sigma0(W[37]) + W[22];
		W[39] = sigma1(W[24]) + W[30] + sigma0(W[38]) + W[23];
		W[40] = sigma1(W[25]) + W[31] + sigma0(W[39]) + W[24];
		W[41] = sigma1(W[26]) + W[32] + sigma0(W[40]) + W[25];
		W[42] = sigma1(W[27]) + W[33] + sigma0(W[41]) + W[26];
		W[43] = sigma1(W[28]) + W[34] + sigma0(W[42]) + W[27];
		W[44] = sigma1(W[29]) + W[35] + sigma0(W[43]) + W[28];
		W[45] = sigma1(W[30]) + W[36] + sigma0(W[44]) + W[29];
		W[46] = sigma1(W[31]) + W[37] + sigma0(W[45]) + W[30];
		W[47] = sigma1(W[32]) + W[38] + sigma0(W[46]) + W[31];
		W[48] = sigma1(W[33]) + W[39] + sigma0(W[47]) + W[32];
		W[49] = sigma1(W[34]) + W[40] + sigma0(W[48]) + W[33];
		W[50] = sigma1(W[35]) + W[41] + sigma0(W[49]) + W[34];
		W[51] = sigma1(W[36]) + W[42] + sigma0(W[50]) + W[35];
		W[52] = sigma1(W[37]) + W[43] + sigma0(W[51]) + W[36];
		W[53] = sigma1(W[38]) + W[44] + sigma0(W[52]) + W[37];
		W[54] = sigma1(W[39]) + W[45] + sigma0(W[53]) + W[38];
		W[55] = sigma1(W[40]) + W[46] + sigma0(W[54]) + W[39];
		W[56] = sigma1(W[41]) + W[47] + sigma0(W[55]) + W[40];
		W[57] = sigma1(W[42]) + W[48] + sigma0(W[56]) + W[41];
		W[58] = sigma1(W[43]) + W[49] + sigma0(W[57]) + W[42];
		W[59] = sigma1(W[44]) + W[50] + sigma0(W[58]) + W[43];
		W[60] = sigma1(W[45]) + W[51] + sigma0(W[59]) + W[44];
		W[61] = sigma1(W[46]) + W[52] + sigma0(W[60]) + W[45];
		W[62] = sigma1(W[47]) + W[53] + sigma0(W[61]) + W[46];
		W[63] = sigma1(W[48]) + W[54] + sigma0(W[62]) + W[47];

		for (int i = 0; i < 64; i++) {

			T1 = ((h << 19 | h >>> 13) ^ (h << 29 | h >>> 3) ^ h)
					+ (f & g ^ g & h ^ f & h) + e + K[i] + W[i];
			T2 = ((d << 11 | d >>> 21) ^ (d << 25 | d >>> 7) ^ d)
					+ (~b & d ^ b & c) + a + K[i] + W[i];
			a = b;
			b = c << 17 | c >>> 15;
			c = d;
			d = T1;
			e = f;
			f = g << 2 | g >>> 30;
			g = h;
			h = T2;
		}

		H[0] += a;
		H[1] += b;
		H[2] += c;
		H[3] += d;
		H[4] += e;
		H[5] += f;
		H[6] += g;
		H[7] += h;
	}

	/**
	 * This method performs the padding and hash the value.
	 */
	private void pad() {
		// compute length of message in bits
		long bitLength = count << 3;

		// append single 1-bit trailed by 0-bits to message
		buffer[(int) count & 63] = (byte) 0x80;
		count++;

		if ((int) (count & 63) > 56) {
			for (int i = (int) count & 63; i < 64; i++) {
				buffer[i] = 0;
				count++;
			}
			processBlock();
		} else if ((int) (count & 63) == 0) {
			processBlock();
		}

		for (int i = (int) count & 63; i < 56; i++) {
			buffer[i] = 0;
		}

		// append length of message
		BigEndianConversions.I2OSP(bitLength, buffer, 56);

		// chomp last block
		processBlock();
	}

	/**
	 * Sigma 0 function
	 */
	private static int sigma0(int x) {
		return ((x << 7) | (x >>> 25)) ^ ((x << 22) | (x >>> 10)) ^ x;
	}

	/**
	 * Sigma 1 function
	 */
	private static int sigma1(int x) {
		return ((x << 13) | (x >>> 19)) ^ ((x << 27) | (x >>> 5)) ^ x;
	}

}
