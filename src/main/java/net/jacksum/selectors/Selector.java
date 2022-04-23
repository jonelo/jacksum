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
package net.jacksum.selectors;

import java.security.NoSuchAlgorithmException;
import java.util.Map;
import net.jacksum.algorithms.AbstractChecksum;

/**
 * A Selector is a verly lightweight object, it only contains info about the
 * algorithm and how to obtain an actual implementation
 *
 * @author johann
 */
abstract public class Selector implements SelectorInterface {

    /**
     * @return the actualAlternateImplementationUsed
     */
    @Override
    public boolean isActualAlternateImplementationUsed() {
        return actualAlternateImplementationUsed;
    }

    /**
     * @param actualAlternateImplementationUsed the
     * actualAlternateImplementationUsed to set
     */
    public void setActualAlternateImplementationUsed(boolean actualAlternateImplementationUsed) {
        this.actualAlternateImplementationUsed = actualAlternateImplementationUsed;
    }

    final static boolean PRIMARY = false;
    final static boolean ALTERNATE = true;
    private boolean actualAlternateImplementationUsed = false;

    public Selector() {
    }

    String name;

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Each selector is repsonsible for obtaining one or more algorithms.
     * This method controls whether a Selector can be used to obtain an
     * implementation by providing an algorithm name.
     * @param name the name of the algorithm
     * @return true if the name matches the responsibility of the Selector
     */
    @Override
    public boolean doesMatch(String name) {
//        return (name.equals(getPrimaryID()) || matchAnyOf(getAlternateIDs()));
        if (getAvailableAlgorithms().containsKey(name)) {
            return true;
        }
        Map<String, String> aliases = getAvailableAliases();
        if (aliases != null && aliases.containsKey(name)) {
            // replace the alias with the actual ID
            this.name = aliases.get(name);
            return true;
        }
        return false;
    }

    /*
    public boolean matchAnyOf(String[] strings) {
        if (strings == null) return false;
        for (String s : strings) {
            if (s == null) return false;
            if (name.equals(s)) return true;
        }
        return false;
    }
     */
    /**
     * Returns the implementation, dependent on the wish to select an alternate
     * implementation and the actual availablity of an alternate implementation.
     *
     * @param alternate should an alternate implementation be used?
     * @return the implementation dependent on the preference
     * @throws NoSuchAlgorithmException if there is no such algorithm.
     */
    @Override
    public AbstractChecksum getImplementation(boolean alternate) throws NoSuchAlgorithmException {
        if (alternate) {
            AbstractChecksum checksum = getAlternateImplementation();
            if (checksum != null) {
                setActualAlternateImplementationUsed(true);
                return checksum;
            }
            // let's continue with calling getPrimaryImplementation()
            // because if there is no alternate implementation at all,
            // we don't want to throw a NoSuchAlgotirhmException in this case
        }
        try {
            setActualAlternateImplementationUsed(false);
            return getPrimaryImplementation();
        } catch (NoSuchAlgorithmException nsae) {
            // if getPrimaryImplementation() fails, we still have a fallback
            // to call getAlternateImplementation()
            AbstractChecksum checksum = getAlternateImplementation();
            if (checksum != null) {
                setActualAlternateImplementationUsed(true);
                return checksum;
            } else {
                throw nsae;
            }
        }
    }

    @Override
    public Map<String, String> getAvailableAliases() {
        return null;
    }

    /**
     * Any selector should implement this method.
     *
     * @return the primary implementation of the algorithm
     * @throws NoSuchAlgorithmException if there is no such algorithm.
     */
    @Override
    public abstract AbstractChecksum getPrimaryImplementation() throws NoSuchAlgorithmException;

    /**
     * By default, there is no alternate Implementation.
     *
     * @return the alternate implementation
     * @throws NoSuchAlgorithmException if the algoritm is not found
     */
    @Override
    public AbstractChecksum getAlternateImplementation() throws NoSuchAlgorithmException {
        return null;
    }

}
