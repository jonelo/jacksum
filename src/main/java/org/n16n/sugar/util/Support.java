/**
 *
 * NumericalChameleon 3.1.0 - more than an unit converter - a NumericalChameleon
 * Copyright (c) 2001-2021 Dipl.-Inf. (FH) Johann Nepomuk Loefflmann, All Rights
 * Reserved, <http://www.numericalchameleon.net>.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.n16n.sugar.util;

import java.util.*;

public class Support {


    
    public static Map<String, String> getSystemPropertiesAsMap() {        
        Map<String, String> map = new HashMap<>(128);
        addSystemPropertiesToMap(map);
        return map;
    }

    public static void addSystemPropertiesToMap(Map<String, String> map) {
        Properties systemProperties = System.getProperties();
        // the following statement truncates long lines!
        //     debugProperties.list(System.out);
        // so let's code it manually!
        for (Enumeration e = systemProperties.keys(); e.hasMoreElements();) {
            String o = (String) e.nextElement();
            map.put(o, systemProperties.getProperty(o));
        }
    }
    
    public static Map<String, String> propertiesToMap(Properties properties) {
        Map<String, String> map = new HashMap<>();
        // the following statement truncates long lines!
        //     properties.list(System.out);
        // so let's code it manually!
        for (Enumeration e = properties.keys(); e.hasMoreElements();) {
            String o = (String) e.nextElement();
            map.put(o, properties.getProperty(o));
        }
        return map;
    }

    public static List<String> sortPopertiesByKeys(Properties properties) {
        return getMapAsList(propertiesToMap(properties));
    }
    
    
    public static List<String> getMapAsList(Map<String, String> map) {
        List<String> list = new ArrayList<>();
        for (String key : map.keySet()) {
            list.add(String.format("%s=%s\n", key, map.get(key)));
        }
        Collections.sort(list);
        return list;
    }

    /*
     * // merge the system properties into our map Map<String,String> map2 =
     * getSystemPropertiesAsMap(); for (String key : map2.keySet()) {
     * map.put(key, map2.get(key)); }
     */
}
