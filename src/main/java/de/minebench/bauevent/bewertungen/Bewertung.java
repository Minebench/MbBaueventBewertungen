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

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@SerializableAs("Bewertung")
public class Bewertung implements ConfigurationSerializable {
    private final UUID playerId;
    private final String playerName;
    private String lastViewed = null;
    private final Map<String, Integer> regions = new HashMap<>();

    public Bewertung(UUID playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Map<String, Integer> getRegions() {
        return regions;
    }

    public String getLastViewed() {
        return lastViewed;
    }

    public void setLastViewed(String lastViewed) {
        this.lastViewed = lastViewed;
    }

    public static Bewertung deserialize(Map<String, Object> map) {
        if (!map.containsKey("player-id") || !map.containsKey("player-name")) {
            throw new IllegalArgumentException("Missing player id or name!");
        }
        Bewertung bewertung = new Bewertung(UUID.fromString((String) map.get("player-id")), (String) map.get("player-name"));
        if (map.get("last-viewed") != null) {
            bewertung.lastViewed = (String) map.get("last-viewed");
        }
        if (map.get("regions") instanceof Map) {
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) map.get("regions")).entrySet()) {
                if (entry.getValue() instanceof Integer) {
                    bewertung.regions.put(entry.getKey(), (Integer) entry.getValue());
                } else if (entry.getValue() instanceof String) {
                    bewertung.regions.put(entry.getKey(), Integer.parseInt((String) entry.getValue()));
                } else {
                    throw new IllegalArgumentException("Value of region " + entry.getKey() + " is not a valid integer!");
                }
            }
        }
        return bewertung;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("player-id", playerId.toString());
        map.put("player-name", playerName);
        map.put("last-viewed", lastViewed);
        map.put("regions", regions);
        return map;
    }
}
