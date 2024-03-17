// https://web.archive.org/web/20160620024611/http://www.saphir2.com/sphlib/files/sphlib-3.0.zip
package net.jacksum.zzadopt.fr.cryptohash.ext;

import net.jacksum.zzadopt.fr.cryptohash.Digest;
import net.jacksum.zzadopt.fr.cryptohash.BLAKE224;
import net.jacksum.zzadopt.fr.cryptohash.BLAKE256;
import net.jacksum.zzadopt.fr.cryptohash.BLAKE384;
import net.jacksum.zzadopt.fr.cryptohash.BLAKE512;
import net.jacksum.zzadopt.fr.cryptohash.JH224;
import net.jacksum.zzadopt.fr.cryptohash.JH384;
import net.jacksum.zzadopt.fr.cryptohash.JH512;
import net.jacksum.zzadopt.fr.cryptohash.JH256;
import net.jacksum.zzadopt.fr.cryptohash.Groestl224;
import net.jacksum.zzadopt.fr.cryptohash.Groestl256;
import net.jacksum.zzadopt.fr.cryptohash.Groestl384;
import net.jacksum.zzadopt.fr.cryptohash.Groestl512;
import net.jacksum.zzadopt.fr.cryptohash.ECHO224;
import net.jacksum.zzadopt.fr.cryptohash.ECHO256;
import net.jacksum.zzadopt.fr.cryptohash.ECHO384;
import net.jacksum.zzadopt.fr.cryptohash.ECHO512;
import net.jacksum.zzadopt.fr.cryptohash.Fugue224;
import net.jacksum.zzadopt.fr.cryptohash.Fugue256;
import net.jacksum.zzadopt.fr.cryptohash.Fugue384;
import net.jacksum.zzadopt.fr.cryptohash.Fugue512;
import net.jacksum.zzadopt.fr.cryptohash.Luffa224;
import net.jacksum.zzadopt.fr.cryptohash.Luffa256;
import net.jacksum.zzadopt.fr.cryptohash.Luffa384;
import net.jacksum.zzadopt.fr.cryptohash.Luffa512;
import net.jacksum.zzadopt.fr.cryptohash.BlueMidnightWish224;
import net.jacksum.zzadopt.fr.cryptohash.BlueMidnightWish256;
import net.jacksum.zzadopt.fr.cryptohash.BlueMidnightWish384;
import net.jacksum.zzadopt.fr.cryptohash.BlueMidnightWish512;
import net.jacksum.zzadopt.fr.cryptohash.SIMD224;
import net.jacksum.zzadopt.fr.cryptohash.SIMD256;
import net.jacksum.zzadopt.fr.cryptohash.SIMD384;
import net.jacksum.zzadopt.fr.cryptohash.SIMD512;
import net.jacksum.zzadopt.fr.cryptohash.CubeHash224;
import net.jacksum.zzadopt.fr.cryptohash.CubeHash256;
import net.jacksum.zzadopt.fr.cryptohash.CubeHash384;
import net.jacksum.zzadopt.fr.cryptohash.CubeHash512;

import net.jacksum.zzadopt.fr.cryptohash.RadioGatun32;
import net.jacksum.zzadopt.fr.cryptohash.RadioGatun64;
import net.jacksum.zzadopt.fr.cryptohash.PANAMA;

public class HashFactory implements Registry {
    
   /** Trivial constructor to enforce <i>Singleton</i> pattern. */
   private HashFactory() {
      super();
   }

   /**
    * <p>Return an instance of a hash algorithm given its name.</p>
    *
    * @param name the name of the hash algorithm.
    * @return an instance of the hash algorithm, or null if none found.
    * @exception InternalError if the implementation does not pass its self-
    * test.
    */
   public static Digest getInstance(String name) {
      if (name == null) {
         return null;
      }
      name = name.trim();
      Digest result = null;
       switch (name) {
           case PANAMA:
               result = new PANAMA();
               break;
           case RADIOGATUN32:
               result = new RadioGatun32();
               break;
           case RADIOGATUN64:
               result = new RadioGatun64();
               break;
           case BLAKE512:
               result = new BLAKE512();
               break;
           case BLAKE384:
               result = new BLAKE384();
               break;
           case BLAKE256:
               result = new BLAKE256();
               break;
           case BLAKE224:
               result = new BLAKE224();
               break;
           case GROESTL512:
               result = new Groestl512();
               break;
           case GROESTL384:
               result = new Groestl384();
               break;
           case GROESTL256:
               result = new Groestl256();
               break;
           case GROESTL224:
               result = new Groestl224();
               break;
           case JH512:
               result = new JH512();
               break;
           case JH384:
               result = new JH384();
               break;
           case JH256:
               result = new JH256();
               break;
           case JH224:
               result = new JH224();
               break;
           case ECHO512:
               result = new ECHO512();
               break;
           case ECHO384:
               result = new ECHO384();
               break;
           case ECHO256:
               result = new ECHO256();
               break;
           case ECHO224:
               result = new ECHO224();
               break;
           case FUGUE512:
               result = new Fugue512();
               break;
           case FUGUE384:
               result = new Fugue384();
               break;
           case FUGUE256:
               result = new Fugue256();
               break;
           case FUGUE224:
               result = new Fugue224();
               break;
           case LUFFA512:
               result = new Luffa512();
               break;
           case LUFFA384:
               result = new Luffa384();
               break;
           case LUFFA256:
               result = new Luffa256();
               break;
           case LUFFA224:
               result = new Luffa224();
               break;
           case BMW512:
               result = new BlueMidnightWish512();
               break;
           case BMW384:
               result = new BlueMidnightWish384();
               break;
           case BMW256:
               result = new BlueMidnightWish256();
               break;
           case BMW224:
               result = new BlueMidnightWish224();
               break;
           case SIMD512:
               result = new SIMD512();
               break;
           case SIMD384:
               result = new SIMD384();
               break;
           case SIMD256:
               result = new SIMD256();
               break;
           case SIMD224:
               result = new SIMD224();
               break;

           case CUBEHASH512:
               result = new CubeHash512();
               break;
           case CUBEHASH384:
               result = new CubeHash384();
               break;
           case CUBEHASH256:
               result = new CubeHash256();
               break;
           case CUBEHASH224:
               result = new CubeHash224();
               break;

           default:
               break;
       }

      return result;
   }
}
