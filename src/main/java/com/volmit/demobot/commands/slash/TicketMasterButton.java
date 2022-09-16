package com.volmit.demobot.commands.slash;

import art.arcane.quill.execution.J;
import com.volmit.demobot.Core;
import com.volmit.demobot.Demo;
import com.volmit.demobot.util.VolmitEmbed;
import com.volmit.demobot.util.io.data.User;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class TicketMasterButton extends ListenerAdapter {


    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (e.getChannel().getName().equals("ticket-hub") && !e.getAuthor().isBot()) {
            e.getMessage().delete().queue();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent e) {
        if (e.getButton().getId().equals("create-ticket")) {
            createTicket(e);
        } else if (e.getButton().getId().equals("close-ticket")) {
            closeTicket(e);
        }
    }


    public void createTicketLog(ButtonInteractionEvent e, String ticketId) throws IOException {
        Member m = e.getMember();
        Guild g = m.getGuild();
        TextChannel channel = g.getTextChannelsByName("ticket-" + ticketId, true).get(0);

        String jarPath = System.getProperty("java.io.tmpdir") + "ticket-" + ticketId + ".txt";
        File path = new File(jarPath);
        PrintWriter printWriter = new PrintWriter(path);

        MessageHistory history = channel.getHistoryFromBeginning(100).complete();
        List<Message> msgList = new ArrayList<>(history.getRetrievedHistory());
        Collections.reverse(msgList);

        printWriter.append("[  THIS IS A PRINTOUT OF YOUR WHOLE TICKET  ]\n \n");
        for (Message msg : msgList) {
            net.dv8tion.jda.api.entities.User author = msg.getAuthor();
            String content = msg.getContentRaw();
            if (author.isBot()) {
                continue;
            } else {
                printWriter.append(author.getName() + ": \n" + content + "\n \n");
            }
        }
        printWriter.flush();
        printWriter.close();


        m.getUser().openPrivateChannel().queue(channel1 -> channel1.sendMessage("Your ticket has been logged!").queue());
        m.getUser().openPrivateChannel().complete().sendFile(new java.io.File(jarPath)).queue();
        e.reply("Ticket closed!").setEphemeral(true).queue();
        if (path.delete()) {
            Demo.info("Deleted file: " + jarPath);
        }


    }

    public void createTicket(ButtonInteractionEvent e) {
        Member m = e.getMember();
        User u = Demo.getLoader().getUser(m.getUser().getIdLong());
        User botData = Demo.getLoader().getUser(1000000001);
        Guild g = m.getGuild();

        //TODO: permissions check here
        if (u.roleIds().size() > 0) {
            //TODO: Add a message saying that the user is already in a ticket
        } else {


            net.dv8tion.jda.api.entities.Category ticketCategory = null;
            for (net.dv8tion.jda.api.entities.Category c : g.getCategories()) {
                if (c.getName().equals("Tickets")) {
                    ticketCategory = c;
                }
            }
            if (ticketCategory == null) {
                Demo.warn("Ticket category not found!");
                return;
            }
            botData.reactions(botData.reactions() + 1);

            String ticketNumber = String.format("%04d", botData.reactions());
            g.createTextChannel("ticket-" + ticketNumber, ticketCategory).queue(chat -> {
                EmbedBuilder embed = new VolmitEmbed();
                embed.setTitle("Welcome to your ticket!");
                embed.setTimestamp(new Date().toInstant());
                embed.setDescription("Welcome to your own personal dimension for issues and problems.\n" +
                        "If you want to close the ticket, just click the close button right below this message!\n " +
                        "it's pinned, so you can always come back");
                Button button = Button.danger("close-ticket", "[ Click to close Ticket ]");
                chat.sendMessage(m.getAsMention() + " has been added to the ticket!").queue();
                chat.sendMessageEmbeds(embed.build()).setActionRow(button).queue(msg -> msg.pin().queue());
            });
            u.ticketIds().add(ticketNumber);
            Demo.info("Created ticket for " + m.getUser().getName() + " with id :" + ticketNumber);
        }
        botData.money(botData.money() + 1);
        remakeEmbedMessage(m.getGuild().getTextChannelsByName("ticket-hub", true).get(0));
        e.reply("Ticket created!").setEphemeral(true).queue();
    }


    private void closeTicket(ButtonInteractionEvent e) {
        Member m = e.getMember();
        User botData = Demo.getLoader().getUser(1000000001);
        User u = Demo.getLoader().getUser(m.getUser().getIdLong());
        u.ticketIds().forEach(id -> {
            if (e.getChannel().getName().contains(id) /*|| is Admin Check*/) {
                try {
                    createTicketLog(e, id);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                e.getChannel().delete().queue(d -> {
                    u.ticketIds().remove(id);
                    Demo.info("Closed ticket for " + m.getUser().getName() + " ID: " + id);
                });
            }
        });
        botData.money(botData.money() - 1);
        remakeEmbedMessage(m.getGuild().getTextChannelsByName("ticket-hub", true).get(0));
    }


    public static void makeTicketEmbedMessage(TextChannel textChannel) {
        int ticketCount = textChannel.getGuild().getCategoriesByName("Tickets", true).get(0).getTextChannels().size() - 1;
        User botData = Demo.getLoader().getUser(1000000001);

        EmbedBuilder embed = new VolmitEmbed();
        embed.setTitle("Welcome to the Ticket center!");
        embed.setTimestamp(new Date().toInstant());
        embed.setDescription("If you want to create a ticket, all you need to do is click the button below!\n" +
                "If you make a ticket you will be given a custom chat and space to talk private with you, " +
                "and the people who are able to help with tickets. you can always do !close to close the " +
                "tickets, or press the button! that's created when you get the chat!");
        embed.setFooter("Current Tickets: " + (int) botData.money(), Core.get().botIMG);
        Button button = Button.success("create-ticket", "[\u2800 \u2800 \u2800 \u2800 Click for a Ticket\u2800 \u2800 \u2800 \u2800]");
        textChannel.sendMessageEmbeds(embed.build())
                .setActionRow(button)
                .queue(m -> m.pin().queue());
    }

    public static void remakeEmbedMessage(TextChannel textChannel) {
        MessageHistory history = textChannel.getHistoryFromBeginning(100).complete();
        List<Message> msgList = new ArrayList<>(history.getRetrievedHistory());
        for (Message msg : msgList) {
            msg.delete().queue();
        }
        J.a(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
            makeTicketEmbedMessage(textChannel);
        });

    }
}
