package com.volmit.demobot.listeners.slash;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;


public class TicketMaster extends SlashCommand {

    public TicketMaster() {
        this.name = "ticketmaster";
        this.help = "This is the command to start the creation of a ticket hub";

        List<OptionData> options = new ArrayList<>();
        this.options = options; // Add options to a List<OptionData>
    }

    @Override
    public void execute(SlashCommandEvent e) {

        categorySetup(e.getGuild());
        e.reply("Ill make it for you!").queue();

    }

    public void categorySetup(Guild g) {

        net.dv8tion.jda.api.entities.Category category = null;
        for (net.dv8tion.jda.api.entities.Category c : g.getCategories()) {
            if (c.getName().equals("Tickets")) {
                category = c;
            }
        }
        if (category == null) {
            g.createCategory("Tickets").queue(t -> g.createTextChannel("ticket-hub", t)
                    .queue(TicketMasterButton::makeTicketEmbedMessage));
        }
        //TODO: Permissions setup for the category
    }

}

