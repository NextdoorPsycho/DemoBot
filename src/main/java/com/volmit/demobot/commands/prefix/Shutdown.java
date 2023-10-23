package com.volmit.demobot.commands.prefix;

import com.volmit.demobot.Core;
import com.volmit.demobot.Demo;
import com.volmit.demobot.commands.VolmitCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;


public class Shutdown extends VolmitCommand {

    // Constructor
    public Shutdown() {
        super(
                "stop",
                List.of("stop", "shutdown", "die", "kill", "end"),
                List.of(Core.get().adminControllerRole),
                "Stops the Bot boi",
                false,
                null
        );
    }

    // Handle
    @Override
    public void handle(List<String> args, MessageReceivedEvent e) {
        Demo.warn("Terminating the Bot");
        String oidcheck = e.getMessage().getAuthor().getId();
        if (oidcheck.equals(Core.get().botOwnerID)) {
            Demo.warn("KILLING BOT");
            Demo.shutdown();
        } else {
            e.getMessage().reply("uR noT my DAddY!").queue();
        }
    }
}
