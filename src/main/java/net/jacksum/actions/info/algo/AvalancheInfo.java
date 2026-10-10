/*
 * Jacksum 4.0.2 - a checksum/hash tool written in Java
 * Copyright (c) 2001-2026 Dipl.-Inf. (FH) Johann N. Löfflmann,
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
 */

package net.jacksum.actions.info.algo;

/**
 * Holds the result of an avalanche test of an algorithm: the minimum, maximum, and average
 * Hamming distances between the hash of a message and the hashes of the message with a
 * single flipped bit, each as a percentage of the hash bits.
 */
public class AvalancheInfo {

    /**
     * Creates a new AvalancheInfo with all values set to 0.0.
     */
    public AvalancheInfo() {
    }

    /**
     * Gets the minimum Hamming distance in percent of the hash bits.
     *
     * @return the minimum Hamming distance in percent
     */
    public double getHammingDistanceMin() {
        return hammingDistanceMin;
    }

    /**
     * Sets the minimum Hamming distance in percent of the hash bits.
     *
     * @param hammingDistanceMin the minimum Hamming distance in percent
     */
    public void setHammingDistanceMin(double hammingDistanceMin) {
        this.hammingDistanceMin = hammingDistanceMin;
    }

    private double hammingDistanceMin = 0.0;

    /**
     * Gets the maximum Hamming distance in percent of the hash bits.
     *
     * @return the maximum Hamming distance in percent
     */
    public double getHammingDistanceMax() {
        return hammingDistanceMax;
    }

    /**
     * Sets the maximum Hamming distance in percent of the hash bits.
     *
     * @param hammingDistanceMax the maximum Hamming distance in percent
     */
    public void setHammingDistanceMax(double hammingDistanceMax) {
        this.hammingDistanceMax = hammingDistanceMax;
    }

    private double hammingDistanceMax = 0.0;

    /**
     * Gets the average Hamming distance in percent of the hash bits.
     *
     * @return the average Hamming distance in percent
     */
    public double getHammingDistanceAvg() {
        return hammingDistanceAvg;
    }

    /**
     * Sets the average Hamming distance in percent of the hash bits.
     *
     * @param hammingDistanceAvg the average Hamming distance in percent
     */
    public void setHammingDistanceAvg(double hammingDistanceAvg) {
        this.hammingDistanceAvg = hammingDistanceAvg;
    }

    private double hammingDistanceAvg = 0.0;



}
