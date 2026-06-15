package io.github.kurrycat.mpkmod.module;

import io.github.kurrycat.mpkmod.api.log.ILogger;
import io.github.kurrycat.mpkmod.api.module.IModuleEntry;
import io.github.kurrycat.mpkmod.api.module.ModuleEntrypoint;

import java.nio.file.Path;

public record LoadedModule(
        Path root,
        String sourceHash,
        ModuleEntry entry,
        ClassLoader classLoader,
        ModuleEntrypoint moduleInstance
) implements ModuleEntrypoint {
    public boolean matchesExactly(DiscoveredModule module) {
        return !module.isError() && sourceHash.equals(module.sourceHash());
    }

    @Override
    public void onLoad(IModuleEntry entry, ILogger logger) {
        moduleInstance.onLoad(entry, logger);
    }

    @Override
    public void onUnload() {
        moduleInstance.onUnload();
    }

    @Override
    public void onFrame() {
        moduleInstance.onFrame();
    }
}
