package com.volmit.demobot.commands.slash;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.volmit.demobot.util.VolmitEmbed;

public class LogCommand extends SlashCommand {

    public LogCommand() {
        this.name = "log"; // This has to be lowercase
        this.help = "Explains hot to send logs";
        this.category = new Category("Volmit"); // This is where the command will show up in the help menu
    }

    @Override
    public void execute(SlashCommandEvent event) {
        VolmitEmbed embed = new VolmitEmbed("**WHAT IS A LOG?**");
        embed.setDescription("This message was sent because we are asking for a log, and you don't know how to get one, or sent something that is not a log. *If you are worried about privacy you can have a private thread for support, just ask the support team and we can get that setup for you!*");
        embed.addField("*__Why do we ask for Logs__*",
                """
                        **1:** So we can see what  the actual problem is.
                        **2:**  So we can check what parts are failing
                        **3:**  To see Versions for Java, Server, And Plugins
                        **4:**  Other Reasons""", false);
        embed.addField("What **__NOT__** to do",
                """
                        **-**  Send us a snippet of error codes, you probably don't know why we ask.
                        **-**  Send images, people on phones cant read it
                        **-**  Removing information You don't know more about the errors than we do""", false);
        embed.addField("How to get a log",
                """
                        **-**  The `latest.log`  file from your server's Log folder
                        **-**  Go to <https://pastebin.com/> And paste that file there.
                        **-**  Alternatively <https://mclo.gs/> and paste it there.
                        **-**  Or just send the file""", false);
        //Commands
        //embed.addField("Name Here", "" + "Value here", false);
        event.replyEmbeds(embed.build()).setEphemeral(false).queue();
    }
}