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
import art.arcane.quill.execution.Looper;
import com.volmit.demobot.util.io.data.Message;
import com.volmit.demobot.util.io.data.User;


public class DataLoader {
    private final StorageAccess storage;
    private final KMap<Class<? extends DataType>, TypedLoader<?>> loaders = new KMap<>();
    private final Looper cleaner = new Looper() {
        @Override
        protected long loop() {
            cleanup(10000);
            return 1000;
        }
    };

    public DataLoader(StorageAccess storage) {
        this.storage = storage;
        cleaner.start();
        registerLoader(User.class);
        registerLoader(Message.class);
    }

    public User getUser(long id) {
        return getLoader(User.class).get(id);
    }

    public Message getMessage(long id) {
        return getLoader(Message.class).get(id);
    }

    private <T extends DataType> void registerLoader(Class<T> c) {
        loaders.put(c, new TypedLoader<>(c, storage));
    }

    public void cleanup(long olderThan) {
        for (TypedLoader<?> i : loaders.values()) {
            i.cleanup(olderThan);
        }
    }

    public void close() {
        cleanup(-1);
        cleaner.interrupt();
    }

    public <T extends DataType> TypedLoader<T> getLoader(Class<T> t) {
        return (TypedLoader<T>) loaders.get(t);
    }
}
