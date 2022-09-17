package com.volmit.demobot.commands.slash;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.volmit.demobot.util.VolmitEmbed;

public class LinkCommand extends SlashCommand {

    public LinkCommand() {
        this.name = "links"; // This has to be lowercase
        this.help = "Sends a list of links to the user";
        this.category = new Category("Volmit"); // This is where the command will show up in the help menu
    }

    @Override
    public void execute(SlashCommandEvent event) {
        VolmitEmbed embed = new VolmitEmbed(" Here you go!");
        //Commands
        embed.addField("**WIKI LINKS**:", """
                **Adapt:**
                *https://volmitsoftware.gitbook.io/adapt/*
                *https://www.spigotmc.org/resources/adapt-leveling-skills-and-abilities.103790/*
                **Iris:**
                *https://volmitsoftware.gitbook.io/iris/*
                *https://www.spigotmc.org/resources/iris-world-gen-custom-biome-colors.84586/*
                """, false);
        embed.addField("**OTHER LINKS**:", """
                **Patreon:**
                *https://www.patreon.com/volmitsoftware*
                **Discord Links:**
                *https://discord.gg/volmit*
                *https://canary.discord.com/channels/189665083817852928/770736450558754817/847589978694877194*
                """, false);
        event.replyEmbeds(embed.build()).setEphemeral(false).queue();
    }
}