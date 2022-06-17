/*

 Sugar for Java 1.6.0
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
  *
  *
  * 01-May-2002: initial release
  *
  * 06-Jul-2002: bug fixed (replaceString and replaceAllString do not work if
  *              oldString starts at pos 0)
  *
  * 09-Mar-2003: bug fixed (endless loop in replaceAllStrings, if oldString is
  *              part of newString), testcases:
  *              replaceAllStrings("aaa","a","abc") => abcabcabc
  *              replaceAllStrings("abbbabbbabbbb","bb","") => ababa
  *              replaceAllStrings("aaa","","b") => bababa
  *              new method: removeAllStrings()
  *
  * 08-May-2004: added encodeUnicode() and decodeEncodedUnicode()
  *
  * 26-May-2005: split

 */
package net.loefflmann.sugar.util;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class GeneralString {
    
    private static final String specialChars = "=: \t\r\n\f#!";

    public static String right(long number, int blanks) {
        StringBuilder sb = new StringBuilder(Long.toString(number));
        while (sb.length() < blanks) {
            sb.insert(0, ' ');
        }
        return sb.toString();
    }

    public static String decformat(long number, String mask) {
        DecimalFormat df = new DecimalFormat(mask);
        return df.format(number);
    }

    public static StringBuffer insertBlanks(StringBuffer sb, int group, char groupChar) {
        int bytecount = sb.length() / 2; // we expect a hex string
        if (bytecount <= group) {
            return sb; // avoid unnecessary action
        }
        StringBuffer sb2 = new StringBuffer(sb.length() + (bytecount / group - 1));
        int group2 = group * 2;
        for (int i = 0; i < sb.length(); i++) {
            if ((i > 0) && (i % (group2)) == 0) {
                sb2.append(groupChar);
            }
            sb2.append(sb.charAt(i));
        }
        return sb2;
    }
    
    /** Creates new GeneralString */
    public GeneralString() {
    }
    
    /**
     * Tokenizes a string, separated by whitespace chars, tokens with
     * whitespace chars have to be quoted.
     * Example: tokenize("abc.txt \"def ghi.txt\" jkl.txt");
     * @param input a String.
     * @return a list of Strings.
     */
    public static List<String> tokenize(String input) {
        List<String> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean quotedString = false;
        
        for (int i=0; i < input.length(); i++) {
            switch (input.charAt(i)) {
                case ' ':
                case '\t':  if (quotedString) {
                                sb.append(input.charAt(i));
                            } else {
                                if (sb.toString().length() > 0) {
                                   list.add(sb.toString());
                                   sb = new StringBuilder();                              
                                }
                            }
                            break;

                case '"':   quotedString = !quotedString;
                            break;

                default:    sb.append(input.charAt(i));
                            break;
            }
        }
        if (sb.toString().length() > 0) {
            list.add(sb.toString());
        }

        return list;
    }
    
    /**
     * Replaces none or only one String oldString to newString in String source.
     * @param source a String.
     * @param oldString the old String.
     * @param newString the new String.
     * @return a String.
     */
    public static String replaceString(String source, String oldString, String newString) {
        
        if (source == null || oldString == null) return source;
        if (newString == null) newString="";
        int pos = source.indexOf(oldString);
        if (pos > -1) {
            StringBuilder sb = new StringBuilder();
            sb.append(source.substring(0,pos));
            sb.append(newString);
            sb.append(source.substring(pos+oldString.length()));            
            return sb.toString();
        } else
            return source;
    }
    
    /**
     * Replaces all oldStrings found within source by newString.
     * @param source a String.
     * @param oldString the old String.
     * @param newString the new String.
     * @return a String.
     */
    public static String replaceAllStrings(String source, String oldString, String newString) {
        StringBuilder buffer = new StringBuilder(source);
        replaceAllStrings(buffer, oldString, newString);
        return buffer.toString();        
    }
    
    /**
     * Replaces all occurrencies of oldString with newString in source
     * @param source the StringBuilder that will be modified if oldString is found
     * @param oldString if oldString is null, nothing will happen; otherwise oldString will be searched in source
     * @param newString if newString is null, all occurencies of oldString will be removed in source
     */
    public static void replaceAllStrings(StringBuilder source, String oldString, String newString) {
        if (source==null || oldString==null) return;
        if (newString == null) {
            removeAllStrings(source, oldString);
            return;
        }
        int idx = source.length();
        int offset = oldString.length();

        while( ( idx=source.toString().lastIndexOf(oldString, idx-1) ) > -1 ) {
            source.replace(idx, idx+offset, newString);
        }
    }
    
    public static String removeAllStrings(String source, String oldString) {
        return replaceAllStrings(source, oldString, "");
    }
    
    public static void removeAllStrings(StringBuilder source, String oldString) {
        replaceAllStrings(source, oldString, "");
    }
    
    
    /**
     * Overwrites a string s at a given position with newString
     * @param s a String.
     * @param pos the position.
     * @param newString the new String that overwrites a String at position.
     * @return a String.
     */
    public static String replaceString(String s, int pos, String newString) {
        StringBuilder sb = new StringBuilder(s);
        for (int i=0; i < newString.length(); i++) {
            sb.setCharAt(pos+i, newString.charAt(i));
        }
        return sb.toString();
    }
    
    /**
     * Translates escape sequences.
     * @param s a String.
     * @return a String.
     */
    public static String translateEscapeSequences(String s) {
        String temp = s;
        temp=replaceAllStrings(temp, "\\t", "\t");  //  \t
        temp=replaceAllStrings(temp, "\\n", "\n");  //  \n
        temp=replaceAllStrings(temp, "\\r", "\r");  //  \r
        temp=replaceAllStrings(temp, "\\\"", "\""); //  \"
        temp=replaceAllStrings(temp, "\\\'", "\'"); //  \'
        temp=replaceAllStrings(temp, "\\\\", "\\"); //  \\
        return temp;
    }
    
    
    /*
     * Removes all chars c in String s
     */
    public static String removeChar(String s, char c) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i ++) {
            if (s.charAt(i) != c) sb.append(s.charAt(i)); // r += s.charAt(i);
        }
        return sb.toString();
    }
    
    /**
     * Removes one char at a given position.
     * @param s a String.
     * @param pos the position.
     * @return a String where a char has been removed at a given position.
     */
    public static String removeChar(String s, int pos) {
        StringBuilder buf = new StringBuilder( s.length() - 1 );
        buf.append(s.substring(0,pos)).append(s.substring(pos+1));
        return buf.toString();
    }
    
    
    /**
     * replaces all characters oldC in a String s with character newC
     * @param s a String.
     * @param oldC a char.
     * @param newC a char.
     * @return a String where all chars have been replaced.
     */
    public static String replaceChar(String s, char oldC, char newC) {
        StringBuilder sb = new StringBuilder(s);
        for (int i=0; i < s.length(); i++) {
            if (s.charAt(i) == oldC) sb.setCharAt(i,newC);
        }
        return sb.toString();
    }
    
   /*
    * replace one char c in String s at a given position pos
    */
    public static String replaceChar(String s, int pos, char c) {
        StringBuilder sb = new StringBuilder(s);
        sb.setCharAt(pos, c);
        return sb.toString();
    }
    
    public static int countChar(String s, char c) {
        int count=0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i)==c) count++;
        }
        return count;
    }
    
    public static String message(String s, char c) {
        Character character = c;
        Object aobj[] = {
            character.toString()
        };
        return MessageFormat.format(s, aobj);
    }
    
    public static String message(String s, int i) {
        Integer integer = i;
        Object aobj[] = {
            integer.toString()
        };
        return MessageFormat.format(s, aobj);
    }
    
    public static String message(String s, int i1, int i2) {
        Integer integer = i1;
        Integer integer2 = i2;
        Object aobj[] = {
            integer.toString(),
            integer2.toString()
        };
        return MessageFormat.format(s, aobj);
    }
    
    
    public static String message(String s, String s1) {
        Object aobj[] = {
            s1
        };
        return MessageFormat.format(s, aobj);
    }
    
    
    /**
     * Converts encoded &#92;uxxxx to unicode chars
     * @param string a String.
     * @return a decoded String.
     */
    public static String decodeEncodedUnicode(String string) {
        char c;
        int length = string.length();
        StringBuilder buffer = new StringBuilder(length);
        
        for(int x=0; x<length;) {
            c = string.charAt(x++);
            if (c == '\\') {
                c = string.charAt(x++);
                switch (c) {
                    case 'u': { int value=0;
                    for (int i=0; i<4; i++) {
                        c = string.charAt(x++);
                        if (c >= '0' && c <= '9')
                            value = (value << 4) + c - '0'; else
                                if (c >= 'a' && c <= 'f')
                                    value = (value << 4) + 10 + c - 'a'; else
                                        if (c >= 'A' && c <= 'F')
                                            value = (value << 4) + 10 + c - 'A'; else
                                                throw new IllegalArgumentException("Wrong \\uxxxx encoding");
                    }
                    buffer.append((char)value);
                    }
                    break;
                    case 'n': buffer.append('\n');
                    break;
                    case 't': buffer.append('\t');
                    break;
                    case 'r': buffer.append('\r');
                    break;
                    case 'f': buffer.append('\f');
                    break;
                    default:  buffer.append(c);
                    break;
                } // end-switch
            } else
                buffer.append(c);
        }
        return buffer.toString();
    }
    
    
    
    /*
     * Converts unicodes to encoded &#92;uxxxx
     */
    public static String encodeUnicode(String string) {
        int length = string.length();
        StringBuilder buffer = new StringBuilder(length*2);
        
        for(int x=0; x<length; x++) {
            char c = string.charAt(x);
            switch(c) {
                case ' ': buffer.append(' ');
                break;
                case '\\':buffer.append('\\');
                buffer.append('\\');
                break;
                case '\n':buffer.append('\\');
                buffer.append('n');
                break;
                case '\t':buffer.append('\\');
                buffer.append('t');
                break;
                case '\r':buffer.append('\\');
                buffer.append('r');
                break;
                case '\f':buffer.append('\\');
                buffer.append('f');
                break;
                default:
                    if ((c < 0x0020) || (c > 0x007e)) {
                        buffer.append('\\');
                        buffer.append('u');
                        buffer.append(ByteSequences.nibbleToHexChar((c >> 12) & 0xF));
                        buffer.append(ByteSequences.nibbleToHexChar((c >>  8) & 0xF));
                        buffer.append(ByteSequences.nibbleToHexChar((c >>  4) & 0xF));
                        buffer.append(ByteSequences.nibbleToHexChar( c        & 0xF));
                    } else {
                        if (specialChars.indexOf(c) != -1)
                            buffer.append('\\');
                        buffer.append(c);
                    }
            }
        }
        return buffer.toString();
    }
    
    public static String[] split(String str, String delimiter) {
        ArrayList<String> al = new ArrayList<>();
        int startpos=0;
        int found;
        do {
            found = str.substring(startpos).indexOf(delimiter);
            if (found > -1) {
                al.add(str.substring(startpos, startpos+found));
                startpos=startpos+found+delimiter.length();
            }
        } while (found > -1);
        if (startpos < str.length())
            al.add(str.substring(startpos));
        
        String[] s = new String[al.size()];
        for (int i=0; i < s.length; i++)
            s[i]=(String)al.get(i);
        
        return s;
    }

    /* remove leading whitespace */
    public static String trimRight(String string) {
        return string.replaceAll("^\\s+", "");
    }

    /* remove trailing whitespace */
    public static String trimLeft(String string) {
        return string.replaceAll("\\s+$", "");
    }

    public static boolean parseBoolean(String arg) throws IllegalArgumentException {
        switch (arg) {
            case "yes":
            case "on":
            case "true":
            case "1":
            case "enabled":
                return true;
            case "no":
            case "off":
            case "false":
            case "0":
            case "disabled":
                return false;
            default: throw new IllegalArgumentException(String.format
                    ("the value \"%s\" does not represent a boolean. The values yes, on, true, 1, enabled or no, off, false, 0, disabled are supported.", arg));
        }
    }

}
