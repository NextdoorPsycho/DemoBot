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
package com.volmit.demobot.bot.util.io.storage;

import com.volmit.demobot.bot.util.io.StorageAccess;
import redis.clients.jedis.Jedis;


public class RedisStorageAccess implements StorageAccess {
    private final Jedis jedis;

    public RedisStorageAccess(String address, int port, String password) {
        jedis = new Jedis(address, port);
        jedis.auth(password);


        if (!jedis.isConnected()) {
            throw new RuntimeException("Reids Connection Failure!");
        }
    }

    private String keyFor(String typeName, Long key) {
        return typeName + ":" + key;
    }

    @Override
    public boolean exists(String typeName, Long key) {
        return jedis.exists(keyFor(typeName, key));
    }

    @Override
    public void delete(String typeName, Long key) {
        jedis.del(keyFor(typeName, key));
    }

    @Override
    public void set(String typeName, Long key, String data) {
        jedis.set(keyFor(typeName, key), data);
    }

    @Override
    public String get(String typeName, Long key) {
        return jedis.get(keyFor(typeName, key));
    }
}
