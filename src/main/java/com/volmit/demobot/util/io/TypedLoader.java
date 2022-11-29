 
package com.volmit.demobot.util.io;

import art.arcane.quill.collections.KMap;
import art.arcane.quill.math.M;
import com.volmit.demobot.Demo;

import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;


public class TypedLoader<T extends DataType> {
    private final Class<T> type;
    private final KMap<Long, T> cache;
    private final KMap<Long, Long> lastUse;
    private final ReentrantLock lock;
    private final StorageAccess storage;

    public TypedLoader(Class<T> type, StorageAccess storage) {
        this.storage = storage;
        this.type = type;
        lock = new ReentrantLock();
        cache = new KMap<>();
        lastUse = new KMap<>();
    }

    public int size() {
        return cache.size();
    }

    public void cleanup(long olderThan) {
        lock.lock();

        for (Long i : lastUse.k()) {
            if (olderThan <= 0 || M.ms() - lastUse.get(i) > olderThan) {
                unload(i);
            }
        }

        lock.unlock();
    }

    private synchronized void unload(Long i) {
        lastUse.remove(i);
        T m = cache.remove(i);
        storage.set(type.getSimpleName().toLowerCase(Locale.ROOT), i, storage.toString(m));
        Demo.info("Saved " + type.getSimpleName() + "[" + i + "]");
    }

    public T get(long key) {
        lock.lock();
        lastUse.compute(key, (k, v) -> M.ms());
        T t = cache.get(key);

        if (t == null) {
            t = cache.computeIfAbsent(key, (v) -> {
                if (storage.exists(type.getSimpleName().toLowerCase(Locale.ROOT), key)) {
                    Demo.info("Loaded " + type.getSimpleName() + "[" + key + "]");
                    return storage.fromString(storage.get(type.getSimpleName().toLowerCase(Locale.ROOT), key), type);
                }

                try {
                    T x = type.getConstructor().newInstance();
                    x.id(key);
                    Demo.info("Created " + type.getSimpleName() + "[" + key + "]");
                    return x;
                } catch (Throwable e) {
                    e.printStackTrace();
                }

                return null;
            });
        }

        lock.unlock();
        return t;
    }
}
