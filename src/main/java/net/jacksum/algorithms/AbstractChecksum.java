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
package net.jacksum.algorithms;

import net.jacksum.formats.FormatPreferences;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.Checksum;
import net.jacksum.formats.Encoding;
import net.jacksum.formats.Formatter;
import net.jacksum.parameters.Sequence;
import net.jacksum.parameters.combined.ChecksumParameters;

/**
 * An abstract class that is actually the parent of all algorithms.
 */
abstract public class AbstractChecksum implements Checksum {

    public final static int BUFFERSIZE = 64 * 1024; //65536; // 64 KiB

    protected long length;
    protected String filename;
    protected long timestamp;
    protected String name;
    protected int bitWidth;
    protected int blocksize;
    private byte[] sequence;

    private boolean actualAlternateImplementationUsed;
    protected FormatPreferences formatPreferences;
    protected Formatter formatter;

    protected static String stdinName = "<stdin>";

    /**
     * Creates an AbstractChecksum.
     */
    public AbstractChecksum() {
        name = null;
        length = 0;
        filename = null;
        sequence = null;
        timestamp = 0;
        bitWidth = 0;
        blocksize = 0;

        formatPreferences = new FormatPreferences();
        formatter = new Formatter(formatPreferences);
    }

    // set the Parameters for the Checksum according to the ChecksumParameters
    // interface. It sets the values only if they are non-default
    public void setParameters(ChecksumParameters parameters) {
//        this.checksumParameters = parameters;
        formatPreferences.overwritePreferences(parameters);
        formatter = new Formatter(formatPreferences);
        if (parameters.isSequence()) {
            this.sequence = parameters.getSequence().asBytes();
        }
        if (parameters.getSequence() != null && parameters.getSequence().getType() != null && parameters.getSequence().getType().equals(Sequence.Type.FILE)) {
            filename = parameters.getSequence().getPayload();
        }

    }

    public FormatPreferences getFormatPreferences() {
        return formatPreferences;
    }

    /**
     * Set the name of the algorithm
     *
     * @param name the name of the algorithm
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the algorithm.
     *
     * @return the name of the algorithm.
     */
    public String getName() {
        return name;
    }

    /**
     * Resets the checksum to its initial value for further use.
     */
    // from the Checksum interface
    @Override
    abstract public void reset();

    /**
     * Updates the checksum with the specified byte.
     *
     * @param i an integer
     */
    // from the Checksum interface
    @Override
    public void update(int i) {
        byte b = (byte) (i & 0xFF);
        update(new byte[]{b}, 0, 1);
    }

    /**
     * Updates the checksum with the specified byte.
     *
     * @param b a byte
     */
    public void update(byte b) {
        update(new byte[]{b}, 0, 1);
    }

    /**
     * Updates the current checksum with the specified array of bytes.
     *
     * @param bytes the byte array to update the checksum with
     * @param offset the start offset of the data
     * @param length the number of bytes to use for the update
     */
    @Override
    abstract public void update(byte[] bytes, int offset, int length);

    /**
     * Updates the current checksum with the specified array of bytes.
     *
     * @param bytes a byte array.
     */
    @Override
    public void update(byte[] bytes) {
        update(bytes, 0, bytes.length);
    }

    /**
     * Update the current checksum using the specified ByteBuffer.
     *
     * @param byteBuffer - the ByteBuffer
     * @param ignorePointers - determines whether the limit and position values
     * of the buffer should be ignored or not.
     *
     * If ignorePointers is true the entire buffer will be processed, and upon
     * return the buffer's pointers will not have changed.
     *
     * If ignorePointers is false both limit and position values are being taken
     * into account and upon return, the buffer's position will be equal to it's
     * limit, it's limit will not have changed.
     *
     * @since Jacksum 3.0.0
     */
    public void update(ByteBuffer byteBuffer, boolean ignorePointers) {
        if (byteBuffer == null) {
            throw new NullPointerException();
        }

        ByteBuffer buffer = byteBuffer.asReadOnlyBuffer();

        int work
                = ignorePointers ? buffer.capacity() : buffer.remaining();
        int start
                = ignorePointers ? 0 : buffer.position();
        int end
                = ignorePointers ? buffer.capacity() : buffer.limit();

        int sliceSize = BUFFERSIZE;
        int fullSlices = work / sliceSize;
        byte[] bytes = new byte[sliceSize];
        for (int i = 0; i < fullSlices; i++) {
            int pos = start + (i * sliceSize);
            buffer.limit(pos + sliceSize);
            buffer.position(pos);
            ByteBuffer slice = buffer.slice();
            slice.get(bytes);
            update(bytes);
        }
        // get the rest as bytes
        int rest = work - (fullSlices * sliceSize);
        buffer.limit(end);
        buffer.position(end - rest);
        ByteBuffer slice = buffer.slice();
        slice.get(bytes, 0, rest);
        update(bytes, 0, rest);

        // update the byteBuffer's position pointer
        if (!ignorePointers) {
            byteBuffer.position(end);
        }
    }

    /**
     * Updates the current checksum using the specified ByteBuffer. Both limit
     * and position values are being taken into account and upon return, the
     * buffer's position will be equal to it's limit, it's limit will not have
     * changed.
     *
     * @param byteBuffer - the ByteBuffer
     * @since Jacksum 3.0.0
     */
    @Override
    public void update(ByteBuffer byteBuffer) {
        update(byteBuffer, false);
    }

    /**
     * Returns the length of the processed bytes.
     *
     * @return the length of the processed bytes.
     */
    public long getLength() {
        return length;
    }


    /**
     * Returns the result of the computation as a byte array.
     *
     * @return the result of the computation as a byte array.
     * @since Jacksum 1.6
     */
    abstract public byte[] getByteArray();

    // use getByteArray() works for any bit width
    // overwrite getValue() for those classes who require this method
    @Override
    public long getValue() {
        throw new UnsupportedOperationException("Operation not supported for bit width " + bitWidth);
    }

    /**
     * The toString() method.
     */
    @Override
    public String toString() {
        return formatter.format(this);
    }

    @Override
    public int hashCode() {
        // let's do a very primitive hash rather than just a sum
        // let's also avoid circular dependencies among classes
        // let's also avoid casts, let's use shifts for performance
        // and prims for better security
        byte[] b = getByteArray();
        int s = 0;
        for (int i = 0; i < b.length; i++) {
            s = ((s << 8) + b[i]) % 0x7FFFF1; // is prim
        }
        return s;
    }

    /**
     * Returns true only if the specified checksum is equal to this object.
     *
     * @param anObject an object for comparison.
     * @return true only if the specified checksum is equal to this object.
     */
    @Override
    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof AbstractChecksum) {
            AbstractChecksum abstractChecksum = (AbstractChecksum) anObject;
            return Arrays.equals(getByteArray(), abstractChecksum.getByteArray());
        }
        return false;
    }

    /**
     * Returns the internal block bitWidth in bytes (if 0 is returned, it is not
     * a hash based algorithm) Method is used for HMAC support
     *
     * @return the block size in bytes
     */
    public int getBlockSize() {
        return blocksize;
    }

    /**
     * Returns the bitWidth in bits of the checksum/digest. Among other things,
     * it is used for Tree Hash support
     *
     * @return the bitWidth in bits of the checksum/digest
     */
    public int getSize() {
        return bitWidth;
    }

    public String getValueFormatted(Encoding encoding) {
        return formatter.getFingerprintFormatter().format(getByteArray(), encoding);
    }

    /**
     * Returns the checksum, formatted.
     *
     * @return the checksum, formatted.
     * @since Jacksum 1.6
     */
    public String getValueFormatted() {
        return formatter.getFingerprintFormatter().format(getByteArray());
    }

    // with this method, the format() method can be customized
    // useful for token-aliases such as #FINGERPRINT
    public void preFormat(StringBuilder format) {
        Formatter.replaceAliases(format);
    }

    // will be triggered by the CLI option --format
    public String format(String format) {
        StringBuilder stringBuilder = new StringBuilder(format);
        preFormat(stringBuilder);
        return Formatter.format(stringBuilder, this, sequence);
    }

    /**
     * Sets the filename.
     *
     * @param filename the filename.
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * Gets the filename.
     *
     * @return the filename.
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Sets the encoding for the hash.
     * @param encoding the encoding for the hash.
     */
    public void setEncoding(Encoding encoding) {
        formatPreferences.setHashEncoding(encoding);
    }

    /**
     * Sets the timestamp.
     *
     * @param filename the file from which the timestamp should be gathered.
     */
    public void setTimestamp(String filename) {
        File file = new File(filename);
        this.timestamp = file.lastModified();
    }

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the timestamp, formatted.
     *
     * @return the timestamp, formatted.
     */
    public String getTimestampFormatted() {
        return formatter.getTimestampFormatter().format(timestamp);
    }

    /**
     * Determines if a timestamp is wanted.
     *
     * @return the boolean if a timestamp is wanted.
     */
    public boolean isTimestampWanted() {
        return (formatPreferences.getTimestampFormat() != null);
    }

    /**
     * Reads a file and calculates the checksum from it.
     *
     * @param filename - the file which should be read
     * @return the number of bytes that have been read.
     * @throws IOException in case of an I/O error.
     */
    public long readFile(String filename) throws IOException {
        return readFile(filename, true);
    }

    /**
     * Reads a file and calculates the checksum from it.
     *
     * @param filename the filename of the file that should be read, if filename
     * is null, data is read from stdin
     * @param reset if reset is true, reset() will be launched before the
     * checksum gets updated
     * @return the number of bytes read
     * @throws IOException if an I/O error occurs
     */
    public long readFile(String filename, boolean reset) throws IOException {
        if (filename == null) { // read from stdin
            return readStdin(reset);
        }

        this.filename = filename;
        if (isTimestampWanted()) {
            setTimestamp(filename);
        }

        InputStream is = null;
        InputStream bis = null;
        long lengthBackup;

        // http://java.sun.com/developer/TechTips/1998/tt0915.html#tip2
        try {
            is = new FileInputStream(filename);
            bis = new BufferedInputStream(is);

            if (reset) {
                reset();
            }
            byte[] buffer = new byte[BUFFERSIZE];
            lengthBackup = length;
            int len;
            while ((len = bis.read(buffer)) > -1) {
                update(buffer, 0, len);
            }
        } finally {
            if (bis != null) {
                bis.close();
            }
            if (is != null) {
                is.close();
            }
        }

        return length - lengthBackup;
    }

    public long readStdin() throws IOException {
        return readStdin(true);
    }

    public static void setStdinName(String stdinName) {
        AbstractChecksum.stdinName = stdinName;
    }

    public static String getStdinName() {
        return stdinName;
    }

    public long readStdin(boolean reset) throws IOException {
        this.filename = stdinName;
        InputStream stdin;
        InputStream stdin_buffered = null;
        long lengthBackup;

        try {
            stdin = System.in;
            stdin_buffered = new BufferedInputStream(stdin);

            if (reset) {
                reset();
            }
            byte[] buffer = new byte[BUFFERSIZE];
            lengthBackup = length;
            int len;
            while ((len = stdin_buffered.read(buffer)) > -1) {
                update(buffer, 0, len);
            }
        } finally { // don't close stdin, only stdin_buffered
            if (stdin_buffered != null) {
                stdin_buffered.close();
            }
        }

        return length - lengthBackup;
    }

    /**
     * @return the actualAlternateImplementationUsed
     */
    public boolean isActualAlternateImplementationUsed() {
        return actualAlternateImplementationUsed;
    }

    /**
     * @param actualAlternateImplementationUsed the
     * actualAlternateImplementationUsed to set
     */
    public void setActualAlternateImplementationUsed(boolean actualAlternateImplementationUsed) {
        this.actualAlternateImplementationUsed = actualAlternateImplementationUsed;
    }

}
