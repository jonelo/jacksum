package net.jacksum.zzadopt.gnu.crypto.hash;

// ----------------------------------------------------------------------------
// $Id: HashFactory.java,v 1.11 2003/06/14 14:44:18 raif Exp $
//
// Copyright (C) 2001, 2002, 2003 Free Software Foundation, Inc.
//
// This file is part of GNU Crypto.
//
// GNU Crypto is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2, or (at your option)
// any later version.
//
// GNU Crypto is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; see the file COPYING.  If not, write to the
//
//    Free Software Foundation Inc.,
//    59 Temple Place - Suite 330,
//    Boston, MA 02111-1307
//    USA
//
// Linking this library statically or dynamically with other modules is
// making a combined work based on this library.  Thus, the terms and
// conditions of the GNU General Public License cover the whole
// combination.
//
// As a special exception, the copyright holders of this library give
// you permission to link this library with independent modules to
// produce an executable, regardless of the license terms of these
// independent modules, and to copy and distribute the resulting
// executable under terms of your choice, provided that you also meet,
// for each linked independent module, the terms and conditions of the
// license of that module.  An independent module is a module which is
// not derived from or based on this library.  If you modify this
// library, you may extend this exception to your version of the
// library, but you are not obligated to do so.  If you do not wish to
// do so, delete this exception statement from your version.
// ----------------------------------------------------------------------------

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.jacksum.zzadopt.gnu.crypto.Registry;

/**
 * <p>A <i>Factory</i> to instantiate message digest algorithm instances.</p>
 *
 * @version $Revision: 1.11 $
 */
public class HashFactory implements Registry {

   // Constants and variables
   // -------------------------------------------------------------------------

   // Constructor(s)
   // -------------------------------------------------------------------------

   /** Trivial constructor to enforce <i>Singleton</i> pattern. */
   private HashFactory() {
      super();
   }

   // Class methods
   // -------------------------------------------------------------------------

   /**
    * <p>Return an instance of a hash algorithm given its name.</p>
    *
    * @param name the name of the hash algorithm.
    * @return an instance of the hash algorithm, or null if none found.
    * @exception InternalError if the implementation does not pass its self-
    * test.
    */
   public static IMessageDigest getInstance(String name) {
      if (name == null) {
         return null;
      }

      name = name.trim();
      IMessageDigest result = null;
      // added by jonelo
      if (name.equalsIgnoreCase(WHIRLPOOL2001_HASH)) {
         result = new Whirlpool2001();
      // added by jonelo
      } else if (name.equalsIgnoreCase(WHIRLPOOL2000_HASH)) {
         result = new Whirlpool2000();
      } else if (name.equalsIgnoreCase(WHIRLPOOL2003_HASH)) {
         result = new Whirlpool2003();
      } else if (name.equalsIgnoreCase(RIPEMD128_HASH)
            || name.equalsIgnoreCase(RIPEMD_128_HASH)) {
         result = new RipeMD128();
      } else if (name.equalsIgnoreCase(RIPEMD160_HASH)
            || name.equalsIgnoreCase(RIPEMD_160_HASH)) {
         result = new RipeMD160();
      } else if (name.equalsIgnoreCase(SHA160_HASH)
            || name.equalsIgnoreCase(SHA_1_HASH)
            || name.equalsIgnoreCase(SHA1_HASH)
            || name.equalsIgnoreCase(SHA_HASH)) {
         result = new Sha160();
      // added by jonelo
      } else if (name.equalsIgnoreCase(SHA224_HASH)) {
         result = new Sha224();
      } else if (name.equalsIgnoreCase(SHA384_HASH)) {
         result = new Sha384();
      } else if (name.equalsIgnoreCase(SHA256_HASH)) {
         result = new Sha256();
      } else if (name.equalsIgnoreCase(SHA512_HASH)) {
         result = new Sha512();
      } else if (name.equalsIgnoreCase(TIGER_HASH)) {
         result = new Tiger();
      } else if (name.equalsIgnoreCase(TIGER2_HASH)) {
         result = new Tiger2();
      } else if (name.equalsIgnoreCase(TIGER160_HASH)) {
         result = new Tiger160();
      } else if (name.equalsIgnoreCase(TIGER128_HASH)) {
         result = new Tiger128();
      } else if (name.equalsIgnoreCase(MD5_HASH)) {
         result = new MD5();
      } else if (name.equalsIgnoreCase(MD4_HASH)) {
         result = new MD4();
      } else if (name.equalsIgnoreCase(MD2_HASH)) {
         result = new MD2();
      } else if (name.equalsIgnoreCase(HAVAL_HASH)) {
         result = new Haval();

      // added by jonelo
      } else if (name.equalsIgnoreCase(HAVAL_HASH_128_3)) {
         result = new Haval(Haval.HAVAL_128_BIT, Haval.HAVAL_3_ROUND);
      } else if (name.equalsIgnoreCase(HAVAL_HASH_128_4)) {
         result = new Haval(Haval.HAVAL_128_BIT, Haval.HAVAL_4_ROUND);
      } else if (name.equalsIgnoreCase(HAVAL_HASH_128_5)) {
         result = new Haval(Haval.HAVAL_128_BIT, Haval.HAVAL_5_ROUND);

      } else if (name.equalsIgnoreCase(HAVAL_HASH_160_3)) {
         result = new Haval(Haval.HAVAL_160_BIT, Haval.HAVAL_3_ROUND);
      } else if (name.equalsIgnoreCase(HAVAL_HASH_160_4)) {
         result = new Haval(Haval.HAVAL_160_BIT, Haval.HAVAL_4_ROUND);
      } else if (name.equalsIgnoreCase(HAVAL_HASH_160_5)) {
         result = new Haval(Haval.HAVAL_160_BIT, Haval.HAVAL_5_ROUND);

      } else if (name.equalsIgnoreCase(HAVAL_HASH_192_3)) {
         result = new Haval(Haval.HAVAL_192_BIT, Haval.HAVAL_3_ROUND);
      } else if (name.equalsIgnoreCase(HAVAL_HASH_192_4)) {
         result = new Haval(Haval.HAVAL_192_BIT, Haval.HAVAL_4_ROUND);
      } else if (name.equalsIgnoreCase(HAVAL_HASH_192_5)) {
         result = new Haval(Haval.HAVAL_192_BIT, Haval.HAVAL_5_ROUND);

      } else if (name.equalsIgnoreCase(HAVAL_HASH_224_3)) {
         result = new Haval(Haval.HAVAL_224_BIT, Haval.HAVAL_3_ROUND);
      } else if (name.equalsIgnoreCase(HAVAL_HASH_224_4)) {
         result = new Haval(Haval.HAVAL_224_BIT, Haval.HAVAL_4_ROUND);
      } else if (name.equalsIgnoreCase(HAVAL_HASH_224_5)) {
         result = new Haval(Haval.HAVAL_224_BIT, Haval.HAVAL_5_ROUND);

      } else if (name.equalsIgnoreCase(HAVAL_HASH_256_3)) {
         result = new Haval(Haval.HAVAL_256_BIT, Haval.HAVAL_3_ROUND);
      } else if (name.equalsIgnoreCase(HAVAL_HASH_256_4)) {
         result = new Haval(Haval.HAVAL_256_BIT, Haval.HAVAL_4_ROUND);
      } else if (name.equalsIgnoreCase(HAVAL_HASH_256_5)) {
         result = new Haval(Haval.HAVAL_256_BIT, Haval.HAVAL_5_ROUND);
      } else if (name.equalsIgnoreCase(SHA0_HASH)) {
         result = new Sha0();
      } else if (name.equalsIgnoreCase(HAS160_HASH)) {
          result = new Has160();
      }
      // end

//      selfTests only during test phases
//
//      if (result != null && !result.selfTest()) {
//         throw new InternalError(result.name());
//      }

      return result;
   }

   /**
    * <p>Returns a {@link Set} of names of hash algorithms supported by this
    * <i>Factory</i>.</p>
    *
    * @return a {@link Set} of hash names (Strings).
    */
   public static final Set getNames() {
      HashSet<String> hs = new HashSet<>();
      hs.add(WHIRLPOOL2001_HASH);
      hs.add(RIPEMD128_HASH);
      hs.add(RIPEMD160_HASH);
      hs.add(SHA160_HASH);
      
      // added by jonelo
      hs.add(SHA224_HASH);
      hs.add(SHA256_HASH);
      hs.add(SHA384_HASH);
      hs.add(SHA512_HASH);
      hs.add(TIGER_HASH);
      hs.add(HAVAL_HASH);
      hs.add(MD5_HASH);
      hs.add(MD4_HASH);
      hs.add(MD2_HASH);
      hs.add(SHA0_HASH);
      return Collections.unmodifiableSet(hs);
   }

   // Instance methods
   // -------------------------------------------------------------------------
}
