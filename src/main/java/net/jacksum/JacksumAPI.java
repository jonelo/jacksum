/*

  Jacksum 4.0.1 - a checksum/hash tool written in Java
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
package net.jacksum;

import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.algorithms.BrokenState;
import net.jacksum.algorithms.BrokenStateRegistry;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import net.loefflmann.sugar.util.Version;
import net.jacksum.formats.Encoding;
import net.jacksum.parameters.base.AlgorithmParameters;

/**
 * This is the Main Application Program Interface (API). Use this API to get an
 * instance of an algorithm and to determine both the available algorithms and
 * the available encodings to represent hash values.
 *
 * <p><strong>State that is shared by the whole process.</strong> A run is described by
 * its {@link net.jacksum.parameters.Parameters} object, and that object is the authority
 * for the run. A few values are process wide nonetheless, because they are read where no
 * parameters object is at hand. They are defaults for the objects that are created after
 * they have been set, and Jacksum itself does not write to them while it is running:</p>
 *
 * <ul>
 * <li>{@link net.jacksum.multicore.ThreadControl} holds the default number of threads for
 * hashing and for reading. A new Parameters object starts from these values.</li>
 * <li>{@link net.jacksum.algorithms.AbstractChecksum#setStdinName(String)} sets the default
 * name that represents the standard input stream. An instance that gets a parameters
 * object uses the name of that object instead.</li>
 * <li>{@link net.jacksum.HashFunctionFactory#setKey(byte[])} holds the key for HMAC,
 * because a hash function is requested by its name alone. Call
 * {@link net.jacksum.HashFunctionFactory#wipeKey()} when the key is not needed any longer:
 * as long as a key is stored, an HMAC that is requested without a key of its own would
 * silently be initialized with it, and the secret would stay in the Java heap. The command
 * line interface wipes the key when a run is over.</li>
 * </ul>
 */
public class JacksumAPI {

    // don't rely on this attribute, it may be removed in future releases!
    public final static boolean concurrencyManyAlgosEnabled;

    static {
        concurrencyManyAlgosEnabled
                = System.getProperty("jacksum.concurrency.manyalgos", "true").equals("true");
    }
    /**
     * The name of the API
     */
    public final static String NAME = "Jacksum";
    /**
     * The version of the API
     */
    public final static String VERSION = "4.0.1";
    /**
     * The URI of the program
     */
    public final static String URI = "https://jacksum.net";

    /**
     * The Copyright of the program
     */
    public final static String COPYRIGHT = "Copyright (C) 2001-2006, 2021-2026, Dipl.-Inf. (FH) Johann N. Loefflmann";

    /**
     * Returns a Version object of this API.
     *
     * @return a Version object of this API
     */
    public static Version getVersion() {
        return new Version(VERSION);
    }

    /**
     * Returns a String representing the version of this API.
     *
     * @return a String representing the version of this API
     */
    public static String getVersionString() {
        return VERSION;
    }

    /**
     * Returns the name of this API.
     *
     * @return the name of this API
     */
    public static String getName() {
        return NAME;
    }

    public static String getURI() {
        return URI;
    }

    /**
     * Runs the Command Line Interface (CLI)
     *
     * @param args the program arguments
     */
    public static void runCLI(String[] args) {
        net.jacksum.cli.Main.main(args);
    }

    /**
     * Returns all available encodings.
     *
     * @return a Map with key and value pairs, 2nd is String (the key can be
     * used to feed the method setEncoding(), the value of the pair is a
     * description of the encoding)
     */
    public static Map<Encoding, String> getAvailableEncodings() {
        return Encoding.getAvailableEncodings();
    }

    /**
     * Returns all available algorithms.
     *
     * @return a Map with key and value pairs, both are Strings (the key can be
     * used to feed the method getChecksumInstance(), the value of the pair is
     * the name of the algorithm which can be used in a GUI for example)
     */
    public static Map<String, String> getAvailableAlgorithms() {
        return HashFunctionFactory.getAvailableAlgorithms();
    }

    public static Map<String, String> getAvailableHMACs() {
        return HashFunctionFactory.getAvailableHMACs();
    }

    /**
     * Gets all available algorithms, dependent on a particular width
     *
     * @param width the width in bits
     * @return a Map with key and value pairs, both are Strings (the key can be
     * used to feed the method getChecksumInstance(), the value of the pair is
     * the name of the algorithm which can be used in a GUI for example)
     */
    public static Map<String, String> getAvailableAlgorithms(int width) {
        return HashFunctionFactory.getAvailableAlgorithms(width);
    }
    
    /**
     * Gets all available algorithms, dependent on a particular search string.
     * It searches in all algorithm IDs and alias IDs
     *
     * @param searchString the search string
     * @return a Map with key and value pairs, both are Strings (the key can be
     * used to feed the method getChecksumInstance(), the value of the pair is
     * the name of the algorithm which can be used in a GUI for example)
     */    
    public static Map<String, String> getAvailableAlgorithms(String searchString) {
        return HashFunctionFactory.getAvailableAlgorithms(searchString);
    }

    /**
     * Returns an object of a checksum algorithm. It tries to select an
     * implementation from the Java API.
     *
     * @param algorithm code for the checksum algorithm
     * @return an object of a checksum algorithm
     * @exception NoSuchAlgorithmException if algorithm is unknown
     */
    public static AbstractChecksum getChecksumInstance(String algorithm)
            throws NoSuchAlgorithmException {
        return getChecksumInstance(algorithm, false);
    }

    /**
     * Returns an object of a checksum algorithm. The method selects an
     * implementation dependent on the AlgorithmParameters
     *
     * @param parameters the AlgorithmParameters
     * @return an object of a checksum algorithm
     * @throws java.security.NoSuchAlgorithmException if an algorithm cannot be found by the parameters.
     */
    public static AbstractChecksum getInstance(AlgorithmParameters parameters)
            throws NoSuchAlgorithmException {
        return JacksumAPI.getChecksumInstance(
                parameters.getAlgorithmIdentifier(),
                parameters.isAlternateImplementationWanted());
    }

    /**
     * Returns an object of a checksum algorithm.
     *
     * @param algorithm identifier for the checksum algorithm
     * @param alternate if true, a pure Java implementation is selected if
     * available
     * @return an object of a checksum algorithm
     * @exception NoSuchAlgorithmException if algorithm is unknown
     */
    public static AbstractChecksum getChecksumInstance(String algorithm,
            boolean alternate) throws NoSuchAlgorithmException {

        return HashFunctionFactory.getHashFunction(algorithm, alternate);
    }

    public static List<String> getAvailableAliases(String algorithm) throws NoSuchAlgorithmException {
        return HashFunctionFactory.getAvailableAliases(algorithm);
    }

    /**
     * Returns whether the algorithm with the given identifier is considered
     * broken. The information is read from Jacksum's documentation.
     *
     * @param algorithm identifier for the algorithm, e.g. "md5",
     * "haval_128_3", "hmac:sha1"
     * @return the state, never null, {@link BrokenState#NOT_APPLICABLE} if the
     * algorithm is unknown or if it does not claim cryptographic security
     * @since 4.0.0
     */
    public static BrokenState getBrokenState(String algorithm) {
        return BrokenStateRegistry.getBrokenState(algorithm);
    }

    /**
     * Returns the explanation of the state that is returned by
     * {@link #getBrokenState(String)}, as it is documented in Jacksum's
     * documentation. The lines are returned as they are wrapped in the
     * documentation.
     *
     * @param algorithm identifier for the algorithm, e.g. "md5"
     * @return the lines of the explanation, an empty list if there is none
     * @since 4.0.0
     */
    public static List<String> getBrokenDescription(String algorithm) {
        return BrokenStateRegistry.getBrokenDescription(algorithm);
    }

    /**
     * Builds a cache that answers all subsequent calls of
     * {@link #getBrokenState(String)} and
     * {@link #getBrokenDescription(String)} without any further I/O. Calling
     * this method is only worthwhile if many algorithms are queried, because a
     * single query is answered by one pass over Jacksum's documentation anyway.
     * The method is idempotent.
     *
     * @since 4.0.0
     */
    public static void preloadBrokenStates() {
        BrokenStateRegistry.preload();
    }

    /**
     * Releases the cache that has been built by
     * {@link #preloadBrokenStates()}.
     *
     * @since 4.0.0
     */
    public static void unloadBrokenStates() {
        BrokenStateRegistry.unload();
    }
}
