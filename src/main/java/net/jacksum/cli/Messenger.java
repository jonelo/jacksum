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
package net.jacksum.cli;


import java.io.Serializable;

/**
 * Prints info, warning, and error messages to standard error, depending
 * on the verbosity settings.
 */
public class Messenger implements Serializable {

    private static final long serialVersionUID = 6273013117713291439L;

    /**
     * The type of a message.
     */
    public enum MsgType {
        /** An informational message. */
        INFO,
        /** A warning message. */
        WARNING,
        /** An error message. */
        ERROR
    }

    /** The verbosity settings that control which messages are printed. */
    private Verbose verbose;

    /**
     * Creates a new Messenger with the given verbosity settings.
     *
     * @param verbose the verbosity settings
     */
    public Messenger(Verbose verbose) {
        this.verbose = verbose;
    }
    
    /**
     * Creates a new Messenger with the default verbosity settings.
     */
    public Messenger() {
        this.verbose = new Verbose();
    }

    
    /**
     * Prints a message to standard error if the verbosity settings allow
     * messages of that type.
     *
     * @param msgType the type of the message
     * @param msg the message
     */
    public void print(MsgType msgType, String msg) {
        String template = null;
        switch (msgType) {
            case INFO: if (verbose.isInfo()) template = "Jacksum: Info: %s\n";
                       break;
            case WARNING: 
                      if (verbose.isWarnings()) template = "Jacksum: Warning: %s\n";
                      break;
            case ERROR:
                      if (verbose.isErrors()) template = "Jacksum: Error: %s\n";
                      break;
        }
        if (template != null) {
            System.err.printf(template, msg);
        }
                
    }
    

    /**
     * Returns the verbosity settings.
     *
     * @return the verbose
     */
    public Verbose getVerbose() {
        return verbose;
    }

    /**
     * Sets the verbosity settings.
     *
     * @param verbose the verbose to set
     */
    public void setVerbose(Verbose verbose) {
        this.verbose = verbose;
    }

}
