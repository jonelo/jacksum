package net.jacksum.zzadopt.gnu.crypto.hash;
// ----------------------------------------------------------------------------
// This file is not part of GNU Crypto
//
// Has160 has been derived from the Sha160 class in GNU Crypto,
// and it has been developed by jonelo.
// ----------------------------------------------------------------------------

import net.jacksum.zzadopt.gnu.crypto.Registry;
import net.jacksum.zzadopt.gnu.crypto.util.Util;

public class Has160 extends BaseHash {

   // Constants and variables
   // -------------------------------------------------------------------------

   private static final int BLOCK_SIZE = 64; // inner block size in bytes

   private static final String DIGEST0 = "975E810488CF2A3D49838478124AFCE4B1C78804";

   private static final int[] w = new int[20];

   /** caches the result of the correctness test, once executed. */
   private static Boolean valid;

   /** 160-bit interim result. */
   private int h0, h1, h2, h3, h4;

   private static final int rot[] = {
       5, 11,  7, 15,  6, 13,  8, 14,  7, 12,  9, 11,  8, 15,  6, 12,  9, 14,  5, 13
   };

   private static final int tor[] = {
       27, 21, 25, 17, 26, 19, 24, 18, 25, 20, 23, 21, 24, 17, 26, 20, 23, 18, 27, 19
   };

   private static int ndx[] = {
          18,  0,  1,  2,  3, 19,  4,  5, 6,  7, 16,  8,  9, 10, 11, 17, 12, 13, 14, 15,
          18,  3,  6,  9, 12, 19, 15,  2, 5,  8, 16, 11, 14,  1,  4, 17,  7, 10, 13,  0,
          18, 12,  5, 14,  7, 19,  0,  9, 2, 11, 16,  4, 13,  6, 15, 17,  8,  1, 10,  3,
          18,  7,  2, 13,  8, 19,  3, 14, 9,  4, 16, 15, 10,  5,  0, 17, 11,  6,  1, 12
   };

   // Constructor(s)
   // -------------------------------------------------------------------------

   /** Trivial 0-arguments constructor. */
   public Has160() {
      super(Registry.HAS160_HASH, 20, BLOCK_SIZE);
   }

   /**
    * <p>Private constructor for cloning purposes.</p>
    *
    * @param md the instance to clone.
    */
   private Has160(Has160 md) {
      this();

      this.h0 = md.h0;
      this.h1 = md.h1;
      this.h2 = md.h2;
      this.h3 = md.h3;
      this.h4 = md.h4;
      this.count = md.count;
      this.buffer = (byte[]) md.buffer.clone();
   }

   // Class methods
   // -------------------------------------------------------------------------

   public static final int[]
   G(int hh0, int hh1, int hh2, int hh3, int hh4, byte[] in, int offset) {

      return has(hh0, hh1, hh2, hh3, hh4, in, offset);
   }

   // Instance methods
   // -------------------------------------------------------------------------

   // java.lang.Cloneable interface implementation ----------------------------

   @Override
   public Object clone() {
      return new Has160(this);
   }

   // Implementation of concrete methods in BaseHash --------------------------

   @Override
   protected void transform(byte[] in, int offset) {
      int[] result = has(h0, h1, h2, h3, h4, in, offset);

      h0 = result[0];
      h1 = result[1];
      h2 = result[2];
      h3 = result[3];
      h4 = result[4];
   }

   @Override
   protected byte[] padBuffer() {
      int n = (int)(count % BLOCK_SIZE);
      int padding = (n < 56) ? (56 - n) : (120 - n);
      byte[] result = new byte[padding + 8];

      // padding is always binary 1 followed by binary 0s
      result[0] = (byte) 0x80;

      // save number of bits, casting the long to an array of 8 bytes
      long bits = count << 3;

      result[padding++] = (byte) bits;
      result[padding++] = (byte)(bits >>>  8);
      result[padding++] = (byte)(bits >>> 16);
      result[padding++] = (byte)(bits >>> 24);

      result[padding++] = (byte)(bits >>> 32);
      result[padding++] = (byte)(bits >>> 40);
      result[padding++] = (byte)(bits >>> 48);
      result[padding] =   (byte)(bits >>> 56);

      return result;
   }

   @Override
   protected byte[] getResult() {
      byte[] result = new byte[] {
         (byte) h0, (byte)(h0 >>> 8), (byte)(h0 >>> 16),(byte)(h0 >>> 24),
         (byte) h1, (byte)(h1 >>> 8), (byte)(h1 >>> 16),(byte)(h1 >>> 24),
         (byte) h2, (byte)(h2 >>> 8), (byte)(h2 >>> 16),(byte)(h2 >>> 24),
         (byte) h3, (byte)(h3 >>> 8), (byte)(h3 >>> 16),(byte)(h3 >>> 24),
         (byte) h4, (byte)(h4 >>> 8), (byte)(h4 >>> 16),(byte)(h4 >>> 24)
      };

      return result;
   }

   @Override
   protected void resetContext() {
      // magic SHA-1/RIPEMD160 initialisation constants
      h0 = 0x67452301;
      h1 = 0xEFCDAB89;
      h2 = 0x98BADCFE;
      h3 = 0x10325476;
      h4 = 0xC3D2E1F0;
   }

   @Override
   public boolean selfTest() {
      if (valid == null) {
         Has160 md = new Has160();
         md.update((byte) 0x61); // a
         md.update((byte) 0x62); // b
         md.update((byte) 0x63); // c
         String result = Util.toString(md.digest());
         valid = DIGEST0.equals(result);
      }
      return valid;
   }

   // HAS specific methods ----------------------------------------------------

   private static synchronized int[]
   has(int hh0, int hh1, int hh2, int hh3, int hh4, byte[] in, int offset) {
      int A = hh0;
      int B = hh1;
      int C = hh2;
      int D = hh3;
      int E = hh4;
      int r, T;

      for (r = 0; r < 16; r++) {
         w[r] = in[offset+3]         << 24 |
               (in[offset+2] & 0xFF) << 16 |
               (in[offset+1] & 0xFF) <<  8 |
               (in[offset] & 0xFF);
         offset+=4;

      }


      // rounds 0-19
      w[16] = w[ 0] ^ w[ 1] ^ w[ 2] ^ w[ 3];
      w[17] = w[ 4] ^ w[ 5] ^ w[ 6] ^ w[ 7];
      w[18] = w[ 8] ^ w[ 9] ^ w[10] ^ w[11];
      w[19] = w[12] ^ w[13] ^ w[14] ^ w[15];
      for (r = 0; r < 20; r++) {
         T = (A << rot[r] | A >>> tor[r]) + ((B & C) | (~B & D)) + E + w[ndx[r]];
         E = D;
         D = C;
         C = B << 10 | B >>> 22;
         B = A;
         A = T;
      }

      // rounds 20-39
      w[16] = w[ 3] ^ w[ 6] ^ w[ 9] ^ w[12];
      w[17] = w[ 2] ^ w[ 5] ^ w[ 8] ^ w[15];
      w[18] = w[ 1] ^ w[ 4] ^ w[11] ^ w[14];
      w[19] = w[ 0] ^ w[ 7] ^ w[10] ^ w[13];
      for (r = 20; r < 40; r++) {
         T = (A << rot[r-20] | A >>> tor[r-20]) + (B ^ C ^ D) + E + w[ndx[r]] + 0x5A827999;
         E = D;
         D = C;
         C = B << 17 | B >>> 15;
         B = A;
         A = T;
      }

      // rounds 40-59
      w[16] = w[ 5] ^ w[ 7] ^ w[12] ^ w[14];
      w[17] = w[ 0] ^ w[ 2] ^ w[ 9] ^ w[11];
      w[18] = w[ 4] ^ w[ 6] ^ w[13] ^ w[15];
      w[19] = w[ 1] ^ w[ 3] ^ w[ 8] ^ w[10];
      for (r = 40; r < 60; r++) {
          T = (A << rot[r-40] | A >>> tor[r-40]) + (C ^ (B | ~D)) + E + w[ndx[r]] + 0x6ED9EBA1;
         E = D;
         D = C;
         C = B << 25 | B >>> 7;
         B = A;
         A = T;
      }

      // rounds 60-79
      w[16] = w[ 2] ^ w[ 7] ^ w[ 8] ^ w[13];
      w[17] = w[ 3] ^ w[ 4] ^ w[ 9] ^ w[14];
      w[18] = w[ 0] ^ w[ 5] ^ w[10] ^ w[15];
      w[19] = w[ 1] ^ w[ 6] ^ w[11] ^ w[12];
      for (r = 60; r < 80; r++) {
         T = (A << rot[r-60] | A >>> tor[r-60]) + (B ^ C ^ D) + E + w[ndx[r]] + 0x8F1BBCDC;
         E = D;
         D = C;
         C = B << 30 | B >>> 2;
         B = A;
         A = T;
      }

      return new int[] {hh0+A, hh1+B, hh2+C, hh3+D, hh4+E};
   }
}
