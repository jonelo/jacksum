/*


  Jacksum 4.0.2 - a checksum/hash tool written in Java
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
package net.jacksum.selectors;

import java.security.NoSuchAlgorithmException;
import java.util.Map;
import net.jacksum.algorithms.AbstractChecksum;

/**
 * The contract every algorithm selector has to fulfill. A selector is
 * responsible for one or more algorithms: it declares their IDs and aliases
 * and creates the primary or alternate implementation for a given name.
 *
 * @author johann
 */
public interface SelectorInterface {

    // ID, description    
    /**
     * Returns the algorithms this selector is responsible for.
     *
     * @return a map from algorithm ID to a human-readable description,
     * or {@code null} if the selector does not provide a fixed list of IDs
     */
    Map<String, String> getAvailableAlgorithms();

    // alias, ID
    /**
     * Returns the aliases of the algorithms this selector is responsible for.
     *
     * @return a map from alias to algorithm ID, or {@code null} if there
     * are no aliases
     */
    Map<String, String> getAvailableAliases();

    /**
     * Sets the name of the algorithm to be selected.
     *
     * @param name the name (ID) of the algorithm
     */
    void setName(String name);

    /**
     * Returns the name of the algorithm to be selected.
     *
     * @return the name (ID) of the algorithm
     */
    String getName();

    /**
     * Checks whether this selector is responsible for the given algorithm name.
     *
     * @param name the name of the algorithm
     * @return true if the selector can provide an implementation for the name
     */
    boolean doesMatch(String name);

    /**
     * Returns an implementation of the selected algorithm, either the primary
     * or, if requested and available, the alternate one.
     *
     * @param alternate true if an alternate implementation is preferred
     * @return the implementation of the algorithm
     * @throws NoSuchAlgorithmException if no implementation is available
     */
    AbstractChecksum getImplementation(boolean alternate) throws NoSuchAlgorithmException;

    /**
     * Returns the primary implementation of the selected algorithm.
     *
     * @return the primary implementation of the algorithm
     * @throws NoSuchAlgorithmException if no implementation is available
     */
    AbstractChecksum getPrimaryImplementation() throws NoSuchAlgorithmException;

    /**
     * Returns the alternate implementation of the selected algorithm.
     *
     * @return the alternate implementation, or {@code null} if there is none
     * @throws NoSuchAlgorithmException if the algorithm is not found
     */
    AbstractChecksum getAlternateImplementation() throws NoSuchAlgorithmException;

    /**
     * Tells whether the implementation returned last by
     * {@link #getImplementation(boolean)} was the alternate one.
     *
     * @return true if the alternate implementation is actually used
     */
    boolean isActualAlternateImplementationUsed();
}
