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

    @Getter
    private static DataLoader loader;
    public static JDA getJDA() {
        return provider.get().getJDA();
    }
    public static final IBotProvider provider = new BotProvider();
    private static void log(String tag, Object t) {
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



    public static void main(String[] args) {
        //This sets the threading priorities to work on Droplet :( This is not safe but it works
        final int cores = Runtime.getRuntime().availableProcessors();
        if (cores <= 1) {
            System.out.println("Available Cores \"" + cores + "\", Attempting to set Parallelism Flag");
            System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "1");
            System.out.println("Parallelism Set!");
        }

        loader = new DataLoader(new FileSystemStorageAccess(new File("Data/BotData")));
        System.out.println("Initializing");

        Core.get().botID = getJDA().getSelfUser().getIdLong();
        Core.get().botUser = getJDA().getUserById(Core.get().botID);
        Core.get().botName = Objects.requireNonNull(Core.get().botUser).getName();
        ListenerRegistry.All(getJDA()); // ALL COMMANDS ARE HERE

        new Looper() {
            @Override
            protected long loop() {
                Core.tick();
                return 1000;
            }
        }.start();
        Runtime.getRuntime().addShutdownHook(new Thread(loader::close));
    }


    @Override
    public void onReady(@NonNull ReadyEvent e) {
        e.getJDA().updateCommands().queue();
        System.out.println(e.getJDA().getSelfUser().getAsTag()+ Core.get().botOnReadyMessage);
        info("Bot has Started: Active Monitoring");
    }

    public static void shutdown() {
        System.out.println("Terminating the bot instance");
        getJDA().getPresence().setStatus(OnlineStatus.OFFLINE);
        getJDA().shutdown();
        System.exit(1);
    }


}
