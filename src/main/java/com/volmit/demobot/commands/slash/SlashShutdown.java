package com.volmit.demobot.commands.slash;


import com.jagrosh.jdautilities.examples.command.PingCommand;
import com.volmit.demobot.Core;
import com.volmit.demobot.Demo;
import net.azzerial.slash.SlashClient;
import net.azzerial.slash.SlashClientBuilder;
import net.azzerial.slash.annotations.Slash;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;


@Slash.Tag("shutdown_command")
@Slash.Command(
        name = "shutdown",
        description = "Attempt to Stop the program"
)
public class SlashShutdown {

    public static void main(String[] args){
        final SlashClient slash = SlashClientBuilder.create(Demo.getJDA())
                .addCommand(new PingCommand()) // register the ping command
                .build();

        slash.getCommand("ping_command") // get the ping command by it's @Slash.Tag
                .upsertGuild(Core.get().discordID); // upsert it as a guild Slash Command
    }


    @Slash.Handler()
    public void callback(SlashCommandInteractionEvent e) {
        e.deferReply()
                .setContent("pong!")
                .queue();
    }

}
