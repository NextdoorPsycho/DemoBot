 
package com.volmit.demobot.util.io;

import com.google.gson.Gson;


public interface StorageAccess {
    boolean exists(String typeName, Long key);
    void delete(String typeName, Long key);
    void set(String typeName, Long key, String data);
    String get(String typeName, Long key);
    default String toString(Object type) {
        return new Gson().toJson(type);
    }
    default <T> T fromString(String data, Class<T> t) {
        return new Gson().fromJson(data, t);
    }
}
