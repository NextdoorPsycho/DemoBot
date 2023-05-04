package com.volmit.demobot;

import art.arcane.quill.execution.Looper;
import com.volmit.demobot.commands.ListenerRegistry;
import com.volmit.demobot.util.instance.BotProvider;
import com.volmit.demobot.util.instance.IBotProvider;
import com.volmit.demobot.util.io.DataLoader;
import com.volmit.demobot.util.io.storage.FileSystemStorageAccess;
import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.File;
import java.util.Objects;

public class Demo extends ListenerAdapter {
    public static final IBotProvider provider = new BotProvider();
    @Getter
    private static DataLoader loader;

    public static void main(String[] args) {
        optimizeCores();
        loader = new DataLoader(new FileSystemStorageAccess(new File("Data/BotData")));
        System.out.println("Initializing");

        initializeCore();
        ListenerRegistry.All(getJDA()); // ALL COMMANDS ARE HERE
        info("Registering Listeners");
        info("Bot Server: " + Core.get().discordGuildID + " | Server Name: " + Objects.requireNonNull(getJDA().getGuildById(Core.get().discordGuildID)).getName() + " | Bot Name: " + Core.get().botName);

        new Looper() {
            @Override
            protected long loop() {
                Core.tick();
                return 1000;
            }
        }.start();

        Runtime.getRuntime().addShutdownHook(new Thread(loader::close));
    }

    private static void initializeCore() {
        Core.get().botID = getJDA().getSelfUser().getIdLong();
        Core.get().botUser = getJDA().getUserById(Core.get().botID);
        Core.get().botName = Objects.requireNonNull(Core.get().botUser).getName();
    }

    private static void optimizeCores() {
        final int cores = Runtime.getRuntime().availableProcessors();
        if (cores <= 1) {
            System.out.println("Available Cores \"" + cores + "\", Attempting to set Parallelism Flag");
            System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "1");
            System.out.println("Parallelism Set!");
        }
    }

    public static JDA getJDA() {
        return provider.get().getJDA();
    }

    public static void log(String tag, Object t) {
        System.out.println("[" + tag + "]-> " + t);
    }

    public static void warn(Object message) {
        log("WARN", message);
    }

    public static void info(Object message) {
        log("INFO", message);
    }

    public static void error(Object message) {
        log("ERROR", message);
    }

    public static void debug(Object message) {
        log("DEBUG", message);
    }

    public static void shutdown() {
        System.out.println("Terminating the bot instance");
        getJDA().getPresence().setStatus(OnlineStatus.OFFLINE);
        getJDA().shutdown();
        System.exit(1);
    }

    @Override
    public void onReady(@NonNull ReadyEvent e) {
        e.getJDA().updateCommands().queue();
        System.out.println(e.getJDA().getSelfUser().getAsTag() + Core.get().botOnReadyMessage);
        info("Bot has Started: Active Monitoring");
    }
}
