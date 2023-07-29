package com.volmit.demobot.commands.slash;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.volmit.demobot.Demo;
import com.volmit.demobot.util.VolmitEmbed;

import java.awt.*;

public class LogCommand extends SlashCommand {

    public LogCommand() {
        this.name = "log"; // This has to be lowercase
        this.help = "Logging Info";
        this.category = new Category("General"); // This is where the command will show up in the help menu
    }

    @Override
    public void execute(SlashCommandEvent event) {
        // Sends a "<bot> is thinking..." response and allows you a delayed response.
        Demo.info("Compliance is everything");
        VolmitEmbed embed = new VolmitEmbed(":mag: Server Log Assistance & Code Sharing Guidelines");
        embed.setColor(Color.WHITE);

        embed.setDescription("This guide is designed to assist you in providing effective server logs and sharing your code for a better support experience. *If you are concerned about privacy, please request a private support thread. We're happy to accommodate!*");
        embed.addField(":scroll: **What Is a Server Log?**", """
                A server log is a file that documents everything happening in your server's environment. These logs are crucial for troubleshooting as they provide us with detailed context about the issues you're encountering.
                """, false);
        embed.addField(":bulb: **Why Do We Ask for Logs?**", """
                **1.** To diagnose the actual problem.
                **2.** To identify failing components.
                **3.** To verify Java, server, and plugin versions.
                **4.** For other necessary context that we could spend 45 minutes explaining to you but please just save us all time and send the damn log.
                """, false);
        embed.addField(":warning: **What NOT to Do When Sharing Logs**", """
                **-** Don't just send us an error snippet. It lacks the broader context we need.
                **-** Avoid sending images of logs. They can be unreadable on some devices.
                **-** Don't remove any information. We understand these logs better than anyone.
                """, false);
        embed.addField(":file_folder: **How to Obtain and Share a Log**", """
                **1.** Locate the `latest.log` file in your server's Log folder.
                **2.** Visit a paste service like [Pastebin](https://pastebin.com/) or [MCLogs](https://mclo.gs/) and paste your log file content there.
                **3.** Alternatively, you can simply send us the `latest.log` file directly.
                """, false);
        embed.addField(":computer: **Sharing Code Effectively**", """
                Properly formatted logs/untouched/FULL logs are crucial for readability and effective support. Please consider using the following paste services to share your code. If you use a bot-supported link, the bot will scan for known issues.
                """, false);
        embed.addField(":link: **Recommended Paste Sites**", """
                - [Pastebin](https://pastebin.com/) `512kb (No account required)`
                - [Hastebin](https://hastebin.com/) `400kb (No account required)`
                - [GitHub Gist](https://gist.github.com/) `100mb` (Requires GitHub account, **Free**)
                - [MCLogs](https://mclo.gs/) `2mb` (Hides sensitive information, **Free**)
                """, false);

        embed.setFooter("For more assistance, contact our support team. We're here to help!");
        event.getChannel().sendMessageEmbeds(embed.build()).queue(m -> event.reply("Info sent!").setEphemeral(true).queue());


    }
}