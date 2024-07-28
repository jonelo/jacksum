/*
 * Jacksum 3.2.0 - a checksum utility in Java
 * Copyright (c) 2001-2024 Dipl.-Inf. (FH) Johann N. Löfflmann,
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

public class AvalancheInfo {
    public double getHammingDistanceMin() {
        return hammingDistanceMin;
    }

    public void setHammingDistanceMin(double hammingDistanceMin) {
        this.hammingDistanceMin = hammingDistanceMin;
    }

    private double hammingDistanceMin = 0.0;

    public double getHammingDistanceMax() {
        return hammingDistanceMax;
    }

    public void setHammingDistanceMax(double hammingDistanceMax) {
        this.hammingDistanceMax = hammingDistanceMax;
    }

    private double hammingDistanceMax = 0.0;

    public double getHammingDistanceAvg() {
        return hammingDistanceAvg;
    }

    public void setHammingDistanceAvg(double hammingDistanceAvg) {
        this.hammingDistanceAvg = hammingDistanceAvg;
    }

    private double hammingDistanceAvg = 0.0;



}
