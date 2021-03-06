
package net.jacksum.zzadopt.fr.cryptohash.ext;

// author: johann loefflmann (jonelo@jonelo.de)
public interface Registry {

   // final round
   final static String KECCAK224 = "keccak-224";
   final static String KECCAK256 = "keccak-256";
   final static String KECCAK384 = "keccak-384";
   final static String KECCAK512 = "keccak-512";
   final static String BLAKE224 = "blake224";
   final static String BLAKE256 = "blake256";
   final static String BLAKE384 = "blake384";
   final static String BLAKE512 = "blake512";
   final static String GROESTL224 = "groestl224";
   final static String GROESTL256 = "groestl256";
   final static String GROESTL384 = "groestl384";
   final static String GROESTL512 = "groestl512";
   final static String JH224 = "jh224";
   final static String JH256 = "jh256";
   final static String JH384 = "jh384";
   final static String JH512 = "jh512";


   // round 2
   final static String ECHO224 = "echo224";
   final static String ECHO256 = "echo256";
   final static String ECHO384 = "echo384";
   final static String ECHO512 = "echo512";
   
   final static String FUGUE224 = "fugue224";
   final static String FUGUE256 = "fugue256";
   final static String FUGUE384 = "fugue384";
   final static String FUGUE512 = "fugue512";

   final static String LUFFA224 = "luffa224";
   final static String LUFFA256 = "luffa256";
   final static String LUFFA384 = "luffa384";
   final static String LUFFA512 = "luffa512";
   

   // not submitted to the SHA-3 competition
   final static String RADIOGATUN32 = "radiogatun32";
   final static String RADIOGATUN64 = "radiogatun64";
   final static String PANAMA = "panama";
   
}

