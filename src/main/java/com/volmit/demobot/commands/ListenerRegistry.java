 
package com.volmit.demobot.commands;

import com.volmit.demobot.Core;
import com.volmit.demobot.Demo;
import com.volmit.demobot.commands.prefix.Passive;
import com.volmit.demobot.commands.prefix.Shutdown;
import com.volmit.demobot.commands.slash.TicketMasterButton;
import com.volmit.demobot.util.instance.SkipCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SkipCommand
public class ListenerRegistry extends ListenerAdapter {
    /**
     * Command package path. Recursively searched for commands not annotated by {@link SkipCommand}
     */
    private static final String commandPackagePath = ListenerRegistry.class.getPackage().getName();


    /**
     * All Slash commands:
     * {@link com.volmit.demobot.util.instance.BotInstance}
     * need to be added like this:
     * builder.addSlashCommand(new PingCommand());
     */
    public static void All(JDA jda) {
        jda.addEventListener(new Demo()); // [ DONT TOUCH THESE  LISTENERS ]
        jda.addEventListener(new Core()); // [ DONT TOUCH THESE  LISTENERS ]
        jda.addEventListener(new Shutdown()); // [ DONT TOUCH THESE  LISTENERS ]
        jda.addEventListener(new Passive());

        //Listeners
        jda.addEventListener(new TicketMasterButton());

        //END
        jda.addEventListener(new CommandCore(jda)); // [ DONT TOUCH THESE  LISTENERS ]
    }


    private static void registerAllCommands(String packagePath, JDA jda) throws NullPointerException, IOException {

        // Get stream of class data
        InputStream stream = ClassLoader.getSystemClassLoader()
                .getResourceAsStream(packagePath.replaceAll("[.]", "/"));

        if (stream == null) {
            throw new NullPointerException("Command loading, package not found: " + packagePath);
        }

        List<String> loadedCommands = new ArrayList<>();
        new BufferedReader(new InputStreamReader(stream))
                .lines()
                .filter(line -> {
                    if (line.endsWith(".class")) {
                        return true;
                    }

                    if (!line.contains(".")) {
                        try {
                            registerAllCommands(packagePath + "." + line, jda);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return false;
                })
                .map(line -> getCommandClass(line, packagePath))
                .filter(Objects::nonNull)
                .filter(c -> !c.isAnnotationPresent(SkipCommand.class))
                .map(cmdClass -> {
                    try {
                        return (ListenerAdapter) cmdClass.getConstructors()[0].newInstance();
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
                        e.printStackTrace();
                        Demo.debug("Failed to load command " + cmdClass.getName() + " with empty constructor!");
                    } catch (ArrayIndexOutOfBoundsException e) {
                        e.printStackTrace();
                        Demo.debug("Failed to load command " + cmdClass.getName() + " due to no constructor being present!");
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .forEach(c -> {
                    jda.addEventListener(c);
                    loadedCommands.add(c.getClass().getSimpleName());
                });
        stream.close();
        Demo.debug("Loaded " + (loadedCommands.isEmpty() ? "NONE" : String.join(", ", loadedCommands)) + " from package " + packagePath);
    }


    private static Class<?> getCommandClass(String className, String packageName) {
        try {
            Class<?> c = Class.forName(packageName + "."
                    + className.substring(0, className.lastIndexOf('.')));
            if (c.isAssignableFrom(ListenerAdapter.class)) {
                Demo.debug("Unable to load class: " + c.getName() + " because it does not extend ListenerAdapter");
            }
            return c;
        } catch (ClassNotFoundException e) {
            // handle the exception
        }
        return null;
    }
}
