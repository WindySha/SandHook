package com.swift.sandhook.xposedcompat.utils;

import com.android.dx.DexMaker;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import dalvik.system.DexClassLoader;

public final class DexMakerHelper {

    private static final String DEX_ENTRY_NAME = "classes.dex";

    private DexMakerHelper() {
    }

    public static ClassLoader loadClassDirect(ClassLoader parent, File dexCache, String dexFileName) {
        File dexJar = new File(dexCache, dexFileName);
        if (dexJar.exists()) {
            return createClassLoader(dexJar, dexCache, parent);
        }
        return null;
    }

    public static ClassLoader generateAndLoad(DexMaker dexMaker, ClassLoader parent, File dexCache,
                                              String dexFileName) throws IOException {
        if (dexCache == null) {
            throw new IllegalArgumentException("dexCache must not be null");
        }

        File dexJar = new File(dexCache, dexFileName);
        if (dexJar.exists()) {
            deleteOldDex(dexJar);
        }

        File parentDir = dexJar.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        byte[] dex = dexMaker.generate();
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

        return createClassLoader(dexJar, dexCache, parent);
    }

    private static ClassLoader createClassLoader(File dexJar, File optimizedDir, ClassLoader parent) {
        return new DexClassLoader(
                dexJar.getPath(),
                optimizedDir.getAbsolutePath(),
                null,
                parent);
    }

    private static void deleteOldDex(File dexFile) {
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
