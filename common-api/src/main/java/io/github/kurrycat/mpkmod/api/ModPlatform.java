package io.github.kurrycat.mpkmod.api;

import io.github.kurrycat.mpkmod.api.log.ILogger;
import io.github.kurrycat.mpkmod.api.log.LogManager;
import io.github.kurrycat.mpkmod.api.minecraft.IFileEnv;
import io.github.kurrycat.mpkmod.api.minecraft.IModInfo;
import io.github.kurrycat.mpkmod.api.module.ModuleRegistry;
import io.github.kurrycat.mpkmod.api.service.ServiceHandle;
import io.github.kurrycat.mpkmod.api.service.Services;

public interface ModPlatform {
    ServiceHandle<ModPlatform> HANDLE = Services.getHandle(ModPlatform.class);

    ILogger LOGGER = LogManager.HANDLE.get().createLogger(HANDLE.get().modInfo().modId());

    static void init() {
        ModPlatform modPlatform = HANDLE.get();
        IModInfo modInfo = modPlatform.modInfo();
        LOGGER.info("Initializing {} ModPlatform for loader \"{}\"", modInfo.modName(), modInfo.modLoader());

        ModuleRegistry.HANDLE.get().loadAllModules();
    }

    IModInfo modInfo();

    IFileEnv fileEnv();
}
