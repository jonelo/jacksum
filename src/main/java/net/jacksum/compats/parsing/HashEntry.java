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
package net.jacksum.compats.parsing;


/**
 * An entry of a hash list as it has been parsed from one line, consisting of
 * the hash value, the file name, and optional attributes such as the file size,
 * the timestamp, and the permissions.
 */
public class HashEntry {

    private String hash;
    private long filesize = -1;
    private String timestamp;
    private String permissions;
    private String filename;
    private Status status;
    
    /**
     * The verification status of an entry.
     */
    public enum Status {
        /** The file has been verified successfully. */
        OK,
        /** The verification of the file has failed. */
        FAILED,
        /** The file has been removed. */
        REMOVED }

    /**
     * Creates a new, empty {@code HashEntry}.
     */
    public HashEntry() {
    }

    /**
     * Gets the verification status.
     *
     * @return the status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Sets the verification status.
     *
     * @param status the status to set
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Gets the hash value.
     *
     * @return the hash
     */
    public String getHash() {
        return hash;
    }

    /**
     * Sets the hash value.
     *
     * @param hash the hash to set
     */
    public void setHash(String hash) {
        this.hash = hash;
    }

    /**
     * Gets the file size; -1 if it is unknown.
     *
     * @return the filesize
     */
    public long getFilesize() {
        return filesize;
    }

    /**
     * Sets the file size.
     *
     * @param filesize the filesize to set
     */
    public void setFilesize(long filesize) {
        this.filesize = filesize;
    }

    /**
     * Gets the timestamp.
     *
     * @return the timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp.
     *
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets the permissions.
     *
     * @return the permissions
     */
    public String getPermissions() {
        return permissions;
    }

    /**
     * Sets the permissions.
     *
     * @param permissions the permissions to set
     */
    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    /**
     * Gets the file name.
     *
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Sets the file name.
     *
     * @param filename the filename to set
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }
    
}
