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
package net.jacksum.multicore.manyfiles;

import java.nio.file.Path;

public class Message {

    public enum Type {
        // instructions what to do
        HASH_FILE, // hash the file
        DONT_HASH_FILE, // file should not be hashed (for detecting new files)
        HASH_STDIN,
        DONT_HASH_STDIN,
        // results
        FILE_HASHED, FILE_NOT_HASHED, FILE_HASHED_AND_MATCHES_EXPECTATION,
        // 
        INFO, ERROR, INFO_DIR_IGNORED,
        // EXIT is the poision pill and marks the end of the Queue
        EXIT;
    }

    private Type type;
    private String info;
    private MessagePayload payload;
    
    /**
     * @return the payload
     */
    public MessagePayload getPayload() {
        return payload;
    }

    /**
     * @param payload the payload to set
     */
    public void setPayload(MessagePayload payload) {
        this.payload = payload;
    }

    /**
     * @return the info
     */
    public String getInfo() {
        return info;
    }

    /**
     * @param info the info to set
     */
    public void setInfo(String info) {
        this.info = info;
    }


    
    public Message(Type type, String info) {
        this.type = type;
        this.info = info;
        payload = new MessagePayload();
    }
    
    public Message(Type type) {
        this.type = type;
        payload = new MessagePayload();
    }

    public Message(Type type, Path path) {
        this.type = type;
        payload = new MessagePayload();
        payload.setPath(path);
    }
    
    public Message(Type type, Path path, String info) {
        this.type = type;
        this.info = info;
        payload = new MessagePayload();
        payload.setPath(path);
    }

    public Type getType() {
        return type;
    }
    
    public void setType(Type type) {
        this.type = type;
    }
   
    
}
