/*


  Jacksum 3.8.0 - a checksum utility in Java
  Copyright (c) 2001-2023 Dipl.-Inf. (FH) Johann N. Löfflmann,
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
package net.jacksum.selectors;

public class Selectors {

    public final static Class<?>[] allSupportedSelectorClasses = {
        Adler32_Selector.class,
        AsconHash_Selector.class,
        AsconHasha_Selector.class,
        AsconXof_Selector.class,
        AsconXofA_Selector.class,
        AstStrsum_Selector.class,
        Blake_224_Selector.class,
        Blake_256_Selector.class,
        Blake_384_Selector.class,
        Blake_512_Selector.class,
        Blake2b_Selector.class,
        Blake2s_Selector.class,
        Blake2sp_Selector.class,
        Blake2bp_Selector.class,
        Blake3_Selector.class,
        BlueMidnightWish224_Selector.class,
        BlueMidnightWish256_Selector.class,
        BlueMidnightWish384_Selector.class,
        BlueMidnightWish512_Selector.class,
        CksumMinix_Selector.class,
        Cksum_Selector.class,
        CRC8_Selector.class,
        CRC16_Selector.class,
        CRC16Minix_Selector.class,
        CRC24_Selector.class,
        CRC32_Selector.class,
        CRC32mpeg2_Selector.class,
        CRC32bzip2_Selector.class,
        CRC32fddi_Selector.class,
        CRC32ubi_Selector.class,
        CRC32_PHP_Selector.class,
        CRC32c_Selector.class,
        CRC64_Selector.class,
        CRC64_ECMA182_Selector.class,
        CRC64_GO_Selector.class,
        CRC64xz_Selector.class,
        CRC82darc_Selector.class,
        CubeHash224_Selector.class,
        CubeHash256_Selector.class,
        CubeHash384_Selector.class,
        CubeHash512_Selector.class,
        DHA256_Selector.class,
        ECHO224_Selector.class,
        ECHO256_Selector.class,
        ECHO384_Selector.class,
        ECHO512_Selector.class,
        Ed2k_Selector.class,
        Elf_Selector.class,
        Esch_Selector.class,
        FCS16_Selector.class,
        Fletcher16_Selector.class,
        FNV0_Selector.class,
        FNV1_Selector.class,
        FNV1a_Selector.class,
        FORK256_Selector.class,
        Fugue224_Selector.class,
        Fugue256_Selector.class,
        Fugue384_Selector.class,
        Fugue512_Selector.class,
        Groestl224_Selector.class,
        Groestl256_Selector.class,
        Groestl384_Selector.class,
        Groestl512_Selector.class,
        GostDefault_Selector.class,
        GostCryptoPro_Selector.class,
        Hamsi224_Selector.class,
        Hamsi256_Selector.class,
        Hamsi384_Selector.class,
        Hamsi512_Selector.class,
        Has160_Selector.class,
        Haval_Selector.class,
        JH224_Selector.class,
        JH256_Selector.class,
        JH384_Selector.class,
        JH512_Selector.class,
        Joaat32_Selector.class,
        KangarooTwelve_Selector.class,
        Keccak224_Selector.class,
        Keccak256_Selector.class,
        Keccak288_Selector.class,
        Keccak384_Selector.class,
        Keccak512_Selector.class,
        Kupyna256_Selector.class,
        Kupyna384_Selector.class,
        Kupyna512_Selector.class,
        LSH_Selector.class,
        Luffa224_Selector.class,
        Luffa256_Selector.class,
        Luffa384_Selector.class,
        Luffa512_Selector.class,
        MarsupilamiFourteen_Selector.class,
        MD2_Selector.class,
        MD4_Selector.class,
        MD5_Selector.class,
        MDC2_Selector.class,
        Panama_Selector.class,
        PrngHash_Selector.class,
        PhotonBeetle_Selector.class,
        Radiogatun32_Selector.class,
        Radiogatun64_Selector.class,
        Ripemd128_Selector.class,
        Ripemd160_Selector.class,
        Ripemd256_Selector.class,
        Ripemd320_Selector.class,
        RomulusH_Selector.class,
        SHA0_Selector.class,
        SHA1_Selector.class,
        SHA2_224_Selector.class,
        SHA2_256_Selector.class,
        SHA2_384_Selector.class,
        SHA2_512_Selector.class,
        SHA2_512_224_Selector.class,
        SHA2_512_256_Selector.class,
        SHA3_224_Selector.class,
        SHA3_256_Selector.class,
        SHA3_384_Selector.class,
        SHA3_512_Selector.class,
        Shabal192_Selector.class,
        Shabal224_Selector.class,
        Shabal256_Selector.class,
        Shabal384_Selector.class,
        Shabal512_Selector.class,
        //Shavite224_Selector.class,
        //Shavite256_Selector.class,
        //Shavite384_Selector.class,
        //Shavite512_Selector.class,

        SHAKE128_Selector.class,
        SHAKE256_Selector.class,
        SIMD224_Selector.class,
        SIMD256_Selector.class,
        SIMD384_Selector.class,
        SIMD512_Selector.class,

        Skein_Selector.class,
        SM3_Selector.class,
        Streebog256_Selector.class,
        Streebog512_Selector.class,
        SumBSD_Selector.class,
        SumMinix_Selector.class,
        SumSysV_Selector.class,
        Sum8_Selector.class,
        Sum16_Selector.class,
        Sum24_Selector.class,
        Sum32_Selector.class,
        Sum40_Selector.class,
        Sum48_Selector.class,
        Sum56_Selector.class,
        Sum64_Selector.class,
        Tiger128_Selector.class,
        Tiger160_Selector.class,
        Tiger_Selector.class,
        Tiger2_Selector.class,
        TTH_Selector.class,
        Tiger_PHP_flavour_4_rounds_Selector.class,
        VSH_Selector.class,
        Whirlpool0_Selector.class,
        Whirlpool1_Selector.class,
        Whirlpool2_Selector.class,
        Xoodyak_Selector.class,
        Xor8_Selector.class,
        xxHash32_Selector.class
    };

    public final static Class<?>[] allSelectorClasses = {
        // the combined hash algorithm has to be the first in this list!
        CombinedChecksum_Selector.class,

        HMAC_Selector.class,
        // most popular algorithms first due to performance reasons
        // order is also used by the brute forcer in order to find algorithms

        // National Standards
        // ==================

        // USA: SHA-3 family
        SHA3_512_Selector.class,
        SHA3_384_Selector.class,
        SHA3_256_Selector.class,
        SHA3_224_Selector.class,
        SHAKE256_Selector.class,
        SHAKE128_Selector.class,
        
        // USA: SHA-2 family
        SHA2_512_256_Selector.class,
        SHA2_512_224_Selector.class,
        SHA2_512_Selector.class,
        SHA2_384_Selector.class,
        SHA2_256_Selector.class,
        SHA2_224_Selector.class,
        
        // USA: SHA-1 family
        SHA1_Selector.class,
        
        // Russia
        Streebog512_Selector.class,
        Streebog256_Selector.class,
        GostCryptoPro_Selector.class,
        GostDefault_Selector.class,

        // South Korea
        LSH_Selector.class,
        Has160_Selector.class,
        
        // Ukraine
        Kupyna512_Selector.class,
        Kupyna384_Selector.class,
        Kupyna256_Selector.class,
        
        // China        
        SM3_Selector.class,

        // NIST Lightweight Cryptography competition (2019–2023)
        // =====================================================

        // final round
        // -----------
        AsconHash_Selector.class,
        AsconHasha_Selector.class,
        AsconXof_Selector.class,
        AsconXofA_Selector.class,

        Esch_Selector.class,
        PhotonBeetle_Selector.class,
        RomulusH_Selector.class,
        Xoodyak_Selector.class,

        // SHA-3 competition
        // =================

        // SHA-3 competition, round 3
        // --------------------------
        Keccak224_Selector.class,
        Keccak256_Selector.class,
        Keccak288_Selector.class,
        Keccak384_Selector.class,
        Keccak512_Selector.class,

        Skein_Selector.class,

        Blake_512_Selector.class,
        Blake_384_Selector.class,
        Blake_256_Selector.class,
        Blake_224_Selector.class,

        JH512_Selector.class,
        JH384_Selector.class,
        JH256_Selector.class,
        JH224_Selector.class,
        
        Groestl512_Selector.class,
        Groestl384_Selector.class,
        Groestl256_Selector.class,
        Groestl224_Selector.class,

                
        // SHA-3 competition, round 2
        // --------------------------        
        ECHO224_Selector.class,
        ECHO256_Selector.class,
        ECHO384_Selector.class,
        ECHO512_Selector.class,
        
        Fugue224_Selector.class,
        Fugue256_Selector.class,
        Fugue384_Selector.class,
        Fugue512_Selector.class,
        
        Luffa224_Selector.class,
        Luffa256_Selector.class,
        Luffa384_Selector.class,
        Luffa512_Selector.class,

        BlueMidnightWish224_Selector.class,
        BlueMidnightWish256_Selector.class,
        BlueMidnightWish384_Selector.class,
        BlueMidnightWish512_Selector.class,

        SIMD224_Selector.class,
        SIMD256_Selector.class,
        SIMD384_Selector.class,
        SIMD512_Selector.class,

        CubeHash224_Selector.class,
        CubeHash256_Selector.class,
        CubeHash384_Selector.class,
        CubeHash512_Selector.class,

        Hamsi224_Selector.class,
        Hamsi256_Selector.class,
        Hamsi384_Selector.class,
        Hamsi512_Selector.class,

        Shabal192_Selector.class,
        Shabal224_Selector.class,
        Shabal256_Selector.class,
        Shabal384_Selector.class,
        Shabal512_Selector.class,

//        Shavite224_Selector.class,
//        Shavite256_Selector.class,
//        Shavite384_Selector.class,
//        Shavite512_Selector.class,


        // pre-SHA-3-competition workshop suggestions
        // ------------------------------------------
        FORK256_Selector.class,
        DHA256_Selector.class,
        VSH_Selector.class,

        
        // cryptographic hash functions
        // ============================
        // K12
        KangarooTwelve_Selector.class,
        // M14
        MarsupilamiFourteen_Selector.class,

        // BLAKE
        Blake3_Selector.class,
        Blake2b_Selector.class,
        Blake2s_Selector.class,
        Blake2sp_Selector.class,
        Blake2bp_Selector.class,
       
        // Tiger
        Tiger2_Selector.class,
        Tiger_Selector.class,
        Tiger160_Selector.class,
        Tiger128_Selector.class,
        TTH_Selector.class,
        Tiger_PHP_flavour_4_rounds_Selector.class,
        // not stable enough with other algos except Tiger and Tiger2
        //TreeHash_Selector.class,
        
        // Whirlpool        
        Whirlpool2_Selector.class,
        Whirlpool1_Selector.class,
        Whirlpool0_Selector.class,

        // Ripemd        
        Ripemd160_Selector.class,
        Ripemd128_Selector.class,
        Ripemd256_Selector.class,
        Ripemd320_Selector.class,

        // HAVAL
        Haval_Selector.class,
        
        // Radiogatun
        Radiogatun64_Selector.class,
        Radiogatun32_Selector.class,

        // MD's
        MD5_Selector.class,
        MD4_Selector.class,
        MD2_Selector.class,

        // ed2k
        Ed2k_Selector.class,
        
        // SHA0        
        SHA0_Selector.class,

        // MDC2
        MDC2_Selector.class,
        
        // Panama
        Panama_Selector.class,
        
        // Haraka v2 is a secure and efficient short-input (256 or 512 bits) hash function
        // means: the input size must be exactly 32 bytes for Haraka256 resp. 64 bytes for Haraka512
        // which makes it less attractive for Jacksum, because Jacksum supports only hash functions
        // that convert variable length input into fixed length hashes.
        // Haraka256_Selector.class,
        // Haraka512_Selector.class,
        
        
        // Non-cryptographic hash functions
        // ================================
        
        // Checksums
        // ---------
        Adler32_Selector.class,
        Cksum_Selector.class,
        SumBSD_Selector.class,
        SumSysV_Selector.class,
        Elf_Selector.class,
        FNV0_Selector.class,
        FNV1_Selector.class,
        FNV1a_Selector.class,
        Fletcher16_Selector.class,
        CksumMinix_Selector.class,
        SumMinix_Selector.class,
        Sum64_Selector.class,
        Sum56_Selector.class,
        Sum48_Selector.class,
        Sum40_Selector.class,
        Sum32_Selector.class,
        Sum24_Selector.class,
        Sum16_Selector.class,
        Sum8_Selector.class,
        Xor8_Selector.class,
        Joaat32_Selector.class,
        xxHash32_Selector.class,
        PrngHash_Selector.class,
        PrngHashGeneric_Selector.class,
        AstStrsum_Selector.class,

        // CRCs
        // ----   
        CRCGeneric_Selector.class,

        CRC82darc_Selector.class,

        CRC64_Selector.class,
        CRC64_ECMA182_Selector.class,
        CRC64_GO_Selector.class,
        CRC64xz_Selector.class,
        
        CRC32_Selector.class,
        CRC32_PHP_Selector.class,
        CRC32ubi_Selector.class,
        CRC32fddi_Selector.class,
        CRC32bzip2_Selector.class,
        CRC32mpeg2_Selector.class,
        CRC32c_Selector.class,
        
        CRC24_Selector.class,
        
        CRC16_Selector.class,
        CRC16Minix_Selector.class,
        FCS16_Selector.class,
        
        CRC8_Selector.class,

        // special selectors
        None_Selector.class,
        Read_Selector.class,
        AllAlgorithms_Selector.class

    };
}
