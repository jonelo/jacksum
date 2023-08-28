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
package net.jacksum;

import net.jacksum.algorithms.AbstractChecksum;
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
    public final static String VERSION = "3.8.0";
    /**
     * The URI of the program
     */
    public final static String URI = "https://jacksum.net";

    /**
     * The Copyright of the program
     */
    public final static String COPYRIGHT = "Copyright (C) 2001-2006, 2021-2023, Dipl.-Inf. (FH) Johann N. Loefflmann";

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
}
