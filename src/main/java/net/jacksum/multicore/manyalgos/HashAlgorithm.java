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

import net.jacksum.JacksumAPI;
import net.jacksum.algorithms.AbstractChecksum;

/**
 * @author Federico Tello Gentile
 * contributor: Johann N. Loefflmann
 */
public class HashAlgorithm implements Comparable<HashAlgorithm> {

    private static final Map<String, Integer> WEIGHTS = new HashMap<>(780); // max. number of entries/load factor = 579/0.75=772
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
        WEIGHTS.put("adler32", 6);
        WEIGHTS.put("ascon-hash", 17);
        WEIGHTS.put("ascon-hasha", 14);
        WEIGHTS.put("ascon-xof", 16);
        WEIGHTS.put("ascon-xofa", 13);
        WEIGHTS.put("aststrsum", 40);
        WEIGHTS.put("belt-hash", 312);
        WEIGHTS.put("blake224", 223);
        WEIGHTS.put("blake256", 225);
        WEIGHTS.put("blake2b-104", 42);
        WEIGHTS.put("blake2b-112", 41);
        WEIGHTS.put("blake2b-120", 42);
        WEIGHTS.put("blake2b-128", 41);
        WEIGHTS.put("blake2b-136", 42);
        WEIGHTS.put("blake2b-144", 41);
        WEIGHTS.put("blake2b-152", 41);
        WEIGHTS.put("blake2b-16", 43);
        WEIGHTS.put("blake2b-160", 41);
        WEIGHTS.put("blake2b-168", 42);
        WEIGHTS.put("blake2b-176", 41);
        WEIGHTS.put("blake2b-184", 42);
        WEIGHTS.put("blake2b-192", 41);
        WEIGHTS.put("blake2b-200", 41);
        WEIGHTS.put("blake2b-208", 43);
        WEIGHTS.put("blake2b-216", 41);
        WEIGHTS.put("blake2b-224", 43);
        WEIGHTS.put("blake2b-232", 41);
        WEIGHTS.put("blake2b-24", 41);
        WEIGHTS.put("blake2b-240", 42);
        WEIGHTS.put("blake2b-248", 41);
        WEIGHTS.put("blake2b-256", 43);
        WEIGHTS.put("blake2b-264", 41);
        WEIGHTS.put("blake2b-272", 41);
        WEIGHTS.put("blake2b-280", 41);
        WEIGHTS.put("blake2b-288", 42);
        WEIGHTS.put("blake2b-296", 42);
        WEIGHTS.put("blake2b-304", 42);
        WEIGHTS.put("blake2b-312", 42);
        WEIGHTS.put("blake2b-32", 42);
        WEIGHTS.put("blake2b-320", 41);
        WEIGHTS.put("blake2b-328", 44);
        WEIGHTS.put("blake2b-336", 41);
        WEIGHTS.put("blake2b-344", 42);
        WEIGHTS.put("blake2b-352", 42);
        WEIGHTS.put("blake2b-360", 42);
        WEIGHTS.put("blake2b-368", 42);
        WEIGHTS.put("blake2b-376", 42);
        WEIGHTS.put("blake2b-384", 41);
        WEIGHTS.put("blake2b-392", 41);
        WEIGHTS.put("blake2b-40", 45);
        WEIGHTS.put("blake2b-400", 41);
        WEIGHTS.put("blake2b-408", 41);
        WEIGHTS.put("blake2b-416", 41);
        WEIGHTS.put("blake2b-424", 42);
        WEIGHTS.put("blake2b-432", 42);
        WEIGHTS.put("blake2b-440", 41);
        WEIGHTS.put("blake2b-448", 41);
        WEIGHTS.put("blake2b-456", 41);
        WEIGHTS.put("blake2b-464", 42);
        WEIGHTS.put("blake2b-472", 42);
        WEIGHTS.put("blake2b-48", 43);
        WEIGHTS.put("blake2b-480", 42);
        WEIGHTS.put("blake2b-488", 41);
        WEIGHTS.put("blake2b-496", 42);
        WEIGHTS.put("blake2b-504", 43);
        WEIGHTS.put("blake2b-512", 42);
        WEIGHTS.put("blake2b-56", 41);
        WEIGHTS.put("blake2b-64", 42);
        WEIGHTS.put("blake2b-72", 42);
        WEIGHTS.put("blake2b-8", 41);
        WEIGHTS.put("blake2b-80", 42);
        WEIGHTS.put("blake2b-88", 42);
        WEIGHTS.put("blake2b-96", 41);
        WEIGHTS.put("blake2bp", 42);
        WEIGHTS.put("blake2s-104", 70);
        WEIGHTS.put("blake2s-112", 69);
        WEIGHTS.put("blake2s-120", 69);
        WEIGHTS.put("blake2s-128", 70);
        WEIGHTS.put("blake2s-136", 69);
        WEIGHTS.put("blake2s-144", 70);
        WEIGHTS.put("blake2s-152", 70);
        WEIGHTS.put("blake2s-16", 69);
        WEIGHTS.put("blake2s-160", 70);
        WEIGHTS.put("blake2s-168", 70);
        WEIGHTS.put("blake2s-176", 70);
        WEIGHTS.put("blake2s-184", 70);
        WEIGHTS.put("blake2s-192", 70);
        WEIGHTS.put("blake2s-200", 69);
        WEIGHTS.put("blake2s-208", 70);
        WEIGHTS.put("blake2s-216", 70);
        WEIGHTS.put("blake2s-224", 69);
        WEIGHTS.put("blake2s-232", 69);
        WEIGHTS.put("blake2s-24", 69);
        WEIGHTS.put("blake2s-240", 69);
        WEIGHTS.put("blake2s-248", 70);
        WEIGHTS.put("blake2s-256", 69);
        WEIGHTS.put("blake2s-32", 72);
        WEIGHTS.put("blake2s-40", 70);
        WEIGHTS.put("blake2s-48", 69);
        WEIGHTS.put("blake2s-56", 70);
        WEIGHTS.put("blake2s-64", 70);
        WEIGHTS.put("blake2s-72", 70);
        WEIGHTS.put("blake2s-8", 69);
        WEIGHTS.put("blake2s-80", 70);
        WEIGHTS.put("blake2s-88", 70);
        WEIGHTS.put("blake2s-96", 75);
        WEIGHTS.put("blake2sp", 71);
        WEIGHTS.put("blake3", 95);
        WEIGHTS.put("blake384", 153);
        WEIGHTS.put("blake512", 154);
        WEIGHTS.put("bluemidnightwish224", 79);
        WEIGHTS.put("bluemidnightwish256", 79);
        WEIGHTS.put("bluemidnightwish384", 27);
        WEIGHTS.put("bluemidnightwish512", 27);
        WEIGHTS.put("cksum", 79);
        WEIGHTS.put("cksum_minix", 79);
        WEIGHTS.put("crc16", 71);
        WEIGHTS.put("crc16_minix", 79);
        WEIGHTS.put("crc24", 94);
        WEIGHTS.put("crc32", 5);
        WEIGHTS.put("crc32_bzip2", 94);
        WEIGHTS.put("crc32_fddi", 87);
        WEIGHTS.put("crc32_go-koopman", 87);
        WEIGHTS.put("crc32_mpeg2", 94);
        WEIGHTS.put("crc32_php", 94);
        WEIGHTS.put("crc32_ubi", 91);
        WEIGHTS.put("crc32c", 4);
        WEIGHTS.put("crc64", 66);
        WEIGHTS.put("crc64_ecma", 107);
        WEIGHTS.put("crc64_go-iso", 86);
        WEIGHTS.put("crc64_nvme", 88);
        WEIGHTS.put("crc64_xz", 87);
        WEIGHTS.put("crc8", 57);
        WEIGHTS.put("crc82_darc", 570);
        WEIGHTS.put("cubehash224", 149);
        WEIGHTS.put("cubehash256", 145);
        WEIGHTS.put("cubehash384", 147);
        WEIGHTS.put("cubehash512", 148);
        WEIGHTS.put("dha256", 103);
        WEIGHTS.put("echo224", 173);
        WEIGHTS.put("echo256", 174);
        WEIGHTS.put("echo384", 317);
        WEIGHTS.put("echo512", 315);
        WEIGHTS.put("ed2k", 39);
        WEIGHTS.put("elf", 57);
        WEIGHTS.put("esch256", 22);
        WEIGHTS.put("esch384", 33);
        WEIGHTS.put("fcs16", 79);
        WEIGHTS.put("fletcher16", 74);
        WEIGHTS.put("fnv-0_1024", 1385);
        WEIGHTS.put("fnv-0_128", 339);
        WEIGHTS.put("fnv-0_256", 545);
        WEIGHTS.put("fnv-0_32", 31);
        WEIGHTS.put("fnv-0_512", 825);
        WEIGHTS.put("fnv-0_64", 63);
        WEIGHTS.put("fnv-1_1024", 1385);
        WEIGHTS.put("fnv-1_128", 334);
        WEIGHTS.put("fnv-1_256", 541);
        WEIGHTS.put("fnv-1_32", 31);
        WEIGHTS.put("fnv-1_512", 841);
        WEIGHTS.put("fnv-1_64", 63);
        WEIGHTS.put("fnv-1a_1024", 1372);
        WEIGHTS.put("fnv-1a_128", 320);
        WEIGHTS.put("fnv-1a_256", 529);
        WEIGHTS.put("fnv-1a_32", 31);
        WEIGHTS.put("fnv-1a_512", 852);
        WEIGHTS.put("fnv-1a_64", 63);
        WEIGHTS.put("fork256", 54);
        WEIGHTS.put("fugue224", 172);
        WEIGHTS.put("fugue256", 173);
        WEIGHTS.put("fugue384", 272);
        WEIGHTS.put("fugue512", 359);
        WEIGHTS.put("gost", 1778);
        WEIGHTS.put("gost:crypto-pro", 1767);
        WEIGHTS.put("groestl-224", 235);
        WEIGHTS.put("groestl-256", 235);
        WEIGHTS.put("groestl-384", 437);
        WEIGHTS.put("groestl-512", 438);
        WEIGHTS.put("hamsi224", 193);
        WEIGHTS.put("hamsi256", 194);
        WEIGHTS.put("hamsi384", 528);
        WEIGHTS.put("hamsi512", 527);
        WEIGHTS.put("has160", 66);
        WEIGHTS.put("haval_128_3", 143);
        WEIGHTS.put("haval_128_4", 203);
        WEIGHTS.put("haval_128_5", 255);
        WEIGHTS.put("haval_160_3", 142);
        WEIGHTS.put("haval_160_4", 200);
        WEIGHTS.put("haval_160_5", 255);
        WEIGHTS.put("haval_192_3", 143);
        WEIGHTS.put("haval_192_4", 200);
        WEIGHTS.put("haval_192_5", 256);
        WEIGHTS.put("haval_224_3", 146);
        WEIGHTS.put("haval_224_4", 204);
        WEIGHTS.put("haval_224_5", 258);
        WEIGHTS.put("haval_256_3", 145);
        WEIGHTS.put("haval_256_4", 203);
        WEIGHTS.put("haval_256_5", 259);
        WEIGHTS.put("jh224", 251);
        WEIGHTS.put("jh256", 252);
        WEIGHTS.put("jh384", 251);
        WEIGHTS.put("jh512", 250);
        WEIGHTS.put("joaat", 39);
        WEIGHTS.put("kangarootwelve", 33);
        WEIGHTS.put("keccak224", 42);
        WEIGHTS.put("keccak256", 44);
        WEIGHTS.put("keccak288", 47);
        WEIGHTS.put("keccak384", 56);
        WEIGHTS.put("keccak512", 78);
        WEIGHTS.put("kupyna-256", 431);
        WEIGHTS.put("kupyna-384", 597);
        WEIGHTS.put("kupyna-512", 593);
        WEIGHTS.put("lsh-256-224", 82);
        WEIGHTS.put("lsh-256-256", 81);
        WEIGHTS.put("lsh-512-224", 48);
        WEIGHTS.put("lsh-512-256", 48);
        WEIGHTS.put("lsh-512-384", 49);
        WEIGHTS.put("lsh-512-512", 48);
        WEIGHTS.put("luffa224", 127);
        WEIGHTS.put("luffa256", 128);
        WEIGHTS.put("luffa384", 194);
        WEIGHTS.put("luffa512", 257);
        WEIGHTS.put("marsupilamifourteen", 45);
        WEIGHTS.put("md2", 2903);
        WEIGHTS.put("md4", 38);
        WEIGHTS.put("md5", 35);
        WEIGHTS.put("md6-104", 60);
        WEIGHTS.put("md6-112", 61);
        WEIGHTS.put("md6-120", 62);
        WEIGHTS.put("md6-128", 64);
        WEIGHTS.put("md6-136", 65);
        WEIGHTS.put("md6-144", 67);
        WEIGHTS.put("md6-152", 68);
        WEIGHTS.put("md6-16", 43);
        WEIGHTS.put("md6-160", 70);
        WEIGHTS.put("md6-168", 71);
        WEIGHTS.put("md6-176", 73);
        WEIGHTS.put("md6-184", 74);
        WEIGHTS.put("md6-192", 76);
        WEIGHTS.put("md6-200", 78);
        WEIGHTS.put("md6-208", 79);
        WEIGHTS.put("md6-216", 80);
        WEIGHTS.put("md6-224", 81);
        WEIGHTS.put("md6-232", 83);
        WEIGHTS.put("md6-24", 45);
        WEIGHTS.put("md6-240", 84);
        WEIGHTS.put("md6-248", 86);
        WEIGHTS.put("md6-256", 87);
        WEIGHTS.put("md6-264", 89);
        WEIGHTS.put("md6-272", 90);
        WEIGHTS.put("md6-280", 92);
        WEIGHTS.put("md6-288", 93);
        WEIGHTS.put("md6-296", 95);
        WEIGHTS.put("md6-304", 96);
        WEIGHTS.put("md6-312", 98);
        WEIGHTS.put("md6-32", 47);
        WEIGHTS.put("md6-320", 100);
        WEIGHTS.put("md6-328", 101);
        WEIGHTS.put("md6-336", 103);
        WEIGHTS.put("md6-344", 104);
        WEIGHTS.put("md6-352", 105);
        WEIGHTS.put("md6-360", 106);
        WEIGHTS.put("md6-368", 108);
        WEIGHTS.put("md6-376", 109);
        WEIGHTS.put("md6-384", 111);
        WEIGHTS.put("md6-392", 113);
        WEIGHTS.put("md6-40", 48);
        WEIGHTS.put("md6-400", 114);
        WEIGHTS.put("md6-408", 115);
        WEIGHTS.put("md6-416", 117);
        WEIGHTS.put("md6-424", 119);
        WEIGHTS.put("md6-432", 120);
        WEIGHTS.put("md6-440", 122);
        WEIGHTS.put("md6-448", 123);
        WEIGHTS.put("md6-456", 124);
        WEIGHTS.put("md6-464", 126);
        WEIGHTS.put("md6-472", 127);
        WEIGHTS.put("md6-48", 50);
        WEIGHTS.put("md6-480", 128);
        WEIGHTS.put("md6-488", 130);
        WEIGHTS.put("md6-496", 131);
        WEIGHTS.put("md6-504", 133);
        WEIGHTS.put("md6-512", 134);
        WEIGHTS.put("md6-56", 51);
        WEIGHTS.put("md6-64", 53);
        WEIGHTS.put("md6-72", 54);
        WEIGHTS.put("md6-8", 42);
        WEIGHTS.put("md6-80", 56);
        WEIGHTS.put("md6-88", 57);
        WEIGHTS.put("md6-96", 58);
        WEIGHTS.put("mdc2", 3903);
        WEIGHTS.put("panama", 25);
        WEIGHTS.put("photon-beetle", 5975);
        WEIGHTS.put("prng", 39);
        WEIGHTS.put("radiogatun:32", 35);
        WEIGHTS.put("radiogatun:64", 23);
        WEIGHTS.put("ripemd128", 77);
        WEIGHTS.put("ripemd160", 247);
        WEIGHTS.put("ripemd256", 72);
        WEIGHTS.put("ripemd320", 244);
        WEIGHTS.put("romulush", 28393);
        WEIGHTS.put("sha-1", 11);
        WEIGHTS.put("sha-224", 96);
        WEIGHTS.put("sha-256", 11);
        WEIGHTS.put("sha-384", 19);
        WEIGHTS.put("sha-512", 19);
        WEIGHTS.put("sha-512/224", 19);
        WEIGHTS.put("sha-512/256", 19);
        WEIGHTS.put("sha0", 70);
        WEIGHTS.put("sha3-224", 32);
        WEIGHTS.put("sha3-256", 32);
        WEIGHTS.put("sha3-384", 43);
        WEIGHTS.put("sha3-512", 62);
        WEIGHTS.put("shabal192", 65);
        WEIGHTS.put("shabal224", 66);
        WEIGHTS.put("shabal256", 64);
        WEIGHTS.put("shabal384", 65);
        WEIGHTS.put("shabal512", 73);
        WEIGHTS.put("shake128", 37);
        WEIGHTS.put("shake256", 44);
        WEIGHTS.put("simd224", 248);
        WEIGHTS.put("simd256", 252);
        WEIGHTS.put("simd384", 4437);
        WEIGHTS.put("simd512", 4479);
        WEIGHTS.put("skein-1024-1000", 42);
        WEIGHTS.put("skein-1024-1008", 42);
        WEIGHTS.put("skein-1024-1016", 42);
        WEIGHTS.put("skein-1024-1024", 42);
        WEIGHTS.put("skein-1024-104", 43);
        WEIGHTS.put("skein-1024-112", 43);
        WEIGHTS.put("skein-1024-120", 42);
        WEIGHTS.put("skein-1024-128", 42);
        WEIGHTS.put("skein-1024-136", 42);
        WEIGHTS.put("skein-1024-144", 42);
        WEIGHTS.put("skein-1024-152", 42);
        WEIGHTS.put("skein-1024-16", 42);
        WEIGHTS.put("skein-1024-160", 42);
        WEIGHTS.put("skein-1024-168", 42);
        WEIGHTS.put("skein-1024-176", 42);
        WEIGHTS.put("skein-1024-184", 42);
        WEIGHTS.put("skein-1024-192", 43);
        WEIGHTS.put("skein-1024-200", 42);
        WEIGHTS.put("skein-1024-208", 42);
        WEIGHTS.put("skein-1024-216", 43);
        WEIGHTS.put("skein-1024-224", 42);
        WEIGHTS.put("skein-1024-232", 42);
        WEIGHTS.put("skein-1024-24", 42);
        WEIGHTS.put("skein-1024-240", 43);
        WEIGHTS.put("skein-1024-248", 42);
        WEIGHTS.put("skein-1024-256", 42);
        WEIGHTS.put("skein-1024-264", 42);
        WEIGHTS.put("skein-1024-272", 42);
        WEIGHTS.put("skein-1024-280", 42);
        WEIGHTS.put("skein-1024-288", 42);
        WEIGHTS.put("skein-1024-296", 42);
        WEIGHTS.put("skein-1024-304", 42);
        WEIGHTS.put("skein-1024-312", 42);
        WEIGHTS.put("skein-1024-32", 43);
        WEIGHTS.put("skein-1024-320", 42);
        WEIGHTS.put("skein-1024-328", 42);
        WEIGHTS.put("skein-1024-336", 42);
        WEIGHTS.put("skein-1024-344", 42);
        WEIGHTS.put("skein-1024-352", 44);
        WEIGHTS.put("skein-1024-360", 42);
        WEIGHTS.put("skein-1024-368", 42);
        WEIGHTS.put("skein-1024-376", 42);
        WEIGHTS.put("skein-1024-384", 42);
        WEIGHTS.put("skein-1024-392", 42);
        WEIGHTS.put("skein-1024-40", 42);
        WEIGHTS.put("skein-1024-400", 42);
        WEIGHTS.put("skein-1024-408", 42);
        WEIGHTS.put("skein-1024-416", 42);
        WEIGHTS.put("skein-1024-424", 42);
        WEIGHTS.put("skein-1024-432", 42);
        WEIGHTS.put("skein-1024-440", 42);
        WEIGHTS.put("skein-1024-448", 42);
        WEIGHTS.put("skein-1024-456", 42);
        WEIGHTS.put("skein-1024-464", 42);
        WEIGHTS.put("skein-1024-472", 42);
        WEIGHTS.put("skein-1024-48", 42);
        WEIGHTS.put("skein-1024-480", 44);
        WEIGHTS.put("skein-1024-488", 42);
        WEIGHTS.put("skein-1024-496", 42);
        WEIGHTS.put("skein-1024-504", 42);
        WEIGHTS.put("skein-1024-512", 42);
        WEIGHTS.put("skein-1024-520", 42);
        WEIGHTS.put("skein-1024-528", 42);
        WEIGHTS.put("skein-1024-536", 42);
        WEIGHTS.put("skein-1024-544", 42);
        WEIGHTS.put("skein-1024-552", 42);
        WEIGHTS.put("skein-1024-56", 42);
        WEIGHTS.put("skein-1024-560", 42);
        WEIGHTS.put("skein-1024-568", 42);
        WEIGHTS.put("skein-1024-576", 42);
        WEIGHTS.put("skein-1024-584", 42);
        WEIGHTS.put("skein-1024-592", 43);
        WEIGHTS.put("skein-1024-600", 42);
        WEIGHTS.put("skein-1024-608", 42);
        WEIGHTS.put("skein-1024-616", 42);
        WEIGHTS.put("skein-1024-624", 42);
        WEIGHTS.put("skein-1024-632", 42);
        WEIGHTS.put("skein-1024-64", 42);
        WEIGHTS.put("skein-1024-640", 42);
        WEIGHTS.put("skein-1024-648", 42);
        WEIGHTS.put("skein-1024-656", 42);
        WEIGHTS.put("skein-1024-664", 42);
        WEIGHTS.put("skein-1024-672", 42);
        WEIGHTS.put("skein-1024-680", 42);
        WEIGHTS.put("skein-1024-688", 42);
        WEIGHTS.put("skein-1024-696", 42);
        WEIGHTS.put("skein-1024-704", 42);
        WEIGHTS.put("skein-1024-712", 42);
        WEIGHTS.put("skein-1024-72", 46);
        WEIGHTS.put("skein-1024-720", 43);
        WEIGHTS.put("skein-1024-728", 42);
        WEIGHTS.put("skein-1024-736", 42);
        WEIGHTS.put("skein-1024-744", 42);
        WEIGHTS.put("skein-1024-752", 42);
        WEIGHTS.put("skein-1024-760", 42);
        WEIGHTS.put("skein-1024-768", 42);
        WEIGHTS.put("skein-1024-776", 42);
        WEIGHTS.put("skein-1024-784", 42);
        WEIGHTS.put("skein-1024-792", 42);
        WEIGHTS.put("skein-1024-8", 42);
        WEIGHTS.put("skein-1024-80", 42);
        WEIGHTS.put("skein-1024-800", 42);
        WEIGHTS.put("skein-1024-808", 42);
        WEIGHTS.put("skein-1024-816", 42);
        WEIGHTS.put("skein-1024-824", 42);
        WEIGHTS.put("skein-1024-832", 42);
        WEIGHTS.put("skein-1024-840", 43);
        WEIGHTS.put("skein-1024-848", 42);
        WEIGHTS.put("skein-1024-856", 42);
        WEIGHTS.put("skein-1024-864", 42);
        WEIGHTS.put("skein-1024-872", 43);
        WEIGHTS.put("skein-1024-88", 43);
        WEIGHTS.put("skein-1024-880", 42);
        WEIGHTS.put("skein-1024-888", 42);
        WEIGHTS.put("skein-1024-896", 42);
        WEIGHTS.put("skein-1024-904", 42);
        WEIGHTS.put("skein-1024-912", 42);
        WEIGHTS.put("skein-1024-920", 42);
        WEIGHTS.put("skein-1024-928", 42);
        WEIGHTS.put("skein-1024-936", 42);
        WEIGHTS.put("skein-1024-944", 42);
        WEIGHTS.put("skein-1024-952", 42);
        WEIGHTS.put("skein-1024-96", 43);
        WEIGHTS.put("skein-1024-960", 42);
        WEIGHTS.put("skein-1024-968", 43);
        WEIGHTS.put("skein-1024-976", 43);
        WEIGHTS.put("skein-1024-984", 42);
        WEIGHTS.put("skein-1024-992", 42);
        WEIGHTS.put("skein-256-104", 62);
        WEIGHTS.put("skein-256-112", 62);
        WEIGHTS.put("skein-256-120", 62);
        WEIGHTS.put("skein-256-128", 63);
        WEIGHTS.put("skein-256-136", 62);
        WEIGHTS.put("skein-256-144", 62);
        WEIGHTS.put("skein-256-152", 62);
        WEIGHTS.put("skein-256-16", 62);
        WEIGHTS.put("skein-256-160", 62);
        WEIGHTS.put("skein-256-168", 62);
        WEIGHTS.put("skein-256-176", 63);
        WEIGHTS.put("skein-256-184", 62);
        WEIGHTS.put("skein-256-192", 62);
        WEIGHTS.put("skein-256-200", 62);
        WEIGHTS.put("skein-256-208", 62);
        WEIGHTS.put("skein-256-216", 62);
        WEIGHTS.put("skein-256-224", 62);
        WEIGHTS.put("skein-256-232", 62);
        WEIGHTS.put("skein-256-24", 62);
        WEIGHTS.put("skein-256-240", 62);
        WEIGHTS.put("skein-256-248", 65);
        WEIGHTS.put("skein-256-256", 62);
        WEIGHTS.put("skein-256-32", 62);
        WEIGHTS.put("skein-256-40", 62);
        WEIGHTS.put("skein-256-48", 62);
        WEIGHTS.put("skein-256-56", 62);
        WEIGHTS.put("skein-256-64", 62);
        WEIGHTS.put("skein-256-72", 63);
        WEIGHTS.put("skein-256-8", 62);
        WEIGHTS.put("skein-256-80", 62);
        WEIGHTS.put("skein-256-88", 62);
        WEIGHTS.put("skein-256-96", 62);
        WEIGHTS.put("skein-512-104", 47);
        WEIGHTS.put("skein-512-112", 48);
        WEIGHTS.put("skein-512-120", 47);
        WEIGHTS.put("skein-512-128", 47);
        WEIGHTS.put("skein-512-136", 48);
        WEIGHTS.put("skein-512-144", 49);
        WEIGHTS.put("skein-512-152", 47);
        WEIGHTS.put("skein-512-16", 47);
        WEIGHTS.put("skein-512-160", 48);
        WEIGHTS.put("skein-512-168", 48);
        WEIGHTS.put("skein-512-176", 49);
        WEIGHTS.put("skein-512-184", 48);
        WEIGHTS.put("skein-512-192", 48);
        WEIGHTS.put("skein-512-200", 47);
        WEIGHTS.put("skein-512-208", 47);
        WEIGHTS.put("skein-512-216", 47);
        WEIGHTS.put("skein-512-224", 47);
        WEIGHTS.put("skein-512-232", 47);
        WEIGHTS.put("skein-512-24", 48);
        WEIGHTS.put("skein-512-240", 47);
        WEIGHTS.put("skein-512-248", 47);
        WEIGHTS.put("skein-512-256", 48);
        WEIGHTS.put("skein-512-264", 47);
        WEIGHTS.put("skein-512-272", 48);
        WEIGHTS.put("skein-512-280", 47);
        WEIGHTS.put("skein-512-288", 48);
        WEIGHTS.put("skein-512-296", 48);
        WEIGHTS.put("skein-512-304", 47);
        WEIGHTS.put("skein-512-312", 47);
        WEIGHTS.put("skein-512-32", 47);
        WEIGHTS.put("skein-512-320", 47);
        WEIGHTS.put("skein-512-328", 47);
        WEIGHTS.put("skein-512-336", 47);
        WEIGHTS.put("skein-512-344", 47);
        WEIGHTS.put("skein-512-352", 47);
        WEIGHTS.put("skein-512-360", 47);
        WEIGHTS.put("skein-512-368", 47);
        WEIGHTS.put("skein-512-376", 47);
        WEIGHTS.put("skein-512-384", 47);
        WEIGHTS.put("skein-512-392", 47);
        WEIGHTS.put("skein-512-40", 47);
        WEIGHTS.put("skein-512-400", 47);
        WEIGHTS.put("skein-512-408", 48);
        WEIGHTS.put("skein-512-416", 47);
        WEIGHTS.put("skein-512-424", 47);
        WEIGHTS.put("skein-512-432", 47);
        WEIGHTS.put("skein-512-440", 48);
        WEIGHTS.put("skein-512-448", 47);
        WEIGHTS.put("skein-512-456", 47);
        WEIGHTS.put("skein-512-464", 47);
        WEIGHTS.put("skein-512-472", 47);
        WEIGHTS.put("skein-512-48", 48);
        WEIGHTS.put("skein-512-480", 47);
        WEIGHTS.put("skein-512-488", 47);
        WEIGHTS.put("skein-512-496", 47);
        WEIGHTS.put("skein-512-504", 47);
        WEIGHTS.put("skein-512-512", 49);
        WEIGHTS.put("skein-512-56", 48);
        WEIGHTS.put("skein-512-64", 47);
        WEIGHTS.put("skein-512-72", 47);
        WEIGHTS.put("skein-512-8", 47);
        WEIGHTS.put("skein-512-80", 47);
        WEIGHTS.put("skein-512-88", 47);
        WEIGHTS.put("skein-512-96", 47);
        WEIGHTS.put("sm3", 105);
        WEIGHTS.put("streebog256", 580);
        WEIGHTS.put("streebog512", 583);
        WEIGHTS.put("sum16", 10);
        WEIGHTS.put("sum24", 10);
        WEIGHTS.put("sum32", 10);
        WEIGHTS.put("sum40", 9);
        WEIGHTS.put("sum48", 10);
        WEIGHTS.put("sum56", 10);
        WEIGHTS.put("sum64", 9);
        WEIGHTS.put("sum8", 10);
        WEIGHTS.put("sum_bsd", 26);
        WEIGHTS.put("sum_minix", 25);
        WEIGHTS.put("sum_sysv", 9);
        WEIGHTS.put("tiger", 43);
        WEIGHTS.put("tiger-128-4-php", 61);
        WEIGHTS.put("tiger-160-4-php", 59);
        WEIGHTS.put("tiger-192-4-php", 59);
        WEIGHTS.put("tiger128", 43);
        WEIGHTS.put("tiger160", 43);
        WEIGHTS.put("tiger2", 43);
        WEIGHTS.put("tree:tiger", 52);
        WEIGHTS.put("tree:tiger2", 51);
        WEIGHTS.put("vsh", 16936);
        WEIGHTS.put("whirlpool0", 368);
        WEIGHTS.put("whirlpool1", 368);
        WEIGHTS.put("whirlpool2", 368);
        WEIGHTS.put("xoodyak", 27);
        WEIGHTS.put("xor8", 9);
        WEIGHTS.put("xxhash32", 8);

// aliases
        WEIGHTS_ALIASES.put("simd-256", WEIGHTS.get("simd256"));
        WEIGHTS_ALIASES.put("fugue-256", WEIGHTS.get("fugue256"));
        WEIGHTS_ALIASES.put("b2sum-456", WEIGHTS.get("blake2b-456"));
        WEIGHTS_ALIASES.put("b2sum-56", WEIGHTS.get("blake2b-56"));
        WEIGHTS_ALIASES.put("b2sum-216", WEIGHTS.get("blake2b-216"));
        WEIGHTS_ALIASES.put("echo-224", WEIGHTS.get("echo224"));
        WEIGHTS_ALIASES.put("b2sum-336", WEIGHTS.get("blake2b-336"));
        WEIGHTS_ALIASES.put("simd-384", WEIGHTS.get("simd384"));
        WEIGHTS_ALIASES.put("b2sum-8", WEIGHTS.get("blake2b-8"));
        WEIGHTS_ALIASES.put("b2sum-320", WEIGHTS.get("blake2b-320"));
        WEIGHTS_ALIASES.put("b2sum-440", WEIGHTS.get("blake2b-440"));
        WEIGHTS_ALIASES.put("b2sum-200", WEIGHTS.get("blake2b-200"));
        WEIGHTS_ALIASES.put("fugue-384", WEIGHTS.get("fugue384"));
        WEIGHTS_ALIASES.put("mdc-2", WEIGHTS.get("mdc2"));
        WEIGHTS_ALIASES.put("sha-3-224", WEIGHTS.get("sha3-224"));
        WEIGHTS_ALIASES.put("b2sum-328", WEIGHTS.get("blake2b-328"));
        WEIGHTS_ALIASES.put("blake3-256", WEIGHTS.get("blake3"));
        WEIGHTS_ALIASES.put("b3sum", WEIGHTS.get("blake3"));
        WEIGHTS_ALIASES.put("b2sum-448", WEIGHTS.get("blake2b-448"));
        WEIGHTS_ALIASES.put("b2sum-64", WEIGHTS.get("blake2b-64"));
        WEIGHTS_ALIASES.put("b2sum-208", WEIGHTS.get("blake2b-208"));
        WEIGHTS_ALIASES.put("ast", WEIGHTS.get("aststrsum"));
        WEIGHTS_ALIASES.put("strsum", WEIGHTS.get("aststrsum"));
        WEIGHTS_ALIASES.put("crc-64_nvme", WEIGHTS.get("crc64_nvme"));
        WEIGHTS_ALIASES.put("streebog-512", WEIGHTS.get("streebog512"));
        WEIGHTS_ALIASES.put("shabal-224", WEIGHTS.get("shabal224"));
        WEIGHTS_ALIASES.put("b2sum-232", WEIGHTS.get("blake2b-232"));
        WEIGHTS_ALIASES.put("b2sum-352", WEIGHTS.get("blake2b-352"));
        WEIGHTS_ALIASES.put("b2sum-112", WEIGHTS.get("blake2b-112"));
        WEIGHTS_ALIASES.put("crc-64_xz", WEIGHTS.get("crc64_xz"));
        WEIGHTS_ALIASES.put("crc64_go-ecma", WEIGHTS.get("crc64_xz"));
        WEIGHTS_ALIASES.put("crc-64_go-ecma", WEIGHTS.get("crc64_xz"));
        WEIGHTS_ALIASES.put("b2sum-472", WEIGHTS.get("blake2b-472"));
        WEIGHTS_ALIASES.put("b2sum-32", WEIGHTS.get("blake2b-32"));
        WEIGHTS_ALIASES.put("tiger_160_4_php", WEIGHTS.get("tiger-160-4-php"));
        WEIGHTS_ALIASES.put("dha-256", WEIGHTS.get("dha256"));
        WEIGHTS_ALIASES.put("b2sum-48", WEIGHTS.get("blake2b-48"));
        WEIGHTS_ALIASES.put("bluemidnightwish-512", WEIGHTS.get("bluemidnightwish512"));
        WEIGHTS_ALIASES.put("tiger-160", WEIGHTS.get("tiger160"));
        WEIGHTS_ALIASES.put("b2sum-344", WEIGHTS.get("blake2b-344"));
        WEIGHTS_ALIASES.put("b2sum-464", WEIGHTS.get("blake2b-464"));
        WEIGHTS_ALIASES.put("rg32", WEIGHTS.get("radiogatun:32"));
        WEIGHTS_ALIASES.put("rg-32", WEIGHTS.get("radiogatun:32"));
        WEIGHTS_ALIASES.put("b2sum-40", WEIGHTS.get("blake2b-40"));
        WEIGHTS_ALIASES.put("b2sum-104", WEIGHTS.get("blake2b-104"));
        WEIGHTS_ALIASES.put("b2sum-224", WEIGHTS.get("blake2b-224"));
        WEIGHTS_ALIASES.put("b2sum-16", WEIGHTS.get("blake2b-16"));
        WEIGHTS_ALIASES.put("sum-48", WEIGHTS.get("sum48"));
        WEIGHTS_ALIASES.put("fletcher-16", WEIGHTS.get("fletcher16"));
        WEIGHTS_ALIASES.put("sum-40", WEIGHTS.get("sum40"));
        WEIGHTS_ALIASES.put("luffa-512", WEIGHTS.get("luffa512"));
        WEIGHTS_ALIASES.put("b2sum-496", WEIGHTS.get("blake2b-496"));
        WEIGHTS_ALIASES.put("b2sum-256", WEIGHTS.get("blake2b-256"));
        WEIGHTS_ALIASES.put("b2sum-376", WEIGHTS.get("blake2b-376"));
        WEIGHTS_ALIASES.put("sha-3-256", WEIGHTS.get("sha3-256"));
        WEIGHTS_ALIASES.put("radiogatun", WEIGHTS.get("radiogatun:64"));
        WEIGHTS_ALIASES.put("rg64", WEIGHTS.get("radiogatun:64"));
        WEIGHTS_ALIASES.put("rg-64", WEIGHTS.get("radiogatun:64"));
        WEIGHTS_ALIASES.put("whirlpool", WEIGHTS.get("whirlpool2"));
        WEIGHTS_ALIASES.put("whirlpool-2", WEIGHTS.get("whirlpool2"));
        WEIGHTS_ALIASES.put("b2sum-136", WEIGHTS.get("blake2b-136"));
        WEIGHTS_ALIASES.put("elf32", WEIGHTS.get("elf"));
        WEIGHTS_ALIASES.put("elf-32", WEIGHTS.get("elf"));
        WEIGHTS_ALIASES.put("whirlpool-0", WEIGHTS.get("whirlpool0"));
        WEIGHTS_ALIASES.put("whirlpool-l", WEIGHTS.get("whirlpool1"));
        WEIGHTS_ALIASES.put("whirlpool-t", WEIGHTS.get("whirlpool1"));
        WEIGHTS_ALIASES.put("simd-224", WEIGHTS.get("simd224"));
        WEIGHTS_ALIASES.put("skein-1024", WEIGHTS.get("skein-1024-1024"));
        WEIGHTS_ALIASES.put("skein1024", WEIGHTS.get("skein-1024-1024"));
        WEIGHTS_ALIASES.put("sum-56", WEIGHTS.get("sum56"));
        WEIGHTS_ALIASES.put("tiger_192_4_php", WEIGHTS.get("tiger-192-4-php"));
        WEIGHTS_ALIASES.put("crc-16_minix", WEIGHTS.get("crc16_minix"));
        WEIGHTS_ALIASES.put("sha-3-384", WEIGHTS.get("sha3-384"));
        WEIGHTS_ALIASES.put("b2sum-360", WEIGHTS.get("blake2b-360"));
        WEIGHTS_ALIASES.put("b2sum-480", WEIGHTS.get("blake2b-480"));
        WEIGHTS_ALIASES.put("b2sum-120", WEIGHTS.get("blake2b-120"));
        WEIGHTS_ALIASES.put("b2sum-240", WEIGHTS.get("blake2b-240"));
        WEIGHTS_ALIASES.put("b2sum-368", WEIGHTS.get("blake2b-368"));
        WEIGHTS_ALIASES.put("keccak-288", WEIGHTS.get("keccak288"));
        WEIGHTS_ALIASES.put("b2sum-488", WEIGHTS.get("blake2b-488"));
        WEIGHTS_ALIASES.put("b2sum-24", WEIGHTS.get("blake2b-24"));
        WEIGHTS_ALIASES.put("b2sum-128", WEIGHTS.get("blake2b-128"));
        WEIGHTS_ALIASES.put("b2sum-248", WEIGHTS.get("blake2b-248"));
        WEIGHTS_ALIASES.put("sum_plan9", WEIGHTS.get("crc32_fddi"));
        WEIGHTS_ALIASES.put("sum-64", WEIGHTS.get("sum64"));
        WEIGHTS_ALIASES.put("b2sum-160", WEIGHTS.get("blake2b-160"));
        WEIGHTS_ALIASES.put("crc-8", WEIGHTS.get("crc8"));
        WEIGHTS_ALIASES.put("b2sum-280", WEIGHTS.get("blake2b-280"));
        WEIGHTS_ALIASES.put("b2sum-272", WEIGHTS.get("blake2b-272"));
        WEIGHTS_ALIASES.put("b2sum-392", WEIGHTS.get("blake2b-392"));
        WEIGHTS_ALIASES.put("shabal-384", WEIGHTS.get("shabal384"));
        WEIGHTS_ALIASES.put("b2sum-152", WEIGHTS.get("blake2b-152"));
        WEIGHTS_ALIASES.put("ripemd-320", WEIGHTS.get("ripemd320"));
        WEIGHTS_ALIASES.put("ripe-md320", WEIGHTS.get("ripemd320"));
        WEIGHTS_ALIASES.put("rmd320", WEIGHTS.get("ripemd320"));
        WEIGHTS_ALIASES.put("rmd-320", WEIGHTS.get("ripemd320"));
        WEIGHTS_ALIASES.put("tiger-128", WEIGHTS.get("tiger128"));
        WEIGHTS_ALIASES.put("shabal-256", WEIGHTS.get("shabal256"));
        WEIGHTS_ALIASES.put("crc-64_go-iso", WEIGHTS.get("crc64_go-iso"));
        WEIGHTS_ALIASES.put("b2sum-144", WEIGHTS.get("blake2b-144"));
        WEIGHTS_ALIASES.put("b2sum-264", WEIGHTS.get("blake2b-264"));
        WEIGHTS_ALIASES.put("crc-32c", WEIGHTS.get("crc32c"));
        WEIGHTS_ALIASES.put("b2sum-384", WEIGHTS.get("blake2b-384"));
        WEIGHTS_ALIASES.put("keccak-384", WEIGHTS.get("keccak384"));
        WEIGHTS_ALIASES.put("groestl384", WEIGHTS.get("groestl-384"));
        WEIGHTS_ALIASES.put("hamsi-256", WEIGHTS.get("hamsi256"));
        WEIGHTS_ALIASES.put("skein-256", WEIGHTS.get("skein-256-256"));
        WEIGHTS_ALIASES.put("skein256", WEIGHTS.get("skein-256-256"));
        WEIGHTS_ALIASES.put("jh-512", WEIGHTS.get("jh512"));
        WEIGHTS_ALIASES.put("keccak-256", WEIGHTS.get("keccak256"));
        WEIGHTS_ALIASES.put("bluemidnightwish-256", WEIGHTS.get("bluemidnightwish256"));
        WEIGHTS_ALIASES.put("cubehash-256", WEIGHTS.get("cubehash256"));
        WEIGHTS_ALIASES.put("groestl256", WEIGHTS.get("groestl-256"));
        WEIGHTS_ALIASES.put("sha-0", WEIGHTS.get("sha0"));
        WEIGHTS_ALIASES.put("crc-82_darc", WEIGHTS.get("crc82_darc"));
        WEIGHTS_ALIASES.put("crc-82", WEIGHTS.get("crc82_darc"));
        WEIGHTS_ALIASES.put("crc82", WEIGHTS.get("crc82_darc"));
        WEIGHTS_ALIASES.put("bluemidnightwish-384", WEIGHTS.get("bluemidnightwish384"));
        WEIGHTS_ALIASES.put("bluemidnightwish-224", WEIGHTS.get("bluemidnightwish224"));
        WEIGHTS_ALIASES.put("vsh-1024", WEIGHTS.get("vsh"));
        WEIGHTS_ALIASES.put("cubehash-384", WEIGHTS.get("cubehash384"));
        WEIGHTS_ALIASES.put("kangaroo12", WEIGHTS.get("kangarootwelve"));
        WEIGHTS_ALIASES.put("k12", WEIGHTS.get("kangarootwelve"));
        WEIGHTS_ALIASES.put("fugue-512", WEIGHTS.get("fugue512"));
        WEIGHTS_ALIASES.put("blake2b", WEIGHTS.get("blake2b-512"));
        WEIGHTS_ALIASES.put("b2sum-512", WEIGHTS.get("blake2b-512"));
        WEIGHTS_ALIASES.put("b2sum", WEIGHTS.get("blake2b-512"));
        WEIGHTS_ALIASES.put("echo-512", WEIGHTS.get("echo512"));
        WEIGHTS_ALIASES.put("keccak-224", WEIGHTS.get("keccak224"));
        WEIGHTS_ALIASES.put("luffa-224", WEIGHTS.get("luffa224"));
        WEIGHTS_ALIASES.put("b2sum-504", WEIGHTS.get("blake2b-504"));
        WEIGHTS_ALIASES.put("gost:default", WEIGHTS.get("gost"));
        WEIGHTS_ALIASES.put("md2sum", WEIGHTS.get("md2"));
        WEIGHTS_ALIASES.put("md4sum", WEIGHTS.get("md4"));
        WEIGHTS_ALIASES.put("hamsi-384", WEIGHTS.get("hamsi384"));
        WEIGHTS_ALIASES.put("romulus-h", WEIGHTS.get("romulush"));
        WEIGHTS_ALIASES.put("md5sum", WEIGHTS.get("md5"));
        WEIGHTS_ALIASES.put("crc-32_mpeg-2", WEIGHTS.get("crc32_mpeg2"));
        WEIGHTS_ALIASES.put("blake2bp-512", WEIGHTS.get("blake2bp"));
        WEIGHTS_ALIASES.put("groestl224", WEIGHTS.get("groestl-224"));
        WEIGHTS_ALIASES.put("tth2", WEIGHTS.get("tree:tiger2"));
        WEIGHTS_ALIASES.put("b2sum-416", WEIGHTS.get("blake2b-416"));
        WEIGHTS_ALIASES.put("b2sum-96", WEIGHTS.get("blake2b-96"));
        WEIGHTS_ALIASES.put("b2sum-408", WEIGHTS.get("blake2b-408"));
        WEIGHTS_ALIASES.put("shabal-192", WEIGHTS.get("shabal192"));
        WEIGHTS_ALIASES.put("b2sum-400", WEIGHTS.get("blake2b-400"));
        WEIGHTS_ALIASES.put("fugue-224", WEIGHTS.get("fugue224"));
        WEIGHTS_ALIASES.put("luffa-256", WEIGHTS.get("luffa256"));
        WEIGHTS_ALIASES.put("cubehash-224", WEIGHTS.get("cubehash224"));
        WEIGHTS_ALIASES.put("fork-256", WEIGHTS.get("fork256"));
        WEIGHTS_ALIASES.put("b2sum-432", WEIGHTS.get("blake2b-432"));
        WEIGHTS_ALIASES.put("sha", WEIGHTS.get("sha-1"));
        WEIGHTS_ALIASES.put("sha1", WEIGHTS.get("sha-1"));
        WEIGHTS_ALIASES.put("sha1sum", WEIGHTS.get("sha-1"));
        WEIGHTS_ALIASES.put("sha160", WEIGHTS.get("sha-1"));
        WEIGHTS_ALIASES.put("sha-160", WEIGHTS.get("sha-1"));
        WEIGHTS_ALIASES.put("dss1", WEIGHTS.get("sha-1"));
        WEIGHTS_ALIASES.put("crc-32_php", WEIGHTS.get("crc32_php"));
        WEIGHTS_ALIASES.put("b2sum-72", WEIGHTS.get("blake2b-72"));
        WEIGHTS_ALIASES.put("b2sum-312", WEIGHTS.get("blake2b-312"));
        WEIGHTS_ALIASES.put("luffa-384", WEIGHTS.get("luffa384"));
        WEIGHTS_ALIASES.put("jh-224", WEIGHTS.get("jh224"));
        WEIGHTS_ALIASES.put("b2sum-80", WEIGHTS.get("blake2b-80"));
        WEIGHTS_ALIASES.put("b2sum-88", WEIGHTS.get("blake2b-88"));
        WEIGHTS_ALIASES.put("b2sum-304", WEIGHTS.get("blake2b-304"));
        WEIGHTS_ALIASES.put("b2sum-424", WEIGHTS.get("blake2b-424"));
        WEIGHTS_ALIASES.put("haval", WEIGHTS.get("haval_128_3"));
        WEIGHTS_ALIASES.put("tree:tiger192", WEIGHTS.get("tree:tiger"));
        WEIGHTS_ALIASES.put("tree:tiger-192", WEIGHTS.get("tree:tiger"));
        WEIGHTS_ALIASES.put("tth", WEIGHTS.get("tree:tiger"));
        WEIGHTS_ALIASES.put("sha512", WEIGHTS.get("sha-512"));
        WEIGHTS_ALIASES.put("tiger192", WEIGHTS.get("tiger"));
        WEIGHTS_ALIASES.put("tiger-192", WEIGHTS.get("tiger"));
        WEIGHTS_ALIASES.put("fcs-16", WEIGHTS.get("fcs16"));
        WEIGHTS_ALIASES.put("crc16_x25", WEIGHTS.get("fcs16"));
        WEIGHTS_ALIASES.put("crc-16_x-25", WEIGHTS.get("fcs16"));
        WEIGHTS_ALIASES.put("crc-64", WEIGHTS.get("crc64"));
        WEIGHTS_ALIASES.put("keccak-512", WEIGHTS.get("keccak512"));
        WEIGHTS_ALIASES.put("jh-256", WEIGHTS.get("jh256"));
        WEIGHTS_ALIASES.put("blake-512", WEIGHTS.get("blake512"));
        WEIGHTS_ALIASES.put("sha512/224", WEIGHTS.get("sha-512/224"));
        WEIGHTS_ALIASES.put("sha512t224", WEIGHTS.get("sha-512/224"));
        WEIGHTS_ALIASES.put("sumsysv", WEIGHTS.get("sum_sysv"));
        WEIGHTS_ALIASES.put("sysv", WEIGHTS.get("sum_sysv"));
        WEIGHTS_ALIASES.put("sysvsum", WEIGHTS.get("sum_sysv"));
        WEIGHTS_ALIASES.put("jh-384", WEIGHTS.get("jh384"));
        WEIGHTS_ALIASES.put("streebog-256", WEIGHTS.get("streebog256"));
        WEIGHTS_ALIASES.put("sha512/256", WEIGHTS.get("sha-512/256"));
        WEIGHTS_ALIASES.put("sha512t256", WEIGHTS.get("sha-512/256"));
        WEIGHTS_ALIASES.put("xxh32", WEIGHTS.get("xxhash32"));
        WEIGHTS_ALIASES.put("crc-64_ecma", WEIGHTS.get("crc64_ecma"));
        WEIGHTS_ALIASES.put("skein-512", WEIGHTS.get("skein-512-512"));
        WEIGHTS_ALIASES.put("skein512", WEIGHTS.get("skein-512-512"));
        WEIGHTS_ALIASES.put("crc-32", WEIGHTS.get("crc32"));
        WEIGHTS_ALIASES.put("fcs32", WEIGHTS.get("crc32"));
        WEIGHTS_ALIASES.put("fcs-32", WEIGHTS.get("crc32"));
        WEIGHTS_ALIASES.put("tiger_128_4_php", WEIGHTS.get("tiger-128-4-php"));
        WEIGHTS_ALIASES.put("crc-32_bzip2", WEIGHTS.get("crc32_bzip2"));
        WEIGHTS_ALIASES.put("crc-32_bzip-2", WEIGHTS.get("crc32_bzip2"));
        WEIGHTS_ALIASES.put("crc-24", WEIGHTS.get("crc24"));
        WEIGHTS_ALIASES.put("crc-32_go-koopman", WEIGHTS.get("crc32_go-koopman"));
        WEIGHTS_ALIASES.put("has-160", WEIGHTS.get("has160"));
        WEIGHTS_ALIASES.put("joaat32", WEIGHTS.get("joaat"));
        WEIGHTS_ALIASES.put("joaat-32", WEIGHTS.get("joaat"));
        WEIGHTS_ALIASES.put("cubehash-512", WEIGHTS.get("cubehash512"));
        WEIGHTS_ALIASES.put("hamsi-224", WEIGHTS.get("hamsi224"));
        WEIGHTS_ALIASES.put("groestl512", WEIGHTS.get("groestl-512"));
        WEIGHTS_ALIASES.put("sha224", WEIGHTS.get("sha-224"));
        WEIGHTS_ALIASES.put("xor-8", WEIGHTS.get("xor8"));
        WEIGHTS_ALIASES.put("b2sum-176", WEIGHTS.get("blake2b-176"));
        WEIGHTS_ALIASES.put("b2sum-296", WEIGHTS.get("blake2b-296"));
        WEIGHTS_ALIASES.put("blake2s", WEIGHTS.get("blake2s-256"));
        WEIGHTS_ALIASES.put("marsupilami14", WEIGHTS.get("marsupilamifourteen"));
        WEIGHTS_ALIASES.put("m14", WEIGHTS.get("marsupilamifourteen"));
        WEIGHTS_ALIASES.put("blake-256", WEIGHTS.get("blake256"));
        WEIGHTS_ALIASES.put("blake-384", WEIGHTS.get("blake384"));
        WEIGHTS_ALIASES.put("b2sum-168", WEIGHTS.get("blake2b-168"));
        WEIGHTS_ALIASES.put("b2sum-288", WEIGHTS.get("blake2b-288"));
        WEIGHTS_ALIASES.put("sum-16", WEIGHTS.get("sum16"));
        WEIGHTS_ALIASES.put("ripemd-128", WEIGHTS.get("ripemd128"));
        WEIGHTS_ALIASES.put("ripe-md128", WEIGHTS.get("ripemd128"));
        WEIGHTS_ALIASES.put("rmd128", WEIGHTS.get("ripemd128"));
        WEIGHTS_ALIASES.put("rmd-128", WEIGHTS.get("ripemd128"));
        WEIGHTS_ALIASES.put("sum-24", WEIGHTS.get("sum24"));
        WEIGHTS_ALIASES.put("sha-3-512", WEIGHTS.get("sha3-512"));
        WEIGHTS_ALIASES.put("simd-512", WEIGHTS.get("simd512"));
        WEIGHTS_ALIASES.put("crc-16", WEIGHTS.get("crc16"));
        WEIGHTS_ALIASES.put("sum-32", WEIGHTS.get("sum32"));
        WEIGHTS_ALIASES.put("ripemd-256", WEIGHTS.get("ripemd256"));
        WEIGHTS_ALIASES.put("ripe-md256", WEIGHTS.get("ripemd256"));
        WEIGHTS_ALIASES.put("rmd256", WEIGHTS.get("ripemd256"));
        WEIGHTS_ALIASES.put("rmd-256", WEIGHTS.get("ripemd256"));
        WEIGHTS_ALIASES.put("b2sum-192", WEIGHTS.get("blake2b-192"));
        WEIGHTS_ALIASES.put("b2sum-184", WEIGHTS.get("blake2b-184"));
        WEIGHTS_ALIASES.put("emule", WEIGHTS.get("ed2k"));
        WEIGHTS_ALIASES.put("edonkey", WEIGHTS.get("ed2k"));
        WEIGHTS_ALIASES.put("sum-8", WEIGHTS.get("sum8"));
        WEIGHTS_ALIASES.put("sumbsd", WEIGHTS.get("sum_bsd"));
        WEIGHTS_ALIASES.put("bsd", WEIGHTS.get("sum_bsd"));
        WEIGHTS_ALIASES.put("bsdsum", WEIGHTS.get("sum_bsd"));
        WEIGHTS_ALIASES.put("blake-224", WEIGHTS.get("blake224"));
        WEIGHTS_ALIASES.put("blake2sp-256", WEIGHTS.get("blake2sp"));
        WEIGHTS_ALIASES.put("echo-384", WEIGHTS.get("echo384"));
        WEIGHTS_ALIASES.put("sha384", WEIGHTS.get("sha-384"));
        WEIGHTS_ALIASES.put("shabal-512", WEIGHTS.get("shabal512"));
        WEIGHTS_ALIASES.put("echo-256", WEIGHTS.get("echo256"));
        WEIGHTS_ALIASES.put("crc-32_ubi", WEIGHTS.get("crc32_ubi"));
        WEIGHTS_ALIASES.put("ubicrc32", WEIGHTS.get("crc32_ubi"));
        WEIGHTS_ALIASES.put("crc32_jamcrc", WEIGHTS.get("crc32_ubi"));
        WEIGHTS_ALIASES.put("crc-32_jamcrc", WEIGHTS.get("crc32_ubi"));
        WEIGHTS_ALIASES.put("hamsi-512", WEIGHTS.get("hamsi512"));
        WEIGHTS_ALIASES.put("ripemd-160", WEIGHTS.get("ripemd160"));
        WEIGHTS_ALIASES.put("ripe-md160", WEIGHTS.get("ripemd160"));
        WEIGHTS_ALIASES.put("rmd160", WEIGHTS.get("ripemd160"));
        WEIGHTS_ALIASES.put("rmd-160", WEIGHTS.get("ripemd160"));
        WEIGHTS_ALIASES.put("sha256", WEIGHTS.get("sha-256"));
        WEIGHTS_ALIASES.put("adler32", WEIGHTS.get("adler32"));
        WEIGHTS_ALIASES.put("adler-32", WEIGHTS.get("adler32"));
        WEIGHTS_ALIASES.put("belthash", WEIGHTS.get("belt-hash"));
        WEIGHTS_ALIASES.put("belt", WEIGHTS.get("belt-hash"));
        WEIGHTS_ALIASES.put("md6", WEIGHTS.get("md6-256"));
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
//        byte[] randomBytes = new byte[64];

        // Mit sicheren Zufallszahlen füllen (wg. HMAC)
//        java.security.SecureRandom secureRandom = new java.security.SecureRandom();
//        secureRandom.nextBytes(randomBytes);
//        HashFunctionFactory.setKey(randomBytes);

        if (args.length > 1) {
            hashes = new TreeSet<>();
            for (int i = 1; i < args.length; i++) {
                hashes.add(args[i]);
            }
        } else {
            hashes = JacksumAPI.getAvailableAlgorithms().keySet();
//            hashes = new HashSet<>(JacksumAPI.getAvailableAlgorithms().keySet());
//            hashes.addAll(JacksumAPI.getAvailableHMACs().keySet());
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
