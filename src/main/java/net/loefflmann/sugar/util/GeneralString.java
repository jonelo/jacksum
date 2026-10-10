/*

 Sugar for Java 1.6.0
 Copyright (c) 2001-2026  Dipl.-Inf. (FH) Johann N. Löfflmann,
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

/**
 * Helper methods for strings.
 */
public class GeneralString {
    
    private static final String specialChars = "=: \t\r\n\f#!";

    /**
     * Converts a number to a string that is right-justified by leading blanks.
     *
     * @param number the number
     * @param blanks the minimum length of the result
     * @return the number padded with leading blanks
     */
    public static String right(long number, int blanks) {
        StringBuilder sb = new StringBuilder(Long.toString(number));
        while (sb.length() < blanks) {
            sb.insert(0, ' ');
        }
        return sb.toString();
    }

    /**
     * Formats a number by a {@code DecimalFormat} pattern.
     *
     * @param number the number
     * @param mask the {@code DecimalFormat} pattern
     * @return the formatted number
     */
    public static String decformat(long number, String mask) {
        DecimalFormat df = new DecimalFormat(mask);
        return df.format(number);
    }

    /**
     * Groups a hex string by inserting a group character after every
     * {@code group} bytes (i.e. 2 * {@code group} hex digits).
     *
     * @param sb the hex string
     * @param group the number of bytes per group
     * @param groupChar the character that separates the groups
     * @return a new buffer with the grouped hex string, or sb itself if the
     * string does not contain more than {@code group} bytes
     */
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
    
    /**
     * Removes all occurrences of a string.
     *
     * @param source the source string
     * @param oldString the string to remove
     * @return the source string without any occurrence of oldString
     */
    public static String removeAllStrings(String source, String oldString) {
        return replaceAllStrings(source, oldString, "");
    }
    
    /**
     * Removes all occurrences of a string in place.
     *
     * @param source the source, it will be modified
     * @param oldString the string to remove
     */
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
     * Translates the escape sequences \t, \n, \r, \", \' and \\ to the character they
     * stand for. Any other sequence that starts with a backslash stays unchanged.
     *
     * @param s a String, possibly with escape sequences.
     * @return a String with all escape sequences translated.
     */
    public static String translateEscapeSequences(String s) {
        return translateEscapeSequences(s, false);
    }

    /**
     * Translates the escape sequences \t, \n, \r, \", \' and \\ to the character they
     * stand for, and optionally also \xHH to the character with the hexadecimal value HH,
     * where HH can become 00 to 7F, and the two hex digits can be written in both upper
     * and lower case. Any other sequence that starts with a backslash stays unchanged,
     * this is also true for an incomplete \xHH sequence and for \x80 to \xFF.
     *
     * The String is processed in one pass from left to right, so a translated character is
     * never interpreted as the start of another escape sequence, e.g. \\n stays a
     * backslash followed by an "n", and \\x41 stays a backslash followed by "x41".
     *
     * @param s a String, possibly with escape sequences.
     * @param hexEscapes if true, \xHH is translated as well.
     * @return a String with all escape sequences translated.
     */
    public static String translateEscapeSequences(String s, boolean hexEscapes) {
        if (s == null) {
            return null;
        }
        // https://en.wikipedia.org/wiki/Escape_sequences_in_C
        // https://docs.oracle.com/javase/tutorial/java/data/characters.html
        int length = s.length();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            if (c != '\\' || i + 1 == length) { // a normal char, or a trailing backslash
                sb.append(c);
                continue;
            }
            char next = s.charAt(++i);
            switch (next) {
                case 't': sb.append('\t'); break;  // 0x09
                case 'n': sb.append('\n'); break;  // 0x0A
                case 'r': sb.append('\r'); break;  // 0x0D
                case '"': sb.append('"'); break;   // 0x22
                case '\'': sb.append('\''); break; // 0x27
                case '\\': sb.append('\\'); break; // 0x5C
                case 'x': {
                    // \xHH, HH can become 00 to 7F, both upper and lower case
                    int high, low;
                    if (hexEscapes
                            && i + 2 < length
                            && (high = hexDigit(s.charAt(i + 1))) >= 0
                            && (low = hexDigit(s.charAt(i + 2))) >= 0
                            && (high << 4 | low) < 128) {
                        sb.append((char) (high << 4 | low));
                        i += 2;
                    } else { // not a valid \xHH sequence, keep it unchanged
                        sb.append(c).append(next);
                    }
                    break;
                }
                default: // an unknown escape sequence, keep it unchanged
                    sb.append(c).append(next);
            }
        }
        return sb.toString();
    }

    /**
     * Returns the value of a hexadecimal digit. In contrast to Character.digit() only
     * the ASCII characters 0-9, a-f and A-F are accepted.
     *
     * @param c a character.
     * @return the value of the hexadecimal digit (0 to 15), or -1 if c is not a
     *         hexadecimal digit.
     */
    private static int hexDigit(char c) {
        if (c >= '0' && c <= '9') return c - '0';
        if (c >= 'a' && c <= 'f') return c - 'a' + 10;
        if (c >= 'A' && c <= 'F') return c - 'A' + 10;
        return -1;
    }
    
    
    /**
     * Removes all chars c in String s.
     *
     * @param s a String.
     * @param c the char to remove.
     * @return s without any occurrence of c.
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
    
   /**
    * Replaces one char in String s at a given position pos by c.
    *
    * @param s a String.
    * @param pos the position.
    * @param c the new char.
    * @return a String where the char at pos has been replaced by c.
    */
    public static String replaceChar(String s, int pos, char c) {
        StringBuilder sb = new StringBuilder(s);
        sb.setCharAt(pos, c);
        return sb.toString();
    }
    
    /**
     * Counts the occurrences of a char in a string.
     *
     * @param s a String.
     * @param c the char to count.
     * @return the number of occurrences of c in s.
     */
    public static int countChar(String s, char c) {
        int count=0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i)==c) count++;
        }
        return count;
    }
    
    /**
     * Formats the pattern s by {@code MessageFormat} with a char as argument.
     *
     * @param s the pattern, e.g. {@code "value: {0}"}
     * @param c the char that replaces {@code {0}}
     * @return the formatted message
     */
    public static String message(String s, char c) {
        Character character = c;
        Object aobj[] = {
            character.toString()
        };
        return MessageFormat.format(s, aobj);
    }
    
    /**
     * Formats the pattern s by {@code MessageFormat} with an int as argument.
     *
     * @param s the pattern, e.g. {@code "value: {0}"}
     * @param i the int that replaces {@code {0}}
     * @return the formatted message
     */
    public static String message(String s, int i) {
        Integer integer = i;
        Object aobj[] = {
            integer.toString()
        };
        return MessageFormat.format(s, aobj);
    }
    
    /**
     * Formats the pattern s by {@code MessageFormat} with two int arguments.
     *
     * @param s the pattern, e.g. {@code "value: {0}"}
     * @param i1 the int that replaces {@code {0}}
     * @param i2 the int that replaces {@code {1}}
     * @return the formatted message
     */
    public static String message(String s, int i1, int i2) {
        Integer integer = i1;
        Integer integer2 = i2;
        Object aobj[] = {
            integer.toString(),
            integer2.toString()
        };
        return MessageFormat.format(s, aobj);
    }
    
    
    /**
     * Formats the pattern s by {@code MessageFormat} with a String as argument.
     *
     * @param s the pattern, e.g. {@code "value: {0}"}
     * @param s1 the String that replaces {@code {0}}
     * @return the formatted message
     */
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
    
    
    
    /**
     * Converts unicodes to encoded &#92;uxxxx. Control characters such as
     * newline and tab and the chars {@code =: #!} are escaped by a backslash.
     *
     * @param string a String.
     * @return an encoded String.
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
    
    /**
     * Splits a string at each occurrence of a delimiter. An empty trailing
     * token is not included.
     *
     * @param str the string to split
     * @param delimiter the delimiter, not interpreted as a regular expression
     * @return the tokens
     */
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

    /**
     * Removes trailing whitespace, i.e. whitespace on the right side.
     *
     * @param string a String.
     * @return the String without trailing whitespace.
     */
    public static String trimRight(String string) {
        return string.replaceAll("\\s+$", "");
    }

    /**
     * Removes leading whitespace, i.e. whitespace on the left side.
     *
     * @param string a String.
     * @return the String without leading whitespace.
     */
    public static String trimLeft(String string) {
        return string.replaceAll("^\\s+", "");
    }

    /**
     * Parses a boolean value. Supported are yes, on, true, 1 and enabled for
     * true, and no, off, false, 0 and disabled for false.
     *
     * @param arg the value to parse (case-sensitive)
     * @return the boolean value
     * @throws IllegalArgumentException if arg does not represent a boolean
     */
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
                    ("The value \"%s\" does not represent a boolean. The supported values are yes, on, true, 1, enabled, and no, off, false, 0, disabled.", arg));
        }
    }

}
