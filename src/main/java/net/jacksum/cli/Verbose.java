/**
 *******************************************************************************
 *
 * Jacksum 3.0.0 - a checksum utility in Java
 * Copyright (c) 2001-2021 Dipl.-Inf. (FH) Johann N. Löfflmann,
 * All Rights Reserved, <https://jacksum.net>.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <https://www.gnu.org/licenses/>.
 *
 *******************************************************************************
 */
package net.jacksum.cli;

/**
 * This class stores the verbose states. It controls the output of Warnings and
 * Details and wheather a statistics is printed.
 */
public class Verbose {


    private boolean info;
    private boolean warnings;
    private boolean errors;
    private boolean summary;

    /**
     * Constructs a verbose object.
     */
    public Verbose() {
        info = false;
        warnings = true;
        errors = true;
        summary = false;
    }

    public void setDefault() {
        info = false;
        warnings = true;
        setErrors(true);
        summary = false;
    }

    /**
     * Resets all verbose states to a default. Warnings and Details are enabled,
     * Summary is disabled.
     */
    public void enableAll() {
        info = true;
        warnings = true;
        setErrors(true);
        summary = true;
    }

    public void disableAll() {
        info = false;
        warnings = false;
        setErrors(false);
        summary = false;
    }

    /**
     * Sets the details state.
     *
     * @param info is info wanted?
     */
    public void setInfo(boolean info) {
        this.info = info;
    }

    /**
     * Gets the details state.
     *
     * @return whether info is wanted
     */
    public boolean isInfo() {
        return info;
    }

    /**
     * Sets the warning state.
     *
     * @param warnings are warnings wanted?
     */
    public void setWarnings(boolean warnings) {
        this.warnings = warnings;
    }
    
    /**
     * @return the warnings
     */
    public boolean isWarnings() {
        return warnings;
    }


    /**
     * Gets the warning state.
     *
     * @return are warnings wanted?
     */
    public boolean warningsWanted() {
        return isWarnings();
    }

    /**
     * @return the errors
     */
    public boolean isErrors() {
        return errors;
    }

    /**
     * @param errors the errors to set
     */
    public void setErrors(boolean errors) {
        this.errors = errors;
    }

    /**
     * Sets the statistics state.
     *
     * @param statistis whether a summary is desired or not.
     */
    public void setSummary(boolean statistis) {
        this.summary = statistis;
    }

    /**
     * Gets the statistics state.
     *
     * @return is a statistic wanted?
     */
    public boolean isSummary() {
        return summary;
    }

    public void setVerbose(String arg) throws IllegalArgumentException {

        String[] tokens = arg.split(",");
        for (String token : tokens) {
            switch (token) {
                case "default":
                    setDefault();
                    break;
                case "all":
                    enableAll();
                    break;
                case "none":
                    disableAll();
                    break;
                case "info":
                    setInfo(true);
                    break;
                case "noinfo":
                    setInfo(false);
                    break;
                case "warnings":
                    setWarnings(true);
                    break;
                case "nowarnings":
                    setWarnings(false);
                    break;
                case "errors":
                    setErrors(true);
                    break;
                case "noerrors":
                    setErrors(false);
                    break;
                case "summary":
                    setSummary(true);
                    break;
                case "nosummary":
                    setSummary(false);
                    break;
                default:
                    throw new IllegalArgumentException(String.format("%s is an invalid parameter", token));
            }
        }
    }
    
    
  


}
