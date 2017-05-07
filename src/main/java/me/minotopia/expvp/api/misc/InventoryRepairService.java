/*
 * This file is part of Expvp,
 * Copyright (c) 2016-2017.
 *
 * This work is protected by international copyright laws and licensed
 * under the license terms which can be found at src/main/resources/LICENSE.txt.
 */

package me.minotopia.expvp.api.misc;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2017-05-05
 */
public class InventoryRepairService implements RepairService {

    @Override
    public void repair(Player player) {
        PlayerInventory inv = player.getInventory();
        inv.setContents(repairAll(inv.getContents()));
        inv.setArmorContents(repairAll(inv.getArmorContents()));
        player.updateInventory();
    }

    private ItemStack[] repairAll(ItemStack[] contents) {
        for (ItemStack item : contents) {
            if (item != null && isRepairable(item.getType())) {
                item.setDurability((short) 0);
            }
        }
        return contents;
    }

    private boolean isRepairable(Material material) {
        switch (material) {
            case LEATHER_HELMET:
            case LEATHER_CHESTPLATE:
            case LEATHER_LEGGINGS:
            case LEATHER_BOOTS:
            case WOOD_SWORD:
            case WOOD_SPADE:
            case WOOD_AXE:
            case BOW:
                return true;
            default:
                return false;
        }
    }
}