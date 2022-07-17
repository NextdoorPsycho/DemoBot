
package com.volmit.demobot.commands;


import com.volmit.demobot.Demo;
import com.volmit.demobot.Core;
import com.volmit.demobot.util.VolmitEmbed;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;


import java.util.*;


public class VolmitCommand extends ListenerAdapter {

    @Getter
    public String name;
    @Getter
    public List<String> commands;
    @Getter
    public List<String> roles;
    @Getter
    public String description;
    @Getter
    public boolean isSlashCommand;
    @Getter
    public boolean needsArguments;
    @Getter
    public String example;
    @Getter
    public String category;
    @Getter
    public List<VolmitCommand> subcommands;

    // Creator sets name, command aliases, requires any one of entered roles, and adds a description and example
    public VolmitCommand(String name, String[] commands, String[] roles, String description, boolean needsArguments, String example) {
        if (commands == null || commands.length == 0) commands = new String[]{name};
        if (roles == null) roles = new String[]{};
        this.name = name;
        this.commands = Arrays.asList(commands);
        this.roles = Arrays.asList(roles);
        this.description = !description.equals("") ? description : "This command has no description";
        this.needsArguments = needsArguments;
        this.example = example;
        this.category = null;
        this.subcommands = null;
    }



    public VolmitCommand(String name, String[] commands, String[] roles, String description, boolean needsArguments, String category, VolmitCommand[] subcommands) {
        if (commands == null || commands.length == 0) commands = new String[]{name};
        if (roles == null) roles = new String[]{};
        this.name = name;
        this.commands = Arrays.asList(commands);
        this.roles = Arrays.asList(roles);
        this.description = !description.equals("") ? description : "This command has no description";
        this.needsArguments = needsArguments;
        this.example = null;
        this.category = category;
        this.subcommands = Arrays.asList(subcommands);
    }

    // Override me!
    public void handle(List<String> args, MessageReceivedEvent e) {
        e.getMessage().reply("The command you ran is improperly written. The handle() must be overwritten.").complete();
    }



    public void onMessageReceived(MessageReceivedEvent e) {
        if (e.getAuthor().isBot()) return;
        if (noPermission(Objects.requireNonNull(e.getMember()).getRoles(), e.getAuthor().getId())) return;
        List<String> args = new LinkedList<>(Arrays.asList(e.getMessage().getContentRaw().replace(Core.get().botPrefix, "").split(" ")));
        List<String> argc = new LinkedList<>(Arrays.asList(e.getMessage().getContentRaw().split(" ")));
        if (!argc.get(0).contains(Core.get().botPrefix)) return; // ignore
        if (!checkCommand(args.get(0))) return;
        continueToHandle(args, e);
    }

    // Handle
    public void continueToHandle(List<String> args, MessageReceivedEvent e) {
        if (getRoles() != null && getRoles().size() != 0) {
            if (noPermission(Objects.requireNonNull(e.getMember()).getRoles(), e.getAuthor().getId())) return;
        }
        Demo.info("Command passed checks: " + getName());
        if (!needsArguments) {
            handle(null, e);
        } else if (getCategory() != null) {
            e.getMessage().delete().queue(); // delete the sent message
            if (args.size() < 2) {
                sendCategoryHelp(e.getMessage());
            } else {
                StringBuilder subs = new StringBuilder("Subs: ");
                for (VolmitCommand cmd : getSubcommands()) subs.append(cmd.getName()).append((" "));
                Demo.info(subs.toString());
                for (VolmitCommand sub : getSubcommands()) {
                    for (String commandAlias : sub.getCommands()) {
                        if (commandAlias.equalsIgnoreCase(args.get(1))) {
                            return;
                        }
                    }
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
        if (getRoles() != null && getRoles().size() != 0) {
            for (Role userRole : roles) {
                String userRoleName = userRole.getName();
                for (String needsRole : getRoles()) {
                    if (needsRole.equals(userRoleName)) {
                        return false;
                    }
                }
                if (ID.equals(userRole.getName())) {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }
    private boolean checkCommand(String command) {
        if (command.equalsIgnoreCase(name)) return true;
        for (String cmd : getCommands()) {
            if (command.equalsIgnoreCase(cmd)) {
                return true;
            }
        }
        return false;
    }
    public void sendHelp(Message message) {
        VolmitEmbed embed = new VolmitEmbed(Core.get().botPrefix + getName() + " Command Usage", message);
        embed.setFooter("All Non-SubCommands are prefaced with the prefix: `" + Core.get().botPrefix + "`");
        String cmd = /*Kit.get().BotPrefix +*/ getName().substring(0, 1).toUpperCase() + getName().substring(1);
        if (getCommands().size() < 2) {
            embed.addField(cmd, "`*no aliases*`\n" + getDescription(), true);
        } else {
            embed.addField(
                    cmd,
                    "\n`" + Core.get().botPrefix +
                            (getCommands().size() == 2 ?
                                    getCommands().get(1) :
                                    " " + getCommands().subList(1, getCommands().size()).toString()
                                            .replace("[", "").replace("]", "")) +
                            "`\n" + getDescription(),
                    true
            );
        }
        if (getExample() != null) {
            embed.addField("**Usage**", "`" + Core.get().botPrefix + getExample() + "`", false);
        }
        if (getRoles() != null && getRoles().size() != 0) {
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
                                (command.getCommands().size() == 2 ?
                                        command.getCommands().get(1) :
                                        " " + command.getCommands().subList(1, command.getCommands().size()))
                                        .replace("[", "").replace("]", "") +
                                "`\n" +
                                command.getDescription() +
                                (command.getExample() != null ? "\n**usage:**\n`" + Core.get().botPrefix + command.getExample() + "`" : "");
                embed.addField(
                        cmd,
                        body,
                        true
                );
            }
        });
        embed.setFooter(Core.get().botCompany, Core.get().botIMG);
        embed.send(message);
    }
}