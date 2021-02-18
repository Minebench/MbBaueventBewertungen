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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class ConfigAccessor {

    private final String fileName;
    protected final Plugin plugin;

    private File configFile;
    private FileConfiguration fileConfiguration;
    private FileConfiguration defaults;

    public ConfigAccessor(Plugin plugin, String fileName) {
        if (plugin == null)
            throw new IllegalArgumentException("plugin cannot be null");
        this.plugin = plugin;
        this.fileName = fileName;
        File dataFolder = plugin.getDataFolder();
        if (dataFolder == null)
            throw new IllegalStateException();
        this.configFile = new File(plugin.getDataFolder(), fileName);
        this.defaults = getDefaults();
        saveDefaultConfig();
        reloadConfig();
    }

    public void reloadConfig() {
        fileConfiguration = YamlConfiguration.loadConfiguration(configFile);

        if (defaults != null) {
            fileConfiguration.setDefaults(defaults);
        }
    }

    public FileConfiguration getConfig() {
        if (fileConfiguration == null) {
            if (defaults != null) {
                return defaults;
            } else {
                return new YamlConfiguration();
            }
        }
        return fileConfiguration;
    }

    public FileConfiguration getDefaults() {
        if (defaults == null) {
            // Look for defaults in the jar
            InputStream defConfigStream = plugin.getResource(fileName);
            if (defConfigStream != null) {
                return YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
            }
        }
        return defaults;
    }

    public void saveConfig() {
        if (fileConfiguration != null && configFile != null) {
            try {
                getConfig().save(configFile);
            } catch (IOException ex) {
                plugin.getLogger().log(Level.SEVERE, "Could not save data to " + configFile, ex);
            }
        }
    }

    public void saveDefaultConfig() {
        if (!configFile.exists()) {
            this.plugin.saveResource(fileName, false);
        }
    }

}
