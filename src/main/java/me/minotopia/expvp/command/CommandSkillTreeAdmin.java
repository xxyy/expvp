/*
 * This file is part of Expvp,
 * Copyright (c) 2016-2016.
 *
 * This work is protected by international copyright laws and licensed
 * under the license terms which can be found at src/main/resources/LICENSE.txt.
 */

package me.minotopia.expvp.command;

import com.sk89q.intake.Command;
import com.sk89q.intake.Require;
import com.sk89q.intake.parametric.annotation.Validate;
import li.l1t.common.intake.provider.annotation.Colored;
import li.l1t.common.intake.provider.annotation.ItemInHand;
import li.l1t.common.intake.provider.annotation.Merged;
import me.minotopia.expvp.command.service.SkillTreeCommandService;
import me.minotopia.expvp.skill.tree.SkillTree;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

/**
 * A command that provides tools for creation, deletion and modification of skill trees.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-07-23
 */
public class CommandSkillTreeAdmin extends YamlManagerCommandBase<SkillTree> {

    @Override
    public String getObjectTypeName() {
        return "Skilltree";
    }

    @Command(aliases = "new", min = 2,
            desc = "Erstellt neuen Skilltree",
            help = "Erstellt einen neuen Skilltree\nDie Id besteht " +
                    "dabei aus\nZahlen, Buchstaben und Bindestrichen\nund ist eindeutig.",
            usage = "[id] [name...]")
    @Require("expvp.admin")
    public void newSkill(SkillTreeCommandService service, CommandSender sender,
                         @Validate(regex = "[a-zA-Z0-9\\-]+") String id,
                         @Merged @Colored String name)
            throws IOException {
        createNew(service, sender, id, name);
    }

    @Command(aliases = "name", min = 2,
            desc = "Ändert den Namen",
            usage = "[id] [name...]")
    @Require("expvp.admin")
    public void editName(SkillTreeCommandService service, CommandSender sender,
                         SkillTree tree, @Merged @Colored String name)
            throws IOException {
        service.changeName(tree, name, sender);
    }

    @Command(aliases = "icon", min = 2,
            desc = "Ändert das Icon",
            help = "Ändert das Icon auf das\nItem in deiner Hand",
            usage = "[id]")
    @Require("expvp.admin")
    public void editIcon(SkillTreeCommandService service, CommandSender sender,
                         SkillTree tree, @ItemInHand ItemStack itemInHand)
            throws IOException {
        service.changeIconStack(tree, itemInHand, sender);
    }

    @Command(aliases = "iconslot", min = 2,
            desc = "Ändert den Iconslot",
            help = "Ändert den Slot, in dem\ndas Icon dieses Trees\nim Übersichtsinventar ist.",
            usage = "[id] [Slot-ID]")
    @Require("expvp.admin")
    public void editSlotId(SkillTreeCommandService service, CommandSender sender,
                         SkillTree tree, int slotId)
            throws IOException {
        service.changeSlotId(tree, slotId, sender);
    }

    @Command(aliases = "branches-exklusive", min = 2,
            desc = "Setzt Branches Exclusive",
            help = "Setzt, ob sich die Äster\ndes Baums gegenseitig\nausschließen.\nDas bedeutet,\n" +
                    "wenn man einen erforscht hat,\nkann man den Nachbar\nnicht mehr erforschen. (true)",
            usage = "[id] [true|false]")
    @Require("expvp.admin")
    public void editBranchesExclusive(SkillTreeCommandService service, CommandSender sender,
                         SkillTree tree, boolean branchesExclusive)
            throws IOException {
        service.changeBranchesExclusive(tree, branchesExclusive, sender);
    }

    @Command(aliases = "info", min = 1,
            desc = "Zeigt Infos zu einem Skilltree",
            usage = "[id]")
    @Require("expvp.admin")
    public void skillInfo(SkillTreeCommandService service, CommandSender sender,
                         SkillTree tree)
            throws IOException {
        sender.sendMessage(String.format("§e➩ §lSkilltree: §6%s §e(ID: %s§e)",
                tree.getDisplayName(), tree.getId()));
        sender.sendMessage(String.format("§e➩ §lBranches Exclusive (siehe /sta help): §6%s",
                tree.areBranchesExclusive()));
        sender.sendMessage(String.format("§e➩ §lSlot-Id in der Übersicht: §6%s",
                tree.getSlotId()));
        sender.sendMessage(String.format("§e➩ §lIcon: %s",
                tree.getIconStack() == null ? "§cnein" : tree.getIconStack()));
    }

    //TODO: /ska handlers
}