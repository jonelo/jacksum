/*

 Sugar for Java 3.0.0
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
package org.n16n.sugar.io;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.ArrayList;

public class NtfsAdsFinder {
    public static ArrayList<String> find(Path path) throws IOException,  InterruptedException {
        ArrayList<String> list = null;

        // check that user defined attributes are supported by the file store
        FileStore store = Files.getFileStore(path);
        if (store.supportsFileAttributeView(UserDefinedFileAttributeView.class)) {
            list = new ArrayList<>();
            UserDefinedFileAttributeView view =
                    Files.getFileAttributeView(path, UserDefinedFileAttributeView.class);

            for (String name: view.list()) {
                if (path.toString().equals(".")) {
                    list.add(String.format("%s:%s:$DATA", ".\\", name));
                } else {
                    list.add(String.format("%s:%s:$DATA", path, name));
                }
            }
        }
        return list;
    }
}
