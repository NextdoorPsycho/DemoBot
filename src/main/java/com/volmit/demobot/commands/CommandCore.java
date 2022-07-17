package com.volmit.demobot.commands;

import com.volmit.demobot.Demo;
import com.volmit.demobot.Core;
import com.volmit.demobot.util.VolmitEmbed;
import com.volmit.demobot.util.instance.SkipCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;

@SkipCommand
public class CommandCore extends VolmitCommand {

    // Commands stored
    private VolmitCommand[] botCommands = null;

    // Constructor
    public CommandCore(JDA jda) {
        super(
                "Commands",
                new String[]{"commands", "?", "help"},
                new String[]{}, // Always permitted if empty. User must have at least one if specified.
                "Sends the command help page (this one) `",
                false,
                null
        );
        setCommands(processCMDs(jda));
    }

    // Handle
    @Override
    public void handle(List<String> args, MessageReceivedEvent e) {
        Demo.info("Command List Initialized");

        // Init embed
        VolmitEmbed embed = new VolmitEmbed("The Abyssalith - " + Core.get().botName + " Info Page!", e.getMessage());

        // Add explanation
        embed.addField(
                "All commands you can use",
                Core.get().botPrefix + "<command> for more help on the command",
                true
        );

        // Loop over and add all commands with their respective information
        for (VolmitCommand command : botCommands) {
            String cmd = command.getName().substring(0, 1).toUpperCase() + command.getName().substring(1);
            embed.setFooter("All Non-SubCommands are prefaced with the prefix: `" + Core.get().botPrefix + "`");
            if (command.getCommands().size() < 2) {
                embed.addField(cmd, "`*no aliases*`\n" + command.getDescription(), true);
            } else {
                StringBuilder body = new StringBuilder();
                body
                        .append("\n`")
                        .append(Core.get().botPrefix)
                        .append(
                                command.getCommands().size() == 2 ?
                                        command.getCommands().get(1) :
                                        "" + command.getCommands().subList(1, command.getCommands().size()).toString()
                                                .replace("[", "").replace("]", "")
                        )
                        .append("`\n")
                        .append(command.getDescription())
                        .append(command.getExample() != null ? "\n**Usage**\n" + command.getExample() : "");
                if (command.getRoles() != null && command.getRoles().size() != 0) {
                    if (command.getRoles().size() == 1) {
                        body.append("\n__Required:__ ").append(command.getRoles().get(0));
                    } else {
                        body.append("\n__Required:__ ").append(command.getRoles().toString()
                                .replace("[", "").replace("]", ""));
                    }
                }
                embed.addField(
                        cmd,
                        body.toString(),
                        true
                );
            }
        }

        // Send the embed
        embed.setFooter(Core.get().botCompany, Core.get().botIMG);
        embed.send(e.getMessage(), true, 1000);
    }

    /// Other functions
    // Sets the commands
    public void setCommands(List<VolmitCommand> commands) {
        botCommands = commands.toArray(new VolmitCommand[0]);
    }

    // Gets all listeners of the specified JDA
    public List<VolmitCommand> processCMDs(JDA jda) {
        List<VolmitCommand> foundCommands = new ArrayList<>();
        jda.getRegisteredListeners().forEach(c -> {
            //package with commands!
            if (c instanceof VolmitCommand && c.getClass().getPackageName().contains(".commands")) {
                foundCommands.add((VolmitCommand) c);
            }
        });
        foundCommands.add(this);
        return foundCommands;
    }
}
