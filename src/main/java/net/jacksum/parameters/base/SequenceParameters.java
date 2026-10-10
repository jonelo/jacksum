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
package net.jacksum.parameters.base;

import net.jacksum.parameters.Sequence;

/**
 * The parameters for a sequence (e.g. a string or hex sequence) that should be processed
 * instead of files.
 */
public interface SequenceParameters {

    /**
     * Tells whether a sequence has been set.
     *
     * @return true if a sequence has been set
     */
    boolean isSequence();

    /**
     * Gets the sequence.
     *
     * @return the sequence, or null if none has been set
     */
    Sequence getSequence();

    /**
     * Sets the sequence.
     *
     * @param sequence the sequence
     */
    void setSequence(Sequence sequence);
}
