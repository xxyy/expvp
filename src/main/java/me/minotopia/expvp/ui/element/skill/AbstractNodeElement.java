/*
 * This file is part of Expvp,
 * Copyright (c) 2016-2016.
 *
 * This work is protected by international copyright laws and licensed
 * under the license terms which can be found at src/main/resources/LICENSE.txt.
 */

package me.minotopia.expvp.ui.element.skill;

import com.google.common.base.Preconditions;
import li.l1t.common.inventory.gui.InventoryMenu;
import li.l1t.common.inventory.gui.element.CheckedMenuElement;
import li.l1t.common.util.inventory.ItemStackFactory;
import me.minotopia.expvp.skill.meta.Skill;
import me.minotopia.expvp.skill.meta.SkillManager;
import me.minotopia.expvp.skilltree.SimpleSkillTreeNode;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Abstract base class for menu elements that store their own instances of skill tree nodes.
 *
 * @author <a href="https://l1t.li/">Literallie</a>
 * @since 2016-08-18
 */
abstract class AbstractNodeElement<M extends InventoryMenu> extends CheckedMenuElement<InventoryMenu, M> {
    protected final SimpleSkillTreeNode node;

    AbstractNodeElement(Class<? extends M> menuType, SimpleSkillTreeNode node) {
        super(InventoryMenu.class, menuType);
        this.node = node;
    }

    public SimpleSkillTreeNode getNode() {
        return node;
    }

    SkillManager getSkillManagerFromValue() {
        Preconditions.checkNotNull(node.getValue(), "node.getValue()");
        SkillManager manager = node.getValue().getManager();
        Preconditions.checkNotNull(manager, "manager");
        return manager;
    }

    ItemStack drawRaw(String lore) {
        Skill skill = getNode().getValue();
        if (skill == null) {
            return new ItemStackFactory(Material.BARRIER)
                    .displayName("§7<Kein Skill>")
                    .produce();
        }
        ItemStackFactory factory = getSkillManagerFromValue().createRawSkillIconFor(skill, true);
        factory.lore(lore);
        return factory.produce();
    }
}
