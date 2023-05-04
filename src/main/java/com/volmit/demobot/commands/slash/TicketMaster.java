package com.volmit.demobot.commands.slash;


import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.volmit.demobot.Core;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TicketMaster extends SlashCommand {

    public TicketMaster() {
        this.name = "ticketmaster";
        this.help = "This is the command to start the creation of a ticket hub";

        List<OptionData> options = new ArrayList<>();
        this.options = options; // Add options to a List<OptionData>
        this.category = new Category("Tickets"); // This is where the command will show up in the help menu

    }

    @Override
    public void execute(SlashCommandEvent e) {
        if (e.getMember().getRoles().contains(e.getGuild().getRolesByName(Core.get().adminControllerRole, false).get(0))) {
            categorySetup(e.getGuild());
            e.reply("Ill make it for you!").setEphemeral(true).queue();
        } else {
            e.reply("You don't have permission to do this!").setEphemeral(true).queue();
        }
    }

    public void categorySetup(Guild g) {
        net.dv8tion.jda.api.entities.channel.concrete.Category category = null;
        for (net.dv8tion.jda.api.entities.channel.concrete.Category c : g.getCategories()) {
            if (c.getName().equals("Tickets")) {
                category = c;
            }
        }
        if (category == null) {
            g.createCategory("Tickets").queue(t -> {
                g.createTextChannel("ticket-hub", t)
                        .addRolePermissionOverride(g.getRolesByName(Core.get().adminControllerRole, false).get(0).getIdLong(), Collections.singleton(Permission.VIEW_CHANNEL), null)
                        .addRolePermissionOverride(g.getRolesByName(Core.get().supportControllerRole, false).get(0).getIdLong(), Collections.singleton(Permission.VIEW_CHANNEL), null)
                        .addRolePermissionOverride(g.getPublicRole().getIdLong(), null, Collections.singleton(Permission.MESSAGE_SEND))
                        .addRolePermissionOverride(g.getPublicRole().getIdLong(),  Collections.singleton(Permission.MESSAGE_ADD_REACTION), null)
                        .queue(TicketMasterButton::makeTicketEmbedMessage);
                g.createTextChannel("ticket-logs", t)
                        .addRolePermissionOverride(g.getRolesByName(Core.get().adminControllerRole, false).get(0).getIdLong(), Collections.singleton(Permission.VIEW_CHANNEL), null)
                        .addRolePermissionOverride(g.getRolesByName(Core.get().supportControllerRole, false).get(0).getIdLong(), Collections.singleton(Permission.VIEW_CHANNEL), null)
                        .addRolePermissionOverride(g.getPublicRole().getIdLong(), null, Collections.singleton(Permission.VIEW_CHANNEL))
                        .queue();

            });
        }
        //TODO: Permissions setup for the category
    }
}

