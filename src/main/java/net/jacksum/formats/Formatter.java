/*


  Jacksum 4.0.0 - a checksum/hash tool written in Java
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

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import net.jacksum.multicore.OSControl;
import net.loefflmann.sugar.util.GeneralString;
import net.jacksum.algorithms.AbstractChecksum;

public class Formatter {

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

        boolean hash_da = !fingerprint.isEmpty();
        boolean size_da = sizeFormatter != null;
        boolean timestamp_da = timestampFormatter != null && timestampFormatter.getParameters().isTimestampWanted();

        StringBuilder sb = new StringBuilder(128);

        // GNU tag
        if (filenameContainedProblematicChars){
            sb.append("\\");
        }

        // hash wanted
        if (hash_da) {
            sb.append(fingerprint);
        }

        // size wanted
        if (size_da) {
            if (hash_da) {
                sb.append(separator);
            }
            sb.append(sizeFormatter.format(checksum.getLength()));
        }

        // timestamp wanted
        if (timestamp_da) {
            if (hash_da || size_da) {
                sb.append(separator);
            }
            sb.append(timestampFormatter.format(checksum.getTimestamp()));
        }

        // name
        // An empty name is treated like an absent one, because there is nothing to separate then.
        // Both cases occur: a name is null if it has never been set, and it is the empty string
        // if the sequence of the option -q is being hashed (QuickAction sets the empty string
        // rather than null on purpose, so that the token #FILENAME is still being replaced if
        // the option -F or --style has been set).
        if (filenameFormatted != null && !filenameFormatted.isEmpty()) {
            if (hash_da || size_da || timestamp_da) {
                sb.append(separator);
            }
            sb.append(filenameFormatted);
        }

        return sb.toString();
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
        if (buffer.indexOf("#SEQUENCE") < 0) {
            return;
        }
        // replace all "#SEQUENCE{<encoding>}"
        SequenceFormatter.resolveEncoding(buffer, sequence, abstractChecksum.getFormatPreferences().getGrouping(),
                abstractChecksum.getFormatPreferences().getGroupChar(), "(#SEQUENCE\\{([^}]+)\\})");

        // just in case we have only a "#SEQUENCE", encode the sequence with the encoding
        // of the hash value (an encoding is not necessarily user selectable, so we must
        // not take a detour by resolving "#SEQUENCE{<encoding>}" here)
        GeneralString.replaceAllStrings(buffer, "#SEQUENCE",
                EncodingDecoding.encodeBytes(sequence,
                        abstractChecksum.getFormatPreferences().getEncoding(),
                        abstractChecksum.getFormatPreferences().getGrouping(),
                        abstractChecksum.getFormatPreferences().getGroupChar()));
    }
    
    private static void _replaceFilesizeToken(StringBuilder buffer, AbstractChecksum abstractChecksum) {
        GeneralString.replaceAllStrings(buffer, "#FILESIZE", Long.toString(abstractChecksum.getLength()));        
    }
    
    private static void _replaceFilenameTokens(StringBuilder buffer, AbstractChecksum abstractChecksum) {

        if (abstractChecksum.getFilename() == null) return;
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
        if (buffer.indexOf("#TIMESTAMP") < 0) {
            return;
        }
        // a timestamp is only available if the data comes from a file, so the token
        // is replaced by an empty string if the data comes from standard input or
        // from a sequence (-q), rather than keeping the token or printing the epoch
        GeneralString.replaceAllStrings(buffer, "#TIMESTAMP",
                abstractChecksum.isTimestampWanted() && abstractChecksum.isTimestampAvailable()
                        ? abstractChecksum.getTimestampFormatted()
                        : "");
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
        FilenameFormatter.replaceAliases(format);
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
