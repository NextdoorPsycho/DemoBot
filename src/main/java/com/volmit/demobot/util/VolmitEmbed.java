/*
 * Abyssalith is a Discord Bot for Volmit Software's Community
 * Copyright (c) 2021 VolmitSoftware (Arcane Arts)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.volmit.demobot.util;

import com.volmit.demobot.Demo;
import com.volmit.demobot.Core;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class VolmitEmbed extends EmbedBuilder {
    private final Message message;

    /*
        Creates a new default VolmitEmbed object.
        String `title` - the title of the embed
        String `name` - the name greeted in the top of the embed
     */
    public VolmitEmbed(String title, String name) {
        this.message = null;
        this.setAuthor("Requested by: " + name, null)
                .setTitle(!title.equals("") ? title : "\u200E")
                .setColor(Color.decode(Core.get().botColor))
                .setFooter(Core.get().botCompany, Core.get().botIMG);
    }

    /*
        Creates a new default VolmitEmbed object.
        String `title` - the title of the embed
        String `message` - the message used to greet the user with
     */
    public VolmitEmbed(String title, Message message) {
        this.message = message;
        this.setAuthor("Requested by: " + message.getAuthor().getName(), null, message.getAuthor().getAvatarUrl())
                .setTitle(!title.equals("") ? title : "\u200E")
                .setColor(Color.decode(Core.get().botColor))
                .setFooter("Made By: " + Core.get().botCompany, Core.get().botIMG);
    }

    /*
        Creates a new default VolmitEmbed object.
        String `title` - the title of the embed
     */
    public VolmitEmbed(String title) {
        this.message = null;
        this.setTitle(title)
                .setColor(Color.decode(Core.get().botColor))
                .setFooter(Core.get().botCompany, Core.get().botIMG);
    }

    /*
        Creates a new shortened VolmitEmbed object.
        String `title` - the title of the embed
        boolean `useShort` - toggles footer
     */
    public VolmitEmbed(String title, boolean useShort) {
        this.message = null;
        this.setTitle(title).setColor(Color.decode(Core.get().botColor));
        if (!useShort) {
            this.setFooter(Core.get().botCompany, Core.get().botIMG);
        }
    }

    /*
        Creates a new default VolmitEmbed object.
        This has no title (unless set later)
     */
    public VolmitEmbed() {
        this.message = null;
        this.setColor(Color.decode(Core.get().botColor))
                .setFooter(Core.get().botCompany, Core.get().botIMG);
    }
    // Send embed in the channel of the message already saved. Does not send if no message was specified.
    public void send() {
        this.send(this.message, null, false, 0);
    }
    // Send embed in the channel of the message already saved. Does not send if no message was specified. Adds reactions
    public void send(List<String> reactions) {
        this.send(this.message, null, false, 0, reactions);
    }
    // Send embed in `channel`
    public void send(MessageChannel channel) {
        this.send(null, channel, false, 0);
    }
    // Send embed in `channel` with reactions `reactions`
    public void send(MessageChannel channel, List<String> reactions) {
        this.send(null, channel, false, 0, reactions);
    }
    // Send embed in channel of `message`
    public void send(Message message) {
        this.send(message, false);
    }
    // Send embed in channel of `message` with reactions `reactions`
    public void send(Message message, List<String> reactions) {
        this.send(message, false, reactions);
    }
    // Send embed in channel of `message` and delete original if `deletesMSG`
    public void send(Message message, boolean deleteMSG) {
        this.send(message, deleteMSG, 0);
    }
    // Send embed in channel of `message` and delete original if `deletesMSG` with reactions `reactions`
    public void send(Message message, boolean deleteMSG, List<String> reactions) {
        this.send(message, deleteMSG, 0, reactions);
    }
    // Send embed in channel of `message` and delete original if `deleteMSG` after `deleteAfterMS`
    public void send(Message message, boolean deleteMSG, int deleteAfterMS) {
        this.send(message, null, deleteMSG, deleteAfterMS);
    }
    // Send embed in channel of `message` and delete original if `deleteMSG` after `deleteAfterMS` with reactions `reactions`
    public void send(Message message, boolean deleteMSG, int deleteAfterMS, List<String> reactions) {
        this.send(message, null, deleteMSG, deleteAfterMS, reactions);
    }
    // Send embed in channel of `message` (if null, send in `channel` instead), delete if `deleteMSG` after `deleteAfterMS`
    public void send(Message message, MessageChannel channel, boolean deleteMSG, int deleteAfterMS) {
        send(message, channel, deleteMSG, deleteAfterMS, null);
    }
    // Send embed in channel of `message` (if null, send in `channel` instead), delete if `deleteMSG` after `deleteAfterMS`, with reactions `reactions`
    public void send(Message message, MessageChannel channel, boolean deleteMSG, int deleteAfterMS, List<String> reactions) {
        if (reactions == null) reactions = new ArrayList<>();
        if (message == null && channel == null) {
            Demo.error("No channel and message specified.");
        } else if (message != null) {
            List<String> finalReactions = reactions;
            message.getChannel().sendMessageEmbeds(this.build()).queue(msg -> {
                for (String emoji : finalReactions) {
                    msg.addReaction(emoji).queue();
                }
            });
            if (deleteMSG) {
                message.delete().queueAfter(deleteAfterMS, TimeUnit.MILLISECONDS);
            }
        } else {
            List<String> finalReactions = reactions;
            channel.sendMessageEmbeds(this.build()).queue(msg -> {
                for (String emoji : finalReactions) {
                    msg.addReaction(emoji).queue();
                }
            });
        }
    }
}