package com.volmit.demobot.commands.slash;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.volmit.demobot.util.VolmitEmbed;

public class PasteServicesCommand extends SlashCommand {

    public PasteServicesCommand() {
        this.name = "paste-services"; // This has to be lowercase
        this.help = "This sends all the possible locations supported for logs";
        this.category = new Category("Volmit"); // This is where the command will show up in the help menu
    }

    @Override
    public void execute(SlashCommandEvent event) {
        VolmitEmbed embed = new VolmitEmbed("Raw Code Alternative!");
        embed.setDescription("Generally speaking you can paste your code however you want, however its hard for some of us to read it when we are on our phones, or anything like that, using a paste service will help us help you\n**AND IF YOU PASTE ONE OF THE BOT SUPPORTED LINKS, THE BOT WILL SCAN FOR KNOWN PROBLEMS**");
        embed.addField("Possible Paste Sites!", "" +
                "https://pastebin.com/ `512kb`\n" +
                "https://hastebin.com/ `400kb`\n" +
                "https://gist.github.com/ `100mb`(need GitHub, **Free**)\n" +
                "https://mclo.gs/ `2mb`, Hides IP's/sensitive info\n", false);
        event.replyEmbeds(embed.build()).setEphemeral(false).queue();
    }
}