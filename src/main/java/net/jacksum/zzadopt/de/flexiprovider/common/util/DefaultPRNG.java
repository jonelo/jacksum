package net.jacksum.zzadopt.de.flexiprovider.common.util;

import net.jacksum.zzadopt.de.flexiprovider.api.SecureRandom;

/**
 * FlexiAPI wrapper for the default PRNG obtained via JCA.
 * 
 * @author Martin D�ring
 */
public class DefaultPRNG extends SecureRandom {

    java.security.SecureRandom javaRand;

    public DefaultPRNG() {
	javaRand = new java.security.SecureRandom();
    }

    public byte[] generateSeed(int numBytes) {
	return javaRand.generateSeed(numBytes);
    }

    public void nextBytes(byte[] bytes) {
	javaRand.nextBytes(bytes);
    }

    public void setSeed(byte[] seed) {
	javaRand.setSeed(seed);
    }

}
