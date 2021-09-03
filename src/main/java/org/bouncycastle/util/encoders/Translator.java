package org.bouncycastle.util.encoders;

/**
 * General interface for a translator.
 */
public interface Translator
{
    /**
     * size of the output block on encoding produced by getDecodedBlockSize()
     * bytes.
     * @return an int.
     */
    public int getEncodedBlockSize();

    public int encode(byte[] in, int inOff, int length, byte[] out, int outOff);

    /**
     * size of the output block on decoding produced by getEncodedBlockSize()
     * bytes.
     * @return an int.
     */
    public int getDecodedBlockSize();

    public int decode(byte[] in, int inOff, int length, byte[] out, int outOff);
}
