/*


  Jacksum 3.0.0 - a checksum utility in Java
  Copyright (c) 2001-2021 Dipl.-Inf. (FH) Johann N. Löfflmann,
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
import java.util.Date;
import net.jacksum.parameters.base.TimestampFormatParameters;

public class TimestampFormatter implements TimestampFormatParameters {


    private final TimestampFormatParameters parameters;
    private Format timestampFormatter;
    public static final String KEY_UNIXTIME = "unixtime";
    public static final String KEY_UNIXTIME_MS = "unixtime-ms";
    public static final String KEY_ISO8601 = "iso8601";
    public static final String KEY_DEFAULT = "default";
    public static final String FORMAT_DEFAULT = "yyyyMMddHHmmssSSS";
    public static final String FORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    public TimestampFormatter(TimestampFormatParameters parameters) {
        this.parameters = parameters;
    }

    public String format(long timestamp) {
        
        if (getParameters().getTimestampFormat().equals(KEY_UNIXTIME)) {
            return Long.toString(timestamp / 1000);
        }
        if (getParameters().getTimestampFormat().equals(KEY_UNIXTIME_MS)) {
            return Long.toString(timestamp);
        }
        if (getParameters().getTimestampFormat().equals(KEY_DEFAULT) && timestampFormatter == null) {
            timestampFormatter = new SimpleDateFormat(FORMAT_DEFAULT);
        } else
        if (getParameters().getTimestampFormat().equals(KEY_ISO8601) && timestampFormatter == null) {
            timestampFormatter = new SimpleDateFormat(FORMAT_ISO8601);
        } else
        if (timestampFormatter == null) {
            timestampFormatter = new SimpleDateFormat(getParameters().getTimestampFormat());
        }
        return timestampFormatter.format(new Date(timestamp));
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
