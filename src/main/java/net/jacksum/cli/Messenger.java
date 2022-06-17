/*


  Jacksum 3.4.0 - a checksum utility in Java
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
package net.jacksum.cli;


import java.io.Serializable;

public class Messenger implements Serializable {

    private static final long serialVersionUID = 6273013117713291439L;

    public enum MsgType {
        INFO, WARNING, ERROR
    }

    private Verbose verbose;

    public Messenger(Verbose verbose) {
        this.verbose = verbose;
    }
    
    public Messenger() {
        this.verbose = new Verbose();
    }

    
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
     * @return the verbose
     */
    public Verbose getVerbose() {
        return verbose;
    }

    /**
     * @param verbose the verbose to set
     */
    public void setVerbose(Verbose verbose) {
        this.verbose = verbose;
    }

}
