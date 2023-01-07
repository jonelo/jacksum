/*

 Sugar for Java 3.0.0
 Copyright (C) 2001-2023  Dipl.-Inf. (FH) Johann N. Löfflmann,
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

import java.util.List;

public class Transformer {
    /**
     * Transforms a String List to a CSV String
     * @param list the list containing the values
     * @return one CSV String representing the list
     */
    public static String list2CsvString(List<String> list){
        StringBuilder sb = new StringBuilder();
        for (String string : list){
            if (sb.length() != 0){
                sb.append(",");
            }
            sb.append(string);
        }
        return sb.toString();
    }
}
