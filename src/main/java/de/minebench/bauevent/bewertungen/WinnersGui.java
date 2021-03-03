package de.minebench.bauevent.bewertungen;

/*
 * bewertungen
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
import com.sk89q.worldguard.util.profile.Profile;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.UUID;

public class WinnersGui {

    public WinnersGui(MbBaueventBewertungen plugin, Player player) {

        List<Placement> winnerRegions = plugin.getWinnerRegions(player.getWorld());

        int amount = winnerRegions.size();

        if (amount == 0) {
            player.sendMessage(plugin.getComponents(player, "no-region"));
            return;
        }

        int filler = (9 - amount) / 2;

        String[] setup;
        if (false) {
            setup = new String[] {
                    "         ",
                    " ".repeat(filler) + "r".repeat(amount) + " ".repeat(amount + 2 * filler == 9 ? filler : filler + 1),
                    "         "
            };
        } else {
            setup = new String[amount];
            int i = 0;
            for (Placement cell : winnerRegions) {
                if (cell.getPlace() < 4) {
                    setup[i] = String.valueOf(cell.getPlace()).repeat(4) + "r" + String.valueOf(cell.getPlace()).repeat(4);
                } else {
                    setup[i] = "    r    ";
                }
                i++;
            }
        }

        InventoryGui gui = new InventoryGui(plugin, plugin.getMessage(player, "gui.winners.title", "world", player.getWorld().getName()), setup);
        gui.setFiller(new ItemStack(Material.GRAY_STAINED_GLASS_PANE));

        GuiElementGroup group = new GuiElementGroup('r');

        int i = 0;
        for (Placement cell : winnerRegions) {
            i++;
            if (i >= 9) {
                break;
            }
            if (cell.getPlace() < 4) {
                gui.addElement(new StaticGuiElement(String.valueOf(cell.getPlace()).charAt(0), new ItemStack(getMaterial(cell.getPlace())), " "));
            }
            String owners = cell.getRegion().getOwners().toPlayersString(plugin.getWorldGuard().getProfileCache()).replace("*", "");
            if (owners.length() > 35) {
                owners = owners.substring(0, 32) + "...";
            }

            ItemStack icon = new ItemStack(Material.DIRT);
            if (!cell.getRegion().getOwners().getUniqueIds().isEmpty()) {
                UUID firstOwner = cell.getRegion().getOwners().getUniqueIds().iterator().next();
                Profile profile = plugin.getWorldGuard().getProfileCache().getIfPresent(firstOwner);
                if (profile != null && profile.getName() != null) {
                    icon = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta meta = (SkullMeta) icon.getItemMeta();
                    PlayerProfile playerProfile = plugin.getServer().createProfile(profile.getUniqueId(), profile.getName());
                    meta.setPlayerProfile(playerProfile);
                    icon.setItemMeta(meta);
                }
            }
            group.addElement(new StaticGuiElement('r', icon, click -> {
                if (click.getType() == ClickType.LEFT) {
                    plugin.teleportToRegion((Player) click.getWhoClicked(), cell.getRegion(), "gui.winners.teleported");
                }
                return true;
            }, plugin.getMessage(player, "gui.winners.entry",
                    "owners", owners,
                    "id", cell.getRegion().getId(),
                    "rating", String.format("%.2f", cell.getRating()),
                    "place", String.valueOf(cell.getPlace())
            )));
        }

        gui.addElement(group);

        gui.show(player);
    }

    private Material getMaterial(int place) {
        switch (place) {
            case 1:
                return Material.GOLD_INGOT;
            case 2:
                return Material.IRON_INGOT;
            case 3:
                return Material.OAK_PLANKS;
            default:
                return Material.GRAY_STAINED_GLASS_PANE;
        }
    }
}
