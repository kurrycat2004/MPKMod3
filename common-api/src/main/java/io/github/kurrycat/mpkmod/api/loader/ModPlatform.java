package io.github.kurrycat.mpkmod.api.loader;

import io.github.kurrycat.mpkmod.api.App;
import io.github.kurrycat.mpkmod.api.log.ILogger;
import io.github.kurrycat.mpkmod.api.module.ModuleRegistry;
import io.github.kurrycat.mpkmod.api.service.ServiceHandle;
import io.github.kurrycat.mpkmod.api.service.Services;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.List;

public interface ModPlatform {
    ServiceHandle<ModPlatform> HANDLE = Services.getHandle(ModPlatform.class);

    ILogger LOGGER = ILogger.createLogger(ModPlatform.class.getSimpleName());

    default void init() {
        LOGGER.info("Initializing {} ({}) ModPlatform for loader({}), mc_version({})",
                App.name(), App.id(), loader(), mcVersion()
        );

        ModuleRegistry.HANDLE.get().loadAllModules();
    }

    String loader();

    String mcVersion();

    /**
     * Get the path to the game directory.
     *
     * @return the path to the game directory
     */
    Path gamePath();

    /**
     * Get the path to the game's config directory.
     *
     * @return the path to the game's config directory
     */
    Path gameConfigPath();

    /**
     * Get the path to the suggested mod's config directory.
     * <p> The returned path is not guaranteed to exist!
     *
     * @return the path to the mod's config directory
     */
    default Path modConfigPath() {
        return gameConfigPath().resolve(App.id() + ".cfg");
    }

    /**
     * Get the root paths of the mod. <br>
     * <strong>Note:</strong> This should return the root path of the mod jar, not its path
     *
     * @return the root path of the mod
     * @see FileSystem#getRootDirectories()
     */
    List<Path> rootPaths();
}
