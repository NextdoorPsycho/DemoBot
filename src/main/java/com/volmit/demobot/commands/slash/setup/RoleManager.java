package com.volmit.demobot.commands.slash.setup;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Arrays;
import java.util.List;

public class RoleManager extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Guild g = event.getGuild();
        Member m = event.getMember();

        List<Role> roles = Arrays.asList(
                g.getRolesByName("Adapt - Notify", true).get(0),
                g.getRolesByName("React - Notify", true).get(0),
                g.getRolesByName("Iris - Notify", true).get(0),
                g.getRolesByName("News - Notify", true).get(0),
                g.getRolesByName("HoloUi - Notify", true).get(0)

        );

        // Assign all roles
        for (Role role : roles) {
            g.addRoleToMember(m, role).queue();
        }
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        Member m = event.getGuild().getMember(event.getMember());
        Guild g = event.getGuild();

        List<Role> roles = Arrays.asList(
                g.getRolesByName("Adapt - Notify", true).get(0),
                g.getRolesByName("React - Notify", true).get(0),
                g.getRolesByName("Iris - Notify", true).get(0),
                g.getRolesByName("News - Notify", true).get(0),
                g.getRolesByName("HoloUi - Notify", true).get(0)
        );

        // Remove all roles initially
        for (Role role : roles) {
            if (m.getRoles().contains(role)) {
                g.removeRoleFromMember(m, role).queue();
            }
        }

        for (String s : event.getValues()) {
            switch (s.toLowerCase()) {
                case "adaptnotif" -> g.addRoleToMember(m, g.getRolesByName("Adapt - Notify", true).get(0)).queue();
                case "reactnotif" -> g.addRoleToMember(m, g.getRolesByName("React - Notify", true).get(0)).queue();
                case "irisnotif" -> g.addRoleToMember(m, g.getRolesByName("Iris - Notify", true).get(0)).queue();
                case "holouinotif" -> g.addRoleToMember(m, g.getRolesByName("HoloUi - Notify", true).get(0)).queue();
                case "newsnotif" -> g.addRoleToMember(m, g.getRolesByName("News - Notify", true).get(0)).queue();
                case "allnotif" -> {
                    // Assign all roles
                    for (Role role : roles) {
                        g.addRoleToMember(m, role).queue();
                    }
                }
                case "none" -> {
                    for (Role role : roles) {
                        if (m.getRoles().contains(role)) {
                            g.removeRoleFromMember(m, role).queue();
                        }
                    }
                }
                default -> System.out.println("Invalid selection: " + s);
            }
        }

        event.reply("Your notification settings have been updated!").setEphemeral(true).queue();
    }
}
