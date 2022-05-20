 
package com.volmit.demobot.util.io.storage;

import com.volmit.demobot.util.io.StorageAccess;
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
