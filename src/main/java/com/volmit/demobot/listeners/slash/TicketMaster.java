package com.volmit.demobot.listeners.slash;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.volmit.demobot.util.VolmitEmbed;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.Date;
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
            g.createCategory("Tickets").queue( t -> g.createTextChannel("ticket-hub", t)
                    .queue( temp -> createTicketEmbed(g)));
        }
        //TODO: Permissions setup for the category
    }

    public void createTicketEmbed(Guild g ) {
        EmbedBuilder embed = new VolmitEmbed();
        embed.setTitle("Welcome to the Ticket center!");
        embed.setTimestamp(new Date().toInstant());
        embed.setDescription("If you want to create a ticket, all you need to do is click the button below!\n" +
                "If you make a ticket you will be given a custom chat and space to talk probate to you, " +
                "and the people who are able to help with tickets. you can always do !close to close the " +
                "tickets, or press t he button! that's created when you get the chat!");
        Button button = Button.success("create-ticket", "[\u2800 \u2800 \u2800 \u2800 Click for a Ticket\u2800 \u2800 \u2800 \u2800]");
        g.getTextChannelsByName("ticket-hub", true).get(0)
                .sendMessageEmbeds(embed.build())
                .setActionRow(button)
                .queue();
    }

}

