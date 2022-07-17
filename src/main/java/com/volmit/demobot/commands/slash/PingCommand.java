package com.volmit.demobot.commands.slash;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

import java.util.concurrent.TimeUnit;

public class PingCommand extends SlashCommand {

    public PingCommand() {
        this.name = "ping"; // This has to be lowercase
        this.help = "Performs a ping to see the bot's delay";
        this.category = new Category("Development"); // This is where the command will show up in the help menu
    }

    @Override
    public void execute(SlashCommandEvent event) {
        // Sends a "<bot> is thinking..." response and allows you a delayed response.

        event.deferReply().queue(
                m -> m.editOriginal("Pong!").queueAfter(1, TimeUnit.SECONDS)
        );
    }
}