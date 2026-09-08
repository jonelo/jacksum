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
package net.jacksum.actions.io.wanted;

import net.jacksum.formats.Encoding;
import net.jacksum.formats.EncodingDecoding;
import net.jacksum.statistics.StatisticsForHashedFiles;
import net.jacksum.cli.ExitCode;
import net.jacksum.cli.Messenger;
import net.jacksum.compats.parsing.HashEntry;
import net.jacksum.multicore.manyfiles.Message;
import net.jacksum.multicore.manyfiles.MessageConsumer;
import net.jacksum.parameters.Parameters;
import net.jacksum.statistics.Statistics;

import static net.jacksum.cli.Messenger.MsgType.ERROR;
import static net.jacksum.cli.Messenger.MsgType.INFO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MessageConsumerForWantedFiles extends MessageConsumer {

    long filesRead, bytesRead, errors;

    private Parameters parameters;
    private Statistics statistics;
    private List<HashEntry> hashEntries;
    private Map<String, List<HashEntry>> map;
    private Messenger messenger;
    private long found = 0;
    private long notfound = 0;
    MatchFilter filter = null;

    public MessageConsumerForWantedFiles() {
        statistics = new StatisticsForHashedFiles();
        messenger = new Messenger();
    }

    public MessageConsumerForWantedFiles(Parameters parameters, List<HashEntry> list) {
        this();
        this.parameters = parameters;
        this.hashEntries = list;
        this.messenger.setVerbose(parameters.getVerbose());
        filter = parameters.getWantedListFilter();
    }

    /**
     * Returns the wanted hashes, mapped for an indexed access by hash.
     *
     * The map is created on demand, because it depends on the encoding of the
     * hash values: if the alphabet of the encoding is case-insensitive, the
     * lookup is case-insensitive as well, see also Encoding.hashesAreEqual().
     *
     * A hash value is mapped to every entry that stores it, because a wanted list keeps
     * duplicates on purpose, see Parser.setReplaceDuplicateFilenames() and WantedHashes,
     * and each of those entries can carry a file name of its own.
     *
     * @return the wanted hashes, mapped for an indexed access by hash
     */
    private Map<String, List<HashEntry>> getWantedHashes() {
        if (map == null) {
            Encoding encoding = formatPreferences == null ? null : formatPreferences.getEncoding();
            map = (encoding == null || encoding.isCaseSensitive())
                    ? new HashMap<>()
                    : new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            if (hashEntries != null) {
                hashEntries.forEach(hashEntry -> {
                    if (hashEntry.getHash() != null) {
                        map.computeIfAbsent(hashEntry.getHash(), key -> new ArrayList<>()).add(hashEntry);
                    }
                });
            }
        }
        return map;
    }

    /**
     * Returns the file names that the wanted list stores for a hash value, separated by a comma.
     * Identical names are reported once, and null is returned if not even one entry stores a
     * name at all, which happens e.g. if the style is "hexhashes-only".
     *
     * @param hash the hash value, as it is stored in the wanted list
     * @return the file names that are stored for that hash value, null if there is no name
     */
    private String wantedFilenames(String hash) {
        List<String> filenames = new ArrayList<>();
        for (HashEntry hashEntry : getWantedHashes().get(hash)) {
            String wantedFilename = hashEntry.getFilename();
            if (wantedFilename != null && !filenames.contains(wantedFilename)) {
                filenames.add(wantedFilename);
            }
        }
        return filenames.isEmpty() ? null : String.join(", ", filenames);
    }

    @Override
    public void handleMessage(Message message) {
        
        switch (message.getType()) {
            case FILE_HASHED:
            case FILE_HASHED_AND_MATCHES_EXPECTATION:
                 filesRead++;
                 bytesRead += message.getPayload().getSize();

                 String hash = EncodingDecoding.encodeBytes(message.getPayload().getDigest(), formatPreferences.getEncoding(), 0, ' ');
                 String filename = message.getPayload().getPath() == null ? "<stdin>" : message.getPayload().getPath().normalize().toString();

                 if (getWantedHashes().containsKey(hash)) {
                     found++;
                     print(filter.isFilterMatch(), "MATCH", filename, wantedFilenames(hash));
                 } else {
                     notfound++;
                     print(filter.isFilterNoMatch(), "NO MATCH", filename, hash);
                 }
                 break;
            case ERROR:
                 errors++;
                 // the messenger honours the settings of option -V, e.g. "noerrors"
                 messenger.print(ERROR, message.getInfo());
                 break;
            case INFO:
            case INFO_DIR_IGNORED:
                messenger.print(INFO, message.getInfo());
                break;
            default:
                break;
        }
    }

    private int exitCode = 0;
    @Override
    public void handleMessagesFinal() {

        // Only a filter that is exclusively negative asks for a non-match, see also the option
        // --match-filter and the exit code rule that "jacksum -h -e" documents. The keyword "none"
        // is a reset rather than a mode ("positive" is documented as "none,match"), so an empty
        // filter prints nothing, but it keeps the verdict of the default, which is "positive".
        boolean negative = filter.isFilterNoMatch() && !filter.isFilterMatch();
        long checkAgainst = negative ? notfound : found;

        // The expectation is met if at least one file matches resp. does not match, dependent on
        // the filter. A run that could not read even one file gets its verdict as well, so that
        // a script is not told that everything is fine although nothing has been searched.
        if (parameters.isExpectation()) {
            System.err.printf("%sJacksum: Expectation %s.%s",
                    parameters.getLineSeparator(),
                    checkAgainst > 0 ? "met" : "not met",
                    parameters.getLineSeparator());

            // HashFilesWantedAction merges the hash of option -e into the wanted hashes, so if a
            // wanted list has been specified as well, the counter covers both of them
            System.err.printf("Jacksum: %d of the successfully read files %s %s hash value.%s",
                    checkAgainst,
                    negative ? (checkAgainst == 1 ? "does not match" : "do not match")
                             : (checkAgainst == 1 ? "matches" : "match"),
                    parameters.isWantedList() ? "a wanted" : "the expected",
                    parameters.getLineSeparator());
        }

        // One single verdict, so that neither of both options can overwrite the exit code of the
        // other one if they are combined; ExitCode.EXPECTATION_MET and ExitCode.OK are the same.
        exitCode = checkAgainst > 0 ? ExitCode.OK
                : parameters.isWantedList() ? ExitCode.WANTED_NOTFOUND : ExitCode.EXPECTATION_NOT_MET;
    }

    @Override
    public Statistics getStatistics() {
        ((StatisticsForHashedFiles)statistics).setFilesRead(filesRead);
        ((StatisticsForHashedFiles)statistics).setErrors(errors);
        ((StatisticsForHashedFiles)statistics).setBytesRead(bytesRead);
        if (parameters.isWantedList()) {
            // only a wanted list brings a number of wanted hashes with it; the single hash value
            // of option -e is no list, see also HashFilesWantedAction. The map is keyed by hash
            // value, so it answers how many different hashes are being searched for, while the
            // number of lines is a matter of the parser statistics.
            ((StatisticsForHashedFiles)statistics).setTotalNumberOfWantedHashes(getWantedHashes().size());
        } else {
            ((StatisticsForHashedFiles)statistics).setWantedHashesNoun("the expected hash");
        }
        if (parameters.isWantedList() || parameters.isExpectation()) {
            ((StatisticsForHashedFiles)statistics).setFilesMatchesWanted(found);
            ((StatisticsForHashedFiles)statistics).setFilesNoMatchesWanted(notfound);
        }
        return statistics;
    }

    @Override
    public int getExitCode() {
        // a message that could not be consumed means that a file has not been processed at all,
        // so this must be checked before any other condition in order not to be masked
        if (getUnexpectedErrors() > 0) {
            return ExitCode.IO_ERROR;
        }
        if (parameters.isExpectation() || parameters.isWantedList()) {
            // a hash value that has been found is a definite result, even if some files could not
            // be read, but "nothing has been found" is not a reliable answer in that case
            if (exitCode == ExitCode.OK) {
                return ExitCode.OK;
            }
            return errors > 0 ? ExitCode.IO_ERROR : exitCode;
        }
        if (errors > 0) {
            return ExitCode.IO_ERROR;
        }
        return ExitCode.OK;
    }

    private void print(boolean output, String status, String filename, String comment) {
        if (output) {
            if (parameters.isList()) {
                System.out.printf("%s%s", filename, parameters.getLineSeparator());
            } else {
                if (comment != null) {
                    System.out.printf("%9s  %s (%s)%s", status, filename, comment, parameters.getLineSeparator());
                } else { // comment can be null if --style hexhashes-only has been selected
                    System.out.printf("%9s  %s%s", status, filename, parameters.getLineSeparator());
                }
            }
        }
    }

}
