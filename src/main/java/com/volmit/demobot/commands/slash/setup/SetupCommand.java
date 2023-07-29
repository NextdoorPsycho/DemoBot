package com.volmit.demobot.commands.slash.setup;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.volmit.demobot.Demo;
import com.volmit.demobot.util.VolmitEmbed;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SetupCommand extends SlashCommand {

    public SetupCommand() {
        this.name = "setup"; // This has to be lowercase
        this.help = "/setup <Blacklisted Roles> <Plugin Channels> <Mod Channels> [Verify Section]";
        this.category = new Category("Development");
        this.guildOnly = true;
        this.ownerCommand = true;

        // Roles will be in the format: "role1,role2,role3..."
        // Channels will be in the format: "chan1,chan2,chan3..."
        // Plugins&Mods will have an Announcements channel, chat, and a Forum channel.
    }

    // /setup <Blacklisted Roles> <Required Channels> [Verify]
    // 1: Check if roles exist, if not, create them [X]
    // 2: Check if plugins exist, if not, create them (Chans and Subvars) [X]
    // 2: Check if mods exist, if not, create them (* Perms) (Chans and Subvars) [X]
    // 3: Check if categories exist, if not, create them (Plugins, and Mods: Volmit - <ID>) [X]
    // 4: Check if channels are in categories, if not, move them [X *]
    // 5: Remove ALL ROLES from ALL USERS (Excluding those in "roles" Variable, if this is the case, Verify Manually), Then add role "Unverified" to all other users  [X]
    // 6: Send Verify Message in "Verify" Channel (With a button that allows users to see chats etc...) []
    // 7: After Verify, remove role "Unverified" and add role "Verified" to user []
    // 8: Send DM to user with "Welcome" Message, and inform them that they are able to get notified when any plugin is updated (none by default) []


    @Override
    public void execute(SlashCommandEvent e) {
        roleExistanceCheck(e, List.of("React", "Adapt", "Iris", "News", "HoloUi"));
        //Make the embed that will be sent to in the chat
        VolmitEmbed embed = new VolmitEmbed();

        // Set the title of the embed
        embed.setTitle(":bell: Notification Role Assignment!");

        // Describe the purpose of the embed in detail
        embed.setDescription("Hello, there! This is your one-stop solution for server notification roles. By assigning these roles, you will stay updated with all the happenings and announcements. Let's get you sorted!");

        // Add fields to the embed
        embed.addField(":gear: **Plugins**", "We've developed a set of unique plugins designed to optimize your server performance and provide an exceptional gaming experience!", false);
        embed.addField(":joystick: **Adapt**", "Adapt is a plugin developed in-house, and It's similar to MCMMO, enhancing your gaming with role-playing elements. Adapt seamlessly integrates with your server for an enriched experience.", false);
        embed.addField(":earth_americas: **Iris**", "Iris isn't just a world generator. It's a whole new perspective on world creation, management, and editing in Minecraft. It's designed with modern technology and modern Studio tools.", false);
        embed.addField(":gem: **React**", "React is a Smart Server Performance plugin. It works tirelessly in the background, optimizing server resources, solving performance issues, and ensuring a smooth gaming experience for players on your server.", false);
        embed.addField(":camera_with_flash: **HoloUi**", "HoloUi is a new dimension of player interactivity, providing clean, performant user interface with infinite customizability.", false);
        embed.addField(":scroll: **News**", "Whenever We make an announcement, you'll be the first to know! We'll keep you updated with all the latest news and announcements.", false);
        embed.addField(":potato: :anger: **NONE**", "I don't want to be notified about anything!", false);


        // Set the author of the embed
        embed.setAuthor("Volmit Software", "https://volmit.com", "https://cdn.discordapp.com/icons/189665083817852928/a_77255398f32f79495ec85ddaccbf39fd.gif?size=4096");

        // Set the color of the embed
        embed.setColor(Color.WHITE);

        // Set the footer of the embed
        embed.setFooter("Volmit Software, Please wait for roles to be assigned!", "https://volmit.com/img/logo.png");

        // Send the embed to the channel
        e.getChannel().sendMessageEmbeds(embed.build()).queue(m -> {
            m.reply("Below are the abilities to get the Pings for the chosen plugins.").addActionRow(
                    StringSelectMenu.create("notif-select")
                            .addOption("Adapt Notifications", "adaptnotif", "I want to hear when Adapt updates!")
                            .addOption("React Notifications", "reactnotif", "I want to hear when React updates!")
                            .addOption("Iris Notifications", "irisnotif", "I want to hear when Iris updates!")
                            .addOption("HoloUi Notifications", "holouinotif", "I want to hear when HoloUi updates!")
                            .addOption("News Notifications", "newsnotif", "Any kind of notification!")
                            .addOption("All Notifications", "allnotif", "I want to hear when anything updates!")
                            .addOption("None Notifications", "nonenotif", "I want to hear NOTHING, DO NOT PING ME!")
                            .setMinValues(1)
                            .setMaxValues(6)
                            .build()
            ).queue();
        });

    }


    private boolean checkIfRoleExists(String roleName, SlashCommandEvent event) {
        return event.getGuild().getRolesByName(roleName, true).size() > 0;
    }

    private boolean checkIfChannelExists(String channelName, SlashCommandEvent event) {
        return event.getGuild().getChannels(true).stream().anyMatch(channel -> channel.getName().equalsIgnoreCase(channelName));
    }

    private boolean checkIfForumExists(String channelName, SlashCommandEvent event) {
        return event.getGuild().getForumChannels().stream().anyMatch(channel -> channel.getName().equalsIgnoreCase(channelName));
    }

    private boolean checkIfCategoryExists(String categoryName, SlashCommandEvent event) {
        return event.getGuild().getCategoriesByName(categoryName, true).size() > 0;
    }

    private boolean checkIfChannelIsInCategory(String channelName, String categoryName, SlashCommandEvent event) {
        return event.getGuild().getCategoriesByName(categoryName, true).get(0).getChannels().stream().anyMatch(channel -> channel.getName().equalsIgnoreCase(channelName));
    }

    private boolean checkIfUserHasRole(Role roleName, SlashCommandEvent event) {
        return event.getMember().getRoles().contains(roleName);
    }

    private void roleExistanceCheck(SlashCommandEvent event, List<String> roles) {
        for (String role : roles) {
            if (!checkIfRoleExists(role + " - Notify", event))
                event.getGuild().createRole().setName(role + " - Notify").setColor(Color.RED).queue();
            Demo.info("Ensured " + role);
        }
    }


    private void removeAllRolesBlanket(SlashCommandEvent event, List<String> roleBlacklists) {
        for (Member member : event.getGuild().getMembers()) {
            for (Role role : new ArrayList<>(member.getRoles())) {
                boolean isRoleInBlacklist = false;

                for (String roleBlacklist : roleBlacklists) {
                    if (role.getName().equalsIgnoreCase(roleBlacklist)) {
                        isRoleInBlacklist = true;
                        break;
                    }
                }

                if (!isRoleInBlacklist) {
                    event.getGuild().removeRoleFromMember(member, role).queue();
                    Demo.info("Removed " + role.getName() + " from " + member.getEffectiveName());
                }
            }
            // add verified if they have more than 1 role after the blacklist check
            if (member.getRoles().size() > 1) {
                event.getGuild().addRoleToMember(member, event.getGuild().getRolesByName("Verified", true).get(0)).queue();
                Demo.info("Ensured Verification to " + member.getEffectiveName());
            }
        }
    }


    private void addRoleBlanket(SlashCommandEvent event, List<Role> roles) {
        for (int i = 0; i < event.getGuild().getMembers().size(); i++) {
            Member member = event.getGuild().getMembers().get(i);
            for (Role role : roles) {
                if (!member.getRoles().contains(role))
                    event.getGuild().addRoleToMember(member, role).queue();

                Demo.info("Ensured " + role.getName() + " to " + member.getEffectiveName());
            }
        }
    }


}