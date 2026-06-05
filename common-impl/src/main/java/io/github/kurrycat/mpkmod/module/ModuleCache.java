package io.github.kurrycat.mpkmod.module;

import io.github.kurrycat.mpkmod.api.loader.ModPlatform;
import io.github.kurrycat.mpkmod.util.FileUtil;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ModuleCache {
    private static final String INTERNAL_MODULE_DIR = ".internal-modules";
    private static final String MODULE_CACHE_DIR = ".module-cache";

    private static final String MODULES_DIR = "mpkmodules";

    public static void extractInternalModules() {
        Path internalModDir = FileUtil.findPath(ModPlatform.HANDLE.get().rootPaths(), MODULES_DIR);
        if (internalModDir == null) {
            ModuleRegistryImpl.LOGGER.info("No internal modules found, skipping extraction");
            return;
        }
        Path internalModuleDir = getInternalModuleDir();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(internalModDir)) {
            for (Path path : stream) {
                if (!Files.isRegularFile(path) || !path.toString().endsWith(".jar")) {
                    continue;
                }
                Path targetPath = FileUtil.resolve(internalModuleDir, path.getFileName());
                Files.copy(path, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static CachedModule getOrCreateCachedModule(DiscoveredModule module) throws ModuleLoadException {
        Path source = module.source();
        String sourceHash = module.sourceHash();

        Path cachedPath = FileUtil.resolve(getCache(), source.getFileName());
        if (!Files.exists(cachedPath)) {
            return writeCachedModule(source, cachedPath, module);
        }

        try {
            String hash = FileUtil.getOrCreateSha256Sum(cachedPath);
            if (hash.equals(sourceHash)) return new CachedModule(cachedPath, sourceHash, module.entry());
        } catch (IOException e) {
            return writeCachedModule(source, cachedPath, module);
        }
        return writeCachedModule(source, cachedPath, module);
    }

    private static CachedModule writeCachedModule(Path source, Path cachedPath, DiscoveredModule module) throws ModuleLoadException {
        try {
            Files.copy(source, cachedPath, StandardCopyOption.REPLACE_EXISTING);
            String hash = FileUtil.createSha256Sum(cachedPath);
            if (!hash.equals(module.sourceHash())) {
                throw new ModuleLoadException("Module hash mismatch after copying: " + source);
            }
            return new CachedModule(cachedPath, module.sourceHash(), module.entry());
        } catch (Exception e) {
            throw new ModuleLoadException("Failed to copy module to cache: " + source, e);
        }
    }

    public static Path getModulesDir() {
        return FileUtil.resolve(ModPlatform.HANDLE.get().gamePath(), MODULES_DIR);
    }

    public static Path getInternalModuleDir() {
        return FileUtil.createHiddenDir(
                FileUtil.resolve(getModulesDir(), INTERNAL_MODULE_DIR)
        );
    }

    public static Path getCache() {
        return FileUtil.createHiddenDir(
                FileUtil.resolve(getModulesDir(), MODULE_CACHE_DIR)
        );
    }
}
