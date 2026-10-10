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
package net.jacksum.formats;

import java.io.File;
import java.nio.file.Path;

import net.jacksum.parameters.combined.FormatParameters;

/**
 * Holds the preferences that control how a hash value and its file information are
 * formatted (separator, encoding, grouping, timestamp, file size, path handling).
 *
 * @author johann
 */
public class FormatPreferences implements FormatParameters {

    // format stuff
    private String separator;
    private Encoding encoding;
    private int grouping; // grouping of hex digits
    private Character groupChar;
    private String timestampFormat;
    private Character pathChar;
    
    private boolean filesizeWanted = false;
    private boolean timestampWanted = false;
    //private boolean filenameWanted = true;
    
    private long filesizeAsByteBlocks = -1;
    private String filesizeWithPrintfFormatted = null;

    private boolean noPath = false;
    private Path pathRelativeTo = null;
    private boolean gnuEscaping = false;
    private boolean gnuEscapingSet = false;

    /**
     * Creates new FormatPreferences with default values.
     */
    public FormatPreferences() {
        setDefaults();
    }
    
    private void setDefaults() {
        separator = " ";
        encoding = Encoding.HEX;
        timestampFormat = null;
        grouping = 0;
        groupChar = ' ';
        setPathChar(File.separatorChar);
        setNoPath(false);
        setPathRelativeTo(null);
        setGnuEscaping(false);
        gnuEscapingSet = false;
    }

    /**
     * Overwrites these preferences with the values that have been set in the specified
     * parameters. The GNU filename escaping is always adopted.
     *
     * @param parameters the parameters that overwrite the preferences
     */
    public void overwritePreferences(FormatParameters parameters) {
        setTimestampWanted(parameters.isTimestampWanted());

        if (parameters.isSeparatorSet()) {
            setSeparator(parameters.getSeparator());
        }
        if (parameters.isEncodingSet()) {
            setHashEncoding(parameters.getEncoding());
        }
        if (parameters.isGroupingSet()) {
            setGrouping(parameters.getGrouping());
        }
        if (parameters.isGroupCharSet()) {
            setGroupChar(parameters.getGroupChar());
        }
        if (parameters.isFilesizeWantedSet()) {
            setFilesizeWanted(parameters.isFilesizeWanted());
        }
        if (parameters.isTimestampWanted()) {
            setTimestampFormat(parameters.getTimestampFormat());
        }
        if (parameters.isPathCharSet()) {
            setPathChar(parameters.getPathChar());
        }
        if (parameters.isNoPath()) {
            setNoPath(parameters.isNoPath());
        }
        if (parameters.getPathRelativeTo() != null) {
            setPathRelativeTo(parameters.getPathRelativeTo());
        }
        // The value is always the effective one, no matter whether it comes from the platform
        // default, from a style (formatter.gnuescaping.enabled), or from the option
        // --gnu-filename-escaping, so it is always adopted. Assign the fields directly rather
        // than calling setGnuEscaping(), because that setter would mark the value as being set
        // by the user; that flag stays reserved for an explicit choice of the user.
        gnuEscaping = parameters.isGnuEscaping();
        gnuEscapingSet = parameters.isGnuEscapingSetByUser();
    }


    /**
     * Gets the separator between the fields of an output line.
     *
     * @return the separator
     */
    @Override
    public String getSeparator() {
        return separator;
    }

    /**
     * Sets the separator between the fields of an output line.
     *
     * @param separator the separator to set
     */
    public void setSeparator(String separator) {
        this.separator = separator;
    }

    /**
     * Gets the encoding of the checksum.
     *
     * @return the encoding
     */
    @Override
    public Encoding getEncoding() {
        return encoding;
    }

    /**
     * Sets the encoding of the hash value.
     *
     * @param encoding the encoding to set
     */
    public void setHashEncoding(Encoding encoding) {
        this.encoding = encoding;
    }

    /**
     * Gets the number of groups (make sense only if encoding has been set to
     * HEX or HEXUP).
     *
     * @return the grouping
     */
    @Override
    public int getGrouping() {
        return grouping;
    }

    /**
     * Sets the number of groups (make sense only if encoding is HEX or HEXUP).
     *
     * @param grouping the grouping to set
     */
    public void setGrouping(int grouping) {
        this.grouping = grouping;
    }

    /**
     * Returns true if groups are wanted (make sense only if encoding is HEX or
     * HEXUP).
     *
     * @return true if the user wanted a grouping.
     */
    public boolean isGroupWanted() {
        return getGrouping() > 0;
    }

    /**
     * Gets the group char (works only if encoding is HEX or HEXUP).
     *
     * @return the groupChar.
     */
    @Override
    public Character getGroupChar() {
        return groupChar;
    }

    /**
     * Sets the group char (make sense only if encoding is HEX or HEXUP).
     *
     * @param groupChar the groupChar to set
     */
    public void setGroupChar(Character groupChar) {
        this.groupChar = groupChar;
    }

    /**
     * Gets the format of the timestamp.
     *
     * @return the timestampFormat
     */
    @Override
    public String getTimestampFormat() {
        return timestampFormat;
    }

    /**
     * Sets the timestampFormat to force a timestamp output
     *
     * @param timestampFormat the timestampFormat to set
     */
    public void setTimestampFormat(String timestampFormat) {
        this.timestampFormat = timestampFormat;
    }

    @Override
    public boolean isSeparatorSet() {
        return separator != null;
    }

    @Override
    public boolean isEncodingSet() {
        return encoding != null;
    }

    @Override
    public boolean isGroupingSet() {
        return grouping > 0;
    }

    @Override
    public boolean isGroupCharSet() {
        return groupChar != null;
    }

    @Override
    public Character getPathChar() {
        return pathChar;
    }

    @Override
    public boolean isTimestampWanted() {
        return timestampWanted;
    }
    
    /**
     * Sets whether a timestamp is wanted in the output.
     *
     * @param timestampWanted the timestampWanted to set
     */
    public void setTimestampWanted(boolean timestampWanted) {
        this.timestampWanted = timestampWanted;
    }

    /**
     * Tells whether the file size is wanted in the output.
     *
     * @return the filesizeWanted
     */
    public boolean isFilesizeWanted() {
        return filesizeWanted;
    }

    /**
     * Sets whether the file size is wanted in the output.
     *
     * @param filesizeWanted the filesizeWanted to set
     */
    public void setFilesizeWanted(boolean filesizeWanted) {
        this.filesizeWanted = filesizeWanted;
    }


    @Override
    public boolean isPathCharSet() {
        return !pathChar.equals(File.separatorChar);
    }

    /**
     * Sets the character that separates the elements of a path in the output.
     *
     * @param pathChar the pathChar to set
     */
    public void setPathChar(Character pathChar) {
        this.pathChar = pathChar;
    }


    @Override
    public boolean isNoPath() {
        return noPath;
    }

    /**
     * Sets whether the path should be omitted from the file name in the output.
     *
     * @param noPath true if the path should be omitted
     */
    public void setNoPath(boolean noPath) {
        this.noPath = noPath;
    }

    /**
     * Gets the size of the blocks in which the file size is expressed.
     *
     * @return the filesizeAsByteBlocks
     */
    @Override
    public long getFilesizeAsByteBlocks() {
        return filesizeAsByteBlocks;
    }

    /**
     * Sets the size of the blocks in which the file size is expressed.
     *
     * @param filesizeAsByteBlocks the filesizeAsByteBlocks to set
     */
    public void setSizeAsByteBlocks(long filesizeAsByteBlocks) {
        this.filesizeAsByteBlocks = filesizeAsByteBlocks;
    }

    /**
     * Gets the printf format for the file size.
     *
     * @return the filesizeWithPrintfFormatted
     */
    @Override
    public String getFilesizeWithPrintfFormatted() {
        return filesizeWithPrintfFormatted;
    }

    @Override
    public boolean isFilesizeWantedSet() {
        return true;
    }


    /**
     * Sets the printf format for the file size.
     *
     * @param filesizeWithPrintfFormatted the filesizeWithPrintfFormatted to set
     */
    public void setSizeWithPrintfFormatted(String filesizeWithPrintfFormatted) {
        this.filesizeWithPrintfFormatted = filesizeWithPrintfFormatted;
    }

    @Override
    public Path getPathRelativeTo() {
        return pathRelativeTo;
    }

    /**
     * Sets the path that file paths in the output are relative to.
     *
     * @param pathRelativeTo the path, or null for no relative paths
     */
    public void setPathRelativeTo(Path pathRelativeTo) {
        this.pathRelativeTo = pathRelativeTo;
    }

    @Override
    public boolean isGnuEscaping() {
        return gnuEscaping;
    }

    @Override
    public boolean isGnuEscapingSetByUser() {
        return gnuEscapingSet;
    }

    /**
     * Sets whether file names are escaped in the GNU style, and marks the value as being
     * set by the user.
     *
     * @param gnuEscaping true if file names should be escaped in the GNU style
     */
    public void setGnuEscaping(boolean gnuEscaping) {
        this.gnuEscaping = gnuEscaping;
        this.gnuEscapingSet = true;
    }
}
