/*

 Sugar for Java 1.6.0
 Copyright (C) 2001-2022  Dipl.-Inf. (FH) Johann N. Löfflmann,
 All Rights Reserved, https://johann.loefflmann.net

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 @author Johann N. Löfflmann

 */
package net.loefflmann.sugar.util;

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
