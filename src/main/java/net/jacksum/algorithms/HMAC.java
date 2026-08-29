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
package net.jacksum.algorithms;

import net.jacksum.JacksumAPI;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * HMAC implementation
 * author: Johann N. Löfflmann
 */
public class HMAC extends AbstractChecksum  {

	private static final byte IPAD = 0x36;
	private static final byte OPAD = 0x5c;

	private int blocksize;
	private int digestsize;
	private AbstractChecksum digest;

	private byte[] i_key_pad;
	private byte[] o_key_pad;

	private boolean keyLengthMatchedRecommendedMinimum;
	private boolean keyWasHashed;

	private int outputLengthInBits = -1;

	public HMAC(String algorithm, int outputLengthInBits) throws NoSuchAlgorithmException {
		this(algorithm);
		if (outputLengthInBits > digest.getSize()) {
			throw new IllegalArgumentException("HMAC: the requested output length in bits must be less than or equal to the original output length of the selected algorithm.");
		}
		this.outputLengthInBits = outputLengthInBits;
	}
	/**
	 * Constructs a HMac.
	 * 
	 * @param algorithm the algorithm to be used with a HMac.
	 * @throws NoSuchAlgorithmException if the algorithm is unsupported.
	 */
	public HMAC(String algorithm) throws NoSuchAlgorithmException {
		if (algorithm == null) {
			throw new IllegalArgumentException("Algorithm must not be null.");
		}

		digest = JacksumAPI.getChecksumInstance(algorithm);
		digestsize = digest.getSize()/8;
		blocksize = digest.getBlockSize();
		if (blocksize <= 0 || blocksize < digestsize) {
			// blocksize < digestsize: ascon-xof is such an example, see FIPS 198-1
			throw new IllegalArgumentException(String.format(
					"Algorithm %s does not support hash-based message authentication code (HMAC), "
					+ "see also the option --hmacs.", algorithm));
		}

		i_key_pad = new byte[blocksize];
		o_key_pad = new byte[blocksize];
	}

	public AbstractChecksum getAlgorithm() {
		return digest;
	}

	public int getBlockSize() {
		return digest.getBlockSize();
	}

	public int getSize() {
		return digest.getSize();
	}

	/**
	 * Returns the number of significant bits of the value that is returned by
	 * getByteArray(). For a truncated HMAC this is less than getSize(), because
	 * getSize() returns the width of the underlying hash function.
	 *
	 * @return the number of significant bits in the byte array
	 * @since Jacksum 4.0.0
	 */
	@Override
	public int getOutputSizeInBits() {
		return outputLengthInBits > 0 ? Math.min(outputLengthInBits, digest.getSize()) : digest.getSize();
	}

	/**
	 * Initialize the HMAC with a key.
	 * 
	 * @param key a byte array containing a secret.
	 */
	public void init(byte[] key) {

		if (key == null) {
			throw new IllegalArgumentException("Key must not be null.");
		}

		/*
		  RFC 2104:
		  "In any case the minimal recommended length for K is L bytes
		  (as the hash output length)
		 */
		// The recommended length for the key in HMAC is at least the hash output length.
		keyLengthMatchedRecommendedMinimum = key.length >= digestsize;

		/*
		  RFC 2104:
		  "The authentication key K can be of any length up to B, the
		  block length of the hash function.  Applications that use keys longer
		  than B bytes will first hash the key using H and then use the
		  resultant L byte string as the actual key to HMAC."
		 */
		if (key.length > blocksize) {
			digest.reset();
			digest.update(key);
			key = digest.getByteArray();
			keyWasHashed = true;
		} else {
			keyWasHashed = false;
		}

		Arrays.fill(i_key_pad, IPAD);
		Arrays.fill(o_key_pad, OPAD);
		for (int i = 0; i < key.length; ++i) {
			i_key_pad[i] ^= (byte) (key[i]);
			o_key_pad[i] ^= (byte) (key[i]);
		}

		reset();
	}

	/**
	 * Wipes the helper arrays i_key_pad and o_key_pad that contain the (xored)
	 * password in the Java heap. After wiping, a call of init() is necessary again.
	 */
	public void wipe() {
		Arrays.fill(i_key_pad, (byte)0x00);
		Arrays.fill(o_key_pad, (byte)0x00);
	}

	/**
	 * Initializes the hash function and updates it with i_key_pad, so that the next
	 * message can be processed. In contrast to reset() it does not touch the number of
	 * processed bytes, because getByteArray() uses it to re-prime the hash function
	 * after the HMAC has been computed, and the caller still has to be able to read
	 * that number afterwards.
	 */
	private void initDigest() {
		digest.reset();
		digest.update(i_key_pad);
	}

	/**
	 * Initialize the hash function and update the hash function with i_key_pad.
	 */
	public void reset() {
		initDigest();
		length = 0;
		virgin = true;
	}

	@Override
	public void update(byte[] bytes, int offset, int length) {
		startNewMessageIfRequired();
		digest.update(bytes, offset, length);
		this.length += length;
	}

	/**
	 * Update the hash function with a message.
	 */
	public void update(byte[] message) {
		if (message == null) {
			return;
		}

		startNewMessageIfRequired();
		digest.update(message);
		this.length += message.length;
	}

	/**
	 * Starts a new message if the HMAC of the previous one has been computed already.
	 *
	 * getByteArray() consumes the state of the hash function in order to compute the
	 * outer hash, so the intermediate state of a message is gone once its HMAC has been
	 * taken. An update() after that therefore begins a new message rather than extending
	 * the previous one: without this, getByteArray() would keep returning the cached HMAC
	 * of the previous message while the number of processed bytes kept growing.
	 */
	private void startNewMessageIfRequired() {
		if (!virgin) {
			reset();
		}
	}


	private byte[] result = null;
	private boolean virgin = true;
	/**
	 * Compute H(i_key_pad || msg), and compute H(o_key_pad || H(i_key_pad || msg)).
	 */
	public byte[] getByteArray() {
		if (virgin) {
			result = digest.getByteArray();
			digest.reset();
			digest.update(o_key_pad);
			digest.update(result);
			result = digest.getByteArray();
			initDigest();
			virgin = false;
		}

		int rest = 0;
		int outputLengthInBytes = 0;
		if (outputLengthInBits > 0) {
			outputLengthInBytes = (outputLengthInBits / 8) + (outputLengthInBits % 8 > 0 ? 1 : 0);
			if (outputLengthInBytes > result.length) {
				outputLengthInBytes = result.length;
			} else {
				// the truncation is actually in effect; if it is not a multiple of 8,
				// the surplus bits of the least significant byte have to be zeroed
				rest = outputLengthInBits % 8;
			}
		} else {
			outputLengthInBytes = result.length;
		}

		// we don't expose internal representations
		byte[] ret = new byte[outputLengthInBytes];
		System.arraycopy(result, 0, ret, 0, outputLengthInBytes);
		if (rest > 0) {
			byte mask = (byte)(0xFF << (8-rest));
			// apply mask to the least significant byte
			ret[ret.length-1] = (byte) (ret[ret.length-1] & mask);
		}
		return ret;
	}

	public int getOutputLengthInBits() {
		return outputLengthInBits;
	}

	public void setOutputLengthInBits(int outputLengthInBits) {
		this.outputLengthInBits = outputLengthInBits;
	}

	public boolean isKeyLengthMatchedRecommendedMinimum() {
		return keyLengthMatchedRecommendedMinimum;
	}

	public boolean isKeyWasHashed() {
		return keyWasHashed;
	}

}
