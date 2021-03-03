package de.minebench.bauevent.bewertungen;

/*
 * MbBaueventBewertungen
 * Copyright (c) 2021 Max Lee aka Phoenix616 (max@themoep.de)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class Placement {

    private final int place;
    private final ProtectedRegion region;
    private final double rating;

    public Placement(int place, ProtectedRegion region, double rating) {
        this.place = place;
        this.region = region;
        this.rating = rating;
    }

    public int getPlace() {
        return place;
    }

    public ProtectedRegion getRegion() {
        return region;
    }

    public double getRating() {
        return rating;
    }
}
