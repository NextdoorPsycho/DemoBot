 
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
