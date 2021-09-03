/** 
 *******************************************************************************
 *
 * Jacksum 3.0.0 - a checksum utility in Java
 * Copyright (c) 2001-2021 Dipl.-Inf. (FH) Johann N. Löfflmann,
 * All Rights Reserved, <https://jacksum.net>.
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
 * this program. If not, see <https://www.gnu.org/licenses/>.
 *
 *******************************************************************************
 */
package net.jacksum.selectors;

import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.jacksum.JacksumAPI;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.algorithms.CombinedChecksum;

/**
 *
 * @author johann
 */
public class AllAlgorithms_Selector extends Selector {

    private final static String ID_ALL = "all";
    private final static String REGEXP_WIDTH = "^all:(\\d+)$";
    private final static String REGEXP_SEARCHSTRING = "^all:(.+)$";
    private static Map<String, String> algos;
    

    @Override
    public Map<String, String> getAvailableAlgorithms() {
        if (algos == null) {
            algos = new LinkedHashMap<>(1);
            algos.put(ID_ALL, "All algorithms");
        }
        return algos;
    }


    
    @Override
    public boolean doesMatch(String name) {
        if (name.equals(ID_ALL)) {
            return true;
        }

        // e.g. all:64
        Pattern pattern = Pattern.compile(REGEXP_WIDTH);
        Matcher matcher = pattern.matcher(name);

        if (matcher.find() && matcher.groupCount() == 1) {
            int width = Integer.valueOf(matcher.group(1));
            if (width < 8 || (width % 8 > 0)) {
                return false;
            }
            return true;
        }
        
        pattern = Pattern.compile(REGEXP_SEARCHSTRING);
        matcher = pattern.matcher(name);
        
        return matcher.find() && matcher.groupCount() == 1;
    }

    @Override
    public AbstractChecksum getImplementation(boolean alternate) throws NoSuchAlgorithmException {

        Map<String, String> map = null;
        int width;
        if (name.equals(ID_ALL)) {
            map = JacksumAPI.getAvailableAlgorithms();
        } else {
            // is it a valid width? e.g. all:64
            Pattern pattern = Pattern.compile(REGEXP_WIDTH);
            Matcher matcher = pattern.matcher(name);

            if (matcher.find() && matcher.groupCount() == 1) {
                width = Integer.valueOf(matcher.group(1));
                if (width < 8 || (width % 8 > 0)) {
                    throw new NoSuchAlgorithmException(String.format("Bit width must be a multiple of 8. Selected bit with %d does not match that criteria.",  width));
                }
                map = JacksumAPI.getAvailableAlgorithms(width);
                if (map.isEmpty()) {
                    throw new NoSuchAlgorithmException(String.format("No algorithm has been found with a bit width of %d.", width));
                }
            } else {
                // is it a search string?
                pattern = Pattern.compile(REGEXP_SEARCHSTRING);
                matcher = pattern.matcher(name);
                if (matcher.find() && matcher.groupCount() == 1) {
                    map = JacksumAPI.getAvailableAlgorithms(matcher.group(1));
                    if (map.isEmpty()) {
                        throw new NoSuchAlgorithmException(String.format("No algorithm has been found that matches %s", name));
                    }
                }
            }            
        }

        Iterator iterator = map.entrySet().iterator();
        String[] codes = new String[map.entrySet().size()];
        int i = 0;
        StringBuilder allNames = new StringBuilder();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            // String description = (String)entry.getValue();
            String n = ((String) entry.getKey());
            allNames.append(n);
            allNames.append('+');
            codes[i++] = n;
        }
        allNames.deleteCharAt(allNames.length() - 1);      
        
        name = allNames.toString();

        return new CombinedChecksum(codes, alternate);
    }

    @Override
    public AbstractChecksum getPrimaryImplementation() throws NoSuchAlgorithmException {
        // won't be called by Jacksum, because the getImplementation method has been overwrittten!
        return getImplementation(PRIMARY);
    }

}
