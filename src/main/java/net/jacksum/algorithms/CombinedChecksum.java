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
/*

  Author: Johann N. Loefflmann, Germany
  Contributors: Federico Tello Gentile, Argentina (multi core support)


 */
package net.jacksum.algorithms;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.n16n.sugar.util.GeneralString;
import net.jacksum.JacksumAPI;
import net.jacksum.multicore.manyalgos.ConcurrentHasher;
import net.jacksum.multicore.manyalgos.HashAlgorithm;
import net.jacksum.formats.Encoding;
import net.jacksum.formats.FingerprintFormatter;
import net.jacksum.formats.Formatter;

/**
 * This design follows the Composite Pattern.
 *
 * AbstractChecksum is the component that acts as the superclass for the
 * composite class and the concrete leaf classes. CombinedChecksum acts as a
 * composite class, because it supports the AbstractChecksum class interface.
 */
public class CombinedChecksum extends AbstractChecksum {

    /**
     * @return the algorithms
     */
    public List<AbstractChecksum> getAlgorithms() {
        return algorithms;
    }

    /**
     * @param algorithms the algorithms to set
     */
    public void setAlgorithms(List<AbstractChecksum> algorithms) {
        this.algorithms = algorithms;
    }

    private List<AbstractChecksum> algorithms;

    /**
     * Creates a new instance of CombinedChecksum
     */
    public CombinedChecksum() {
        algorithms = new ArrayList<>();
        length = 0;
        filename = null;
        formatPreferences.setSeparator(" ");
        formatPreferences.setHashEncoding(Encoding.HEX);
        formatPreferences.setSizeWanted(true);
    }

    public CombinedChecksum(String[] algos, boolean alternate) throws NoSuchAlgorithmException {
        this();
        setAlgorithms(algos, alternate);
    }

    public void addAlgorithm(String algorithm, boolean alternate) throws NoSuchAlgorithmException {
        AbstractChecksum checksum = JacksumAPI.getChecksumInstance(algorithm, alternate);
        checksum.setName(algorithm);
        addAlgorithm(checksum);
    }

    public void addAlgorithm(AbstractChecksum checksum) {
        bitWidth += checksum.getSize();
        algorithms.add(checksum);
    }

    public final void setAlgorithms(String[] algos, boolean alternate) throws NoSuchAlgorithmException {
        for (String algo : algos) {
            addAlgorithm(algo, alternate);
        }
    }

    /**
     * Removes one algorithm specified by its name
     *
     * @param algorithm the name of the algorithm
     */
    public void removeAlgorithm(String algorithm) {
        for (int i = 0; i < algorithms.size(); i++) {
            AbstractChecksum checksum = algorithms.get(i);
            if (checksum.getName().equals(algorithm)) {
                bitWidth -= checksum.getSize();
                algorithms.remove(i);
                return;
            }
        }
    }

    /**
     * Removes one algorithm specified by its name.
     *
     * @param checksum a AbstractChecksum object.
     */
    public void removeAlgorithm(AbstractChecksum checksum) {
        removeAlgorithm(checksum.getName());
    }

    @Override
    public void reset() {
        for (AbstractChecksum algorithm : algorithms) {
            algorithm.reset();
        }
        length = 0;
    }

    /**
     * Updates all checksums with the specified byte.
     *
     * @param integer the value for the update
     */
    @Override
    public void update(int integer) {
        for (AbstractChecksum algorithm : algorithms) {
            algorithm.update(integer);
        }
        length++;
    }

    /**
     * Updates all checksums with the specified byte.
     *
     * @param b a single byte.
     */
    @Override
    public void update(byte b) {
        for (AbstractChecksum algorithm : algorithms) {
            algorithm.update(b);
        }
        length++;
    }

    /**
     * Updates all checksums with the specified array of bytes.
     */
    @Override
    public void update(byte[] bytes, int offset, int length) {
        for (AbstractChecksum algorithm : algorithms) {
            algorithm.update(bytes, offset, length);
        }
        this.length += length;
    }

    /**
     * Updates all checksums with the specified array of bytes.
     */
    @Override
    public void update(byte[] bytes) {
        for (AbstractChecksum algorithm : algorithms) {
            algorithm.update(bytes);
        }
        this.length += bytes.length;
    }

    /**
     * Returns the result of the computation as a byte array.
     *
     * @return the result of the computation as a byte array
     */
    @Override
    public byte[] getByteArray() {
        List<byte[]> byteArrays = new ArrayList<>();
        int size = 0;
        for (AbstractChecksum algorithm : algorithms) {
            byte[] byteArray = algorithm.getByteArray();
            if (byteArray != null) { // algorithms none or read can return null
                byteArrays.add(byteArray);
                size += byteArray.length;
            }
        }
        byte[] ret = new byte[size];
        int offset = 0;
        for (byte[] src : byteArrays) {
            System.arraycopy(src, 0, ret, offset, src.length);
            offset += src.length;
        }
        return ret;
    }

    /**
     * with this method the format() method can be customized, it will be
     * launched at the beginning of format()
     *
     * @param formatBuf a StringBuilder.
     */
    @Override
    public void preFormat(StringBuilder formatBuf) {

        // normalize the checksum code token and the filesize/length token
        Formatter.replaceAliases(formatBuf);

        // normalize the output of every algorithm
        setEncoding(getFormatPreferences().getEncoding());

        StringBuilder buf = new StringBuilder();
        String format = formatBuf.toString();

        if (format.contains("#CHECKSUM{i") || format.contains("#ALGONAME{i")) {

            for (AbstractChecksum algorithm : algorithms) {
                StringBuilder line = new StringBuilder(format);
                GeneralString.replaceAllStrings(line, "#CHECKSUM{i}", algorithm.getValueFormatted());
                FingerprintFormatter.resolveEncoding(line, algorithm, "(#CHECKSUM\\{i,([^}]+)\\})");
                GeneralString.replaceAllStrings(line, "#ALGONAME{i}", algorithm.getName());
                GeneralString.replaceAllStrings(line, "#ALGONAME{i,uppercase}", algorithm.getName().toUpperCase(Locale.US));
                GeneralString.replaceAllStrings(line, "#ALGONAME{i,lowercase}", algorithm.getName().toLowerCase(Locale.US));
                buf.append(line);
                if (algorithms.size() > 1) {
                    buf.append("\n");
                }
            }
        } else {
            buf.append(format);
        }

        if (buf.toString().contains("#ALGONAME{")) {
            for (int i = 0; i < algorithms.size(); i++) {
                // replace token "ALGONAME{<index>}" with value of ALGONAME{<index>}
                GeneralString.replaceAllStrings(buf, "#ALGONAME{" + i + "}",
                        (algorithms.get(i)).getName());
            }
        }
        
        // are there still tokens which need to be transformed ?
        if (buf.toString().contains("#CHECKSUM{")) {
            // token #CHECKSUM indexed by name
            for (AbstractChecksum algorithm : algorithms) {
                // replace all "#CHECKSUM{<name>}" with "#CHECKSUM{<index>}"
                GeneralString.replaceAllStrings(buf, "#CHECKSUM{" + algorithm.getName() + "}", algorithm.getValueFormatted() );
                // replace all "#CHECKSUM{<name>,<encoding>}" with "#CHECKSUM{<index>,<encoding>}"
                FingerprintFormatter.resolveEncoding(buf, algorithm, "(#CHECKSUM\\{" + algorithm.getName() + ",\\s*([^}]+)\\})");
            }
        }
        
        // are there still tokens which need to be transformed ?
        if (buf.toString().contains("#CHECKSUM{")) {
           
            // tocken #CHECKSUM indexed by an integer
            for (int i = 0; i < algorithms.size(); i++) {
                // replace the token "#CHECKSUM{<i>}" with the formatted value of CHECKSUM{n}
                GeneralString.replaceAllStrings(buf, "#CHECKSUM{" + i + "}", (algorithms.get(i)).getValueFormatted());
                // replace the token "#CHECKSUM{<i>,<encoding>}" with the formatted value of CHECKSUM{<i>,<encoding>}
                FingerprintFormatter.resolveEncoding(buf, algorithms.get(i), "(#CHECKSUM\\{" + i + ",\\s*([^}]+)\\})");
            }
        }
        


        formatBuf.setLength(0);
        formatBuf.append(buf);
    }

    @Override
    public void setEncoding(Encoding encoding) {
        for (AbstractChecksum algorithm : algorithms) {
            algorithm.getFormatPreferences().setHashEncoding(encoding);
        }
        this.getFormatPreferences().setHashEncoding(algorithms.get(0).getFormatPreferences().getEncoding());
    }

    @Override
    public String getName() {
        if (algorithms.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        int i;
        for (i = 0; i < algorithms.size() - 1; i++) {
            sb.append((algorithms.get(i)).getName());
            sb.append('+');
        }
        sb.append((algorithms.get(i)).getName());
        return sb.toString();
    }

    @Override
    public long readFile(String filename, boolean reset) throws IOException {

        if (JacksumAPI.concurrencyManyAlgosEnabled && filename != null) {
            this.filename = filename;
            if (isTimestampWanted()) {
                setTimestamp(filename);
            }
        
            long lengthBackup;

            if (reset) {
                reset();
            }
            List<HashAlgorithm> hashAlgorithms
                    = new ArrayList<>(this.algorithms.size());
            for (AbstractChecksum algorithm : this.algorithms) {
                hashAlgorithms.add(HashAlgorithm.getAlgorithm(algorithm));
            }
            lengthBackup = length;

            // new ConcurrentHasher().updateHashes(file, hashAlgorithms);
            // File.length() returns 0 bytes on disks and partitions like /dev/sda, /dev/sda1 on Linux,
            // resp. \\.\c: on Windows, so we have to store the total bytes that have been read
            ConcurrentHasher concurrentHasher = new ConcurrentHasher();
            concurrentHasher.updateHashes(new File(filename), hashAlgorithms);
            this.length += concurrentHasher.getTotalRead();

            // this.length += file.length();
            
            return length - lengthBackup;

        } else {
            return super.readFile(filename, reset);           
        }

        
    }
}
