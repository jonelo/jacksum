/*


  Jacksum 3.5.0 - a checksum utility in Java
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

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import net.jacksum.multicore.OSControl;
import net.loefflmann.sugar.util.GeneralString;
import net.jacksum.algorithms.AbstractChecksum;

public class Formatter {

    private final static String EMPTY_STRING = "";

    private FormatPreferences formatPreferences;
    private LineFormatter lineFormatter;
    private FingerprintFormatter fingerprintFormatter;
    private FilenameFormatter sharedFilenameFormatter;
    private SizeFormatter sizeFormatter;
    private TimestampFormatter timestampFormatter;
    
  
    
    public Formatter(FormatPreferences formatPreferences) {

        this.formatPreferences = formatPreferences;
        lineFormatter = new LineFormatter(formatPreferences);
        fingerprintFormatter = new FingerprintFormatter(formatPreferences);
        if (formatPreferences.isFilesizeWanted()) {
            sizeFormatter = new SizeFormatter(formatPreferences);
        }
        if (formatPreferences.isTimestampWanted()) {
            timestampFormatter = new TimestampFormatter(formatPreferences);
        }
        sharedFilenameFormatter = new FilenameFormatter(formatPreferences);
    }

    
    public String format(AbstractChecksum checksum) {
        String separator = lineFormatter.getParameters().getSeparator();
        String fingerprint = fingerprintFormatter.format(checksum.getByteArray());

        // We need to check whether the file name has a problematic character in it (in other words, whether the
        // problematic characters were replaced by calling fileformatter's format() method).
        // To check that, we need to use a temp. non-shared fileformatter instance. And only in the case that GNU escaping
        // have been performed successfully (there was at least one problematic character), we need to flag that fact
        // in the output with a leading backslash (see filenameContainedProblematicChars).
        FilenameFormatter nonSharedFilenameFormatter = checksum.getFormatPreferences().isGnuEscaping() ?
                new FilenameFormatter(formatPreferences) : sharedFilenameFormatter;
        String filenameFormatted = null;
        if (checksum.getFilename() != null) {
            filenameFormatted = nonSharedFilenameFormatter.format(checksum.getFilename());
        }
        boolean filenameContainedProblematicChars = nonSharedFilenameFormatter.didTheFormatMethodChangeProblematicChars();
        
        return String.format("%s%s%s%s%s",
                fingerprint.length() > 0 && filenameContainedProblematicChars ?
                        "\\" : EMPTY_STRING,

                fingerprint.length() > 0 ?
                        fingerprint : EMPTY_STRING,

                sizeFormatter != null ?
                        (fingerprint.length() > 0 ? separator : EMPTY_STRING) + sizeFormatter.format(checksum.getLength()) : EMPTY_STRING,
                
                timestampFormatter != null && timestampFormatter.getParameters().isTimestampWanted() ?  
                        separator + timestampFormatter.format(checksum.getTimestamp()) : EMPTY_STRING,
                
                checksum.getFilename() != null ?
                        separator + filenameFormatted : EMPTY_STRING);
    }

    
    private static void _replaceFingerprintTokens(StringBuilder buffer, AbstractChecksum abstractChecksum) {
        GeneralString.replaceAllStrings(buffer, "#CHECKSUM{i}", "#CHECKSUM");
        GeneralString.replaceAllStrings(buffer, "#CHECKSUM{0}", "#CHECKSUM");
        GeneralString.replaceAllStrings(buffer, "#CHECKSUM{" + abstractChecksum.getName() + "}", "#CHECKSUM" );
        FingerprintFormatter.resolveEncoding(buffer, abstractChecksum, "(#CHECKSUM\\{i,([^}]+)\\})");
        FingerprintFormatter.resolveEncoding(buffer, abstractChecksum, "(#CHECKSUM\\{0,([^}]+)\\})");
        FingerprintFormatter.resolveEncoding(buffer, abstractChecksum, "(#CHECKSUM\\{"+abstractChecksum.getName()+",([^}]+)\\})");
        FingerprintFormatter.resolveEncoding(buffer, abstractChecksum, "(#CHECKSUM\\{([^}]+)\\})");
        GeneralString.replaceAllStrings(buffer, "#CHECKSUM", abstractChecksum.getValueFormatted());
    }
    
    private static void _replaceAlgorithmTokens(StringBuilder buffer, AbstractChecksum abstractChecksum) {
        // algorithm names        
        GeneralString.replaceAllStrings(buffer, "#ALGONAME{i}", "#ALGONAME");
        GeneralString.replaceAllStrings(buffer, "#ALGONAME{i,uppercase}", "#ALGONAME{uppercase}");
        GeneralString.replaceAllStrings(buffer, "#ALGONAME{i,lowercase}", "#ALGONAME{lowercase}");
        GeneralString.replaceAllStrings(buffer, "#ALGONAME{0}", "#ALGONAME");
        GeneralString.replaceAllStrings(buffer, "#ALGONAME{0,uppercase}", "#ALGONAME{uppercase}");
        GeneralString.replaceAllStrings(buffer, "#ALGONAME{0,lowercase}", "#ALGONAME{lowercase}");
        GeneralString.replaceAllStrings(buffer, "#ALGONAME{uppercase}", abstractChecksum.getName().toUpperCase(Locale.US));
        GeneralString.replaceAllStrings(buffer, "#ALGONAME{lowercase}", abstractChecksum.getName().toLowerCase(Locale.US));
        GeneralString.replaceAllStrings(buffer, "#ALGONAME", abstractChecksum.getName());
    }
    
    private static void _replaceSequenceTokens(StringBuilder buffer, AbstractChecksum abstractChecksum, byte[] sequence) {
        // replace all "#SEQUENCE{<encoding>}"
        SequenceFormatter.resolveEncoding(buffer, sequence, abstractChecksum.getFormatPreferences().getGrouping(),
                abstractChecksum.getFormatPreferences().getGroupChar(), "(#SEQUENCE\\{([^}]+)\\})");

        // just in case we have only a "#SEQUENCE", replace it with "#SEQUENCE{<encodig>}"
        GeneralString.replaceAllStrings(buffer, "#SEQUENCE",
                "#SEQUENCE{" + Encoding.encoding2String(abstractChecksum.getFormatPreferences().getEncoding()) + "}");

        // and replace all "#SEQUENCE{<encoding>}" again
        SequenceFormatter.resolveEncoding(buffer,
                sequence, abstractChecksum.getFormatPreferences().getGrouping(),
                abstractChecksum.getFormatPreferences().getGroupChar(), "(#SEQUENCE\\{([^}]+)\\})");        
    }
    
    private static void _replaceFilesizeToken(StringBuilder buffer, AbstractChecksum abstractChecksum) {
        GeneralString.replaceAllStrings(buffer, "#FILESIZE", Long.toString(abstractChecksum.getLength()));        
    }
    
    private static void _replaceFilenameTokens(StringBuilder buffer, AbstractChecksum abstractChecksum) {

        FilenameFormatter filenameFormatter = new FilenameFormatter(abstractChecksum.getFormatPreferences());
        String formattedFilename = filenameFormatter.format(abstractChecksum.getFilename());

        boolean escape = abstractChecksum.getFormatPreferences().isGnuEscaping();
        boolean escaped = filenameFormatter.didTheFormatMethodChangeProblematicChars();
        GeneralString.replaceAllStrings(buffer, "#ESCAPETAG", escape && escaped ? "\\" : "");

        if (buffer.toString().contains("#FILENAME{")) {

            String name = null;
            String directory = null;
            try {
                name = Paths.get(abstractChecksum.getFilename()).getFileName().toString();
                Path parent = Paths.get(abstractChecksum.getFilename()).getParent();
                if (parent == null) {
                    directory = Paths.get("./").toString();
                } else {
                    directory = Paths.get(abstractChecksum.getFilename()).getParent().toString();
                }
            } catch (InvalidPathException ipe) {
                name = formattedFilename;
            } catch (Exception e) {
                e.printStackTrace();
            }

            GeneralString.replaceAllStrings(buffer, "#FILENAME{name}",
                    escape ? FilenameFormatter.gnuEscapeProblematicCharsInFilename(name) : name);
            if (directory == null) directory = "";
            GeneralString.replaceAllStrings(buffer, "#FILENAME{path}",
                    escape ? FilenameFormatter.gnuEscapeProblematicCharsInFilename(directory) : directory);
        }
        GeneralString.replaceAllStrings(buffer, "#FILENAME", formattedFilename);

/*
        if (buffer.toString().contains("#FILENAME{")) {
            File filetemp = new File(abstractChecksum.getFilename());
            GeneralString.replaceAllStrings(buffer, "#FILENAME{name}", filetemp.getName());
            String parent = filetemp.getParent();
            if (parent == null) {
                parent = "";
            } else if (!parent.endsWith(File.separator)
                    && // for files on a different drive where the working dir has changed
                    (!parent.endsWith(":") && System.getProperty("os.name").toLowerCase().startsWith("windows"))) {
                parent += File.separator;
            }
            if (abstractChecksum.getFormatPreferences().isPathCharSet()) {
                GeneralString.replaceAllStrings(buffer, "#FILENAME{path}", parent.replace(File.separatorChar, abstractChecksum.getFormatPreferences().getPathChar()));
            } else {
                GeneralString.replaceAllStrings(buffer, "#FILENAME{path}", parent);
            }
        }
        
        if (abstractChecksum.getFormatPreferences().isPathCharSet()) {
            GeneralString.replaceAllStrings(buffer, "#FILENAME", abstractChecksum.getFilename().replace(File.separatorChar, abstractChecksum.getFormatPreferences().getPathChar()));
        } else {            
            GeneralString.replaceAllStrings(buffer, "#FILENAME", abstractChecksum.getFilename());
        }
        */
    }

    private static void _replaceTimestampToken(StringBuilder buffer, AbstractChecksum abstractChecksum) {
        // timestamp
        if (abstractChecksum.isTimestampWanted()) {
            GeneralString.replaceAllStrings(buffer, "#TIMESTAMP", abstractChecksum.getTimestampFormatted());
        }
    }
    
    private static void _replaceSpecialCharTokens(StringBuilder buffer, AbstractChecksum abstractChecksum) {
        // special chars: separator
        GeneralString.replaceAllStrings(buffer, "#SEPARATOR", abstractChecksum.getFormatPreferences().getSeparator());
        
        // special chars: quotes
        GeneralString.replaceAllStrings(buffer, "#QUOTE", "\"");        
    }

    private static void _replaceBintagToken(StringBuilder buffer, AbstractChecksum abstractChecksum) {
        GeneralString.replaceAllStrings(buffer, "#BINTAG", OSControl.isWindows() ? "*": " ");
    }
    
    public static String format(StringBuilder buffer, AbstractChecksum abstractChecksum, byte[] sequence) {
        _replaceBintagToken(buffer, abstractChecksum);
        _replaceFingerprintTokens(buffer, abstractChecksum);
        _replaceAlgorithmTokens(buffer, abstractChecksum);
        _replaceSequenceTokens(buffer, abstractChecksum, sequence);
        _replaceFilesizeToken(buffer, abstractChecksum);
        _replaceFilenameTokens(buffer, abstractChecksum);
        _replaceTimestampToken(buffer, abstractChecksum);
        _replaceSpecialCharTokens(buffer, abstractChecksum);       
        return buffer.toString();
    }

    public static void replaceAliases(StringBuilder format) {
        FingerprintFormatter.replaceAliases(format);
        SizeFormatter.replaceAliases(format);
    }

    public FingerprintFormatter getFingerprintFormatter() {
        return fingerprintFormatter;
    }

    public void setFingerprintFormatter(FingerprintFormatter fingerprintFormatter) {
        this.fingerprintFormatter = fingerprintFormatter;
    }

    public SizeFormatter getSizeFormatter() {
        return sizeFormatter;
    }

    public void setSizeFormatter(SizeFormatter sizeFormatter) {
        this.sizeFormatter = sizeFormatter;
    }

    public TimestampFormatter getTimestampFormatter() {
        return timestampFormatter;
    }

    public void setTimestampFormatter(TimestampFormatter timestampFormatter) {
        this.timestampFormatter = timestampFormatter;
    }

    public FilenameFormatter getFilenameFormatter() {
        return sharedFilenameFormatter;
    }

    public void setFilenameFormatter(FilenameFormatter filenameFormatter) {
        this.sharedFilenameFormatter = filenameFormatter;
    }

    /**
     * @return the lineFormatter
     */
    public LineFormatter getLineFormatter() {
        return lineFormatter;
    }

    /**
     * @param lineFormatter the lineFormatter to set
     */
    public void setLineFormatter(LineFormatter lineFormatter) {
        this.lineFormatter = lineFormatter;
    }
}
