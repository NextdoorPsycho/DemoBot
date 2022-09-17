package com.volmit.demobot.commands.slash;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.volmit.demobot.util.VolmitEmbed;

public class ChunkyStatement extends SlashCommand {

    public ChunkyStatement() {
        this.name = "chunky"; // This has to be lowercase
        this.help = "Sends the Stance on the Chunky Pregenerator";
        this.category = new Category("Volmit"); // This is where the command will show up in the help menu
    }

    @Override
    public void execute(SlashCommandEvent event) {
        VolmitEmbed embed = new VolmitEmbed("Chunky or Iris Pregen");
        embed.setDescription("""
                Iris comes with a built-in world generator(Using the core generation code from Chunky). The generator that we feature is designed to replace Chunky in Iris worlds. You can of course still use Chunky on vanilla worlds, as it may perform better there. However, Using Chunky in an iris world may cause problems, Chunky is not at fault for this,  but this is why we suggest not using it for iris worlds, as its already implemented as needed. Plus, Iris' pregenerator makes use of the MCA world file system that Minecraft uses, and writes directly to those files so the worlds can be generated much more quickly.
        """);
        event.replyEmbeds(embed.build()).setEphemeral(false).queue();
    }
}