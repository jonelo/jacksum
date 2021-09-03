package net.jacksum.zzadopt.kr.re.nsr.crypto.util;

/**
 * Tool for converting byte arrays to little-endian type integers
 */
public class PackLE {

    /**
     * Convert byte array to unsigned integer
     *
     * @param in byte array to convert
     * @param offset Starting offset
     * @return an int.
     */
    public static int toU32(byte[] in, int offset) {

        int result = (in[offset] & 0xff);
        result |= (in[++offset] & 0xff) << 8;
        result |= (in[++offset] & 0xff) << 16;
        result |= (in[++offset] & 0xff) << 24;

        return result;
    }

    /**
     * Convert byte array to unsigned integer array
     *
     * @param in Byte array to convert
     * @param inOff Starting offset of byte array
     * @param out array of unsigned integers
     * @param outOff starting offset of unsigned integer array
     * @param length Length of unsigned integer to convert
     */
    public static void toU32(byte[] in, int inOff, int[] out, int outOff, int length) {

        for (int idx = outOff; idx < outOff + length; ++idx, ++inOff) {
            out[idx] = in[inOff] & 0xff;
            out[idx] |= (in[++inOff] & 0xff) << 8;
            out[idx] |= (in[++inOff] & 0xff) << 16;
            out[idx] |= (in[++inOff] & 0xff) << 24;
        }

    }

    /**
     * Convert byte array to unsigned long
     *
     * @param in 변환할 바이트 배열
     * @param offset 시작 오프셋
     * @return a long.
     */
    public static long toU64(byte[] in, int offset) {

        long result = (in[offset] & 0xff);
        result |= (long) (in[++offset] & 0xff) << 8;
        result |= (long) (in[++offset] & 0xff) << 16;
        result |= (long) (in[++offset] & 0xff) << 24;
        result |= (long) (in[++offset] & 0xff) << 32;
        result |= (long) (in[++offset] & 0xff) << 40;
        result |= (long) (in[++offset] & 0xff) << 48;
        result |= (long) (in[++offset] & 0xff) << 56;

        return result;
    }

    /**
     * Convert byte array to unsigned long array
     *
     * @param in 변환할 바이트 배열
     * @param inOff 바이트 배열의 시작 오프셋
     * @param out unsigned long 배열
     * @param outOff unsigned long 배열의 시작 오프셋
     * @param length 변환할 unsigned long 의 길이
     */
    public static void toU64(byte[] in, int inOff, long[] out, int outOff, int length) {

        for (int idx = outOff; idx < outOff + length; ++idx, ++inOff) {
            out[idx] = in[inOff] & 0xff;
            out[idx] |= (long) (in[++inOff] & 0xff) << 8;
            out[idx] |= (long) (in[++inOff] & 0xff) << 16;
            out[idx] |= (long) (in[++inOff] & 0xff) << 24;
            out[idx] |= (long) (in[++inOff] & 0xff) << 32;
            out[idx] |= (long) (in[++inOff] & 0xff) << 40;
            out[idx] |= (long) (in[++inOff] & 0xff) << 48;
            out[idx] |= (long) (in[++inOff] & 0xff) << 56;
        }

    }
}
