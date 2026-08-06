/*


  Jacksum 4.0.0 - a checksum/hash tool written in Java
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
/*

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

  */
package net.jacksum.multicore.manyalgos;

import java.io.*;
import java.util.*;

import net.jacksum.HashFunctionFactory;
import net.jacksum.JacksumAPI;
import net.jacksum.algorithms.AbstractChecksum;

/**
 * @author Federico Tello Gentile
 * contributor: Johann N. Loefflmann
 */
public class HashAlgorithm implements Comparable<HashAlgorithm> {

    private static final Map<String, Integer> WEIGHTS = new HashMap<>(690); // max. number of entries/load factor = 512/0.75=683
    private static final Map<String, Integer> WEIGHTS_ALIASES = new HashMap<>(350);
    private final String name;
    private final int weight;
    private final AbstractChecksum cs;

    /*
     * This weight information is used to balance the load among different
     * worker threads.
     * To regenerate this information use the main method in this class.
     * java -classpath jacksum.jar net.jacksum.multicore.manyalgos.HashAlgorithm <20 MB+ file> [algoname]...
     */
    static {

        // GENERATION START
        WEIGHTS.put("adler32", 7);
        WEIGHTS.put("ascon-hash", 28);
        WEIGHTS.put("ascon-hasha", 23);
        WEIGHTS.put("ascon-xof", 34);
        WEIGHTS.put("ascon-xofa", 22);
        WEIGHTS.put("aststrsum", 53);
        WEIGHTS.put("blake224", 227);
        WEIGHTS.put("blake256", 225);
        WEIGHTS.put("blake2b-104", 59);
        WEIGHTS.put("blake2b-112", 47);
        WEIGHTS.put("blake2b-120", 42);
        WEIGHTS.put("blake2b-128", 42);
        WEIGHTS.put("blake2b-136", 50);
        WEIGHTS.put("blake2b-144", 42);
        WEIGHTS.put("blake2b-152", 44);
        WEIGHTS.put("blake2b-16", 55);
        WEIGHTS.put("blake2b-160", 43);
        WEIGHTS.put("blake2b-168", 43);
        WEIGHTS.put("blake2b-176", 42);
        WEIGHTS.put("blake2b-184", 44);
        WEIGHTS.put("blake2b-192", 44);
        WEIGHTS.put("blake2b-200", 45);
        WEIGHTS.put("blake2b-208", 43);
        WEIGHTS.put("blake2b-216", 42);
        WEIGHTS.put("blake2b-224", 44);
        WEIGHTS.put("blake2b-232", 42);
        WEIGHTS.put("blake2b-24", 42);
        WEIGHTS.put("blake2b-240", 42);
        WEIGHTS.put("blake2b-248", 44);
        WEIGHTS.put("blake2b-256", 43);
        WEIGHTS.put("blake2b-264", 42);
        WEIGHTS.put("blake2b-272", 42);
        WEIGHTS.put("blake2b-280", 43);
        WEIGHTS.put("blake2b-288", 43);
        WEIGHTS.put("blake2b-296", 42);
        WEIGHTS.put("blake2b-304", 43);
        WEIGHTS.put("blake2b-312", 42);
        WEIGHTS.put("blake2b-32", 43);
        WEIGHTS.put("blake2b-320", 47);
        WEIGHTS.put("blake2b-328", 43);
        WEIGHTS.put("blake2b-336", 42);
        WEIGHTS.put("blake2b-344", 49);
        WEIGHTS.put("blake2b-352", 53);
        WEIGHTS.put("blake2b-360", 42);
        WEIGHTS.put("blake2b-368", 42);
        WEIGHTS.put("blake2b-376", 53);
        WEIGHTS.put("blake2b-384", 42);
        WEIGHTS.put("blake2b-392", 43);
        WEIGHTS.put("blake2b-40", 53);
        WEIGHTS.put("blake2b-400", 43);
        WEIGHTS.put("blake2b-408", 42);
        WEIGHTS.put("blake2b-416", 42);
        WEIGHTS.put("blake2b-424", 50);
        WEIGHTS.put("blake2b-432", 44);
        WEIGHTS.put("blake2b-440", 44);
        WEIGHTS.put("blake2b-448", 42);
        WEIGHTS.put("blake2b-456", 42);
        WEIGHTS.put("blake2b-464", 44);
        WEIGHTS.put("blake2b-472", 50);
        WEIGHTS.put("blake2b-48", 44);
        WEIGHTS.put("blake2b-480", 42);
        WEIGHTS.put("blake2b-488", 42);
        WEIGHTS.put("blake2b-496", 43);
        WEIGHTS.put("blake2b-504", 44);
        WEIGHTS.put("blake2b-512", 43);
        WEIGHTS.put("blake2b-56", 43);
        WEIGHTS.put("blake2b-64", 43);
        WEIGHTS.put("blake2b-72", 42);
        WEIGHTS.put("blake2b-8", 43);
        WEIGHTS.put("blake2b-80", 45);
        WEIGHTS.put("blake2b-88", 43);
        WEIGHTS.put("blake2b-96", 44);
        WEIGHTS.put("blake2bp", 45);
        WEIGHTS.put("blake2s-104", 69);
        WEIGHTS.put("blake2s-112", 68);
        WEIGHTS.put("blake2s-120", 68);
        WEIGHTS.put("blake2s-128", 69);
        WEIGHTS.put("blake2s-136", 68);
        WEIGHTS.put("blake2s-144", 69);
        WEIGHTS.put("blake2s-152", 67);
        WEIGHTS.put("blake2s-16", 68);
        WEIGHTS.put("blake2s-160", 68);
        WEIGHTS.put("blake2s-168", 68);
        WEIGHTS.put("blake2s-176", 69);
        WEIGHTS.put("blake2s-184", 68);
        WEIGHTS.put("blake2s-192", 69);
        WEIGHTS.put("blake2s-200", 68);
        WEIGHTS.put("blake2s-208", 69);
        WEIGHTS.put("blake2s-216", 68);
        WEIGHTS.put("blake2s-224", 68);
        WEIGHTS.put("blake2s-232", 67);
        WEIGHTS.put("blake2s-24", 73);
        WEIGHTS.put("blake2s-240", 68);
        WEIGHTS.put("blake2s-248", 68);
        WEIGHTS.put("blake2s-256", 68);
        WEIGHTS.put("blake2s-32", 73);
        WEIGHTS.put("blake2s-40", 70);
        WEIGHTS.put("blake2s-48", 68);
        WEIGHTS.put("blake2s-56", 68);
        WEIGHTS.put("blake2s-64", 67);
        WEIGHTS.put("blake2s-72", 69);
        WEIGHTS.put("blake2s-8", 68);
        WEIGHTS.put("blake2s-80", 69);
        WEIGHTS.put("blake2s-88", 70);
        WEIGHTS.put("blake2s-96", 70);
        WEIGHTS.put("blake2sp", 74);
        WEIGHTS.put("blake3", 86);
        WEIGHTS.put("blake384", 161);
        WEIGHTS.put("blake512", 160);
        WEIGHTS.put("bluemidnightwish224", 81);
        WEIGHTS.put("bluemidnightwish256", 81);
        WEIGHTS.put("bluemidnightwish384", 28);
        WEIGHTS.put("bluemidnightwish512", 29);
        WEIGHTS.put("cksum", 89);
        WEIGHTS.put("cksum_minix", 90);
        WEIGHTS.put("crc16", 81);
        WEIGHTS.put("crc16_minix", 90);
        WEIGHTS.put("crc24", 106);
        WEIGHTS.put("crc32", 5);
        WEIGHTS.put("crc32_bzip2", 107);
        WEIGHTS.put("crc32_fddi", 97);
        WEIGHTS.put("crc32_go-koopman", 95);
        WEIGHTS.put("crc32_mpeg2", 105);
        WEIGHTS.put("crc32_php", 108);
        WEIGHTS.put("crc32_ubi", 99);
        WEIGHTS.put("crc32c", 5);
        WEIGHTS.put("crc64", 73);
        WEIGHTS.put("crc64_ecma", 104);
        WEIGHTS.put("crc64_go-iso", 97);
        WEIGHTS.put("crc64_nvme", 105);
        WEIGHTS.put("crc64_xz", 100);
        WEIGHTS.put("crc8", 66);
        WEIGHTS.put("crc82_darc", 636);
        WEIGHTS.put("cubehash224", 161);
        WEIGHTS.put("cubehash256", 153);
        WEIGHTS.put("cubehash384", 152);
        WEIGHTS.put("cubehash512", 153);
        WEIGHTS.put("dha256", 118);
        WEIGHTS.put("echo224", 172);
        WEIGHTS.put("echo256", 172);
        WEIGHTS.put("echo384", 317);
        WEIGHTS.put("echo512", 316);
        WEIGHTS.put("ed2k", 44);
        WEIGHTS.put("elf", 61);
        WEIGHTS.put("esch256", 28);
        WEIGHTS.put("esch384", 48);
        WEIGHTS.put("fcs16", 90);
        WEIGHTS.put("fletcher16", 90);
        WEIGHTS.put("fnv-0_1024", 6991);
        WEIGHTS.put("fnv-0_128", 1336);
        WEIGHTS.put("fnv-0_256", 1986);
        WEIGHTS.put("fnv-0_32", 35);
        WEIGHTS.put("fnv-0_512", 3142);
        WEIGHTS.put("fnv-0_64", 71);
        WEIGHTS.put("fnv-1_1024", 7081);
        WEIGHTS.put("fnv-1_128", 1375);
        WEIGHTS.put("fnv-1_256", 1875);
        WEIGHTS.put("fnv-1_32", 37);
        WEIGHTS.put("fnv-1_512", 3168);
        WEIGHTS.put("fnv-1_64", 72);
        WEIGHTS.put("fnv-1a_1024", 7309);
        WEIGHTS.put("fnv-1a_128", 1388);
        WEIGHTS.put("fnv-1a_256", 1904);
        WEIGHTS.put("fnv-1a_32", 36);
        WEIGHTS.put("fnv-1a_512", 3300);
        WEIGHTS.put("fnv-1a_64", 72);
        WEIGHTS.put("fork256", 52);
        WEIGHTS.put("fugue224", 177);
        WEIGHTS.put("fugue256", 172);
        WEIGHTS.put("fugue384", 279);
        WEIGHTS.put("fugue512", 365);
        WEIGHTS.put("gost", 1785);
        WEIGHTS.put("gost:crypto-pro", 1786);
        WEIGHTS.put("groestl-224", 238);
        WEIGHTS.put("groestl-256", 241);
        WEIGHTS.put("groestl-384", 442);
        WEIGHTS.put("groestl-512", 439);
        WEIGHTS.put("hamsi224", 197);
        WEIGHTS.put("hamsi256", 199);
        WEIGHTS.put("hamsi384", 540);
        WEIGHTS.put("hamsi512", 539);
        WEIGHTS.put("has160", 68);
        WEIGHTS.put("haval_128_3", 153);
        WEIGHTS.put("haval_128_4", 218);
        WEIGHTS.put("haval_128_5", 259);
        WEIGHTS.put("haval_160_3", 159);
        WEIGHTS.put("haval_160_4", 219);
        WEIGHTS.put("haval_160_5", 268);
        WEIGHTS.put("haval_192_3", 153);
        WEIGHTS.put("haval_192_4", 218);
        WEIGHTS.put("haval_192_5", 264);
        WEIGHTS.put("haval_224_3", 156);
        WEIGHTS.put("haval_224_4", 219);
        WEIGHTS.put("haval_224_5", 262);
        WEIGHTS.put("haval_256_3", 156);
        WEIGHTS.put("haval_256_4", 220);
        WEIGHTS.put("haval_256_5", 265);
        WEIGHTS.put("hmac:blake224", 228);
        WEIGHTS.put("hmac:blake256", 224);
        WEIGHTS.put("hmac:blake2b-104", 42);
        WEIGHTS.put("hmac:blake2b-112", 44);
        WEIGHTS.put("hmac:blake2b-120", 43);
        WEIGHTS.put("hmac:blake2b-128", 43);
        WEIGHTS.put("hmac:blake2b-136", 42);
        WEIGHTS.put("hmac:blake2b-144", 46);
        WEIGHTS.put("hmac:blake2b-152", 42);
        WEIGHTS.put("hmac:blake2b-16", 49);
        WEIGHTS.put("hmac:blake2b-160", 43);
        WEIGHTS.put("hmac:blake2b-168", 43);
        WEIGHTS.put("hmac:blake2b-176", 44);
        WEIGHTS.put("hmac:blake2b-184", 49);
        WEIGHTS.put("hmac:blake2b-192", 42);
        WEIGHTS.put("hmac:blake2b-200", 42);
        WEIGHTS.put("hmac:blake2b-208", 43);
        WEIGHTS.put("hmac:blake2b-216", 42);
        WEIGHTS.put("hmac:blake2b-224", 42);
        WEIGHTS.put("hmac:blake2b-232", 46);
        WEIGHTS.put("hmac:blake2b-24", 44);
        WEIGHTS.put("hmac:blake2b-240", 42);
        WEIGHTS.put("hmac:blake2b-248", 42);
        WEIGHTS.put("hmac:blake2b-256", 48);
        WEIGHTS.put("hmac:blake2b-264", 57);
        WEIGHTS.put("hmac:blake2b-272", 44);
        WEIGHTS.put("hmac:blake2b-280", 43);
        WEIGHTS.put("hmac:blake2b-288", 50);
        WEIGHTS.put("hmac:blake2b-296", 45);
        WEIGHTS.put("hmac:blake2b-304", 43);
        WEIGHTS.put("hmac:blake2b-312", 42);
        WEIGHTS.put("hmac:blake2b-32", 43);
        WEIGHTS.put("hmac:blake2b-320", 42);
        WEIGHTS.put("hmac:blake2b-328", 42);
        WEIGHTS.put("hmac:blake2b-336", 42);
        WEIGHTS.put("hmac:blake2b-344", 43);
        WEIGHTS.put("hmac:blake2b-352", 48);
        WEIGHTS.put("hmac:blake2b-360", 42);
        WEIGHTS.put("hmac:blake2b-368", 42);
        WEIGHTS.put("hmac:blake2b-376", 42);
        WEIGHTS.put("hmac:blake2b-384", 57);
        WEIGHTS.put("hmac:blake2b-392", 43);
        WEIGHTS.put("hmac:blake2b-40", 44);
        WEIGHTS.put("hmac:blake2b-400", 44);
        WEIGHTS.put("hmac:blake2b-408", 43);
        WEIGHTS.put("hmac:blake2b-416", 43);
        WEIGHTS.put("hmac:blake2b-424", 45);
        WEIGHTS.put("hmac:blake2b-432", 43);
        WEIGHTS.put("hmac:blake2b-440", 42);
        WEIGHTS.put("hmac:blake2b-448", 42);
        WEIGHTS.put("hmac:blake2b-456", 42);
        WEIGHTS.put("hmac:blake2b-464", 42);
        WEIGHTS.put("hmac:blake2b-472", 60);
        WEIGHTS.put("hmac:blake2b-48", 43);
        WEIGHTS.put("hmac:blake2b-480", 42);
        WEIGHTS.put("hmac:blake2b-488", 42);
        WEIGHTS.put("hmac:blake2b-496", 48);
        WEIGHTS.put("hmac:blake2b-504", 45);
        WEIGHTS.put("hmac:blake2b-512", 45);
        WEIGHTS.put("hmac:blake2b-56", 43);
        WEIGHTS.put("hmac:blake2b-64", 43);
        WEIGHTS.put("hmac:blake2b-72", 42);
        WEIGHTS.put("hmac:blake2b-8", 45);
        WEIGHTS.put("hmac:blake2b-80", 42);
        WEIGHTS.put("hmac:blake2b-88", 43);
        WEIGHTS.put("hmac:blake2b-96", 43);
        WEIGHTS.put("hmac:blake2s-104", 70);
        WEIGHTS.put("hmac:blake2s-112", 68);
        WEIGHTS.put("hmac:blake2s-120", 70);
        WEIGHTS.put("hmac:blake2s-128", 69);
        WEIGHTS.put("hmac:blake2s-136", 68);
        WEIGHTS.put("hmac:blake2s-144", 68);
        WEIGHTS.put("hmac:blake2s-152", 71);
        WEIGHTS.put("hmac:blake2s-16", 75);
        WEIGHTS.put("hmac:blake2s-160", 67);
        WEIGHTS.put("hmac:blake2s-168", 69);
        WEIGHTS.put("hmac:blake2s-176", 67);
        WEIGHTS.put("hmac:blake2s-184", 68);
        WEIGHTS.put("hmac:blake2s-192", 69);
        WEIGHTS.put("hmac:blake2s-200", 82);
        WEIGHTS.put("hmac:blake2s-208", 90);
        WEIGHTS.put("hmac:blake2s-216", 79);
        WEIGHTS.put("hmac:blake2s-224", 72);
        WEIGHTS.put("hmac:blake2s-232", 68);
        WEIGHTS.put("hmac:blake2s-24", 76);
        WEIGHTS.put("hmac:blake2s-240", 68);
        WEIGHTS.put("hmac:blake2s-248", 68);
        WEIGHTS.put("hmac:blake2s-256", 69);
        WEIGHTS.put("hmac:blake2s-32", 68);
        WEIGHTS.put("hmac:blake2s-40", 68);
        WEIGHTS.put("hmac:blake2s-48", 69);
        WEIGHTS.put("hmac:blake2s-56", 75);
        WEIGHTS.put("hmac:blake2s-64", 69);
        WEIGHTS.put("hmac:blake2s-72", 76);
        WEIGHTS.put("hmac:blake2s-8", 70);
        WEIGHTS.put("hmac:blake2s-80", 75);
        WEIGHTS.put("hmac:blake2s-88", 75);
        WEIGHTS.put("hmac:blake2s-96", 69);
        WEIGHTS.put("hmac:blake2sp", 74);
        WEIGHTS.put("hmac:blake384", 161);
        WEIGHTS.put("hmac:blake512", 161);
        WEIGHTS.put("hmac:bluemidnightwish224", 80);
        WEIGHTS.put("hmac:bluemidnightwish256", 82);
        WEIGHTS.put("hmac:bluemidnightwish384", 28);
        WEIGHTS.put("hmac:bluemidnightwish512", 29);
        WEIGHTS.put("hmac:dha256", 107);
        WEIGHTS.put("hmac:echo224", 172);
        WEIGHTS.put("hmac:echo256", 174);
        WEIGHTS.put("hmac:echo384", 315);
        WEIGHTS.put("hmac:echo512", 315);
        WEIGHTS.put("hmac:fork256", 51);
        WEIGHTS.put("hmac:gost", 1774);
        WEIGHTS.put("hmac:gost:crypto-pro", 1785);
        WEIGHTS.put("hmac:groestl-224", 240);
        WEIGHTS.put("hmac:groestl-256", 242);
        WEIGHTS.put("hmac:groestl-384", 444);
        WEIGHTS.put("hmac:groestl-512", 442);
        WEIGHTS.put("hmac:has160", 75);
        WEIGHTS.put("hmac:haval_128_3", 156);
        WEIGHTS.put("hmac:haval_128_4", 222);
        WEIGHTS.put("hmac:haval_128_5", 263);
        WEIGHTS.put("hmac:haval_160_3", 155);
        WEIGHTS.put("hmac:haval_160_4", 217);
        WEIGHTS.put("hmac:haval_160_5", 261);
        WEIGHTS.put("hmac:haval_192_3", 156);
        WEIGHTS.put("hmac:haval_192_4", 219);
        WEIGHTS.put("hmac:haval_192_5", 264);
        WEIGHTS.put("hmac:haval_224_3", 155);
        WEIGHTS.put("hmac:haval_224_4", 219);
        WEIGHTS.put("hmac:haval_224_5", 262);
        WEIGHTS.put("hmac:haval_256_3", 156);
        WEIGHTS.put("hmac:haval_256_4", 219);
        WEIGHTS.put("hmac:haval_256_5", 263);
        WEIGHTS.put("hmac:jh224", 250);
        WEIGHTS.put("hmac:jh256", 252);
        WEIGHTS.put("hmac:jh384", 253);
        WEIGHTS.put("hmac:jh512", 250);
        WEIGHTS.put("hmac:kangarootwelve", 34);
        WEIGHTS.put("hmac:keccak224", 44);
        WEIGHTS.put("hmac:keccak256", 46);
        WEIGHTS.put("hmac:keccak288", 50);
        WEIGHTS.put("hmac:keccak384", 57);
        WEIGHTS.put("hmac:keccak512", 81);
        WEIGHTS.put("hmac:kupyna-256", 441);
        WEIGHTS.put("hmac:kupyna-384", 593);
        WEIGHTS.put("hmac:kupyna-512", 597);
        WEIGHTS.put("hmac:lsh-256-224", 84);
        WEIGHTS.put("hmac:lsh-256-256", 83);
        WEIGHTS.put("hmac:lsh-512-224", 51);
        WEIGHTS.put("hmac:lsh-512-256", 49);
        WEIGHTS.put("hmac:lsh-512-384", 53);
        WEIGHTS.put("hmac:lsh-512-512", 49);
        WEIGHTS.put("hmac:marsupilamifourteen", 45);
        WEIGHTS.put("hmac:md2", 3237);
        WEIGHTS.put("hmac:md4", 45);
        WEIGHTS.put("hmac:md5", 40);
        WEIGHTS.put("hmac:mdc2", 3818);
        WEIGHTS.put("hmac:panama", 27);
        WEIGHTS.put("hmac:ripemd128", 131);
        WEIGHTS.put("hmac:ripemd160", 250);
        WEIGHTS.put("hmac:ripemd256", 77);
        WEIGHTS.put("hmac:ripemd320", 249);
        WEIGHTS.put("hmac:sha-1", 13);
        WEIGHTS.put("hmac:sha-224", 100);
        WEIGHTS.put("hmac:sha-256", 13);
        WEIGHTS.put("hmac:sha-384", 22);
        WEIGHTS.put("hmac:sha-512", 22);
        WEIGHTS.put("hmac:sha-512/224", 22);
        WEIGHTS.put("hmac:sha-512/256", 22);
        WEIGHTS.put("hmac:sha0", 74);
        WEIGHTS.put("hmac:sha3-224", 33);
        WEIGHTS.put("hmac:sha3-256", 34);
        WEIGHTS.put("hmac:sha3-384", 46);
        WEIGHTS.put("hmac:sha3-512", 64);
        WEIGHTS.put("hmac:shabal192", 70);
        WEIGHTS.put("hmac:shabal224", 70);
        WEIGHTS.put("hmac:shabal256", 74);
        WEIGHTS.put("hmac:shabal384", 75);
        WEIGHTS.put("hmac:shabal512", 76);
        WEIGHTS.put("hmac:shake128", 38);
        WEIGHTS.put("hmac:shake256", 46);
        WEIGHTS.put("hmac:simd224", 251);
        WEIGHTS.put("hmac:simd256", 253);
        WEIGHTS.put("hmac:simd384", 4628);
        WEIGHTS.put("hmac:simd512", 4627);
        WEIGHTS.put("hmac:skein-1024-1000", 43);
        WEIGHTS.put("hmac:skein-1024-1008", 45);
        WEIGHTS.put("hmac:skein-1024-1016", 43);
        WEIGHTS.put("hmac:skein-1024-1024", 43);
        WEIGHTS.put("hmac:skein-1024-104", 44);
        WEIGHTS.put("hmac:skein-1024-112", 43);
        WEIGHTS.put("hmac:skein-1024-120", 43);
        WEIGHTS.put("hmac:skein-1024-128", 44);
        WEIGHTS.put("hmac:skein-1024-136", 50);
        WEIGHTS.put("hmac:skein-1024-144", 43);
        WEIGHTS.put("hmac:skein-1024-152", 44);
        WEIGHTS.put("hmac:skein-1024-16", 43);
        WEIGHTS.put("hmac:skein-1024-160", 43);
        WEIGHTS.put("hmac:skein-1024-168", 43);
        WEIGHTS.put("hmac:skein-1024-176", 44);
        WEIGHTS.put("hmac:skein-1024-184", 43);
        WEIGHTS.put("hmac:skein-1024-192", 43);
        WEIGHTS.put("hmac:skein-1024-200", 43);
        WEIGHTS.put("hmac:skein-1024-208", 43);
        WEIGHTS.put("hmac:skein-1024-216", 44);
        WEIGHTS.put("hmac:skein-1024-224", 44);
        WEIGHTS.put("hmac:skein-1024-232", 43);
        WEIGHTS.put("hmac:skein-1024-24", 44);
        WEIGHTS.put("hmac:skein-1024-240", 44);
        WEIGHTS.put("hmac:skein-1024-248", 44);
        WEIGHTS.put("hmac:skein-1024-256", 46);
        WEIGHTS.put("hmac:skein-1024-264", 43);
        WEIGHTS.put("hmac:skein-1024-272", 44);
        WEIGHTS.put("hmac:skein-1024-280", 43);
        WEIGHTS.put("hmac:skein-1024-288", 44);
        WEIGHTS.put("hmac:skein-1024-296", 43);
        WEIGHTS.put("hmac:skein-1024-304", 44);
        WEIGHTS.put("hmac:skein-1024-312", 43);
        WEIGHTS.put("hmac:skein-1024-32", 44);
        WEIGHTS.put("hmac:skein-1024-320", 44);
        WEIGHTS.put("hmac:skein-1024-328", 43);
        WEIGHTS.put("hmac:skein-1024-336", 44);
        WEIGHTS.put("hmac:skein-1024-344", 43);
        WEIGHTS.put("hmac:skein-1024-352", 45);
        WEIGHTS.put("hmac:skein-1024-360", 43);
        WEIGHTS.put("hmac:skein-1024-368", 43);
        WEIGHTS.put("hmac:skein-1024-376", 45);
        WEIGHTS.put("hmac:skein-1024-384", 44);
        WEIGHTS.put("hmac:skein-1024-392", 44);
        WEIGHTS.put("hmac:skein-1024-40", 43);
        WEIGHTS.put("hmac:skein-1024-400", 44);
        WEIGHTS.put("hmac:skein-1024-408", 43);
        WEIGHTS.put("hmac:skein-1024-416", 44);
        WEIGHTS.put("hmac:skein-1024-424", 47);
        WEIGHTS.put("hmac:skein-1024-432", 43);
        WEIGHTS.put("hmac:skein-1024-440", 45);
        WEIGHTS.put("hmac:skein-1024-448", 43);
        WEIGHTS.put("hmac:skein-1024-456", 43);
        WEIGHTS.put("hmac:skein-1024-464", 44);
        WEIGHTS.put("hmac:skein-1024-472", 43);
        WEIGHTS.put("hmac:skein-1024-48", 44);
        WEIGHTS.put("hmac:skein-1024-480", 43);
        WEIGHTS.put("hmac:skein-1024-488", 43);
        WEIGHTS.put("hmac:skein-1024-496", 44);
        WEIGHTS.put("hmac:skein-1024-504", 44);
        WEIGHTS.put("hmac:skein-1024-512", 44);
        WEIGHTS.put("hmac:skein-1024-520", 44);
        WEIGHTS.put("hmac:skein-1024-528", 44);
        WEIGHTS.put("hmac:skein-1024-536", 51);
        WEIGHTS.put("hmac:skein-1024-544", 46);
        WEIGHTS.put("hmac:skein-1024-552", 43);
        WEIGHTS.put("hmac:skein-1024-56", 43);
        WEIGHTS.put("hmac:skein-1024-560", 44);
        WEIGHTS.put("hmac:skein-1024-568", 43);
        WEIGHTS.put("hmac:skein-1024-576", 43);
        WEIGHTS.put("hmac:skein-1024-584", 43);
        WEIGHTS.put("hmac:skein-1024-592", 44);
        WEIGHTS.put("hmac:skein-1024-600", 43);
        WEIGHTS.put("hmac:skein-1024-608", 44);
        WEIGHTS.put("hmac:skein-1024-616", 44);
        WEIGHTS.put("hmac:skein-1024-624", 44);
        WEIGHTS.put("hmac:skein-1024-632", 44);
        WEIGHTS.put("hmac:skein-1024-64", 44);
        WEIGHTS.put("hmac:skein-1024-640", 44);
        WEIGHTS.put("hmac:skein-1024-648", 43);
        WEIGHTS.put("hmac:skein-1024-656", 44);
        WEIGHTS.put("hmac:skein-1024-664", 44);
        WEIGHTS.put("hmac:skein-1024-672", 44);
        WEIGHTS.put("hmac:skein-1024-680", 44);
        WEIGHTS.put("hmac:skein-1024-688", 43);
        WEIGHTS.put("hmac:skein-1024-696", 44);
        WEIGHTS.put("hmac:skein-1024-704", 43);
        WEIGHTS.put("hmac:skein-1024-712", 43);
        WEIGHTS.put("hmac:skein-1024-72", 51);
        WEIGHTS.put("hmac:skein-1024-720", 44);
        WEIGHTS.put("hmac:skein-1024-728", 43);
        WEIGHTS.put("hmac:skein-1024-736", 54);
        WEIGHTS.put("hmac:skein-1024-744", 50);
        WEIGHTS.put("hmac:skein-1024-752", 44);
        WEIGHTS.put("hmac:skein-1024-760", 45);
        WEIGHTS.put("hmac:skein-1024-768", 44);
        WEIGHTS.put("hmac:skein-1024-776", 50);
        WEIGHTS.put("hmac:skein-1024-784", 43);
        WEIGHTS.put("hmac:skein-1024-792", 48);
        WEIGHTS.put("hmac:skein-1024-8", 43);
        WEIGHTS.put("hmac:skein-1024-80", 43);
        WEIGHTS.put("hmac:skein-1024-800", 46);
        WEIGHTS.put("hmac:skein-1024-808", 44);
        WEIGHTS.put("hmac:skein-1024-816", 43);
        WEIGHTS.put("hmac:skein-1024-824", 46);
        WEIGHTS.put("hmac:skein-1024-832", 43);
        WEIGHTS.put("hmac:skein-1024-840", 44);
        WEIGHTS.put("hmac:skein-1024-848", 44);
        WEIGHTS.put("hmac:skein-1024-856", 43);
        WEIGHTS.put("hmac:skein-1024-864", 43);
        WEIGHTS.put("hmac:skein-1024-872", 43);
        WEIGHTS.put("hmac:skein-1024-88", 44);
        WEIGHTS.put("hmac:skein-1024-880", 43);
        WEIGHTS.put("hmac:skein-1024-888", 43);
        WEIGHTS.put("hmac:skein-1024-896", 44);
        WEIGHTS.put("hmac:skein-1024-904", 44);
        WEIGHTS.put("hmac:skein-1024-912", 44);
        WEIGHTS.put("hmac:skein-1024-920", 43);
        WEIGHTS.put("hmac:skein-1024-928", 43);
        WEIGHTS.put("hmac:skein-1024-936", 43);
        WEIGHTS.put("hmac:skein-1024-944", 44);
        WEIGHTS.put("hmac:skein-1024-952", 43);
        WEIGHTS.put("hmac:skein-1024-96", 44);
        WEIGHTS.put("hmac:skein-1024-960", 44);
        WEIGHTS.put("hmac:skein-1024-968", 43);
        WEIGHTS.put("hmac:skein-1024-976", 43);
        WEIGHTS.put("hmac:skein-1024-984", 43);
        WEIGHTS.put("hmac:skein-1024-992", 47);
        WEIGHTS.put("hmac:skein-256-104", 64);
        WEIGHTS.put("hmac:skein-256-112", 63);
        WEIGHTS.put("hmac:skein-256-120", 65);
        WEIGHTS.put("hmac:skein-256-128", 64);
        WEIGHTS.put("hmac:skein-256-136", 64);
        WEIGHTS.put("hmac:skein-256-144", 63);
        WEIGHTS.put("hmac:skein-256-152", 64);
        WEIGHTS.put("hmac:skein-256-16", 69);
        WEIGHTS.put("hmac:skein-256-160", 63);
        WEIGHTS.put("hmac:skein-256-168", 63);
        WEIGHTS.put("hmac:skein-256-176", 63);
        WEIGHTS.put("hmac:skein-256-184", 64);
        WEIGHTS.put("hmac:skein-256-192", 64);
        WEIGHTS.put("hmac:skein-256-200", 63);
        WEIGHTS.put("hmac:skein-256-208", 64);
        WEIGHTS.put("hmac:skein-256-216", 63);
        WEIGHTS.put("hmac:skein-256-224", 63);
        WEIGHTS.put("hmac:skein-256-232", 63);
        WEIGHTS.put("hmac:skein-256-24", 65);
        WEIGHTS.put("hmac:skein-256-240", 64);
        WEIGHTS.put("hmac:skein-256-248", 63);
        WEIGHTS.put("hmac:skein-256-256", 63);
        WEIGHTS.put("hmac:skein-256-32", 63);
        WEIGHTS.put("hmac:skein-256-40", 64);
        WEIGHTS.put("hmac:skein-256-48", 64);
        WEIGHTS.put("hmac:skein-256-56", 63);
        WEIGHTS.put("hmac:skein-256-64", 63);
        WEIGHTS.put("hmac:skein-256-72", 64);
        WEIGHTS.put("hmac:skein-256-8", 64);
        WEIGHTS.put("hmac:skein-256-80", 63);
        WEIGHTS.put("hmac:skein-256-88", 63);
        WEIGHTS.put("hmac:skein-256-96", 64);
        WEIGHTS.put("hmac:skein-512-104", 49);
        WEIGHTS.put("hmac:skein-512-112", 49);
        WEIGHTS.put("hmac:skein-512-120", 49);
        WEIGHTS.put("hmac:skein-512-128", 49);
        WEIGHTS.put("hmac:skein-512-136", 49);
        WEIGHTS.put("hmac:skein-512-144", 49);
        WEIGHTS.put("hmac:skein-512-152", 49);
        WEIGHTS.put("hmac:skein-512-16", 48);
        WEIGHTS.put("hmac:skein-512-160", 49);
        WEIGHTS.put("hmac:skein-512-168", 48);
        WEIGHTS.put("hmac:skein-512-176", 49);
        WEIGHTS.put("hmac:skein-512-184", 50);
        WEIGHTS.put("hmac:skein-512-192", 49);
        WEIGHTS.put("hmac:skein-512-200", 49);
        WEIGHTS.put("hmac:skein-512-208", 49);
        WEIGHTS.put("hmac:skein-512-216", 48);
        WEIGHTS.put("hmac:skein-512-224", 49);
        WEIGHTS.put("hmac:skein-512-232", 49);
        WEIGHTS.put("hmac:skein-512-24", 49);
        WEIGHTS.put("hmac:skein-512-240", 49);
        WEIGHTS.put("hmac:skein-512-248", 49);
        WEIGHTS.put("hmac:skein-512-256", 51);
        WEIGHTS.put("hmac:skein-512-264", 48);
        WEIGHTS.put("hmac:skein-512-272", 55);
        WEIGHTS.put("hmac:skein-512-280", 48);
        WEIGHTS.put("hmac:skein-512-288", 49);
        WEIGHTS.put("hmac:skein-512-296", 50);
        WEIGHTS.put("hmac:skein-512-304", 49);
        WEIGHTS.put("hmac:skein-512-312", 49);
        WEIGHTS.put("hmac:skein-512-32", 48);
        WEIGHTS.put("hmac:skein-512-320", 49);
        WEIGHTS.put("hmac:skein-512-328", 48);
        WEIGHTS.put("hmac:skein-512-336", 49);
        WEIGHTS.put("hmac:skein-512-344", 49);
        WEIGHTS.put("hmac:skein-512-352", 49);
        WEIGHTS.put("hmac:skein-512-360", 49);
        WEIGHTS.put("hmac:skein-512-368", 49);
        WEIGHTS.put("hmac:skein-512-376", 49);
        WEIGHTS.put("hmac:skein-512-384", 50);
        WEIGHTS.put("hmac:skein-512-392", 48);
        WEIGHTS.put("hmac:skein-512-40", 49);
        WEIGHTS.put("hmac:skein-512-400", 50);
        WEIGHTS.put("hmac:skein-512-408", 49);
        WEIGHTS.put("hmac:skein-512-416", 49);
        WEIGHTS.put("hmac:skein-512-424", 50);
        WEIGHTS.put("hmac:skein-512-432", 49);
        WEIGHTS.put("hmac:skein-512-440", 49);
        WEIGHTS.put("hmac:skein-512-448", 49);
        WEIGHTS.put("hmac:skein-512-456", 49);
        WEIGHTS.put("hmac:skein-512-464", 49);
        WEIGHTS.put("hmac:skein-512-472", 49);
        WEIGHTS.put("hmac:skein-512-48", 49);
        WEIGHTS.put("hmac:skein-512-480", 49);
        WEIGHTS.put("hmac:skein-512-488", 49);
        WEIGHTS.put("hmac:skein-512-496", 50);
        WEIGHTS.put("hmac:skein-512-504", 48);
        WEIGHTS.put("hmac:skein-512-512", 49);
        WEIGHTS.put("hmac:skein-512-56", 49);
        WEIGHTS.put("hmac:skein-512-64", 49);
        WEIGHTS.put("hmac:skein-512-72", 48);
        WEIGHTS.put("hmac:skein-512-8", 48);
        WEIGHTS.put("hmac:skein-512-80", 48);
        WEIGHTS.put("hmac:skein-512-88", 48);
        WEIGHTS.put("hmac:skein-512-96", 48);
        WEIGHTS.put("hmac:sm3", 109);
        WEIGHTS.put("hmac:streebog256", 588);
        WEIGHTS.put("hmac:streebog512", 587);
        WEIGHTS.put("hmac:tiger", 45);
        WEIGHTS.put("hmac:tiger-128-4-php", 61);
        WEIGHTS.put("hmac:tiger-160-4-php", 61);
        WEIGHTS.put("hmac:tiger-192-4-php", 61);
        WEIGHTS.put("hmac:tiger128", 44);
        WEIGHTS.put("hmac:tiger160", 44);
        WEIGHTS.put("hmac:tiger2", 45);
        WEIGHTS.put("hmac:vsh", 17271);
        WEIGHTS.put("hmac:whirlpool0", 379);
        WEIGHTS.put("hmac:whirlpool1", 378);
        WEIGHTS.put("hmac:whirlpool2", 384);
        WEIGHTS.put("jh224", 252);
        WEIGHTS.put("jh256", 255);
        WEIGHTS.put("jh384", 251);
        WEIGHTS.put("jh512", 254);
        WEIGHTS.put("joaat", 46);
        WEIGHTS.put("kangarootwelve", 33);
        WEIGHTS.put("keccak224", 45);
        WEIGHTS.put("keccak256", 46);
        WEIGHTS.put("keccak288", 47);
        WEIGHTS.put("keccak384", 58);
        WEIGHTS.put("keccak512", 81);
        WEIGHTS.put("kupyna-256", 445);
        WEIGHTS.put("kupyna-384", 601);
        WEIGHTS.put("kupyna-512", 597);
        WEIGHTS.put("lsh-256-224", 83);
        WEIGHTS.put("lsh-256-256", 84);
        WEIGHTS.put("lsh-512-224", 50);
        WEIGHTS.put("lsh-512-256", 50);
        WEIGHTS.put("lsh-512-384", 50);
        WEIGHTS.put("lsh-512-512", 50);
        WEIGHTS.put("luffa224", 131);
        WEIGHTS.put("luffa256", 130);
        WEIGHTS.put("luffa384", 197);
        WEIGHTS.put("luffa512", 260);
        WEIGHTS.put("marsupilamifourteen", 45);
        WEIGHTS.put("md2", 3187);
        WEIGHTS.put("md4", 43);
        WEIGHTS.put("md5", 41);
        WEIGHTS.put("mdc2", 3868);
        WEIGHTS.put("panama", 25);
        WEIGHTS.put("photon-beetle", 6093);
        WEIGHTS.put("prng", 53);
        WEIGHTS.put("radiogatun:32", 36);
        WEIGHTS.put("radiogatun:64", 25);
        WEIGHTS.put("ripemd128", 132);
        WEIGHTS.put("ripemd160", 250);
        WEIGHTS.put("ripemd256", 78);
        WEIGHTS.put("ripemd320", 250);
        WEIGHTS.put("romulush", 28618);
        WEIGHTS.put("sha-1", 13);
        WEIGHTS.put("sha-224", 100);
        WEIGHTS.put("sha-256", 13);
        WEIGHTS.put("sha-384", 22);
        WEIGHTS.put("sha-512", 22);
        WEIGHTS.put("sha-512/224", 22);
        WEIGHTS.put("sha-512/256", 22);
        WEIGHTS.put("sha0", 73);
        WEIGHTS.put("sha3-224", 33);
        WEIGHTS.put("sha3-256", 35);
        WEIGHTS.put("sha3-384", 47);
        WEIGHTS.put("sha3-512", 65);
        WEIGHTS.put("shabal192", 72);
        WEIGHTS.put("shabal224", 71);
        WEIGHTS.put("shabal256", 70);
        WEIGHTS.put("shabal384", 71);
        WEIGHTS.put("shabal512", 72);
        WEIGHTS.put("shake128", 39);
        WEIGHTS.put("shake256", 45);
        WEIGHTS.put("simd224", 253);
        WEIGHTS.put("simd256", 254);
        WEIGHTS.put("simd384", 4625);
        WEIGHTS.put("simd512", 4593);
        WEIGHTS.put("skein-1024-1000", 44);
        WEIGHTS.put("skein-1024-1008", 44);
        WEIGHTS.put("skein-1024-1016", 44);
        WEIGHTS.put("skein-1024-1024", 44);
        WEIGHTS.put("skein-1024-104", 43);
        WEIGHTS.put("skein-1024-112", 43);
        WEIGHTS.put("skein-1024-120", 43);
        WEIGHTS.put("skein-1024-128", 44);
        WEIGHTS.put("skein-1024-136", 43);
        WEIGHTS.put("skein-1024-144", 44);
        WEIGHTS.put("skein-1024-152", 44);
        WEIGHTS.put("skein-1024-16", 44);
        WEIGHTS.put("skein-1024-160", 43);
        WEIGHTS.put("skein-1024-168", 44);
        WEIGHTS.put("skein-1024-176", 44);
        WEIGHTS.put("skein-1024-184", 44);
        WEIGHTS.put("skein-1024-192", 43);
        WEIGHTS.put("skein-1024-200", 44);
        WEIGHTS.put("skein-1024-208", 44);
        WEIGHTS.put("skein-1024-216", 44);
        WEIGHTS.put("skein-1024-224", 43);
        WEIGHTS.put("skein-1024-232", 46);
        WEIGHTS.put("skein-1024-24", 43);
        WEIGHTS.put("skein-1024-240", 43);
        WEIGHTS.put("skein-1024-248", 44);
        WEIGHTS.put("skein-1024-256", 43);
        WEIGHTS.put("skein-1024-264", 44);
        WEIGHTS.put("skein-1024-272", 43);
        WEIGHTS.put("skein-1024-280", 44);
        WEIGHTS.put("skein-1024-288", 44);
        WEIGHTS.put("skein-1024-296", 44);
        WEIGHTS.put("skein-1024-304", 43);
        WEIGHTS.put("skein-1024-312", 44);
        WEIGHTS.put("skein-1024-32", 44);
        WEIGHTS.put("skein-1024-320", 44);
        WEIGHTS.put("skein-1024-328", 43);
        WEIGHTS.put("skein-1024-336", 44);
        WEIGHTS.put("skein-1024-344", 44);
        WEIGHTS.put("skein-1024-352", 44);
        WEIGHTS.put("skein-1024-360", 43);
        WEIGHTS.put("skein-1024-368", 43);
        WEIGHTS.put("skein-1024-376", 43);
        WEIGHTS.put("skein-1024-384", 43);
        WEIGHTS.put("skein-1024-392", 43);
        WEIGHTS.put("skein-1024-40", 44);
        WEIGHTS.put("skein-1024-400", 43);
        WEIGHTS.put("skein-1024-408", 44);
        WEIGHTS.put("skein-1024-416", 43);
        WEIGHTS.put("skein-1024-424", 44);
        WEIGHTS.put("skein-1024-432", 43);
        WEIGHTS.put("skein-1024-440", 44);
        WEIGHTS.put("skein-1024-448", 48);
        WEIGHTS.put("skein-1024-456", 43);
        WEIGHTS.put("skein-1024-464", 45);
        WEIGHTS.put("skein-1024-472", 44);
        WEIGHTS.put("skein-1024-48", 45);
        WEIGHTS.put("skein-1024-480", 43);
        WEIGHTS.put("skein-1024-488", 44);
        WEIGHTS.put("skein-1024-496", 43);
        WEIGHTS.put("skein-1024-504", 44);
        WEIGHTS.put("skein-1024-512", 44);
        WEIGHTS.put("skein-1024-520", 44);
        WEIGHTS.put("skein-1024-528", 46);
        WEIGHTS.put("skein-1024-536", 43);
        WEIGHTS.put("skein-1024-544", 54);
        WEIGHTS.put("skein-1024-552", 43);
        WEIGHTS.put("skein-1024-56", 43);
        WEIGHTS.put("skein-1024-560", 43);
        WEIGHTS.put("skein-1024-568", 43);
        WEIGHTS.put("skein-1024-576", 43);
        WEIGHTS.put("skein-1024-584", 44);
        WEIGHTS.put("skein-1024-592", 43);
        WEIGHTS.put("skein-1024-600", 44);
        WEIGHTS.put("skein-1024-608", 43);
        WEIGHTS.put("skein-1024-616", 43);
        WEIGHTS.put("skein-1024-624", 43);
        WEIGHTS.put("skein-1024-632", 44);
        WEIGHTS.put("skein-1024-64", 43);
        WEIGHTS.put("skein-1024-640", 44);
        WEIGHTS.put("skein-1024-648", 44);
        WEIGHTS.put("skein-1024-656", 43);
        WEIGHTS.put("skein-1024-664", 46);
        WEIGHTS.put("skein-1024-672", 43);
        WEIGHTS.put("skein-1024-680", 44);
        WEIGHTS.put("skein-1024-688", 44);
        WEIGHTS.put("skein-1024-696", 43);
        WEIGHTS.put("skein-1024-704", 43);
        WEIGHTS.put("skein-1024-712", 43);
        WEIGHTS.put("skein-1024-72", 44);
        WEIGHTS.put("skein-1024-720", 43);
        WEIGHTS.put("skein-1024-728", 44);
        WEIGHTS.put("skein-1024-736", 44);
        WEIGHTS.put("skein-1024-744", 44);
        WEIGHTS.put("skein-1024-752", 44);
        WEIGHTS.put("skein-1024-760", 44);
        WEIGHTS.put("skein-1024-768", 45);
        WEIGHTS.put("skein-1024-776", 43);
        WEIGHTS.put("skein-1024-784", 46);
        WEIGHTS.put("skein-1024-792", 43);
        WEIGHTS.put("skein-1024-8", 44);
        WEIGHTS.put("skein-1024-80", 44);
        WEIGHTS.put("skein-1024-800", 43);
        WEIGHTS.put("skein-1024-808", 45);
        WEIGHTS.put("skein-1024-816", 44);
        WEIGHTS.put("skein-1024-824", 45);
        WEIGHTS.put("skein-1024-832", 43);
        WEIGHTS.put("skein-1024-840", 43);
        WEIGHTS.put("skein-1024-848", 43);
        WEIGHTS.put("skein-1024-856", 43);
        WEIGHTS.put("skein-1024-864", 43);
        WEIGHTS.put("skein-1024-872", 43);
        WEIGHTS.put("skein-1024-88", 43);
        WEIGHTS.put("skein-1024-880", 44);
        WEIGHTS.put("skein-1024-888", 43);
        WEIGHTS.put("skein-1024-896", 43);
        WEIGHTS.put("skein-1024-904", 43);
        WEIGHTS.put("skein-1024-912", 45);
        WEIGHTS.put("skein-1024-920", 43);
        WEIGHTS.put("skein-1024-928", 44);
        WEIGHTS.put("skein-1024-936", 44);
        WEIGHTS.put("skein-1024-944", 43);
        WEIGHTS.put("skein-1024-952", 43);
        WEIGHTS.put("skein-1024-96", 43);
        WEIGHTS.put("skein-1024-960", 43);
        WEIGHTS.put("skein-1024-968", 43);
        WEIGHTS.put("skein-1024-976", 43);
        WEIGHTS.put("skein-1024-984", 44);
        WEIGHTS.put("skein-1024-992", 45);
        WEIGHTS.put("skein-256-104", 63);
        WEIGHTS.put("skein-256-112", 63);
        WEIGHTS.put("skein-256-120", 64);
        WEIGHTS.put("skein-256-128", 64);
        WEIGHTS.put("skein-256-136", 64);
        WEIGHTS.put("skein-256-144", 64);
        WEIGHTS.put("skein-256-152", 63);
        WEIGHTS.put("skein-256-16", 63);
        WEIGHTS.put("skein-256-160", 64);
        WEIGHTS.put("skein-256-168", 64);
        WEIGHTS.put("skein-256-176", 64);
        WEIGHTS.put("skein-256-184", 64);
        WEIGHTS.put("skein-256-192", 64);
        WEIGHTS.put("skein-256-200", 63);
        WEIGHTS.put("skein-256-208", 64);
        WEIGHTS.put("skein-256-216", 63);
        WEIGHTS.put("skein-256-224", 64);
        WEIGHTS.put("skein-256-232", 63);
        WEIGHTS.put("skein-256-24", 63);
        WEIGHTS.put("skein-256-240", 64);
        WEIGHTS.put("skein-256-248", 69);
        WEIGHTS.put("skein-256-256", 64);
        WEIGHTS.put("skein-256-32", 65);
        WEIGHTS.put("skein-256-40", 63);
        WEIGHTS.put("skein-256-48", 64);
        WEIGHTS.put("skein-256-56", 64);
        WEIGHTS.put("skein-256-64", 64);
        WEIGHTS.put("skein-256-72", 64);
        WEIGHTS.put("skein-256-8", 64);
        WEIGHTS.put("skein-256-80", 63);
        WEIGHTS.put("skein-256-88", 64);
        WEIGHTS.put("skein-256-96", 64);
        WEIGHTS.put("skein-512-104", 48);
        WEIGHTS.put("skein-512-112", 49);
        WEIGHTS.put("skein-512-120", 49);
        WEIGHTS.put("skein-512-128", 48);
        WEIGHTS.put("skein-512-136", 50);
        WEIGHTS.put("skein-512-144", 49);
        WEIGHTS.put("skein-512-152", 48);
        WEIGHTS.put("skein-512-16", 49);
        WEIGHTS.put("skein-512-160", 49);
        WEIGHTS.put("skein-512-168", 49);
        WEIGHTS.put("skein-512-176", 49);
        WEIGHTS.put("skein-512-184", 49);
        WEIGHTS.put("skein-512-192", 49);
        WEIGHTS.put("skein-512-200", 49);
        WEIGHTS.put("skein-512-208", 49);
        WEIGHTS.put("skein-512-216", 48);
        WEIGHTS.put("skein-512-224", 54);
        WEIGHTS.put("skein-512-232", 50);
        WEIGHTS.put("skein-512-24", 49);
        WEIGHTS.put("skein-512-240", 48);
        WEIGHTS.put("skein-512-248", 49);
        WEIGHTS.put("skein-512-256", 49);
        WEIGHTS.put("skein-512-264", 49);
        WEIGHTS.put("skein-512-272", 48);
        WEIGHTS.put("skein-512-280", 49);
        WEIGHTS.put("skein-512-288", 49);
        WEIGHTS.put("skein-512-296", 49);
        WEIGHTS.put("skein-512-304", 51);
        WEIGHTS.put("skein-512-312", 48);
        WEIGHTS.put("skein-512-32", 60);
        WEIGHTS.put("skein-512-320", 49);
        WEIGHTS.put("skein-512-328", 49);
        WEIGHTS.put("skein-512-336", 49);
        WEIGHTS.put("skein-512-344", 49);
        WEIGHTS.put("skein-512-352", 49);
        WEIGHTS.put("skein-512-360", 49);
        WEIGHTS.put("skein-512-368", 48);
        WEIGHTS.put("skein-512-376", 50);
        WEIGHTS.put("skein-512-384", 49);
        WEIGHTS.put("skein-512-392", 48);
        WEIGHTS.put("skein-512-40", 49);
        WEIGHTS.put("skein-512-400", 48);
        WEIGHTS.put("skein-512-408", 49);
        WEIGHTS.put("skein-512-416", 50);
        WEIGHTS.put("skein-512-424", 49);
        WEIGHTS.put("skein-512-432", 49);
        WEIGHTS.put("skein-512-440", 49);
        WEIGHTS.put("skein-512-448", 49);
        WEIGHTS.put("skein-512-456", 48);
        WEIGHTS.put("skein-512-464", 50);
        WEIGHTS.put("skein-512-472", 50);
        WEIGHTS.put("skein-512-48", 49);
        WEIGHTS.put("skein-512-480", 49);
        WEIGHTS.put("skein-512-488", 51);
        WEIGHTS.put("skein-512-496", 51);
        WEIGHTS.put("skein-512-504", 49);
        WEIGHTS.put("skein-512-512", 49);
        WEIGHTS.put("skein-512-56", 48);
        WEIGHTS.put("skein-512-64", 48);
        WEIGHTS.put("skein-512-72", 49);
        WEIGHTS.put("skein-512-8", 49);
        WEIGHTS.put("skein-512-80", 49);
        WEIGHTS.put("skein-512-88", 50);
        WEIGHTS.put("skein-512-96", 49);
        WEIGHTS.put("sm3", 109);
        WEIGHTS.put("streebog256", 588);
        WEIGHTS.put("streebog512", 585);
        WEIGHTS.put("sum16", 11);
        WEIGHTS.put("sum24", 11);
        WEIGHTS.put("sum32", 11);
        WEIGHTS.put("sum40", 11);
        WEIGHTS.put("sum48", 11);
        WEIGHTS.put("sum56", 11);
        WEIGHTS.put("sum64", 10);
        WEIGHTS.put("sum8", 11);
        WEIGHTS.put("sum_bsd", 29);
        WEIGHTS.put("sum_minix", 29);
        WEIGHTS.put("sum_sysv", 11);
        WEIGHTS.put("tiger", 44);
        WEIGHTS.put("tiger-128-4-php", 62);
        WEIGHTS.put("tiger-160-4-php", 61);
        WEIGHTS.put("tiger-192-4-php", 61);
        WEIGHTS.put("tiger128", 45);
        WEIGHTS.put("tiger160", 46);
        WEIGHTS.put("tiger2", 45);
        WEIGHTS.put("tree:tiger", 54);
        WEIGHTS.put("tree:tiger2", 53);
        WEIGHTS.put("vsh", 17204);
        WEIGHTS.put("whirlpool0", 373);
        WEIGHTS.put("whirlpool1", 375);
        WEIGHTS.put("whirlpool2", 376);
        WEIGHTS.put("xoodyak", 35);
        WEIGHTS.put("xor8", 11);
        WEIGHTS.put("xxhash32", 9);

        // aliases
        WEIGHTS_ALIASES.put("adler-32", WEIGHTS.get("adler32"));
        WEIGHTS_ALIASES.put("adler32", WEIGHTS.get("adler32"));
        WEIGHTS_ALIASES.put("ast", WEIGHTS.get("aststrsum"));
        WEIGHTS_ALIASES.put("b2sum", WEIGHTS.get("blake2b-512"));
        WEIGHTS_ALIASES.put("b2sum-104", WEIGHTS.get("blake2b-104"));
        WEIGHTS_ALIASES.put("b2sum-112", WEIGHTS.get("blake2b-112"));
        WEIGHTS_ALIASES.put("b2sum-120", WEIGHTS.get("blake2b-120"));
        WEIGHTS_ALIASES.put("b2sum-128", WEIGHTS.get("blake2b-128"));
        WEIGHTS_ALIASES.put("b2sum-136", WEIGHTS.get("blake2b-136"));
        WEIGHTS_ALIASES.put("b2sum-144", WEIGHTS.get("blake2b-144"));
        WEIGHTS_ALIASES.put("b2sum-152", WEIGHTS.get("blake2b-152"));
        WEIGHTS_ALIASES.put("b2sum-16", WEIGHTS.get("blake2b-16"));
        WEIGHTS_ALIASES.put("b2sum-160", WEIGHTS.get("blake2b-160"));
        WEIGHTS_ALIASES.put("b2sum-168", WEIGHTS.get("blake2b-168"));
        WEIGHTS_ALIASES.put("b2sum-176", WEIGHTS.get("blake2b-176"));
        WEIGHTS_ALIASES.put("b2sum-184", WEIGHTS.get("blake2b-184"));
        WEIGHTS_ALIASES.put("b2sum-192", WEIGHTS.get("blake2b-192"));
        WEIGHTS_ALIASES.put("b2sum-200", WEIGHTS.get("blake2b-200"));
        WEIGHTS_ALIASES.put("b2sum-208", WEIGHTS.get("blake2b-208"));
        WEIGHTS_ALIASES.put("b2sum-216", WEIGHTS.get("blake2b-216"));
        WEIGHTS_ALIASES.put("b2sum-224", WEIGHTS.get("blake2b-224"));
        WEIGHTS_ALIASES.put("b2sum-232", WEIGHTS.get("blake2b-232"));
        WEIGHTS_ALIASES.put("b2sum-24", WEIGHTS.get("blake2b-24"));
        WEIGHTS_ALIASES.put("b2sum-240", WEIGHTS.get("blake2b-240"));
        WEIGHTS_ALIASES.put("b2sum-248", WEIGHTS.get("blake2b-248"));
        WEIGHTS_ALIASES.put("b2sum-256", WEIGHTS.get("blake2b-256"));
        WEIGHTS_ALIASES.put("b2sum-264", WEIGHTS.get("blake2b-264"));
        WEIGHTS_ALIASES.put("b2sum-272", WEIGHTS.get("blake2b-272"));
        WEIGHTS_ALIASES.put("b2sum-280", WEIGHTS.get("blake2b-280"));
        WEIGHTS_ALIASES.put("b2sum-288", WEIGHTS.get("blake2b-288"));
        WEIGHTS_ALIASES.put("b2sum-296", WEIGHTS.get("blake2b-296"));
        WEIGHTS_ALIASES.put("b2sum-304", WEIGHTS.get("blake2b-304"));
        WEIGHTS_ALIASES.put("b2sum-312", WEIGHTS.get("blake2b-312"));
        WEIGHTS_ALIASES.put("b2sum-32", WEIGHTS.get("blake2b-32"));
        WEIGHTS_ALIASES.put("b2sum-320", WEIGHTS.get("blake2b-320"));
        WEIGHTS_ALIASES.put("b2sum-328", WEIGHTS.get("blake2b-328"));
        WEIGHTS_ALIASES.put("b2sum-336", WEIGHTS.get("blake2b-336"));
        WEIGHTS_ALIASES.put("b2sum-344", WEIGHTS.get("blake2b-344"));
        WEIGHTS_ALIASES.put("b2sum-352", WEIGHTS.get("blake2b-352"));
        WEIGHTS_ALIASES.put("b2sum-360", WEIGHTS.get("blake2b-360"));
        WEIGHTS_ALIASES.put("b2sum-368", WEIGHTS.get("blake2b-368"));
        WEIGHTS_ALIASES.put("b2sum-376", WEIGHTS.get("blake2b-376"));
        WEIGHTS_ALIASES.put("b2sum-384", WEIGHTS.get("blake2b-384"));
        WEIGHTS_ALIASES.put("b2sum-392", WEIGHTS.get("blake2b-392"));
        WEIGHTS_ALIASES.put("b2sum-40", WEIGHTS.get("blake2b-40"));
        WEIGHTS_ALIASES.put("b2sum-400", WEIGHTS.get("blake2b-400"));
        WEIGHTS_ALIASES.put("b2sum-408", WEIGHTS.get("blake2b-408"));
        WEIGHTS_ALIASES.put("b2sum-416", WEIGHTS.get("blake2b-416"));
        WEIGHTS_ALIASES.put("b2sum-424", WEIGHTS.get("blake2b-424"));
        WEIGHTS_ALIASES.put("b2sum-432", WEIGHTS.get("blake2b-432"));
        WEIGHTS_ALIASES.put("b2sum-440", WEIGHTS.get("blake2b-440"));
        WEIGHTS_ALIASES.put("b2sum-448", WEIGHTS.get("blake2b-448"));
        WEIGHTS_ALIASES.put("b2sum-456", WEIGHTS.get("blake2b-456"));
        WEIGHTS_ALIASES.put("b2sum-464", WEIGHTS.get("blake2b-464"));
        WEIGHTS_ALIASES.put("b2sum-472", WEIGHTS.get("blake2b-472"));
        WEIGHTS_ALIASES.put("b2sum-48", WEIGHTS.get("blake2b-48"));
        WEIGHTS_ALIASES.put("b2sum-480", WEIGHTS.get("blake2b-480"));
        WEIGHTS_ALIASES.put("b2sum-488", WEIGHTS.get("blake2b-488"));
        WEIGHTS_ALIASES.put("b2sum-496", WEIGHTS.get("blake2b-496"));
        WEIGHTS_ALIASES.put("b2sum-504", WEIGHTS.get("blake2b-504"));
        WEIGHTS_ALIASES.put("b2sum-512", WEIGHTS.get("blake2b-512"));
        WEIGHTS_ALIASES.put("b2sum-56", WEIGHTS.get("blake2b-56"));
        WEIGHTS_ALIASES.put("b2sum-64", WEIGHTS.get("blake2b-64"));
        WEIGHTS_ALIASES.put("b2sum-72", WEIGHTS.get("blake2b-72"));
        WEIGHTS_ALIASES.put("b2sum-8", WEIGHTS.get("blake2b-8"));
        WEIGHTS_ALIASES.put("b2sum-80", WEIGHTS.get("blake2b-80"));
        WEIGHTS_ALIASES.put("b2sum-88", WEIGHTS.get("blake2b-88"));
        WEIGHTS_ALIASES.put("b2sum-96", WEIGHTS.get("blake2b-96"));
        WEIGHTS_ALIASES.put("b3sum", WEIGHTS.get("blake3"));
        WEIGHTS_ALIASES.put("blake-224", WEIGHTS.get("blake224"));
        WEIGHTS_ALIASES.put("blake-256", WEIGHTS.get("blake256"));
        WEIGHTS_ALIASES.put("blake-384", WEIGHTS.get("blake384"));
        WEIGHTS_ALIASES.put("blake-512", WEIGHTS.get("blake512"));
        WEIGHTS_ALIASES.put("blake2b", WEIGHTS.get("blake2b-512"));
        WEIGHTS_ALIASES.put("blake2bp-512", WEIGHTS.get("blake2bp"));
        WEIGHTS_ALIASES.put("blake2s", WEIGHTS.get("blake2s-256"));
        WEIGHTS_ALIASES.put("blake2sp-256", WEIGHTS.get("blake2sp"));
        WEIGHTS_ALIASES.put("blake3-256", WEIGHTS.get("blake3"));
        WEIGHTS_ALIASES.put("bluemidnightwish-224", WEIGHTS.get("bluemidnightwish224"));
        WEIGHTS_ALIASES.put("bluemidnightwish-256", WEIGHTS.get("bluemidnightwish256"));
        WEIGHTS_ALIASES.put("bluemidnightwish-384", WEIGHTS.get("bluemidnightwish384"));
        WEIGHTS_ALIASES.put("bluemidnightwish-512", WEIGHTS.get("bluemidnightwish512"));
        WEIGHTS_ALIASES.put("bsd", WEIGHTS.get("sum_bsd"));
        WEIGHTS_ALIASES.put("bsdsum", WEIGHTS.get("sum_bsd"));
        WEIGHTS_ALIASES.put("crc-16", WEIGHTS.get("crc16"));
        WEIGHTS_ALIASES.put("crc-16_minix", WEIGHTS.get("crc16_minix"));
        WEIGHTS_ALIASES.put("crc-16_x-25", WEIGHTS.get("fcs16"));
        WEIGHTS_ALIASES.put("crc-24", WEIGHTS.get("crc24"));
        WEIGHTS_ALIASES.put("crc-32", WEIGHTS.get("crc32"));
        WEIGHTS_ALIASES.put("crc-32_bzip-2", WEIGHTS.get("crc32_bzip2"));
        WEIGHTS_ALIASES.put("crc-32_bzip2", WEIGHTS.get("crc32_bzip2"));
        WEIGHTS_ALIASES.put("crc-32_go-koopman", WEIGHTS.get("crc32_go-koopman"));
        WEIGHTS_ALIASES.put("crc-32_jamcrc", WEIGHTS.get("crc32_ubi"));
        WEIGHTS_ALIASES.put("crc-32_mpeg-2", WEIGHTS.get("crc32_mpeg2"));
        WEIGHTS_ALIASES.put("crc-32_php", WEIGHTS.get("crc32_php"));
        WEIGHTS_ALIASES.put("crc-32_ubi", WEIGHTS.get("crc32_ubi"));
        WEIGHTS_ALIASES.put("crc-32c", WEIGHTS.get("crc32c"));
        WEIGHTS_ALIASES.put("crc-64", WEIGHTS.get("crc64"));
        WEIGHTS_ALIASES.put("crc-64_ecma", WEIGHTS.get("crc64_ecma"));
        WEIGHTS_ALIASES.put("crc-64_go-ecma", WEIGHTS.get("crc64_xz"));
        WEIGHTS_ALIASES.put("crc-64_go-iso", WEIGHTS.get("crc64_go-iso"));
        WEIGHTS_ALIASES.put("crc-64_nvme", WEIGHTS.get("crc64_nvme"));
        WEIGHTS_ALIASES.put("crc-64_xz", WEIGHTS.get("crc64_xz"));
        WEIGHTS_ALIASES.put("crc-8", WEIGHTS.get("crc8"));
        WEIGHTS_ALIASES.put("crc-82", WEIGHTS.get("crc82_darc"));
        WEIGHTS_ALIASES.put("crc-82_darc", WEIGHTS.get("crc82_darc"));
        WEIGHTS_ALIASES.put("crc16_x25", WEIGHTS.get("fcs16"));
        WEIGHTS_ALIASES.put("crc32_jamcrc", WEIGHTS.get("crc32_ubi"));
        WEIGHTS_ALIASES.put("crc64_go-ecma", WEIGHTS.get("crc64_xz"));
        WEIGHTS_ALIASES.put("crc82", WEIGHTS.get("crc82_darc"));
        WEIGHTS_ALIASES.put("cubehash-224", WEIGHTS.get("cubehash224"));
        WEIGHTS_ALIASES.put("cubehash-256", WEIGHTS.get("cubehash256"));
        WEIGHTS_ALIASES.put("cubehash-384", WEIGHTS.get("cubehash384"));
        WEIGHTS_ALIASES.put("cubehash-512", WEIGHTS.get("cubehash512"));
        WEIGHTS_ALIASES.put("dha-256", WEIGHTS.get("dha256"));
        WEIGHTS_ALIASES.put("dss1", WEIGHTS.get("sha-1"));
        WEIGHTS_ALIASES.put("echo-224", WEIGHTS.get("echo224"));
        WEIGHTS_ALIASES.put("echo-256", WEIGHTS.get("echo256"));
        WEIGHTS_ALIASES.put("echo-384", WEIGHTS.get("echo384"));
        WEIGHTS_ALIASES.put("echo-512", WEIGHTS.get("echo512"));
        WEIGHTS_ALIASES.put("edonkey", WEIGHTS.get("ed2k"));
        WEIGHTS_ALIASES.put("elf-32", WEIGHTS.get("elf"));
        WEIGHTS_ALIASES.put("elf32", WEIGHTS.get("elf"));
        WEIGHTS_ALIASES.put("emule", WEIGHTS.get("ed2k"));
        WEIGHTS_ALIASES.put("fcs-16", WEIGHTS.get("fcs16"));
        WEIGHTS_ALIASES.put("fcs-32", WEIGHTS.get("crc32"));
        WEIGHTS_ALIASES.put("fcs32", WEIGHTS.get("crc32"));
        WEIGHTS_ALIASES.put("fletcher-16", WEIGHTS.get("fletcher16"));
        WEIGHTS_ALIASES.put("fork-256", WEIGHTS.get("fork256"));
        WEIGHTS_ALIASES.put("fugue-224", WEIGHTS.get("fugue224"));
        WEIGHTS_ALIASES.put("fugue-256", WEIGHTS.get("fugue256"));
        WEIGHTS_ALIASES.put("fugue-384", WEIGHTS.get("fugue384"));
        WEIGHTS_ALIASES.put("fugue-512", WEIGHTS.get("fugue512"));
        WEIGHTS_ALIASES.put("gost:default", WEIGHTS.get("gost"));
        WEIGHTS_ALIASES.put("groestl224", WEIGHTS.get("groestl-224"));
        WEIGHTS_ALIASES.put("groestl256", WEIGHTS.get("groestl-256"));
        WEIGHTS_ALIASES.put("groestl384", WEIGHTS.get("groestl-384"));
        WEIGHTS_ALIASES.put("groestl512", WEIGHTS.get("groestl-512"));
        WEIGHTS_ALIASES.put("hamsi-224", WEIGHTS.get("hamsi224"));
        WEIGHTS_ALIASES.put("hamsi-256", WEIGHTS.get("hamsi256"));
        WEIGHTS_ALIASES.put("hamsi-384", WEIGHTS.get("hamsi384"));
        WEIGHTS_ALIASES.put("hamsi-512", WEIGHTS.get("hamsi512"));
        WEIGHTS_ALIASES.put("has-160", WEIGHTS.get("has160"));
        WEIGHTS_ALIASES.put("haval", WEIGHTS.get("haval_128_3"));
        WEIGHTS_ALIASES.put("jh-224", WEIGHTS.get("jh224"));
        WEIGHTS_ALIASES.put("jh-256", WEIGHTS.get("jh256"));
        WEIGHTS_ALIASES.put("jh-384", WEIGHTS.get("jh384"));
        WEIGHTS_ALIASES.put("jh-512", WEIGHTS.get("jh512"));
        WEIGHTS_ALIASES.put("joaat-32", WEIGHTS.get("joaat"));
        WEIGHTS_ALIASES.put("joaat32", WEIGHTS.get("joaat"));
        WEIGHTS_ALIASES.put("k12", WEIGHTS.get("kangarootwelve"));
        WEIGHTS_ALIASES.put("kangaroo12", WEIGHTS.get("kangarootwelve"));
        WEIGHTS_ALIASES.put("keccak-224", WEIGHTS.get("keccak224"));
        WEIGHTS_ALIASES.put("keccak-256", WEIGHTS.get("keccak256"));
        WEIGHTS_ALIASES.put("keccak-288", WEIGHTS.get("keccak288"));
        WEIGHTS_ALIASES.put("keccak-384", WEIGHTS.get("keccak384"));
        WEIGHTS_ALIASES.put("keccak-512", WEIGHTS.get("keccak512"));
        WEIGHTS_ALIASES.put("luffa-224", WEIGHTS.get("luffa224"));
        WEIGHTS_ALIASES.put("luffa-256", WEIGHTS.get("luffa256"));
        WEIGHTS_ALIASES.put("luffa-384", WEIGHTS.get("luffa384"));
        WEIGHTS_ALIASES.put("luffa-512", WEIGHTS.get("luffa512"));
        WEIGHTS_ALIASES.put("m14", WEIGHTS.get("marsupilamifourteen"));
        WEIGHTS_ALIASES.put("marsupilami14", WEIGHTS.get("marsupilamifourteen"));
        WEIGHTS_ALIASES.put("md2sum", WEIGHTS.get("md2"));
        WEIGHTS_ALIASES.put("md4sum", WEIGHTS.get("md4"));
        WEIGHTS_ALIASES.put("md5sum", WEIGHTS.get("md5"));
        WEIGHTS_ALIASES.put("mdc-2", WEIGHTS.get("mdc2"));
        WEIGHTS_ALIASES.put("radiogatun", WEIGHTS.get("radiogatun:64"));
        WEIGHTS_ALIASES.put("rg-32", WEIGHTS.get("radiogatun:32"));
        WEIGHTS_ALIASES.put("rg-64", WEIGHTS.get("radiogatun:64"));
        WEIGHTS_ALIASES.put("rg32", WEIGHTS.get("radiogatun:32"));
        WEIGHTS_ALIASES.put("rg64", WEIGHTS.get("radiogatun:64"));
        WEIGHTS_ALIASES.put("ripe-md128", WEIGHTS.get("ripemd128"));
        WEIGHTS_ALIASES.put("ripe-md160", WEIGHTS.get("ripemd160"));
        WEIGHTS_ALIASES.put("ripe-md256", WEIGHTS.get("ripemd256"));
        WEIGHTS_ALIASES.put("ripe-md320", WEIGHTS.get("ripemd320"));
        WEIGHTS_ALIASES.put("ripemd-128", WEIGHTS.get("ripemd128"));
        WEIGHTS_ALIASES.put("ripemd-160", WEIGHTS.get("ripemd160"));
        WEIGHTS_ALIASES.put("ripemd-256", WEIGHTS.get("ripemd256"));
        WEIGHTS_ALIASES.put("ripemd-320", WEIGHTS.get("ripemd320"));
        WEIGHTS_ALIASES.put("rmd-128", WEIGHTS.get("ripemd128"));
        WEIGHTS_ALIASES.put("rmd-160", WEIGHTS.get("ripemd160"));
        WEIGHTS_ALIASES.put("rmd-256", WEIGHTS.get("ripemd256"));
        WEIGHTS_ALIASES.put("rmd-320", WEIGHTS.get("ripemd320"));
        WEIGHTS_ALIASES.put("rmd128", WEIGHTS.get("ripemd128"));
        WEIGHTS_ALIASES.put("rmd160", WEIGHTS.get("ripemd160"));
        WEIGHTS_ALIASES.put("rmd256", WEIGHTS.get("ripemd256"));
        WEIGHTS_ALIASES.put("rmd320", WEIGHTS.get("ripemd320"));
        WEIGHTS_ALIASES.put("romulus-h", WEIGHTS.get("romulush"));
        WEIGHTS_ALIASES.put("sha", WEIGHTS.get("sha-1"));
        WEIGHTS_ALIASES.put("sha-0", WEIGHTS.get("sha0"));
        WEIGHTS_ALIASES.put("sha-160", WEIGHTS.get("sha-1"));
        WEIGHTS_ALIASES.put("sha-3-224", WEIGHTS.get("sha3-224"));
        WEIGHTS_ALIASES.put("sha-3-256", WEIGHTS.get("sha3-256"));
        WEIGHTS_ALIASES.put("sha-3-384", WEIGHTS.get("sha3-384"));
        WEIGHTS_ALIASES.put("sha-3-512", WEIGHTS.get("sha3-512"));
        WEIGHTS_ALIASES.put("sha1", WEIGHTS.get("sha-1"));
        WEIGHTS_ALIASES.put("sha160", WEIGHTS.get("sha-1"));
        WEIGHTS_ALIASES.put("sha1sum", WEIGHTS.get("sha-1"));
        WEIGHTS_ALIASES.put("sha224", WEIGHTS.get("sha-224"));
        WEIGHTS_ALIASES.put("sha256", WEIGHTS.get("sha-256"));
        WEIGHTS_ALIASES.put("sha384", WEIGHTS.get("sha-384"));
        WEIGHTS_ALIASES.put("sha512", WEIGHTS.get("sha-512"));
        WEIGHTS_ALIASES.put("sha512/224", WEIGHTS.get("sha-512/224"));
        WEIGHTS_ALIASES.put("sha512/256", WEIGHTS.get("sha-512/256"));
        WEIGHTS_ALIASES.put("sha512t224", WEIGHTS.get("sha-512/224"));
        WEIGHTS_ALIASES.put("sha512t256", WEIGHTS.get("sha-512/256"));
        WEIGHTS_ALIASES.put("shabal-192", WEIGHTS.get("shabal192"));
        WEIGHTS_ALIASES.put("shabal-224", WEIGHTS.get("shabal224"));
        WEIGHTS_ALIASES.put("shabal-256", WEIGHTS.get("shabal256"));
        WEIGHTS_ALIASES.put("shabal-384", WEIGHTS.get("shabal384"));
        WEIGHTS_ALIASES.put("shabal-512", WEIGHTS.get("shabal512"));
        WEIGHTS_ALIASES.put("simd-224", WEIGHTS.get("simd224"));
        WEIGHTS_ALIASES.put("simd-256", WEIGHTS.get("simd256"));
        WEIGHTS_ALIASES.put("simd-384", WEIGHTS.get("simd384"));
        WEIGHTS_ALIASES.put("simd-512", WEIGHTS.get("simd512"));
        WEIGHTS_ALIASES.put("skein-1024", WEIGHTS.get("skein-1024-1024"));
        WEIGHTS_ALIASES.put("skein-256", WEIGHTS.get("skein-256-256"));
        WEIGHTS_ALIASES.put("skein-512", WEIGHTS.get("skein-512-512"));
        WEIGHTS_ALIASES.put("skein1024", WEIGHTS.get("skein-1024-1024"));
        WEIGHTS_ALIASES.put("skein256", WEIGHTS.get("skein-256-256"));
        WEIGHTS_ALIASES.put("skein512", WEIGHTS.get("skein-512-512"));
        WEIGHTS_ALIASES.put("streebog-256", WEIGHTS.get("streebog256"));
        WEIGHTS_ALIASES.put("streebog-512", WEIGHTS.get("streebog512"));
        WEIGHTS_ALIASES.put("strsum", WEIGHTS.get("aststrsum"));
        WEIGHTS_ALIASES.put("sum-16", WEIGHTS.get("sum16"));
        WEIGHTS_ALIASES.put("sum-24", WEIGHTS.get("sum24"));
        WEIGHTS_ALIASES.put("sum-32", WEIGHTS.get("sum32"));
        WEIGHTS_ALIASES.put("sum-40", WEIGHTS.get("sum40"));
        WEIGHTS_ALIASES.put("sum-48", WEIGHTS.get("sum48"));
        WEIGHTS_ALIASES.put("sum-56", WEIGHTS.get("sum56"));
        WEIGHTS_ALIASES.put("sum-64", WEIGHTS.get("sum64"));
        WEIGHTS_ALIASES.put("sum-8", WEIGHTS.get("sum8"));
        WEIGHTS_ALIASES.put("sum_plan9", WEIGHTS.get("crc32_fddi"));
        WEIGHTS_ALIASES.put("sumbsd", WEIGHTS.get("sum_bsd"));
        WEIGHTS_ALIASES.put("sumsysv", WEIGHTS.get("sum_sysv"));
        WEIGHTS_ALIASES.put("sysv", WEIGHTS.get("sum_sysv"));
        WEIGHTS_ALIASES.put("sysvsum", WEIGHTS.get("sum_sysv"));
        WEIGHTS_ALIASES.put("tiger-128", WEIGHTS.get("tiger128"));
        WEIGHTS_ALIASES.put("tiger-160", WEIGHTS.get("tiger160"));
        WEIGHTS_ALIASES.put("tiger-192", WEIGHTS.get("tiger"));
        WEIGHTS_ALIASES.put("tiger192", WEIGHTS.get("tiger"));
        WEIGHTS_ALIASES.put("tiger_128_4_php", WEIGHTS.get("tiger-128-4-php"));
        WEIGHTS_ALIASES.put("tiger_160_4_php", WEIGHTS.get("tiger-160-4-php"));
        WEIGHTS_ALIASES.put("tiger_192_4_php", WEIGHTS.get("tiger-192-4-php"));
        WEIGHTS_ALIASES.put("tree:tiger-192", WEIGHTS.get("tree:tiger"));
        WEIGHTS_ALIASES.put("tree:tiger192", WEIGHTS.get("tree:tiger"));
        WEIGHTS_ALIASES.put("tth", WEIGHTS.get("tree:tiger"));
        WEIGHTS_ALIASES.put("tth2", WEIGHTS.get("tree:tiger2"));
        WEIGHTS_ALIASES.put("ubicrc32", WEIGHTS.get("crc32_ubi"));
        WEIGHTS_ALIASES.put("vsh-1024", WEIGHTS.get("vsh"));
        WEIGHTS_ALIASES.put("whirlpool", WEIGHTS.get("whirlpool2"));
        WEIGHTS_ALIASES.put("whirlpool-0", WEIGHTS.get("whirlpool0"));
        WEIGHTS_ALIASES.put("whirlpool-2", WEIGHTS.get("whirlpool2"));
        WEIGHTS_ALIASES.put("whirlpool-l", WEIGHTS.get("whirlpool1"));
        WEIGHTS_ALIASES.put("whirlpool-t", WEIGHTS.get("whirlpool1"));
        WEIGHTS_ALIASES.put("xor-8", WEIGHTS.get("xor8"));
        WEIGHTS_ALIASES.put("xxh32", WEIGHTS.get("xxhash32"));
        // GENERATION END
    }

    /**
     * Computes and prints to System.out the above map. Run with -server option.
     * The argument needed is the name of the file used to compute the hashes
     * weights. At least 20 MB for more accurate results.
     *
     * @param args program args.
     */
    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("The name of the file to process is missing.");
            System.exit(1);
        }
        File file = new File(args[0]);
        if (file.length() <= 20 * 1024 * 1024) {
            System.err.printf("Warning: file size of %s is <= 20 MiB%n", file);
        }

        Set<String> hashes;


        // 64-Byte-array
        byte[] randomBytes = new byte[64];

        // Mit sicheren Zufallszahlen füllen
        java.security.SecureRandom secureRandom = new java.security.SecureRandom();
        secureRandom.nextBytes(randomBytes);
        HashFunctionFactory.setKey(randomBytes);

        if (args.length > 1) {
            hashes = new TreeSet<>();
            for (int i = 1; i < args.length; i++) {
                hashes.add(args[i]);
            }
        } else {
            hashes = new HashSet<>(JacksumAPI.getAvailableAlgorithms().keySet());
            hashes.addAll(JacksumAPI.getAvailableHMACs().keySet());
        }

        try {

            /*
               Warmup. Run all algorithms once so JVM has time to compile
               bytecodes and the file gets cached.
             */
            for (String hash : hashes) {
                HashAlgorithm alg = HashAlgorithm.getAlgorithm(JacksumAPI.getChecksumInstance(hash));
                runSequential(args[0], alg.getChecksum());
            }

            /*
              Now start timing runs.
             */
            final int STEPS = 20;
            long[] ms = new long[STEPS];

            for (String hash : hashes) {
                HashAlgorithm alg = HashAlgorithm.getAlgorithm(JacksumAPI.getChecksumInstance(hash));

                for (int i = 0; i < STEPS; i++) {
                    ms[i] = runSequential(args[0], alg.getChecksum());
                }
                // print all weights for all primary algorithm IDs
                System.out.printf("WEIGHTS.put(\"%s\", %d);\n",
                        alg.getName(), Math.round(e(ms)));
            }

            System.out.println("\n// aliases");
            for (String hash : hashes) {
                // print out weights for all aliases
                List<String> aliases = JacksumAPI.getAvailableAliases(hash);
                for (String alias : aliases) {
                    System.out.printf("WEIGHTS_ALIASES.put(\"%s\", WEIGHTS.get(\"%s\"));\n", alias, hash);
                }
            }

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }

    /**
     * Arithmetic mean
     */
    private static double e(long[] x) {
        long acum = 0L;
        for (long xi : x) {
            acum += xi;
        }
        return ((double) acum) / x.length;
    }

    private static long runSequential(String fileName, final AbstractChecksum md) throws IOException {
        final File src = new File(fileName);

        final InputStream is = new FilterInputStream(new BufferedInputStream(
                new FileInputStream(src))) {

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                int leido = this.in.read(b, off, len);
                if (leido != -1) {
                    md.update(b, off, leido);
                }
                return leido;
            }
        };
        byte[] BUFFER = new byte[AbstractChecksum.BUFFERSIZE];
        long st = System.nanoTime();

        while (is.read(BUFFER) != -1) {
            // No hay que hacer nada, el trabajo se hace en el FilterInputStream
            // Do not do anything, the work is done in the FilterInputStream
        }

        md.getByteArray();

        st = System.nanoTime() - st;
        is.close();
        return st / 1000000;
    }

    public static int getWeight(String name) {
        Integer answer = WEIGHTS.get(name);
        if (answer == null) {
            answer = WEIGHTS_ALIASES.get(name);
            if (answer == null) {
                return 1;
            }
        }
        return answer;
    }

    public static int getRank(String name) {
        int rank = 1;
        int reference = getWeight(name);
        for (int weight : WEIGHTS.values()) {
            if (reference > weight) {
                rank++;
            }
        }
        return rank;
    }

    public static int getMaxWeight() {
        int max = 0;
        for (int weight : WEIGHTS.values()) {
            //System.out.println(weight);
            if (weight > max) {
                max = weight;
            }
        }
        return max;
    }


    public static HashAlgorithm getAlgorithm(AbstractChecksum cs) {
        String name = cs.getName();
        return new HashAlgorithm(name, cs);
    }

    private HashAlgorithm(String name, AbstractChecksum cs) {
        this.name = name;
        Integer w = WEIGHTS.get(name);
        if (w == null) {
            w = WEIGHTS_ALIASES.get(name);
        }
        /* if there's no weight for a given algorithm, then
         * it is possible that one processor gets much more work to do
         * than the rest and the total time will not be optimal.
         * So use the main method in this class to regenerate the algorithm's
         * weights every time a new hash function in added or when a new
         * implementation is used.
         */
        this.weight = (w == null) ? 1 : w;
        this.cs = cs;
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }

    public AbstractChecksum getChecksum() {
        return this.cs;
    }

    @Override
    public int compareTo(HashAlgorithm t) {
        return t.getWeight() - this.getWeight();
    }
}
