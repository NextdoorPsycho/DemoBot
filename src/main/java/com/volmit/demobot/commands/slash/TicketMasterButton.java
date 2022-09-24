package com.volmit.demobot.commands.slash;


import art.arcane.quill.execution.J;
import com.volmit.demobot.Core;
import com.volmit.demobot.Demo;
import com.volmit.demobot.util.VolmitEmbed;
import com.volmit.demobot.util.io.data.User;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

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


    @SneakyThrows
    public void createTicketLog(ButtonInteractionEvent e, String ticketId) throws IOException {
        Member m = e.getMember();
        Guild g = m.getGuild();
        TextChannel channel = g.getTextChannelsByName("ticket-" + ticketId, true).get(0);

        File path = new File("Data/Tickets/ticket-" + ticketId + ".txt");
        path.getParentFile().mkdirs();
        PrintWriter printWriter = new PrintWriter(path);

        MessageHistory history = channel.getHistoryFromBeginning(100).complete();
        List<Message> msgList = new ArrayList<>(history.getRetrievedHistory());
        Collections.reverse(msgList);

        printWriter.append("[  THIS IS A PRINTOUT OF YOUR WHOLE TICKET  ]\n \n");
        Set<Member> members = new HashSet<>();
        for (Message msg : msgList) {
            net.dv8tion.jda.api.entities.User author = msg.getAuthor();
            String content = msg.getContentRaw();
            if (author.isBot()) {
                continue;
            } else {
                members.add(msg.getMember());
                printWriter.append(author.getName() + ": \n" + content + "\n \n");
            }
        }
        printWriter.flush();
        printWriter.close();
        e.getGuild().getTextChannelsByName("ticket-logs", true).get(0).sendFiles(FileUpload.fromData(path)).queue();

        for (Member mem : members) {
            mem.getUser().openPrivateChannel().queue(channel1 -> channel1.sendMessage("Your ticket has been logged!").queue());
            mem.getUser().openPrivateChannel().complete().sendFiles(FileUpload.fromData(path)).queue();
        }
        e.reply("Ticket closed!").setEphemeral(true).queue();
        if (path.delete()) {
            Demo.info("Deleted file: Tickets/ticket-" + ticketId + ".txt");
        }


    }

    public void createTicket(ButtonInteractionEvent e) {
        Member m = e.getMember();
        User u = Demo.getLoader().getUser(m.getUser().getIdLong());
        User botData = Demo.getLoader().getUser(1000000001);
        Guild g = m.getGuild();

        //TODO: permissions check here
        if (u.ticketIds().size() > 0) {
            e.reply("You already have a ticket!").setEphemeral(true).queue();
        } else {
            Category ticketCategory = null;
            for (Category c : g.getCategories()) {
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
            g.createTextChannel("ticket-" + ticketNumber, ticketCategory)
                    .addRolePermissionOverride(g.getRolesByName(Core.get().adminControllerRole, false).get(0).getIdLong(), Collections.singleton(Permission.VIEW_CHANNEL), null)
                    .addRolePermissionOverride(g.getRolesByName(Core.get().supportControllerRole, false).get(0).getIdLong(), Collections.singleton(Permission.VIEW_CHANNEL), null)
                    .addRolePermissionOverride(g.getPublicRole().getIdLong(), null, Collections.singleton(Permission.VIEW_CHANNEL))
                    .addPermissionOverride(m, Collections.singleton(Permission.VIEW_CHANNEL), null)
                    .queue(chat -> {
                        EmbedBuilder embed = new VolmitEmbed();
                        embed.setTitle("Welcome to your ticket!");
                        embed.setTimestamp(new Date().toInstant());
                        embed.setDescription(
                                "Welcome to your own personal dimension for Everything you need in Volmit in one place\n" +
                                        "If you want to close the ticket, just click the close button right below this message!\n " +
                                        "it's pinned, so you can always come back\n");
                        embed.addField("**Liability Notice**", "**__PLEASE NOTE THAT IF YOU ARE MAKING ANY MONEY RELATED QUERIES, OR TRANSACTIONS; WE ARE NOT " +
                                "RESPONSIBLE FOR ANYTHING THAT CAN HAPPEN, ALL TRANSACTIONS ARE DONE AT YOUR OWN RISK, AND " +
                                "VOLMIT WILL NOT BE HELD RESPONSIBLE FOR INTRAPERSONAL SALES OR TRANSACTIONS, WE ARE MERELY " +
                                "A MEDIUM WITH ZERO LIABILITY; AND WE WILL NOT BE HELD RESPONSIBLE FOR ANYTHING THAT HAPPENS.__**", false);

                        Button button = Button.danger("close-ticket", "[ Click to close Ticket ]");
                        chat.sendMessage(m.getAsMention() + " has been added to the ticket!").queue();
                        chat.sendMessageEmbeds(embed.build()).setActionRow(button).queue(msg -> msg.pin().queue());
                    });
            u.ticketIds().add(ticketNumber);
            Demo.info("Created ticket for " + m.getUser().getName() + " with id :" + ticketNumber);

            botData.money(botData.money() + 1);
            remakeEmbedMessage(m.getGuild().getTextChannelsByName("ticket-hub", true).get(0));
            e.reply("Ticket created!").setEphemeral(true).queue();
        }

    }


    private void closeTicket(ButtonInteractionEvent e) {
        Member m = e.getMember();
        User botData = Demo.getLoader().getUser(1000000001);
        TextChannel tc = e.getChannel().asTextChannel();
        String tcid = tc.getName().trim().replace("ticket-", "");
        User u = Demo.getLoader().getUser(m.getUser().getIdLong());


        if (u.ticketIds().contains(tcid) || (m.getRoles().contains(m.getGuild().getRolesByName("Administrator", false).get(0)) || m.getRoles().contains(m.getGuild().getRolesByName("Support", false).get(0)))) {
            try {
                createTicketLog(e, tcid);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            e.getChannel().delete().queue(d -> {
                for (Member mem : tc.getMembers()) {
                    User cUsers = Demo.getLoader().getUser(mem.getUser().getIdLong());
                    Demo.info("Removed: " + tcid + "from: " + mem);
                    cUsers.ticketIds().remove(tcid);
                }
                Demo.info("Closed ticket for " + m.getUser().getName() + " ID: " + tcid);
            });

            botData.money(botData.money() - 1);
            J.a(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                remakeEmbedMessage(m.getGuild().getTextChannelsByName("ticket-hub", true).get(0));
            });
        }

    }


    public static void makeTicketEmbedMessage(TextChannel textChannel) {
        User botData = Demo.getLoader().getUser(1000000001);

        EmbedBuilder embed = new VolmitEmbed();
        embed.setTitle("Welcome to the Ticket center!");
        embed.setTimestamp(new Date().toInstant());
        embed.setDescription("If you want to create a ticket, all you need to do is click the button below!\n" +
                "**TICKETS ARE FOR THE FOLLOWING:**\n" +
                "- Commissioned projects\n" +
                "- Private/Developer Support\n" +
                "- Reporting Illegal activity (Spam, Phishing etc...)\n" +
                "- Business Inquires\n" +
                "**TICKETS ARE NOT FOR THE FOLLOWING:**\n" +
                "- General Support (Use the chats for that)\n" +
                "- Plugin Support/Questions (Also Use Chats for that)\n" +
                "- General Chit Chat\n");
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
