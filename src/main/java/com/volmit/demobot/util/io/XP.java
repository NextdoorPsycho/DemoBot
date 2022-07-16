
package com.volmit.demobot.util.io;


import com.volmit.demobot.Core;

public class XP {
    public static double getExponent() {
        // Higher values take exponentially more xp per level increase.
        // Lower values make more consistent leveling. I.e. lvl 1 > 2 takes a little less than lvl 34 > 35
        return Core.get().xpBaseMultiplier;
    }
    public static double getXpForLevel(double level) {
        return Math.pow(level, getExponent());
    }
    public static int getLevelForXp(double xp) {
        return (int) Math.floor(Math.pow(xp, 1D / getExponent()));
    }
}
