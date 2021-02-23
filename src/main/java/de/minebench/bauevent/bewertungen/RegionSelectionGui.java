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

import com.destroystokyo.paper.profile.PlayerProfile;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.util.profile.Profile;
import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class RegionSelectionGui {

    private static final String[] SETUP = {
            "rrrrrrrrr",
            "rrrrrrrrr",
            "rrrrrrrrr",
            "rrrrrrrrr",
            "rrrrrrrrr",
            "p       n"
    };

    public RegionSelectionGui(MbBaueventBewertungen plugin, Player player) {
        InventoryGui gui = new InventoryGui(plugin, plugin.getMessage(player, "gui.regions.title", "world", player.getWorld().getName()), SETUP);
        gui.setFiller(new ItemStack(Material.GRAY_STAINED_GLASS_PANE));

        GuiElementGroup group = new GuiElementGroup('r');

        RegionManager manager = plugin.getWorldGuard().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(player.getWorld()));
        if (manager != null) {
            for (ProtectedRegion region : manager.getRegions().values()) {
                if (region.getOwners().size() > 0) {
                    String owners = region.getOwners().toPlayersString(plugin.getWorldGuard().getProfileCache());

                    ItemStack icon = new ItemStack(Material.DIRT);
                    if (!region.getOwners().getUniqueIds().isEmpty()) {
                        UUID firstOwner = region.getOwners().getUniqueIds().iterator().next();
                        Profile profile = plugin.getWorldGuard().getProfileCache().getIfPresent(firstOwner);
                        if (profile != null && profile.getName() != null) {
                            icon = new ItemStack(Material.PLAYER_HEAD);
                            SkullMeta meta = (SkullMeta) icon.getItemMeta();
                            PlayerProfile playerProfile = plugin.getServer().createProfile(profile.getUniqueId(), profile.getName());
                            meta.setPlayerProfile(playerProfile);
                            icon.setItemMeta(meta);
                        }
                    }

                    ItemStack finalIcon = icon;
                    group.addElement(new DynamicGuiElement('r', viewer -> new StaticGuiElement('r', finalIcon, click -> {
                        if (click.getType() == ClickType.LEFT) {
                            plugin.toggleRateable(click.getWhoClicked(), player.getWorld().getName(), region.getId());
                            click.getGui().draw();
                        } else if (click.getType() == ClickType.RIGHT) {
                            plugin.teleportToRegion((Player) click.getWhoClicked(), region, "gui.regions.teleported");
                        }
                        return true;
                    }, plugin.getMessage(player, "gui.regions.entry." + (plugin.isRateable(player.getWorld(), region) ? "enabled" : "disabled"),
                            "owners", owners,
                            "id", region.getId()
                    ))));
                }
            }
        }

        gui.addElement(group);

        gui.addElement(new GuiPageElement('p', new ItemStack(Material.ARROW), GuiPageElement.PageAction.PREVIOUS, plugin.getMessage(player, "gui.regions.previous")));

        gui.addElement(new GuiPageElement('n', new ItemStack(Material.ARROW), GuiPageElement.PageAction.NEXT, plugin.getMessage(player, "gui.regions.next")));

        gui.show(player);
    }
}
