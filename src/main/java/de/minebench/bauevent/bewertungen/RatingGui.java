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
import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.GuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Function;

public class RatingGui {

    private static final String[] SETUP = {
            "  sssss  ",
            "  bbbbb  ",
            "  zzzzz  ",
            "         ",
            "   a c   ",
            "         "
    };

    private int rating = 0;

    public RatingGui(MbBaueventBewertungen plugin, Player player, Bewertung bewertung, ProtectedRegion region) {
        String owners = region.getOwners().toPlayersString(plugin.getWorldGuard().getProfileCache()).replace("*", "");
        String shortOwners;
        if (owners.length() > 23) {
            shortOwners = owners.substring(0, 20) + "...";
        } else {
            shortOwners = owners;
        }
        InventoryGui gui = new InventoryGui(plugin, plugin.getMessage(player, "gui.rate.title", "owners", shortOwners), SETUP);
        gui.setFiller(new ItemStack(Material.GRAY_STAINED_GLASS_PANE));

        GuiElementGroup sGroup = new GuiElementGroup('s');
        GuiElementGroup group = new GuiElementGroup('b');
        GuiElementGroup zGroup = new GuiElementGroup('z');

        for (int i = 1; i < 6; i++) {
            int finalI = i;
            group.addElement(new DynamicGuiElement('b', viewer
                    -> new StaticGuiElement('b', new ItemStack(Material.NETHER_STAR), finalI, click -> {
                        if (rating != finalI) {
                            rating = finalI;
                            click.getGui().draw();
                        }
                        return true;
                    }, plugin.getMessage(player, "gui.rate.entry", "rating", String.valueOf(finalI), "owners", shortOwners))));
            Function<HumanEntity, GuiElement> createSelector = viewer -> {
                if (rating == finalI) {
                    return new StaticGuiElement('i', new ItemStack(Material.YELLOW_STAINED_GLASS_PANE), " ");
                } else {
                    return gui.getFiller();
                }
            };
            sGroup.addElement(new DynamicGuiElement('s', createSelector));
            zGroup.addElement(new DynamicGuiElement('z', createSelector));
        }

        gui.addElement(sGroup);
        gui.addElement(group);
        gui.addElement(zGroup);

        gui.addElement(new StaticGuiElement('a', new ItemStack(Material.RED_WOOL), click -> {
            click.getGui().close();
            return true;
        }, plugin.getMessage(player, "gui.rate.cancel", "owners", owners)));

        gui.addElement(new DynamicGuiElement('c', viewer -> {
            if (rating > 0) {
                return new StaticGuiElement('c', new ItemStack(Material.GREEN_WOOL), click -> {
                    bewertung.getRegions().put(region.getId(), rating);
                    bewertung.setLastViewed(null);
                    click.getGui().close();
                    player.sendMessage(plugin.getComponents(player, "rated", "owners", owners, "rating", String.valueOf(rating)));
                    plugin.save(bewertung);
                    plugin.teleportToNewRegion(player, bewertung);
                    return true;
                }, plugin.getMessage(player, "gui.rate.confirm", "owners", shortOwners, "rating", String.valueOf(rating)));
            } else {
                return gui.getFiller();
            }
        }));

        gui.show(player);
    }
}
