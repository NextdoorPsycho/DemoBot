package com.volmit.demobot.listeners.slash;

import com.volmit.demobot.Demo;
import com.volmit.demobot.util.VolmitEmbed;
import com.volmit.demobot.util.io.data.User;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.Date;
import java.util.Random;

public class TicketMasterButton extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent e){
        if (e.getChannel().getName().equals("ticket-hub")) {
            e.getMessage().delete().queue();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent e) {
        if (e.getButton().getId().equals("create-ticket")) {
            Member m = e.getMember();
            User u = Demo.getLoader().getUser(m.getUser().getIdLong());
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


                Random random = new Random();
                int randomNumber = random.nextInt(999999);
                g.createTextChannel("ticket-" + randomNumber, ticketCategory).queue(chat -> {
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
                u.ticketIds().add(randomNumber);
                Demo.info("Created ticket for " + m.getUser().getName() + " with id :" + randomNumber);
            }
        } else if (e.getButton().getId().equals("close-ticket")) {
            Member m = e.getMember();
            User u = Demo.getLoader().getUser(m.getUser().getIdLong());
            Guild g = m.getGuild();

            u.ticketIds().forEach(id -> {
                if (e.getChannel().getName().contains(id.toString()) /*|| is Admin Check*/) {
                    //TODO : Iterate through all messages and create a ticket log

                    e.getChannel().delete().queue( deletion -> {
                        u.ticketIds().remove(id);
                        Demo.info("Closed ticket for " + m.getUser().getName() + " ID: " + id);
                    });

                } else {
                    System.out.println(id);
                    System.out.println("ticket-" + id);
                    System.out.println(g.getTextChannelsByName("ticket-" + id, true).get(0).getId());
                }
            });


        }


    }

}
