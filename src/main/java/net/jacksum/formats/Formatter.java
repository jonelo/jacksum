/*


  Jacksum 4.0.1 - a checksum/hash tool written in Java
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
        // A timestamp is only available if the data comes from a file. If the data comes from
        // standard input, from a sequence (-q), or from a string (--string-list), no timestamp
        // is printed rather than the epoch, see also _replaceTimestampToken().
        boolean timestamp_da = timestampFormatter != null && timestampFormatter.getParameters().isTimestampWanted()
                && checksum.isTimestampAvailable();

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

    
    private static void _replaceFingerprintTokens(StringBuilder buffer, AbstractChecksum abstractChecksum, TokenValueStore store) {
        // these three are normalizations, they replace a token by another token
        GeneralString.replaceAllStrings(buffer, "#CHECKSUM{i}", "#CHECKSUM");
        GeneralString.replaceAllStrings(buffer, "#CHECKSUM{0}", "#CHECKSUM");
        GeneralString.replaceAllStrings(buffer, "#CHECKSUM{" + abstractChecksum.getName() + "}", "#CHECKSUM" );
        // the algorithm answers to a second name as well, e.g. sha256 besides sha-256
        String nameAlias = abstractChecksum.getNameAlias();
        if (nameAlias != null) {
            GeneralString.replaceAllStrings(buffer, "#CHECKSUM{" + nameAlias + "}", "#CHECKSUM" );
        }
        FingerprintFormatter.resolveEncoding(buffer, abstractChecksum, "(#CHECKSUM\\{i,([^}]+)\\})", store);
        FingerprintFormatter.resolveEncoding(buffer, abstractChecksum, "(#CHECKSUM\\{0,([^}]+)\\})", store);
        FingerprintFormatter.resolveEncoding(buffer, abstractChecksum, "(#CHECKSUM\\{"+abstractChecksum.getName()+",([^}]+)\\})", store);
        if (nameAlias != null) {
            FingerprintFormatter.resolveEncoding(buffer, abstractChecksum, "(#CHECKSUM\\{"+nameAlias+",([^}]+)\\})", store);
        }
        FingerprintFormatter.resolveEncoding(buffer, abstractChecksum, "(#CHECKSUM\\{([^}]+)\\})", store);
        GeneralString.replaceAllStrings(buffer, "#CHECKSUM", store.protect(abstractChecksum.getValueFormatted()));
    }
    
    private static void _replaceAlgorithmTokens(StringBuilder buffer, AbstractChecksum abstractChecksum, TokenValueStore store) {
        // algorithm names: the first six are normalizations, they replace a token by another token
        GeneralString.replaceAllStrings(buffer, "#ALGONAME{i}", "#ALGONAME");
        GeneralString.replaceAllStrings(buffer, "#ALGONAME{i,uppercase}", "#ALGONAME{uppercase}");
        GeneralString.replaceAllStrings(buffer, "#ALGONAME{i,lowercase}", "#ALGONAME{lowercase}");
        GeneralString.replaceAllStrings(buffer, "#ALGONAME{0}", "#ALGONAME");
        GeneralString.replaceAllStrings(buffer, "#ALGONAME{0,uppercase}", "#ALGONAME{uppercase}");
        GeneralString.replaceAllStrings(buffer, "#ALGONAME{0,lowercase}", "#ALGONAME{lowercase}");
        GeneralString.replaceAllStrings(buffer, "#ALGONAME{uppercase}", store.protect(abstractChecksum.getName().toUpperCase(Locale.US)));
        GeneralString.replaceAllStrings(buffer, "#ALGONAME{lowercase}", store.protect(abstractChecksum.getName().toLowerCase(Locale.US)));
        GeneralString.replaceAllStrings(buffer, "#ALGONAME", store.protect(abstractChecksum.getName()));
    }
    
    private static void _replaceSequenceTokens(StringBuilder buffer, AbstractChecksum abstractChecksum, byte[] sequence, TokenValueStore store) {
        if (buffer.indexOf("#SEQUENCE") < 0) {
            return;
        }
        // replace all "#SEQUENCE{<encoding>}"
        SequenceFormatter.resolveEncoding(buffer, sequence, abstractChecksum.getFormatPreferences().getGrouping(),
                abstractChecksum.getFormatPreferences().getGroupChar(), "(#SEQUENCE\\{([^}]+)\\})", store);

        // just in case we have only a "#SEQUENCE", encode the sequence with the encoding
        // of the hash value (an encoding is not necessarily user selectable, so we must
        // not take a detour by resolving "#SEQUENCE{<encoding>}" here)
        GeneralString.replaceAllStrings(buffer, "#SEQUENCE",
                store.protect(EncodingDecoding.encodeBytes(sequence,
                        abstractChecksum.getFormatPreferences().getEncoding(),
                        abstractChecksum.getFormatPreferences().getGrouping(),
                        abstractChecksum.getFormatPreferences().getGroupChar())));
    }
    
    private static void _replaceFilesizeToken(StringBuilder buffer, AbstractChecksum abstractChecksum, TokenValueStore store) {
        GeneralString.replaceAllStrings(buffer, "#FILESIZE", store.protect(Long.toString(abstractChecksum.getLength())));
    }
    
    private static void _replaceFilenameTokens(StringBuilder buffer, AbstractChecksum abstractChecksum, TokenValueStore store) {

        if (abstractChecksum.getFilename() == null) return;
        FilenameFormatter filenameFormatter = new FilenameFormatter(abstractChecksum.getFormatPreferences());
        String formattedFilename = filenameFormatter.format(abstractChecksum.getFilename());

        boolean escape = abstractChecksum.getFormatPreferences().isGnuEscaping();
        boolean escaped = filenameFormatter.didTheFormatMethodChangeProblematicChars();
        GeneralString.replaceAllStrings(buffer, "#ESCAPETAG", store.protect(escape && escaped ? "\\" : ""));

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
                    store.protect(escape ? FilenameFormatter.gnuEscapeProblematicCharsInFilename(name) : name));
            if (directory == null) directory = "";
            GeneralString.replaceAllStrings(buffer, "#FILENAME{path}",
                    store.protect(escape ? FilenameFormatter.gnuEscapeProblematicCharsInFilename(directory) : directory));
        }
        GeneralString.replaceAllStrings(buffer, "#FILENAME", store.protect(formattedFilename));

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

    private static void _replaceTimestampToken(StringBuilder buffer, AbstractChecksum abstractChecksum, TokenValueStore store) {
        // timestamp
        if (buffer.indexOf("#TIMESTAMP") < 0) {
            return;
        }
        // a timestamp is only available if the data comes from a file, so the token
        // is replaced by an empty string if the data comes from standard input or
        // from a sequence (-q), rather than keeping the token or printing the epoch
        GeneralString.replaceAllStrings(buffer, "#TIMESTAMP",
                store.protect(abstractChecksum.isTimestampWanted() && abstractChecksum.isTimestampAvailable()
                        ? abstractChecksum.getTimestampFormatted()
                        : ""));
    }
    
    private static void _replaceSpecialCharTokens(StringBuilder buffer, AbstractChecksum abstractChecksum, TokenValueStore store) {
        // special chars: separator
        GeneralString.replaceAllStrings(buffer, "#SEPARATOR", store.protect(abstractChecksum.getFormatPreferences().getSeparator()));

        // special chars: quotes
        GeneralString.replaceAllStrings(buffer, "#QUOTE", store.protect("\""));
    }

    private static void _replaceBintagToken(StringBuilder buffer, AbstractChecksum abstractChecksum, TokenValueStore store) {
        GeneralString.replaceAllStrings(buffer, "#BINTAG", store.protect(OSControl.isWindows() ? "*": " "));
    }
    
    public static String format(StringBuilder buffer, AbstractChecksum abstractChecksum, byte[] sequence) {
        TokenValueStore store = new TokenValueStore();
        format(buffer, abstractChecksum, sequence, store);
        // the store has been created here, so it is resolved here as well
        store.resolve(buffer);
        return buffer.toString();
    }

    /**
     * Replaces all tokens of the buffer, protecting every value that is being substituted
     * by the given store. The caller owns the store and has to call its resolve() method
     * once all substitutions are done, see TokenValueStore.
     *
     * The order of the steps below is load bearing and must not be changed.
     *
     * @param buffer the buffer with the format
     * @param abstractChecksum the checksum that provides the values
     * @param sequence the sequence for the token #SEQUENCE
     * @param store the store that protects the substituted values
     * @return the content of the buffer, still with the placeholders of the store
     */
    public static String format(StringBuilder buffer, AbstractChecksum abstractChecksum, byte[] sequence, TokenValueStore store) {
        _replaceBintagToken(buffer, abstractChecksum, store);
        _replaceFingerprintTokens(buffer, abstractChecksum, store);
        _replaceAlgorithmTokens(buffer, abstractChecksum, store);
        _replaceSequenceTokens(buffer, abstractChecksum, sequence, store);
        _replaceFilesizeToken(buffer, abstractChecksum, store);
        _replaceFilenameTokens(buffer, abstractChecksum, store);
        _replaceTimestampToken(buffer, abstractChecksum, store);
        _replaceSpecialCharTokens(buffer, abstractChecksum, store);
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
