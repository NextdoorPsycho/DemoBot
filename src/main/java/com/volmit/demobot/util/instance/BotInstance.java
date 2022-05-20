package com.volmit.demobot.util.instance;

import com.jagrosh.jdautilities.examples.command.PingCommand;
import com.volmit.demobot.Demo;
import com.volmit.demobot.Core;
import net.azzerial.slash.SlashClient;
import net.azzerial.slash.SlashClientBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.security.auth.login.LoginException;
import java.util.Objects;


public class BotInstance {

    private final JDA jda;
    public BotInstance(String s) throws LoginException, InterruptedException {
        if (Objects.equals(Core.get().botToken, "")) {
            Demo.error("YOU NEED TO GIVE A VALID BOT TOKEN");
            System.exit(0);
        }

        jda = JDABuilder.createDefault(s)
                .setChunkingFilter(ChunkingFilter.ALL) // enable member chunking for all guilds
                .setMemberCachePolicy(MemberCachePolicy.ALL) // ignored if chunking enabled
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .build().awaitReady();
        jda.getPresence().setStatus(OnlineStatus.IDLE);
        jda.getPresence().setActivity(Activity.watching("The Universe: .?"));
        Demo.info("Bot Instantiated");
    }

    public void close() {
        Demo.info("Terminating Bot Instance");
        jda.shutdown();
    }

    public JDA getJDA() {
        return jda;
    }
}
