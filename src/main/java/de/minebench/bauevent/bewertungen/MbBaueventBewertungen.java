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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.themoep.minedown.MineDown;
import de.themoep.utils.lang.bukkit.LanguageManager;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public final class MbBaueventBewertungen extends JavaPlugin {
    private LanguageManager lang;
    private WorldGuard wg;

    private final Multimap<String, String> rateableRegions = MultimapBuilder.hashKeys().hashSetValues().build();
    private final Map<UUID, Bewertung> bewertungen = new HashMap<>();
    private ConfigAccessor bewertungsConfig;

    @Override
    public void onEnable() {
        ConfigurationSerialization.registerClass(Bewertung.class);
        wg = WorldGuard.getInstance();
        bewertungsConfig = new ConfigAccessor(this, "bewertungen.yml");
        loadConfig();
        getCommand("bewertung").setExecutor(this);
    }

    private void loadConfig() {
        saveDefaultConfig();
        reloadConfig();
        lang = new LanguageManager(this, getConfig().getString("language"));

        rateableRegions.clear();
        ConfigurationSection rateableRegionsSection = getConfig().getConfigurationSection("rateable-regions");
        if (rateableRegionsSection != null) {
            for (String world : rateableRegionsSection.getKeys(false)) {
                rateableRegions.putAll(world, rateableRegionsSection.getStringList(world));
            }
        }

        bewertungen.clear();
        bewertungsConfig.reloadConfig();
        ConfigurationSection bewertungsSection = bewertungsConfig.getConfig().getConfigurationSection("bewertungen");
        if (bewertungsSection != null) {
            for (String key : bewertungsSection.getKeys(false)) {
                Object value = bewertungsSection.get(key);
                if (value instanceof Bewertung) {
                    bewertungen.put(((Bewertung) value).getPlayerId(), (Bewertung) value);
                } else {
                    getLogger().log(Level.WARNING, "Invalid value specified for " + key + " in bewertungen.yml! " + value);
                }
            }
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 0) {
            if ("reload".equalsIgnoreCase(args[0]) && sender.hasPermission("mbbb.command.reload")) {
                loadConfig();
                sender.sendMessage(getComponents(sender, "reloaded"));
                return true;
            } else if ("admin".equalsIgnoreCase(args[0]) && sender.hasPermission("mbbb.command.admin")) {

                return true;
            }
        } else if (sender instanceof Player) {
            Bewertung bewertung = bewertungen.get(((Player) sender).getUniqueId());
            if (bewertung == null) {
                bewertung = new Bewertung(((Player) sender).getUniqueId(), sender.getName());
                bewertungen.put(((Player) sender).getUniqueId(), bewertung);
            }

            RegionManager manager = wg.getPlatform().getRegionContainer().get(BukkitAdapter.adapt(((Player) sender).getWorld()));
            if (manager == null) {
                sender.sendMessage(getComponents(sender, "no-region"));
                return true;
            }

            boolean teleport = false;
            ProtectedRegion toRate = null;

            ApplicableRegionSet regions = manager.getApplicableRegions(BukkitAdapter.adapt(((Player) sender).getLocation()).toVector().toBlockPoint());
            for (ProtectedRegion region : regions.getRegions()) {
                if (canRate(((Player) sender).getWorld(), bewertung, region, true)) {
                    toRate = region;
                    break;
                }
            }

            if (toRate == null && bewertung.getLastViewed() != null) {
                toRate = manager.getRegion(bewertung.getLastViewed());
            }

            if (toRate == null || bewertung.getRegions().containsKey(toRate.getId()) || !isRateable(((Player) sender).getWorld(), toRate)) {
                bewertung.setLastViewed(null);
                toRate = getNewRegion((Player) sender, bewertung);
                if (toRate == null) {
                    sender.sendMessage(getComponents(sender, "rated-all"));
                    return true;
                }
                teleport = true;
            }

            if (toRate.getOwners().contains(((Player) sender).getUniqueId()) || toRate.getMembers().contains(((Player) sender).getUniqueId())) {
                sender.sendMessage(getComponents(sender, "cant-rate-own"));
                return true;
            }

            bewertung.setLastViewed(toRate.getId());
            if (teleport) {
                teleportToRegion((Player) sender, toRate);
            } else {
                new RatingGui(this, (Player) sender, bewertung, toRate);
            }
            return true;
        }
        return false;
    }

    void teleportToNewRegion(Player player, Bewertung bewertung) {
        ProtectedRegion region = getNewRegion(player, bewertung);
        if (region == null) {
            player.sendMessage(getComponents(player, "rated-all"));
            return;
        }
        bewertung.setLastViewed(region.getId());
        teleportToRegion(player, region);
    }

    private void teleportToRegion(Player player, ProtectedRegion region) {
        com.sk89q.worldedit.util.Location location = region.getFlag(Flags.TELE_LOC);
        if (location == null) {
            location = new com.sk89q.worldedit.util.Location(BukkitAdapter.adapt(player.getWorld()),
                    region.getMaximumPoint().add(region.getMaximumPoint().subtract(region.getMinimumPoint())).toVector3())
                    .setYaw(player.getEyeLocation().getYaw())
                    .setPitch(player.getEyeLocation().getPitch());
        }
        com.sk89q.worldedit.util.Location finalLocation = location;
        player.teleportAsync(BukkitAdapter.adapt(location)).thenAccept((success) -> {
            if (success) {
                player.sendMessage(getComponents(player, "teleported", "owners", region.getOwners().toPlayersString(wg.getProfileCache())));
            } else {
                player.sendMessage("Error teleporting to " + finalLocation);
            }
        });
    }

    private boolean canRate(World world, Bewertung bewertung, ProtectedRegion region, boolean ignoreOwn) {
        return region.getOwners().size() > 0
                && !bewertung.getRegions().containsKey(region.getId())
                && isRateable(world, region)
                && (ignoreOwn
                        || (!region.getOwners().contains(bewertung.getPlayerId())
                                && !region.getMembers().contains(bewertung.getPlayerId())));
    }

    private ProtectedRegion getNewRegion(Player player, Bewertung bewertung) {
        RegionManager manager = wg.getPlatform().getRegionContainer().get(BukkitAdapter.adapt(player.getWorld()));
        if (manager == null) {
            return null;
        }
        List<String> regions = new ArrayList<>(rateableRegions.get(player.getWorld().getName()));
        regions.removeAll(bewertung.getRegions().keySet());
        Collections.shuffle(regions);
        for (String regionId : regions) {
            ProtectedRegion region = manager.getRegion(regionId);
            if (region != null && canRate(player.getWorld(), bewertung, region, false)) {
                return region;
            }
        }
        return null;
    }

    private boolean isRateable(World world, ProtectedRegion region) {
        return rateableRegions.get(world.getName()).contains(region.getId());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public BaseComponent[] getComponents(CommandSender sender, String key, String... replacements) {
        return MineDown.parse(lang.getConfig(sender).get(key), replacements);
    }

    public String getMessage(CommandSender sender, String key, String... replacements) {
        return TextComponent.toLegacyText(getComponents(sender, key, replacements));
    }

    public WorldGuard getWorldGuard() {
        return wg;
    }

    public void save(Bewertung bewertung) {
        bewertungsConfig.getConfig().set("bewertungen." + bewertung.getPlayerId().toString(), bewertung);
        bewertungsConfig.saveConfig();
    }
}
