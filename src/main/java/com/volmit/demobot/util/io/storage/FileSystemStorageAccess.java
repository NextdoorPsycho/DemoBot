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
package com.volmit.demobot.util.io.storage;

import art.arcane.quill.io.IO;
import com.volmit.demobot.Demo;
import com.volmit.demobot.util.io.StorageAccess;


import java.io.File;
import java.io.IOException;


public class FileSystemStorageAccess implements StorageAccess {
    private final File root;

    public FileSystemStorageAccess(File root) {
        this.root = root;
        Demo.info("Created Storage Access (File System) in " + root.getAbsolutePath());
    }

    private File fileFor(String typeName, Long key) {
        File f = new File(root, typeName + "/" + key + ".json");
        f.getParentFile().mkdirs();
        return f;
    }

    @Override
    public boolean exists(String typeName, Long key) {
        return fileFor(typeName, key).exists();
    }

    @Override
    public void delete(String typeName, Long key) {
        fileFor(typeName, key).delete();
    }

    @Override
    public void set(String typeName, Long key, String data) {
        try {
            IO.writeAll(fileFor(typeName, key), data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String get(String typeName, Long key) {
        try {
            return IO.readAll(fileFor(typeName, key));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
