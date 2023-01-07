/*

  Sugar for Java 1.6.0
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
/*
  EncodingException.java
 */
package net.loefflmann.sugar.encodings;

/**
 * Thrown to indicate that the application has attempted to specify a non-
 * supported encoding.
 */
public class EncodingException extends IllegalArgumentException {

    /**
     * Constructs a <code>EncodingException</code> with no detail message.
     */
    public EncodingException() {
        super();
    }

    /**
     * Constructs a <code>EncodingException</code> with the specified detail
     * message.
     *
     * @param s the detail message.
     */
    public EncodingException(String s) {
        super(s);
    }

}
