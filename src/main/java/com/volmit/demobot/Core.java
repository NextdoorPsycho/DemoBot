package com.volmit.demobot;

import art.arcane.quill.cache.AtomicCache;
import art.arcane.quill.execution.J;
import art.arcane.quill.io.FileWatcher;
import art.arcane.quill.io.IO;
import art.arcane.quill.json.JSONObject;
import art.arcane.quill.logging.L;
import com.google.gson.Gson;
import com.volmit.demobot.util.io.Range;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.File;
import java.io.IOException;

public class Core extends ListenerAdapter {


    // Set from config
    public String botOnReadyMessage = "Bot has Started";
    public String botActivityMessage = "The Universe: .?";
    public String botInstanceMessage = "Watching for Messages!";
    public String botToken = ""; // LEAVE BLANK FOR TOKEN
    public String botCompany = "VolmitSoftware";
    public String discordGuildID = "";
    public String botColor = "0x000000";
    public String botOwnerID = "PutYourIdHere"; // ME
    public String botIMG = "https://i.imgur.com/TpCn8vW.png"; // Cat pic
    public String botPrefix = ".";
    public String adminControllerRole = "Administrator";
    public String supportControllerRole = "Support";
    public Range xpPerMessage = Range.jitter(0.85f, 0.15f);
    public double xpBaseMultiplier = 2.13d;

    // Set from main class
    public transient Long botID;
    public transient User botUser;
    public transient String botName;

    // Used for hot-loading and config
    private static final FileWatcher fw = new FileWatcher(getFile());
    private static AtomicCache<Core> instance = new AtomicCache<>();

    public void save() {
        File file = getFile();
        file.getParentFile().mkdirs();
        J.attempt(() -> IO.writeAll(file, new JSONObject(new Gson().toJson(this)).toString(4)));
    }

    public static void tick() {
        if (fw.checkModified()) {
            instance = new AtomicCache<>();
            L.v("Hot-loaded Config");
            Demo.getJDA();
        }
    }

    public static Core get() {
        return instance.aquire(() -> {
            File f = getFile();
            System.out.println("Config File location:\n" + f.getAbsolutePath());
            f.getParentFile().mkdirs();
            Core dummy = new Core();

            if (!f.exists()) {
                dummy.save();
            }
            try {
                Core tk = new Gson().fromJson(IO.readAll(f), Core.class);
                tk.save();
                fw.checkModified();
                return tk;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return dummy;
        });
    }

    private static File getFile() {
        return new File("Data/Config.json");
    }


}
