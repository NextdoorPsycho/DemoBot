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
