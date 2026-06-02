package com.swift.sandhook.xposedcompat.utils;

import android.os.Build;

import com.android.dx.DexMaker;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import dalvik.system.DexClassLoader;
import dalvik.system.InMemoryDexClassLoader;

public final class DexMakerHelper {

    private static final String DEX_ENTRY_NAME = "classes.dex";

    private DexMakerHelper() {
    }

    public static ClassLoader loadClassDirect(ClassLoader parent, File dexCache, String dexFileName) {
        File dexJar = new File(dexCache, dexFileName);
        if (!dexJar.exists()) {
            return null;
        }
        if (!ensureReadOnlyDexFile(dexJar)) {
            return null;
        }
        try {
            return createClassLoader(dexJar, dexCache, parent);
        } catch (SecurityException e) {
            return null;
        }
    }

    public static ClassLoader generateAndLoad(DexMaker dexMaker, ClassLoader parent, File dexCache,
                                              String dexFileName) throws IOException {
        if (dexCache == null) {
            throw new IllegalArgumentException("dexCache must not be null");
        }

        byte[] dex = dexMaker.generate();
        try {
            return writeDexJarAndLoad(dex, parent, dexCache, dexFileName);
        } catch (SecurityException e) {
            return loadInMemory(dex, parent);
        }
    }

    private static ClassLoader writeDexJarAndLoad(byte[] dex, ClassLoader parent, File dexCache,
                                                  String dexFileName) throws IOException {
        File dexJar = new File(dexCache, dexFileName);
        if (dexJar.exists()) {
            deleteOldDex(dexJar);
        }

        File parentDir = dexJar.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        JarOutputStream jarOut = new JarOutputStream(
                new BufferedOutputStream(new FileOutputStream(dexJar)));
        try {
            JarEntry entry = new JarEntry(DEX_ENTRY_NAME);
            entry.setSize(dex.length);
            jarOut.putNextEntry(entry);
            jarOut.write(dex);
            jarOut.closeEntry();
        } finally {
            jarOut.close();
        }

        if (!ensureReadOnlyDexFile(dexJar)) {
            throw new SecurityException("Failed to mark dex file read-only: " + dexJar);
        }

        return createClassLoader(dexJar, dexCache, parent);
    }

    private static ClassLoader loadInMemory(byte[] dex, ClassLoader parent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new InMemoryDexClassLoader(ByteBuffer.wrap(dex), parent);
        }
        throw new UnsupportedOperationException(
                "In-memory dex loading requires Android O+, current SDK=" + Build.VERSION.SDK_INT);
    }

    private static boolean ensureReadOnlyDexFile(File dexJar) {
        if (!dexJar.exists()) {
            return false;
        }
        if (!dexJar.canWrite()) {
            return true;
        }
        return dexJar.setReadOnly();
    }

    private static ClassLoader createClassLoader(File dexJar, File optimizedDir, ClassLoader parent) {
        return new DexClassLoader(
                dexJar.getPath(),
                optimizedDir.getAbsolutePath(),
                null,
                parent);
    }

    private static void deleteOldDex(File dexFile) {
        dexFile.setWritable(true);
        dexFile.delete();
        File oatDir = new File(dexFile.getParentFile(), "oat");
        if (!oatDir.exists()) {
            return;
        }
        String namePrefix = dexFile.getName().replace(".jar", "");
        deleteOatFiles(oatDir, namePrefix);
        deleteOatFiles(new File(oatDir, "arm"), namePrefix);
        deleteOatFiles(new File(oatDir, "arm64"), namePrefix);
    }

    private static void deleteOatFiles(File dir, String namePrefix) {
        if (!dir.exists()) {
            return;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.getName().startsWith(namePrefix)) {
                file.delete();
            }
        }
    }
}
