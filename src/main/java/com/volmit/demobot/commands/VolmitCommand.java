package com.volmit.demobot.commands;


import com.volmit.demobot.Core;
import com.volmit.demobot.Demo;
import com.volmit.demobot.util.VolmitEmbed;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.*;
import java.util.stream.Collectors;

public class VolmitCommand extends ListenerAdapter {

    @Getter
    private final String name;
    @Getter
    private final List<String> commands;
    @Getter
    private final List<String> roles;
    @Getter
    private final String description;
    @Getter
    private final boolean needsArguments;
    @Getter
    private final String example;
    @Getter
    private final String category;
    @Getter
    private final List<VolmitCommand> subcommands;

    public VolmitCommand(String name, List<String> commands, List<String> roles, String description, boolean needsArguments, String example) {
        this.name = name;
        this.commands = Optional.ofNullable(commands).orElse(Collections.singletonList(name));
        this.roles = Optional.ofNullable(roles).orElse(Collections.emptyList());
        this.description = !description.isEmpty() ? description : "This command has no description";
        this.needsArguments = needsArguments;
        this.example = example;
        this.category = null;
        this.subcommands = null;
    }

    public VolmitCommand(String name, List<String> commands, List<String> roles, String description, boolean needsArguments, String category, List<VolmitCommand> subcommands) {
        this.name = name;
        this.commands = Optional.ofNullable(commands).orElse(Collections.singletonList(name));
        this.roles = Optional.ofNullable(roles).orElse(Collections.emptyList());
        this.description = !description.isEmpty() ? description : "This command has no description";
        this.needsArguments = needsArguments;
        this.example = null;
        this.category = category;
        this.subcommands = Optional.ofNullable(subcommands).orElse(Collections.emptyList());
    }

    public void handle(List<String> args, MessageReceivedEvent e) {
        e.getMessage().reply("The command you ran is improperly written. The handle() must be overwritten.").complete();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (e.getAuthor().isBot()) return;
        if (noPermission(Objects.requireNonNull(e.getMember()).getRoles(), e.getAuthor().getId())) return;
        List<String> args = new LinkedList<>(Arrays.asList(e.getMessage().getContentRaw().replace(Core.get().botPrefix, "").split(" ")));
        List<String> argc = new LinkedList<>(Arrays.asList(e.getMessage().getContentRaw().split(" ")));
        if (!argc.get(0).contains(Core.get().botPrefix)) return;
        if (!checkCommand(args.get(0))) return;
        continueToHandle(args, e);
    }

    public void continueToHandle(List<String> args, MessageReceivedEvent e) {
        if (!getRoles().isEmpty()) {
            if (noPermission(Objects.requireNonNull(e.getMember()).getRoles(), e.getAuthor().getId())) return;
        }
        Demo.info("Command passed checks: " + getName());
        if (!needsArguments) {
            handle(null, e);
        } else if (getCategory() != null) {
            e.getMessage().delete().queue();
            if (args.size() < 2) {
                sendCategoryHelp(e.getMessage());
            } else {
                String subs = getSubcommands().stream()
                        .map(VolmitCommand::getName)
                        .collect(Collectors.joining(" "));
                Demo.info("Subs: " + subs);
                boolean subCommandFound = getSubcommands().stream()
                        .flatMap(sub -> sub.getCommands().stream())
                        .anyMatch(commandAlias -> commandAlias.equalsIgnoreCase(args.get(1)));
                if (!subCommandFound) {
                    return;
                }
            }
        } else if (args.size() < 2) {
            sendHelp(e.getMessage());
        } else {
            Demo.info("Final command. Running: " + getName());
            handle(args, e);
        }
    }

    private boolean noPermission(List<Role> roles, String ID) {
        if (!getRoles().isEmpty()) {
            return roles.stream()
                    .map(Role::getName)
                    .noneMatch(userRoleName -> getRoles().contains(userRoleName) || ID.equals(userRoleName));
        }
        return false;
    }

    private boolean checkCommand(String command) {
        return command.equalsIgnoreCase(name) || getCommands().stream().anyMatch(cmd -> command.equalsIgnoreCase(cmd));
    }

    public void sendHelp(Message message) {
        VolmitEmbed embed = new VolmitEmbed(Core.get().botPrefix + getName() + " Command Usage", message);
        embed.setFooter("All Non-SubCommands are prefaced with the prefix: `" + Core.get().botPrefix + "`");
        String cmd = getName().substring(0, 1).toUpperCase() + getName().substring(1);
        if (getCommands().size() < 2) {
            embed.addField(cmd, "`*no aliases*`\n" + getDescription(), true);
        } else {
            String aliases = getCommands().subList(1, getCommands().size())
                    .toString()
                    .replace("[", "").replace("]", "");
            embed.addField(cmd, "\n`" + Core.get().botPrefix + aliases + "`\n" + getDescription(), true);
        }
        if (getExample() != null) {
            embed.addField("**Usage**", "`" + Core.get().botPrefix + getExample() + "`", false);
        }
        if (!getRoles().isEmpty()) {
            embed.addField("**Permitted for role(s)**", "`" + getRoles().toString() + "`", false);
        }
        embed.setFooter(Core.get().botCompany, Core.get().botIMG);
        embed.send(message);
    }

    protected void sendCategoryHelp(Message message) {
        VolmitEmbed embed = new VolmitEmbed(getName() + " Command Usage", message);
        String menuName = getName();
        getSubcommands().forEach(command -> {
            String cmd = Core.get().botPrefix + menuName + " " + command.getName().substring(0, 1).toUpperCase() + command.getName().substring(1);

            if (command.getCommands().size() < 2) {
                embed.addField(cmd, "`*no aliases*`\n" + command.getDescription(), true);
            } else {
                String body =
                        "\n`" +
                                command.getCommands().subList(1, command.getCommands().size())
                                        .toString().replace("[", "").replace("]", "") +
                                "`\n" +
                                command.getDescription() +
                                (command.getExample() != null ? "\n**usage:**\n`" + Core.get().botPrefix + command.getExample() + "`" : "");
                embed.addField(cmd, body, true);
            }
        });
        embed.setFooter(Core.get().botCompany, Core.get().botIMG);
        embed.send(message);
    }
}