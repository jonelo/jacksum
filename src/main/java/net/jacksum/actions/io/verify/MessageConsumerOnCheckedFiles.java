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
package net.jacksum.actions.io.verify;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.compats.parsing.HashEntry;
import net.jacksum.formats.Encoding;
import net.jacksum.formats.EncodingDecoding;
import net.jacksum.multicore.manyfiles.Message;
import net.jacksum.multicore.manyfiles.MessageConsumer;
import net.jacksum.cli.ExitCode;
import net.jacksum.cli.Messenger;
import static net.jacksum.cli.CLIParameters.__CHECK_STRICT;
import static net.jacksum.cli.CLIParameters.__LIST_FILTER;
import static net.jacksum.cli.Messenger.MsgType.ERROR;
import static net.jacksum.cli.Messenger.MsgType.INFO;
import static net.jacksum.cli.Messenger.MsgType.WARNING;
import net.jacksum.formats.TimestampFormatter;
import net.jacksum.statistics.StatisticsOnCheckedFiles;
import net.jacksum.statistics.Statistics;

public class MessageConsumerOnCheckedFiles extends MessageConsumer {

    private final static String OK = "OK";
    private final static String FAILED = "FAILED";
    private final static String MISSING = "MISSING";
    private final static String NEW = "NEW";

    private long filesRead, bytesRead, newFiles;
    private CheckConsumerParameters parameters;
    private List<HashEntry> hashEntries;
    private HashMap<String, HashEntry> map;
    private long matches, mismatches, errors, filesMissing;
    private final Statistics statistics;
    private final Messenger messenger;

    // private Set<String> notRemovedFilesSet;
    public MessageConsumerOnCheckedFiles() {
        statistics = new StatisticsOnCheckedFiles();
        messenger = new Messenger();
        // notRemovedFilesSet = new HashSet<>();
    }

    public MessageConsumerOnCheckedFiles(List<HashEntry> list) {
        this();
        this.hashEntries = list;

        // Let's put the hashEntries to a map for an indexed access by filename.
        map = new HashMap<>();
        hashEntries.forEach(hashEntry -> map.put(key(hashEntry.getFilename()), hashEntry));
    }

    /**
     * Returns the key that a file name of the check file is stored under in the map.
     * See also handleMessage() which calculates the very same key for the file name
     * of a message that has been received.
     *
     * @param filename a file name as it occurs in the check file
     * @return the key for the map
     */
    private static String key(String filename) {
        // The pseudo name for standard input (e.g. <stdin> or -) does not refer to a file on
        // the file system, so it must not be resolved against the current working directory:
        // a message for stdin does not carry a path, and it is looked up by that pseudo name.
        if (filename.equals(AbstractChecksum.getStdinName())) {
            return filename;
        }
        // we need to put the absolute, normalized path to the hash map in order to detect
        // unique filenames
        try {
            return Paths.get(filename).toAbsolutePath().normalize().toString();
        } catch (InvalidPathException ipe) {
            return filename;
        }
    }

    private TimestampFormatter timestampFormatter;

    // set if the parameters are inconsistent, see setParameters() and getExitCode()
    private boolean parameterError;

    public void setParameters(CheckConsumerParameters checkConsumerParameters) {
        this.parameters = checkConsumerParameters;
        messenger.setVerbose(parameters.getVerbose());
        if (parameters.isTimestampWanted()) {
             timestampFormatter = new TimestampFormatter(parameters);
        }

        // A strict check has to detect all statuses, but a filter can prevent hashing which would
        // prevent a reliable detection. Parameters.checkParameters() rejects that combination
        // before any file is being read, but a program that uses Jacksum as a library could have
        // skipped that validation, so such a check must not be reported as a success.
        if (parameters.isCheckStrict() && !parameters.getListFilter().isAll()) {
            parameterError = true;
            messenger.print(ERROR, String.format("Option %s requires %s all, because a filter could prevent hashing which could prevent a reliable detection, but %s %s has been set.",
                    __CHECK_STRICT, __LIST_FILTER, __LIST_FILTER, parameters.getListFilter()));
        }
    }

    // in order to warn only once, and not for every single entry, see warnTimestampNotAvailable()
    private boolean timestampNotAvailableWarned = false;

    /**
     * Warns once that the timestamp of an entry of the check file cannot be verified, because
     * a timestamp is only available for data that comes from a file.
     *
     * @param filename the name of the first entry that is affected
     */
    private void warnTimestampNotAvailable(String filename) {
        if (!timestampNotAvailableWarned) {
            timestampNotAvailableWarned = true;
            messenger.print(WARNING, String.format(
                    "The timestamp of \"%s\" cannot be verified, because a timestamp is not available for that input. Only hash values and file sizes are verified for such entries.",
                    filename));
        }
    }

    private void print(boolean output, String status, String filename) {
        if (output) {
            if (parameters.isList()) {
                System.out.printf("%s\n", filename);                
            } else {
                System.out.printf("%9s  %s\n", status, filename);
            }
        }
    }

    @Override
    public void handleMessage(Message message) {

        ListFilter filter = parameters.getListFilter();
        String filename;
        String filenameAsKey;

        switch (message.getType()) {
            case FILE_HASHED:
            case FILE_HASHED_AND_MATCHES_EXPECTATION:
                // some statistics
                filesRead++;
                bytesRead += message.getPayload().getSize();

                filenameAsKey = null;
                // set the filename
                if (message.getPayload().getPath() == null) {
                    if (message.getPayload().getSpecialPath() == null) {
                        filename = AbstractChecksum.getStdinName();
                    } else {
                        filename = message.getPayload().getSpecialPath();
                    }
                } else {
                    filename = message.getPayload().getPath().toString();
                    filenameAsKey = message.getPayload().getPath().toAbsolutePath().normalize().toString();
                }
                if (filenameAsKey == null) {
                    filenameAsKey = filename;
                }
                
                
                
                // is it a file that we can compare ...?
                if (map.containsKey(filenameAsKey)) {

                    boolean cont = true;
                    
                    // check if filesize is available in the map
                    if (!parameters.isIgnoreSizes() && map.get(filenameAsKey).getFilesize() > -1 && map.get(filenameAsKey).getFilesize() != message.getPayload().getSize()) {
                            print(filter.isFilterFailed(), FAILED, filename);
                            if (!parameters.isList() && parameters.getVerbose().isInfo()) {
                                System.err.printf("           [filesize expected: %s, actual: %s]\n", map.get(filenameAsKey).getFilesize(), message.getPayload().getSize());
                            }
                            mismatches++;
                            cont = false;
                    }

                    // check the timestamp if timestamp is available in the map
                    if (cont && !parameters.isIgnoreTimestamps() && map.get(filenameAsKey).getTimestamp() != null) {
                        // a timestamp is only available if the data comes from a file, so there is
                        // nothing to compare for standard input or an NTFS alternate data stream
                        if (timestampFormatter == null || message.getPayload().getBasicFileAttributes() == null) {
                            warnTimestampNotAvailable(filename);
                        } else {
                            String actualTimestampAsString = timestampFormatter.format(message.getPayload().getBasicFileAttributes().lastModifiedTime().to(TimeUnit.MILLISECONDS));
                            if (!map.get(filenameAsKey).getTimestamp().equals(actualTimestampAsString)) {
                                print(filter.isFilterFailed(), FAILED, filename);
                                if (!parameters.isList() && parameters.getVerbose().isInfo()) {
                                    System.err.printf("           [timestamp expected: %s, actual: %s]\n", map.get(filenameAsKey).getTimestamp(), actualTimestampAsString);
                                }
                                mismatches++;
                                cont = false;
                            }
                        }
                    }

                    // '-a none' has not been set, a hash is not there, but the file is there for sure (message type == FILE_HASHED)
                    if (cont && message.getPayload().getDigest() == null) {
                        print(filter.isFilterOk(), OK, filename);
                        matches++;
                        cont = false;
                    }

                    // a hash value is there
                    if (cont && !parameters.isIgnoreHashes()) {
                        // compare the hashes: OK or FAILED
                        // the comparison is tolerant regarding upper and lower case if the
                        // alphabet of the encoding allows it, see Encoding.hashesAreEqual()
                        if (Encoding.hashesAreEqual(
                                EncodingDecoding.encodeBytes(message.getPayload().getDigest(), parameters.getEncoding(), 0, ' '),
                                map.get(filenameAsKey).getHash(),
                                parameters.getEncoding())) {
                            print(filter.isFilterOk(), OK, filename);
                            matches++;
                            cont = false;
                            //map.get(filename).setStatus(HashEntry.Status.OK);
                        } else {
                            print(filter.isFilterFailed(), FAILED, filename);
                            mismatches++;
                            cont = false;
                            //map.get(filename).setStatus(HashEntry.Status.FAILED);
                        }
                    }

                    // we only check the existence of the file, and since it is tagged with FILE_HASHED, we know it is there for sure.
                    if (cont) {
                        print(filter.isFilterOk(), OK, filename);
                        matches++;
                    }
                // ... or is it a new file?
                } else {
                    // the name is known by the consumer, but it is not in parsedHashEntries, so it must be a new file
                    print(filter.isFilterNew(), NEW, filename);
                    newFiles++;
                }
                break;

            case FILE_NOT_HASHED:
                filenameAsKey = null;
                if (message.getPayload().getPath() == null) {

                    if (message.getPayload().getSpecialPath() == null) {
                        filename = AbstractChecksum.getStdinName();
                    } else {
                        filename = message.getPayload().getSpecialPath();
                    }
                } else {
                    filename = message.getPayload().getPath().toString();
                    filenameAsKey = message.getPayload().getPath().toAbsolutePath().normalize().toString();
                }
                if (filenameAsKey == null) {
                    filenameAsKey = filename;
                }

                if (!map.containsKey(filenameAsKey)) {
                    print(filter.isFilterNew(), NEW, filename);
                    newFiles++;
                }
                break;

            case ERROR:
                messenger.print(ERROR, message.getInfo());                
                errors++;

                filenameAsKey = null;
                if (message.getPayload().getPath() == null) {
                    if (message.getPayload().getSpecialPath() == null) {
                        filename = AbstractChecksum.getStdinName();
                    } else {
                        filename = message.getPayload().getSpecialPath();
                    }
                } else {
                    filename = message.getPayload().getPath().toString();
                    filenameAsKey = message.getPayload().getPath().toAbsolutePath().normalize().toString();
                }
                if (filenameAsKey == null) {
                    filenameAsKey = filename;
                }

                if (map.containsKey(filenameAsKey)) {
                    print(filter.isFilterMissing(), MISSING, filename);
                    filesMissing++;
                }

                break;

            case INFO:
            case INFO_DIR_IGNORED:
                messenger.print(INFO, message.getInfo());                
                break;
            default:
                break;
        }
    }

    @Override
    public void handleMessagesFinal() {
    }

    @Override
    public Statistics getStatistics() {
        ((StatisticsOnCheckedFiles) statistics).setListFilter(parameters.getListFilter());
        ((StatisticsOnCheckedFiles) statistics).setBytesRead(bytesRead);
        ((StatisticsOnCheckedFiles) statistics).setFilesRead(filesRead);
        ((StatisticsOnCheckedFiles) statistics).setMatches(matches);
        ((StatisticsOnCheckedFiles) statistics).setMismatches(mismatches);
        ((StatisticsOnCheckedFiles) statistics).setMissingFiles(filesMissing);
        ((StatisticsOnCheckedFiles) statistics).setErrors(errors);
        ((StatisticsOnCheckedFiles) statistics).setNewFiles(newFiles);
        return statistics;
    }

    @Override
    public int getExitCode() {
        // the parameters don't allow a reliable check at all, see setParameters()
        if (parameterError) {
            return ExitCode.PARAMETER_ERROR;
        }
        // a message that could not be consumed means that a file has not been verified at all
        if (errors > 0 || getUnexpectedErrors() > 0) {
            return ExitCode.IO_ERROR;
        }
        if (parameters.isCheckStrict()) {
            if (mismatches + filesMissing + newFiles > 0) {
                return ExitCode.EXPECTATION_NOT_MET;
            }
        } else {
            if (mismatches > 0) {
                return ExitCode.CHECK_MISMATCH;
            }
        }
        return ExitCode.OK;
    }

}
