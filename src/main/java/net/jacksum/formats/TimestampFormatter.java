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
package net.jacksum.formats;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import net.jacksum.parameters.base.TimestampFormatParameters;

public class TimestampFormatter implements TimestampFormatParameters {


    private final TimestampFormatParameters parameters;
    private Format timestampFormatter;
    public static final String KEY_ISO8601 = "iso8601";
    public static final String KEY_ISO = "iso";

    public static final String KEY_ISO8601_UTC = "iso8601utc";
    public static final String KEY_ISO_UTC = "iso-utc";

    public static final String KEY_UNIXTIME = "unixtime";
    public static final String KEY_UNIXTIME_MS = "unixtime-ms";

    public static final String KEY_DEFAULT = "default";
    public static final String KEY_DEFAULT_UTC = "default-utc";
    public static final String FORMAT_DEFAULT = "yyyyMMddHHmmssSSS";

    public static final String FORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    public static final String FORMAT_ISO8601_UTC = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public TimestampFormatter(TimestampFormatParameters parameters) {
        this.parameters = parameters;

        // init timestampFormatter if required
        switch (parameters.getTimestampFormat()) {
            case KEY_ISO8601_UTC:
            case KEY_ISO_UTC:
            case KEY_UNIXTIME:
            case KEY_UNIXTIME_MS:
            case KEY_DEFAULT_UTC:
                 break;
            case KEY_ISO8601:
            case KEY_ISO:
                timestampFormatter = new SimpleDateFormat(FORMAT_ISO8601);
                break;
            case KEY_DEFAULT:
                timestampFormatter = new SimpleDateFormat(FORMAT_DEFAULT);
                break;
            default:
                timestampFormatter = new SimpleDateFormat(getParameters().getTimestampFormat());
        }
    }

    public static boolean isFormatASupportedKeyword(String format) {
        switch (format) {
            case KEY_ISO8601:
            case KEY_ISO:
            case KEY_ISO8601_UTC:
            case KEY_ISO_UTC:
            case KEY_UNIXTIME:
            case KEY_UNIXTIME_MS:
            case KEY_DEFAULT:
            case KEY_DEFAULT_UTC:
            return true;
            default: return false;
        }
    }

    public String format(long timestamp) {

        switch (parameters.getTimestampFormat()) {

            case KEY_ISO8601_UTC:
            case KEY_ISO_UTC:
                return DateTimeFormatter
                        .ofPattern(FORMAT_ISO8601_UTC)
                        .withZone(ZoneOffset.UTC)
                        .format(Instant.ofEpochMilli(timestamp));

            case KEY_DEFAULT_UTC:
                return DateTimeFormatter
                        .ofPattern(FORMAT_DEFAULT)
                        .withZone(ZoneOffset.UTC)
                        .format(Instant.ofEpochMilli(timestamp));

            case KEY_UNIXTIME:
                return Long.toString(timestamp / 1000);

            case KEY_UNIXTIME_MS:
                return Long.toString(timestamp);

            case KEY_ISO8601:
            case KEY_ISO:
            case KEY_DEFAULT:
            default:
                return timestampFormatter.format(new Date(timestamp));
        }
    }
    
    /**
     * @return the parameters
     */
    public TimestampFormatParameters getParameters() {
        return parameters;
    }

    @Override
    public boolean isTimestampWanted() {
        return parameters.isTimestampWanted();
    }

    @Override
    public String getTimestampFormat() {
        return parameters.getTimestampFormat();
    }
}
