/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.fml.loading.moddiscovery;

import com.mojang.logging.LogUtils;
import cpw.mods.jarhandling.JarContentsBuilder;
import cpw.mods.jarhandling.SecureJar;
import net.neoforged.fml.loading.LogMarkers;
import net.neoforged.neoforgespi.language.IConfigurable;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.locating.IModFile;
import net.neoforged.neoforgespi.locating.IModLocator;
import net.neoforged.neoforgespi.locating.IModProvider;
import net.neoforged.neoforgespi.locating.ModFileLoadingException;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public abstract class AbstractModProvider implements IModProvider
{
    private static final   Logger LOGGER    = LogUtils.getLogger();
    protected static final String MODS_TOML = "META-INF/mods.toml";
    protected static final String MANIFEST = "META-INF/MANIFEST.MF";

    protected IModLocator.ModFileOrException createMod(Path... path) {
        var jarContents = new JarContentsBuilder()
                .paths(path)
                .build();

        IModFile mod;
        var type = jarContents.getManifest().getMainAttributes().getValue(ModFile.TYPE);
        if (type == null) {
            type = getDefaultJarModType();
        }
        if (jarContents.findFile(MODS_TOML).isPresent()) {
            LOGGER.debug(LogMarkers.SCAN, "Found {} mod of type {}: {}", MODS_TOML, type, path);
            var mjm = new ModJarMetadata(jarContents);
            mod = new ModFile(SecureJar.from(jarContents, mjm), this, ModFileParser::modsTomlParser);
            mjm.setModFile(mod);
        } else if (type != null) {
            LOGGER.debug(LogMarkers.SCAN, "Found {} mod of type {}: {}", MANIFEST, type, path);
            mod = new ModFile(SecureJar.from(jarContents), this, this::manifestParser, type);
        } else {
            return new IModLocator.ModFileOrException(null, new ModFileLoadingException("Invalid mod file found "+ Arrays.toString(path)));
        }

        return new IModLocator.ModFileOrException(mod, null);
    }

    protected IModFileInfo manifestParser(final IModFile mod) {
        Function<String, Optional<String>> cfg = name -> Optional.ofNullable(mod.getSecureJar().moduleDataProvider().getManifest().getMainAttributes().getValue(name));
        var license = cfg.apply("LICENSE").orElse("");
        var dummy = new IConfigurable() {
            @Override
            public <T> Optional<T> getConfigElement(String... key) {
                return Optional.empty();
            }
            @Override
            public List<? extends IConfigurable> getConfigList(String... key) {
                return Collections.emptyList();
            }
        };

        return new DefaultModFileInfo(mod, license, dummy);
    }

    @Override
    public boolean isValid(final IModFile modFile) {
        return true;
    }

    protected String getDefaultJarModType() {
        return null;
    }

    private record DefaultModFileInfo(IModFile mod, String license, IConfigurable configurable) implements IModFileInfo, IConfigurable {
        @Override
        public <T> Optional<T> getConfigElement(final String... strings)
        {
            return Optional.empty();
        }

        @Override
        public List<? extends IConfigurable> getConfigList(final String... strings)
        {
            return null;
        }

        @Override public List<IModInfo> getMods() { return Collections.emptyList(); }
        @Override public List<LanguageSpec> requiredLanguageLoaders() { return Collections.emptyList(); }
        @Override public boolean showAsResourcePack() { return false; }
        @Override public Map<String, Object> getFileProperties() { return Collections.emptyMap(); }
        @Override
        public String getLicense() { return license; }

        @Override public IModFile getFile() { return mod; }
        @Override public IConfigurable getConfig() { return configurable; }

        // These Should never be called as it's only called from ModJarMetadata.version and we bypass that
        @Override public String moduleName() { return mod.getSecureJar().name(); }
        @Override public String versionString() { return null; }
        @Override public List<String> usesServices() { return null; }

        @Override
        public String toString() {
            return "IModFileInfo(" + mod.getFilePath() + ")";
        }
    }
}
