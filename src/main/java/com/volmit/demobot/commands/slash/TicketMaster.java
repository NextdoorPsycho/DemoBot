package com.volmit.demobot.commands.slash;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.volmit.demobot.util.VolmitEmbed;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class TicketMaster extends SlashCommand {

    public TicketMaster() {
        this.name = "ticket-master";
        this.help = "This is the command to start the creation of a ticket hub";

        List<OptionData> options = new ArrayList<>();

        options.add(new OptionData(OptionType.STRING, "title", "The Title of the Embed Message.").setRequired(true));
        options.add(new OptionData(OptionType.STRING, "description", "What would you like to say in the Embed?").setRequired(true));

        this.options = options; // Add options to a List<OptionData>
    }

    @Override
    public void execute(SlashCommandEvent e) {
        OptionMapping title = e.getOption("title"); // string must match the name we set above for the option.
        OptionMapping description = e.getOption("description"); // string must match the name we set above for the option.
//        e.deferReply().queue();


        EmbedBuilder embed = new VolmitEmbed();
        embed.setTitle(title.getAsString());
        embed.setTimestamp(new Date().toInstant());
        embed.setDescription(description.getAsString());
        Button button = Button.danger("create-ticket", "[\u2800 \u2800 \u2800 \u2800 Click for a ticket! \u2800 \u2800 \u2800 \u2800]");

        e.getChannel().sendMessageEmbeds(embed.build()).setActionRow(button).queue();
        e.reply("Ticket created!").setEphemeral(true).queue();
    }
}