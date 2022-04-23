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
package net.jacksum;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.selectors.Selector;
import net.jacksum.selectors.SelectorInterface;
import static net.jacksum.selectors.Selectors.allSelectorClasses;
import static net.jacksum.selectors.Selectors.allSupportedSelectorClasses;

/**
 * HashFunctionFactory
 */
public class HashFunctionFactory {

    private static final Map<String, Class> cacheOfSelectorClasses = new HashMap<>();

    private static boolean cacheOfSelectorClassesEnabled = true;

    public static void setCacheOfSelectorClassesEnabled(boolean bool) {
        cacheOfSelectorClassesEnabled = bool;
    }

    /**
     * Get a hash function.
     * @param algorithm the name of the algorithm.
     * @param alternate whether an alternative should be used
     * @return an instance of an AbstractChecksum that matches the criteria
     * @throws NoSuchAlgorithmException if an algorithm with the criteria cannot be found
     */
    public static AbstractChecksum getHashFunction(String algorithm, boolean alternate) throws NoSuchAlgorithmException {

        // construct an array of classes so that we can iterate over it
        Class<?>[] arrayOfSelectorClasses;
        if (cacheOfSelectorClasses.containsKey(algorithm)) {
            arrayOfSelectorClasses = new Class[1];
            arrayOfSelectorClasses[0] = cacheOfSelectorClasses.get(algorithm);
        } else {
            arrayOfSelectorClasses = allSelectorClasses;
        }

        for (Class<?> selectorClass : arrayOfSelectorClasses) {
            try {
                Constructor<?> constructor = selectorClass.getConstructor();
                SelectorInterface selector = (Selector) constructor.newInstance();

                selector.setName(algorithm);
//System.out.println(selector);

                if (selector.doesMatch(algorithm)) {
                    AbstractChecksum checksum = selector.getImplementation(alternate);
                    checksum.setActualAlternateImplementationUsed(selector.isActualAlternateImplementationUsed());
                    checksum.setName(selector.getName());

                    if (cacheOfSelectorClassesEnabled) {
                        // fill the cache with the algorithm that we have just found
                        // in order to save time the next time the same request comes along
                        cacheOfSelectorClasses.put(selector.getName(), selectorClass);

                        // and if the selector has aliases for us, put them to the cache as well
                        Map<String, String> aliases = selector.getAvailableAliases();

                        if (aliases != null) {
                            for (String key : aliases.keySet()) {
                                cacheOfSelectorClasses.put(key, selectorClass);
                            }
                        }
                    }
                    return checksum;
                }
            } catch (NoSuchMethodException | SecurityException | InstantiationException
                    | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(JacksumAPI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        throw new NoSuchAlgorithmException(algorithm + " is an unknown algorithm.");

    }

    /**
     * Gets all available algorithms.
     *
     * @return a Map with key and value pairs, both are Strings (the key can be
     * used to feed the method getChecksumInstance(), the value of the pair is
     * the name of the algorithm which can be used in a GUI for example)
     */
    public static Map<String, String> getAvailableAlgorithms() {
        // key, description
        Map<String, String> map = new LinkedHashMap<>(683); // ceil(512/0.75)

        for (Class<?> selectorClass : allSupportedSelectorClasses) {
            try {
                Constructor<?> constructor = selectorClass.getConstructor();
                SelectorInterface selector = (Selector) constructor.newInstance();
                map.putAll(selector.getAvailableAlgorithms());
            } catch (NoSuchMethodException | SecurityException | InstantiationException
                    | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(JacksumAPI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return map;
    }

    /**
     * Returns all available algorithms that have a particular width
     *
     * @param width the width in bits that an algorithm must have.
     * @return a map that contains two strings: the algo ID and a description
     */
    public static Map<String, String> getAvailableAlgorithms(int width) {
        Map<String, String> map = getAvailableAlgorithms();
        Map<String, String> mapFiltered = new LinkedHashMap<>(171); // ceil(128/0,75)

        Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            AbstractChecksum checksum;
            try {
                checksum = JacksumAPI.getChecksumInstance(entry.getKey());
            } catch (NoSuchAlgorithmException e) {
                // should not happen
                e.printStackTrace();
                throw new RuntimeException("INTERNAL ERROR in JacksumAPI.getAvailableAlgorithms(int width)");
            }
            if (checksum != null && checksum.getSize() == width) {
                mapFiltered.put(entry.getKey(), entry.getValue());
            }
        }
        return mapFiltered;

    }

    /**
     * Returns all available algorithms that have a particular substring.
     *
     * @param searchString the search string in order to match algorithms.
     * @return a map that contains two strings: the algo id and a description
     */
    public static Map<String, String> getAvailableAlgorithms(String searchString) {

        Map<String, String> mapFiltered = new LinkedHashMap<>(683); // ceil(512/0.75)

        for (Class<?> selectorClass : allSupportedSelectorClasses) {
            try {
                Constructor<?> constructor = selectorClass.getConstructor();
                SelectorInterface selector = (Selector) constructor.newInstance();

                // algo id, description
                Map<String, String> mapAlgos = selector.getAvailableAlgorithms();
                for (Map.Entry<String, String> entry : mapAlgos.entrySet()) {
                    if (entry.getKey().contains(searchString)) {
                        mapFiltered.put(entry.getKey(), entry.getValue()); // algo id, description
                    }
                }

                // Let's search in the aliases.
                // The map contains alias, and algo id
                Map<String, String> mapAliases = selector.getAvailableAliases();
                if (mapAliases != null) {
                    for (Map.Entry<String, String> entry : mapAliases.entrySet()) {
                        // search only in the alias if we haven't added the algo id yet already
                        if (!mapFiltered.containsKey(entry.getValue()) && entry.getKey().contains(searchString)) {
                            mapFiltered.put(entry.getValue(), mapAlgos.get(entry.getKey())); // algo id, description
                        }
                    }
                }

            } catch (NoSuchMethodException | SecurityException | InstantiationException
                    | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(JacksumAPI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return mapFiltered;
    }


    /**
     * Returns all available aliases for all
     * @param algorithm the algorithm name
     * @throws NoSuchAlgorithmException if the algorithm cannot be found
     * @return a map that contains two strings: the algo id and a description
     */
    public static List<String> getAvailableAliases(String algorithm) throws NoSuchAlgorithmException {

        List<String> aliases = new ArrayList<>();
        for (Class<?> selectorClass : allSupportedSelectorClasses) {
            try {
                Constructor<?> constructor = selectorClass.getConstructor();
                SelectorInterface selector = (Selector) constructor.newInstance();


                // algo id, description
                Map<String, String> mapAlgos = selector.getAvailableAlgorithms();
                for (Map.Entry<String, String> entry : mapAlgos.entrySet()) {
// System.out.println(entry.getKey());
                    if (entry.getKey().equals(algorithm)) {
// System.out.println("we are here:"+algorithm);
                        // Let's search in the aliases.
                        // The map contains alias, and algo id
                        Map<String, String> mapAliases = selector.getAvailableAliases();
                        if (mapAliases != null) {
                            for (Map.Entry<String, String> aliasEntry : mapAliases.entrySet()) {
                                if (aliasEntry.getValue().equals(algorithm)) {
                                    aliases.add(aliasEntry.getKey());
                                }
                            }
                        }
                    }
                }

            } catch (NoSuchMethodException | SecurityException | InstantiationException
                    | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(JacksumAPI.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        return aliases;

        //throw new NoSuchAlgorithmException(algorithm + " is an unknown algorithm.");
    }

}
