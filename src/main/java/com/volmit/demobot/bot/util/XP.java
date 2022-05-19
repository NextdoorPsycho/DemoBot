/*
 * Abyssalith is a Discord Bot for Volmit Software's Community
 * Copyright (c) 2021 VolmitSoftware (Arcane Arts)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.volmit.demobot.bot.util;


import com.volmit.demobot.bot.Core;

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
