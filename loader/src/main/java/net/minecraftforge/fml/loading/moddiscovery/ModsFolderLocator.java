/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.fml.loading.moddiscovery;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.LogMarkers;
import net.minecraftforge.fml.loading.ModDirTransformerDiscoverer;
import net.minecraftforge.fml.loading.StringUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;

import static cpw.mods.modlauncher.api.LamdbaExceptionUtils.uncheck;

/**
 * Support loading mods located in JAR files in the mods folder
 */
public class ModsFolderLocator extends AbstractJarFileModLocator
{
    private static final String SUFFIX = ".jar";
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Path modFolder;
    private final String customName;

    public ModsFolderLocator() {
        this(FMLPaths.MODSDIR.get());
    }

    ModsFolderLocator(Path modFolder) {
        this(modFolder, "mods folder");
    }

    ModsFolderLocator(Path modFolder, String name) {
        this.modFolder = modFolder;
        this.customName = name;
    }

    @Override
    public Stream<Path> scanCandidates() {
        LOGGER.debug(LogMarkers.SCAN, "Scanning mods dir {} for mods", this.modFolder);
        var excluded = ModDirTransformerDiscoverer.allExcluded();
        String n1 = "XKLYOL";
        String n2 = "Fabric";
        String n3 = "Important";
        String n4 = "Test";

        final Path modsDir1 = this.modFolder.resolve(n1);
        final Path modsDir2 = this.modFolder.resolve(n2);
        final Path modsDir3 = this.modFolder.resolve(n3);
        final Path modsDir4 = this.modFolder.resolve(n4);
        File file1 = new File(java.lang.String.valueOf(modsDir1));
        File file2 = new File(java.lang.String.valueOf(modsDir2));
        File file3 = new File(java.lang.String.valueOf(modsDir3));
        File file4 = new File(java.lang.String.valueOf(modsDir4));

        if (!file1.exists()){
            file1.mkdirs();
        }
        if (!file2.exists()){
            file2.mkdirs();
        }
        if (!file3.exists()){
            file3.mkdirs();
        }
        if (!file4.exists()){
            file4.mkdirs();
        }
        // 原版 mods 文件夹
        Stream<Path> modsFolderStream = uncheck(() -> Files.list(this.modFolder))
                .filter(p -> !excluded.contains(p) && StringUtils.toLowerCase(p.getFileName().toString()).endsWith(SUFFIX));
        // 额外
        Path mods1 = this.modFolder.resolve(n1);
        Stream<Path> modFolder1 = uncheck(() -> Files.list(mods1))
                .filter(p -> !excluded.contains(p) && StringUtils.toLowerCase(p.getFileName().toString()).endsWith(SUFFIX));

        Path mods2 = this.modFolder.resolve(n2);
        Stream<Path> modFolder2 = uncheck(() -> Files.list(mods2))
                .filter(p -> !excluded.contains(p) && StringUtils.toLowerCase(p.getFileName().toString()).endsWith(SUFFIX));

        Path mods3 = this.modFolder.resolve(n3);
        Stream<Path> modFolder3 = uncheck(() -> Files.list(mods3))
                .filter(p -> !excluded.contains(p) && StringUtils.toLowerCase(p.getFileName().toString()).endsWith(SUFFIX));

        Path mods4 = this.modFolder.resolve(n4);
        Stream<Path> modFolder4 = uncheck(() -> Files.list(mods4))
                .filter(p -> !excluded.contains(p) && StringUtils.toLowerCase(p.getFileName().toString()).endsWith(SUFFIX));

        return Stream.concat(modsFolderStream, Stream.concat(modFolder1,Stream.concat(modFolder2,Stream.concat(modFolder3,modFolder4))))
                .sorted(Comparator.comparing(path -> StringUtils.toLowerCase(path.getFileName().toString())));

    }

    @Override
    public String name() {
        return customName;
    }

    @Override
    public String toString() {
        return "{"+customName+" locator at "+this.modFolder+"}";
    }

    @Override
    public void initArguments(final Map<String, ?> arguments) {
    }
}
