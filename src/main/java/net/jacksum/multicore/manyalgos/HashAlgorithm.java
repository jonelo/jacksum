/*


  Jacksum 3.3.0 - a checksum utility in Java
  Copyright (c) 2001-2022 Dipl.-Inf. (FH) Johann N. Löfflmann,
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

    private static final Map<String, Integer> WEIGHTS = new HashMap<>(640); // max. number of entries/load factor = 480/0.75=640
    private static final Map<String, Integer> WEIGHTS_ALIASES = new HashMap<>(333);
    private final String name;
    private final int weight;
    private final AbstractChecksum cs;

    /*
     * This weight information is used to balance the load among different
     * worker threads.
     * To regenerate this information use the main method in this class.
     * java -classpath jacksum.jar net.jacksum.multicore.manyalgos.HashAlgorithm <20 MB+ file> [algoname]
     */
    static {

        // GENERATION START
        WEIGHTS.put("adler32", 16);
        WEIGHTS.put("blake224", 250);
        WEIGHTS.put("blake256", 250);
        WEIGHTS.put("blake384", 172);
        WEIGHTS.put("blake512", 172);
        WEIGHTS.put("blake2b-8", 77);
        WEIGHTS.put("blake2b-16", 77);
        WEIGHTS.put("blake2b-24", 77);
        WEIGHTS.put("blake2b-32", 77);
        WEIGHTS.put("blake2b-40", 80);
        WEIGHTS.put("blake2b-48", 78);
        WEIGHTS.put("blake2b-56", 77);
        WEIGHTS.put("blake2b-64", 76);
        WEIGHTS.put("blake2b-72", 80);
        WEIGHTS.put("blake2b-80", 80);
        WEIGHTS.put("blake2b-88", 79);
        WEIGHTS.put("blake2b-96", 78);
        WEIGHTS.put("blake2b-104", 81);
        WEIGHTS.put("blake2b-112", 78);
        WEIGHTS.put("blake2b-120", 78);
        WEIGHTS.put("blake2b-128", 78);
        WEIGHTS.put("blake2b-136", 81);
        WEIGHTS.put("blake2b-144", 77);
        WEIGHTS.put("blake2b-152", 77);
        WEIGHTS.put("blake2b-160", 77);
        WEIGHTS.put("blake2b-168", 77);
        WEIGHTS.put("blake2b-176", 78);
        WEIGHTS.put("blake2b-184", 77);
        WEIGHTS.put("blake2b-192", 78);
        WEIGHTS.put("blake2b-200", 80);
        WEIGHTS.put("blake2b-208", 78);
        WEIGHTS.put("blake2b-216", 77);
        WEIGHTS.put("blake2b-224", 79);
        WEIGHTS.put("blake2b-232", 80);
        WEIGHTS.put("blake2b-240", 79);
        WEIGHTS.put("blake2b-248", 78);
        WEIGHTS.put("blake2b-256", 77);
        WEIGHTS.put("blake2b-264", 79);
        WEIGHTS.put("blake2b-272", 77);
        WEIGHTS.put("blake2b-280", 78);
        WEIGHTS.put("blake2b-288", 78);
        WEIGHTS.put("blake2b-296", 79);
        WEIGHTS.put("blake2b-304", 78);
        WEIGHTS.put("blake2b-312", 78);
        WEIGHTS.put("blake2b-320", 77);
        WEIGHTS.put("blake2b-328", 81);
        WEIGHTS.put("blake2b-336", 79);
        WEIGHTS.put("blake2b-344", 77);
        WEIGHTS.put("blake2b-352", 79);
        WEIGHTS.put("blake2b-360", 81);
        WEIGHTS.put("blake2b-368", 82);
        WEIGHTS.put("blake2b-376", 80);
        WEIGHTS.put("blake2b-384", 84);
        WEIGHTS.put("blake2b-392", 79);
        WEIGHTS.put("blake2b-400", 79);
        WEIGHTS.put("blake2b-408", 77);
        WEIGHTS.put("blake2b-416", 80);
        WEIGHTS.put("blake2b-424", 81);
        WEIGHTS.put("blake2b-432", 79);
        WEIGHTS.put("blake2b-440", 77);
        WEIGHTS.put("blake2b-448", 76);
        WEIGHTS.put("blake2b-456", 78);
        WEIGHTS.put("blake2b-464", 78);
        WEIGHTS.put("blake2b-472", 77);
        WEIGHTS.put("blake2b-480", 78);
        WEIGHTS.put("blake2b-488", 78);
        WEIGHTS.put("blake2b-496", 78);
        WEIGHTS.put("blake2b-504", 79);
        WEIGHTS.put("blake2b-512", 79);
        WEIGHTS.put("blake2s-8", 124);
        WEIGHTS.put("blake2s-16", 124);
        WEIGHTS.put("blake2s-24", 123);
        WEIGHTS.put("blake2s-32", 122);
        WEIGHTS.put("blake2s-40", 126);
        WEIGHTS.put("blake2s-48", 124);
        WEIGHTS.put("blake2s-56", 123);
        WEIGHTS.put("blake2s-64", 127);
        WEIGHTS.put("blake2s-72", 124);
        WEIGHTS.put("blake2s-80", 122);
        WEIGHTS.put("blake2s-88", 123);
        WEIGHTS.put("blake2s-96", 124);
        WEIGHTS.put("blake2s-104", 123);
        WEIGHTS.put("blake2s-112", 123);
        WEIGHTS.put("blake2s-120", 123);
        WEIGHTS.put("blake2s-128", 122);
        WEIGHTS.put("blake2s-136", 122);
        WEIGHTS.put("blake2s-144", 127);
        WEIGHTS.put("blake2s-152", 125);
        WEIGHTS.put("blake2s-160", 125);
        WEIGHTS.put("blake2s-168", 123);
        WEIGHTS.put("blake2s-176", 123);
        WEIGHTS.put("blake2s-184", 123);
        WEIGHTS.put("blake2s-192", 124);
        WEIGHTS.put("blake2s-200", 124);
        WEIGHTS.put("blake2s-208", 123);
        WEIGHTS.put("blake2s-216", 123);
        WEIGHTS.put("blake2s-224", 125);
        WEIGHTS.put("blake2s-232", 123);
        WEIGHTS.put("blake2s-240", 124);
        WEIGHTS.put("blake2s-248", 123);
        WEIGHTS.put("blake2s-256", 123);
        WEIGHTS.put("blake3", 122);
        WEIGHTS.put("cksum_minix", 63);
        WEIGHTS.put("cksum", 63);
        WEIGHTS.put("crc8", 55);
        WEIGHTS.put("crc16", 58);
        WEIGHTS.put("crc16_minix", 69);
        WEIGHTS.put("crc24", 65);
        WEIGHTS.put("crc32", 18);
        WEIGHTS.put("crc32c", 17);
        WEIGHTS.put("crc32_mpeg2", 63);
        WEIGHTS.put("crc32_bzip2", 64);
        WEIGHTS.put("crc32_fddi", 65);
        WEIGHTS.put("crc32_ubi", 64);
        WEIGHTS.put("crc32_php", 65);
        WEIGHTS.put("crc64", 64);
        WEIGHTS.put("crc64_ecma", 64);
        WEIGHTS.put("crc64_go-iso", 64);
        WEIGHTS.put("crc64_xz", 64);
        WEIGHTS.put("dha256", 154);
        WEIGHTS.put("echo224", 391);
        WEIGHTS.put("echo256", 388);
        WEIGHTS.put("echo384", 739);
        WEIGHTS.put("echo512", 735);
        WEIGHTS.put("ed2k", 46);
        WEIGHTS.put("elf", 57);
        WEIGHTS.put("fcs16", 65);
        WEIGHTS.put("fletcher16", 90);
        WEIGHTS.put("fnv-0_32", 34);
        WEIGHTS.put("fnv-0_64", 4864);
        WEIGHTS.put("fnv-0_128", 6479);
        WEIGHTS.put("fnv-0_256", 10795);
        WEIGHTS.put("fnv-0_512", 22988);
        WEIGHTS.put("fnv-0_1024", 62040);
        WEIGHTS.put("fnv-1_32", 34);
        WEIGHTS.put("fnv-1_64", 4844);
        WEIGHTS.put("fnv-1_128", 6462);
        WEIGHTS.put("fnv-1_256", 10788);
        WEIGHTS.put("fnv-1_512", 22910);
        WEIGHTS.put("fnv-1_1024", 62278);
        WEIGHTS.put("fnv-1a_32", 33);
        WEIGHTS.put("fnv-1a_64", 4905);
        WEIGHTS.put("fnv-1a_128", 6546);
        WEIGHTS.put("fnv-1a_256", 10908);
        WEIGHTS.put("fnv-1a_512", 23063);
        WEIGHTS.put("fnv-1a_1024", 62315);
        WEIGHTS.put("fork256", 107);
        WEIGHTS.put("fugue224", 284);
        WEIGHTS.put("fugue256", 282);
        WEIGHTS.put("fugue384", 414);
        WEIGHTS.put("fugue512", 543);
        WEIGHTS.put("groestl-224", 290);
        WEIGHTS.put("groestl-256", 293);
        WEIGHTS.put("groestl-384", 438);
        WEIGHTS.put("groestl-512", 432);
        WEIGHTS.put("gost", 2705);
        WEIGHTS.put("gost:crypto-pro", 2706);
        WEIGHTS.put("has160", 97);
        WEIGHTS.put("haval_128_3", 94);
        WEIGHTS.put("haval_128_4", 123);
        WEIGHTS.put("haval_128_5", 181);
        WEIGHTS.put("haval_160_3", 96);
        WEIGHTS.put("haval_160_4", 124);
        WEIGHTS.put("haval_160_5", 182);
        WEIGHTS.put("haval_192_3", 99);
        WEIGHTS.put("haval_192_4", 123);
        WEIGHTS.put("haval_192_5", 183);
        WEIGHTS.put("haval_224_3", 94);
        WEIGHTS.put("haval_224_4", 123);
        WEIGHTS.put("haval_224_5", 181);
        WEIGHTS.put("haval_256_3", 95);
        WEIGHTS.put("haval_256_4", 123);
        WEIGHTS.put("haval_256_5", 182);
        WEIGHTS.put("jh224", 472);
        WEIGHTS.put("jh256", 453);
        WEIGHTS.put("jh384", 452);
        WEIGHTS.put("jh512", 450);
        WEIGHTS.put("joaat", 42);
        WEIGHTS.put("kangarootwelve", 63);
        WEIGHTS.put("keccak224", 299);
        WEIGHTS.put("keccak256", 315);
        WEIGHTS.put("keccak288", 332);
        WEIGHTS.put("keccak384", 408);
        WEIGHTS.put("keccak512", 572);
        WEIGHTS.put("kupyna-256", 694);
        WEIGHTS.put("kupyna-384", 960);
        WEIGHTS.put("kupyna-512", 976);
        WEIGHTS.put("lsh-256-224", 208);
        WEIGHTS.put("lsh-256-256", 202);
        WEIGHTS.put("lsh-512-224", 124);
        WEIGHTS.put("lsh-512-256", 127);
        WEIGHTS.put("lsh-512-384", 122);
        WEIGHTS.put("lsh-512-512", 124);
        WEIGHTS.put("luffa224", 192);
        WEIGHTS.put("luffa256", 190);
        WEIGHTS.put("luffa384", 260);
        WEIGHTS.put("luffa512", 360);
        WEIGHTS.put("marsupilamifourteen", 82);
        WEIGHTS.put("md2", 2279);
        WEIGHTS.put("md4", 48);
        WEIGHTS.put("md5", 65);
        WEIGHTS.put("mdc2", 5360);
        WEIGHTS.put("panama", 46);
        WEIGHTS.put("radiogatun:32", 74);
        WEIGHTS.put("radiogatun:64", 51);
        WEIGHTS.put("ripemd128", 99);
        WEIGHTS.put("ripemd160", 178);
        WEIGHTS.put("ripemd256", 94);
        WEIGHTS.put("ripemd320", 334);
        WEIGHTS.put("sha0", 107);
        WEIGHTS.put("sha1", 87);
        WEIGHTS.put("sha224", 158);
        WEIGHTS.put("sha256", 131);
        WEIGHTS.put("sha384", 96);
        WEIGHTS.put("sha512", 100);
        WEIGHTS.put("sha512/224", 110);
        WEIGHTS.put("sha512/256", 108);
        WEIGHTS.put("sha3-224", 158);
        WEIGHTS.put("sha3-256", 165);
        WEIGHTS.put("sha3-384", 207);
        WEIGHTS.put("sha3-512", 291);
        WEIGHTS.put("shake128", 266);
        WEIGHTS.put("shake256", 314);
        WEIGHTS.put("skein-256-8", 126);
        WEIGHTS.put("skein-256-16", 126);
        WEIGHTS.put("skein-256-24", 128);
        WEIGHTS.put("skein-256-32", 126);
        WEIGHTS.put("skein-256-40", 127);
        WEIGHTS.put("skein-256-48", 125);
        WEIGHTS.put("skein-256-56", 126);
        WEIGHTS.put("skein-256-64", 127);
        WEIGHTS.put("skein-256-72", 126);
        WEIGHTS.put("skein-256-80", 127);
        WEIGHTS.put("skein-256-88", 127);
        WEIGHTS.put("skein-256-96", 128);
        WEIGHTS.put("skein-256-104", 128);
        WEIGHTS.put("skein-256-112", 126);
        WEIGHTS.put("skein-256-120", 128);
        WEIGHTS.put("skein-256-128", 126);
        WEIGHTS.put("skein-256-136", 128);
        WEIGHTS.put("skein-256-144", 133);
        WEIGHTS.put("skein-256-152", 133);
        WEIGHTS.put("skein-256-160", 128);
        WEIGHTS.put("skein-256-168", 125);
        WEIGHTS.put("skein-256-176", 126);
        WEIGHTS.put("skein-256-184", 125);
        WEIGHTS.put("skein-256-192", 129);
        WEIGHTS.put("skein-256-200", 128);
        WEIGHTS.put("skein-256-208", 126);
        WEIGHTS.put("skein-256-216", 127);
        WEIGHTS.put("skein-256-224", 129);
        WEIGHTS.put("skein-256-232", 127);
        WEIGHTS.put("skein-256-240", 128);
        WEIGHTS.put("skein-256-248", 126);
        WEIGHTS.put("skein-256-256", 128);
        WEIGHTS.put("skein-512-8", 110);
        WEIGHTS.put("skein-512-16", 111);
        WEIGHTS.put("skein-512-24", 110);
        WEIGHTS.put("skein-512-32", 114);
        WEIGHTS.put("skein-512-40", 112);
        WEIGHTS.put("skein-512-48", 112);
        WEIGHTS.put("skein-512-56", 111);
        WEIGHTS.put("skein-512-64", 110);
        WEIGHTS.put("skein-512-72", 110);
        WEIGHTS.put("skein-512-80", 110);
        WEIGHTS.put("skein-512-88", 114);
        WEIGHTS.put("skein-512-96", 110);
        WEIGHTS.put("skein-512-104", 111);
        WEIGHTS.put("skein-512-112", 110);
        WEIGHTS.put("skein-512-120", 110);
        WEIGHTS.put("skein-512-128", 111);
        WEIGHTS.put("skein-512-136", 111);
        WEIGHTS.put("skein-512-144", 112);
        WEIGHTS.put("skein-512-152", 110);
        WEIGHTS.put("skein-512-160", 110);
        WEIGHTS.put("skein-512-168", 113);
        WEIGHTS.put("skein-512-176", 113);
        WEIGHTS.put("skein-512-184", 110);
        WEIGHTS.put("skein-512-192", 110);
        WEIGHTS.put("skein-512-200", 110);
        WEIGHTS.put("skein-512-208", 111);
        WEIGHTS.put("skein-512-216", 111);
        WEIGHTS.put("skein-512-224", 112);
        WEIGHTS.put("skein-512-232", 111);
        WEIGHTS.put("skein-512-240", 110);
        WEIGHTS.put("skein-512-248", 110);
        WEIGHTS.put("skein-512-256", 110);
        WEIGHTS.put("skein-512-264", 111);
        WEIGHTS.put("skein-512-272", 113);
        WEIGHTS.put("skein-512-280", 110);
        WEIGHTS.put("skein-512-288", 114);
        WEIGHTS.put("skein-512-296", 113);
        WEIGHTS.put("skein-512-304", 112);
        WEIGHTS.put("skein-512-312", 112);
        WEIGHTS.put("skein-512-320", 111);
        WEIGHTS.put("skein-512-328", 112);
        WEIGHTS.put("skein-512-336", 110);
        WEIGHTS.put("skein-512-344", 114);
        WEIGHTS.put("skein-512-352", 112);
        WEIGHTS.put("skein-512-360", 112);
        WEIGHTS.put("skein-512-368", 113);
        WEIGHTS.put("skein-512-376", 113);
        WEIGHTS.put("skein-512-384", 110);
        WEIGHTS.put("skein-512-392", 111);
        WEIGHTS.put("skein-512-400", 111);
        WEIGHTS.put("skein-512-408", 110);
        WEIGHTS.put("skein-512-416", 111);
        WEIGHTS.put("skein-512-424", 110);
        WEIGHTS.put("skein-512-432", 110);
        WEIGHTS.put("skein-512-440", 110);
        WEIGHTS.put("skein-512-448", 111);
        WEIGHTS.put("skein-512-456", 111);
        WEIGHTS.put("skein-512-464", 110);
        WEIGHTS.put("skein-512-472", 114);
        WEIGHTS.put("skein-512-480", 119);
        WEIGHTS.put("skein-512-488", 116);
        WEIGHTS.put("skein-512-496", 115);
        WEIGHTS.put("skein-512-504", 115);
        WEIGHTS.put("skein-512-512", 110);
        WEIGHTS.put("skein-1024-8", 108);
        WEIGHTS.put("skein-1024-16", 108);
        WEIGHTS.put("skein-1024-24", 106);
        WEIGHTS.put("skein-1024-32", 107);
        WEIGHTS.put("skein-1024-40", 106);
        WEIGHTS.put("skein-1024-48", 107);
        WEIGHTS.put("skein-1024-56", 108);
        WEIGHTS.put("skein-1024-64", 108);
        WEIGHTS.put("skein-1024-72", 107);
        WEIGHTS.put("skein-1024-80", 109);
        WEIGHTS.put("skein-1024-88", 108);
        WEIGHTS.put("skein-1024-96", 106);
        WEIGHTS.put("skein-1024-104", 107);
        WEIGHTS.put("skein-1024-112", 108);
        WEIGHTS.put("skein-1024-120", 106);
        WEIGHTS.put("skein-1024-128", 109);
        WEIGHTS.put("skein-1024-136", 106);
        WEIGHTS.put("skein-1024-144", 110);
        WEIGHTS.put("skein-1024-152", 107);
        WEIGHTS.put("skein-1024-160", 107);
        WEIGHTS.put("skein-1024-168", 106);
        WEIGHTS.put("skein-1024-176", 107);
        WEIGHTS.put("skein-1024-184", 107);
        WEIGHTS.put("skein-1024-192", 108);
        WEIGHTS.put("skein-1024-200", 108);
        WEIGHTS.put("skein-1024-208", 108);
        WEIGHTS.put("skein-1024-216", 106);
        WEIGHTS.put("skein-1024-224", 106);
        WEIGHTS.put("skein-1024-232", 107);
        WEIGHTS.put("skein-1024-240", 106);
        WEIGHTS.put("skein-1024-248", 108);
        WEIGHTS.put("skein-1024-256", 106);
        WEIGHTS.put("skein-1024-264", 106);
        WEIGHTS.put("skein-1024-272", 107);
        WEIGHTS.put("skein-1024-280", 107);
        WEIGHTS.put("skein-1024-288", 106);
        WEIGHTS.put("skein-1024-296", 108);
        WEIGHTS.put("skein-1024-304", 109);
        WEIGHTS.put("skein-1024-312", 108);
        WEIGHTS.put("skein-1024-320", 107);
        WEIGHTS.put("skein-1024-328", 108);
        WEIGHTS.put("skein-1024-336", 107);
        WEIGHTS.put("skein-1024-344", 106);
        WEIGHTS.put("skein-1024-352", 108);
        WEIGHTS.put("skein-1024-360", 112);
        WEIGHTS.put("skein-1024-368", 106);
        WEIGHTS.put("skein-1024-376", 108);
        WEIGHTS.put("skein-1024-384", 107);
        WEIGHTS.put("skein-1024-392", 107);
        WEIGHTS.put("skein-1024-400", 109);
        WEIGHTS.put("skein-1024-408", 106);
        WEIGHTS.put("skein-1024-416", 106);
        WEIGHTS.put("skein-1024-424", 106);
        WEIGHTS.put("skein-1024-432", 112);
        WEIGHTS.put("skein-1024-440", 106);
        WEIGHTS.put("skein-1024-448", 107);
        WEIGHTS.put("skein-1024-456", 106);
        WEIGHTS.put("skein-1024-464", 106);
        WEIGHTS.put("skein-1024-472", 107);
        WEIGHTS.put("skein-1024-480", 106);
        WEIGHTS.put("skein-1024-488", 108);
        WEIGHTS.put("skein-1024-496", 108);
        WEIGHTS.put("skein-1024-504", 106);
        WEIGHTS.put("skein-1024-512", 107);
        WEIGHTS.put("skein-1024-520", 108);
        WEIGHTS.put("skein-1024-528", 106);
        WEIGHTS.put("skein-1024-536", 106);
        WEIGHTS.put("skein-1024-544", 107);
        WEIGHTS.put("skein-1024-552", 108);
        WEIGHTS.put("skein-1024-560", 106);
        WEIGHTS.put("skein-1024-568", 107);
        WEIGHTS.put("skein-1024-576", 108);
        WEIGHTS.put("skein-1024-584", 106);
        WEIGHTS.put("skein-1024-592", 107);
        WEIGHTS.put("skein-1024-600", 107);
        WEIGHTS.put("skein-1024-608", 106);
        WEIGHTS.put("skein-1024-616", 111);
        WEIGHTS.put("skein-1024-624", 106);
        WEIGHTS.put("skein-1024-632", 107);
        WEIGHTS.put("skein-1024-640", 110);
        WEIGHTS.put("skein-1024-648", 106);
        WEIGHTS.put("skein-1024-656", 110);
        WEIGHTS.put("skein-1024-664", 108);
        WEIGHTS.put("skein-1024-672", 106);
        WEIGHTS.put("skein-1024-680", 107);
        WEIGHTS.put("skein-1024-688", 107);
        WEIGHTS.put("skein-1024-696", 106);
        WEIGHTS.put("skein-1024-704", 106);
        WEIGHTS.put("skein-1024-712", 106);
        WEIGHTS.put("skein-1024-720", 107);
        WEIGHTS.put("skein-1024-728", 109);
        WEIGHTS.put("skein-1024-736", 108);
        WEIGHTS.put("skein-1024-744", 106);
        WEIGHTS.put("skein-1024-752", 108);
        WEIGHTS.put("skein-1024-760", 106);
        WEIGHTS.put("skein-1024-768", 108);
        WEIGHTS.put("skein-1024-776", 106);
        WEIGHTS.put("skein-1024-784", 107);
        WEIGHTS.put("skein-1024-792", 108);
        WEIGHTS.put("skein-1024-800", 107);
        WEIGHTS.put("skein-1024-808", 108);
        WEIGHTS.put("skein-1024-816", 107);
        WEIGHTS.put("skein-1024-824", 106);
        WEIGHTS.put("skein-1024-832", 107);
        WEIGHTS.put("skein-1024-840", 107);
        WEIGHTS.put("skein-1024-848", 106);
        WEIGHTS.put("skein-1024-856", 110);
        WEIGHTS.put("skein-1024-864", 106);
        WEIGHTS.put("skein-1024-872", 106);
        WEIGHTS.put("skein-1024-880", 107);
        WEIGHTS.put("skein-1024-888", 108);
        WEIGHTS.put("skein-1024-896", 107);
        WEIGHTS.put("skein-1024-904", 106);
        WEIGHTS.put("skein-1024-912", 106);
        WEIGHTS.put("skein-1024-920", 107);
        WEIGHTS.put("skein-1024-928", 106);
        WEIGHTS.put("skein-1024-936", 106);
        WEIGHTS.put("skein-1024-944", 106);
        WEIGHTS.put("skein-1024-952", 109);
        WEIGHTS.put("skein-1024-960", 106);
        WEIGHTS.put("skein-1024-968", 106);
        WEIGHTS.put("skein-1024-976", 107);
        WEIGHTS.put("skein-1024-984", 107);
        WEIGHTS.put("skein-1024-992", 109);
        WEIGHTS.put("skein-1024-1000", 108);
        WEIGHTS.put("skein-1024-1008", 106);
        WEIGHTS.put("skein-1024-1016", 106);
        WEIGHTS.put("skein-1024-1024", 106);
        WEIGHTS.put("streebog256", 635);
        WEIGHTS.put("streebog512", 634);
        WEIGHTS.put("sum_bsd", 37);
        WEIGHTS.put("sum_minix", 37);
        WEIGHTS.put("sum_sysv", 15);
        WEIGHTS.put("sum8", 16);
        WEIGHTS.put("sum16", 16);
        WEIGHTS.put("sum24", 16);
        WEIGHTS.put("sum32", 16);
        WEIGHTS.put("sum40", 16);
        WEIGHTS.put("sum48", 16);
        WEIGHTS.put("sum56", 16);
        WEIGHTS.put("tiger128", 69);
        WEIGHTS.put("tiger160", 70);
        WEIGHTS.put("tiger", 71);
        WEIGHTS.put("tiger2", 69);
        WEIGHTS.put("tree:tiger", 84);
        WEIGHTS.put("tree:tiger2", 84);
        WEIGHTS.put("vsh", 22651);
        WEIGHTS.put("whirlpool0", 363);
        WEIGHTS.put("whirlpool1", 363);
        WEIGHTS.put("whirlpool2", 365);
        WEIGHTS.put("xor8", 16);
        WEIGHTS.put("xxhash32", 18);

        // aliases
        WEIGHTS_ALIASES.put("adler32", WEIGHTS.get("adler32"));
        WEIGHTS_ALIASES.put("adler-32", WEIGHTS.get("adler32"));
        WEIGHTS_ALIASES.put("blake-224", WEIGHTS.get("blake224"));
        WEIGHTS_ALIASES.put("blake-256", WEIGHTS.get("blake256"));
        WEIGHTS_ALIASES.put("blake-384", WEIGHTS.get("blake384"));
        WEIGHTS_ALIASES.put("blake-512", WEIGHTS.get("blake512"));
        WEIGHTS_ALIASES.put("b2sum-8", WEIGHTS.get("blake2b-8"));
        WEIGHTS_ALIASES.put("b2sum-16", WEIGHTS.get("blake2b-16"));
        WEIGHTS_ALIASES.put("b2sum-24", WEIGHTS.get("blake2b-24"));
        WEIGHTS_ALIASES.put("b2sum-32", WEIGHTS.get("blake2b-32"));
        WEIGHTS_ALIASES.put("b2sum-40", WEIGHTS.get("blake2b-40"));
        WEIGHTS_ALIASES.put("b2sum-48", WEIGHTS.get("blake2b-48"));
        WEIGHTS_ALIASES.put("b2sum-56", WEIGHTS.get("blake2b-56"));
        WEIGHTS_ALIASES.put("b2sum-64", WEIGHTS.get("blake2b-64"));
        WEIGHTS_ALIASES.put("b2sum-72", WEIGHTS.get("blake2b-72"));
        WEIGHTS_ALIASES.put("b2sum-80", WEIGHTS.get("blake2b-80"));
        WEIGHTS_ALIASES.put("b2sum-88", WEIGHTS.get("blake2b-88"));
        WEIGHTS_ALIASES.put("b2sum-96", WEIGHTS.get("blake2b-96"));
        WEIGHTS_ALIASES.put("b2sum-104", WEIGHTS.get("blake2b-104"));
        WEIGHTS_ALIASES.put("b2sum-112", WEIGHTS.get("blake2b-112"));
        WEIGHTS_ALIASES.put("b2sum-120", WEIGHTS.get("blake2b-120"));
        WEIGHTS_ALIASES.put("b2sum-128", WEIGHTS.get("blake2b-128"));
        WEIGHTS_ALIASES.put("b2sum-136", WEIGHTS.get("blake2b-136"));
        WEIGHTS_ALIASES.put("b2sum-144", WEIGHTS.get("blake2b-144"));
        WEIGHTS_ALIASES.put("b2sum-152", WEIGHTS.get("blake2b-152"));
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
        WEIGHTS_ALIASES.put("b2sum-480", WEIGHTS.get("blake2b-480"));
        WEIGHTS_ALIASES.put("b2sum-488", WEIGHTS.get("blake2b-488"));
        WEIGHTS_ALIASES.put("b2sum-496", WEIGHTS.get("blake2b-496"));
        WEIGHTS_ALIASES.put("b2sum-504", WEIGHTS.get("blake2b-504"));
        WEIGHTS_ALIASES.put("blake2b", WEIGHTS.get("blake2b-512"));
        WEIGHTS_ALIASES.put("b2sum-512", WEIGHTS.get("blake2b-512"));
        WEIGHTS_ALIASES.put("b2sum", WEIGHTS.get("blake2b-512"));
        WEIGHTS_ALIASES.put("blake2s", WEIGHTS.get("blake2s-256"));
        WEIGHTS_ALIASES.put("blake3-256", WEIGHTS.get("blake3"));
        WEIGHTS_ALIASES.put("b3sum", WEIGHTS.get("blake3"));
        WEIGHTS_ALIASES.put("crc-8", WEIGHTS.get("crc8"));
        WEIGHTS_ALIASES.put("crc-16", WEIGHTS.get("crc16"));
        WEIGHTS_ALIASES.put("crc-16_minix", WEIGHTS.get("crc16_minix"));
        WEIGHTS_ALIASES.put("crc-24", WEIGHTS.get("crc24"));
        WEIGHTS_ALIASES.put("crc-32", WEIGHTS.get("crc32"));
        WEIGHTS_ALIASES.put("fcs32", WEIGHTS.get("crc32"));
        WEIGHTS_ALIASES.put("fcs-32", WEIGHTS.get("crc32"));
        WEIGHTS_ALIASES.put("crc-32_mpeg-2", WEIGHTS.get("crc32_mpeg2"));
        WEIGHTS_ALIASES.put("crc-32_bzip2", WEIGHTS.get("crc32_bzip2"));
        WEIGHTS_ALIASES.put("crc-32_bzip-2", WEIGHTS.get("crc32_bzip2"));
        WEIGHTS_ALIASES.put("crc-32c", WEIGHTS.get("crc32c"));
        WEIGHTS_ALIASES.put("sum_plan9", WEIGHTS.get("crc32_fddi"));
        WEIGHTS_ALIASES.put("crc-32_ubi", WEIGHTS.get("crc32_ubi"));
        WEIGHTS_ALIASES.put("ubicrc32", WEIGHTS.get("crc32_ubi"));
        WEIGHTS_ALIASES.put("crc32_jamcrc", WEIGHTS.get("crc32_ubi"));
        WEIGHTS_ALIASES.put("crc-32_jamcrc", WEIGHTS.get("crc32_ubi"));
        WEIGHTS_ALIASES.put("crc-32_php", WEIGHTS.get("crc32_php"));
        WEIGHTS_ALIASES.put("crc-64", WEIGHTS.get("crc64"));
        WEIGHTS_ALIASES.put("crc-64_ecma", WEIGHTS.get("crc64_ecma"));
        WEIGHTS_ALIASES.put("crc-64_go-iso", WEIGHTS.get("crc64_go-iso"));
        WEIGHTS_ALIASES.put("crc-64_xz", WEIGHTS.get("crc64_xz"));
        WEIGHTS_ALIASES.put("crc64_go-ecma", WEIGHTS.get("crc64_xz"));
        WEIGHTS_ALIASES.put("crc-64_go-ecma", WEIGHTS.get("crc64_xz"));
        WEIGHTS_ALIASES.put("dha-256", WEIGHTS.get("dha256"));
        WEIGHTS_ALIASES.put("echo-224", WEIGHTS.get("echo224"));
        WEIGHTS_ALIASES.put("echo-256", WEIGHTS.get("echo256"));
        WEIGHTS_ALIASES.put("echo-384", WEIGHTS.get("echo384"));
        WEIGHTS_ALIASES.put("echo-512", WEIGHTS.get("echo512"));
        WEIGHTS_ALIASES.put("emule", WEIGHTS.get("ed2k"));
        WEIGHTS_ALIASES.put("edonkey", WEIGHTS.get("ed2k"));
        WEIGHTS_ALIASES.put("elf32", WEIGHTS.get("elf"));
        WEIGHTS_ALIASES.put("elf-32", WEIGHTS.get("elf"));
        WEIGHTS_ALIASES.put("fcs-16", WEIGHTS.get("fcs16"));
        WEIGHTS_ALIASES.put("crc16_x25", WEIGHTS.get("fcs16"));
        WEIGHTS_ALIASES.put("crc-16_x-25", WEIGHTS.get("fcs16"));
        WEIGHTS_ALIASES.put("fletcher-16", WEIGHTS.get("fletcher16"));
        WEIGHTS_ALIASES.put("fork-256", WEIGHTS.get("fork256"));
        WEIGHTS_ALIASES.put("fugue-224", WEIGHTS.get("fugue224"));
        WEIGHTS_ALIASES.put("fugue-256", WEIGHTS.get("fugue256"));
        WEIGHTS_ALIASES.put("fugue-384", WEIGHTS.get("fugue384"));
        WEIGHTS_ALIASES.put("fugue-512", WEIGHTS.get("fugue512"));
        WEIGHTS_ALIASES.put("groestl224", WEIGHTS.get("groestl-224"));
        WEIGHTS_ALIASES.put("groestl256", WEIGHTS.get("groestl-256"));
        WEIGHTS_ALIASES.put("groestl384", WEIGHTS.get("groestl-384"));
        WEIGHTS_ALIASES.put("groestl512", WEIGHTS.get("groestl-512"));
        WEIGHTS_ALIASES.put("gost:default", WEIGHTS.get("gost"));
        WEIGHTS_ALIASES.put("has-160", WEIGHTS.get("has160"));
        WEIGHTS_ALIASES.put("haval", WEIGHTS.get("haval_128_3"));
        WEIGHTS_ALIASES.put("jh-224", WEIGHTS.get("jh224"));
        WEIGHTS_ALIASES.put("jh-256", WEIGHTS.get("jh256"));
        WEIGHTS_ALIASES.put("jh-384", WEIGHTS.get("jh384"));
        WEIGHTS_ALIASES.put("jh-512", WEIGHTS.get("jh512"));
        WEIGHTS_ALIASES.put("joaat32", WEIGHTS.get("joaat"));
        WEIGHTS_ALIASES.put("joaat-32", WEIGHTS.get("joaat"));
        WEIGHTS_ALIASES.put("kangaroo12", WEIGHTS.get("kangarootwelve"));
        WEIGHTS_ALIASES.put("k12", WEIGHTS.get("kangarootwelve"));
        WEIGHTS_ALIASES.put("keccak-224", WEIGHTS.get("keccak224"));
        WEIGHTS_ALIASES.put("keccak-256", WEIGHTS.get("keccak256"));
        WEIGHTS_ALIASES.put("keccak-288", WEIGHTS.get("keccak288"));
        WEIGHTS_ALIASES.put("keccak-284", WEIGHTS.get("keccak384"));
        WEIGHTS_ALIASES.put("keccak-512", WEIGHTS.get("keccak512"));
        WEIGHTS_ALIASES.put("luffa-224", WEIGHTS.get("luffa224"));
        WEIGHTS_ALIASES.put("luffa-256", WEIGHTS.get("luffa256"));
        WEIGHTS_ALIASES.put("luffa-384", WEIGHTS.get("luffa384"));
        WEIGHTS_ALIASES.put("luffa-512", WEIGHTS.get("luffa512"));
        WEIGHTS_ALIASES.put("marsupilami14", WEIGHTS.get("marsupilamifourteen"));
        WEIGHTS_ALIASES.put("m14", WEIGHTS.get("marsupilamifourteen"));
        WEIGHTS_ALIASES.put("md2sum", WEIGHTS.get("md2"));
        WEIGHTS_ALIASES.put("md4sum", WEIGHTS.get("md4"));
        WEIGHTS_ALIASES.put("md5sum", WEIGHTS.get("md5"));
        WEIGHTS_ALIASES.put("mdc-2", WEIGHTS.get("mdc2"));
        WEIGHTS_ALIASES.put("rg32", WEIGHTS.get("radiogatun:32"));
        WEIGHTS_ALIASES.put("rg-32", WEIGHTS.get("radiogatun:32"));
        WEIGHTS_ALIASES.put("radiogatun", WEIGHTS.get("radiogatun:64"));
        WEIGHTS_ALIASES.put("rg64", WEIGHTS.get("radiogatun:64"));
        WEIGHTS_ALIASES.put("rg-64", WEIGHTS.get("radiogatun:64"));
        WEIGHTS_ALIASES.put("ripemd-128", WEIGHTS.get("ripemd128"));
        WEIGHTS_ALIASES.put("ripe-md128", WEIGHTS.get("ripemd128"));
        WEIGHTS_ALIASES.put("rmd128", WEIGHTS.get("ripemd128"));
        WEIGHTS_ALIASES.put("rmd-128", WEIGHTS.get("ripemd128"));
        WEIGHTS_ALIASES.put("ripemd-160", WEIGHTS.get("ripemd160"));
        WEIGHTS_ALIASES.put("ripe-md160", WEIGHTS.get("ripemd160"));
        WEIGHTS_ALIASES.put("rmd160", WEIGHTS.get("ripemd160"));
        WEIGHTS_ALIASES.put("rmd-160", WEIGHTS.get("ripemd160"));
        WEIGHTS_ALIASES.put("ripemd-256", WEIGHTS.get("ripemd256"));
        WEIGHTS_ALIASES.put("ripe-md256", WEIGHTS.get("ripemd256"));
        WEIGHTS_ALIASES.put("rmd256", WEIGHTS.get("ripemd256"));
        WEIGHTS_ALIASES.put("rmd-256", WEIGHTS.get("ripemd256"));
        WEIGHTS_ALIASES.put("ripemd-320", WEIGHTS.get("ripemd320"));
        WEIGHTS_ALIASES.put("ripe-md320", WEIGHTS.get("ripemd320"));
        WEIGHTS_ALIASES.put("rmd320", WEIGHTS.get("ripemd320"));
        WEIGHTS_ALIASES.put("rmd-320", WEIGHTS.get("ripemd320"));
        WEIGHTS_ALIASES.put("sha-0", WEIGHTS.get("sha0"));
        WEIGHTS_ALIASES.put("sha1sum", WEIGHTS.get("sha1"));
        WEIGHTS_ALIASES.put("sha", WEIGHTS.get("sha1"));
        WEIGHTS_ALIASES.put("sha-1", WEIGHTS.get("sha1"));
        WEIGHTS_ALIASES.put("sha160", WEIGHTS.get("sha1"));
        WEIGHTS_ALIASES.put("sha-160", WEIGHTS.get("sha1"));
        WEIGHTS_ALIASES.put("dss1", WEIGHTS.get("sha1"));
        WEIGHTS_ALIASES.put("sha-224", WEIGHTS.get("sha224"));
        WEIGHTS_ALIASES.put("sha-256", WEIGHTS.get("sha256"));
        WEIGHTS_ALIASES.put("sha-384", WEIGHTS.get("sha384"));
        WEIGHTS_ALIASES.put("sha-512", WEIGHTS.get("sha512"));
        WEIGHTS_ALIASES.put("sha-512/224", WEIGHTS.get("sha512/224"));
        WEIGHTS_ALIASES.put("sha512t224", WEIGHTS.get("sha512/224"));
        WEIGHTS_ALIASES.put("sha-512/256", WEIGHTS.get("sha512/256"));
        WEIGHTS_ALIASES.put("sha512t256", WEIGHTS.get("sha512/256"));
        WEIGHTS_ALIASES.put("sha-3-224", WEIGHTS.get("sha3-224"));
        WEIGHTS_ALIASES.put("sha-3-256", WEIGHTS.get("sha3-256"));
        WEIGHTS_ALIASES.put("sha-3-384", WEIGHTS.get("sha3-384"));
        WEIGHTS_ALIASES.put("sha-3-512", WEIGHTS.get("sha3-512"));
        WEIGHTS_ALIASES.put("skein-256", WEIGHTS.get("skein-256-256"));
        WEIGHTS_ALIASES.put("skein-512", WEIGHTS.get("skein-512-512"));
        WEIGHTS_ALIASES.put("skein-1024", WEIGHTS.get("skein-1024-1024"));
        WEIGHTS_ALIASES.put("skein256", WEIGHTS.get("skein-256-256"));
        WEIGHTS_ALIASES.put("skein512", WEIGHTS.get("skein-512-512"));
        WEIGHTS_ALIASES.put("skein1024", WEIGHTS.get("skein-1024-1024"));
        WEIGHTS_ALIASES.put("streebog-256", WEIGHTS.get("streebog256"));
        WEIGHTS_ALIASES.put("streebog-512", WEIGHTS.get("streebog512"));
        WEIGHTS_ALIASES.put("sumbsd", WEIGHTS.get("sum_bsd"));
        WEIGHTS_ALIASES.put("bsd", WEIGHTS.get("sum_bsd"));
        WEIGHTS_ALIASES.put("bsdsum", WEIGHTS.get("sum_bsd"));
        WEIGHTS_ALIASES.put("sumsysv", WEIGHTS.get("sum_sysv"));
        WEIGHTS_ALIASES.put("sysv", WEIGHTS.get("sum_sysv"));
        WEIGHTS_ALIASES.put("sysvsum", WEIGHTS.get("sum_sysv"));
        WEIGHTS_ALIASES.put("sum-8", WEIGHTS.get("sum8"));
        WEIGHTS_ALIASES.put("sum-16", WEIGHTS.get("sum16"));
        WEIGHTS_ALIASES.put("sum-24", WEIGHTS.get("sum24"));
        WEIGHTS_ALIASES.put("sum-32", WEIGHTS.get("sum32"));
        WEIGHTS_ALIASES.put("sum-40", WEIGHTS.get("sum40"));
        WEIGHTS_ALIASES.put("sum-48", WEIGHTS.get("sum48"));
        WEIGHTS_ALIASES.put("sum-56", WEIGHTS.get("sum56"));
        WEIGHTS_ALIASES.put("tiger-128", WEIGHTS.get("tiger128"));
        WEIGHTS_ALIASES.put("tiger-160", WEIGHTS.get("tiger160"));
        WEIGHTS_ALIASES.put("tiger192", WEIGHTS.get("tiger"));
        WEIGHTS_ALIASES.put("tiger-192", WEIGHTS.get("tiger"));
        WEIGHTS_ALIASES.put("tree:tiger192", WEIGHTS.get("tree:tiger"));
        WEIGHTS_ALIASES.put("tree:tiger-192", WEIGHTS.get("tree:tiger"));
        WEIGHTS_ALIASES.put("tth", WEIGHTS.get("tree:tiger"));
        WEIGHTS_ALIASES.put("tth2", WEIGHTS.get("tree:tiger2"));
        WEIGHTS_ALIASES.put("vsh-1024", WEIGHTS.get("vsh"));
        WEIGHTS_ALIASES.put("whirlpool-0", WEIGHTS.get("whirlpool0"));
        WEIGHTS_ALIASES.put("whirlpool-l", WEIGHTS.get("whirlpool1"));
        WEIGHTS_ALIASES.put("whirlpool-t", WEIGHTS.get("whirlpool1"));
        WEIGHTS_ALIASES.put("whirlpool", WEIGHTS.get("whirlpool2"));
        WEIGHTS_ALIASES.put("whirlpool-2", WEIGHTS.get("whirlpool2"));
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
            System.err.printf("Warning: file size of %s is <= 20 MB\n", file);
        }

        final Set<String> hashes;
        if (args.length > 1) {
            hashes = new TreeSet<>();
            for (int i = 1; i < args.length; i++) {
                hashes.add(args[i]);
            }
        } else {
            hashes = JacksumAPI.getAvailableAlgorithms().keySet();
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

    private static long runSequential(String fileName, final AbstractChecksum md) throws FileNotFoundException, IOException {
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
        /* if there's no weigth for a given algorithm, then
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
