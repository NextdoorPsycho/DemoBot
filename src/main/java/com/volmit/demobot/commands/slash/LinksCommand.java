package com.volmit.demobot.commands.slash;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.volmit.demobot.Demo;
import com.volmit.demobot.util.VolmitEmbed;

import java.awt.*;

public class LinksCommand extends SlashCommand {

    public LinksCommand() {
        this.name = "links"; // This has to be lowercase
        this.help = "Links Info";
        this.category = new Category("General"); // This is where the command will show up in the help menu
    }

    @Override
    public void execute(SlashCommandEvent event) {
        // Sends a "<bot> is thinking..." response and allows you a delayed response.
        Demo.info("Links ARE everything");
        VolmitEmbed embed = new VolmitEmbed("Volmit Software: Your Ultimate Resource Hub");
        embed.setColor(Color.WHITE);
        embed.setDescription("Explore our resources to maximize your experience with our products.");

//Commands
        embed.addField(":book: **WIKI LINKS**:", "", false);

        embed.addField("**Adapt - Leveling, Skills and Abilities**:", """
                :scroll: Documentation: [Adapt Docs](https://docs.volmit.com/adapt/)
                SpigotMC Shop: [Adapt Resource](https://www.spigotmc.org/resources/adapt-leveling-skills-and-abilities.103790/)
                Polymart Updates: [Adapt Resource](https://polymart.org/resource/adapt-leveling-skills-abilities.3622/updates)
                """, false);
        embed.addField("**React - Performance Tuning**:", """
                :scroll: Documentation: [React Docs](https://docs.volmit.com/react/)
                :exclamation: Not Yet Available for Purchase! Please stay tuned for updates.
                """, false);
        embed.addField("**Iris - Dimension Engine**:", """
                :scroll: Documentation: [Iris Docs](https://docs.volmit.com/iris/)
                SpigotMC Shop: [Iris Resource](https://www.spigotmc.org/resources/iris-dimension-engine.84586/)
                Polymart Updates: [Iris Resource](https://polymart.org/resource/iris-dimension-engine.3623)
                """, false);
        embed.addField("**HoloUI - Immersive UIs, Cameras, and Holograms**:", """
                :scroll: Documentation: [HoloUi Docs](https://docs.volmit.com/holoui/)
                :exclamation: Not Yet Available for Purchase! Please stay tuned for updates.
                """, false);
        embed.addField("**OTHER LINKS**:", "", false);
        embed.addField("**Support Us On Patreon**:", """
                :sparkling_heart: [Volmit Patreon](https://www.patreon.com/volmitsoftware)
                """, false);
        embed.addField("**Discord Community Vanity URL**:", """
                - [Discord Vanity Link](https://discord.gg/volmit) 
                """, false);
        embed.setFooter("For more assistance, contact our support team. We're here to help!");
        event.getChannel().sendMessageEmbeds(embed.build()).queue(m -> event.reply("Links sent!").setEphemeral(true).queue());
    }
}