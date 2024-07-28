/*

  Jacksum 3.8.0 - a checksum utility in Java
  Copyright (c) 2001-2023 Dipl.-Inf. (FH) Johann N. Löfflmann,
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.jacksum.algorithms.AbstractChecksum;
import net.jacksum.compats.parsing.HashEntry;
import net.jacksum.formats.EncodingDecoding;
import net.jacksum.multicore.manyfiles.Message;
import net.jacksum.multicore.manyfiles.MessageConsumer;
import net.jacksum.cli.ExitCode;
import net.jacksum.cli.Messenger;
import static net.jacksum.cli.Messenger.MsgType.ERROR;
import static net.jacksum.cli.Messenger.MsgType.INFO;
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
    private Statistics statistics;
    private Messenger messenger;

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
        /*
            for (HashEntry hashEntry : hashEntries) {
            map.put(hashEntry.getFilename(), hashEntry);
        }*/
        hashEntries.forEach(hashEntry -> {

            // we need to put the absolute, normalized path to the hash map in order to detect
            // unique filenames
            try {
                Path absolute = Paths.get(hashEntry.getFilename()).toAbsolutePath().normalize();
                map.put(absolute.toString(), hashEntry);
            } catch (InvalidPathException ipe) {
                map.put(hashEntry.getFilename(), hashEntry);
            }

        });
    }

    private TimestampFormatter timestampFormatter;

    public void setParameters(CheckConsumerParameters checkConsumerParameters) {
        this.parameters = checkConsumerParameters;
        messenger.setVerbose(parameters.getVerbose());
        if (parameters.isTimestampWanted()) {
             timestampFormatter = new TimestampFormatter(parameters);
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
                    if (map.get(filenameAsKey).getFilesize() > -1 && map.get(filenameAsKey).getFilesize() != message.getPayload().getSize()) {
                            print(filter.isFilterFailed(), FAILED, filename);
                            if (!parameters.isList() && parameters.getVerbose().isInfo()) {
                                System.err.printf("           [filesize expected: %s, actual: %s]\n", map.get(filenameAsKey).getFilesize(), message.getPayload().getSize());
                            }
                            mismatches++;
                            cont = false;
                    }
                    
                    // check the timestamp if timestamp is available in the map
                    if (cont && map.get(filenameAsKey).getTimestamp() != null) {
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
                    
                    if (cont) {
                        // compare the hashes: OK or FAILED
                        if (EncodingDecoding.encodeBytes(message.getPayload().getDigest(), parameters.getEncoding(), 0, ' ').equals(map.get(filenameAsKey).getHash())) {
                            print(filter.isFilterOk(), OK, filename);
                            matches++;
                            //map.get(filename).setStatus(HashEntry.Status.OK);
                        } else {
                            print(filter.isFilterFailed(), FAILED, filename);
                            mismatches++;
                            //map.get(filename).setStatus(HashEntry.Status.FAILED);
                        }
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
        if (errors > 0) {
            return ExitCode.IO_ERROR;
        }
        ListFilter listFilter = parameters.getListFilter();
        if (parameters.isCheckStrict()) {
            // if --check-strict is set, --list-filter all must be set
            if (!listFilter.isFilterOk() ||
                    !listFilter.isFilterFailed() ||
                    !listFilter.isFilterMissing() ||
                    !listFilter.isFilterNew()) {
                return ExitCode.PARAMETER_ERROR;
            }
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
