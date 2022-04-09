/*


  Jacksum 3.2.0 - a checksum utility in Java
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
package net.jacksum.formats;

import java.io.File;
import java.nio.file.Path;

import net.jacksum.parameters.combined.FormatParameters;

/**
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
        if (parameters.isGnuEscapingSetByUser()) {
            setGnuEscaping(parameters.isGnuEscaping());
        }
    }


    /**
     * @return the separator
     */
    @Override
    public String getSeparator() {
        return separator;
    }

    /**
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
     * @param timestampWanted the timestampWanted to set
     */
    public void setTimestampWanted(boolean timestampWanted) {
        this.timestampWanted = timestampWanted;
    }

    /**
     * @return the filesizeWanted
     */
    public boolean isFilesizeWanted() {
        return filesizeWanted;
    }

    /**
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
     * @param pathChar the pathChar to set
     */
    public void setPathChar(Character pathChar) {
        this.pathChar = pathChar;
    }


    @Override
    public boolean isNoPath() {
        return noPath;
    }

    public void setNoPath(boolean noPath) {
        this.noPath = noPath;
    }

    /**
     * @return the filesizeAsByteBlocks
     */
    @Override
    public long getFilesizeAsByteBlocks() {
        return filesizeAsByteBlocks;
    }

    /**
     * @param filesizeAsByteBlocks the filesizeAsByteBlocks to set
     */
    public void setSizeAsByteBlocks(long filesizeAsByteBlocks) {
        this.filesizeAsByteBlocks = filesizeAsByteBlocks;
    }

    /**
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
     * @param filesizeWithPrintfFormatted the filesizeWithPrintfFormatted to set
     */
    public void setSizeWithPrintfFormatted(String filesizeWithPrintfFormatted) {
        this.filesizeWithPrintfFormatted = filesizeWithPrintfFormatted;
    }

    @Override
    public Path getPathRelativeTo() {
        return pathRelativeTo;
    }

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

    public void setGnuEscaping(boolean gnuEscaping) {
        this.gnuEscaping = gnuEscaping;
        this.gnuEscapingSet = true;
    }
}
