package io.github.kurrycat.mpkmod.module;

import io.github.kurrycat.mpkmod.api.module.ModuleEntrypoint;

import java.nio.file.Path;

public record LoadedModule(
        Path root,
        String sourceHash,
        ModuleEntry entry,
        ClassLoader classLoader,
        ModuleEntrypoint moduleInstance
) {
    public boolean matchesExactly(DiscoveredModule module) {
        return !module.isError() && sourceHash.equals(module.sourceHash());
    }
}
