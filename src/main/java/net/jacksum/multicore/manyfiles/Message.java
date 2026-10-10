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
package net.jacksum.multicore.manyfiles;

import java.nio.file.Path;

/**
 * A message that is passed through the queues of the producer/consumer engine
 * that walks and hashes many files. It carries a type (an instruction or a
 * result), an optional info text, and a payload such as the path of the file.
 */
public class Message {

    /**
     * The type of a message.
     */
    public enum Type {
        // instructions what to do
        /** Instruction to hash the file. */
        HASH_FILE, // hash the file
        /** Instruction not to hash the file. */
        DONT_HASH_FILE, // file should not be hashed (for detecting new files)
        /** Instruction to hash standard input. */
        HASH_STDIN,
        /** Instruction not to hash standard input. */
        DONT_HASH_STDIN,
        // results
        /** Result: the file has been hashed. */
        FILE_HASHED,
        /** Result: the file has not been hashed on purpose. */
        FILE_NOT_HASHED,
        /** Result: the file has been hashed and the hash matches the expected value. */
        FILE_HASHED_AND_MATCHES_EXPECTATION,
        // 
        /** An informational message. */
        INFO,
        /** An error message. */
        ERROR,
        /** An informational message that a directory has been ignored. */
        INFO_DIR_IGNORED,
        // EXIT is the poison pill and marks the end of the queue
        /** The poison pill that marks the end of the queue. */
        EXIT
    }

    private Type type;
    private String info;
    private MessagePayload payload;
    
    /**
     * Gets the payload.
     *
     * @return the payload
     */
    public MessagePayload getPayload() {
        return payload;
    }

    /**
     * Sets the payload.
     *
     * @param payload the payload to set
     */
    public void setPayload(MessagePayload payload) {
        this.payload = payload;
    }

    /**
     * Gets the info text, e.g. the formatted hash or a message text.
     *
     * @return the info
     */
    public String getInfo() {
        return info;
    }

    /**
     * Sets the info text.
     *
     * @param info the info to set
     */
    public void setInfo(String info) {
        this.info = info;
    }


    /**
     * Creates a new message with an empty payload.
     *
     * @param type the type of the message
     */
    public Message(Type type) {
        this.type = type;
        payload = new MessagePayload();
    }
    
    /**
     * Creates a new message with an info text and an empty payload.
     *
     * @param type the type of the message
     * @param info the info text
     */
    public Message(Type type, String info) {
        this.type = type;
        this.info = info;
        payload = new MessagePayload();
    }

    /**
     * Creates a new message for a file name that a {@code Path} object does not
     * support, such as device names or ADS paths on Windows.
     *
     * @param type the type of the message
     * @param info the info text
     * @param specialPath the special path to store in the payload
     */
    public Message(Type type, String info, String specialPath) {
        this.type = type;
        this.info = info;
        payload = new MessagePayload();
        payload.setSpecialPath(specialPath);
    }

    /**
     * Creates a new message for a path.
     *
     * @param type the type of the message
     * @param info the info text
     * @param path the path to store in the payload, may be null
     */
    public Message(Type type, String info, Path path) {
        this.type = type;
        this.info = info;
        payload = new MessagePayload();
        payload.setPath(path);
    }

    /**
     * Gets the type of the message.
     *
     * @return the type
     */
    public Type getType() {
        return type;
    }
    
    /**
     * Sets the type of the message.
     *
     * @param type the type to set
     */
    public void setType(Type type) {
        this.type = type;
    }
   
    
}
