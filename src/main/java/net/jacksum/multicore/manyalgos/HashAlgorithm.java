/*


  Jacksum 3.6.0 - a checksum utility in Java
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
        WEIGHTS.put("adler32", 12);
        WEIGHTS.put("blake224", 194);
        WEIGHTS.put("blake256", 194);
        WEIGHTS.put("blake384", 111);
        WEIGHTS.put("blake512", 111);
        WEIGHTS.put("blake2b-8", 76);
        WEIGHTS.put("blake2b-16", 76);
        WEIGHTS.put("blake2b-24", 76);
        WEIGHTS.put("blake2b-32", 76);
        WEIGHTS.put("blake2b-40", 76);
        WEIGHTS.put("blake2b-48", 76);
        WEIGHTS.put("blake2b-56", 76);
        WEIGHTS.put("blake2b-64", 76);
        WEIGHTS.put("blake2b-72", 76);
        WEIGHTS.put("blake2b-80", 76);
        WEIGHTS.put("blake2b-88", 76);
        WEIGHTS.put("blake2b-96", 76);
        WEIGHTS.put("blake2b-104", 76);
        WEIGHTS.put("blake2b-112", 76);
        WEIGHTS.put("blake2b-120", 76);
        WEIGHTS.put("blake2b-128", 76);
        WEIGHTS.put("blake2b-136", 76);
        WEIGHTS.put("blake2b-144", 76);
        WEIGHTS.put("blake2b-152", 76);
        WEIGHTS.put("blake2b-160", 76);
        WEIGHTS.put("blake2b-168", 76);
        WEIGHTS.put("blake2b-176", 76);
        WEIGHTS.put("blake2b-184", 76);
        WEIGHTS.put("blake2b-192", 75);
        WEIGHTS.put("blake2b-200", 75);
        WEIGHTS.put("blake2b-208", 76);
        WEIGHTS.put("blake2b-216", 76);
        WEIGHTS.put("blake2b-224", 76);
        WEIGHTS.put("blake2b-232", 76);
        WEIGHTS.put("blake2b-240", 76);
        WEIGHTS.put("blake2b-248", 76);
        WEIGHTS.put("blake2b-256", 76);
        WEIGHTS.put("blake2b-264", 76);
        WEIGHTS.put("blake2b-272", 76);
        WEIGHTS.put("blake2b-280", 76);
        WEIGHTS.put("blake2b-288", 76);
        WEIGHTS.put("blake2b-296", 76);
        WEIGHTS.put("blake2b-304", 76);
        WEIGHTS.put("blake2b-312", 76);
        WEIGHTS.put("blake2b-320", 76);
        WEIGHTS.put("blake2b-328", 77);
        WEIGHTS.put("blake2b-336", 76);
        WEIGHTS.put("blake2b-344", 76);
        WEIGHTS.put("blake2b-352", 76);
        WEIGHTS.put("blake2b-360", 76);
        WEIGHTS.put("blake2b-368", 76);
        WEIGHTS.put("blake2b-376", 76);
        WEIGHTS.put("blake2b-384", 76);
        WEIGHTS.put("blake2b-392", 76);
        WEIGHTS.put("blake2b-400", 76);
        WEIGHTS.put("blake2b-408", 75);
        WEIGHTS.put("blake2b-416", 76);
        WEIGHTS.put("blake2b-424", 77);
        WEIGHTS.put("blake2b-432", 76);
        WEIGHTS.put("blake2b-440", 76);
        WEIGHTS.put("blake2b-448", 76);
        WEIGHTS.put("blake2b-456", 76);
        WEIGHTS.put("blake2b-464", 76);
        WEIGHTS.put("blake2b-472", 76);
        WEIGHTS.put("blake2b-480", 76);
        WEIGHTS.put("blake2b-488", 75);
        WEIGHTS.put("blake2b-496", 75);
        WEIGHTS.put("blake2b-504", 76);
        WEIGHTS.put("blake2b-512", 76);
        WEIGHTS.put("blake2s-8", 121);
        WEIGHTS.put("blake2s-16", 123);
        WEIGHTS.put("blake2s-24", 121);
        WEIGHTS.put("blake2s-32", 121);
        WEIGHTS.put("blake2s-40", 121);
        WEIGHTS.put("blake2s-48", 121);
        WEIGHTS.put("blake2s-56", 121);
        WEIGHTS.put("blake2s-64", 121);
        WEIGHTS.put("blake2s-72", 122);
        WEIGHTS.put("blake2s-80", 121);
        WEIGHTS.put("blake2s-88", 122);
        WEIGHTS.put("blake2s-96", 121);
        WEIGHTS.put("blake2s-104", 121);
        WEIGHTS.put("blake2s-112", 121);
        WEIGHTS.put("blake2s-120", 121);
        WEIGHTS.put("blake2s-128", 122);
        WEIGHTS.put("blake2s-136", 122);
        WEIGHTS.put("blake2s-144", 121);
        WEIGHTS.put("blake2s-152", 121);
        WEIGHTS.put("blake2s-160", 121);
        WEIGHTS.put("blake2s-168", 123);
        WEIGHTS.put("blake2s-176", 121);
        WEIGHTS.put("blake2s-184", 121);
        WEIGHTS.put("blake2s-192", 121);
        WEIGHTS.put("blake2s-200", 121);
        WEIGHTS.put("blake2s-208", 121);
        WEIGHTS.put("blake2s-216", 121);
        WEIGHTS.put("blake2s-224", 121);
        WEIGHTS.put("blake2s-232", 121);
        WEIGHTS.put("blake2s-240", 121);
        WEIGHTS.put("blake2s-248", 121);
        WEIGHTS.put("blake2s-256", 121);
        WEIGHTS.put("blake3", 122);
        WEIGHTS.put("cksum_minix", 63);
        WEIGHTS.put("cksum", 65);
        WEIGHTS.put("crc8", 56);
        WEIGHTS.put("crc16", 58);
        WEIGHTS.put("crc16_minix", 65);
        WEIGHTS.put("crc24", 42);
        WEIGHTS.put("crc32", 7);
        WEIGHTS.put("crc32_mpeg2", 65);
        WEIGHTS.put("crc32_bzip2", 46);
        WEIGHTS.put("crc32_fddi", 62);
        WEIGHTS.put("crc32_ubi", 62);
        WEIGHTS.put("crc32_php", 46);
        WEIGHTS.put("crc32c", 6);
        WEIGHTS.put("crc64", 58);
        WEIGHTS.put("crc64_ecma", 66);
        WEIGHTS.put("crc64_go-iso", 62);
        WEIGHTS.put("crc64_xz", 62);
        WEIGHTS.put("crc82_darc", 629);
        WEIGHTS.put("dha256", 147);
        WEIGHTS.put("echo224", 348);
        WEIGHTS.put("echo256", 358);
        WEIGHTS.put("echo384", 631);
        WEIGHTS.put("echo512", 637);
        WEIGHTS.put("ed2k", 40);
        WEIGHTS.put("elf", 56);
        WEIGHTS.put("fcs16", 63);
        WEIGHTS.put("fletcher16", 86);
        WEIGHTS.put("fnv-0_32", 31);
        WEIGHTS.put("fnv-0_64", 4213);
        WEIGHTS.put("fnv-0_128", 6001);
        WEIGHTS.put("fnv-0_256", 9959);
        WEIGHTS.put("fnv-0_512", 20661);
        WEIGHTS.put("fnv-0_1024", 57149);
        WEIGHTS.put("fnv-1_32", 31);
        WEIGHTS.put("fnv-1_64", 4073);
        WEIGHTS.put("fnv-1_128", 5914);
        WEIGHTS.put("fnv-1_256", 9930);
        WEIGHTS.put("fnv-1_512", 20680);
        WEIGHTS.put("fnv-1_1024", 57254);
        WEIGHTS.put("fnv-1a_32", 31);
        WEIGHTS.put("fnv-1a_64", 3969);
        WEIGHTS.put("fnv-1a_128", 6091);
        WEIGHTS.put("fnv-1a_256", 10146);
        WEIGHTS.put("fnv-1a_512", 20711);
        WEIGHTS.put("fnv-1a_1024", 57341);
        WEIGHTS.put("fork256", 86);
        WEIGHTS.put("fugue224", 236);
        WEIGHTS.put("fugue256", 234);
        WEIGHTS.put("fugue384", 343);
        WEIGHTS.put("fugue512", 447);
        WEIGHTS.put("groestl-224", 250);
        WEIGHTS.put("groestl-256", 251);
        WEIGHTS.put("groestl-384", 343);
        WEIGHTS.put("groestl-512", 346);
        WEIGHTS.put("gost", 2609);
        WEIGHTS.put("gost:crypto-pro", 2596);
        WEIGHTS.put("has160", 73);
        WEIGHTS.put("haval_128_3", 101);
        WEIGHTS.put("haval_128_4", 127);
        WEIGHTS.put("haval_128_5", 179);
        WEIGHTS.put("haval_160_3", 101);
        WEIGHTS.put("haval_160_4", 127);
        WEIGHTS.put("haval_160_5", 179);
        WEIGHTS.put("haval_192_3", 101);
        WEIGHTS.put("haval_192_4", 127);
        WEIGHTS.put("haval_192_5", 179);
        WEIGHTS.put("haval_224_3", 102);
        WEIGHTS.put("haval_224_4", 127);
        WEIGHTS.put("haval_224_5", 179);
        WEIGHTS.put("haval_256_3", 102);
        WEIGHTS.put("haval_256_4", 127);
        WEIGHTS.put("haval_256_5", 181);
        WEIGHTS.put("jh224", 367);
        WEIGHTS.put("jh256", 368);
        WEIGHTS.put("jh384", 366);
        WEIGHTS.put("jh512", 366);
        WEIGHTS.put("joaat", 39);
        WEIGHTS.put("kangarootwelve", 47);
        WEIGHTS.put("keccak224", 84);
        WEIGHTS.put("keccak256", 90);
        WEIGHTS.put("keccak288", 94);
        WEIGHTS.put("keccak384", 112);
        WEIGHTS.put("keccak512", 157);
        WEIGHTS.put("kupyna-256", 549);
        WEIGHTS.put("kupyna-384", 765);
        WEIGHTS.put("kupyna-512", 764);
        WEIGHTS.put("lsh-256-224", 215);
        WEIGHTS.put("lsh-256-256", 215);
        WEIGHTS.put("lsh-512-224", 128);
        WEIGHTS.put("lsh-512-256", 128);
        WEIGHTS.put("lsh-512-384", 128);
        WEIGHTS.put("lsh-512-512", 128);
        WEIGHTS.put("luffa224", 160);
        WEIGHTS.put("luffa256", 160);
        WEIGHTS.put("luffa384", 225);
        WEIGHTS.put("luffa512", 291);
        WEIGHTS.put("marsupilamifourteen", 62);
        WEIGHTS.put("md2", 2092);
        WEIGHTS.put("md4", 40);
        WEIGHTS.put("md5", 59);
        WEIGHTS.put("mdc2", 7594);
        WEIGHTS.put("panama", 50);
        WEIGHTS.put("radiogatun:32", 69);
        WEIGHTS.put("radiogatun:64", 45);
        WEIGHTS.put("ripemd128", 94);
        WEIGHTS.put("ripemd160", 162);
        WEIGHTS.put("ripemd256", 90);
        WEIGHTS.put("ripemd320", 302);
        WEIGHTS.put("sha0", 92);
        WEIGHTS.put("sha-1", 17);
        WEIGHTS.put("sha-224", 134);
        WEIGHTS.put("sha-256", 18);
        WEIGHTS.put("sha-384", 41);
        WEIGHTS.put("sha-512", 41);
        WEIGHTS.put("sha-512/224", 88);
        WEIGHTS.put("sha-512/256", 88);
        WEIGHTS.put("sha3-224", 118);
        WEIGHTS.put("sha3-256", 124);
        WEIGHTS.put("sha3-384", 156);
        WEIGHTS.put("sha3-512", 219);
        WEIGHTS.put("shake128", 74);
        WEIGHTS.put("shake256", 89);
        WEIGHTS.put("skein-256-8", 111);
        WEIGHTS.put("skein-256-16", 111);
        WEIGHTS.put("skein-256-24", 111);
        WEIGHTS.put("skein-256-32", 112);
        WEIGHTS.put("skein-256-40", 111);
        WEIGHTS.put("skein-256-48", 111);
        WEIGHTS.put("skein-256-56", 112);
        WEIGHTS.put("skein-256-64", 113);
        WEIGHTS.put("skein-256-72", 112);
        WEIGHTS.put("skein-256-80", 121);
        WEIGHTS.put("skein-256-88", 125);
        WEIGHTS.put("skein-256-96", 111);
        WEIGHTS.put("skein-256-104", 112);
        WEIGHTS.put("skein-256-112", 111);
        WEIGHTS.put("skein-256-120", 112);
        WEIGHTS.put("skein-256-128", 112);
        WEIGHTS.put("skein-256-136", 112);
        WEIGHTS.put("skein-256-144", 112);
        WEIGHTS.put("skein-256-152", 111);
        WEIGHTS.put("skein-256-160", 111);
        WEIGHTS.put("skein-256-168", 110);
        WEIGHTS.put("skein-256-176", 111);
        WEIGHTS.put("skein-256-184", 110);
        WEIGHTS.put("skein-256-192", 112);
        WEIGHTS.put("skein-256-200", 112);
        WEIGHTS.put("skein-256-208", 111);
        WEIGHTS.put("skein-256-216", 111);
        WEIGHTS.put("skein-256-224", 110);
        WEIGHTS.put("skein-256-232", 111);
        WEIGHTS.put("skein-256-240", 112);
        WEIGHTS.put("skein-256-248", 111);
        WEIGHTS.put("skein-256-256", 114);
        WEIGHTS.put("skein-512-8", 80);
        WEIGHTS.put("skein-512-16", 80);
        WEIGHTS.put("skein-512-24", 80);
        WEIGHTS.put("skein-512-32", 79);
        WEIGHTS.put("skein-512-40", 79);
        WEIGHTS.put("skein-512-48", 79);
        WEIGHTS.put("skein-512-56", 79);
        WEIGHTS.put("skein-512-64", 79);
        WEIGHTS.put("skein-512-72", 79);
        WEIGHTS.put("skein-512-80", 79);
        WEIGHTS.put("skein-512-88", 79);
        WEIGHTS.put("skein-512-96", 80);
        WEIGHTS.put("skein-512-104", 81);
        WEIGHTS.put("skein-512-112", 80);
        WEIGHTS.put("skein-512-120", 80);
        WEIGHTS.put("skein-512-128", 80);
        WEIGHTS.put("skein-512-136", 79);
        WEIGHTS.put("skein-512-144", 80);
        WEIGHTS.put("skein-512-152", 79);
        WEIGHTS.put("skein-512-160", 79);
        WEIGHTS.put("skein-512-168", 80);
        WEIGHTS.put("skein-512-176", 80);
        WEIGHTS.put("skein-512-184", 80);
        WEIGHTS.put("skein-512-192", 79);
        WEIGHTS.put("skein-512-200", 80);
        WEIGHTS.put("skein-512-208", 80);
        WEIGHTS.put("skein-512-216", 79);
        WEIGHTS.put("skein-512-224", 79);
        WEIGHTS.put("skein-512-232", 80);
        WEIGHTS.put("skein-512-240", 79);
        WEIGHTS.put("skein-512-248", 80);
        WEIGHTS.put("skein-512-256", 80);
        WEIGHTS.put("skein-512-264", 79);
        WEIGHTS.put("skein-512-272", 79);
        WEIGHTS.put("skein-512-280", 79);
        WEIGHTS.put("skein-512-288", 80);
        WEIGHTS.put("skein-512-296", 79);
        WEIGHTS.put("skein-512-304", 79);
        WEIGHTS.put("skein-512-312", 79);
        WEIGHTS.put("skein-512-320", 80);
        WEIGHTS.put("skein-512-328", 79);
        WEIGHTS.put("skein-512-336", 79);
        WEIGHTS.put("skein-512-344", 80);
        WEIGHTS.put("skein-512-352", 79);
        WEIGHTS.put("skein-512-360", 79);
        WEIGHTS.put("skein-512-368", 79);
        WEIGHTS.put("skein-512-376", 79);
        WEIGHTS.put("skein-512-384", 79);
        WEIGHTS.put("skein-512-392", 79);
        WEIGHTS.put("skein-512-400", 79);
        WEIGHTS.put("skein-512-408", 79);
        WEIGHTS.put("skein-512-416", 80);
        WEIGHTS.put("skein-512-424", 79);
        WEIGHTS.put("skein-512-432", 79);
        WEIGHTS.put("skein-512-440", 80);
        WEIGHTS.put("skein-512-448", 79);
        WEIGHTS.put("skein-512-456", 79);
        WEIGHTS.put("skein-512-464", 79);
        WEIGHTS.put("skein-512-472", 79);
        WEIGHTS.put("skein-512-480", 79);
        WEIGHTS.put("skein-512-488", 79);
        WEIGHTS.put("skein-512-496", 80);
        WEIGHTS.put("skein-512-504", 80);
        WEIGHTS.put("skein-512-512", 80);
        WEIGHTS.put("skein-1024-8", 90);
        WEIGHTS.put("skein-1024-16", 90);
        WEIGHTS.put("skein-1024-24", 90);
        WEIGHTS.put("skein-1024-32", 90);
        WEIGHTS.put("skein-1024-40", 90);
        WEIGHTS.put("skein-1024-48", 90);
        WEIGHTS.put("skein-1024-56", 90);
        WEIGHTS.put("skein-1024-64", 90);
        WEIGHTS.put("skein-1024-72", 90);
        WEIGHTS.put("skein-1024-80", 91);
        WEIGHTS.put("skein-1024-88", 90);
        WEIGHTS.put("skein-1024-96", 90);
        WEIGHTS.put("skein-1024-104", 90);
        WEIGHTS.put("skein-1024-112", 90);
        WEIGHTS.put("skein-1024-120", 90);
        WEIGHTS.put("skein-1024-128", 90);
        WEIGHTS.put("skein-1024-136", 90);
        WEIGHTS.put("skein-1024-144", 90);
        WEIGHTS.put("skein-1024-152", 90);
        WEIGHTS.put("skein-1024-160", 90);
        WEIGHTS.put("skein-1024-168", 90);
        WEIGHTS.put("skein-1024-176", 90);
        WEIGHTS.put("skein-1024-184", 90);
        WEIGHTS.put("skein-1024-192", 89);
        WEIGHTS.put("skein-1024-200", 90);
        WEIGHTS.put("skein-1024-208", 90);
        WEIGHTS.put("skein-1024-216", 91);
        WEIGHTS.put("skein-1024-224", 91);
        WEIGHTS.put("skein-1024-232", 90);
        WEIGHTS.put("skein-1024-240", 90);
        WEIGHTS.put("skein-1024-248", 90);
        WEIGHTS.put("skein-1024-256", 90);
        WEIGHTS.put("skein-1024-264", 90);
        WEIGHTS.put("skein-1024-272", 90);
        WEIGHTS.put("skein-1024-280", 90);
        WEIGHTS.put("skein-1024-288", 89);
        WEIGHTS.put("skein-1024-296", 90);
        WEIGHTS.put("skein-1024-304", 90);
        WEIGHTS.put("skein-1024-312", 90);
        WEIGHTS.put("skein-1024-320", 90);
        WEIGHTS.put("skein-1024-328", 89);
        WEIGHTS.put("skein-1024-336", 90);
        WEIGHTS.put("skein-1024-344", 90);
        WEIGHTS.put("skein-1024-352", 90);
        WEIGHTS.put("skein-1024-360", 90);
        WEIGHTS.put("skein-1024-368", 90);
        WEIGHTS.put("skein-1024-376", 90);
        WEIGHTS.put("skein-1024-384", 90);
        WEIGHTS.put("skein-1024-392", 90);
        WEIGHTS.put("skein-1024-400", 90);
        WEIGHTS.put("skein-1024-408", 89);
        WEIGHTS.put("skein-1024-416", 90);
        WEIGHTS.put("skein-1024-424", 91);
        WEIGHTS.put("skein-1024-432", 103);
        WEIGHTS.put("skein-1024-440", 90);
        WEIGHTS.put("skein-1024-448", 90);
        WEIGHTS.put("skein-1024-456", 90);
        WEIGHTS.put("skein-1024-464", 90);
        WEIGHTS.put("skein-1024-472", 90);
        WEIGHTS.put("skein-1024-480", 90);
        WEIGHTS.put("skein-1024-488", 90);
        WEIGHTS.put("skein-1024-496", 90);
        WEIGHTS.put("skein-1024-504", 90);
        WEIGHTS.put("skein-1024-512", 90);
        WEIGHTS.put("skein-1024-520", 90);
        WEIGHTS.put("skein-1024-528", 89);
        WEIGHTS.put("skein-1024-536", 91);
        WEIGHTS.put("skein-1024-544", 90);
        WEIGHTS.put("skein-1024-552", 90);
        WEIGHTS.put("skein-1024-560", 90);
        WEIGHTS.put("skein-1024-568", 90);
        WEIGHTS.put("skein-1024-576", 89);
        WEIGHTS.put("skein-1024-584", 89);
        WEIGHTS.put("skein-1024-592", 89);
        WEIGHTS.put("skein-1024-600", 90);
        WEIGHTS.put("skein-1024-608", 90);
        WEIGHTS.put("skein-1024-616", 98);
        WEIGHTS.put("skein-1024-624", 91);
        WEIGHTS.put("skein-1024-632", 90);
        WEIGHTS.put("skein-1024-640", 90);
        WEIGHTS.put("skein-1024-648", 90);
        WEIGHTS.put("skein-1024-656", 89);
        WEIGHTS.put("skein-1024-664", 90);
        WEIGHTS.put("skein-1024-672", 90);
        WEIGHTS.put("skein-1024-680", 89);
        WEIGHTS.put("skein-1024-688", 90);
        WEIGHTS.put("skein-1024-696", 90);
        WEIGHTS.put("skein-1024-704", 89);
        WEIGHTS.put("skein-1024-712", 90);
        WEIGHTS.put("skein-1024-720", 90);
        WEIGHTS.put("skein-1024-728", 91);
        WEIGHTS.put("skein-1024-736", 90);
        WEIGHTS.put("skein-1024-744", 90);
        WEIGHTS.put("skein-1024-752", 92);
        WEIGHTS.put("skein-1024-760", 90);
        WEIGHTS.put("skein-1024-768", 90);
        WEIGHTS.put("skein-1024-776", 90);
        WEIGHTS.put("skein-1024-784", 90);
        WEIGHTS.put("skein-1024-792", 90);
        WEIGHTS.put("skein-1024-800", 90);
        WEIGHTS.put("skein-1024-808", 90);
        WEIGHTS.put("skein-1024-816", 90);
        WEIGHTS.put("skein-1024-824", 91);
        WEIGHTS.put("skein-1024-832", 93);
        WEIGHTS.put("skein-1024-840", 90);
        WEIGHTS.put("skein-1024-848", 90);
        WEIGHTS.put("skein-1024-856", 90);
        WEIGHTS.put("skein-1024-864", 90);
        WEIGHTS.put("skein-1024-872", 90);
        WEIGHTS.put("skein-1024-880", 90);
        WEIGHTS.put("skein-1024-888", 90);
        WEIGHTS.put("skein-1024-896", 90);
        WEIGHTS.put("skein-1024-904", 90);
        WEIGHTS.put("skein-1024-912", 90);
        WEIGHTS.put("skein-1024-920", 90);
        WEIGHTS.put("skein-1024-928", 90);
        WEIGHTS.put("skein-1024-936", 90);
        WEIGHTS.put("skein-1024-944", 90);
        WEIGHTS.put("skein-1024-952", 90);
        WEIGHTS.put("skein-1024-960", 90);
        WEIGHTS.put("skein-1024-968", 90);
        WEIGHTS.put("skein-1024-976", 90);
        WEIGHTS.put("skein-1024-984", 90);
        WEIGHTS.put("skein-1024-992", 90);
        WEIGHTS.put("skein-1024-1000", 90);
        WEIGHTS.put("skein-1024-1008", 90);
        WEIGHTS.put("skein-1024-1016", 90);
        WEIGHTS.put("skein-1024-1024", 90);
        WEIGHTS.put("sm3", 150);
        WEIGHTS.put("streebog256", 605);
        WEIGHTS.put("streebog512", 607);
        WEIGHTS.put("sum_bsd", 35);
        WEIGHTS.put("sum_minix", 35);
        WEIGHTS.put("sum_sysv", 12);
        WEIGHTS.put("sum8", 12);
        WEIGHTS.put("sum16", 12);
        WEIGHTS.put("sum24", 12);
        WEIGHTS.put("sum32", 12);
        WEIGHTS.put("sum40", 12);
        WEIGHTS.put("sum48", 12);
        WEIGHTS.put("sum56", 12);
        WEIGHTS.put("sum64", 12);
        WEIGHTS.put("tiger128", 50);
        WEIGHTS.put("tiger160", 50);
        WEIGHTS.put("tiger", 50);
        WEIGHTS.put("tiger2", 50);
        WEIGHTS.put("tree:tiger", 64);
        WEIGHTS.put("tree:tiger2", 64);
        WEIGHTS.put("tiger-192-4-php", 70);
        WEIGHTS.put("tiger-160-4-php", 70);
        WEIGHTS.put("tiger-128-4-php", 70);
        WEIGHTS.put("vsh", 21659);
        WEIGHTS.put("whirlpool0", 248);
        WEIGHTS.put("whirlpool1", 247);
        WEIGHTS.put("whirlpool2", 246);
        WEIGHTS.put("xor8", 12);
        WEIGHTS.put("xxhash32", 12);

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
        WEIGHTS_ALIASES.put("sum_plan9", WEIGHTS.get("crc32_fddi"));
        WEIGHTS_ALIASES.put("crc-32_ubi", WEIGHTS.get("crc32_ubi"));
        WEIGHTS_ALIASES.put("ubicrc32", WEIGHTS.get("crc32_ubi"));
        WEIGHTS_ALIASES.put("crc32_jamcrc", WEIGHTS.get("crc32_ubi"));
        WEIGHTS_ALIASES.put("crc-32_jamcrc", WEIGHTS.get("crc32_ubi"));
        WEIGHTS_ALIASES.put("crc-32_php", WEIGHTS.get("crc32_php"));
        WEIGHTS_ALIASES.put("crc-32c", WEIGHTS.get("crc32c"));
        WEIGHTS_ALIASES.put("crc-64", WEIGHTS.get("crc64"));
        WEIGHTS_ALIASES.put("crc-64_ecma", WEIGHTS.get("crc64_ecma"));
        WEIGHTS_ALIASES.put("crc-64_go-iso", WEIGHTS.get("crc64_go-iso"));
        WEIGHTS_ALIASES.put("crc-64_xz", WEIGHTS.get("crc64_xz"));
        WEIGHTS_ALIASES.put("crc64_go-ecma", WEIGHTS.get("crc64_xz"));
        WEIGHTS_ALIASES.put("crc-64_go-ecma", WEIGHTS.get("crc64_xz"));
        WEIGHTS_ALIASES.put("crc-82_darc", WEIGHTS.get("crc82_darc"));
        WEIGHTS_ALIASES.put("crc-82", WEIGHTS.get("crc82_darc"));
        WEIGHTS_ALIASES.put("crc82", WEIGHTS.get("crc82_darc"));
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
        WEIGHTS_ALIASES.put("sha", WEIGHTS.get("sha-1"));
        WEIGHTS_ALIASES.put("sha1", WEIGHTS.get("sha-1"));
        WEIGHTS_ALIASES.put("sha1sum", WEIGHTS.get("sha-1"));
        WEIGHTS_ALIASES.put("sha160", WEIGHTS.get("sha-1"));
        WEIGHTS_ALIASES.put("sha-160", WEIGHTS.get("sha-1"));
        WEIGHTS_ALIASES.put("dss1", WEIGHTS.get("sha-1"));
        WEIGHTS_ALIASES.put("sha224", WEIGHTS.get("sha-224"));
        WEIGHTS_ALIASES.put("sha256", WEIGHTS.get("sha-256"));
        WEIGHTS_ALIASES.put("sha384", WEIGHTS.get("sha-384"));
        WEIGHTS_ALIASES.put("sha512", WEIGHTS.get("sha-512"));
        WEIGHTS_ALIASES.put("sha512/224", WEIGHTS.get("sha-512/224"));
        WEIGHTS_ALIASES.put("sha512t224", WEIGHTS.get("sha-512/224"));
        WEIGHTS_ALIASES.put("sha512/256", WEIGHTS.get("sha-512/256"));
        WEIGHTS_ALIASES.put("sha512t256", WEIGHTS.get("sha-512/256"));
        WEIGHTS_ALIASES.put("sha-3-224", WEIGHTS.get("sha3-224"));
        WEIGHTS_ALIASES.put("sha-3-256", WEIGHTS.get("sha3-256"));
        WEIGHTS_ALIASES.put("sha-3-384", WEIGHTS.get("sha3-384"));
        WEIGHTS_ALIASES.put("sha-3-512", WEIGHTS.get("sha3-512"));
        WEIGHTS_ALIASES.put("skein-256", WEIGHTS.get("skein-256-256"));
        WEIGHTS_ALIASES.put("skein256", WEIGHTS.get("skein-256-256"));
        WEIGHTS_ALIASES.put("skein-512", WEIGHTS.get("skein-512-512"));
        WEIGHTS_ALIASES.put("skein512", WEIGHTS.get("skein-512-512"));
        WEIGHTS_ALIASES.put("skein-1024", WEIGHTS.get("skein-1024-1024"));
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
        WEIGHTS_ALIASES.put("sum-64", WEIGHTS.get("sum64"));
        WEIGHTS_ALIASES.put("tiger-128", WEIGHTS.get("tiger128"));
        WEIGHTS_ALIASES.put("tiger-160", WEIGHTS.get("tiger160"));
        WEIGHTS_ALIASES.put("tiger192", WEIGHTS.get("tiger"));
        WEIGHTS_ALIASES.put("tiger-192", WEIGHTS.get("tiger"));
        WEIGHTS_ALIASES.put("tree:tiger192", WEIGHTS.get("tree:tiger"));
        WEIGHTS_ALIASES.put("tree:tiger-192", WEIGHTS.get("tree:tiger"));
        WEIGHTS_ALIASES.put("tth", WEIGHTS.get("tree:tiger"));
        WEIGHTS_ALIASES.put("tth2", WEIGHTS.get("tree:tiger2"));
        WEIGHTS_ALIASES.put("tiger_192_4_php", WEIGHTS.get("tiger-192-4-php"));
        WEIGHTS_ALIASES.put("tiger_160_4_php", WEIGHTS.get("tiger-160-4-php"));
        WEIGHTS_ALIASES.put("tiger_128_4_php", WEIGHTS.get("tiger-128-4-php"));
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
            System.err.printf("Warning: file size of %s is <= 20 MiB%n", file);
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
